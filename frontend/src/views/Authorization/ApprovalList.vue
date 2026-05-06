<template>
    <div class="ApprovalList">
        <div class="page-header">
            <h2 class="page-title">审批列表</h2>
        </div>

        <ApprovalSearchTemplate
            ref="approvalSearchRef"
            :search-data="data.searchData"
            :item-per-row="2.5"
            :show-clear-btn="true"
            :show-more-search-btn="true"
            :more-search-data="data.moreSearchData"
            :default-index-array="data.defaultIndexArray"
            @search-click="handleSearch"/>
        <ApprovalTableTemplate
            :table-data="data.tableData"
            :loading="isLoading"
            :fields-white-list="data.tableWhiteList"
            :translation-object="translationObject"
            :translation-field-list="translationFieldList"
            :show-head-radio-btn="true"
            :head-radio-fields="data.headRadioFields"
            :head-radio-default-value="0"
            @detail-click="handleDetail"
            @func-click="handleFuncMapping"
            @radio-change="handleRadioChange">
        </ApprovalTableTemplate>
        <ApprovalDialogTemplate
            ref="approvalDialogRef"
            :dialog-visible="data.dialogVisible"
            :dialog-title="data.dialogTitle"
            :application-info="data.applicationInfo"
            :form-display-fields="data.formDisplayFields"
            :form-rules="dialogFormRules"
            :show-descriptions="true"
            :descriptions="descriptions"
            @dialog-close="handleDialogClose"
            @delete-file="handleDeleteAllTemFile"
            @reset-click="handleResetBtnClick"
            @submit-click="handleSubmitBtnClick"
            @upload-success="handleUploadSuccess"
            @before-remove="handleBeforeRemoveFile">
            <template #divider_slot>
                <el-divider content-position="left" class="custom-divider create-template-divider">
                    组件预览
                </el-divider>
            </template>
        </ApprovalDialogTemplate>

        <div class="pagination-box">
            <el-pagination
                @size-change="GetApplicationList()"
                @current-change="GetApplicationList()"
                layout="total, sizes, prev, pager, next, jumper"
                background
                v-model:current-page="data.pageNum"
                v-model:page-size="data.pageSize"
                :page-sizes="[5, 10, 15, 20]"
                v-model:total="data.total" />
        </div>
    </div>
</template>
<script setup>
import { onMounted, ref, reactive } from 'vue';
import ApprovalDialogTemplate from './Components/ApprovalDialogTemplate.vue';
import ApprovalTableTemplate from './Components/ApprovalTableTemplate.vue';
import ApprovalSearchTemplate from './Components/ApprovalSearchTemplate.vue';
import request from '@/utils/request';
import { OptionBuilder } from './Utils/OptionBuilder';
import { AddApplyDataForDescriptions, BuildFormDescriptions, optionTypeMap } from './RelationMap';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useApprovalTimer } from './Utils/TimerComposables';
import { useRouter } from 'vue-router';


const data = reactive({
    pageNum: 1,
    pageSize: 10,
    total: 0,
    tableData: [],
    searchData: [],
    moreSearchData: [],
    defaultIndexArray: ['type', 'apply_account'],   //  保存的是name而不是key
    formDisplayFields: [],
    dialogVisible: false,
    dialogTitle: undefined,
    tableWhiteList: ['aid', 'apply_account', 'user_name', 'user_email', 'type'],
    applicationInfo: {},
    headRadioFields: [
        {
            'label': '代办',
            'value': 0
        },
        {
            'label': '全部',
            'value': 1
        }
    ],
    radioValue: 0
})

const approvalSearchRef = ref(null);
const approvalDialogRef = ref(null)
// 从optionTypeMap为可用的翻译目标,type字段需要翻译
const translationObject = ref(optionTypeMap);    //  译文对象
const translationFieldList = ref(['type']); //  翻译字段
const descriptions = ref({});   //  弹出框描述组件数据
let userID = null;
let filePathData = [];  //  弹出框Upload组件保存的文件路径
const isLoading = ref(true);
let token = ''; //  审批需要的验证身份的token
let isSubmited = false; //  是否通过“提交”的方式关闭弹出框，以判断是否需要UnLock操作
const { start: startTimer, stop: stopTimer } = useApprovalTimer(); //  计时器组合式函数
let apply_dataTem = {};

