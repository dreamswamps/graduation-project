<template>
    <div>
        <div class="Card">
            <el-input style="width: 240px;margin-right: 8px;" prefix-icon="Search" v-model="data.title" placeholder="What's the title of article?"/>
            <el-button type="primary" @click="Load">查 询</el-button>
            <el-button type="warning" @click="Reset">重 置</el-button>
        </div>
        <div class="Card">
            <el-button type="primary" v-if="userRole === 'admin'" @click="HandleAddDialog">新 增</el-button>
            <el-button type="primary" v-if="userRole === 'user'" @click="HandleAddDialog">撰写文章</el-button>
            <el-button type="warning" v-if="userRole === 'admin'" @click="DeleteBatch">批量删除</el-button>
            <el-button 
                v-if="data.delete_ids.length > 0" 
                type="success" 
                @click="UndoDelete"
                icon="RefreshLeft">
                撤回全部({{ data.delete_ids.length }})
            </el-button>
        </div>
        <div class="Card">
            <el-table :data="data.article_list" style="width: 100%" @selection-change="handleSelectionChange">
                <el-table-column type="selection" width="55" />
                <el-table-column prop="id" label="编号" />
                <el-table-column prop="title" label="标题" show-overflow-tooltip="true"/>
                <el-table-column label="封面">
                    <template #default="scope">
                        <el-image v-if="scope.row.img" :src="scope.row.img" :preview-src-list="[scope.row.img]" preview-teleported="true" style="display: block; width: 100px; height: 40px; border: 1px solid black;"/>
                    </template>
                </el-table-column>
                <el-table-column label="内容">
                    <template #default="scope">
                        <el-button type="primary" @click="ViewContent(scope.row.content)">查看</el-button>
                    </template>
                </el-table-column>
                <el-table-column prop="description" label="简介" show-overflow-tooltip="true"/>
                <el-table-column prop="time" label="发表时间" />
                <el-table-column prop="publisher_name" label="发布者">
                    <template #default="scope">
                        <el-popover trigger="hover" placement="left" width="100"
                            @show="data.popover_newname = null">
                            <template #default>
                                <div style="white-space: nowrap;overflow: hidden;text-overflow: ellipsis; max-width: 90px;">
                                    {{ scope.row.popover_newname || '新名称?' }}
                                </div>
                                <div style="display: flex; justify-content: right; margin-top: 4px;">
                                    <el-button size="small" type="primary" v-if="!scope.row.popover_newname" @click="GetNewName(scope.row.publisher_id,scope.row)">查看</el-button>
                                    <el-button size="small" type="info" v-if="scope.row.popover_newname" @click="scope.row.popover_newname = null">隐藏</el-button>
                                </div>
                            </template>
                            <template #reference>
                                <div style="white-space: nowrap;overflow: hidden;text-overflow: ellipsis; max-width: 90px;">
                                    {{ scope.row.publisher_name }}
                                </div>
                            </template>
                        </el-popover>
                    </template>
                </el-table-column>
                <el-table-column v-if="userRole === 'admin'" label="操作" width="120">
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

        <el-dialog v-model="data.dialog_form_visible" title="文章信息" width="800">
            <el-form ref="formRef" :model="data.form" label-width="80px" class="Diolag_Input">
                <el-form-item label="标题" prop="title">
                    <el-input v-model="data.form.title" autocomplete="off" placeholder="请输入文章标题"/>
                </el-form-item>
                <el-form-item label="封面">
                    <el-upload
                        :action="baseURL+'/files/upload'"
                        list-type="picture"
                        :on-success="handleAvatarSuccess">
                        <el-button type="primary">上传封面</el-button>
                    </el-upload>
                </el-form-item>
                <el-form-item label="简介" prop="description">
                    <el-input type="textarea" :row="3" v-model="data.form.description" autocomplete="off" placeholder="请输入名称"/>
                </el-form-item>
                <el-form-item label="文章内容" prop="content">
                    <div style="border: 1px solid #ccc;padding: 20px;">
                        <Toolbar
                            style="border-bottom: 1px solid #ccc"
                            :editor="editorRef"
                            :mode="mode"
                        />
                        <Editor
                            style="height: 500px; overflow-y: hidden;"
                            v-model="data.form.content"
                            :defaultConfig="editorConfig"
                            :mode="mode"
                            @onCreated="handleCreated"
                        />
                    </div>
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

        <el-dialog v-model="data.view_visible" title="文章内容" width="800">
            <div style="padding: 20px;" v-html="data.form.content"></div>
            <template #footer>
            <div class="dialog-footer">
                <el-button type="danger" @click="data.view_visible=false">关闭</el-button>
            </div>
            </template>
        </el-dialog>
    </div>
