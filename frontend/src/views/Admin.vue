<template>
    <div>
        <div class="Card">
            <el-input style="width: 240px;margin-right: 8px;" prefix-icon="Search" v-model="data.name" placeholder="What's your name?"/>
            <el-button type="primary" @click="Load">查 询</el-button>
            <el-button type="warning" @click="Reset">重 置</el-button>
        </div>
        <div class="Card">
            <el-button type="primary" @click="HandleAddDialog">新 增</el-button>
            <el-button type="warning" @click="DeleteBatch">批量删除</el-button>
            <el-upload
                style="display: inline-block; margin: 0px 10px;"
                :action="baseURL + '/files/import'"
                :show-file-list="false"
                :on-success="HadnleImportSuccess"
                >
                <el-button type="primary">导 入</el-button>
            </el-upload>
            <el-button type="success" @click="ExportData">导 出</el-button>
            <el-button 
                v-if="data.delete_ids.length > 0" 
                type="success" 
                @click="UndoDelete"
                icon="RefreshLeft">
                撤回全部({{ data.delete_ids.length }})
            </el-button>
        </div>
        <div class="Card">
            <el-table :data="data.admin_list" style="width: 100%" @selection-change="handleSelectionChange">
                <el-table-column type="selection" width="55" />
                <el-table-column prop="id" label="编号" />
                <el-table-column label="头像">
                    <template #default="scope">
                        <img v-if="scope.row.avatar" :src="scope.row.avatar" style="display: block; width: 40px; height: 40px; border-radius: 50%; border: 1px solid black;">
                        <div v-else style="display: block; width: 40px; height: 40px; border-radius: 50%; border: 1px solid black;"></div>
                    </template>
                </el-table-column>
                <el-table-column prop="name" label="名称" />
                <el-table-column prop="username" label="账号" />
                <el-table-column prop="role" label="身份" />
                <el-table-column label="加入时间">
                    <template #default="scope">
                        {{ (scope.row.addtime)?scope.row.addtime:'未知' }}
                    </template>
                </el-table-column>
                <el-table-column prop="gender" label="性别" />
                <el-table-column label="电话">
                    <template #default="scope">
                        {{ (scope.row.phone)?scope.row.phone:'未知' }}
                    </template>
                </el-table-column>
                <el-table-column label="邮箱">
                    <template #default="scope">
                        {{ (scope.row.email)?scope.row.email:'未知' }}
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="120">
                    <template #default="scope">
                        <el-button @click="HandleUpdateDialog(scope.row)" type="primary" icon="Edit" circle/>
                        <el-button @click="Delete(scope.row.id)" type="danger" icon="Delete" circle/>
                    </template>
                </el-table-column>
            </el-table>
            <div style="margin-top: 12px;">
                <el-pagination 
                    @size-change="Load()"
                    @current-change="Load()"
                    layout="total, sizes, prev, pager, next, jumper" 
                    background
                    v-model:current-page="data.pageNum"
                    v-model:page-size="data.pageSize"
                    :page-sizes="[5, 10, 15, 20]"
                    v-model:total="data.total" />
            </div>
        </div>

        <el-dialog v-model="data.dialog_form_visible" title="用户信息" width="500">
            <el-form ref="formRef" :rules="rules" :model="data.form" label-width="80px" class="Diolag_Input">
                <el-form-item label="编号" prop="id" required >
                    <el-input v-model="data.form.id" autocomplete="off" placeholder="请输入数字编号" 
                    :disabled="data.isEditMode"/>
                </el-form-item>
                <el-form-item label="头像">
                    <el-upload
                        :action="baseURL+'/files/upload'"
                        list-type="picture"
                        :on-success="handleAvatarSuccess">
                        <el-button type="primary">上传头像</el-button>
                    </el-upload>
                </el-form-item>
                <el-form-item label="名称" prop="name" required>
                    <el-input v-model="data.form.name" autocomplete="off" placeholder="请输入名称"/>
                </el-form-item>
                <el-form-item label="加入时间">
                    <el-date-picker
                        v-model="data.form.addtime"
                        type="date"
                        placeholder="请选择日期"
                        format="YYYY/MM/DD"/>
                </el-form-item>
                <el-form-item label="性别" prop="gender" required>
                    <el-radio-group v-model="data.form.gender">
                        <el-radio value="男">男</el-radio>
                        <el-radio value="女">女</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="电话" prop="phone">
                    <el-input v-model="data.form.phone" autocomplete="off" placeholder="请输入电话号码"/>
                </el-form-item>
            </el-form>
            <template #footer>
            <div class="dialog-footer">
                <el-button type="danger" @click="Cancel">取消</el-button>
                <el-button type="primary" @click="Save" v-if="!data.isEditMode">保存</el-button>
                <el-button type="success" @click="Submit">确认</el-button>
            </div>
            </template>
        </el-dialog>
    </div>