// 获取所有待审批事务
const GetApplicationList= async ()=>{

    isLoading.value = true;
    let inputValue = {};

    if (approvalSearchRef.value) {
        inputValue = approvalSearchRef.value.getInputValue();
    }

    const params = {
        pageNum: data.pageNum,
        pageSize: data.pageSize,
        is_All: data.radioValue
    };

    // 合并搜索框内容至参数
    if (inputValue && Object.keys(inputValue).length > 0) {
        Object.assign(params, inputValue);   
    }

    request.get("/approval/pending",{
        params:params,
        headers:{
            'X-Current-User-ID': userID,
        }
    }).then(res=>{
        // 对“申请类型”进行翻译
        // let obj = res.data.obj;
        // obj.forEach((value, key) => {
        //     obj[key].type = optionTypeMap[value.type];
        // });
        if (res.code === '200') {
            data.tableData = res.data.obj;
            data.total = res.data.total;
            data.pageNum = res.data.pageNum;
            data.pageSize = res.data.pageSize;
            setTimeout(()=>{
                isLoading.value=false;
            },1000)    
        } else if (res.code === '501') {
            ElMessage.error(res.msg);
            setTimeout(()=>{
                router.push('/manager');
            },3000);
        }
    })
}

// 获取搜索框动态渲染数据
const GetSearchRender = () => {
    // 一次性将所有可能用到的动态数据获取
    const searchParams = ["apply_account", "apply_type"];
    const moreSearchParams = ["apply_id"];

    request.post("/approval/render/search", searchParams).then(res => {
        // 按照searchParams顺序重新构建searchData对象
        const orderedData = {};
        data.defaultIndexArray.forEach(key => {
            if (res.data[key]) {
                orderedData[key] = res.data[key];
            }
        });
        data.searchData = orderedData;
    }).then(() => {
        request.get("/approval/render/typelist").then(res => {
            data.searchData.type.option = OptionBuilder(optionTypeMap, res.data);
        })
    })

    // 更多查询选项
    request.post("/approval/render/search", moreSearchParams).then(res => {
        data.moreSearchData = res.data;
    })
}

// 获取弹出框动态渲染数据，当点击弹出框时才调用
// 在完成组件的动态渲染之后，对该申请进行Lock操作
const GetDialogRender=async (templateName, objData)=>{
    try {
        const renderRes = await request.get("/approval/render/templates/"+templateName);
        data.formDisplayFields = renderRes.data;

        // 如果成功Lock则完成数据渲染
        const isLocked = await LockApplicationWorker(objData.rowData.aid);
        if (isLocked) {
            DialogPropsTransmit(objData);
        }
    } catch (error) {
        console.log('渲染弹出框数据时出错：',error);
    }
}

// Lock操作，锁定指定申请
const LockApplicationWorker=async (aid)=>{
    try {
        const res = await request.put("/approval/lock/"+aid,null,{
            headers: {
                'X-Current-User-ID': userID,
            }
        });
        if (res.code === '200' && res.data != null) {
            token = res.data;
            return true;
        }
        ElMessage.warning('锁定失败，不允许审批该申请');
        return false;
    } catch (error) {
        console.log('锁定资源时出错：',error);
        return false;
    }
}

// UnLock操作，解锁指定的申请，并销毁指定token
// 由于解锁操作后伴随着弹出框关闭后的刷新页面，因此需要等待完成解锁再刷新，否则会出现未刷新出被解锁申请的情况
const UnLockApplicationWorker=async (aid)=>{
    if (!token) {
        ElMessage.error('用户存在异常操作！');
        return;
    }
    await request.put("/approval/unlock/"+aid,null,{
            headers: {
                'X-Current-User-ID': userID,
                'X-Lock-Token': token,
            }
        });
}
// 提交审批结果，根据type处理完数据再发送给后端
const UpdateApplication=()=>{
    approvalDialogRef.value.formLoading = true;
    let formData = approvalDialogRef.value.formData;
    
    const params = {
        'aid': data.applicationInfo.aid,
        'approval': formData.approval_result,
        'type': data.applicationInfo.type,
        'formData': {...formData},
        'apply_data': {...apply_dataTem}
    }
    request.put('/approval/result',params,{
        headers: {
            'X-Current-User-ID': userID,
            'X-Lock-Token': token,
        }
    }).then(res=>{
        if (res.code === '200'){
            isSubmited = true;  //  标记为提交
            ElMessage.success('提交成功');
            approvalDialogRef.value.handleClose();
        }else {
            ElMessage.error('审批提交失败，请重试');
            approvalDialogRef.value.formLoading = false;
        }
    })
}

