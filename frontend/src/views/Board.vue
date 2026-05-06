<template>
    <div class="board-container">
        <div class="page-header">
            <h2 class="page-title">待办清单</h2>
        </div>

        <div class="Card main-card">
            <div class="TodoListBox">
                <el-button type="primary" class="DrawerButton" @click="drawer_form_visable = true">
                    添加新的待办事务
                </el-button>

                <div class="Collapse_Box">
                    <el-collapse accordion id="Collapse" v-model="active_line_name">
                        <draggable v-model="data.collapse_list" item-key="id"
                            :animation="300" handle=".Handler"
                            @start="OnDragStart"
                            v-if="!loading">
                            <template #item="{ element, index }">
                                <el-collapse-item class="Collapse_Item_Line" :name="element.id">
                                    <template #title="{ isActive }">
                                        <div :class="['Collapse_Title', { 'Is_Active': isActive }]"
                                            :tag-like="element.tag_like">
                                            <div class="Handler">
                                                <el-icon><DCaret /></el-icon>
                                            </div>
                                            <el-text :class="{ 'Text_ThroughLine': element.tag_done }" class="Title_Text" line-clamp="1">
                                                {{ index + 1 }}. {{ element.title }}
                                            </el-text>
                                            <div class="Title_Icons">
                                                <img v-show="element.tag_like === 1" :src="heartfillRed" class="Heart_Icon">
                                                <el-icon class="Collapse_Icon"><arrow-right /></el-icon>
                                            </div>
                                        </div>
                                    </template>
                                    <div class="Collapse_Container" :tag-like="element.tag_like">
                                        <div class="Content_Box">{{ element.content }}</div>
                                        <div class="Time_Box">
                                            <div class="Time_Item">
                                                <el-icon><Clock /></el-icon>
                                                <span class="Time_Label">开始</span>
                                                <span class="Time_Value">{{ element.start_time || '未设置' }}</span>
                                            </div>
                                            <div class="Time_Item">
                                                <el-icon><Clock /></el-icon>
                                                <span class="Time_Label">截止</span>
                                                <span class="Time_Value">{{ element.end_time || '未设置' }}</span>
                                            </div>
                                        </div>
                                        <div class="Rate_Box">
                                            <el-rate v-model="element.rate" disabled show-score
                                                text-color="#ff9900" score-template="{value} 分" />
                                        </div>
                                        <div class="Operation_Box">
                                            <el-button @click="TodoEdit(element)" type="info" icon="Edit" circle />
                                            <RippleCircleButton
                                                :icon_src="element.tag_like == 0 ? heartIcon : heartfullIcon"
                                                :button_color="'#FF3852'"
                                                :class="{ 'IconActive': isAnimation }"
                                                @click="TodoTag('Like', element.id)"
                                                style="max-width: 32px; max-height: 32px;" />
                                            <RippleCircleButton
                                                :icon_src="element.tag_done == 0 ? checkboxEmpty : checkboxChecked"
                                                :button_color="'#4B73FF'"
                                                :ripple_color="'#4B73FF'"
                                                :class="{ 'IconActive': isAnimation }"
                                                @click="TodoTag('Done', element.id)"
                                                style="max-width: 32px; max-height: 32px;" />
                                            <el-button @click="TodoDelete(element.id)" type="danger" icon="Delete" circle />
                                        </div>
                                    </div>
                                </el-collapse-item>
                            </template>
                        </draggable>
                    </el-collapse>
                    <img class="Empty_Img"
                        v-if="!loading && (!data.collapse_list || data.collapse_list.length === 0)"
                        src="../assets/emptybox.png">
                </div>

                <el-button type="success" class="DrawerButton SaveButton" @click="SaveDrag"
                    :disabled="!data.collapse_list || data.collapse_list.length === 0">
                    保存待办清单排序
                </el-button>
            </div>
        </div>
    </div>

    <el-drawer
        v-model="drawer_form_visable"
        @open="DrawerOpenHandler"
        @close="DrawerCloseHanlder"
        direction="ttb"
        :show-close="false"
        size="60%"
        class="Todo_Drawer">
        <template #header>
            <div class="Drawer_Header">
                <h4 class="Drawer_Title">{{ data.isEditMode ? '修改待办清单' : '填写待办清单' }}</h4>
                <el-button type="danger" @click="drawer_form_visable = false" class="Drawer_Close_Btn">
                    <el-icon style="margin-right: 6px;"><CircleCloseFilled /></el-icon>关闭
                </el-button>
            </div>
        </template>
        <template #default>
            <div class="Drawer_Body">
                <div class="Card Drawer_Form_Card">
                    <el-form :rules="rules" ref="formRef" :model="data.form_new_Todo" label-width="60px">
                        <el-form-item label="标题" prop="title" required>
                            <el-input v-model="data.form_new_Todo.title" prefix-icon="Tickets" maxlength="40" show-word-limit />
                        </el-form-item>
                        <el-form-item label="内容" prop="content">
                            <el-input v-model="data.form_new_Todo.content" type="textarea"
                                :autosize="{ minRows: 2, maxRows: 4 }"
                                maxlength="200" show-word-limit />
                        </el-form-item>
                        <el-form-item label="时间" prop="date">
                            <div class="Date_Row">
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
                                    style="width: 90%;" />
                                <el-tooltip placement="bottom">
                                    <template #content>选择两点确定范围<br/>不可选择已过日期</template>
                                    <el-icon class="Help_Icon"><QuestionFilled /></el-icon>
                                </el-tooltip>
                            </div>
                        </el-form-item>
                        <el-form-item label="重要" prop="rate">
                            <el-rate v-model="data.form_new_Todo.rate" clearable :colors="colors" />
                            <el-tooltip content="再次点击以重置评分" placement="right">
                                <el-icon class="Help_Icon"><QuestionFilled /></el-icon>
                            </el-tooltip>
                        </el-form-item>
                    </el-form>
                </div>
            </div>
        </template>
        <template #footer>
            <div class="Drawer_Footer">
                <el-tooltip content="修改时不允许重置💢" placement="top" :disabled="!data.isEditMode">
                    <el-button size="large" class="Footer_Btn" type="warning" @click="FormReset" :disabled="data.isEditMode">重置</el-button>
                </el-tooltip>
                <el-tooltip :content="data.isSaved ? '粘贴后模板自动销毁' : '保存允许粘贴的模板'" placement="top">
                    <el-button size="large" class="Footer_Btn" :type="data.isSaved ? 'default' : 'primary'" @click="FormSave">
                        {{ data.isSaved ? '粘贴模板' : '保存模板' }}
                    </el-button>
                </el-tooltip>
                <el-tooltip :disabled="data.collapse_list.length <= 5" content="太多事没做...别拖延了" placement="top">
                    <el-button size="large" class="Footer_Btn" type="success" @click="FormSubmit" :disabled="data.collapse_list.length > 5">完成</el-button>
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
    new_Todo: null,
    form_new_Todo: { date: [] },
    collapse_list: { length: 0 },
    isEditMode: false,
    isSaved: false,
    form_save_Todo: {},
    form_cache_Todo: {}
})

