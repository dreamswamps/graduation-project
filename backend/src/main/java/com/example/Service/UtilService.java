package com.example.Service;

import com.example.Exception.CustomException;
import com.example.Util.Result;
import com.example.Util.ValidateCode.EmailHandlerConfig;
import com.example.Util.ValidateCode.EmailHandlerCreator;
import com.example.Util.ValidateCode.ValidateCodeCreator;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;

@Service
public class UtilService {
    /*
    该service专门用于单独的util功能实现
     */

    @Resource
    private ValidateCodeCreator validateCodeCreator;

    @Resource
    private EmailHandlerCreator emailHandlerCreator;

//    生成验证码图片
    public void generateValidateCode(HttpServletRequest request, HttpServletResponse response){
        try {
            ValidateCodeCreator.CodeImageResult result = validateCodeCreator.generate();
            HttpSession session = request.getSession();
            session.setAttribute("image_captcha", result.getCode());
            session.setMaxInactiveInterval(5*60);   //  5分钟的有效时间
            response.setContentType("image/png");
            response.setHeader("Cache-Control", "no-cache, no-store");  //  不缓存，不存储
            ImageIO.write(result.getImage(), "PNG", response.getOutputStream());
        }catch (Exception e){
            throw new CustomException("500",e.getMessage());
        }
    }

//    校验验证码，需要前端提供需要验证哪种类型
    public Result verifyCaptcha(HttpServletRequest request, String code, String type) {
        try {
            HttpSession session = request.getSession();
            boolean status = false;
            if ("Image".equals(type)) {
//                由于captcha在未成功创建会话session时会为null，因此在这里可以捕获到验证码无效
                String captcha = (String) session.getAttribute("image_captcha");
                status = captcha.equalsIgnoreCase(code);
                if (status) session.removeAttribute("image_captcha");
            } else if ("Email".equals(type)) {
                String captcha = (String) session.getAttribute("email_captcha");
                status = captcha.equalsIgnoreCase(code);
                if (status) session.removeAttribute("email_captcha");
            }
            if (status) session.removeAttribute("captcha");
            return Result.success(status);
        }catch (Exception e){
            throw new CustomException("500",e.getMessage());
        }
    }

    public Result sendEmail(EmailHandlerConfig config) {
        return emailHandlerCreator.sendEmail(config);
    }

    public Result sendEmailCaptcha(HttpServletRequest request, String to) {
        try {
            String captcha = (String) emailHandlerCreator.sendEmailCaptcha(to).getData();
            HttpSession session = request.getSession();
            session.setAttribute("email_captcha", captcha);
            session.setMaxInactiveInterval(15*60);
            return Result.success("邮件已发送");
        }catch (Exception e){
            throw new CustomException("500",e.getMessage());
        }
    }

}
