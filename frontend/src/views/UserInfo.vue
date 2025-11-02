<template>
    <div class="Container">
        <div class="Card" style="margin-bottom: 24px; width: 400px; padding-top: 24px; padding-bottom: 24px;">
            <el-descriptions title="个人信息" :column="2" border>
                <el-descriptions-item v-for="item in description">
                    <template #label>
                        <div class="cell-item">
                        <el-icon>
                            <component :is="item.icon" />
                        </el-icon>
                        {{ item.key }}
                        </div>

                    </template>
                    {{item.value}}

                </el-descriptions-item>
            </el-descriptions>
        </div>
        <!-- 修改信息区域 -->
        <div class="Card" style="margin-bottom: 24px; width: 400px; padding-top: 24px; padding-bottom: 24px;">
            <el-form ref="formRef" :rules="rules" :model="data.form" label-width="80px" class="Diolag_Input">
                <div style="display: flex;justify-content: center;width: 100%; margin-bottom: 16px;">
                    <el-upload
                        class="avatar-uploader"
                        :action="baseURL+'/files/upload'"
                        :show-file-list = "false"
                        :on-success="handleAvatarSuccess">
                        <img v-if="data.form.avatar" :src="data.form.avatar" class="avatar" />
                        <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
                    </el-upload>
                </div>
                <el-form-item style="width: 360px;" label="名称" prop="name" required>
                    <el-input v-model="data.form.name" autocomplete="off" placeholder="修改名称..."/>
                </el-form-item>
                <el-form-item label="加入时间">
                    <el-date-picker
                        v-model="data.form.addtime"
                        type="date"
                        placeholder="修改加入日期..."
                        format="YYYY/MM/DD"
                        value-format="YYYY-MM-DD"/>
                </el-form-item>
                <el-form-item label="性别" prop="gender" required>
                    <el-radio-group v-model="data.form.gender">
                        <el-radio value="男">男</el-radio>
                        <el-radio value="女">女</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item style="width: 360px;" label="电话" prop="phone">
                    <el-input v-model="data.form.phone" autocomplete="off" placeholder="修改电话号码..."/>
                </el-form-item>
            </el-form>
            <div style="display: flex;justify-content: center;">
                <el-button type="primary" @click="Update">确认修改</el-button>
                <el-button type="primary" @click="data.dialog_visible=true">修改账号密码</el-button>
            </div>
        </div>
    </div>

        <el-dialog v-model="data.dialog_visible" title="修改账号密码" width="500" style="justify-items: center;" destroy-on-close>
            <el-form ref="formRef" :rules="rules" :model="data.form" style="width: 400px">
                <el-form-item prop="username">
                    <el-input v-model="data.form.username" placeholder="请输入账号" prefix-icon="User"/>
                </el-form-item>
                <el-form-item prop="pwd">
                    <el-input show-password v-model="data.form.pwd" placeholder="请输入密码" prefix-icon="Lock"/>
                </el-form-item>
                <el-form-item prop="pwd_auth">
                    <el-input show-password v-model="data.form.pwd_auth" placeholder="请确认密码" prefix-icon="Lock"/>
                </el-form-item>
                <div>
                    <el-button @click="ChangePWD" style="width: 100%;font-size: 18px;" type="primary">完成修改</el-button>
                </div> 
            </el-form>
        </el-dialog>

</template>
<script setup>
import request from '@/utils/request';
import { reactive,ref,inject, computed } from 'vue';
import { Avatar, Female, Histogram, Male, Message, Phone, Platform, User } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';

const data=reactive({
    info:{},
    form:{},
    dialog_visible:false
})

const baseURL = import.meta.env.VITE_API_BASE_URL;

const userID = inject('UserID');

const formRef = ref()

const ValidatePwdAuth=(rule,value,callback)=>{
    if (!value) { 
        callback(new Error('请再次确认密码！'));
    }else if (value !== data.form.pwd) {
        callback(new Error('两次密码不匹配！'));
    }else{
        callback();
    }
}

