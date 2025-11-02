<template>
    <div style="display: flex; justify-content: center; align-items: center;">
        <div v-if="tag == 'Image'" class="Card" style="width: 80%;">
            <div style="text-align: center;">
                <span style="font-weight: 700; font-size: 20px;">确认提交</span>
                <br/>提交
                <span style="color: blue; text-decoration: underline;">
                    {{ data.form.username }}
                </span>
                用户的密码重置申请？
                申请将会提交给管理员审批。
            </div>
        </div>
        <div v-if="tag == 'Email'" class="Card" style="width: 80%;">
            <el-form ref="formRef" :rules="rules" :model="data.form">
                <el-form-item label="账号:" props="data.form.username">
                    {{ data.form.username }}
                </el-form-item>
                <el-form-item label="邮箱:">
                    {{ data.form.email }}
                </el-form-item>
                <el-form-item prop="pwd">
                    <el-input @input="InputHandler" show-password placeholder="请输入密码" prefix-icon="Lock" v-model="data.form.pwd"/>
                </el-form-item>
                <el-form-item prop="pwd_auth">
                    <el-input @input="AuthInputHandler" show-password placeholder="请确认密码" prefix-icon="Lock" v-model="data.form.pwd_auth"/>
                </el-form-item>
            </el-form>
        </div>
    </div>
</template>
<script setup>
import request from '@/utils/request';
import { ElMessage } from 'element-plus';
import { onMounted, reactive, ref } from 'vue';

const loading = ref(false);

const props = defineProps({
    tag: {
        type: String,
        required: false
    }
})

const data = reactive({
    form:{}
})
const formRef = ref()

const InputHandler=(value)=>{
    data.form.pwd = value.replace(/\s+/g, "");
}

const AuthInputHandler=(value)=>{
    data.form.pwd_auth = value.replace(/\s+/g, "");
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
    pwd:[
        { required:true, message:"请输入密码", trigger:'blur' },
    ],
    pwd_auth:[
        { validator:ValidatePwdAuth, trigger: ['blur', 'change'] }
    ],
}

const ResetPwd=async ()=>{
    try{
        const res = await request.post('/email-resetpwd',data.form);
        return res.code == '200' ? res.data : false;
    }catch(e){
        return false;
    }
}

const SubmitApplication=async ()=>{
    try{
        const res = await request.post('/application/submit',null,{
            params:{
                type:'pwd-reset',
                apply_account:data.form.username
            }
        })
        if (res.code == '200'){
            ElMessage.success(res.msg);
            sessionStorage.setItem('Application',JSON.stringify(res.data));
            return true;
        }
        else ElMessage(res.msg);
        return false;
    }catch(e){
        ElMessage.error('出错了，申请提交失败');
        return false;
    }
}

const ValiedValue=async ()=>{
    let valied = false;
    loading.value = true;
    try{
        if (props.tag === 'Email') {
            valied = await formRef.value.validate();
            if (valied == true) {
                valied = await ResetPwd();
                if (valied == false) {
                    ElMessage.error('出错了，密码重置失败');
                }else{
                    // 重置成功，清空状态
                    sessionStorage.removeItem('has_send_email');
                }
            }
        }else{
            valied = await SubmitApplication();
        }
    }finally{
        loading.value = false;
        return valied
    }
}

defineExpose({ValiedValue});

onMounted(()=>{
    data.form.username = sessionStorage.getItem('info_username');
    if (props.tag == 'Email') {
        data.form.email = sessionStorage.getItem('info_email');
    }
})

</script>
<style scoped>
.el-form-item{
    margin-bottom: 8px;
}
</style>