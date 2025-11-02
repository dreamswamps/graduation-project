<template>
    <div class="Box">
        <div class="Card" style="width: 80%; max-width: 720px; display: flex; justify-content: center;">
            <div class="TodoListBox">
                <el-button type="primary" class="DrawerButton" @click="drawer_form_visable = true">
                    添加新的待办事务
                </el-button>

                <div class="Collapse_Box">
                    <el-collapse accordion id="Collapse" v-model="active_line_name">
                        <!-- 绑定数据并指定主键 -->
                        <draggable v-model="data.collapse_list" item-key="id"
                            :animation="300" handle=".Handler"
                            @start="OnDragStart"
                            v-if="!loading">
                            <!-- #item="{ element }"element为默认的变量名 -->
                            <!-- draggable中的template，只能包含单个元素，在其中进行包裹。注释也算元素 -->
                            <!-- 进行包装只需要修改数据传递方式即可 -->
                            <template #item="{ element,index }">
                                <el-collapse-item class="Collapse_Item_Line" :name="element.id">
                                    <!-- 将数据替换为上面元素传递出的element数据 -->
                                    <template #title="{ isActive }">
                                        <div :class="['Collapse_Title',{ 'Is_Active': isActive }]"
                                            :tag-like="element.tag_like">
                                            <div class="Handler">
                                                <el-icon><DCaret /></el-icon>
                                            </div>
                                            <!-- el-text自动占据所有剩余空间,随便设定一个width: 1px;就可以让text不会超出范围，不知道原因，设置为百分比无效 -->
                                            <el-text :class="{'Text_ThroughLine' : element.tag_done}" style="flex: 1; width: 1px;" line-clamp="1" >
                                                {{ index + 1 }}. {{ element.title }}
                                            </el-text>
                                            <div style="display: flex; align-items: center; gap: 4px;">
                                                <img v-show="element.tag_like === 1" :src="heartfillRed" 
                                                    style="width: 16px;">
                                                <el-icon class="Collapse_Icon">
                                                    <arrow-right></arrow-right>
                                                </el-icon>
                                            </div>

                                        </div>
                                    </template>
                                    <div class="Collapse_Container" :tag-like="element.tag_like">
                                        <div class="Content_Box">
                                            {{ element.content }}
                                        </div>
                                        <div class="Time_Box">
                                            <div class="Time_Item">
                                                <el-icon><Clock /></el-icon>
                                                <span class="Time_Label">开始时间：</span>
                                                <span class="Time_Value">{{ element.start_time ? element.start_time: '未知' }}</span>
                                            </div>
                                            <div class="Time_Item">
                                                <el-icon><Clock /></el-icon>
                                                <span class="Time_Label">结束时间：</span>
                                                <span class="Time_Value">{{ element.end_time ? element.end_time: '未知' }}</span>
                                            </div>
                                        </div>
                                        <div class="Rate_Box">
                                            <el-rate
                                                v-model="element.rate"
                                                disabled
                                                show-score
                                                text-color="#ff9900"
                                                score-template="{value} 分"/>  
                                        </div>
                                        <div class="Operation_Box">
                                            <el-button @click="TodoEdit(element)" type="info" icon="Edit" circle />
                                            <RippleCircleButton
                                                :icon_src="element.tag_like == 0 ? heartIcon : heartfullIcon"
                                                :button_color="'#FF3852'"
                                                :class="{ 'IconActive' : isAnimation }"
                                                @click="TodoTag('Like',element.id)"
                                                style="max-width: 32px; max-height: 32px;"
                                            />
                                            <RippleCircleButton
                                                :icon_src="element.tag_done == 0 ? checkboxEmpty : checkboxChecked"
                                                :button_color="'#4B73FF'"
                                                :ripple_color="'#4B73FF'"
                                                :class="{ 'IconActive' : isAnimation }"
                                                @click="TodoTag('Done',element.id)"
                                                style="max-width: 32px; max-height: 32px;"
                                            />
                                            <el-button @click="TodoDelete(element.id)" type="danger" icon="Delete" circle/>
                                        </div>
                                    </div>
                                </el-collapse-item>
                            </template>
                        </draggable>
                    </el-collapse>
                    <img style="box-sizing: border-box;width: 100%;"
                        v-if="!loading && (!data.collapse_list || data.collapse_list.length === 0)" src="../assets/emptybox.png">
                </div>

                <el-button type="success" style="margin-top: 16px;" class="DrawerButton" @click="SaveDrag"
                    :disabled="!data.collapse_list || data.collapse_list.length === 0">
                    保存待办清单排序
                </el-button>
            </div>
        </div>
    </div>

    <!-- 上抽屉填写表单 -->
    <el-drawer
        v-model="drawer_form_visable"
        @open="DrawerOpenHandler"
        @close="DrawerCloseHanlder"
        direction="ttb"
        :show-close="false"
        size="60%"
        style="background: linear-gradient(135deg, #292a3a, #536976);">
        <template #header>
                <div style="display: flex; justify-content: space-between; align-items: center; height: 40px;">
                    <h4 style="font-size: 20px;color: white;">
                        {{ data.isEditMode ? '修改待办清单' : '填写代办清单'}} 
                    </h4>
                    <el-button style="font-size: 16px;" type="danger" @click="drawer_form_visable = false">
                        <el-icon style="margin-right: 8px;"><CircleCloseFilled /></el-icon>
                        关闭
                    </el-button>
                </div>
        </template>
        <template #default>
            <div style="display: flex; justify-content: center; height: 100%; overflow-y: hidden">
                <div class="Card" style="width: 60%; height: 100%; max-width: 600px; overflow-y: auto;padding: 16px;">
                    <el-form :rules="rules" ref="formRef" :model="data.form_new_Todo" label-width="60px">
                        <el-form-item label="标题" prop="title" required>
                            <el-input v-model="data.form_new_Todo.title" prefix-icon="Tickets" maxlength="40" show-word-limit/>
                        </el-form-item>
                        <el-form-item label="内容" prop="content">
                            <el-input v-model="data.form_new_Todo.content" type="textarea"
                            :autosize="{ minRows: 2, maxRows: 4}"
                            maxlength="200" show-word-limit/>
                        </el-form-item>
                        <el-form-item label="时间" prop="date">
                            <div style="display: flex;align-items: center;">
                            <el-date-picker
                                v-model="data.form_new_Todo.date"
                                type="datetimerange"
                                start-placeholder="开始日期"
                                end-placeholder="截止日期"
                                format="YYYY-MM-DD HH:mm:ss"
                                value-format="YYYY-MM-DD HH:mm:ss"
                                date-format="YYYY/MM/DD ddd"
                                time-format="hh:mm:ss"
                                clearable="false"
                                prefix-icon="Calendar"
                                :disabled-date="disabledDate"
                                :default-time="default_timerange"
                                @change="DateChangeHandler"
                                style="width: 90%;;"/>
                                <el-tooltip placement="bottom">
                                    <template #content>选择两点确定范围<br/>不可选择已过日期</template>
                                    <el-icon style="color: #409eff;margin-left: 8px; font-size: 18px;"><QuestionFilled /></el-icon>
                                </el-tooltip>
                            </div>

                        </el-form-item>
                        <el-form-item label="重要" prop="rate">
                            <el-rate v-model="data.form_new_Todo.rate" clearable :colors="colors"/>
                                <el-tooltip content="再次点击以重置评分" placement="right">
                                    <el-icon style="color: #409eff;margin-left: 8px; font-size: 18px;"><QuestionFilled /></el-icon>
                                </el-tooltip>
                        </el-form-item>
                    </el-form>
                </div>
            </div>

        </template>
        <template #footer>
            <div style="flex: auto;margin-top: 16px;">
                <el-tooltip content="修改时不允许重置💢" placement="top" :disabled="data.isEditMode === false">
                    <el-button size="large" style="font-size: 16px;letter-spacing: 2px;" type="warning" @click="FormReset" :disabled="data.isEditMode === true">重置</el-button>
                </el-tooltip>
                <el-tooltip :content="data.isSaved ? '粘贴后模板自动销毁' : '保存允许粘贴的模板'" placement="top">
                    <el-button size="large" style="font-size: 16px;letter-spacing: 2px;" :type="data.isSaved ? 'default' : 'primary'" @click="FormSave">{{ data.isSaved ? '粘贴模板' : '保存模板' }}</el-button>
                </el-tooltip>
                <el-tooltip :disabled="data.collapse_list.length <= 5" content="太多事没做...别拖延了" placement="top">
                    <el-button size="large" style="font-size: 16px;letter-spacing: 2px;" type="success" @click="FormSubmit" :disabled="data.collapse_list.length > 5">完成</el-button>
                </el-tooltip>
            </div>
        </template>
    </el-drawer>
