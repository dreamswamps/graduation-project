package com.example.Service.Strategy;

import com.example.Exception.CustomException;
import com.example.Mapper.AdminAuthMapper;
import com.example.Mapper.ApplyMapper;
import com.example.POJO.Application;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class PwdResetStrategy implements ApprovalStrategy{
    @Resource
    private AdminAuthMapper adminAuthMapper;
    @Resource
    private ApplyMapper applyMapper;
    /*
    重置密码的封装策略逻辑
    通过：
        1.更新approval=1(已知的)
        approved_time,
        has_completed=1,
        working=0,
        worker(自带)
        2.重置密码
    拒绝：
        更新approval=0(已知的)
        approved_time,
        has_completed=1,
        working=0,
        worker(自带)
        reason(自带/default)
     */
    @Override
    public void approve(Application application) {
        application.setApproved_time(new Date());
        application.setHas_completed(1);
        application.setWorking(0);

        try {
            applyMapper.resultApplication(application);
            adminAuthMapper.ResetPwdByAccount("123456", application.getApply_account());
        }catch (Exception e){
            throw new CustomException("500",e.getMessage());
        }
    }

    @Override
    public void reject(Application application) {
        application.setApproved_time(new Date());
        application.setHas_completed(1);
        application.setWorking(0);
        if (application.getReason().isEmpty()){
            application.setReason("申请未通过");
        }
        try {
            applyMapper.resultApplication(application);
        }catch (Exception e){
            throw new CustomException("500",e.getMessage());
        }
    }
}
