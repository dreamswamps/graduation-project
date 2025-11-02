<template>
    <div class="Game">
        <div class="Box">
            <div v-if="stop_status" class="Stop_Box">
                <div class="Stop_Text">
                    游戏暂停
                </div>
            </div>
            <div :class="['Dragon',{'DragonJumpClass':jump_status}]"></div>
            <div :class="['Block']"></div>
        </div>
    </div>

</template>
<script setup>
import { ref } from 'vue';
import '@/assets/font/fusion-pixel-12px-proportional.css'
// 暂停状态
const stop_status = ref(true);
// 跳跃状态
const jump_status = ref(false);
// 监听空格/esc
window.addEventListener("keydown",event =>{
    // console.log(event);
    if (event.code === 'Space' && jump_status.value === false) {        
        jump_status.value = true;
        setTimeout(()=>{
            jump_status.value = false;
        },600)
    }
    else if (event.code === 'Escape'){
        stop_status.value = !stop_status.value;
    }
})
// 监听是否碰撞
setInterval(()=>{
    const dragon = document.querySelector('.Dragon');
    const block = document.querySelector('.Block');

    const dragon_rect = dragon.getBoundingClientRect();
    const block_rect = block.getBoundingClientRect();
    
    const IsCollision = !(
        dragon_rect.right < block_rect.left ||
        dragon_rect.left > block_rect.right ||
        dragon_rect.bottom < block_rect.top
    )
    
    if (IsCollision) {
        console.log('碰撞');
    }
},100)



</script>
<style scoped>
.Game{
    width: 100vw;
    height: 100vh;
    padding: 20px;
    background-color: white;
}
.Box{
    width: 500px;
    height: 200px;
    border: 1px solid black;
    position: relative;
}
.Stop_Box{
    box-sizing: border-box;
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
}
.Stop_Text{
    font-family: 'Pixel';
    font-size: 36px;
    letter-spacing: 2px;
}
.Dragon{
    width: 20px;
    height: 50px;
    background-color: red;
    position: absolute;
    bottom: 0;
}
.DragonJumpClass {
    animation: DragonJump 0.6s linear infinite;
    animation-play-state: v-bind("stop_status ? 'paused':'running'");
}
@keyframes DragonJump {
    0%{
        bottom: 0;
    }
    40%{
        bottom: 100px;
    }
    50%{
        bottom: 110px;
    }
    60%{
        bottom: 100px;
    }
    100%{
        bottom: 0;
    }
}
.Block{
    width: 20px;
    height: 20px;
    background-color: aqua;
    position: absolute;
    bottom: 0;
    left: 450px;
    animation: BlockMove 1.5s linear infinite;
    animation-play-state: v-bind("stop_status ? 'paused':'running'");
}
@keyframes BlockMove {
    0%{
        left: 450px;
    }
    100%{
        left: -20px;
    }
}
</style>