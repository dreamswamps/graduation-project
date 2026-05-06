<template>
    <div class="UnifiedApply">
        <div class="page-header">
            <h2 class="page-title">统一申请入口</h2>
        </div>

        <!-- 复用搜索模板组件 -->
        <ApprovalSearchTemplate
            ref="approvalSearchRef"
            :search-data="data.searchData"
            :show-clear-btn="true"
            :show-more-search-btn="false"
            :default-index-array="defaultIndexArray"
            @search-click="handleSearch"/>
        
        <!-- 复用表格模板组件 -->
        <ApprovalTableTemplate
            :table-data="data.tableData"
            :loading="isLoading"
            :fields-white-list="data.tableWhiteList"
            :translation-object="translationObject"
            :translation-field-list="translationFieldList"
            :show-detail-btn="false"
            :func-default-text="'申请'"
            :func-relation-map="unifiedApplyTypeFuncMap"
            :divider-text="'申请模板'"
            @func-click="handleFuncMapping"/>
        
        <!-- 复用弹出框模板组件 -->
        <ApprovalDialogTemplate
            ref="approvalDialogRef"
            :dialog-visible="data.dialogVisible"
            :dialog-title="data.dialogTitle"
            :application-info="data.applicationInfo"
            :form-display-fields="data.formDisplayFields"
            :form-rules="dialogFormRules"
            :show-descriptions="true && data.applicationInfo.type != 'creat-new-template'"
            :descriptions="descriptions"
            :show-slot="isAdmin"
            @dialog-close="handleDialogClose"
            @delete-file="handleDeleteAllTemFile"
            @reset-click="handleResetBtnClick"
            @submit-click="handleSubmitBtnClick"
            @upload-success="handleUploadSuccess"
            @before-remove="handleBeforeRemoveFile">
            <!-- 管理员验证插槽 -->
            <template #admin_verify_slot>
                <div style="display: flex; gap: 8px;">
                    <el-input
                        v-model="captcha"
                        :disabled="captchaPass"
                        placeholder="输入邮箱验证码"/>
                    <el-button
                        type="primary"
                        :disabled="countdown > 0 || sendLoading"
                        @click="SendEmailCaptcha">
                        {{ countdown > 0 ? `${countdown}秒后重试` : '发送验证码' }}
                    </el-button>
                </div>
            </template>

            <template #create_template_slot>
                <div class="create-template-container">
                    <div class="create-template-header">
                        <el-select 
                            v-model="configType" 
                            placeholder="选择类型" 
                            @change="GetConfigKeyList()">
                            <el-option 
                                v-for="item in typeOptions" 
                                :label="item.label" 
                                :value="item.value">
                            </el-option>
                        </el-select>
                        
                        <el-select 
                            v-model="configKey"
                            placeholder="选择标签">
                            <el-option 
                                v-for="item in keyOptions" 
                                :key="item.value" 
                                :label="item.label" 
                                :value="item.value">
                            </el-option>
                        </el-select>

                        <el-select 
                            v-if="['select', 'rate', 'upload', 'checkbox', 'radio'].includes(configType)" 
                            v-model="extraFieldValue" 
                            placeholder="可选值组合">
                                <el-option
                                    v-for="item in extraFieldOptions"
                                    :key="item.value" 
                                    :label="item.label" 
                                    :value="item.value">
                                </el-option>
                        </el-select>
                        
                        <el-button type="primary" @click="AddField">添加</el-button>
                    </div>
                </div>
                
                <el-divider content-position="left" class="custom-divider create-template-divider">
                    组件列表
                </el-divider>
                
                <div class="table-container">
                    <el-table :data="nameList.map(item => ({ value: item }))" style="width: 100%">
                        <el-table-column prop="value" label="字段标签" min-width="200">
                            <template #default="{ row }">
                                <span>{{ row.value }}</span>
                            </template>
                        </el-table-column>
                        
                        <el-table-column label="操作" width="120" fixed="right">
                            <template #default="{ $index }">
                                <el-button 
                                    type="danger" 
                                    @click="handleConfigFieldDelete($index)">
                                    删除
                                </el-button>
                            </template>
                        </el-table-column>
                    </el-table>
                </div>
                
                <el-divider content-position="left" class="custom-divider create-template-divider">
                    组件预览
                </el-divider>
            </template>
        </ApprovalDialogTemplate>
        
        <!-- 分页 -->
        <div class="pagination-box">
            <el-pagination
                @size-change="GetApplicationList"
                @current-change="GetApplicationList"
                layout="total, sizes, prev, pager, next, jumper"
                background
                v-model:current-page="data.pageNum"
                v-model:page-size="data.pageSize"
                :page-sizes="[5, 10, 15, 20]"
                :total="data.total" />
        </div>
    </div>
