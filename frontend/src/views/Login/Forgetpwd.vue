<template>
    <div class="Container">
        <div class="Box">
            <!--  mode="out-in"确保旧元素消失再加入新元素
                appeear初次渲染 -->
            <transition name="FadeSlide" mode="out-in" appear>
                <div v-if="data.serviceNo == null" style="width: 60%; margin-top: 24px; display: block;">
                    <div style="display: flex; justify-content: center; font-size: 48px;">
                        重置密码
                    </div>
                    <el-button @click="data.serviceNo = 'ResetPwd'; data.tagNo = 'Image'" 
                        style="width: 100%; height: 50px; margin: 20px 0 0 0; font-size: 20px;">
                        申请重置密码
                    </el-button>
                    <el-button @click="data.serviceNo = 'ResetPwd'; data.tagNo = 'Email'"
                        style="width: 100%; height: 50px; margin: 20px 0 0 0; font-size: 20px;">
                        邮箱验证重置
                    </el-button>
                    <el-button @click="data.serviceNo = 'CheckApplication'"
                        style="width: 100%; height: 50px; margin: 20px 0 0 0; font-size: 20px;">
                        查询申请进度
                    </el-button>
                    <el-button @click="ReturnLogin"
                        style="width: 100%; height: 50px; margin: 20px 0 0 0; font-size: 20px;">
                        返回登录页面
                    </el-button>
                </div>
                <!-- 重置密码 -->
                <div v-else-if="data.serviceNo === 'ResetPwd'" style="display: flex; width: 100%;; margin-top: 30px;">
                    <div style="width: 20%; display: flex; justify-content: right;">
                        <el-button @click="StepReset" :icon="HomeFilled" circle/>
                        <el-button @click="StepBack" :icon="ArrowLeftBold" circle/>
                    </div>
                    <div style="width: 60%;">
                        <el-steps align-center :active="active_index" finish-status="success">
                            <el-step v-for="item in steptitle_list" :title="item[data.tagNo]"/>
                        </el-steps>
                        <transition name="FadeSlideCom" mode="out-in" appear>
                            <component ref="ChildComponentRef" :is="CurrentComponent" class="ResetBlock" v-bind:tag="data.tagNo"/>
                        </transition>
                        <el-button @click="NextStep" v-if="active_index < 3"
                            style="width: 80%; margin: 16px 10%;">
                            下一步
                        </el-button>
                    </div>
                </div>
                <!-- 查看申请进度 -->
                <div v-else-if="data.serviceNo === 'CheckApplication'" style="display: flex; width: 100%;; margin-top: 30px;">
                    <div style="width: 20%; display: flex; justify-content: right;">
                        <el-button @click="StepBack" :icon="ArrowLeftBold" circle/>
                    </div>
                    <div style="width: 60%;">
                        <transition name="FadeSlideCom" mode="out-in" appear>
                            <component :is="CheckApplication" class="ResetBlock"/>
                        </transition>
                    </div>
                </div>
            </transition>

        </div>
    </div>
</template>
<script setup>
import { ArrowLeftBold, HomeFilled } from '@element-plus/icons-vue';
import { ElMessageBox } from 'element-plus';
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue';
import InforGather from './ResetPwdCom/InfoGather.vue'
import VirefyCode from './ResetPwdCom/VirefyCode.vue'
import InfoConfirm from './ResetPwdCom/InfoConfirm.vue'
import StepResult from './ResetPwdCom/StepResult.vue'
import CheckApplication from './ResetPwdCom/CheckApplication.vue';

const data = reactive({
    serviceNo: null,
    tagNo: null,
})

const ChildComponentRef = ref(null)

let active_index = ref(0)

const compunent_map = {
    0: InforGather,
    1: VirefyCode, 
    2: InfoConfirm,
    3: StepResult
}

const steptitle_list = [
    {
        'Image': '输入账号',
        'Email': '输入账号邮箱'
    },
    {
        'Image': '输入验证码',
        'Email': '检查邮箱验证码'
    },
    {
        'Image': '提交申请',
        'Email': '重置密码'
    }
]

// 从Map中获取当前需要渲染的组件
const CurrentComponent = computed(()=>compunent_map[active_index.value]);

const StepReset=()=>{
    active_index.value = 0;
    data.serviceNo = null;
}

const StepBack=()=>{
    if (active_index.value == 0) {
        data.serviceNo = null;
    }else {
        active_index.value --;
    }
}

const NextStep=async ()=>{
    
    if (!await ChildComponentRef.value.ValiedValue()){
        return;
    } 
    if (active_index.value < 3) {
        active_index.value ++;
    }
}

const ReturnLogin=()=>{
    ElMessageBox.confirm('确定返回登录界面？未提交的申请不会被保存。',{type:'warning'}).then(()=>{
        sessionStorage.removeItem('info_username');
        sessionStorage.removeItem('info_email');
    }).then(()=>{
        location.href = '/login'
    }).catch(()=>{})
}

onMounted(()=>{
    sessionStorage.setItem('has_send_email',null);
    window.addEventListener('beforeunload',UnloadHandler);
    window.addEventListener('pagehide',PageHideHandler);
})

onUnmounted(()=>{
    window.removeEventListener('beforeunload',UnloadHandler);
    window.removeEventListener('pagehide',PageHideHandler);
})

// 由于beforeunload本身不区分刷新和关闭，但是可以下一步操作来判断。
const UnloadHandler=async (e)=>{
    if (sessionStorage.getItem('has_send_email') === 'true') {
        e.preventDefault();
        e.returnValue = '您已经发送了验证码，离开页面将丢失会话信息，确定要离开吗？';        
        return e.returnValue;
    }else{

    }
}
// 确定刷新/关闭操作后下一步生命周期事件
const PageHideHandler=()=>{
    sessionStorage.removeItem('info_username');
    sessionStorage.removeItem('info_email');
    sessionStorage.removeItem('has_send_email');
}
</script>
<style scoped>
.Container{
    height: 100vh;
    overflow: hidden;
    background-image: url('@/assets/login_backround.png');
    background-size: 100% 100%;
    display: flex;
    justify-content: center;
    align-items: center;
}
.Box{
    min-width: 600px;
    min-height: 400px;
    width: 60%;
    height: 50%;
    background-color: rgba(255, 255, 255, 0.6);
    border-radius: 15px;
    box-shadow: 5px 5px 8px rgba(0, 0, 0, 0.3);
    display: flex;
    justify-content: center;
}
.ResetBlock{
    width: 80%;
    margin: 0 10%;
    height: 60%;
}
/* 均为标签动画 */
.FadeSlide-enter-active,.FadeSlide-leave-active{
    transition: all 0.5s ease;
}
.FadeSlide-enter-from{
    opacity: 0;
    transform: translateY(20px);
}
.FadeSlide-leave-to{
    opacity: 0;
    transform: translateY(-20px);
}
.FadeSlideCom-enter-active,.FadeSlideCom-leave-active{
    transition: all 0.3s ease;
}
.FadeSlideCom-enter-from{
    opacity: 0;
    transform: translateX(20px);
}
.FadeSlideCom-leave-to{
    opacity: 0;
    transform: translateX(-20px);
}
</style>