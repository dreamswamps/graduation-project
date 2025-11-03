package com.example.Controller;

import com.example.POJO.Application;
import com.example.Service.ApplicationService;
import com.example.Util.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/application")
public class ApplicationController {
    /*
    该Controller主要用于用户提交申请
     */
    @Resource
    private ApplicationService applicationService;

    /*
    用户提交申请
    需要包含申请类型type和申请账号apply_account
     */
    @PostMapping("/submit")
    public Result submitApplication(@RequestParam String type, @RequestParam String apply_account) {
        return applicationService.submitApplication(type, apply_account);
    }

    /*
    允许用户查看指定账号的申请状态
     */
    @GetMapping("/check")
    public Result checkApplication(@RequestParam String apply_account,
                                   @RequestParam(required = false) String type) {
        return Result.success(applicationService.checkApplication(apply_account, type));
    }
}
