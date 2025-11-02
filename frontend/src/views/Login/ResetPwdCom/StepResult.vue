<template>
    <div style="justify-items: center; align-content: center;">
        <div class="Card" style="width: 80%; min-height: 60%; margin-top: 12px;">
            <div v-if="tag == 'Image'" style="text-align: center; margin: 8px;">
                <el-descriptions title="申请内容" column="1"  border>
                    <template #extra>
                        <div style="display: flex;">
                            <el-popover placement="right-start">
                                <template #reference>
                                    <el-icon color="rgba(125, 125, 125, 0.6)" size="20"><QuestionFilled /></el-icon>
                                </template>
                                <template #default>
                                    <el-text>
                                        你可以在<br/>
                                        [重置密码]-[查询申请进度]<br/>
                                        查看申请进度<br/>
                                    </el-text>
                                </template>
                            </el-popover>
                        </div>
                    </template>
                    <el-descriptions-item label="账号">
                        {{ data.application.apply_account }}
                    </el-descriptions-item>
                    <el-descriptions-item label="时间">
                        {{ data.application.apply_time }}
                    </el-descriptions-item>
                    <el-descriptions-item label="类型">
                        重置密码
                    </el-descriptions-item>
                </el-descriptions>
            </div>
            <div v-else-if="tag == 'Email'" style="text-align: center; margin: 8px;">
                <el-result
                    icon="success"
                    title="密码重置成功"
                    sub-title="点击按钮前往登录">
                </el-result>
            </div>
            <div style="margin-top: 24px;">
                <el-button type="primary" style="width: 100%; font-size: 18px;"
                    @click="ResultClick">
                    {{(tag == 'Image')?'查看进度':'前往登录'}}
                </el-button>
            </div>
        </div>
    </div>
</template>
<script setup>
import { onMounted, reactive } from 'vue';

const props = defineProps({
    tag: {
        type: String,
        required: false
    }
})

const data = reactive({
    form:{},
    application:{}
})

const ResultClick=()=>{
    if (props.tag == 'Image') {
        
    }else{
        location.href = '/login';
    }
}

onMounted(()=>{
    data.application = JSON.parse(sessionStorage.getItem('Application'));
})
</script>
<style scoped>

</style>