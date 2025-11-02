<template>
    <div class="Register_Container">
        <div class="Register_Box">
            <div class="Register_Block">
                <div class="Welcome_Title">注册账号</div>
                <el-form ref="formRef" :rules="rules" :model="data.form" style="width: 400px">
                    <el-form-item prop="username">
                        <el-input size="large" v-model="data.form.username" placeholder="请输入账号" prefix-icon="User"/>
                    </el-form-item>
                    <el-form-item prop="pwd">
                        <el-input @input="InputHandler" size="large" show-password v-model="data.form.pwd" placeholder="请输入密码" prefix-icon="Lock"/>
                    </el-form-item>
                    <el-form-item prop="pwd_auth">
                        <el-input @input="AuthInputHandler" size="large" show-password v-model="data.form.pwd_auth" placeholder="请确认密码" prefix-icon="Lock"/>
                    </el-form-item>
                    <el-form-item label="密码强度:">
                        <div class="Level_Line">
                            <div v-for="item in pwd_level" class="Pwd_Level" :level="pwd_level"></div>
                        </div>
                    </el-form-item>
                    <div>
                        <el-button @click="Register" size="large" style="width: 100%;font-size: 18px;" type="primary">注册</el-button>
                    </div>
                    <div style="text-align: right; margin-top: 12px; font-size: 18px;">
                        已有账号？请 <a style="color: rgb(0, 128, 225);text-decoration: none;" href="/login">登录</a>
                    </div>
                </el-form>
            </div>
        </div>
    </div>
</template>
<script setup>
import request from '@/utils/request';
import { ElMessage } from 'element-plus';
import { reactive, ref } from 'vue';


const data = reactive({
    form: {}
})

const pwd_level = ref(0)

const InputHandler=(value)=>{
    data.form.pwd = value.replace(/\s+/g, "");
}

const AuthInputHandler=(value)=>{
    data.form.pwd_auth = value.replace(/\s+/g, "");
}

const CalculatePwdLevel=(password)=>{
    const pwd = String(password);
    let level = 0;
    if(/[a-z]/.test(pwd)) level++;
    if(/[A-Z]/.test(pwd)) level++;
    if(/\d/.test(pwd)) level++;
    if(/\W/.test(pwd)) level++;
    return level;
}

const ValidatePwd=(rule, value, callback)=>{
    pwd_level.value = CalculatePwdLevel(value);
    callback();
}

const ValidateUserName=(rule, value, callback)=>{
    const str = String(value)

    const firstChar = str.charAt(0);
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

const ValidatePwdAuth=(rule,value,callback)=>{
    if (!value) { 
        callback(new Error('请再次确认密码！'));
    }else if (value !== data.form.pwd) {
        callback(new Error('两次密码不匹配！'));
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
        { required:true, message:"请输入密码", trigger:'blur' },
        { validator:ValidatePwd, trigger:['change']}
    ],
    pwd_auth:[
        { validator:ValidatePwdAuth, trigger: ['blur', 'change'] }
    ],
}

const formRef = ref()

const Register=()=>{
    formRef.value.validate((valid)=>{
        if(valid){
            request.post('/register',data.form).then(res=>{
                if (res.code === '200'){
                    ElMessage.success('注册成功');
                    setTimeout(() => { location.href = '/login' }, 1500);
                }else{
                    ElMessage.error(res.msg);
                }
            })
        }
    })
}
</script>
<style scoped>
.Register_Container{
    height: 100vh;
    overflow: hidden;
    background-image: url('@/assets/login_backround.png');
    background-size: 100% 100%;
}
.Register_Box{
    width: 50%;
    min-width: 460px;
    height: 100%;
    right: 0;
    position: absolute;
    display: flex;
    align-items: center;
    justify-content: center;
    
}
.Register_Block{
    background-color: white;
    justify-items: center;
    padding: 50px 30px;
    border-radius: 10px;
    box-shadow: 6px 6px 10px rgba(0, 0, 0, 0.3);
}
.Welcome_Title{
    font-size: 36px;
    font-weight: 600;
    color: rgb(0, 136, 255);
    margin-bottom: 24px;
}
.Level_Line{
    display: flex;
}
.Level_Line :first-child{
    border-radius: 8px 0 0 8px;
}
.Level_Line :last-child{
    border-radius: 0 8px 8px 0;
}
.Level_Line :only-child{
    border-radius: 8px;
}
.Pwd_Level{
    border: 1px solid black;
    box-shadow: 1px 1px rgba(0, 0, 0, 0.2);
    width: 48px;
    height: 16px;
    margin-right: 2px;
}
.Pwd_Level[level='1']{
    background-color: #E20000;
}
.Pwd_Level[level='2']{
    background-color: #FFC328;
}
.Pwd_Level[level='3']{
    background-color: #4DEF8E;
}
.Pwd_Level[level='4']{
    background:linear-gradient(220.55deg, #FFF500 0%, #FFB800 100%);
}
</style>