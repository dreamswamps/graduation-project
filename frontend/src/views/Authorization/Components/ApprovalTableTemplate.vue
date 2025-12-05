<template>
    <div>
        <el-table :data="props.tableData" style="width: 100%" v-loading="props.loading">
          <el-table-column v-for="item in tableColumn"
            :prop="item.prop"
            :label="item.label"
            align="left"
            empty="123"
            show-overflow-tooltip>
            <template #default="scope">
                {{ scope.row[item.prop] || props.emptyText }}
            </template>
          </el-table-column>
          <el-table-column
            label="操作列"
            align="center">
            <template #default="scope">
                <el-button
                    v-if="props.showDetailBtn"
                    @click="handleDetail(scope.row)">
                    详情
                </el-button>
                <el-button
                    v-if="props.showFuncBtn"
                    type="primary"
                    @click="handleFuncMapping(scope.row)"
                    >
                    {{ typeIdFuncMap[scope.row.type_id]?.name || '处理' }}
                </el-button>
            </template>
          </el-table-column>
        </el-table>
    </div>
</template>
<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { FieldsWitheList, FormatLabel, typeIdFuncMap } from '../Utils/TableLabelMap';

const props = defineProps({
    tableData:{
        type: Array,
        default: ()=>[]
    },
    loading:{
        type: Boolean,
        default: false
    },
    emptyText:{
        type: String,
        default: '暂无数据'
    },
    showDetailBtn:{
        type: Boolean,
        default: true
    },
    showFuncBtn:{
        type: Boolean,
        default: true 
    }
})

const emit = defineEmits([
    'detail-click',
    'func-click',
])

/*
获取申请列表的第一行数据，获取其所有数据的key，并且遍历返回新的数组
其中FormatLabel方法为通过映射表返回对应key的中文，实现于TableLabelMap.js
*/
const tableColumn=computed(()=>{
    console.log('tableColumn computed触发了');
    
    if (props.tableData.length === 0) return [];
    const firstItem = props.tableData[0];
    // 返回白名单字段名数组
    const fieldsArray = FieldsWitheList();
    return Object.keys(firstItem)
    .filter(key => fieldsArray.includes(key))
    .map(key => ({
        prop:key,
        label:FormatLabel(key)
    }));
})

const handleDetail=(row)=>{
    emit('detail-click', row);
}
/*
统一入口，处理方法的映射关联
*/
const handleFuncMapping=(row)=>{    
    const funcMap = typeIdFuncMap[row.type_id] || {
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

</style>