</template>
<script setup>
import request from '@/utils/request';
import { ElMessage, ElMessageBox } from 'element-plus';
import { inject, onMounted, reactive, ref } from 'vue';
import draggable from 'vuedraggable';
import heartIcon from '@/assets/heart.svg';
import heartfullIcon from '@/assets/heartfull.svg';
import heartfillRed from '@/assets/heart_fill_red.svg'
import checkboxEmpty from '@/assets/checkbox_empty.svg';
import checkboxChecked from '@/assets/checkbox_checked.svg';
import RippleCircleButton from '@/components/base/RippleCircleButton.vue';

const data = reactive({
    new_Todo : null,
    form_new_Todo : {
        date : []
    },
    collapse_list : {
        length : 0
    },
    isEditMode : false,
    isSaved : false,
    form_save_Todo : {},
    form_cache_Todo : {}
})

const loading = ref(true);

// 禁用已过日期
const disabledDate=(time)=>{
    return time.getTime() < Date.now() - 24 * 60 * 60 * 1000
}

// 默认的时间，现在时间+一小时后
const default_timerange = [
    new Date(),
    new Date(Date.now() + 60 * 60 * 1000)
]

const userID = inject('UserID');

const colors = ref(['#BBDEFB', '#F7BA2A', '#FF9900'])

