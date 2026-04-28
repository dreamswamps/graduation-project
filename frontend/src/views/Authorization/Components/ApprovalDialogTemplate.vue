<template>
    <div class="approvalDialogTemplate">
        <el-dialog
            v-model="props.dialogVisible"
            :close-on-click-modal="false"
            width="70%"
            :title="props.dialogTitle"
            :before-close="handleBeforeClose"
            class="custom-dialog">
            <!-- 该部分较为特殊，为前端拼接数据 -->
            <el-descriptions
                v-if="props.showDescriptions && props.descriptions"
                :title="props.descriptions.title"
                :column="props.descriptions.column"
                border
                class="custom-descriptions">
                <el-descriptions-item v-for="item in props.descriptions.items">
                    <template #label>
                        {{ item.label }}
                    </template>
                    {{ item.value }}
                </el-descriptions-item>
            </el-descriptions>
            <el-form
                :model="formData"
                ref="formRef"
                :label-width="props.formLabelWidth+'px'"
                v-loading="formLoading"
                class="custom-form">
                <transition-group>
                    <template v-for="field in props.formDisplayFields">
                        <el-form-item
                            v-if="field && GetFieldVisibility(field.key) && (field.type != 'slot' || props.showSlot)"
                            :label="field.label"
                            :prop="field.key"
                            :rules="field.isRule ? GetValidationRules(field.type) : {required: false}"
                            class="custom-form-item">
                            <!-- 需要插槽支持 -->
                            <slot
                                v-if="field.type === 'slot' && props.showSlot"
                                :name="field.key"
                                :field="field"
                                :formData="formData"
                                :rowData="props.applicationInfo?.rowData"
                            ></slot>
                            <el-input
                                v-if="field.type === 'input'"
                                v-model="formData[field.key]"
                                :style="{width:(field.width || '80%')}"
                                :placeholder="field.placeholder"
                                :disabled="field.disabled"
                                class="custom-input">
                            </el-input>
                            <el-input
                                v-if="field.type === 'textarea'"
                                v-model="formData[field.key]"
                                :style="{width:(field.width || '80%')}"
                                type="textarea"
                                :placeholder="field.placeholder"
                                :rows="(field.rows || 2)"
                                :disabled="field.disabled"
                                class="custom-textarea">
                            </el-input>
                            <el-select
                                v-if="field.type === 'select'"
                                v-model="formData[field.key]"
                                :style="{width:(field.width || '80%')}"
                                :placeholder="field.placeholder"
                                :disabled="field.disabled"
                                class="custom-select">
                                <el-option
                                    v-for="option in field.option"
                                    :value="option.value"
                                    :label="option.label">
                                </el-option>
                            </el-select>
                            <el-radio-group
                                v-if="field.type === 'radio'"
                                v-model="formData[field.key]"
                                :disabled="field.disabled"
                                class="custom-radio-group">
                                <el-radio 
                                    v-for="option in field.option"
                                    :value="option.value"
                                    :label="option.label">
                                </el-radio>
                            </el-radio-group>
                            <el-checkbox-group
                                v-if="field.type === 'checkbox'"
                                v-model="formData[field.key]"
                                :style="{width:(field.width || '80%')}"
                                :disabled="field.disabled"
                                class="custom-checkbox-group">
                                <el-checkbox
                                    v-for="option in field.option"
                                    :value="option.value"
                                    :label="option.label">
                                </el-checkbox>
                            </el-checkbox-group>
                            <el-rate
                                v-if="field.type === 'rate'"
                                v-model="formData[field.key]"
                                :style="{width:(field.width || '80%')}"
                                :disabled="field.disabled"
                                clearable
                                :show-text="(field.showText || false)"
                                :texts="(field.texts || undefined)"
                                :colors="(field.colors || undefined)"
                                class="custom-rate">
                            </el-rate>
                            <el-time-picker
                                v-if="field.type === 'time'"
                                v-model="formData[field.key]"
                                :style="{width:(field.width || '80%')}"
                                :placeholder="field.placeholder"
                                :disabled="field.disabled"
                                clearable
                                :format="(field.format || 'HH:mm:ss')"
                                :value-format="(field.format || 'HH:mm:ss')"
                                class="custom-time-picker">
                            </el-time-picker>
                            <el-date-picker
                                v-if="field.type === 'date'"
                                v-model="formData[field.key]"
                                :style="{width:(field.width || '80%')}"
                                :placeholder="field.placeholder"
                                :disabled="field.disabled"
                                clearable
                                :format="(field.format || YYYY-MM-DD)"
                                :value-format="(field.format || YYYY-MM-DD)"
                                class="custom-date-picker">
                            </el-date-picker>
                            <el-upload
                                v-if="field.type === 'upload'"
                                v-model="formData[field.key]"
                                :style="{width:(field.width || '80%')}"
                                :disabled="field.disabled"
                                :action="field.actionURL || ''"
                                :multiple="field.multiple"
                                :limit="field.limit || 3"
                                :on-success="handleUploadFileSuccess"
                                :on-error="handleUploadFileError"
                                :before-upload="handleBeforeUpload"
                                :before-remove="handleBeforeRemove"
                                show-file-list
                                class="custom-upload">
                                <el-button type="primary">
                                    {{ field.texts }}
                                </el-button>
                            </el-upload>
                        </el-form-item>
                    </template>
                </transition-group>
            </el-form>
            <div slot="footer" class="dialog_footer">
                <el-button type="warning" @click="emit('reset-click')">{{ props.resetBtnText }}</el-button>
                <el-button type="primary" @click="handleSubmit">{{ props.submitBtnText }}</el-button>
            </div>
        </el-dialog>
    </div>
