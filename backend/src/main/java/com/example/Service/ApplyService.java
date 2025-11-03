package com.example.Service;

import com.example.Exception.CustomException;
import com.example.Mapper.ApplyMapper;
import com.example.POJO.Application;
import com.example.Util.Result;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class ApplyService {
    @Resource
    private ApplyMapper applyMapper;

//    boolean需要提供，执行该方法时顺道清理状态异常的申请
    public List<Application> pendingApplication(String type, String apply_account, boolean is_DESC) {
        try {
            clearApplication();
            return applyMapper.pendingApplication(type, apply_account, is_DESC);
        }catch (Exception e){
            return null;
        }
    }

    public boolean lockApplication(Integer aid, Integer currentUserId) {
        LocalDateTime now = LocalDateTime.now();
        return applyMapper.lockApplication(aid, currentUserId, now) > 0;
    }

    public boolean unlockApplication(Integer aid) {
        return applyMapper.unlockApplication(aid) > 0;
    }

//    更新审批结果approval。同时需要更新:审批时间，完成状态，禁用状态
//    更新:现在直接传递类，该类应该至少包含了aid,approval,和reason(可为null)
    public boolean resultApplication(Application application, Integer currentUserId) {
        try {
            if (!checkWorkerId(application.getAid(), currentUserId)) {
                return false;
            }
//            为了避免传输进乱七八糟的值，只有为1时才设置为1(通过)，其他均为0
            application.setApproval((application.getApproval() == 1) ? 1 : 0);
            application.setApproved_time(new Date());
            application.setWorker(currentUserId);
            return applyMapper.resultApplication(application) > 0;
        }catch (Exception e) {
            return false;
        }
    }

//    检查请求修改的id是否和aid对应的worker匹配
    public boolean checkWorkerId(Integer aid, Integer currentUserId) {
        return Objects.equals(currentUserId, applyMapper.checkWorkerId(aid));
    }

    public void clearApplication() {
        try {
            LocalDateTime deadline = LocalDateTime.now().minusMinutes(15);
            applyMapper.clearApplication(deadline);
        }catch (Exception e) {
            throw new CustomException("500", e.getMessage());
        }
    }

    public Result heartbeat(Integer aid) {
        try {
            Application application = applyMapper.selectByAid(aid);
            Integer completed = application.getHas_completed();
            Integer working = application.getWorking();
            if (completed == 0 && working  == 1) {
                unlockApplication(aid);
                return Result.success("管理员长时间未审批申请，页面将刷新");
            } else if (completed == 1) {
                return Result.success("该申请已经被审批完成且审批时间结束，页面将刷新");
            } else if (working == 0) {
                return Result.success("该申请已被释放且审批时间结束，页面将刷新");
            }
//            这是不可能的，除非修改数据库数据
            return Result.success("不是你怎么做到的？？？");
        }catch (Exception e) {
            return Result.error("状态异常");
        }
    }
}