onMounted(()=>{
    userID = JSON.parse(window.localStorage.getItem('current_user')).id;
    const userRole = JSON.parse(window.localStorage.getItem('current_user')).role;
    if (userRole != '管理员') {
        ElMessage.error('检测越权访问，即将跳转页面');
        setTimeout(()=>{
            router.push('/manager');
        },3000);
    } else {
        GetApplicationList();
        GetSearchRender();
    }
})

/*
-----以下均为按钮点击事件+父子组件事件交互-----
*/
const handleSearch=()=>{
    data.pageNum = 1;
    GetApplicationList();
}


const handleRadioChange=(radioValue)=>{
    data.radioValue = radioValue;
    GetApplicationList();
}

const router = useRouter();

const handleDetail=(rowData)=>{

    sessionStorage.setItem('approvalDetail', JSON.stringify(rowData));

    router.push({
        name: '申请详情',
    })
}

// 点击“处理”按钮   objData.rowData为当前申请信息
const handleFuncMapping=(objData)=>{
    
    switch (objData.funcConfig.func){
        case 'handlePwdReset':
            handlePwdReset(objData);
            break;
        case 'handleDisableTemplate':
            handleDisableTemplate(objData);
            break;
        case 'handleResumeTemplate':
            handleResumeTemplate(objData);
            break;
        case 'handleCreateTemplate':
            handleCreateTemplate(objData);
            break;
        default:
            handleCustomTemplate(objData);
    }
}

const handleCustomTemplate=(objData)=>{
    GetDialogRender('SimpleApproval', objData);
}
const handlePwdReset=(objData)=>{
    GetDialogRender('ResetApproval', objData);
}
const handleDisableTemplate=(objData)=>{
    GetDialogRender('SimpleApproval', objData);
}
const handleResumeTemplate=(objData)=>{
    GetDialogRender('SimpleApproval', objData);
}
const handleCreateTemplate=async (objData)=>{
    await GetDialogRender('CreateApprovalTemplate', objData);
    const applyData = objData.rowData.apply_data;
    const { base_keys, relation_keys } = applyData;
    
    const res = await request.post("/approval/render/templates/keyList", {
        baseKeys: base_keys,
        relationKeys: relation_keys
    });
    const newFields = res.data;
    newFields.forEach((item, index) => {
        item.isRule = 0;
        const length = Object.keys(data.formDisplayFields).length;

        data.formDisplayFields = {
            ...data.formDisplayFields,
            [length]: newFields[index]
        }
    })
}


/*
以下为关于Dialog部分
*/
// 弹出框动态渲染组件指定类型的校验规则
const dialogFormRules = {
    input:[
        { required: true, message: '请输入!', trigger: 'blur' }
    ],
    select:[
        { required: true, message: '请选择!', trigger: 'change' }
    ],
    checkbox:[
        { type: 'array', required: true, message: '请选择!', trigger: 'change' }
    ]
}

// 显示弹出框组件并传递参数
const DialogPropsTransmit=(objData)=>{
    data.dialogVisible = true;
    data.dialogTitle = objData.funcConfig.name;
    data.applicationInfo = objData.rowData;
    approvalDialogRef.value.GetDisplayFields();

    // 需要展示哪些用户数据
    const keysToKeep = [ 'apply_account', 'user_name', 'user_email', 'apply_time' ];
    let descriptionsTem = {};
    descriptionsTem = BuildFormDescriptions(keysToKeep, objData.rowData);

    // 由于存在对象apply_data因此，根据申请类型的不同也要从apply_data展示不同的数据
    if (objData.rowData.apply_data) {
        objData.rowData.apply_data = JSON.parse(objData.rowData.apply_data);
        apply_dataTem = objData.rowData.apply_data;
        descriptionsTem = AddApplyDataForDescriptions(data.applicationInfo.type, descriptionsTem, objData.rowData.apply_data);
    }
    
    descriptions.value = descriptionsTem;

    // 开始计时，同时传入超时的回调函数
    startTimer({
        overFunction: () => overFunction(),
        aid: objData.rowData.aid,
        userID: userID,
        token: token
    })
}

