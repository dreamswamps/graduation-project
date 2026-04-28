<template>
    <div class="approvalSearchTemplate">
        <div>
            <el-divider content-position="left" class="custom-divider">
                {{ props.dividerText }}
            </el-divider>
            <el-form class="custom-search-form">
                <el-row :gutter="25">
                    <transition-group>
                        <template v-for="name in indexArray">
                            <el-col :span="24 / IntRange(props.itemPerRow)">
                                <el-form-item 
                                    :label="allSearchData[name]?.label">
                                    <el-input
                                        v-if="allSearchData[name]?.type === 'input'"
                                        v-model="inputValue[name]"
                                        :placeholder="allSearchData[name]?.placeholder"
                                        clearable>
                                    </el-input>
                                    <el-select
                                        v-if="allSearchData[name]?.type === 'select'"
                                        v-model="inputValue[name]" 
                                        :placeholder="allSearchData[name]?.placeholder"
                                        :multiple="props.multipleSelect"
                                        clearable>
                                        <el-option
                                            v-for="option in allSearchData[name]?.option"
                                            :value="option.value"
                                            :label="option.label">
                                        </el-option>
                                    </el-select>
                                </el-form-item>
                            </el-col>

                        </template>
                    </transition-group>
                </el-row>
            </el-form>
            <div class="SearchButton custom-search-buttons">
                <el-button v-if="props.showClearBtn" type="warning" @click="Clear()">
                    {{ props.resetBtnText }}
                </el-button>
                <el-tooltip :content=props.moreSearchTooltipText placement="top">
                    <el-button v-if="props.showMoreSearchBtn" type="success" @click="dialogVisable = true">
                        {{ props.moreSearchBtnText }}
                    </el-button>
                </el-tooltip>
                <el-button type="primary" @click="Search()">
                    {{ props.searchBtnText }}
                </el-button>
            </div>
        </div>
    </div>

    <el-dialog
        class="addMoreSearchDialog"
        v-model="dialogVisable"
        :title=props.moreSearchDialogTitle
        @closed="handleDialogClose">
        <el-select
            v-model="moreSearchName"
            multiple>
            <el-option
                v-for="(item,name) in moreSearchData"
                :label="formatMap[name]"
                :value="name"/>
        </el-select>
        <template #footer>
          <div class="more-search-footer">
            <el-button @click="handleClearMoreSearch" type="warning">
              {{ props.clearMoreSearchBtnText }}
            </el-button>
            <el-button @click="handleAddMoreSearch" type="success">
              {{ props.addMoreSearchBtnText }}
            </el-button>
          </div>
        </template>
    </el-dialog>
</template>
<script setup>
import { onMounted, ref, watch } from 'vue';
import { formatMap } from '../RelationMap';

const inputValue = ref([]);
const allSearchData = ref({});
const dialogVisable = ref(false);
const moreSearchName = ref([]);
let cacheName = []; 
const indexArray = ref([]);

const props = defineProps({
    searchData: {
        type: Object,
        required: true
    },
    showRadioBtn: {
        type: Boolean,
        default: true
    },
    multipleSelect: {
        type: Boolean,
        default: false
    },
    itemPerRow: {
        type: Number,
        default: 1,
    },
    showClearBtn: {
        type: Boolean,
        default: false
    },
    showMoreSearchBtn: {
        type: Boolean,
        default: false
    },
    moreSearchData: {
        type: Object,
        default: {}
    },
    defaultIndexArray: {
        type: Array,
        default: []
    },
    dividerText: {
        type: String,
        default: '查询条件'
    },
    resetBtnText: {
        type: String,
        default: '重置'
    },
    moreSearchTooltipText: {
        type: String,
        default: '点击添加更多查询条件'
    },
    moreSearchBtnText: {
        type: String,
        default: '添加查询条件'
    },
    searchBtnText: {
        type: String,
        default: '查询'
    },
    moreSearchDialogTitle: {
        type: String,
        default: '添加更多查询条件'
    },
    clearMoreSearchBtnText: {
        type: String,
        default: '清空'
    },
    addMoreSearchBtnText: {
        type: String,
        default: '添加'
    }
})

