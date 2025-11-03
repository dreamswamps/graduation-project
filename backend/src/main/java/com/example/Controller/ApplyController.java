package com.example.Controller;

import com.example.POJO.Application;
import com.example.Service.AdminService;
import com.example.Service.ApplyService;
import com.example.Util.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/apply")
public class ApplyController {
    /*
    该Controller主要用于管理员审批申请
     */
    @Resource
    private ApplyService applyService;
    @Resource
    private AdminService adminService;

    /*
    管理员获取所有待处理申请，未经过分页处理
    需要提供管理员id
    可以提供模糊查询的类型type，精确查询的申请账号apply_account，和决定申请时间升序降序的is_DESC(1表示降序)
     */
    @GetMapping("/pending")
    public Result pendingApplication(@RequestParam(required = false) String type,
                                     @RequestParam(required = false) String apply_account,
                                     @RequestParam(required = false, defaultValue = "false") boolean is_DESC,
                                     @RequestHeader("X-Current-User-ID") Integer currentUserId) {
        adminService.IDAuthManager(currentUserId);
        return Result.success(applyService.pendingApplication(type, apply_account, is_DESC));
    }

    /*
    锁定working编辑状态，返回true/false
    为了避免两个管理员同时审批同个申请导致的后者覆盖前者操作。
    可能没法根除这个问题
    需要提供申请的aid，同样需要保存管理员身份和审批时间
     */
    @PutMapping("/lock/{aid}")
    public Result lockApplication(@PathVariable Integer aid,
                                  @RequestHeader("X-Current-User-ID") Integer currentUserId) {
        adminService.IDAuthManager(currentUserId);
        return Result.success(applyService.lockApplication(aid, currentUserId));
    }

    /*
    解锁working
    搭配锁定。由于非常规完成审批，因此需要清空worker。
     */
    @PutMapping("/unlock/{aid}")
    public Result unlockApplication(@PathVariable Integer aid,
                                    @RequestHeader("X-Current-User-ID") Integer currentUserId) {
        adminService.IDAuthManager(currentUserId);
        return Result.success(applyService.unlockApplication(aid));
    }

    /*
    管理员审批申请
    需要审批结果1->通过/other->拒绝，和对应的aid
    审批完毕后解锁working并设置完成时间
    更新：为了支持策略模式的可拓展性和针对性，现在该接口被升级为需要提供type类型和原因reason
         同时，考虑到需要的数据量增多，修改为使用body直接传递类
     */
    @PutMapping("/result")
    public Result resultApplication(@RequestBody Application application,
                                    @RequestHeader("X-Current-User-ID") Integer currentUserId) {
        adminService.IDAuthManager(currentUserId);
        return Result.success(applyService.resultApplication(application, currentUserId));
    }

    /*
    清除所有状态异常的申请，释放working状态
    状态异常：仍处于working状态，但是距离审批时间已经过去了15min
     */
    @PutMapping("/clear")
    public Result clearApplication(@RequestHeader("X-Current-User-ID") Integer currentUserId) {
        adminService.IDAuthManager(currentUserId);
        applyService.clearApplication();
        return Result.success("清理完毕");
    }

    /*
    心跳检测
    前端在审批界面组件渲染后的15分钟后，若仍未被卸载，则向后端发送一次心跳检测
    如果此时出现了仍在审批working = 1并且仍未审批结束is_completed这种管理员长时间不操作的情况,释放working资源
    所有heartbeat都需要前端刷新页面
    正常返回true->管理员操作异常->刷新/false->该数据已被释放/完成->刷新
     */
    @GetMapping("/heartbeat/{aid}")
    public Result heartbeat(@PathVariable Integer aid,
                            @RequestHeader("X-Current-User-ID") Integer currentUserId) {
        adminService.IDAuthManager(currentUserId);
        return applyService.heartbeat(aid);
    }
}
