/*
名称关联映射
*/
export const formatMap = {
    'aid': '申请编号',
    'type': '申请类型',
    'apply_account': '申请账号',
    'apply_time': '申请时间',
    'approved_time': '审批时间'
}

/*
业务字段type_id与组件函数方法关联映射
*/
export const typeIdFuncMap = {
    1: {
        name:  '审批',
        func: 'handlePwdReset'
    },
    2: {
        name: '处理',
        func: 'handleTestFunc'
    }
}

/*
使用白名单，列表只显示在白名单中的字段
用于隐藏部分需要传递但又不希望展示的数据
遍历返回包含所有字段值的数组
*/
export const FieldsWitheList=()=>{
    let fields = [];
    Object.keys(formatMap).forEach(key => {
        fields.push(key);
    })
    return fields;
}

/*
格式化label，formatMap作为字段中英文映射表，若未找到映射值，则展示key本身
*/
export const FormatLabel=(key)=>{
    return formatMap[key] || undefined;
}