const loading = ref(true);

const disabledDate = (time) => time.getTime() < Date.now() - 24 * 60 * 60 * 1000

const default_timerange = [new Date(), new Date(Date.now() + 60 * 60 * 1000)]

const userID = inject('UserID');
const colors = ref(['#BBDEFB', '#F7BA2A', '#FF9900'])
const drawer_form_visable = ref(false)
const formRef = ref()

const rules = {
    title: [{ required: true, message: '请填写待办事务标题！', trigger: 'blur' }]
}

onMounted(() => { FormLoad(); })

const DateChangeHandler = () => {
    if (data.form_new_Todo.date) {
        data.form_new_Todo.start_time = data.form_new_Todo.date[0];
        data.form_new_Todo.end_time = data.form_new_Todo.date[1];
    } else {
        data.form_new_Todo.start_time = null;
        data.form_new_Todo.end_time = null;
    }
}

const DrawerOpenHandler = () => {
    if (!data.isEditMode) {
        data.form_new_Todo = data.form_cache_Todo;
        data.form_cache_Todo = {};
    }
}

const DrawerCloseHanlder = () => {
    if (data.isEditMode) {
        data.form_new_Todo = { date: [] }
    } else {
        data.form_cache_Todo = data.form_new_Todo;
    }
    data.isEditMode = false;
}

const RequestExceptionHandler = (res) => { ElMessage.error(res.msg); }

const FormSubmit = () => {
    data.form_new_Todo.index = data.collapse_list.length;
    formRef.value.validate((valid) => {
        if (valid) { data.isEditMode ? FormUpdate() : FormAdd() }
    })
}

const FormSave = () => {
    if (!data.isSaved) {
        data.form_save_Todo = data.form_new_Todo;
    } else {
        data.form_new_Todo = data.form_save_Todo;
    }
    data.isSaved = !data.isSaved
}

