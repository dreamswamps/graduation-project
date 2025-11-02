import axios from "axios";
import { ElMessage } from "element-plus";

const baseURL = import.meta.env.VITE_API_BASE_URL;

const request = axios.create({
    baseURL:baseURL,
    timeout:30000   // 后端接口超时时间
})

// 设置request拦截器
request.interceptors.request.use(
    config => {
        config.headers['Content-Type'] = 'application/json;charset=UTF-8'
        return config
    },
    error => {
        return Promise.reject(error)
    }
)

// 设置response拦截器
request.interceptors.response.use(
    response => {
        // console.log(response);
        
        let res = response.data
        if (typeof res === 'string'){
            // 判断res是否为空字符串，若为空，则不修改，否则转换成json格式数据
            res = res ? JSON.parse(res) : res
        }
        return res
    },
    error => {
        if (error.response.status === 404){
            ElMessage.error("未能找到请求接口！")
        }else if(error.response.status === 500){
            ElMessage.error("系统异常，请检查后端控制台报错内容！")
        }else{
            console.error(error.messgae)
        }
        return Promise.reject(error)
    }
)

export default request