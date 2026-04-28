<template>
    <div class="ApprovalTableTemplate">
        <el-divider content-position="left" class="custom-table-divider">
            {{ props.dividerText }}
        </el-divider>
        <el-radio-group v-model="radioValue" v-if="showHeadRadioBtn"
            @change="emit('radio-change', radioValue)"
            class="custom-radio-group">
            <el-radio v-for="item in props.headRadioFields"
                :value="item.value"
                :label="item.label">
            </el-radio>
        </el-radio-group>
        <el-table class="custom-table"
            :data="props.tableData" style="width: 100%" v-loading="props.loading">
          <el-table-column v-for="item in tableColumn"
            :prop="item.prop"
            :label="item.label"
            align="left"
            show-overflow-tooltip>
            <template #default="scope">
                {{ translationFieldList.includes(item.prop) 
                ? translationObject[scope.row[item.prop]] || scope.row[item.prop] || props.emptyText
                : scope.row[item.prop] || props.emptyText }}
            </template>
          </el-table-column>
          <el-table-column
            min-width="100"
            :label="props.btnRowText"
            align="center"
            class-name="custom-operation-column">
            <template #default="scope">
                <el-button
                    v-if="props.showDetailBtn"
                    @click="handleDetail(scope.row)"
                    class="custom-detail-btn">
                    {{ props.detailDefaultText }}
                </el-button>
                <el-button
                    v-if="props.showFuncBtn && ( !scope.row.approval || scope.row.approval == '0') "
                    :disabled="scope.row.disabled"
                    type="primary"
                    @click="handleFuncMapping(scope.row)"
                    class="custom-func-btn">
                    {{ props.funcRelationMap[scope.row.type]?.name || props.funcDefaultText }}
                </el-button>
            </template>
          </el-table-column>
        </el-table>
    </div>
</template>
<script setup>
import { computed, ref } from 'vue';
import { FieldsWitheList, FormatLabel } from '../Utils/TableLabelMap';
import { formatMap, typeFuncMap } from '../RelationMap';

/** 审批表格模板组件
 * 功能: 动态渲染审批表格，支持通过字段白名单展示，操作列按钮映射关联
 * 
 * 数据流转:
 * 1. 接收父组件传入的tableData作为展示数据
 * 2. 接收父组件传入的fieldsWhiteList作为白名单展示指定字段
 * 3. 关联TableLabelMap.js文件组合显示白名单字段，映射按钮关联方法名称func
 * 4. 渲染表格和操作列按钮
 * 5. 按钮点击事件向父组件传递包含关联方法名称func的数据结构
 */

/** 组件属性定义
 * @typedef {Object} props
 * @property {Object[]} tableData - 表格数据源
 * @property {boolean} [loading=fasle] - 加载状态
 * @property {string} [emptyText='暂无数据'] - 空数据提示文本
 * @property {boolean} [showDetailBtn=true] - 是否显示"详情"按钮
 * @property {boolean} [showFuncBtn=true] - 是否显示"处理"按钮
 * @property {string[]} [fieldsWhiteList=[]] - 白名单字段名
 * @property {Object} [translationObject={}] - 需要翻译字段的键值对
 * @property {Object[]} [translationFieldList=[]] - 需要翻译的字段名
 * @property {Boolean} [showHeadRadioBtn=false] - 顶部单选框
 * @property {Object[]} [headRadioFields=[]] - 顶部单选框字段
 */
 const props = defineProps({
    tableData: {
        type: Array,
        default: ()=>[]
    },
    loading: {
        type: Boolean,
        default: false
    },
    emptyText: {
        type: String,
        default: '暂无数据'
    },
    showDetailBtn: {
        type: Boolean,
        default: true
    },
    showFuncBtn: {
        type: Boolean,
        default: true 
    },
    fieldsWhiteList: {
        type: Array,
        default: []
    },
    translationObject: {
        type: Object,
        default: {}
    },
    translationFieldList: {
        type: Array,
        default: []
    },
    showHeadRadioBtn: {
        type: Boolean,
        default: false
    },
    headRadioFields: {
        type: Array,
        default: []
    },
    headRadioDefaultValue: {
        type: String,
        default: 0
    },
    dividerText: {
        type: String,
        default: '表格信息'
    },
    detailDefaultText: {
        type: String,
        default: '详情'
    },
    funcDefaultText: {
        type: String,
        default: '处理'
    },
    funcRelationMap: {
        type: Map,
        default: typeFuncMap
    },
    btnRowText: {
        type: String,
        default: "操作列"
    }
})

const radioValue = ref(props.headRadioDefaultValue);

const emit = defineEmits([
    'detail-click',
    'func-click',
    'radio-change'
])

/*
获取申请表格的第一行数据，获取其所有数据的key，并且遍历返回新的数组
其中FormatLabel方法为通过映射表返回对应key的中文，实现于TableLabelMap.js
*/
const tableColumn=computed(()=>{    
    if (props.tableData.length === 0) return [];
    const firstItem = props.tableData[0];
    // 返回白名单字段名数组
    const fieldsArray = FieldsWitheList(props.fieldsWhiteList, formatMap);
    return fieldsArray.filter(key => key in firstItem)
    .map(key => ({
        prop:key,
        label:FormatLabel(key, formatMap)
    }));
})

const handleDetail=(row)=>{
    emit('detail-click', row);
}
/*
统一入口，处理方法的映射关联
*/
const handleFuncMapping=(row)=>{    
    const funcMap = props.funcRelationMap[row.type] || {
        name: '处理',
        func: 'handleTestFunc'
    }
    emit('func-click',{
        rowData: row,
        funcConfig: funcMap
    })

}

</script>
<style scoped>
.ApprovalTableTemplate{
    padding: 10px;
}
/*
AI
*/
.ApprovalTableTemplate :deep(.custom-table-divider .el-divider__text) {
  font-size: 18px;
  font-weight: 600;
  color: #409eff;
}

.ApprovalTableTemplate :deep(.custom-table-divider) {
  margin: 8px 0 20px 0;
}

/* 美化顶部单选框组 */
.ApprovalTableTemplate :deep(.custom-radio-group) {
  margin-bottom: 8px;
  padding: 8px 16px;
  background-color: white;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

/* 美化表格 */
.ApprovalTableTemplate :deep(.custom-table) {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 2px 2px 6px rgba(0, 0, 0, 0.1);
}

.ApprovalTableTemplate :deep(.custom-table .el-table__header th) {
  background-color: #f0f7ff;
  color: #409eff;
  font-weight: 600;
  font-size: 14px;
  min-height: 48px;
}

.ApprovalTableTemplate :deep(.custom-table .el-table__row) {
  min-height: 48px;
  transition: all 0.3s ease;
}

.ApprovalTableTemplate :deep(.custom-table .el-table__row:hover) {
  background-color: #f5f9ff;
}

.ApprovalTableTemplate :deep(.custom-table .el-table__cell) {
  padding: 12px 0;
  font-size: 14px;
}

/* 美化操作列 */
.ApprovalTableTemplate :deep(.custom-operation-column .cell) {
  display: flex;
  justify-content: center;
  gap: 8px;
}

/* 美化详情按钮 */
.ApprovalTableTemplate :deep(.custom-detail-btn) {
  border-radius: 8px;
  padding: 8px 16px;
  border: 1px solid #409eff;
  color: #409eff;
}

/* 美化功能按钮 */
.ApprovalTableTemplate :deep(.custom-func-btn) {
  border-radius: 8px;
  padding: 8px 16px;
}
</style>