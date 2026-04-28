<template>
    <div class="Box">
        <div class="Card" style="width: 80%; max-width: 720px; display: flex; justify-content: center;">
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
.Box {
    padding-top: 32px;
    width: 100%;
    display: flex;
    justify-content: center;
}
.TodoListBox {
    padding: 16px;
    width: 85%;
}
.DrawerButton {
    padding: 16px;
    width: 100%;
    font-size: 15px;
    letter-spacing: 2px;
    transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.DrawerButton:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}
.SaveButton {
    margin-top: 14px;
}
.Collapse_Box {
    margin-top: 20px;
    border: 1px solid rgba(0, 0, 0, 0.12);
    border-radius: 8px;
    padding: 10px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    background-color: rgba(0, 0, 0, 0.04);
}
.Empty_Img {
    box-sizing: border-box;
    width: 100%;
    padding: 16px;
    opacity: 0.7;
}
.Text_ThroughLine {
    text-decoration: line-through;
    opacity: 0.5;
}
.Title_Text {
    flex: 1;
    width: 1px;
}
.Title_Icons {
    display: flex;
    align-items: center;
    gap: 4px;
}
.Heart_Icon {
    width: 15px;
}
.Collapse_Item_Line {
    border-radius: 6px;
    overflow: hidden;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.12);
}
.Collapse_Item_Line:not(:last-child) {
    margin-bottom: 10px;
}
.Collapse_Title {
    display: flex;
    align-items: center;
    justify-content: space-between;
    position: relative;
    transition: all 0.3s ease;
    overflow: hidden;
    color: #1a1a1a;
    text-indent: 0.5em;
    width: 100%;
}
.Collapse_Title > * {
    position: relative;
    z-index: 1;
}
.Collapse_Icon {
    margin-right: 14px;
    color: #444;
    transition: transform 0.3s ease;
}
.Collapse_Title.Is_Active .Collapse_Icon {
    transform: rotate(90deg);
}
.Collapse_Title::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    transition: left 0.3s ease;
    z-index: 0;
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
.Collapse_Container {
    padding: 14px 16px;
    border-radius: 0 0 6px 6px;
}
.Collapse_Container[tag-like="0"] {
    background: linear-gradient(220.55deg, #7CF7FF 0%, #4B73FF 100%);
}
.Collapse_Container[tag-like="1"] {
    background: linear-gradient(220.55deg, #FF3F3F 0%, #063CFF 100%);
}
.Content_Box {
    border-radius: 6px;
    background: linear-gradient(220.55deg, #FFF6EB 0%, #e8ddd5 100%);
    padding: 8px 12px;
    text-indent: 2em;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.12);
    word-wrap: break-word;
    line-height: 1.6;
    font-size: 14px;
    color: #333;
    min-height: 36px;
}
.Time_Box {
    margin-top: 12px;
    display: flex;
    gap: 16px;
    justify-content: center;
    flex-wrap: wrap;
}
.Time_Item {
    display: flex;
    align-items: center;
    gap: 5px;
    color: rgba(255, 255, 255, 0.92);
    font-size: 13px;
    background: rgba(0, 0, 0, 0.15);
    padding: 4px 10px;
    border-radius: 20px;
}
.Time_Item .el-icon {
    color: #F7BA2A;
    font-size: 13px;
}
.Time_Label {
    font-weight: 600;
    opacity: 0.85;
}
.Time_Value {
    font-weight: 400;
}
.Rate_Box {
    display: flex;
    align-items: center;
    justify-content: center;
    margin-top: 10px;
}
.Operation_Box {
    margin-top: 12px;
    display: flex;
    justify-content: flex-end;
    gap: 10px;
}
.Handler {
    cursor: grab;
    user-select: none;
    padding: 0 6px;
    color: #888;
    transition: color 0.2s;
}
.Handler:hover {
    color: #333;
}
.Handler:active {
    cursor: grabbing;
}
.Drawer_Header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    height: 40px;
}
.Drawer_Title {
    font-size: 20px;
    color: white;
    margin: 0;
    font-weight: 600;
    letter-spacing: 1px;
}
.Drawer_Close_Btn {
    font-size: 15px;
}
.Drawer_Body {
    display: flex;
    justify-content: center;
    height: 100%;
    overflow-y: hidden;
}
.Drawer_Form_Card {
    width: 60%;
    height: 100%;
    max-width: 600px;
    overflow-y: auto;
    padding: 20px;
}
.Date_Row {
    display: flex;
    align-items: center;
}
.Help_Icon {
    color: #409eff;
    margin-left: 8px;
    font-size: 18px;
    cursor: help;
}
.Drawer_Footer {
    flex: auto;
    margin-top: 12px;
}
.Footer_Btn {
    font-size: 15px;
    letter-spacing: 2px;
}
.el-form-item {
    margin-bottom: 22px;
}
</style>
<style>
.Todo_Drawer {
    background: linear-gradient(135deg, #292a3a, #536976) !important;
}
#Collapse .el-collapse-item__arrow {
    display: none;
}
#Collapse .el-collapse-item__content {
    padding-bottom: 0;
}
.Collapse_Box .el-collapse-icon-position-right .el-collapse-item__header {
    padding-right: 0;
}
</style>
