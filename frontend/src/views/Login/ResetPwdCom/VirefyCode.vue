<template>
    <div style="justify-items: center; align-content: center;">
        <div style="margin-bottom: 16px;">
            <el-input v-model="code" style="width: 160px; height: 40px;"/>
        </div>
        <div v-if="tag === 'Image'" @click="(nomore_limit)?GenerateCaptcha():ClickToGenerateCaptcha()">
            <el-image :src="image_url" v-loading="isloading"
                style="width: 160px; height: 40px; cursor: pointer;">
                <template #placeholder>
                    <div style="background-color: white; display: flex; justify-content: center;">
                        <img :src="breakphoto"  style="height: 40px;">
                    </div>
                </template>
            </el-image>
        </div>
        <div v-if="tag === 'Email'">
            <el-button @click="email_dialog_visable = true" type="primary" :disabled="last_second > 0" 
                style="width: 160px; height: 40px; font-size: 18px;">
                {{ (last_second > 0) ? `重新发送(${last_second}s)` : '获取验证码'}}
            </el-button>
            <div style="width: 160px; margin-top: 6px; text-align: center;">
                目标邮箱
            </div>
            <div class="EmailText">
                {{ to_email }}
            </div>
        </div>

        <el-dialog
        v-model="email_dialog_visable"
        title="确认发送邮件"
        width="500"
        align-center>
            <div v-loading="isloading" element-loading-text="正在尝试发送邮件...">
                <span>是否确认向
                    <span style="color: blue; text-decoration: underline;">
                        {{ to_email }}
                    </span>
                    发送邮箱验证码？<br/>验证码有效时间为15分钟。<br/>
                    获取验证码后请勿离开该页面，网页不会为您存储信息。<br/>
                    若离开页面，为了保护您的隐私，请重新获取验证码。
                </span>
            </div>
            <template #footer>
                <div>
                    <el-button @click="email_dialog_visable = false" type="warning">
                        取消发送
                    </el-button>
                    <el-button @click="SendEmailCaptcha" type="success">
                        确认发送
                    </el-button>
                </div>
            </template>
        </el-dialog>
    </div>
</template>
<script setup>
import request from '@/utils/request';
import { onMounted, ref } from 'vue';
import breakphoto from '@/assets/breakphoto.svg'
import { ElMessage, ElMessageBox } from 'element-plus';

const image_url = ref(null);
const Min_Interval = 3000;  //  最小时间间隔
let last_click_time = 0;    //  记录最近一次点击时间戳
let over_click_count = 0;   //  记录点击过快次数
const nomore_limit = ref(false);
const isloading = ref(false);
const last_second = ref(null);

const code = ref();

const to_email = ref();
const email_dialog_visable = ref(false);

const props = defineProps({
    tag: {
        type: String,
        required: false
    }
})

// 接收图片验证码
const GenerateCaptcha=()=>{
    isloading.value = true;
    request.get('/util/generatecaptcha',{
        responseType: 'blob',
        withCredentials: true   //  传递凭证，用于身份验证+授权
    }).then(res=>{
        image_url.value = URL.createObjectURL(res)
    }).finally(isloading.value = false)
}

// 通过点击进行图片验证码请求
const ClickToGenerateCaptcha=()=>{
    const current_time = Date.now();
    try{
        if (current_time - last_click_time < Min_Interval) {
            ElMessage.warning('请不要频繁点击！');
            while (over_click_count > 10) {
                over_click_count = -1;
                ElMessageBox.confirm('好玩吗?','停下!',{
                    type: 'warning',
                    confirmButtonText: '好玩',
                    cancelButtonText: '好吧',
                    closeOnClickModal: false
                }).then(()=>{
                    ElMessage.success({
                        message: '如果你喜欢...',
                        plain: true,
                    })
                    nomore_limit.value = true;
                }
                ).catch(()=>{
                    ElMessage.info('下次别这样了');
                });
            }
        }else {
            GenerateCaptcha();
        }
    }finally{
        last_click_time = current_time;
        over_click_count++;
    }
}

// 发送邮件验证码请求
const SendEmailCaptcha=()=>{
    isloading.value = true
    request.post('/util/send-email-captcha',null,{
        params:{
            to: to_email.value
        },
        withCredentials: true
    }).then(res=>{
        if (res.code === '200') {
            ElMessage.success('邮件发送成功，请查收邮件');
            sessionStorage.setItem('has_send_email','true');
            LastSecond();
        }else{
            ElMessage.error('出了些问题...邮件发送失败...');
        }
    }).finally(()=>{
        email_dialog_visable.value = false;
        isloading.value = false;
    });
}

let timer = null;
// 倒计时60s
const LastSecond=()=>{
    last_second.value = 60;
    timer= setInterval(()=>{
        last_second.value --;
        if (last_second.value <= 0) {
            clearInterval(timer);
        }
    },1000)
}

// 验证验证码是否正确
// 图片验证码和邮箱验证码共用一个接口，但都需要再保留在同一个会话状态。
const ValiedValue=async ()=>{
    let boolean = false;
    try{
        await request.post('/util/verifycaptcha',{},{
            params:{
                code:code.value,
                type:props.tag
            },
            withCredentials: true
        })
        .then(res=>{
            if (res.code === '200') {
                boolean = res.data;
                if (!boolean) {
                    ElMessage.error("验证码错误")
                }
            }else{
                ElMessage.error('验证码已失效，请刷新验证码')
            }
        })
    }finally{
        return boolean;
    }
}

defineExpose({ValiedValue});

onMounted(()=>{
    if (props.tag === 'Email') {
        to_email.value = sessionStorage.getItem('info_email');
    }else{
        GenerateCaptcha();
    }
})

</script>
<style scoped>
.EmailText{
    width: 160px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}
.EmailText:hover{
    overflow: visible;
    white-space: pre-line;
}
</style>