// 根据templateName模板名称执行模板所需要进行的页面交互判断
export const FieldShowInteractionByTemplateName=(templateName, key, value)=>{
    switch (templateName){
        case 'ResetApproval':
            return ResetApproval(key, value);
        default:
            return true;
    }
    return true;
}

// 根据type模板类型执行模板所需要进行的页面交互判断
export const FieldShowInteractionByType=(info, key, value)=>{
    
    // 申请状态
    if (info.tid) {
        switch (info.type){
            case 'pwd-reset':
                return PwdResetApply(key, value);
            case 'disable-template':
                return DisableTemplate(key, value);
            default:
                return true;
        }
    }

    // 审批状态
    switch (info.type){
        case 'pwd-reset':
            return ResetApproval(key, value);
        default:
            return true;
    }
}

/*
展示：“审批结果”
额外：“同意”-“原因”   “拒绝”-“拒绝原因”
*/
const ResetApproval=(key, value)=>{
    switch (key){
        case 'approval_result':
        case 'send_email':
            return true;
    }
    switch (value.approval_result){
        case undefined:
            return false;
        case '1':
            if (key === 'reason') return false;
            if (key === 'reset_method') return true;
            switch (value.reset_method){
                case '0':
                    break;
                case '1':
                    return key === 'sim_data';
                case '2':
                    break;
            }
        case '0':
            return key === 'reason';
        default:
            return false;
    }
    return false;
}

/*
申请密码重置
*/
const PwdResetApply=(key, value)=>{
    return true;
}

/*
停用模板申请
*/
const DisableTemplate=(key, value)=>{
    return true;
}