</template>
<script setup>
import request from '@/utils/request';
import { ElMessage, ElMessageBox } from 'element-plus';
import { reactive, toRaw, ref, inject } from 'vue';

const baseURL = import.meta.env.VITE_API_BASE_URL;

const data = reactive({
    name:null,
    admin_list:[],
    pageNum:1,
    pageSize:5,
    total:0,
    dialog_form_visible:false,
    form:{},
    save_tag:false,
    isEditMode:false,
    ids:[],
    delete_ids: [],
})
const userID = inject('UserID');
let form_copy = {}

const formRef = ref()

// 针对id的正则校验
const ValidateId=(rule, value, callback)=>{
    const reg_id = /^(0|[1-9][0-9]{0,5})$/;
    if (!reg_id.test(String(value))){
        callback(new Error('编号请输出至多6位且不以0开头的数字！'));
    } else{
        callback();
    }
}
//  针对电话的正则表校验
const ValidatePhone=(rule, value, callback)=>{
    if (!value) { 
        callback();
        return;
    }
    const reg = /^1[3-9]\d{9}$/;
    if (!reg.test(value)) {
        callback(new Error('手机号码格式不正确！'));
    }else{
        callback();
    }
}

const rules = {
    id:[
        { required:true, message:'请输入编号', trigger:'blur' },
        { validator:ValidateId, trigger:'blur'}
    ],
    name:[
        { required:true, message:'请输入名称', trigger:'blur' }
    ],
    gender:[
        { required:true, message:'请选择性别', trigger:'enum' }
    ],
    phone:[
        { validator:ValidatePhone, trigger: ['blur', 'change'] }
    ],
}

//---request统一异常处理---
const RequestExceptionHandler=(res)=>{
    ElMessage.error(res.msg);
}

//---request操作---
const Load=()=>{
    request.get('/admin/selectpage',{
        params:{
            pageNum: data.pageNum,
            pageSize:data.pageSize,
            name:data.name,
        },
        headers:{
            'X-Current-User-ID': userID,
        }
    }).then(res=>{
        if (res.code === '200'){
            data.admin_list = res.data.list;
            data.total = res.data.total;
        } else{
            RequestExceptionHandler(res);
        }
    })
}
//---request-End---

// 进入界面自动查询显示数据
Load()

const Reset=()=>{
    data.name = null;
    Load();
}

const HandleAddDialog=()=>{
    data.dialog_form_visible = true;
    // console.log(form_copy);
    data.isEditMode = false;
    
    if (!data.save_tag){ 
        data.form = {};
        form_copy = {};
    } else {
        data.form = JSON.parse(JSON.stringify(form_copy));
    }
}

const HandleUpdateDialog=(row)=>{
    // console.log(row);
    data.form = JSON.parse(JSON.stringify(row));
    data.dialog_form_visible = true;
    data.isEditMode = true;
}

const handleSelectionChange = (rows) => {
    // 可以获取所有被勾选的行信息
    // console.log(rows);
    data.ids = rows.map(row => row.id)
    // console.log(data.ids);
    
}

//---弹窗按钮事件---