const overFunction=()=>{
    ElMessageBox.alert("长时间未完成，已退出审批");
    approvalDialogRef.value.handleClose();
}

const handleDialogClose=async ()=>{
    // 停止计时
    stopTimer();

    data.dialogVisible = false;
    // 如果仍有残余的文件，则删除
    if (filePathData?.length > 0) {
        handleDeleteAllTemFile();
    }
    // 如果未提交申请，则解锁，并等待保证完成解锁再刷新
    if (!isSubmited) {
        await UnLockApplicationWorker(data.applicationInfo.aid);
    }
    apply_dataTem = {};
    token = '';
    filePathData = null;
    descriptions.value = {};
    isSubmited = false;
    GetApplicationList();
}

// 删除缓存的文件
const handleDeleteAllTemFile=()=>{
    request.post("/files/tem/delete",filePathData);
}

const handleResetBtnClick=()=>{
    ElMessageBox.confirm('确定重置表单？','确定').then(()=>{
        if (approvalDialogRef.value.filePathData) {
            handleDeleteAllTemFile(filePathData);
        }
        ElMessage.success('重置成功');
        approvalDialogRef.value.formRef.resetFields();
    }).catch(() => {});
}

const handleSubmitBtnClick=()=>{
    approvalDialogRef.value.formRef.validate((valid)=>{
        if (valid) {
            if (filePathData?.length > 0) {
                request.post('/files/tem/move', filePathData).then(res => {
                    UpdateApplication();
                })
            } else {
                UpdateApplication();
            }
        }
    })   
}

const handleUploadSuccess=(res)=>{
    ElMessage.success('文件上传成功');
    filePathData.push(res.data);
}

const handleBeforeRemoveFile=async (file, fileList, resolve)=>{
    try {
        const fileName = file.response?.data?.split('/').pop();
        let fileNameList = [];
        fileNameList.push(fileName);
        
        await ElMessageBox.confirm('确定删除该文件？');
        
        const res = await request.post(`/files/tem/delete`,fileNameList);

        if (res.code === '200') {
            ElMessage.success(res.data);
            filePathData = filePathData.filter(item => item !== fileName);
            resolve(true);
        } else {
            ElMessage.error(res.msg);
            resolve(false);
        }
    } catch (error) {
        resolve(false);
    }
}
/*
-----以上均为按钮点击事件+父子组件事件交互-----
*/
</script>
<style scoped>
.ApprovalList {
    padding: 24px;
    background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf4 100%);
    min-height: 100vh;
    animation: fadeIn 0.5s ease-in;
}

@keyframes fadeIn {
    from { opacity: 0; transform: translateY(10px); }
    to { opacity: 1; transform: translateY(0); }
}

.page-header {
    margin-bottom: 28px;
    text-align: center;
}

.page-title {
    font-size: 32px;
    font-weight: 600;
    margin: 0 0 8px 0;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
}

.page-subtitle {
    font-size: 14px;
    color: #7f8c8d;
    margin: 0;
}

.pagination-box {
    display: flex;
    justify-content: flex-end;
    margin-top: 8px;
    padding: 0 4px;
}

/* 按钮统一 */
.el-button {
    border-radius: 8px;
    font-weight: 500;
    letter-spacing: 1px;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.el-button:hover { transform: translateY(-2px); }
.el-button:active { transform: translateY(0); }

/* divider 样式 */
.custom-divider.create-template-divider {
    margin: 30px 0 20px;
}

.custom-divider.create-template-divider :deep(.el-divider__text) {
    background: linear-gradient(135deg, #667eea, #764ba2) !important;
    color: white;
    padding: 8px 24px;
    border-radius: 30px;
    font-size: 16px;
    font-weight: 500;
    letter-spacing: 1px;
    box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

/* 修复子组件中 divider 的背景色 */
:deep(.el-divider__text) {
    background-color: transparent !important;
}

/* 仅对 create-template-divider 保留渐变背景 */
.custom-divider.create-template-divider :deep(.el-divider__text) {
    background: linear-gradient(135deg, #667eea, #764ba2) !important;
}
</style>