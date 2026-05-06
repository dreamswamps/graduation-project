<template>
    <div class="data-container">
        <div class="page-header">
            <h2 class="page-title">数据统计</h2>
        </div>

        <div class="charts-row">
            <div id="bar_echarts" class="Card chart-card"></div>
            <div id="pie_echarts" class="Card chart-card"></div>
            
        </div>

        <div class="charts-row" style="display: none;">
            <div id="line_echarts" class="Card chart-card"></div>
            <div id="test_echarts" class="Card chart-card"></div>
        </div>
    </div>
</template>
<script setup>
import { onMounted, onUnmounted, reactive } from 'vue';
import * as echarts from 'echarts';
import request from '@/utils/request';
import { ElMessage } from 'element-plus';

const data = reactive({
    genderNameList:[],
    genderCountList:[],
    dateCountList:[],
    dateRangeStrList:[],
    rolePieMap:[]
})

let option_bar = reactive({});
let option_line = reactive({});
let option_pie = reactive({});

// 统一组装option
const OptionBuildFactory=()=>{
    // 颜色数组
    const color_palette = ['#ff6b6b', '#ff9e6b', '#ffd56b', '#90ee90', '#87ceeb', '#9370db', '#ba55d3']
    
    option_bar = {
        title:{
            text: '性别人数统计'
        },
        tooltip: {
            // 有默认样式
        },
        label:{
            show:true,
            position: 'top'
        },
        xAxis: {
            name: '性别',
            nameLocation: 'end',    //  显示位置，默认就在末端
            data: data.genderNameList,
        },
        yAxis: {
            type: 'value',
            name: '人数',
            minInterval: 1, //  最小为1
            axisLabel: {
                formatter: '{value}' // 格式化为简单数值，不添加小数点
            }
        },
        series: [
            {
            name: '人数',
            type: 'bar',
            data: data.genderCountList.map((value,index)=>({
                value:value,
                itemStyle: {
                    color: color_palette[index % color_palette.length]
                }
            })),
            }
        ]
    }

    option_line = {
        title:{
            text: '新文章统计'
        },
        tooltip:{},
        label:{
            show:true   //  显示数字，默认在上方
        },
        xAxis: {
            type: 'category',
            data: data.dateRangeStrList,
        },
        yAxis: {
            type: 'value',
            name: '文章数量',
            nameLocation: 'end',
            // 确保纵坐标为整数
            minInterval: 1,
            axisLabel: {
                formatter: '{value}' // 确保显示整数
            }
        },
        series: [
            {
                name:'文章数量',
                data: data.dateCountList,
                type: 'line'
            }
        ]
    }

    option_pie = {
        title:{
            text: '账号权限统计',
            subtext: 'AorM',
        },
        tooltip:{},
        legend:{
            // 图案角标
            orient: 'vertical', //  垂直排列，默认横向可换行
            left: 'left'    //  默认居中
        },
        series:[{
            name: '权限',
            type: 'pie',
            data: data.rolePieMap,
            emphasis:{
                itemStyle:{
                    shadowBlur: 10,
                    shadowOffsetX: 0,
                    shadowColor: 'rgba(0, 0, 0, 0.5)'
                }
            }
        }],
    }
}

// 提前定义需要使用的实例引用
let BarEChart = null;
let LineEchart = null;
let PieEchart = null;

// 绘制Echarts图
const EchartsMaker = () => {
    const barElement = document.getElementById('bar_echarts');
    const lineElement = document.getElementById('line_echarts');
    const pieElement = document.getElementById('pie_echarts');
    
    if (barElement && lineElement && pieElement) {
        // 初始化Echarts，只初始化一次
        if (!BarEChart){
            BarEChart = echarts.init(barElement);
            LineEchart = echarts.init(lineElement);
            PieEchart = echarts.init(pieElement);

            // 强制等待渲染完毕后再进行事件监听，可以避免出现因为事件监听导致的强制刷新而失去动效
            Promise.all([
                new Promise(resolve => BarEChart.on('finished', resolve)),
                new Promise(resolve => LineEchart.on('finished', resolve)),
                new Promise(resolve => PieEchart.on('finished', resolve))
            ]).then(()=>{
                // 使用ECharts自带的resize监听=>窗口变化
                // ResizeObserver检测dom元素的尺寸变化
                const observer = new ResizeObserver(() => {
                    // 延迟渲染
                    setTimeout(() => {
                        BarEChart.resize(); //  自动计算调整尺寸+重新绘制
                        LineEchart.resize();
                        PieEchart.resize();
                    },16);
                });
                // 监听两个绘制容器的尺寸变化。窗口大小变化=>尺寸变化=>事件监听
                observer.observe(barElement);
                observer.observe(lineElement);
                observer.observe(pieElement);
                
                //  卸载生命周期
                onUnmounted(() => {
                    observer.disconnect();  //  结束监听
                    BarEChart.dispose();    //  销毁实例
                    LineEchart.dispose();
                    PieEchart.dispose();
                    BarEChart = null;
                    LineEchart = null;
                    PieEchart = null;
                });
            });
        }
        BarEChart.setOption(option_bar);
        LineEchart.setOption(option_line);
        PieEchart.setOption(option_pie);
    }
}

