<template>
    <div class="Top_Box">
        <div class="Title">
            <el-image style="width: 40px; height: 40px" :src="LogoUrl" fit="fill" />
            <span style="margin-left: 12px;">管理系统</span>
        </div>
        <div style="flex: 1;">

        </div>
        <div class="User_Image">
            <el-popover :width="200"
                popper-style="padding : 20px">
                <template #reference>
                    <el-avatar style="border: 1px solid rgba(0, 0, 0, 0.2);" :size="50" :src="CircleUrl" />
                </template>
                <template #default>
                    <div>
                        <el-avatar :size="50" :src="CircleUrl" 
                            style="border: 1px solid rgba(0, 0, 0, 0.2); margin-bottom: 12px;
                                box-shadow: 0 10px 30px rgba(0, 0, 0, 0.12), 0 4px 8px rgba(0, 0, 0, 0.06);"/>
                    </div>
                    <div style="margin-bottom: 4px;">
                        <el-text style="font-size: 21px; font-weight: 600;">{{ data.user.name }}</el-text>
                    </div>
                    <div style="display: flex; align-items: center;">
                        <el-icon>
                            <component :is=" data.user.role === '管理员' ? Avatar : UserFilled "/>
                        </el-icon>
                        <el-text style="font-size: 14px; margin-left: 4px; color: rgba(0, 0, 0, 0.4);">{{ data.user.role }}</el-text>
                    </div>
                    <div>
                        <el-button @click="PopoverJump"
                            style="margin-top: 12px; width: 100%; box-shadow: 1px 1px 2px rgba(0, 0, 0, 0.4);">
                            个人资料
                        </el-button>
                    </div>
                    <div>
                        <el-button @click="Logout" type="danger"
                            style="margin-top: 12px; width: 100%; box-shadow: 1px 1px 2px rgba(0, 0, 0, 0.4);">
                            退出登录
                        </el-button>
                    </div>
                    <div>
                        <el-button @click="Logout" v-if="data.user.email == '未知'"
                            style="margin-top: 12px; width: 100%; box-shadow: 1px 1px 2px rgba(0, 0, 0, 0.4);">
                            身份认证
                        </el-button>
                    </div>
                </template>
            </el-popover>
            <span style="color: white;margin-left: 12px; font-size: 20px;"> {{ data.user.role }}</span>
            <span style="color: white;margin-left: 4px; font-size: 20px;"> {{ data.user.name }}</span>
            <el-link type="danger" underline @click="Logout" style="margin-left: 12px; margin-right: 24px;">退出登录</el-link>
        </div>
    </div>
    <div class="Href_Box">
        <div @click="ToggleArrow" class="ToggleArrow_Box">
            <el-icon style="font-size: 32px;" :class="['ArrowTransform',{'rorated':toggle_arrow}]">
                <DArrowRight/>
            </el-icon>
        </div>
        <div class="PathDisplay">
            <el-icon style="margin-right: 8px;"><Location /></el-icon>
            <div v-for="matched in data.matched_list" class="FastPathItem">
                <router-link :to="matched.path">{{ matched.name }}</router-link>
            </div>
        </div>
    </div>
    <div class="Body_Box">
        <div>
            <!-- default-active只是文字高亮≠选中该item -->
            <el-menu router :default-active="router.currentRoute.value.path" :default-openeds="['1']"
                :collapse="toggle_arrow" style="height: 100%;">
                <el-menu-item index="/manager">
                    <el-icon><HomeFilled /></el-icon>
                    <template #title>系统首页</template>
                </el-menu-item>
                <el-sub-menu>
                    <template #title>
                        <el-icon><UserFilled /></el-icon><span>用户管理</span>
                    </template>
                    <el-menu-item v-if="data.user.role === '管理员'" index="/manager/admin">
                        <el-icon><Collection /></el-icon><span>用户资料</span>
                    </el-menu-item>
                    <el-menu-item index="/manager/info">
                        <el-icon><User /></el-icon><span>个人资料</span>
                    </el-menu-item>
                    <!-- <el-menu-item index="/manager/article">
                        <el-icon><Reading /></el-icon><span>文章仓库</span>
                    </el-menu-item> -->
                    <el-menu-item index="/manager/unified-apply">
                        <el-icon><FolderAdd /></el-icon>
                        <span>申请大厅</span>
                    </el-menu-item>
                    <el-menu-item  v-if="data.user.role === '管理员'" index="/manager/approval-list">
                        <el-icon><Edit /></el-icon>
                        <span>审批申请</span>
                    </el-menu-item>
                </el-sub-menu>
                <el-menu-item index="/manager/data">
                    <el-icon><Histogram /></el-icon>
                    <template #title>数据详情</template>
                </el-menu-item>
            </el-menu>
        </div>
        <div class="Detail_Box">
            <RouterView v-if="$route.matched.length > 1"/>
            <Board v-else/>
        </div>
    </div>
</template>
<script setup>
import router from '@/router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { provide, reactive, onMounted, onUnmounted, computed, ref, watch } from 'vue';
import { RouterView, useRoute } from 'vue-router';
import Board from './Board.vue';
import { Avatar, UserFilled } from '@element-plus/icons-vue'
import request from '@/utils/request';

const token = localStorage.getItem('token')

const route_current = useRoute()

const toggle_arrow = ref(false);

let user_data = localStorage.getItem('current_user');

const ToggleArrow=()=>{    
    toggle_arrow.value = !toggle_arrow.value;
}