const drawer_form_visable = ref(false)

const formRef = ref()

const rules = {
    title:[
        { required:true, message:'请填写待办事务标题！', trigger:'blur'}
    ]
}

onMounted(()=>{
    FormLoad();
})

// 拆解日期范围用于存储
const DateChangeHandler=()=>{
    // console.log(data.form_new_Todo.date);
    
    if (data.form_new_Todo.date){
        data.form_new_Todo.start_time = data.form_new_Todo.date[0];
        data.form_new_Todo.end_time = data.form_new_Todo.date[1];
    }else {
        data.form_new_Todo.start_time = null;
        data.form_new_Todo.end_time = null;
    }
}

// 抽屉打开数据复原
const DrawerOpenHandler=()=>{
    if (!data.isEditMode){
        data.form_new_Todo = data.form_cache_Todo;
        data.form_cache_Todo = {};
    }
}

// 抽屉关闭逻辑处理
const DrawerCloseHanlder=()=>{
    // 如果关闭抽屉时是编辑状态，则清空表单中的数据
    if (data.isEditMode){
        data.form_new_Todo = {
            date : []
        }
    }else{
        // 将添加状态的表单存入缓存数组，尝试避免在添加状态编辑数据后，进入编辑状态，数据也被清空的状态
        data.form_cache_Todo = data.form_new_Todo;
    }
    data.isEditMode = false;
}

// 统一报错处理
const RequestExceptionHandler=(res)=>{
    ElMessage.error(res.msg);
}

// 折叠面板操作
const FormSubmit=()=>{
    data.form_new_Todo.index = data.collapse_list.length;
    console.log(data.form_new_Todo);
    
    formRef.value.validate((valid)=>{
        if(valid){
            data.isEditMode ? FormUpdate() : FormAdd()
        }
    })
}

const FormSave=()=>{
    if (!data.isSaved){
        data.form_save_Todo = data.form_new_Todo;
        // console.log(data.form_save_Todo); 
    }else {
        data.form_new_Todo = data.form_save_Todo;
    }
    data.isSaved = !data.isSaved
}

