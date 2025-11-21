package com.example.Mapper;

import com.example.POJO.Application;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface ApplyMapper {
    List<Application> pendingApplication(String type, String apply_account, boolean is_DESC);

    int lockApplication(Integer aid, Integer currentUserId, LocalDateTime now);

    int unlockApplication(Integer aid);

    int resultApplication(Application application);

//    疑点报错，目前不影响运行
    Map<String, Object> checkWorkerId(Integer aid);

    void clearApplication(LocalDateTime deadline);

    Application selectByAid(Integer aid);
}