const emit = defineEmits([
    'search-click'
])

// 直接获取搜索框数据
defineExpose({
    getInputValue: () => inputValue.value
})

const Search=()=>{
    emit('search-click',inputValue)    
}

const Clear=()=>{
    inputValue.value = [];
    cacheName = [];
    handleClearMoreSearch();
    Search();
}

// 控制范围且只保留整数部分
const IntRange=(value)=>{
    value = parseInt(value);
    if (value <= 1) {
        return 1;
    }
    if (value >= 4) {
        return 4;
    }
    return value;
}

const handleClearMoreSearch=()=>{
    moreSearchName.value = [];
    handleAddMoreSearch();
}

// 处理 添加查询条件
const handleAddMoreSearch=()=>{
    allSearchData.value = { ...props.searchData };
    indexArray.value.length = 0;
    console.log(props.defaultIndexArray);
    
    if (props.defaultIndexArray?.length > 0) {        
        indexArray.value = props.defaultIndexArray;
    }
    else{        
        Object.keys(allSearchData.value).forEach(key => {
            indexArray.value.push(key);
        })
    }
    
    moreSearchName.value.forEach(name => {
        allSearchData.value[name] = {
            ...props.moreSearchData[name],
            name: name
        };
        indexArray.value.push(name)
    })
    cacheName = moreSearchName.value;
    Search();
    dialogVisable.value = false;
}

// 添加更多查询条件弹出框关闭，将moreSearchName数据回退至保存数据，以实现清除未保存数据的作用
const handleDialogClose=()=>{
    moreSearchName.value = cacheName;
}

// watch监听searchData的修改，至少保证第一次渲染能够正确获取
watch(() => props.searchData, (newValue, oldValue) => {
    allSearchData.value = {...newValue};
    indexArray.value.length = 0;

    if (props.defaultIndexArray?.length > 0) {
        indexArray.value = props.defaultIndexArray;
    }else{
        Object.keys(allSearchData.value).forEach(key => {
            indexArray.value.push(key);
        })
    }
},{ immediate: true })

</script>
<style scoped>
.approvalSearchTemplate{
    padding: 10px;
    margin-right: 11px;
}
.SearchButton{
    text-align: right;
    margin-top: 20px;
}

/*
以下为AI生成
 */
/* 美化查询组件样式 */
.approvalSearchTemplate :deep(.custom-divider .el-divider__text) {
  font-size: 18px;
  font-weight: 600;
  color: #409eff;
}

.approvalSearchTemplate :deep(.custom-divider) {
  margin-top: 4px;
  margin-bottom: 12px;
}