const FormReset = () => {
    ElMessageBox.confirm('是否确定重置表单信息？', '确定', { type: 'warning' }).then(() => {
        ElMessage.success('表单已重置');
        formRef.value.resetFields();
    }).catch(() => {})
}

const FormLoad = () => {
    request.get('/todo/selecttodolistbyid', {
        headers: { 'X-Current-User-ID': userID }
    }).then(res => {
        if (res.code === '200') {
            data.collapse_list = res.data.sort((a, b) => a.index - b.index);
            loading.value = false;
        } else {
            RequestExceptionHandler(res);
        }
    }).catch(() => {})
}

const FormAdd = () => {
    ElMessageBox.confirm('信息填写完毕？', '确定', { type: 'primary' }).then(() => {
        request.post('/todo/inserttodolist', data.form_new_Todo, {
            headers: { 'X-Current-User-ID': userID }
        }).then(res => {
            if (res.code === '200') {
                drawer_form_visable.value = false;
                ElMessage.success('新增操作成功！');
                formRef.value.resetFields();
                FormLoad();
            } else {
                RequestExceptionHandler(res);
            }
        })
    }).catch(() => { ElMessage.error('取消提交') })
}

const FormUpdate = () => {
    request.put('/todo/updatetodolist', data.form_new_Todo, {
        headers: { 'X-Current-User-ID': userID }
    }).then(res => {
        if (res.code === '200') {
            drawer_form_visable.value = false;
            ElMessage.success('修改操作成功！');
            FormLoad();
        } else {
            RequestExceptionHandler(res);
        }
    })
}

const TodoEdit = (item) => {
    setTimeout(() => {
        if (item.start_time && item.end_time) {
            data.form_new_Todo.date = [item.start_time, item.end_time];
        }
    }, 50)
    data.form_new_Todo = item;
    data.isEditMode = true;
    drawer_form_visable.value = true;
}

const isAnimation = ref(false);

const TodoTag = (tag, id) => {
    isAnimation.value = true;
    setTimeout(() => { isAnimation.value = false; }, 300)
    request.put('/todo/tagtoggle/' + id + '?tag=' + tag).then(res => {
        if (res.code === '200') {
            FormLoad();
        } else {
            RequestExceptionHandler(res);
        }
    })
}

const TodoDelete = (id) => {
    ElMessageBox({
        title: '',
        dangerouslyUseHTMLString: true,
        center: true,
        message: `
            <div style="display:flex;align-items:center;margin-bottom:10px;justify-content:center">
                <img src="/src/assets/fire.svg" width="30px" style="display:block;">
                <span style="margin-left:8px;font-weight:700;font-size:24px">注意</span>
            </div>
            <div>删除待办事务是不可撤回的！</div>`,
        confirmButtonText: '烧了',
        cancelButtonText: '蒜鸟',
        showCancelButton: true
    }).then(() => {
        request.delete('/todo/harddeletetodolist/' + id, {
            headers: { 'X-Current-User-ID': userID }
        }).then(res => {
            if (res.code === '200') {
                ElMessage({ message: '燃尽了...只剩下了雪白的灰', type: null, customClass: 'el-message--info' });
                FormLoad();
            } else {
                RequestExceptionHandler(res);
            }
        })
    })
}

const active_line_name = ref('')

const OnDragStart = () => { active_line_name.value = ''; }