const ValidateUserName=(rule, value, callback)=>{
    const str = String(value)

    const firstChar = str.charAt(0);
    if (str === data.form.username){
        callback();
        return;
    }

    if (!/[A-Z0-9]/.test(firstChar)) {
        callback(new Error('必须以数字或大写字母开头'));
        return;
    }

    if (str.length > 13) {
        callback(new Error('账号最高13位'));
        return;
    }
    
    if (!/^[A-Za-z0-9_]+$/.test(str)) {
        callback(new Error('账号由数字，字母或下划线组成'));
        return;
    }
    callback();
}

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
    username:[
        { required:true, message:"请输入账号", trigger:'blur' },
        { validator:ValidateUserName, trigger:['blur', 'change']}
    ],
    pwd:[
        { required:true, message:"请输入密码", trigger:'blur' }
    ],
    pwd_auth:[
        { validator:ValidatePwdAuth, trigger: ['blur', 'change'] }
    ],
    name:[
        { required:true, message:'名称不能为空！', trigger:'blur' }
    ],
    gender:[
        { required:true, message:'请选择性别', trigger:'enum' }
    ],
    phone:[
        { validator:ValidatePhone, trigger: ['blur', 'change'] }
    ],
}

const Load=()=>{
    request.get('/admin/personal/'+userID).then(res=>{
        if (res.code === '200'){
            data.info = res.data;
            // console.log(data.info);
            data.form = { ...res.data }; 
        } else{
            ElMessage.error(res.msg);
        }
    })
}

Load()

const DescriptionValue=(value)=>{
    return (value)?value:'未知';
}

const description = computed(()=> [
    {
        key:"编号",icon:Platform,value:data.info.id
    },
    {
        key:"账号",icon:User,value:data.info.username
    },
    {
        key:"名称",icon:Avatar,value:data.info.name
    },
    {
        key:"性别",icon:(data.info.gender == "男"?Male:Female),value:data.info.gender
    },
    {
        key:"加入时间",icon:Histogram,value:DescriptionValue(data.info.addtime)
    },
    {
        key:"电话",icon:Phone,value:DescriptionValue(data.info.phone)
    },
    {
        key:"邮箱",icon:Message,value:DescriptionValue(data.info.email)
    }
])

const Update=()=>{
    formRef.value.validate((valid)=>{
        if(valid){
            request.put('/admin/updatebyid',data.form,{
                headers:{
                    'X-Current-User-ID': userID,
                }
            }).then(res=>{
                if(res.code === '200'){
                    ElMessage.success('修改成功！即将刷新页面');
                    // 更新缓存
                    const currentUser = JSON.parse(localStorage.getItem('current_user'));
                    currentUser.avatar = data.form.avatar;
                    currentUser.name = data.form.name;
                    localStorage.setItem('current_user', JSON.stringify(currentUser));
                    // 触发父组件监听事件，重置页面信息
                    window.dispatchEvent(new CustomEvent('userInfoUpdated'));
                    Load();
                }else{
                    ElMessage.error(res.msg);
                }
            })
        }
    })

}

const ChangePWD=()=>{
    // console.log(data.form);
    
    formRef.value.validate((valid)=>{
        if(valid){
            request.put('/changepwd',data.form,{
                headers:{
                    'User-ID': data.form.id,
                    'X-Current-User-ID': userID,
                }
            }).then(res=>{
                if(res.code === '200'){
                    data.dialog_visible = false;
                    ElMessage.success('账号密码修改成功！');
                    Load();
                }else{            
                    ElMessage.error(res.msg);
                }
            })
        }
    })

}

const handleAvatarSuccess=(res)=>{
    data.form.avatar = res.data;  
}
</script>
<style scoped>
.cell-item {
  display: flex;
  align-items: center;
}
.Container{
    padding: 36px;

}
.avatar-uploader .avatar {
  width: 120px;
  height: 120px;
  display: block;
}
</style>
<style>
.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}

.el-icon.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  text-align: center;
}
</style>