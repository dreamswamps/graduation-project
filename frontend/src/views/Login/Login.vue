<template>
    <div class="Login_Container">
        <div class="Logo_Box">
            <div class="Logo_Block">
                <div class="Logo_Header">
                    &nbsp; A &nbsp; &nbsp; Graduation &nbsp; &nbsp; Project
                </div>
                <div class="Logo_Title">
                    蜥蜴的毕设
                </div>
            </div>
        </div>
        <div class="Login_Box">
            <div class="Login_Block">
                <div class="Welcome_Title">登录账号</div>
                <el-form ref="formRef" :rules="rules" :model="data.form" style="width: 400px">
                    <el-form-item prop="username">
                        <el-input size="large" v-model="data.form.username" placeholder="请输入账号..." prefix-icon="User"/>
                    </el-form-item>
                    <el-form-item prop="pwd">
                        <el-input size="large" show-password v-model="data.form.pwd" placeholder="请输入密码..." prefix-icon="Lock"/>
                    </el-form-item>
                    <div>
                        <el-button @click="Login" size="large" style="width: 100%;font-size: 18px;" type="primary">登录</el-button>
                    </div>
                    <div style="margin-top: 12px; font-size: 18px; display: flex; justify-content: space-between; align-items: center;">
                        <el-checkbox v-model="data.remember_user" label="记住我"/>
                        <a style="color: rgb(0, 128, 225);text-decoration: none;" href="/forgetpwd">忘记密码</a>
                    </div>
                    <div style="text-align: right; margin-top: 12px; font-size: 18px;">
                        没有账号？请 <a style="color: rgb(0, 128, 225);text-decoration: none;" href="/register">注册</a>
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
    form: {},
    remember_user: false
})

if (localStorage.getItem('token')){
    location.href = '/manager';
}else{
    const remember = JSON.parse(localStorage.getItem('remember_user'));
    if (remember) {        
        data.form.username = remember.username;
        data.form.pwd = remember.pwd;
        data.remember_user = true;
    }
    
}

const rules = {
    username:[
        { required:true, message:"请输入账号", trigger:'blur' }
    ],
    pwd:[
        { required:true, message:"请输入密码", trigger:'blur' }
    ]
}

const formRef = ref()

const Login=()=>{
    formRef.value.validate((valid)=>{
        if(valid){
            request.post('/login',data.form).then(res=>{
                if (res.code === '200'){
                    localStorage.setItem('token',JSON.stringify(res.data));
                }else{
                    ElMessage.error(res.msg);
                }
            })
            .then(()=>{
                request.post('/verifytoken',localStorage.getItem('token')).then(res=>{
                    localStorage.setItem('current_user', JSON.stringify(res.data));
                    ElMessage.success('登陆成功'+res.data.role+res.data.name);
                })
            })
            .then(()=>{
                if (data.remember_user == true) {                    
                    localStorage.setItem('remember_user',JSON.stringify(data.form));                    
                }else{
                    localStorage.removeItem('remember_user');
                }
                setTimeout(() => { location.href = '/manager' }, 1500);
            })
            .catch((error)=>{
                console.error(error);
            })
        }
    })
}


</script>
<style scoped>
.Login_Container{
    height: 100vh;
    overflow: hidden;
    background-image: url('@/assets/login_backround.png');
    background-size: 100% 100%;
}
.Logo_Box{
    box-sizing: border-box;
    width: 50%;
    height: 100%;
    left: 0;
    position: absolute;
    display: flex;
    padding-top: 16vh;
    justify-content: center;
}
.Logo_Block{
    /* width: 200px; */
    padding-left: 12px;
    height: 200px;
}
.Logo_Title{
    font-family: 'BF';
    font-size: 64px;
    color: rgb(25, 84, 102);
    text-shadow: 0px 1px 2px rgba(0, 0, 0, 0.3);
}
.Login_Box{
    width: 50%;
    min-width: 460px;
    height: 100%;
    right: 0;
    position: absolute;
    display: flex;
    align-items: center;
    justify-content: center;
    
}
.Login_Block{
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
</style>