<template>
    <div>
        <el-dialog
            v-model="props.dialogVisible"
            :close-on-click-modal="false"
            width="70%"
            :title="props.dialogTitle"
            @close="handleClose">
            <el-form
                :model="formData"
                ref="formRef"
                :label-width="props.formLabelWidth+'px'"
                v-loading="formLoading">
                <transition-group>
                    <template v-for="field in props.formDisplayFields">
                        <el-form-item
                            v-if="field"
                            :label="field.label"
                            :prop="field.key"
                            :rules="field.isRule ? GetValidationRules(field.type) : {required: false}">
                            <el-input
                                v-if="field.type === 'input'"
                                v-model="formData[field.key]"
                                :style="{width:(field.width || '80%')}"
                                :placeholder="field.placeholder"
                                :disabled="field.disabled">
                            </el-input>
                            <el-input
                                v-if="field.type === 'textarea'"
                                v-model="formData[field.key]"
                                :style="{width:(field.width || '80%')}"
                                type="textarea"
                                :placeholder="field.placeholder"
                                :rows="(field.rows || 2)"
                                :disabled="field.disabled">
                            </el-input>
                            <el-select
                                v-if="field.type === 'select'"
                                v-model="formData[field.key]"
                                :style="{width:(field.width || '80%')}"
                                :placeholder="field.placeholder"
                                :disabled="field.disabled">
                                <el-option
                                    v-for="option in field.option"
                                    :value="option.value"
                                    :label="option.label">
                                </el-option>
                            </el-select>
                            <el-radio-group
                                v-if="field.type === 'radio'"
                                v-model="formData[field.key]"
                                :disabled="field.disabled">
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
                                :disabled="field.disabled">
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
                                :colors="(field.colors || undefined)">
                            </el-rate>
                            <el-time-picker
                                v-if="field.type === 'time'"
                                v-model="formData[field.key]"
                                :style="{width:(field.width || '80%')}"
                                :placeholder="field.placeholder"
                                :disabled="field.disabled"
                                clearable
                                :format="(field.format || 'HH:mm:ss')"
                                :value-format="(field.format || 'HH:mm:ss')">
                            </el-time-picker>
                            <el-date-picker
                                v-if="field.type === 'date'"
                                v-model="formData[field.key]"
                                :style="{width:(field.width || '80%')}"
                                :placeholder="field.placeholder"
                                :disabled="field.disabled"
                                clearable
                                :format="(field.format || YYYY-MM-DD)"
                                :value-format="(field.format || YYYY-MM-DD)">
                            </el-date-picker>
                            <!-- Upload上传 未完成 -->
                        </el-form-item>
                    </template>
                </transition-group>
            </el-form>
            <div slot="footer" class="dialog_footer">
                <el-button type="warning" @click="Reset()">重置</el-button>
                <el-button type="primary" @click="Submit()">提交</el-button>
            </div>
        </el-dialog>
    </div>
</template>
<script setup>
import { List } from 'echarts';
import { ElMessage, ElMessageBox } from 'element-plus';
import { onMounted, reactive, ref } from 'vue';

const formData = reactive({});
const formLoading = ref(true);

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
        type: List,
        required: true
    },
    formDisplayFields: {
        type: Array,
        required: true
    }
})

const GetDisplayFields=()=>{
    props.formDisplayFields.forEach(feild => {
        formData[feild.key] = feild.defaultValue;        
    })
    
    setTimeout(()=>{
        formLoading.value = false;
    },1000)
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
            return formRules.input;
        case 'select':
        case 'radio':
            return formRules.select;
        case 'checkbox':
            return formRules.checkbox;
        default:
            return {required: false};
    }
}
const formRules = {
    input:[
        { required: true, message: '请输入!', trigger: 'blur' }
    ],
    select:[
        { required: true, message: '请选择!', trigger: 'change' }
    ],
    checkbox:[
        { type: 'array', required: true, message: '请选择!', trigger: 'change' }
    ]
}

/**
 * 弹出框按钮方法
 */
const Reset=()=>{
    ElMessageBox.confirm('确定重置表单？','确定').then(()=>{
        ElMessage.success('重置成功');
        formRef.value.resetFields();
    })
}
const Submit=()=>{
    formRef.value.validate((valid)=>{
        if (valid) {
            ElMessage.success('提交成功');
            handleClose();
        }
    })   
}

/**
 * 父子组件事件交互
 */
const emit = defineEmits([
    'dialog-close',
])
defineExpose({
    GetDisplayFields
})
const handleClose=()=>{
    emit('dialog-close');
    formLoading.value = true;
}

</script>
<style scoped>
.dialog_footer{
    text-align: right;
    margin: 6px;
}
</style>