</template>

<script setup>
import { onMounted, ref, reactive } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import ApprovalDialogTemplate from './Components/ApprovalDialogTemplate.vue';
import ApprovalTableTemplate from './Components/ApprovalTableTemplate.vue';
import ApprovalSearchTemplate from './Components/ApprovalSearchTemplate.vue';
import { BuildFormDescriptions, fieldTypeMap, optionTypeMap, unifiedApplyTypeFuncMap } from './RelationMap';
import request from '@/utils/request';
import { OptionBuilder } from './Utils/OptionBuilder';
import { useCountdown } from './Utils/TimerComposables';

const data = reactive({
    pageNum: 1,
    pageSize: 10,
    total: 0,
    tableData: [],
    tableDataAllStorage: null,  //  缓存全部，可拓展
    searchData: {},
    defaultIndexArray: ['type','disabled'],     //  保存的是name而不是key
    formDisplayFields: [],
    formDisplayFieldsFirstStorage: null,
    dialogVisible: false,
    dialogTitle: '',
    tableWhiteList: ['templateName', 'templateDesc'],
    applicationInfo: {},
});

// 关于createTemplate部分
const configType = ref('')
const configKey = ref('')
const typeOptions = ref([])
const keyOptions = ref([])
const extraFieldOptions = ref([])
const nameList = ref([])
const extraFieldValue = ref('')
let keyList = [];
let relation_keys = [];
let fields_options = [];

const approvalSearchRef = ref(null);
const approvalDialogRef = ref(null);
const isLoading = ref(false);
let filePathData = [];
let isSubmited = false;
const translationObject = ref(optionTypeMap);
const translationFieldList = ref(['type']);
const descriptions = ref({});
let userID = null;
let userEmail = null;
const isAdmin = ref(false);
const captcha = ref();
const sendLoading = ref(false);
let captchaPass = false;
let token = '';
// 验证码禁用计时器
const { countdown, startCountdown, stopCountdown } = useCountdown(60);

// 获取所有可用的申请模板
const GetApplicationList= async ()=>{

    isLoading.value = true;
    let inputValue = {};

    if (approvalSearchRef.value) {
        inputValue = approvalSearchRef.value.getInputValue();
    }

    const params = {
        pageNum: data.pageNum,
        pageSize: data.pageSize,
    };

    // 合并搜索框内容至参数
    if (inputValue && Object.keys(inputValue).length > 0) {
        Object.assign(params, inputValue);   
    }

    request.get("/unified-apply/template",{
        params:params,
        headers:{
            'X-Current-User-ID': userID,
        }
    }).then(res=>{
        data.tableData = res.data.obj;
        if (data.tableDataAllStorage == null) {
            data.tableDataAllStorage = res.data.obj;
        }
        data.total = res.data.total;
        data.pageNum = res.data.pageNum;
        data.pageSize = res.data.pageSize;
        setTimeout(()=>{
            isLoading.value=false;
        },1000)
    })
}

// 获取所有可搜索的申请模板
const GetSearchRender=()=>{
    //  该页面只需要两个下拉框
    const searchParams = ["template_type","template_disabled"];

    request.post("/approval/render/search",searchParams).then(res=>{
        // 按照searchParams顺序重新构建searchData对象
        const orderedData = {};
        data.defaultIndexArray.forEach(key => {
            if (res.data[key]) {
                orderedData[key] = res.data[key];
            }
        });
        data.searchData = orderedData;
    }).then(()=>{
        request.get("/unified-apply/render/typelist").then(res=>{
            data.searchData.type.option = OptionBuilder(optionTypeMap,res.data);
        })
    })
}

