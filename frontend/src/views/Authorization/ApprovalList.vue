<template>
    <div>
        <ApprovalTableTemplate
            :table-data="tableData"
            :loading="isLoading"
            @detail-click="handleDetail"
            @func-click="handleFuncMapping"/>
        <ApprovalDialogTemplate
            ref="ApprovalDialogRef"
            :dialog-visible="dialogVisible"
            :dialog-title="dialogTitle"
            :application-info="applicationInfo"
            :form-display-fields="formDisplayFields"
            @dialog-close="handleDialogClose"/>
    </div>
</template>
<script setup>
import { onMounted, ref, reactive } from 'vue';
import ApprovalDialogTemplate from './Components/ApprovalDialogTemplate.vue';
import ApprovalTableTemplate from './Components/ApprovalTableTemplate.vue';

const tableData = ref([])
const isLoading = ref(true)
const dialogVisible = ref(false)
const dialogTitle = ref(undefined)
const applicationInfo = ref({})
const formDisplayFields = ref([])
const ApprovalDialogRef = ref(null)

onMounted(()=>{
    tableData.value=[
        {
            'aid':1,
            'type_id':1,
            'type':'pwd-reset',
            'apply_account':'admin_1'
        },
        {
            'aid':2,
            'type_id':1,
            'type':'pwd-reset',
            'apply_account':'admin_2'
        },
        {
            'aid':3,
            'type_id':2,
            'type':'worklist',
            'apply_account':null
        }
    ];
    /**
     * 表单行数据
     * 必填 key, type, label, option(type:select, checkbox, radio), isRule, defaultValue,
     * 可选 width, placeholder, disabled
     */
    formDisplayFields.value = reactive([
            {
                'key':'approval_title',
                'defaultValue':'',
                'type':'input',
                'label':'审批',
                'width':'70%',
                'placeholder':'待输入...',
                'disabled':undefined,
                'isRule':true
            },
            {
                'key':'reason',
                'defaultValue':'111',
                'type':'textarea',
                'label':'原因',
                'placeholder':'待输入...',
                'rows':3,
                'disabled':null,
                'isRule':true
            },
            {
                'key':'approval_result',
                'defaultValue':null,
                'type':'select',
                'label':'审批结果',
                'placeholder':'待选择...',
                'disabled':undefined,
                'isRule':true,
                'option':[
                    {
                        'label':'同意',
                        'value':1
                    },
                    {
                        'label':'拒绝',
                        'value':2
                    }
                ]
            },
            {
                'key':'radio_key',
                'defaultValue':2,
                'type':'radio',
                'label':'选择',
                'disabled':false,
                'option':[
                    {
                        'label':'选择1',
                        'value':1
                    },
                    {
                        'label':'选择2',
                        'value':2
                    }
                ]
            },
            {
                'key':'CC_email',
                'defaultValue':[],
                'type':'checkbox',
                'label':'抄送',
                'disabled':undefined,
                'isRule':true,
                'option':[
                    {
                        'label':'Test1@email.com',
                        'value':'Test1@email.com'
                    },
                    {
                        'label':'Test2@email.com',
                        'value':'Test2@email.com'
                    }
                ]
            },
            {
                'key':'importance_rate',
                'defaultValue':null,
                'type':'rate',
                'label':'重要度',
                'width':'80%',
                'disabled':undefined,
                'isRule':false,
                'showText':true,
                'texts':['E', 'D', 'C', 'B', 'A'],
                'colors':['#99A9BF', '#F7BA2A', '#FF9900']
            },
            {
                'key':'time',
                'defaultValue':null,
                'type':'time',
                'label':'时间',
                'placeholder':'待选择...',
                'disabled':undefined,
                'isRule':true,
                'format':'hh:mm:ss a'
            },
            {
                'key':'date',
                'defaultValue':null,
                'type':'date',
                'label':'时间',
                'placeholder':'待选择...',
                'disabled':undefined,
                'isRule':true,
                'format':'YYYY-MM-DD dd'
            },
    ])
    setTimeout(()=>{
        isLoading.value=false;
    },1000)
    
})

/*
-----以下均为按钮点击事件+父子组件事件交互-----
*/
const handleDetail=(rowData)=>{
    console.log(rowData);
    
}
const handleFuncMapping=(objData)=>{
    switch (objData.funcConfig.func){
        case 'handlePwdReset':
            handlePwdReset(objData);
            break;
        default:
            handleTestFunc(objData);
    }
}

const handleTestFunc=(objData)=>{
    DialogPropsTransmit(objData);
}
const handlePwdReset=(objData)=>{
    DialogPropsTransmit(objData);
}

const DialogPropsTransmit=(objData)=>{
    dialogVisible.value = true;
    dialogTitle.value = objData.funcConfig.name;
    applicationInfo.value = objData.rowData;
    ApprovalDialogRef.value.GetDisplayFields();
    
}

const handleDialogClose=()=>{
    dialogVisible.value = false;
}
/*
-----以上均为按钮点击事件+父子组件事件交互-----
*/
</script>
<style scoped>

</style>