const Cancel=()=>{
    if(!data.isEditMode) data.form = JSON.parse(JSON.stringify(form_copy));  //  深拷贝，等号会导致二者指向同个对象
    data.dialog_form_visible = false;
    ElMessage.success('取消操作');
}

const Save=()=>{
    data.save_tag = true;
    data.dialog_form_visible = false;
    form_copy = JSON.parse(JSON.stringify(toRaw(data.form)));
    ElMessage.success('保存成功！');
}

const Submit=()=>{
    formRef.value.validate((valid)=>{
        if (valid) {
            data.isEditMode ? Update() : Add()
        }
    })
}
//---弹窗按钮-End---

//---request操作---

const Add=()=>{
    request.post('/admin/insert',data.form,{
        headers:{
            'X-Current-User-ID': userID,
        }
    }).then(res=>{
        if(res.code === '200'){
            data.dialog_form_visible = false;
            data.save_tag = false;
            ElMessage.success('新增操作成功！');
            Load();
        }else{
            RequestExceptionHandler(res);
        }
    })
}
const Update=()=>{
    request.put('/admin/updatebyid',data.form,{
        headers:{
            'X-Current-User-ID': userID,
        }
    }).then(res=>{
        if(res.code === '200'){
            data.dialog_form_visible = false;
            ElMessage.success('更新操作成功！');
            Load();
        }else{
            RequestExceptionHandler(res);
        }
    })
}
const Delete=(id)=>{
    ElMessageBox.confirm('是否确认进行删除操作？删除后，你可以通过点击"撤回"按钮恢复被删除的记录。"','确认',{type:'warning'}).then(()=>{
        request.delete('/admin/deletebyid/'+id,{
        headers:{
            'X-Current-User-ID': userID,
        }
    }).then(res=>{
            if(res.code === '200'){
                ElMessage.success('删除操作成功！');
                // 保存需要删除的数据用于撤回操作
                data.delete_ids.push(id);
                Load();
            }else{
                RequestExceptionHandler(res);
            }
        })
    }).catch()
}
const DeleteBatch=()=>{
    if (data.ids.length === 0){
        ElMessage.warning('请勾选至少一行数据');
        return;
    }
    ElMessageBox.confirm('是否确认进行批量删除操作？删除后，你可以通过点击"撤回"按钮恢复被删除的记录。','确认',{type:'warning'}).then(()=>{
        request.delete('/admin/deletebatch',{
            data:data.ids,
            headers:{
            'X-Current-User-ID': userID,
            }
        }).then(res=>{
            if(res.code === '200'){
                ElMessage.success('批量删除操作成功！');
                // 保存被删除的记录
                data.delete_ids = data.delete_ids.concat(data.ids);
                Load();
            }else{
                RequestExceptionHandler(res);
            }
        })
    }).catch()
}

// 撤回操作
const UndoDelete = () => {
    if (!data.delete_ids) return;
    
    request.post('/admin/undo',data.delete_ids).then(res => {
        if (res.code === '200') {
            ElMessage.success(`成功撤回${data.delete_ids.length}条记录！`);
            data.delete_ids = [];
            Load();
        } else {
            RequestExceptionHandler(res);
        }
    }).catch(error => {
        ElMessage.error('撤回操作出错: ' + error.message);
    });
}
//---request-End---

//---文件导入导出---

const ExportData=()=>{
    // 导出数据=>输出流导出excel
    window.open(`${baseURL}/files/export`)   //  打开新的窗口并向该URL发送GET请求
}

//---文件导入导出结束---


const handleAvatarSuccess=(res)=>{
    data.form.avatar = res.data;  
}

const HadnleImportSuccess=(res)=>{
    if(res.code === '200'){
        ElMessage.success("导入成功");
        Load();
    }else{
        RequestExceptionHandler(res);
    }
}
</script>
<style scoped>
:deep(.required-label .el-form-item__label::before) {
    content: '*';
    color: #ff3b3b;
    margin-right: 4px;
}
.Diolag_Input{
    padding-right: 40px;
    margin-top: 12px;
}
.Card{
    margin-bottom: 8px;
}
</style>