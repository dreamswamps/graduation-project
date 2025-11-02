<template>
    <div style="display: flex; justify-content: center; align-items: center;" 
        v-loading="loading" element-loading-text="检查邮箱..." element-loading-background="rgba(255, 255, 255, 0.1)">
        <el-form ref="formRef" :rules="rules" :model="data.form" label-width="70" style="margin-top: 16px;">
            <el-form-item label="账号" prop="username">
                <el-input style="width: 230px;" maxlength="13" v-model="data.form.username"/>
            </el-form-item>
            <el-form-item v-if="tag === 'Email'" label="邮箱" prop="email">
                <el-input style="width: 230px;" v-model="data.form.email"/>
            </el-form-item>
        </el-form>
    </div>
</template>
<script setup>
import request from '@/utils/request';
import { ElMessage } from 'element-plus';
import { onMounted, onUnmounted, reactive, ref } from 'vue';

const props = defineProps({
    tag: {
        type: String,
        required: false
    }
})

const loading = ref(false)

const data = reactive({
    form:{
        username: '',
        email: ''
    }
})

const ValiEmail=(rule, value, callback)=>{    
    const string = String(value);
    const regular = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    if (!regular.test(string)) {
        callback(new Error("邮箱格式有误"))
        return;
    }
    callback();
}

const rules = {
    username:[
        { required:true, message:"请输入账号", trigger:'blur' }
    ],
    email:[
        { required:props.tag === 'Email'?true:false, message:"请输入邮箱", trigger:'blur' },
        { validator:ValiEmail, trigger: ['blur'] }
    ],
}

const formRef = ref()

// 完成检验后，还需要查询账号和邮箱是否匹配
const EmailVerify=async ()=>{
    try{
        const res = await request.post('/email-verify',null,{
            params:{
                username:data.form.username,
                email:data.form.email
            }
        })
        return res.code == 200 ? res.data : false;
    }catch(e){        
        return false;
    }
}

// 由于验证方法为异步操作通过回调函数返回值，因此需要在这里也使用异步函数
const ValiedValue=async ()=>{
    let valied = false;
    loading.value = true;
    try{
        valied = await formRef.value.validate();
        if (valied == true && props.tag === 'Email') {
            valied = await EmailVerify();
            if (valied == false) {
                ElMessage.error('出错了，请检查数据或网络连接')
            }
        }
    }finally{
        loading.value = false;
        return valied
    }
}

defineExpose({ValiedValue});

onMounted(()=>{
    const info_username = sessionStorage.getItem('info_username');
    const info_email = sessionStorage.getItem('info_email');
    if (info_username && info_username !== 'undefined') {
        data.form.username = info_username;
    }
    if (props.tag === 'Email' && info_email && info_email !== 'undefined') {
        data.form.email = info_email;
    }
})

onUnmounted(()=>{
    sessionStorage.setItem('info_username',data.form.username);
    if (props.tag === 'Email') {
        sessionStorage.setItem('info_email',data.form.email);
    }
})
</script>
<style scoped>

</style>