</template>
<script setup>
import { ElMessageBox } from 'element-plus';
import { reactive, ref, watch } from 'vue';
import { FieldShowInteractionByType } from '../Utils/FieldShowInteraction';
/** 审批处理弹出框表单模板组件
 * 功能: 动态渲染弹出框模板，支持通过传入数据结构动态展示表单信息
 * 
 * 数据流转:
 * 1. 接收父组件传入的formDisplayFields作为表单信息
 * 2. 接收父组件传入的applicationInfo保存审批的申请信息
 * 3. 渲染表单
 */
const formData = reactive({});
const formLoading = ref(true);

/** 组件属性定义
 * @typedef {Object} props
 * @property {boolean} [dialogVisible=false] - 弹出框显示
 * @property {string} [dialogTitle='申请'] - 弹出框标题
 * @property {number} [formLabelWidth=100] - 表单标签宽度
 * @property {Object} applicationInfo - 操作行数据信息
 * @property {Object[]} formDisplayFields - 表单数据源
 */
const props = defineProps({
    dialogVisible: {
        type: Boolean,
        default: false
    },
    dialogTitle: {
        type: String,
        default: '申请'
    },
    formLabelWidth: {
        type: Number,
        default: 100
    },
    applicationInfo: {
        type: Object,
        required: true
    },
    formDisplayFields: {
        type: Array,
        required: true
    },
    resetBtnText: {
        type: String,
        default: '重置'
    },
    submitBtnText: {
        type: String,
        default: '提交'
    },
    formRules: {
        type: Object,
        required: true
    },
    showDescriptions: {
        type: Boolean,
        default: false
    },
    descriptions: {
        type: Object,
        default: {}
    },
    showSlot: {
        type: Boolean,
        default: true,
    }
})

// 确保值可以正确获取，避免第一次空值的污染
watch(() => props.formDisplayFields, (newFields) => {
  if (newFields && newFields.length > 0) {
    GetDisplayFields();
  }
}, { immediate: true });

// 处理动态渲染数据
const GetDisplayFields=()=>{

    if (!props.formDisplayFields || props.formDisplayFields.length === 0) {
        return;
    }
    props.formDisplayFields.forEach(field => {
        if (field && field.key) {
            formData[field.key] = field.defaultValue;        
        }     
    })
    
    setTimeout(()=>{
        formLoading.value = false;
    },1000)
}

// 弹出框关闭数据清理
const handleClose=()=>{
    Object.keys(formData).forEach(key => {
        delete formData[key];
    });
    formLoading.value = true;
    if (formRef.value) {
        formRef.value.clearValidate(); // 先清除所有验证
        formRef.value.resetFields();   // 再重置字段
    }
    emit('dialog-close');
}

/**
 * 正则验证
 * 数据结构中isRule表示是否启用正则验证
 * switch匹配对应正则规则
 */