// 制作新的申请时，因为type改变而调用获取符合的keyList
const GetConfigKeyList=()=>{
    configKey.value = null;
    request.get("/unified-apply/render/dialog-data/keyList/"+configType.value).then(res=>{
        let keyOptionsTem = [];
        if (res.data != null) {
            res.data.forEach(item => {
                keyOptionsTem.push({
                    'label': item.label,
                    'value': item.field_key
                })
            })
            keyOptions.value = keyOptionsTem;
        }
    })

    extraFieldValue.value = null;
    if (['select', 'rate', 'upload', 'checkbox', 'radio'].includes(configType.value)) {
        request.get("/unified-apply/render/dialog-render-data/spValue/"+configType.value).then(res=>{
            let extraFieldOptionTem = [];
            if (res.data != null) {
                res.data.forEach(item => {
                    extraFieldOptionTem.push({
                        'label': item.fieldKey,
                        'value': item.fieldKey,
                        'config': JSON.parse(item.config)
                    })
                })
            }
            extraFieldOptions.value = extraFieldOptionTem;
            
        })
    }
}

const AddField=()=>{
    if (!configKey.value) {
        ElMessage.error('请选择类型和标签')
        return;
    }
    // 如果相同的，则不允许被添加
    if (!keyList.includes(configKey.value)) {
        keyList.push(configKey.value);
        keyOptions.value.forEach(item => {
            if (item.value === configKey.value) {
                nameList.value.push(item.label);
            }
        })

        if (extraFieldValue.value) {
            // 如果有值，push extraFieldValue 和对应的 config
            // 这里需要先定义 relation_keys 和 fields_options 数组（如果还没有定义的话）
            // 假设你需要在组件的 reactive data 或 ref 中定义这两个数组
            relation_keys.push(extraFieldValue.value);
            
            // 从 extraFieldOptions 中找到对应的 config
            const selectedOption = extraFieldOptions.value.find(
                item => item.value === extraFieldValue.value
            );
            fields_options.push(selectedOption?.config || '1');
        } else {
            // 如果没有值，都 push "1"
            relation_keys.push('1');
            fields_options.push('1');
        }
        
        request.post("/unified-apply/render/dialog-render-data/keyList", keyList).then(res=>{
            if (res.data != null) {
                const newField = res.data;
                data.formDisplayFields = data.formDisplayFieldsFirstStorage
                newField.forEach((item, index) => {
                    // 获取当前索引对应的 fields_options 值
                    const fieldOption = fields_options[index];
                    
                    // 如果 fieldOption 存在且不是 "1"，则将其合并到 item 中
                    if (fieldOption && fieldOption !== '1') {
                        Object.assign(item, fieldOption);
                    }
                    
                    const length = Object.keys(data.formDisplayFields).length;
                    data.formDisplayFields = {
                        ...data.formDisplayFields,
                        [length]: item
                    }
                })
            } else {
                ElMessage.error('未找到渲染数据');
            }
        });
    } else {
        ElMessage.error('你不可以添加两个一样的组件！')
    }
    

}

const handleConfigFieldDelete=(index)=>{
    delete data.formDisplayFields[index + 1];
    nameList.value.splice(index, 1);
    keyList.splice(index, 1);
    relation_keys.splice(index, 1);
    fields_options.splice(index, 1);
}

// 表单校验规则
const dialogFormRules = {
    input: [{ required: true, message: '请输入!', trigger: 'blur' }],
    select: [{ required: true, message: '请选择!', trigger: 'change' }],
    checkbox: [{ type: 'array', required: true, message: '请选择!', trigger: 'change' }]
};

// 初始化加载
onMounted(() => {
    userID = JSON.parse(window.localStorage.getItem('current_user')).id;
    userEmail = JSON.parse(window.localStorage.getItem('current_user')).email;
    isAdmin.value = JSON.parse(window.localStorage.getItem('current_user')).role === '管理员';
    GetApplicationList();
    GetSearchRender();
});

