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
        <div class="Menu_Container">
            <el-menu
                router
                :default-active="router.currentRoute.value.path"
                :default-openeds="['user-management']"
                :collapse="toggle_arrow"
                class="custom-menu">
                <el-menu-item
                    v-for="item in filteredMenuItems"
                    :key="item.index"
                    :index="item.index"
                    v-show="!item.children"
                    class="menu-item-custom">
                    <el-icon><component :is="item.icon" /></el-icon>
                    <template #title>{{ item.title }}</template>
                </el-menu-item>

                <el-sub-menu
                    v-for="group in filteredMenuGroups"
                    :key="group.index"
                    :index="group.index"
                    class="submenu-custom">
                    <template #title>
                        <el-icon><component :is="group.icon" /></el-icon>
                        <span>{{ group.title }}</span>
                    </template>
                    <el-menu-item
                        v-for="child in group.children"
                        :key="child.index"
                        :index="child.index"
                        class="submenu-item-custom">
                        <el-icon><component :is="child.icon" /></el-icon>
                        <span>{{ child.title }}</span>
                    </el-menu-item>
                </el-sub-menu>
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
import { Avatar, UserFilled, HomeFilled, Collection, User, FolderAdd, Edit, Histogram, Tickets } from '@element-plus/icons-vue'
import request from '@/utils/request';

const token = localStorage.getItem('token')

const route_current = useRoute()

const toggle_arrow = ref(false);

let user_data = localStorage.getItem('current_user');

const ToggleArrow=()=>{
    toggle_arrow.value = !toggle_arrow.value;
}

// 菜单配置
const menuConfig = {
    items: [
        {
            index: '/manager',
            title: '系统首页',
            icon: HomeFilled,
            roles: ['管理员', '用户']
        },
        {
            index: '/manager/data',
            title: '数据详情',
            icon: Histogram,
            roles: ['管理员', '用户']
        }
    ],
    groups: [
        {
            index: 'user-management',
            title: '用户管理',
            icon: UserFilled,
            children: [
                {
                    index: '/manager/admin',
                    title: '用户资料',
                    icon: Collection,
                    roles: ['管理员']
                },
                {
                    index: '/manager/info',
                    title: '个人资料',
                    icon: User,
                    roles: ['管理员', '用户']
                },
                {
                    index: '/manager/unified-apply',
                    title: '申请大厅',
                    icon: FolderAdd,
                    roles: ['管理员', '用户']
                },
                {
                    index: '/manager/my-applications',
                    title: '我的申请',
                    icon: Tickets,
                    roles: ['管理员', '用户']
                },
                {
                    index: '/manager/approval-list',
                    title: '审批申请',
                    icon: Edit,
                    roles: ['管理员']
                }
            ]
        }
    ]
}

// 根据角色过滤菜单项
const filteredMenuItems = computed(() => {
    return menuConfig.items.filter(item =>
        item.roles.includes(data.user.role)
    );
});

const filteredMenuGroups = computed(() => {
    return menuConfig.groups.map(group => ({
        ...group,
        children: group.children.filter(child =>
            child.roles.includes(data.user.role)
        )
    })).filter(group => group.children.length > 0);
});

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
    min-height: calc(100vh - 128px);
    gap: 16px;
    padding: 16px;
    background: #f7faff;
}

.Menu_Container {
    background: white;
    border-radius: 16px;
    box-shadow: 0 4px 20px rgba(102, 126, 234, 0.08);
    overflow: hidden;
    transition: all 0.3s ease;
}

.custom-menu {
    border: none;
    background: transparent;
    height: 100%;
}

.custom-menu:not(.el-menu--collapse) {
    width: 240px;
}

/* 菜单项通用样式 */
.menu-item-custom,
.submenu-item-custom {
    margin: 8px 12px;
    border-radius: 12px;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    border: none !important;
    position: relative;
    overflow: hidden;
}

.menu-item-custom::before,
.submenu-item-custom::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    height: 100%;
    width: 4px;
    background: linear-gradient(180deg, #667eea 0%, #64b6ff 100%);
    transform: scaleY(0);
    transition: transform 0.3s ease;
}

.submenu-custom .el-sub-menu__title {
    position: relative;
}
.submenu-custom .el-sub-menu__title::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    height: 100%;
    width: 4px;
    background: linear-gradient(180deg, #667eea 0%, #64b6ff 100%);
    transform: scaleY(0);
    transition: transform 0.3s ease;
}
.submenu-custom .el-sub-menu__title:hover::before {
    transform: scaleY(1);
}

.menu-item-custom:hover,
.submenu-item-custom:hover {
    background: linear-gradient(135deg, #f0f4ff 0%, #e8f4ff 100%) !important;
    transform: translateX(4px);
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.12);
}

.menu-item-custom:hover::before,
.submenu-item-custom:hover::before {
    transform: scaleY(1);
}

/* 激活状态 */
.custom-menu .is-active {
    background: linear-gradient(135deg, #667eea15 0%, #64b6ff15 100%) !important;
    color: #667eea !important;
    font-weight: 600;
    box-shadow: 0 2px 8px rgba(102, 126, 234, 0.15);
}

.custom-menu .is-active::before {
    transform: scaleY(1);
}

.custom-menu .is-active .el-icon {
    color: #667eea;
    transform: scale(1.1);
}

/* 子菜单样式 */
.submenu-custom {
    margin: 8px 12px;
    border-radius: 12px;
    overflow: hidden;
}

.submenu-custom .el-sub-menu__title {
    border-radius: 12px;
    transition: all 0.3s ease;
    margin: 0;
    height: 56px;
}

.submenu-custom .el-sub-menu__title:hover {
    background: linear-gradient(135deg, #f0f4ff 0%, #e8f4ff 100%) !important;
    transform: translateX(4px);
}

.submenu-custom .el-menu {
    background: #fafbff;
    border-radius: 0 0 12px 12px;
}

/* 图标样式 */
.custom-menu .el-icon {
    font-size: 20px;
    transition: all 0.3s ease;
    color: #606266;
}

.custom-menu .menu-item-custom:hover .el-icon,
.custom-menu .submenu-item-custom:hover .el-icon {
    color: #667eea;
    transform: scale(1.15);
}

/* 折叠状态优化 */
.custom-menu.el-menu--collapse {
    width: 64px;
}

.custom-menu.el-menu--collapse .menu-item-custom {
    margin: 8px auto;
    width: 48px;
    display: flex;
    justify-content: center;
}
.custom-menu.el-menu--collapse .submenu-custom {
    margin: 8px auto;
    width: 48px;
}

/* 折叠状态下子菜单标题的特殊处理 */
.custom-menu.el-menu--collapse .submenu-custom .el-sub-menu__title {
    width: 48px !important;
    height: 48px !important;
    padding: 0 !important;
    display: flex !important;
    justify-content: center !important;
    align-items: center !important;
    margin: 0 !important;
}

.custom-menu.el-menu--collapse .submenu-custom .el-sub-menu__title .el-icon {
    margin: 0 !important;
}

/* 折叠状态下禁用悬停位移效果 */
.custom-menu.el-menu--collapse .menu-item-custom:hover,
.custom-menu.el-menu--collapse .submenu-custom .el-sub-menu__title:hover {
    transform: none !important;
}
.Detail_Box {
    flex: 1;
    background: white;
    padding: 24px;
    border-radius: 16px;
    box-shadow: 0 4px 20px rgba(102, 126, 234, 0.08);
    min-width: 0;
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