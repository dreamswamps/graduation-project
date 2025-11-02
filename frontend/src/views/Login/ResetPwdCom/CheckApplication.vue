<template>
    <div style="display: flex; justify-content: center; align-items: center;">
        <div class="Card" style="padding: 24px; margin-top: 32px;" 
            v-loading="isloading" element-loading-text="正在查找...">
            <div>
                <div style="margin-top: 16px;">
                    <h3>查询申请进度</h3>
                </div>
                <div style="margin-top: 16px;">
                    <el-form ref="formRef" :rules="rules" :model="data">
                        <el-form-item prop="apply_account">
                            <el-input v-model="data.apply_account" 
                                prefix-icon="Search" placeholder="请输入申请账号"/>
                        </el-form-item>
                    </el-form> 
                </div>
                <div style="display: flex; justify-content: flex-end;">
                    <el-button @click="CheckApplication" type="primary" style="margin-top: 16px;">
                        确认
                    </el-button>
                </div>
            </div>
            <div style="margin-top: 16px;" v-if="data.application.length > 0">
                <el-collapse accordion>
                    <el-collapse-item v-for="item in data.application">
                        <template #title="{ isActive }">
                            <el-text>
                                时间: {{ item.apply_time }}
                            </el-text>
                        </template>
                        <el-text>
                            编号: {{ item.aid }}<br/>
                            类型: {{ item.type }}<br/>
                            状态: {{ item.has_completed == 1 ? '已审批' : '未审批'}}<br/>
                        </el-text>
                        <el-text v-if="item.has_completed == 1">
                            结果: {{ item.approval == 1 ? '通过' : '拒绝' }}
                        </el-text>
                    </el-collapse-item>
                </el-collapse>
            </div>
        </div>
    </div>
</template>
<script setup>
import request from '@/utils/request';
import { ElMessage } from 'element-plus';
import { reactive, ref } from 'vue';

const data = reactive({
    apply_account: null,
    application: []
})

const isloading = ref(false);

const formRef = ref(null);

const rules = {
    apply_account:[
        { required:true, message:'请输入账号', trigger:'blur' }
    ]
}

const CheckApplication=()=>{
    isloading.value = true;
    try{
        formRef.value.validate((valid) => {
            if (valid) {
                request.get('/application/check',{
                    params:{
                        apply_account: data.apply_account,
                        type: "pwd-reset"
                    }
                }).then(res=>{
                    if (res.code === '200') {
                        data.application = res.data;
                    }else{
                        ElMessage.error(res.msg);
                    }
                })
            }
        })
    }finally{
        isloading.value = false;
    }
}
</script>
<style scoped>

</style>