const formRef = ref();
const GetValidationRules=(type)=>{
    switch (type){
        case 'input':
        case 'textarea':
            return props.formRules.input;
        case 'select':
        case 'radio':
            return props.formRules.select;
        case 'checkbox':
            return props.formRules.checkbox;
        default:
            return {required: false};
    }
}

/**
 * 父子组件事件交互
 * 其中最后五个为upload组件需要的方法，若不使用该组件，可不使用对应方法
 */
const emit = defineEmits([
    'dialog-close',
    'reset-click',
    'submit-click',
    'delete-file',
    'upload-success',
    'upload-error',
    'before-upload',
    'before-remove'
])

defineExpose({
    GetDisplayFields,
    handleClose,
    formRef,
    formData,
    formLoading
})

const handleSubmit=()=>{
    emit('submit-click');
}

const handleBeforeClose=()=>{
    ElMessageBox.confirm("确定取消编辑？页面不会保存未完成的数据！").then(()=>{
        handleClose();
    }).catch(()=>{
    })
}

// 展示时判断是否有额外展示条件
// ⚠目前存在的问题，进入弹出框会计算一次，加载结束后会再计算一次，并且每次发生逻辑渲染改变时也会计算一次
// 每次计算方法调用次数 = 渲染数据的长度
const GetFieldVisibility=(key)=>{
    if (!props.applicationInfo.type) return false;
    
    // 新增：监听表单数据变化，当字段隐藏时清除其验证
    if (formRef.value) {
        // 使用 nextTick 确保 DOM 更新后清除验证
        setTimeout(() => {
            if (formRef.value) {
                formRef.value.clearValidate([key]);
            }
        }, 0);
    }
    return FieldShowInteractionByType(props.applicationInfo, key, formData);
}

// Upload组件对应的四个事件
const handleUploadFileSuccess=(res)=>{
    emit('upload-success', res)
}

const handleUploadFileError=()=>{
    emit('upload-error')
}

const handleBeforeUpload=()=>{
    return emit('before-upload');
}

const handleBeforeRemove=async (file, fileList)=>{
    return new Promise((resolve) => {
        emit('before-remove', file, fileList, resolve);
    });
}