const FormReset=()=>{
    console.log(data.collapse_list);
    
    ElMessageBox.confirm('是否确定重置表单信息？','确定',{type:'warning'}).then(()=>{
        ElMessage.success('表单已重置');
        formRef.value.resetFields();
    }).catch(()=>{})
}

const FormLoad=()=>{
    request.get('/todo/selecttodolistbyid',{
        headers:{
            'X-Current-User-ID': userID,
        }}).then(res=>{
            if(res.code === '200'){
                // 根据index进行排序
                data.collapse_list = res.data.sort((a, b) => a.index - b.index);
                loading.value = false;
                // console.log(data.collapse_list);
            }else{
                RequestExceptionHandler(res);
            }
        }).catch(()=>{})
}

const FormAdd=()=>{
    ElMessageBox.confirm('信息填写完毕？','确定',{type:'primary'}).then(()=>{
        request.post('/todo/inserttodolist',data.form_new_Todo,{
        headers:{
            'X-Current-User-ID': userID,
        }}).then(res=>{
            if(res.code === '200'){
                drawer_form_visable.value = false;
                ElMessage.success('新增操作成功！');
                formRef.value.resetFields();
                FormLoad();
            }else{
                RequestExceptionHandler(res);
            }
        })
    }).catch((error)=>{
        // console.log(error);
        ElMessage.error('取消提交')
    })
}

// 待办事务三操作
const TodoEdit=(item)=>{

    // 提供时延确保数据完成绑定再进行渲染，尝试避免可能出现的虽然数据已绑定，但是视图渲染时仍未绑定完成的现象
    setTimeout(()=>{
        if (item.start_time && item.end_time){
            data.form_new_Todo.date = [item.start_time, item.end_time];
        }
    },50)
    // console.log(data.form_new_Todo.date);
    data.form_new_Todo = item;
    data.isEditMode = true;
    drawer_form_visable.value = true;
}

const isAnimation = ref(false);


const TodoTag=(tag,id)=>{
    isAnimation.value = true;
    setTimeout(()=>{
        isAnimation.value = false;
    },300)
    request.put('/todo/tagtoggle/'+id + '?tag=' + tag).then(res=>{
                if(res.code === '200'){
                    // ElMessage.success(res.data);
                    FormLoad();
                }else{
                    RequestExceptionHandler(res);
                }
            })
}

const TodoDelete=(id)=>{
    ElMessageBox(
        {
            title: '',
            dangerouslyUseHTMLString: true,
            center: true,
            message: `
                <div style="display: flex; align-items: center; margin-bottom: 10px; justify-content: center">
                    <img src="/src/assets/fire.svg" width="30px" style="display: block;">
                    <span style="margin-left: 8px; font-weight: 700; font-size: 24px">注意</span>
                </div>
                <div>删除待办事务是不可撤回的！</div>        
                `,
            confirmButtonText: '烧了',
            cancelButtonText: '蒜鸟',
            showCancelButton: true
        }).then(()=>{
            request.delete('/todo/harddeletetodolist/'+id,{
                headers:{
                    'X-Current-User-ID': userID,
                }
            }).then(res=>{
                if(res.code === '200'){
                    ElMessage({
                        message:'燃尽了...只剩下了雪白的灰',
                        type: null,
                        customClass: 'el-message--info',
                    });
                    FormLoad();
                }else{
                    RequestExceptionHandler(res);
                }
            })
        })
}

// 关于Drag操作
const active_line_name = ref('')    // 当前展开的行名称

const OnDragStart=()=>{
    active_line_name.value = '';
}

const SaveDrag=()=>{
    ElMessageBox.confirm('确认保存顺序？','确定').then(()=>{
        request.put('/todo/saveindex',data.collapse_list,{
                headers:{
                    'X-Current-User-ID': userID,
                }
            }).then(res=>{
                if(res.code === '200'){
                    ElMessage.success(res.data);
                }else{
                    RequestExceptionHandler(res);
                }
            })
    })
}