const VerifyToken=()=>{
    request.post('/verifytoken',token).then(res=>{
        if (res.code === '200'){
            localStorage.setItem('current_user', JSON.stringify(res.data));
            user_data = localStorage.getItem('current_user');
        }else{
            ElMessageBox.alert('你的身份认证已过期，请重新登录！','确认',{type:'danger'}).then(()=>{
            }).finally(()=>{
                localStorage.removeItem('token');
                location.href = '/login';
            })
        }
    })
}

// 没有token则返回登录界面
if (!token){
    location.href = '/login';
}else{
    try{
        // 验证更新本地缓存用户信息
        VerifyToken();
    }catch{
        location.href = '/login';
    }
}

const data = reactive({
    matched_list: [],
    user: user_data ? JSON.parse(user_data) : { id:'', name: '', role: '', avatar: '', email: ''}
})
provide('UserID',data.user.id);
provide('UserRole',(data.user.role === '管理员')?'admin':'user');

const PopoverJump=()=>{
    location.href = '/manager/info';
}

const Logout=()=>{
    ElMessageBox.confirm('确定退出登录？','确认',{type:'warning'}).then(()=>{
        localStorage.removeItem('token');
        localStorage.removeItem('current_user');
        location.href = '/login';
    })
}

// 定义事件监听，判断是否需要重置用户信息

onMounted(() => {
    data.matched_list = route_current.matched;
    window.addEventListener('userInfoUpdated', handleUserInfoUpdate);
});

onUnmounted(() => {
    window.removeEventListener('userInfoUpdated', handleUserInfoUpdate);
});

const handleUserInfoUpdate = () => {
    // 用户信息被更改后，需要重新生成一份密钥，因为密钥内保存的信息已经被替换
    user_data = localStorage.getItem('current_user');
    request.post('/generationtoken',user_data).then(res=>{
        if (res.code === '200'){
            localStorage.setItem('token',JSON.stringify(res.data));
        }else{
            ElMessage.error('密钥生成出错!XMX!请尝试重新登陆');
        }
    })
    .then(()=>{
        VerifyToken();
    })
    .then(()=>{
        setTimeout(()=>{
            window.location.reload();
        },1500)
    })
    .catch((error)=>{
        console.error(error);
    })

};

// 事件监听结束

// 监听地址，若发生变化则同步变化地址栏。
watch(()=>route_current.path,()=>{
    // console.log(route_current.matched);
    data.matched_list = route_current.matched; 
})

const CircleUrl = computed(()=>{
    return data.user.avatar ? data.user.avatar : 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';
})
const LogoUrl = '/logo.png';
</script>
<style scoped>
/* 统一顶部栏 */
.Top_Box {
    height: 80px;
    background: linear-gradient(90deg, #667eea 0%, #64b6ff 100%);
    display: flex;
    align-items: center;
    box-shadow: 0 4px 16px rgba(102, 126, 234, 0.10);
}
.Title {
    display: flex;
    align-items: center;
    width: 240px;
    font-size: 24px;
    color: white;
    font-weight: 500;
    margin-left: 24px;
}
.User_Image {
    display: flex;
    align-items: center;
    width: fit-content;
}
.Href_Box {
    height: 48px;
    display: flex;
    align-items: center;
    background: #f7faff;
    border-bottom: 1px solid rgba(125, 125, 125,0.12);
    border-radius: 0 0 12px 12px;
}
.PathDisplay {
    height: 100%;
    display: flex;
    align-items: center;
    padding: 0 12px;
    font-size: 20px;
    background: linear-gradient(to right, white, #c5ecff);
    border-radius: 8px;
}
.FastPathItem {
    height: 100%;
    display: flex;
    align-items: center;
}
.FastPathItem:not(:last-child)::after {
    content: ">";
    margin: 0 8px;
    color: #888;
}
.FastPathItem .router-link-active {
    transition: transform 0.3s ease,color 0.3s ease;
    text-decoration: none;
    color: #333;
}
.FastPathItem .router-link-active:hover {
    transform: scale(1.1);
    color: #409eff;
}
.ToggleArrow_Box {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100%;
    width: 64px;
    border-right: 1px solid #dcdfe6;
    background-color: white;
    cursor: pointer;
    border-radius: 8px 0 0 8px;
}
.Body_Box {
    display: flex;
    min-height: calc(100vh - 80px);
}
.Nav_Box {
    width: 200px;
    border-right: 1px solid rgba(125, 125, 125,0.12);
    min-height: calc(100vh - 80px);
}
.el-menu:not(.el-menu--collapse) {
    width: 200px;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(102, 126, 234, 0.06);
}
.el-menu .el-menu-item, .el-menu .el-sub-menu {
    border-bottom: 1px solid rgba(125, 125, 125,0.08);
}
.el-menu .is-active {
    background: linear-gradient(to right, white, #c5ecff);
}
.Detail_Box {
    flex-grow: 1;
    width: 0;
    background: #f7faff;
    padding: 24px 16px;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(102, 126, 234, 0.06);
}
.ArrowTransform {
    transition: transform 0.4s ease, color 0.4s ease;
    transform-origin: center;
    cursor: pointer;
}
.ArrowTransform.rorated {
    transform: rotate(90deg);
    color: #409eff;
}
.ArrowTransform:hover {
    transform: scale(1.2);
}
.ArrowTransform:hover.rorated {
    transform: rotate(90deg) scale(1.2);
}
/* 统一按钮风格 */
.el-button {
    border-radius: 8px;
    font-weight: 500;
    letter-spacing: 1px;
}
</style>