const SaveDrag = () => {
    ElMessageBox.confirm('确认保存顺序？', '确定').then(() => {
        request.put('/todo/saveindex', data.collapse_list, {
            headers: { 'X-Current-User-ID': userID }
        }).then(res => {
            if (res.code === '200') {
                ElMessage.success(res.data);
            } else {
                RequestExceptionHandler(res);
            }
        })
    })
}
</script>
<style scoped>
/* 页面容器 */
.board-container {
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

/* 主卡片容器 */
.main-card {
    max-width: 800px;
    margin: 0 auto;
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

/* 待办列表容器 */
.TodoListBox {
    padding: 24px;
    width: 100%;
}

/* 统一卡片风格 */
.Card {
    background: #ffffff;
    border-radius: 20px;
    box-shadow: 0 8px 24px rgba(102, 126, 234, 0.12);
    padding: 32px;
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
    opacity: 0;
    transition: opacity 0.3s ease;
}

.Card:hover::before {
    opacity: 1;
}

.Card:hover {
    box-shadow: 0 12px 40px rgba(102, 126, 234, 0.20);
    transform: translateY(-2px);
    border-color: rgba(102, 126, 234, 0.15);
}

/* 抽屉按钮 */
/* 抽屉按钮 */
.DrawerButton {
    padding: 18px;
    width: 100%;
    font-size: 16px;
    font-weight: 500;
    letter-spacing: 2px;
    border-radius: 12px;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

.DrawerButton:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 20px rgba(102, 126, 234, 0.25);
}

.DrawerButton:active {
    transform: translateY(-1px);
}

.SaveButton {
    margin-top: 20px;
}

/* 折叠面板容器 */
.Collapse_Box {
    margin-top: 24px;
    border: 1px solid rgba(102, 126, 234, 0.12);
    border-radius: 12px;
    padding: 16px;
    box-shadow: 0 2px 12px rgba(102, 126, 234, 0.08);
    background: linear-gradient(135deg, rgba(255, 255, 255, 0.9) 0%, rgba(247, 250, 255, 0.9) 100%);
    backdrop-filter: blur(10px);
    transition: all 0.3s ease;
}

.Collapse_Box:hover {
    border-color: rgba(102, 126, 234, 0.2);
    box-shadow: 0 4px 16px rgba(102, 126, 234, 0.12);
}

/* 空状态图片 */
.Empty_Img {
    box-sizing: border-box;
    width: 100%;
    padding: 32px;
    opacity: 0.6;
    filter: grayscale(20%);
    transition: all 0.3s ease;
}

.Empty_Img:hover {
    opacity: 0.8;
    transform: scale(1.02);
}

/* 文本删除线 */
.Text_ThroughLine {
    text-decoration: line-through;
    opacity: 0.5;
    transition: opacity 0.3s ease;
}

.Title_Text {
    flex: 1;
    width: 1px;
    font-weight: 500;
}

.Title_Icons {
    display: flex;
    align-items: center;
    gap: 6px;
}

.Heart_Icon {
    width: 16px;
    animation: heartBeat 0.5s ease;
}

@keyframes heartBeat {
    0%, 100% { transform: scale(1); }
    25% { transform: scale(1.2); }
    50% { transform: scale(1); }
}

/* 折叠项 */
.Collapse_Item_Line {
    border-radius: 10px;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(102, 126, 234, 0.12);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    background: white;
}

.Collapse_Item_Line:hover {
    box-shadow: 0 4px 16px rgba(102, 126, 234, 0.18);
    transform: translateY(-2px);
}

.Collapse_Item_Line:not(:last-child) {
    margin-bottom: 12px;
}
/* 折叠标题 */
.Collapse_Title {
    display: flex;
    align-items: center;
    justify-content: space-between;
    position: relative;
    transition: all 0.3s ease;
    overflow: hidden;
    color: #2c3e50;
    text-indent: 0.5em;
    width: 100%;
    padding: 4px 0;
    min-height: 40px;
    line-height: 1.5;
}

.Collapse_Title > * {
    position: relative;
    z-index: 1;
    display: flex;
    align-items: center;
}

.Collapse_Icon {
    margin-right: 16px;
    color: #667eea;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    font-size: 16px;
}

.Collapse_Title.Is_Active .Collapse_Icon {
    transform: rotate(90deg);
    color: #764ba2;
}

.Collapse_Title::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    transition: left 0.4s cubic-bezier(0.4, 0, 0.2, 1);
    z-index: 0;
    transform: translateZ(0);
    backface-visibility: hidden;
    will-change: left;
}

.Collapse_Title[tag-like="0"]::before {
    background: linear-gradient(to right, #ffffff, #d6f0ff);
}

.Collapse_Title[tag-like="1"]::before {
    background: linear-gradient(to right, #ffffff, #ffd0cc);
}

.Collapse_Title.Is_Active::before {
    left: 0;
}
/* 折叠内容容器 */
.Collapse_Container {
    padding: 20px;
    border-radius: 0 0 10px 10px;
}

.Collapse_Container[tag-like="0"] {
    background: linear-gradient(220.55deg, #7CF7FF 0%, #4B73FF 100%);
}

.Collapse_Container[tag-like="1"] {
    background: linear-gradient(220.55deg, #FF3F3F 0%, #063CFF 100%);
}

.Collapse_Container > * {
    position: relative;
    z-index: 1;
}

/* 内容框 */
.Content_Box {
    border-radius: 10px;
    background: linear-gradient(220.55deg, #FFF6EB 0%, #e8ddd5 100%);
    padding: 16px 20px;
    text-indent: 2em;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    word-wrap: break-word;
    line-height: 1.8;
    font-size: 14px;
    color: #2c3e50;
    min-height: 48px;
    transition: all 0.3s ease;
}

.Content_Box:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
    transform: translateY(-1px);
}
/* 时间显示 */
.Time_Box {
    margin-top: 16px;
    display: flex;
    gap: 12px;
    justify-content: center;
    flex-wrap: wrap;
}

.Time_Item {
    display: flex;
    align-items: center;
    gap: 6px;
    color: rgba(255, 255, 255, 0.95);
    font-size: 13px;
    background: rgba(255, 255, 255, 0.2);
    backdrop-filter: blur(10px);
    padding: 8px 16px;
    border-radius: 24px;
    border: 1px solid rgba(255, 255, 255, 0.3);
    transition: all 0.3s ease;
}

.Time_Item:hover {
    background: rgba(255, 255, 255, 0.25);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.Time_Item .el-icon {
    color: #ffd700;
    font-size: 14px;
}

.Time_Label {
    font-weight: 600;
    opacity: 0.9;
}

.Time_Value {
    font-weight: 400;
}

/* 评分显示 */
.Rate_Box {
    display: flex;
    align-items: center;
    justify-content: center;
    margin-top: 14px;
    padding: 8px;
    background: rgba(255, 255, 255, 0.15);
    border-radius: 12px;
    backdrop-filter: blur(10px);
}

/* 操作按钮组 */
.Operation_Box {
    margin-top: 16px;
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    flex-wrap: wrap;
}
/* 拖拽手柄 */
.Handler {
    cursor: grab;
    user-select: none;
    padding: 0 8px;
    color: #95a5a6;
    transition: all 0.3s ease;
    border-radius: 4px;
}

.Handler:hover {
    color: #667eea;
    background: rgba(102, 126, 234, 0.1);
}

.Handler:active {
    cursor: grabbing;
    color: #764ba2;
}

/* 抽屉样式 */
.Drawer_Header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    height: 48px;
    padding: 0 8px;
}

.Drawer_Title {
    font-size: 22px;
    color: white;
    margin: 0;
    font-weight: 600;
    letter-spacing: 1.5px;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.Drawer_Close_Btn {
    font-size: 15px;
    font-weight: 500;
    transition: all 0.3s ease;
}

.Drawer_Close_Btn:hover {
    transform: scale(1.05);
}

.Drawer_Body {
    display: flex;
    justify-content: center;
    height: 100%;
    overflow-y: hidden;
    padding: 20px;
}

.Drawer_Form_Card {
    width: 90%;
    height: 100%;
    max-width: 680px;
    overflow-y: auto;
    padding: 32px;
    border-radius: 16px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
}
/* 表单行 */
.Date_Row {
    display: flex;
    align-items: center;
    gap: 8px;
}

.Help_Icon {
    color: #667eea;
    margin-left: 8px;
    font-size: 18px;
    cursor: help;
    transition: all 0.3s ease;
}

.Help_Icon:hover {
    color: #764ba2;
    transform: scale(1.1);
}

/* 抽屉底部 */
.Drawer_Footer {
    flex: auto;
    margin-top: 16px;
    display: flex;
    gap: 12px;
    justify-content: center;
    flex-wrap: wrap;
}

.Footer_Btn {
    font-size: 15px;
    letter-spacing: 2px;
    padding: 12px 28px;
    border-radius: 10px;
    font-weight: 500;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.Footer_Btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
}

.Footer_Btn:active {
    transform: translateY(0);
}

.el-form-item {
    margin-bottom: 24px;
}

/* 响应式设计 */
@media (max-width: 768px) {
    .board-container {
        padding: 16px;
    }

    .page-title {
        font-size: 24px;
    }

    .main-card {
        padding: 20px;
    }

    .TodoListBox {
        padding: 16px;
    }

    .Drawer_Form_Card {
        width: 95%;
        padding: 20px;
    }

    .Footer_Btn {
        flex: 1;
        min-width: 120px;
    }
}
</style>
<style>
.Todo_Drawer {
    background: linear-gradient(135deg, #292a3a, #536976) !important;
}

.Todo_Drawer .el-drawer__body {
    padding: 0;
}

#Collapse .el-collapse-item__arrow {
    display: none;
}

#Collapse .el-collapse-item__content {
    padding-bottom: 0;
}

.Collapse_Box .el-collapse-icon-position-right .el-collapse-item__header {
    padding-right: 0;
    padding-left: 8px;
}

/* 优化 Element Plus 组件样式 */
.el-button.is-circle {
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.el-button.is-circle:hover {
    transform: translateY(-2px) scale(1.05);
}

.el-button.is-circle:active {
    transform: translateY(0) scale(1);
}
</style>