// 获取绘制图表所需的所有数据+数据处理
const InitializeDate=async ()=>{
    try{
        // 并发请求+等待响应
        const [gender_res,article_res,role_res] = await Promise.all([
            request.get('/admin/gendernumberselect'),
            request.get('/article/articlerecentselect'),
            request.get('/rolenumberselect')
        ]);

        // 处理性别数据
        if (gender_res.code === '200'){
            data.genderNameList = gender_res.data.genderNameList;
            data.genderCountList = gender_res.data.genderCountList;
        }else{
            ElMessage.error(gender_res.msg);
        }

        // 处理文章数据
        if (article_res.code === '200'){
            data.dateCountList = article_res.data.dateCountList;
            data.dateRangeStrList = article_res.data.dateRangeStrList;
        }else{
            ElMessage.error(article_res.msg);
        }

        // 处理职责数据
        if (role_res.code === '200'){
            data.rolePieMap = role_res.data;
            // console.log(data.rolePieMap);
            
        }else{
            ElMessage.error(role_res.msg);
        }

        OptionBuildFactory();
        EchartsMaker();
    }catch(e){
        ElMessage.error("数据加载失败！");
        console.log(e);
    }
}

// 等待所有组件加载完毕再申请数据，避免出现获取数据但是未加载组件的情况
onMounted(()=>{
    InitializeDate();
})


</script>
<style scoped>
/* 页面容器 */
.data-container {
    padding: 24px;
    background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf4 100%);
    min-height: 100vh;
    animation: fadeIn 0.5s ease-in;
}

@keyframes fadeIn {
    from {
        opacity: 0;
        transform: translateY(10px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

/* 页面标题区域 */
.page-header {
    margin-bottom: 32px;
    text-align: center;
    animation: slideDown 0.6s ease-out;
}

@keyframes slideDown {
    from {
        opacity: 0;
        transform: translateY(-20px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.page-title {
    font-size: 32px;
    font-weight: 600;
    color: #2c3e50;
    margin: 0 0 8px 0;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
}

.page-subtitle {
    font-size: 14px;
    color: #7f8c8d;
    margin: 0;
}

/* 图表行容器 */
.charts-row {
    display: flex;
    gap: 24px;
    margin-bottom: 24px;
    animation: slideUp 0.7s ease-out;
}

@keyframes slideUp {
    from {
        opacity: 0;
        transform: translateY(20px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

/* 统一卡片风格 */
.Card {
    background: #ffffff;
    border-radius: 20px;
    box-shadow: 0 8px 24px rgba(102, 126, 234, 0.12);
    padding: 28px 36px;
    min-width: 300px;
    min-height: 300px;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    border: 1px solid rgba(102, 126, 234, 0.08);
    position: relative;
    overflow: hidden;
}

.Card::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 4px;
    background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
    transform: scaleX(0);
    transition: transform 0.3s ease;
}

.Card:hover::before {
    transform: scaleX(1);
}

.Card:hover {
    box-shadow: 0 12px 40px rgba(102, 126, 234, 0.20);
    transform: translateY(-4px);
    border-color: rgba(102, 126, 234, 0.15);
}

/* 图表卡片 */
.chart-card {
    flex: 1;
    height: 420px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
    .charts-row {
        flex-direction: column;
    }

    .chart-card {
        height: 380px;
    }
}

@media (max-width: 768px) {
    .data-container {
        padding: 16px;
    }

    .page-title {
        font-size: 24px;
    }

    .charts-row {
        gap: 16px;
        margin-bottom: 16px;
    }

    .Card {
        padding: 20px 24px;
    }

    .chart-card {
        height: 320px;
    }
}
</style>