// 管理员用于验证身份的发送邮箱验证码
const SendEmailCaptcha=()=>{
    if (!userEmail) {
        return TransformUserApply();    
    }

    // 倒计时状态不允许重新发送
    if (countdown.value > 0) return;

    sendLoading.value = true;
    request.post('/util/send-email-captcha',null,{
        params:{
            to: userEmail
        },
        withCredentials: true
    }).then(res=>{
        if (res.code === '200') {
            ElMessage.success('邮件发送成功，请查收邮件');
            startCountdown();   // 开始计时
        }else{
            ElMessage.error('邮件验证码发送失败...');
            TransformUserApply();
        }
    }).finally(()=>{
        sendLoading.value = false;
    });
}

const TransformUserApply=()=>{
    ElMessageBox.confirm("你目前未能通过验证，以此进行管理员快速申请，是否改为提交普通申请？").then(()=>{
        ElMessage.success('已修改为普通申请');
        isAdmin.value = false;

        captcha.value = '';
    }).catch(()=>{});
}

// 点击搜索
const handleSearch = () => {
    data.pageNum = 1;
    GetApplicationList();
};

// 显示弹出框组件并传递参数
const DialogPropsTransmit=(objData)=>{
    data.dialogVisible = true;
    data.dialogTitle = objData.funcConfig.name;
    data.applicationInfo = objData.rowData;
    approvalDialogRef.value.GetDisplayFields();
}

// 获取申请弹出框的编辑模板，不再需要锁定状态锁
const GetDialogRender=async (templateName=null, objData, wait=false)=>{
    
    if (templateName == null) {
        const tid = objData.rowData.tid
        const res = await request.get("/approval/render/templates",{
            params: {
                tid: tid
            }
        });
        data.formDisplayFields = res.data;
        DialogPropsTransmit(objData);
        return;
    }
    try {
        const renderRes = await request.get("/approval/render/templates/"+templateName);
        // 不需要等待，直接装填数据并渲染
        if (!wait) {
            data.formDisplayFields = renderRes.data;
            DialogPropsTransmit(objData);
        } else {
            // 需要等待，返回数据
            return renderRes;
        }
    } catch (error) {
        console.log('渲染弹出框数据时出错：',error);
    }
}

// 申请按钮
const handleFuncMapping =async (objData) => {
    const keysToKeep = [ 'tid', 'templateName', 'templateDesc' ];
    descriptions.value = BuildFormDescriptions(keysToKeep, objData.rowData, '模板信息');
    let renderRes = null;
    let optionTem = [];
    // 简化单独设置函数，如果有需要仍可以指定函数
    switch (objData.funcConfig.func){
        case 'handlePwdReset':
            GetDialogRender('PwdResetApply', objData);
            break;
        case 'handleDisableTemplate':
            renderRes = await GetDialogRender('DisableApplyTemplate', objData, true);
            // 为类型type装填目前typelist内容
            data.tableDataAllStorage.forEach(item=>{
                if (!item?.disabled) {
                    optionTem.push({
                        'label': item.templateName,
                        'value': item.templateName
                    })
                }
            })

            renderRes.data[0].option = optionTem;
            data.formDisplayFields = renderRes.data;
            renderRes.data[1].defaultValue = userID;
            renderRes.data[1].disabled = 1;
            DialogPropsTransmit(objData);
            break;
        case 'handleResumeTemplate':
            renderRes = await GetDialogRender('ResumeApplyTemplate', objData, true);
            // 为类型type装填目前typelist内容
            data.tableDataAllStorage.forEach(item=>{
                if (item?.disabled) {
                    optionTem.push({
                        'label': item.templateName,
                        'value': item.templateName
                    })
                }
            })

            renderRes.data[0].option = optionTem;
            data.formDisplayFields = renderRes.data;
            renderRes.data[1].defaultValue = userID;
            renderRes.data[1].disabled = 1;
            DialogPropsTransmit(objData);
            break;
        case 'handleCreateTemplate':
            renderRes = await GetDialogRender('CreateApplyTemplate', objData, true);
            const dialogformRes = await request.get("/unified-apply/render/dialog-data");

            // 制作类型选择option
            let typeOptionsTem = []
            dialogformRes.data.forEach(item => {
                typeOptionsTem.push({
                    'label': fieldTypeMap[item],
                    'value': item
                })
            })
            typeOptions.value = typeOptionsTem;

            data.formDisplayFields = renderRes.data;
            data.formDisplayFieldsFirstStorage = data.formDisplayFields;
            DialogPropsTransmit(objData);
            break;
        default:
            // 自定模板，修改为传递tid
            GetDialogRender(null, objData);
    }
};
// 以上为申请按钮的方法