/* 美化搜索表单样式 */
.approvalSearchTemplate :deep(.custom-search-form) {
  background: linear-gradient(135deg, #eeeeee 0%, #fff9f9 100%);
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  margin-top: 16px;
}

.approvalSearchTemplate :deep(.custom-search-form .el-form-item) {
  margin-bottom: 16px;
}

.approvalSearchTemplate :deep(.custom-search-form .el-form-item__label) {
  font-weight: 600;
  font-size: 15px;
}

.approvalSearchTemplate :deep(.custom-search-form .el-input__wrapper),
.approvalSearchTemplate :deep(.custom-search-form .el-select__wrapper) {
  border-radius: 8px;
  border: rgba(0, 0, 0, 0.1) 1px solid;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.approvalSearchTemplate :deep(.custom-search-form .el-input__wrapper.is-focus),
.approvalSearchTemplate :deep(.custom-search-form .el-select__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
  border-color: #409eff;
}

/* 美化按钮区域 */
.approvalSearchTemplate :deep(.custom-search-buttons) {
  text-align: right;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #e4e7ed;
}

.approvalSearchTemplate :deep(.custom-search-buttons .el-button) {
  border-radius: 8px;
  padding: 12px 24px;
  font-weight: 500;
  letter-spacing: 2px;
  margin-left: 10px;
}

/* 美化按钮颜色 */
.approvalSearchTemplate :deep(.custom-search-buttons .el-button--warning) {
  background: linear-gradient(135deg, #e79926 0%, #ebb563 100%);
  border: none;
  color: white;
}

.approvalSearchTemplate :deep(.custom-search-buttons .el-button--warning:hover) {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(230, 162, 60, 0.3);
}

.approvalSearchTemplate :deep(.custom-search-buttons .el-button--success) {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  border: none;
  color: white;
}

.approvalSearchTemplate :deep(.custom-search-buttons .el-button--success:hover) {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.3);
}

.approvalSearchTemplate :deep(.custom-search-buttons .el-button--primary) {
  background: linear-gradient(135deg, #208fff 0%, #7bbbff 100%);
  border: none;
  color: white;
}

.approvalSearchTemplate :deep(.custom-search-buttons .el-button--primary:hover) {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}
</style>
<style>
/*
更多搜索弹出框  - 必须不在scope中，成因未知
可能是子组件生成的对话框并不作用于子组件因此子组件的scope无法关联
*/
.addMoreSearchDialog.el-dialog {
  border-radius: 8px;
  overflow: hidden;
}

.addMoreSearchDialog.el-dialog .el-dialog__header {
  background: linear-gradient(135deg, #208fff 0%, #7bbbff 100%);
  padding: 20px;
}

.addMoreSearchDialog.el-dialog .el-dialog__title {
  color: white;
  font-size: 20px;
  font-weight: 500;
}

.addMoreSearchDialog.el-dialog .el-dialog__headerbtn {
  top: 25px;
  right: 20px;
}

.addMoreSearchDialog.el-dialog .el-dialog__headerbtn .el-dialog__close {
  color: white;
  font-size: 20px;
  transition: all 0.3s ease;
}

.addMoreSearchDialog.el-dialog .el-dialog__headerbtn:hover .el-dialog__close {
  color: #e6f7ff;
  transform: scale(1.2);
}

.addMoreSearchDialog.el-dialog .el-dialog__body {
  padding: 30px;
}

.addMoreSearchDialog.el-dialog .el-dialog__footer {
  padding: 20px 30px;
  background-color: #f8f9fa;
  border-top: 1px solid #e4e7ed;
}

/* 替换内联 margin 的样式（用于 footer） */
.addMoreSearchDialog.el-dialog .more-search-footer {
  margin-top: 150px;
}

/* 对话框内选择框样式 */
.addMoreSearchDialog.el-dialog .el-select {
  width: 100%;
}

.addMoreSearchDialog.el-dialog .el-select .el-select__wrapper {
  border-radius: 8px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.addMoreSearchDialog.el-dialog .el-select .el-select__wrapper.is-focus {
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
  border-color: #409eff;
}

/* 对话框内按钮样式 */
.addMoreSearchDialog.el-dialog .el-button {
  border-radius: 8px;
  padding: 12px 24px;
  font-weight: 500;
  letter-spacing: 2px;
  margin-left: 10px;
  border: none;
  transition: all 0.3s ease;
}

.addMoreSearchDialog.el-dialog .el-button--warning {
  background: linear-gradient(135deg, #e79926 0%, #ebb563 100%);
  color: white;
}

.addMoreSearchDialog.el-dialog .el-button--warning:hover {
  transform: scale(1.2);
  box-shadow: 0 4px 12px rgba(230, 162, 60, 0.3);
}

.addMoreSearchDialog.el-dialog .el-button--success {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  color: white;
}

.addMoreSearchDialog.el-dialog .el-button--success:hover {
  transform: scale(1.2);
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.3);
}
</style>