/*
名称关联映射
用于表单
*/
export const formatMap = {
    'aid': '申请编号',
    'type': '申请类型',
    'apply_account': '申请账号',
    'apply_time': '申请时间',
    'approved_time': '审批时间',
    'user_name': '用户名称',
    'user_email': '用户邮箱',
    'templateName': '模板简称',
    'templateDesc': '模板信息',
    'tid': '模板编号',
    'approval': '审批状态',
}

/*
label-value关联映射
用于下拉框option
*/
export const optionTypeMap = {
    'pwd-reset': '重置密码',
    'worklist': '工作项目',
    'creat-new-template': '新增申请模板',
    'disable-template': '停用申请模板',
    'resume-template': '恢复申请模板',
    'custom-template': '自定义模板'
}

/*
业务字段type_id与组件函数方法关联映射
*/
export const typeFuncMap = {
    'pwd-reset': {
        name:  '审批',
        func: 'handlePwdReset'
    },
    'worklist': {
        name: '处理',
        func: 'handleTestFunc'
    },
    'creat-new-template': {
        name:  '审批',
        func: 'handleCreateTemplate'
    },
    'disable-template': {
        name:  '审批',
        func: 'handleDisableTemplate'
    },
    'resume-template': {
        name:  '审批',
        func: 'handleResumeTemplate'
    },
    'custom-template': {
        name: '审批',
        func: 'handleCustomTemplate'
    }
}

/*
申请统一页面需要的关联映射
*/
export const unifiedApplyTypeFuncMap = {
    'pwd-reset': {
        name:  '申请',
        func: 'handlePwdReset'
    },
    'worklist': {
        name: '操作',
        func: 'handleTestFunc'
    },
    'creat-new-template': {
        name:  '申请',
        func: 'handleCreateTemplate'
    },
    'disable-template': {
        name:  '申请',
        func: 'handleDisableTemplate'
    },
    'resume-template': {
        name:  '申请',
        func: 'handleResumeTemplate'
    },
    'custom-template': {
        name: '申请',
        func: 'handleCustomTemplate'
    }
}

// 审批状态映射
export const approvalStatusMap = {
    0: { text: '待审批', type: 'warning' },
    1: { text: '已通过', type: 'success' },
    2: { text: '未通过', type: 'danger' },
}

// 完成状态映射
export const completedStatusMap = {
    1: { text: '已完成', type: 'success' },
    0: { text: '未完成', type: 'info' }
}

// 处理状态映射，其中100只发生于手动设置
export const workingStatusMap = {
    100: { text: '已完成', type: 'success' },
    0: { text: '待处理', type: 'info' },
    1: { text: '处理中', type: 'warning' }
}

// 我的申请状态映射
export const approvalStatusTextMap = {
    0: '待审批',
    1: '已通过',
    2: '未通过'
}

// 审批类型标签样式映射
export const typeTagMap = {
    'pwd-reset': 'warning',
    'worklist': 'primary'
}

//  组件类型英文type与中文映射对象
export const fieldTypeMap = {
    select: '下拉选择框',
    rate: '评分',
    upload: '文件上传',
    textarea: '多行文本',
    checkbox: '复选框',
    radio: '单选框',
    time: '时间选择',
    date: '日期选择',
    input: '单行文本',
    slot: '自定义插槽'
};

/*
代替后端绘制弹出框中的descriptions部分数据结构
拼接当前用户信息
*/
export const BuildFormDescriptions=(keysToKeep, rowData, title='用户信息', column=2)=>{
    let items = [];
    keysToKeep.forEach(key => {
        const item = {
            'label': formatMap[key],
            'icon': null,
            'value': rowData[key] || '未知'
        }
        items.push(item);
    })
    return {
        'title': title,
        'column': column,
        'items': items
    }
}

/*
搭配BuildFormDescriptions还需要从apply_data中根据类型不同插入不同的数据组合
*/
export const AddApplyDataForDescriptions=(type, descriptionsTem, apply_data)=>{
    switch(type){
        case 'pwd-reset':
        case 'disable-template':
            // push返回是长度，因此不能直接return
            descriptionsTem.items.push({
                'label': '申请原因',
                'icon': null,
                'value': apply_data.reason})
            return descriptionsTem;
        default:
            return descriptionsTem;
    }
}