// 弹窗关闭
const handleDialogClose = () => {
    data.dialogVisible = false;
    // 如果有临时文件，删除
    if (filePathData?.length > 0) {
        handleDeleteAllTemFile();
    }

    stopCountdown();
    captcha.value = '';
    captchaPass = false;
    token = '';

    keyList = [];
    relation_keys = [];
    fields_options = [];
    
    filePathData = null;
    descriptions.value = {};
    isSubmited = false;
    GetApplicationList();
};

// 重置按钮
const handleResetBtnClick = () => {
    ElMessageBox.confirm('确定重置表单？', '确定').then(() => {
        if (approvalDialogRef.value.filePathData) {
            handleDeleteAllTemFile();
        }
        ElMessage.success('重置成功');
        approvalDialogRef.value.formRef.resetFields();
    }).catch(() => {});
};

// 提交按钮!!!!!!!!!!!!!
const handleSubmitBtnClick=async () => {
    
    // 提交新增模板申请
    if (data.applicationInfo.type === 'creat-new-template') {
        try{
            console.log(keyList);
            console.log(relation_keys);
            
            
            approvalDialogRef.value.formLoading = true;
            let formData = {
                base_keys: keyList,
                relation_keys: relation_keys
            };
            const requestBody = {
                type: data.applicationInfo.type,
                apply_account: userID,
                apply_data: formData,
            }

            const res = await request.post('/application/submit/strategy', requestBody)
            if (res.code === '200'){
                ElMessage.success(res.msg);
                handleDialogClose();
            }
            else ElMessage(res.msg);
        }catch(e){
            console.log(e);
            
            ElMessage.error('出错了，申请提交失败');
        }finally{
            approvalDialogRef.value.formLoading = false;
            return;
        }
    }
    // 判断表单中是否有 admin_verify_slot 字段
    const hasAdminVerifyField = data.formDisplayFields.some(field => field.key === 'admin_verify_slot');
    // 如果存在 admin_verify_slot 字段且是管理员，才需要进行验证码验证
    if (hasAdminVerifyField && isAdmin.value && !captchaPass) {
        if (!captcha.value) {
            ElMessage.error('请输入验证码');
            TransformUserApply();
            return;
        }
        
        const result = await request.post('/util/verifycaptcha',{},{ 
            params:{
                code: captcha.value,
                type: 'Email',
                needKeep: true
            },
            withCredentials: true
        });
        if (!result.data) {
            ElMessage.error('验证码错误');
            TransformUserApply();
            return;
        } else {
            token = result.data;
            captchaPass = true;
        }
    }

    approvalDialogRef.value.formRef.validate((valid)=>{
        if (valid) {
            if (filePathData?.length > 0) {
                request.post('/files/tem/move', filePathData).then(res => {
                    SubmitApplication();
                })
            } else {
                SubmitApplication();
            }
        }
    })
};

// 提交申请
const SubmitApplication=async ()=>{    
    try{
        approvalDialogRef.value.formLoading = true;
        let formData = approvalDialogRef.value.formData;
        
        // 验证通过则赋值
        if (captcha.value) {
            formData = {
                ...formData,
                'token': token
            }
        }
        const requestBody = {
            type: data.applicationInfo.type,
            apply_account: userID,
            apply_data: formData,
        }

        const res = await request.post('/application/submit/strategy', requestBody)
        if (res.code === '200'){
            ElMessage.success(res.msg);
            handleDialogClose();
        }
        else ElMessage(res.msg);
    }catch(e){
        console.log(e);
        
        ElMessage.error('出错了，申请提交失败');
    }finally{
        approvalDialogRef.value.formLoading = false;
    }
}

