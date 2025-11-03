package com.example.Service.Strategy;

import com.example.POJO.Application;

public interface ApprovalStrategy {
    /*
    审批流程策略模式统一约束接口
     */
    void approve(Application application);
    void reject(Application application);
}
