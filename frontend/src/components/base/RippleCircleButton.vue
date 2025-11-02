<template>
    <!-- class指向静态的css，style指向动态的css -->
    <!-- 按钮位于整个容器左上角 -->
    <div class="RippleContainer">
        <!-- 触发按钮 -->
        <el-button 
            circle 
            @click="ClickHandler"
            :color="button_color" 
            :style="{
                zIndex: 2,
                position: 'absolute'
            }"
            :width="`${button_size}px`">
            <img 
                class="Icon"
                :class="{'Animation': isActive}"
                :src="icon_src" 
                slot="icon" 
                :style="{
                    width: '100%'}">            
        </el-button>
        <!-- 涟漪图层 -->
        <div 
            :style="{
            zIndex: 1,
            position: absolute,
            width: `${container_size}px`,
            height: `${container_size}px`,
            marginTop: `-${button_size}px`,
            marginLeft: `-${button_size}px`}">
            <svg 
                :style="{
                display: block, 
                width: '100%', 
                height: '100%'}">
                <circle 
                    :class="{'Stork': isActive}" 
                    class="RippleCircle"
                    :cx="circle_center" 
                    :cy="circle_center" 
                    :r="circle_radius"/>
            </svg>
        </div>
    </div>

</template>
<script setup>
import { computed, ref } from 'vue';


// 定义可以接受的参数以及配置默认值
const props = defineProps({
    // 按钮颜色
    button_color: {
        type: String,
        default: 'red'
    },
    // 按钮尺寸，决定整体大小
    button_size: {
        type: Number,
        default: 16
    },
    // 必填，icon图片来源
    icon_src: {
        type: String,
        required: true
    },
    // 以下为RippleCircle可以修改的变量
    // 涟漪颜色
    ripple_color: {
        type: String,
        default: '#FF6356'
    },
    // 动画持续时间
    ripple_duration: {
        type: String,
        default: '1s'
    },
    // 放大倍率，不建议太大
    ripple_scale: {
        type: Number,
        default: 1.8
    },
    // 涟漪宽度
    ripple_storke_width: {
        type: Number,
        default: 3
    }
})

// 自定义事件，可以通知父组件子组件的点击事件，进而父组件可以监听到来自子组件的事件
const emit = defineEmits(['click'])

// 响应状态变量
const isActive = ref(false)

// 计算/转化属性
const button_size = computed(() => props.button_size)
const button_color = computed(() => props.button_color)
const icon_src = computed(() => props.icon_src)

const container_size = computed(() => props.button_size*4)
const circle_center = computed(() => props.button_size*2)
const circle_radius = computed(() => props.button_size)

// 自带方法修改响应状态
const RippleTrigger=()=>{
    isActive.value = true
    setTimeout(()=>{
        isActive.value = false
    }, 700)
}

const ClickHandler=()=>{
    RippleTrigger()
    emit('click')
}

// 暴露方法(如果需要)
defineExpose({
    RippleTrigger
})
</script>
<style scoped>
.RippleContainer {
    position: relative;
    display: inline-block;
    /* width: v-bind('container_size + "px"');
    height: v-bind('container_size + "px"'); */
}
.Icon.Animation{
    animation: IconOut 0.3s ease;
}
@keyframes IconOut {
    0%{
        opacity: 0;
        transform: scale(0);
    }
    80%{
        opacity: 1;
        transform: scale(1.2);
    }
    100%{
        transform: scale(1);
    }
}
.RippleCircle{
    --color: v-bind(ripple_color);
    --duration: v-bind(ripple_duration);
    --scale: v-bind(ripple_scale);
    --origin: v-bind('`${circle_center}px`');
    --storke_width: v-bind(ripple_storke_width);

    stroke-width: 0;
    stroke: var(--color);
    fill: transparent;
    transform-origin: var(--origin) var(--origin);
}
.RippleCircle.Stork{
    animation: CircleOut var(--duration) ease forwards;
}
@keyframes CircleOut {
    0%{
        stroke-width: var(--storke_width,3);
        stroke-opacity: 1;
        transform: scale(1);
    }
    70%{
        transform: scale(var(--scale));
        stroke-opacity: 1;
    }
    100%{
        stroke-width: 0;
        stroke-opacity: 0.2;
        transform: scale(var(--scale));
    }
}
</style>