</template>
<script setup>
import request from '@/utils/request';
import { ElMessage, ElMessageBox } from 'element-plus';
import { reactive, toRaw, ref, inject, computed } from 'vue';

import '@wangeditor/editor/dist/css/style.css' // 引入 css
import '@/assets/view.css'  //  官网提供的自定义样式
import { onBeforeUnmount, shallowRef, onMounted } from 'vue'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'

const data = reactive({
    title:null,
    article_list:[],
    pageNum:1,
    pageSize:5,
    total:0,
    dialog_form_visible:false,
    view_visible:false,
    form:{},
    save_tag:false,
    isEditMode:false,
    ids:[],
    delete_ids: [],
    // popover_newname: null,
})


const userRole = inject('UserRole');
const userID = inject('UserID');

let form_copy = {}

const formRef = ref()

//---request操作---
const Load=()=>{
    request.get('/article/selectpage',{
        params:{
            pageNum: data.pageNum,
            pageSize:data.pageSize,
            title:data.title,
        }
    }).then(res=>{
        if (res.code === '200'){
            data.article_list = res.data.list;
            data.total = res.data.total;
        } else{
            ElMessage.error(res.msg);
        }
    })
}
//---request-End---

// 进入界面自动查询显示数据
Load()

const Reset=()=>{
    data.title = null;
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
    console.log(row);
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
    request.post('/article/add',data.form,{
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
            ElMessage.error(res.msg);
        }
    })
}
const Update=()=>{
    request.put('/article/updatebyid',data.form,{
        headers:{
            'X-Current-User-ID': userID,
        }
    }).then(res=>{
        if(res.code === '200'){
            data.dialog_form_visible = false;
            ElMessage.success('更新操作成功！');
            Load();
        }else{
            ElMessage.error(res.msg);
        }
    })
}
const Delete=(id)=>{
    ElMessageBox.confirm('是否确认进行删除操作？删除后，你可以通过点击"撤回"按钮恢复被删除的记录。"','确认',{type:'warning'}).then(()=>{
        request.delete('/article/deletebyid/'+id),{
            headers:{
                'X-Current-User-ID': userID,
            }
        }.then(res=>{
                if(res.code === '200'){
                    ElMessage.success('删除操作成功！');
                    // 保存需要删除的数据用于撤回操作
                    data.delete_ids.push(id);
                    Load();
                }else{
                    ElMessage.error(res.msg);
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
        request.delete('/article/deletebatch',{
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
                ElMessage.error(res.msg);
            }
        })
    }).catch()
}

// 撤回操作
const UndoDelete = () => {
    if (!data.delete_ids) return;
    
    request.post('/article/undo',data.delete_ids,{
        headers:{
            'X-Current-User-ID': userID,
        }
    }).then(res => {
        if (res.code === '200') {
            ElMessage.success(`成功撤回${data.delete_ids.length}条记录！`);
            data.delete_ids = [];
            Load();
        } else {
            ElMessage.error('撤回失败: ' + res.msg);
        }
    }).catch(error => {
        ElMessage.error('撤回操作出错: ' + error.message);
    });
}

const GetNewName=(id,row)=>{
    request.get('/admin/newname/'+id).then(res=>{
        if (res.code === '200'){
            row.popover_newname = res.data;
        } else {
            ElMessage.error(res.msg);
        }
    })
}
//---request-End---


//---富文本编辑器---

// 编辑器实例，必须用 shallowRef
const baseURL = import.meta.env.VITE_API_BASE_URL
const editorRef = shallowRef()
const mode = 'default'   // 或 'simple'
const editorConfig = { 
    placeholder: '请输入内容...',
    MENU_CONF:{}
}

// 配置上传图片的目标地址
editorConfig.MENU_CONF['uploadImage'] = {
  server: `${baseUrl}/files/wangEditor/upload`,
  fieldName: 'file'
}

// 组件销毁时，也及时销毁编辑器
onBeforeUnmount(() => {
    const editor = editorRef.value
    if (editor == null) return
    editor.destroy()
})

const handleCreated = (editor) => {
    editorRef.value = editor // 记录 editor 实例，重要！
}

const ViewContent = (content) => {
    data.form.content = content;
    data.view_visible = true;
}
//---富文本编辑器结束---

const handleAvatarSuccess=(res)=>{
    data.form.img = res.data;  
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