</script>
<style scoped>
.Box{   
    padding-top: 32px;
    width: 100%;
    display: flex;
    justify-content: center;
}
.TodoListBox{
    padding: 12px;
    width: 80%;
}
.DrawerButton{
    padding: 16px;
    width: 100%;
    font-size: 16px; 
    letter-spacing: 2px;
}
.DrawerButton:hover{
    transform: scale(1.1);
    color: black;
}
.el-form-item{
    margin-bottom: 24px;
}
.Collapse_Box{
    margin-top: 24px;
    border: 1px solid rgba(0, 0, 0, 0.8);
    border-radius: 5px;
    padding: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
    background-color: rgba(0, 0, 0, 0.1);
}
.Text_ThroughLine{
    text-decoration: line-through;
    opacity: 0.6;
}
.Collapse_Item_Line{
    box-shadow: -1px -2px 2px rgba(0, 0, 0, 0.5);
    border-radius: 5px;
}
.Collapse_Item_Line:not(:last-child){
    margin-bottom: 12px;
}
.Collapse_Title{
    display: flex;
    align-items: center;
    justify-content: space-between;
    position: relative;
    /* border-bottom: 1px solid rgba(0, 0, 0, 0.2); */
    transition: all 0.3s ease;
    overflow: hidden;
    color: #000;
    text-indent: 1em;
    /* 移动到上层，可见 */
    width: 100%;
}
.Collapse_Icon{
    margin-right: 16px;
    color: #000;
    transition: transform 0.3s ease;
}
.Collapse_Title.Is_Active .Collapse_Icon{
    transform: rotate(90deg);
}
.Collapse_Title::before {
    content: '';
    position: absolute;
    top: 0;
    /* 一开始在不可见区域 */
    left: -100%;
    width: 100%;
    height: 100%;
    transition: left 0.3s ease;
    /* 在下层，保证不可见不遮挡 */
    z-index: -1;
}
.Collapse_Title[tag-like="0"]::before{
    background:linear-gradient(to right, white, #c5ecff);
}
.Collapse_Title[tag-like="1"]::before{
    background:linear-gradient(to right, white, #FF6356);
}
.Collapse_Title.Is_Active::before {
    left: 0; /* 激活时背景从左侧移入 */
}
.Collapse_Container{
    padding: 16px;
    border-radius: 0px 0px 5px 5px;
}
.Collapse_Container[tag-like="0"]{
    background: linear-gradient(220.55deg, #7CF7FF 0%, #4B73FF 100%);
}
.Collapse_Container[tag-like="1"]{
    background:linear-gradient(220.55deg, #FF3F3F 0%, #063CFF 100%);
}
.Content_Box{
    border-radius: 5px;
    background:linear-gradient(220.55deg, #FFF6EB 0%, #DFD1C5 100%);
    padding: 4px;
    text-indent: 2em;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
    max-width: 100%;
    box-sizing: border-box;
    /* 允许长单词换行 */
    word-wrap: break-word;
}
.Time_Box{
    margin-top: 16px;
}
.Time_Item{
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 8px;
    color: whitesmoke;
    font-size: 14px;
}
.Time_Item .el-icon{
    margin-right: 8px;
    color: #F7BA2A;
}
.Time_Label{
    font-weight: 500;
}
.Time_Value{
    font-weight: 500;
}
.Rate_Box{
    display: flex;
    align-items: center;
    justify-content: center;
}
.Operation_Box{
    margin: 4px;
    margin-top: 12px;
    display: flex;
    justify-content: right;
    gap: 12px;
    /* transition: transform 0.3s ease; */
}
/* .Operation_Box .el-button:hover{
    transform: scale(1.2);
} */
</style>
<style scoped>
/* 该style用于实现draggable */
.Handler{
    cursor: grab;
    user-select: none;
}
.Handler:active{
    cursor: grabbing;
}
</style>
<style>
#Collapse .el-collapse-item__arrow{
    display: none;
    /* 隐藏自带的icon图标 */
}
/* 强制修改组件自带的padding问题 */
#Collapse .el-collapse-item__content{
    padding-bottom: 0px;
}
.Collapse_Box .el-collapse-icon-position-right .el-collapse-item__header{
    padding-right: 0px;
}
</style>