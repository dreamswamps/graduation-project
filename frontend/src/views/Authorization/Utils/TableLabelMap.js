// 所有Map均需要传入，非该JS文件写死

/*
函数需要白名单数组
JS白名单
展示同时在白名单数组和JS白名单中的字段
*/
export const FieldsWitheList=(whiteList = [], formatMap)=>{    
    return whiteList.filter(key => key in formatMap);
}

/*
格式化label，formatMap作为字段中英文映射表，若未找到映射值，则展示key本身
*/
export const FormatLabel=(key, formatMap)=>{
    return formatMap[key] || undefined;
}