package com.example.Mapper;

import com.example.POJO.Application;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ApplicationMapper {
    int submitApplication(Application application);

//    该方法未筛选
    List<Application> checkApplication(String apply_account, String type);
}