</script>
<style scoped>
/* 美化弹出框样式 */
.approvalDialogTemplate :deep(.custom-dialog .el-dialog__header) {
  background: linear-gradient(135deg, #208fff 0%, #7bbbff 100%);
  padding: 20px;
}

.approvalDialogTemplate :deep(.custom-dialog .el-dialog__title) {
  color: white;
  font-size: 20px;
  font-weight: 500;
  cursor: default;
}

.approvalDialogTemplate :deep(.custom-dialog .el-dialog__headerbtn) {
  top: 25px;
  right: 20px;
}

.approvalDialogTemplate :deep(.custom-dialog .el-dialog__headerbtn .el-dialog__close) {
  color: white;
  font-size: 20px;
  transition: all 0.3s ease;
}

.approvalDialogTemplate :deep(.custom-dialog .el-dialog__headerbtn:hover .el-dialog__close) {
  transform: scale(1.3);
  color: #e6f7ff;
}

.approvalDialogTemplate :deep(.custom-dialog .el-dialog__body) {
  padding: 30px 30px 10px 30px;
  max-height: 70vh;
  overflow-y: auto;
}

/* 美化对话框内的按钮 */
.approvalDialogTemplate :deep(.custom-dialog .el-button) {
  border-radius: 8px;
  padding: 12px 24px;
  letter-spacing: 2px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.approvalDialogTemplate :deep(.custom-dialog .el-button--primary) {
  background: linear-gradient(135deg, #208fff 0%, #7bbbff 100%);
  border: none;
}

.approvalDialogTemplate :deep(.custom-dialog .el-button--primary:hover) {
  transform: scale(1.2);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.approvalDialogTemplate :deep(.custom-dialog .el-button--warning) {
  background: linear-gradient(135deg, #e79926 0%, #ebb563 100%);
  border: none;
  color: white;
}

.approvalDialogTemplate :deep(.custom-dialog .el-button--warning:hover) {
  transform: scale(1.2);
  box-shadow: 0 4px 12px rgba(230, 162, 60, 0.3);
}

/* 美化 descriptions 样式   - AI生成
使用:deep指定该组件生效样式  不使用global这会导致同个父组件下其他子组件也生效该style */
.approvalDialogTemplate :deep(.custom-descriptions .el-descriptions__title) {
  font-size: 18px;
  font-weight: 600;
  padding-left: 8px;
  border-left: 4px solid #409eff;
}

.approvalDialogTemplate :deep(.custom-descriptions .el-descriptions__body) {
  background-color: #f8f9fa;
  border-radius: 12px;
  padding: 16px;
}

.approvalDialogTemplate :deep(.custom-descriptions .el-descriptions__label) {
  min-width: 100px;
  padding: 12px 16px !important;
}

.approvalDialogTemplate :deep(.custom-descriptions .el-descriptions__content) {
  padding: 12px 16px !important;
  color: #606266;
  background-color: white;
  font-weight: 500;
}

/* 美化 form 表单样式 */
.approvalDialogTemplate :deep(.custom-form) {
  padding: 16px 0;
}

.approvalDialogTemplate :deep(.custom-form-item) {
  transition: all 0.3s ease;
}

.approvalDialogTemplate :deep(.custom-form-item:hover) {
  transform: translateX(5px);
}

.approvalDialogTemplate :deep(.custom-form-item .el-form-item__label) {
  font-weight: 600;
  color: #333;
  font-size: 15px;
}

.approvalDialogTemplate :deep(.custom-form-item .el-form-item__content) {
  margin-left: 16px !important;
}

/* 输入框美化 */
.approvalDialogTemplate :deep(.custom-input .el-input__wrapper) {
  border-radius: 8px;
  border: rgba(0, 0, 0, 0.1) 1px solid;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.approvalDialogTemplate :deep(.custom-input .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
  border-color: #409eff;
}

/* 文本域美化 */
.approvalDialogTemplate :deep(.custom-textarea .el-textarea__inner) {
  border-radius: 8px;
  border: rgba(0, 0, 0, 0.1) 1px solid;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  font-family: inherit;
  line-height: 1.5;
}

.approvalDialogTemplate :deep(.custom-textarea .el-textarea__inner:focus) {
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
  border-color: #409eff;
}

/* 下拉选择框美化 */
.approvalDialogTemplate :deep(.custom-select .el-select__wrapper) {
  border-radius: 8px;
  transition: all 0.3s ease;
  border: rgba(0, 0, 0, 0.1) 1px solid;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.approvalDialogTemplate :deep(.custom-select .el-select__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

/* 单选框美化 */
.approvalDialogTemplate :deep(.custom-radio-group .el-radio) {
  margin-right: 16px;
}

.approvalDialogTemplate :deep(.custom-radio-group .el-radio__label) {
  color: #555;
  font-weight: 500;
}

/* 复选框美化 */
.approvalDialogTemplate :deep(.custom-checkbox-group) {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.approvalDialogTemplate :deep(.custom-checkbox .el-checkbox__label) {
  color: #555;
  font-weight: 500;
}

/* 评分组件美化 */
.approvalDialogTemplate :deep(.custom-rate .el-rate__icon) {
  font-size: 22px;
  margin-right: 6px;
}

/* 时间选择器美化 */
.approvalDialogTemplate :deep(.custom-time-picker .el-input__wrapper),
.approvalDialogTemplate :deep(.custom-date-picker .el-input__wrapper) {
  border-radius: 8px;
  transition: all 0.3s ease;
  border: rgba(0, 0, 0, 0.1) 1px solid;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.approvalDialogTemplate :deep(.custom-time-picker .el-input__wrapper.is-focus),
.approvalDialogTemplate :deep(.custom-date-picker .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

/* 上传组件美化 */
.approvalDialogTemplate :deep(.custom-upload .el-upload-list__item) {
  transition: all 0.3s ease;
  border-radius: 8px;
}

.approvalDialogTemplate :deep(.custom-upload .el-upload-list__item:hover) {
  background-color: #f5f7fa;
  transform: translateX(3px);
}

/* 禁用状态样式 */
.approvalDialogTemplate :deep(.custom-input .el-input.is-disabled .el-input__wrapper),
.approvalDialogTemplate :deep(.custom-textarea .el-textarea.is-disabled .el-textarea__inner),
.approvalDialogTemplate :deep(.custom-select .el-select.is-disabled .el-select__wrapper) {
  background-color: #f5f7fa;
  cursor: not-allowed;
}

/* 原有的 dialog_footer 样式 */
.approvalDialogTemplate :deep(.dialog_footer) {
    text-align: right;
    margin: 6px;
}
</style>