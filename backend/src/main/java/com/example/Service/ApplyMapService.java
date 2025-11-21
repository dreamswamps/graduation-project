package com.example.Service;

import com.example.Exception.CustomException;
import com.example.POJO.Application;
import com.example.Service.Strategy.ApplyStrategyMap;
import com.example.Service.Strategy.ApprovalStrategy;
import jakarta.annotation.Resource;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class ApplyMapService {

    /*
    该Service是用于搭配ApplyService的审批流程
    提供一个根据type指向不同业务层逻辑的中间层
    需要:类型type，审批状态approval
    传递Application类
     */
    @Resource
    private ApplyStrategyMap applyStrategyMap;
    @Resource
    private ApplicationContext applicationContext;

    /*
    审批入口方法
    需要传入完整的申请对象
     */
    public void dispatch(Application application) {
//        从.yml中获取到当前申请type对应的值
        String strategyClassName = applyStrategyMap.getMap().get(application.getType());
        if (strategyClassName == null) {
            throw new CustomException("511","未知的审批类型："+application.getType());
        }
//        由于Spring中Bean标签都以小写字母开头，因此对统一对首字母转小写
        String strategyBeanName = strategyClassName.substring(0,1).toLowerCase()+strategyClassName.substring(1);
        ApprovalStrategy strategy = applicationContext.getBean(strategyBeanName, ApprovalStrategy.class);

        if (application.getApproval() == 1){
            strategy.approve(application);
        }else {
            strategy.reject(application);
        }
    }



}
