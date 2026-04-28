import request from "@/utils/request";
import { onUnmounted, ref } from "vue";

/**
 * 审批计时器
 * 每间隔2分钟，发送短心跳复苏revive，连续失败两次执行长心跳检测
 * 15分钟后，发送长心跳检测(侧重于心跳停止over)，并关闭弹出框
 */
export function useApprovalTimer() {
    // 原本打算设计为记录失败次数，但是连续失败两次，失败时判断是否上次失败过即可。
    const reviveFail = ref(false);
    const isActive = ref(false);
    const reviveInterVal = ref(null);
    const overTimeout = ref(null);

    // 回调函数，用于支持自定义的结束处理方式
    let overCallBackFunction = null;
    
    // 方法所需要的参数，同回调函数传递
    let aid = null;     //  存储审批编号
    let userID = null;  //  存储审批人员ID
    let token = null;   //  存储密钥
    
    // 清除计时器数据
    const clearTimer=()=>{
        if (reviveInterVal.value) {
            clearInterval(reviveInterVal.value);
            reviveInterVal.value = null;
        }
        if (overTimeout.value) {
            clearTimeout(overTimeout.value);
            overTimeout.value = null;
        }
    };

    // 处理心跳停止over操作
    const handleOver=async ()=>{
        if (!isActive.value) return;

        try {
            await request.get("/approval/heartbeat/"+aid, {
                headers: {
                    'X-Current-User-ID': userID,
                    'X-Lock-Token': token,
                }
            });            
        } catch (error) {
            console.log("over操作存在异常");
        } finally {
            // 执行自定义回调函数
            if (overCallBackFunction) {
                overCallBackFunction();
            }
            stop();
        }
    }

    // 开始计时操作
    // 需要传递一个对象{ aid, userID, token, overFunction }
    const start=(config)=>{
        aid = config.aid;
        userID = config.userID;
        token = config.token;
        overCallBackFunction = config.overFunction;

        // 基础数据设置
        reviveFail.value = false;
        isActive.value = true;

        clearTimer();

        // 间隔两分钟的短心跳复苏
        reviveInterVal.value = setInterval(async ()=>{
            if (!isActive.value) return;
            try {
                const res = await request.get("/approval/heartbeat/revive/"+aid, {
                    headers: {
                        'X-Current-User-ID': userID,
                        'X-Lock-Token': token,
                    }
                });
                if (res.code === '200') {
                    reviveFail.value = false;
                } else {
                    if (reviveFail.value) {
                        await handleOver();
                    } else {
                        reviveFail.value = true;
                    }
                }
            } catch (error){
                // 请求异常也算失败
                if (reviveFail.value) {
                    await handleOver();
                } else {
                    reviveFail.value = true;
                }
                console.log('短心跳出现异常');
            }
        }, 2*60*1000);

        // 15分钟的长心跳停止
        overTimeout.value = setTimeout(() => {
            handleOver();
        }, 15*60*1000);
    };

    // 暂停计时操作
    const stop=()=>{
        isActive.value = false;
        reviveFail.value = false;
        clearTimer();
        overCallBackFunction = null;
    };

    onUnmounted(()=>{
        stop();
    })

    return {
        reviveFail,
        isActive,
        start,
        stop
    }
}

/**
 * 通用倒计时
 */
export function useCountdown(initialSeconds = 60) {
    const countdown = ref(0);
    const isCounting = ref(false);
    let timer = null;

    const startCountdown = () => {
        if (isCounting.value) return;
        
        countdown.value = initialSeconds;
        isCounting.value = true;
        
        if (timer) clearInterval(timer);
        timer = setInterval(() => {
            countdown.value -= 1;
            if (countdown.value <= 0) {
                stopCountdown();
            }
        }, 1000);
    };

    const stopCountdown = () => {
        isCounting.value = false;
        countdown.value = 0;
        if (timer) {
            clearInterval(timer);
            timer = null;
        }
    };

    onUnmounted(() => {
        stopCountdown();
    });

    return {
        countdown,
        isCounting,
        startCountdown,
        stopCountdown
    };
}