<template>
    <div class="userinfo-container">
        <div class="page-header">
            <h2 class="page-title">个人中心</h2>
        </div>

        <div class="cards-wrapper">
            <!-- 信息展示卡片 -->
            <div class="Card info-card">
                <div class="avatar-display">
                    <img v-if="data.info.avatar" :src="data.info.avatar" class="info-avatar">
                    <div v-else class="info-avatar-placeholder">
                        <el-icon size="40"><User /></el-icon>
                    </div>
                    <div class="info-name">{{ data.info.name || '未知' }}</div>
                    <div class="info-role">{{ data.info.role || '' }}</div>
                </div>
                <el-descriptions :column="1" border class="info-desc">
                    <el-descriptions-item v-for="item in description" :key="item.key">
                        <template #label>
                            <div class="cell-item">
                                <el-icon><component :is="item.icon" /></el-icon>
                                {{ item.key }}
                            </div>
                        </template>
                        {{ item.value }}
                    </el-descriptions-item>
                </el-descriptions>
            </div>

            <!-- 修改信息卡片 -->
            <div class="Card edit-card">
                <div class="card-section-title">修改信息</div>
                <div class="avatar-upload-wrapper">
                    <el-upload
                        class="avatar-uploader"
                        :action="baseURL+'/files/upload'"
                        :show-file-list="false"
                        :on-success="handleAvatarSuccess">
                        <img v-if="data.form.avatar" :src="data.form.avatar" class="avatar" />
                        <div v-else class="avatar-uploader-placeholder">
                            <el-icon size="28"><Plus /></el-icon>
                            <span>上传头像</span>
                        </div>
                    </el-upload>
                </div>
                <el-form ref="formRef" :rules="rules" :model="data.form" label-width="80px">
                    <el-form-item label="名称" prop="name" required>
                        <el-input v-model="data.form.name" placeholder="修改名称..." />
                    </el-form-item>
                    <el-form-item label="加入时间">
                        <el-date-picker v-model="data.form.addtime" type="date" placeholder="修改加入日期..."
                            format="YYYY/MM/DD" value-format="YYYY-MM-DD" style="width: 100%;" />
                    </el-form-item>
                    <el-form-item label="性别" prop="gender" required>
                        <el-radio-group v-model="data.form.gender">
                            <el-radio value="男">男</el-radio>
                            <el-radio value="女">女</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item label="电话" prop="phone">
                        <el-input v-model="data.form.phone" placeholder="修改电话号码..." />
                    </el-form-item>
                </el-form>
                <div class="edit-actions">
                    <el-button type="primary" @click="Update" icon="Check">确认修改</el-button>
                    <el-button type="warning" @click="data.dialog_visible=true" icon="Lock">修改密码</el-button>
                </div>
            </div>
        </div>

        <!-- 修改密码弹窗 -->
        <el-dialog v-model="data.dialog_visible" title="修改账号密码" width="460" destroy-on-close>
            <el-form ref="formRef" :rules="rules" :model="data.form" class="pwd-form">
                <el-form-item prop="username">
                    <el-input v-model="data.form.username" placeholder="请输入账号" prefix-icon="User" />
                </el-form-item>
                <el-form-item prop="pwd">
                    <el-input show-password v-model="data.form.pwd" placeholder="请输入密码" prefix-icon="Lock" />
                </el-form-item>
                <el-form-item prop="pwd_auth">
                    <el-input show-password v-model="data.form.pwd_auth" placeholder="请确认密码" prefix-icon="Lock" />
                </el-form-item>
                <el-button @click="ChangePWD" style="width: 100%; font-size: 16px;" type="primary">完成修改</el-button>
            </el-form>
        </el-dialog>
    </div>
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
.userinfo-container {
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

/* 双栏布局 */
.cards-wrapper {
    display: flex;
    gap: 24px;
    align-items: flex-start;
    flex-wrap: wrap;
}

.Card {
    background: #ffffff;
    border-radius: 20px;
    box-shadow: 0 8px 24px rgba(102, 126, 234, 0.12);
    padding: 28px 32px;
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

.info-card {
    flex: 1;
    min-width: 300px;
    max-width: 420px;
}

.edit-card {
    flex: 1;
    min-width: 320px;
    max-width: 480px;
}

/* 信息卡头像区域 */
.avatar-display {
    display: flex;
    flex-direction: column;
    align-items: center;
    margin-bottom: 24px;
    padding-bottom: 20px;
    border-bottom: 1px solid rgba(102, 126, 234, 0.1);
}

.info-avatar {
    width: 88px;
    height: 88px;
    border-radius: 50%;
    object-fit: cover;
    border: 3px solid rgba(102, 126, 234, 0.3);
    box-shadow: 0 4px 16px rgba(102, 126, 234, 0.2);
    transition: transform 0.3s ease;
}

.info-avatar:hover { transform: scale(1.05); }

.info-avatar-placeholder {
    width: 88px;
    height: 88px;
    border-radius: 50%;
    background: linear-gradient(135deg, #e8ecf4 0%, #d0d8f0 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    color: #95a5a6;
    border: 3px solid rgba(102, 126, 234, 0.2);
}

.info-name {
    margin-top: 12px;
    font-size: 20px;
    font-weight: 600;
    color: #2c3e50;
}

.info-role {
    margin-top: 4px;
    font-size: 13px;
    color: #7f8c8d;
}

.info-desc { width: 100%; }

.cell-item {
    display: flex;
    align-items: center;
    gap: 6px;
}

/* 编辑卡片 */
.card-section-title {
    font-size: 18px;
    font-weight: 600;
    color: #2c3e50;
    margin-bottom: 20px;
    padding-bottom: 12px;
    border-bottom: 1px solid rgba(102, 126, 234, 0.1);
}

.avatar-upload-wrapper {
    display: flex;
    justify-content: center;
    margin-bottom: 24px;
}

.avatar-uploader .el-upload {
    border: 2px dashed rgba(102, 126, 234, 0.3);
    border-radius: 50%;
    cursor: pointer;
    overflow: hidden;
    transition: all 0.3s ease;
}

.avatar-uploader .el-upload:hover {
    border-color: #667eea;
    box-shadow: 0 4px 16px rgba(102, 126, 234, 0.2);
}

.avatar {
    width: 100px;
    height: 100px;
    border-radius: 50%;
    display: block;
    object-fit: cover;
}

.avatar-uploader-placeholder {
    width: 100px;
    height: 100px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 6px;
    color: #95a5a6;
    font-size: 12px;
    background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf4 100%);
}

.edit-actions {
    display: flex;
    justify-content: center;
    gap: 12px;
    margin-top: 8px;
}

/* 密码弹窗表单 */
.pwd-form {
    padding: 8px 16px 0;
    display: flex;
    flex-direction: column;
    gap: 4px;
}

/* 按钮统一 */
.el-button {
    border-radius: 8px;
    font-weight: 500;
    letter-spacing: 1px;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.el-button:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,0.12); }
.el-button:active { transform: translateY(0); }

@media (max-width: 768px) {
    .userinfo-container { padding: 16px; }
    .cards-wrapper { flex-direction: column; }
    .info-card, .edit-card { max-width: 100%; }
}
</style>