// 上传成功
const handleUploadSuccess = (res) => {
    ElMessage.success('文件上传成功');
    filePathData.push(res.data);
};

// 删除前
const handleBeforeRemoveFile = async (file, fileList, resolve) => {
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
};

// 删除所有临时文件
const handleDeleteAllTemFile = () => {
    request.post("/files/tem/delete",filePathData);
};
</script>

<style scoped>
.UnifiedApply {
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

/* 统一卡片风格 */
.Card {
    background: #ffffff;
    border-radius: 20px;
    box-shadow: 0 8px 24px rgba(102, 126, 234, 0.12);
    padding: 24px 32px;
    margin-bottom: 20px;
    border: 1px solid rgba(102, 126, 234, 0.08);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    position: relative;
    overflow: hidden;
}

.Card::before {
    content: '';
    position: absolute;
    top: 0; left: 0; right: 0;
    height: 3px;
    background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
    opacity: 0;
    transition: opacity 0.3s ease;
}

.Card:hover::before { opacity: 1; }

.Card:hover {
    box-shadow: 0 12px 40px rgba(102, 126, 234, 0.18);
    transform: translateY(-2px);
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

/* 创建模板插槽 */
.create-template-container {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    padding: 24px;
    border-radius: 12px;
    margin-bottom: 20px;
    box-shadow: 0 10px 30px rgba(102, 126, 234, 0.2);
    animation: slideIn 0.5s ease-out;
}

.create-template-header {
    display: flex;
    gap: 12px;
    align-items: center;
    flex-wrap: wrap;
    background: rgba(255, 255, 255, 0.1);
    padding: 20px;
    border-radius: 10px;
    backdrop-filter: blur(10px);
}

.create-template-header :deep(.el-select) {
    flex: 1;
    min-width: 150px;
}

.create-template-header :deep(.el-select .el-select__wrapper) {
    background: rgba(255, 255, 255, 0.9);
    border: none;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    transition: all 0.3s ease;
}

.create-template-header :deep(.el-select .el-select__wrapper:hover) {
    transform: translateY(-2px);
    box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15);
}

.create-template-header :deep(.el-button--primary) {
    background: linear-gradient(135deg, #ff6b6b 0%, #feca57 100%);
    border: none;
    padding: 12px 30px;
    font-weight: 600;
    box-shadow: 0 4px 15px rgba(255, 107, 107, 0.3);
}

.create-template-header :deep(.el-button--primary:hover) {
    transform: translateY(-2px);
    box-shadow: 0 8px 25px rgba(255, 107, 107, 0.4);
}

.custom-divider.create-template-divider {
    margin: 30px 0 20px;
}

.custom-divider.create-template-divider :deep(.el-divider__text) {
    background: linear-gradient(135deg, #667eea, #764ba2);
    color: white;
    padding: 8px 24px;
    border-radius: 30px;
    font-size: 16px;
    font-weight: 500;
    letter-spacing: 1px;
    box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.table-container {
    background: #f8f9fa;
    border-radius: 12px;
    padding: 20px;
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
    animation: slideIn 0.5s ease-out;
}

.table-container :deep(.el-table) {
    border-radius: 10px;
    overflow: hidden;
}

.table-container :deep(.el-table th) {
    background: linear-gradient(135deg, #f5f7fa 0%, #e9ecef 100%);
    color: #495057;
    font-weight: 600;
}

.table-container :deep(.el-button--danger) {
    background: linear-gradient(135deg, #ff6b6b 0%, #ee5253 100%);
    border: none;
    transition: all 0.3s ease;
}

.table-container :deep(.el-button--danger:hover) {
    transform: scale(1.05);
    box-shadow: 0 4px 12px rgba(255, 107, 107, 0.3);
}

@keyframes slideIn {
    from { opacity: 0; transform: translateY(20px); }
    to { opacity: 1; transform: translateY(0); }
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