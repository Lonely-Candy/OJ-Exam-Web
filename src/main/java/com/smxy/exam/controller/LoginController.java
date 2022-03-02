package com.smxy.exam.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.smxy.exam.beans.Admin;
import com.smxy.exam.beans.User;
import com.smxy.exam.service.IAdminService;
import com.smxy.exam.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpSession;

/**
 * 登录业务处理器
 *
 * @author 范颂扬
 * @create 2022-02-22 13:13
 */
@Controller
public class LoginController {

    private IUserService userService;

    private IAdminService adminService;

    @Autowired
    public LoginController(IUserService userService, IAdminService adminService) {
        this.adminService = adminService;
        this.userService = userService;
    }

    /**
     * 登录接口
     *
     * @param user    表单用户信息
     * @param session 会话对象
     * @param model   模块对象
     * @return java.lang.String 返回视图
     * @author 范颂扬
     * @date 2022-02-22 15:51
     */
    @PostMapping("/login")
    public String login(User user, HttpSession session, Model model) {
        boolean isAdmin = false;
        User queryUserResult = null;
        Admin queryAdminResult = null;
        Object loginUserData = null;
        // 1. 首先查询是否学生登录
        QueryWrapper<User> userWrapper = new QueryWrapper<User>()
                .eq("userid", user.getUserid())
                .eq("password", user.getPassword());
        queryUserResult = userService.getOne(userWrapper);
        if (queryUserResult == null) {
            // 2. 再查询是否教师登录
            QueryWrapper<Admin> adminWrapper = new QueryWrapper<Admin>()
                    .eq("adminid", user.getUserid())
                    .eq("password", user.getPassword());
            queryAdminResult = adminService.getOne(adminWrapper);
            if (queryAdminResult == null) {
                model.addAttribute("message", "no_user");
                return "login";
            }
            isAdmin = true;
        }
        // 3. 封装数据
        loginUserData = isAdmin ? queryAdminResult : queryUserResult;
        session.setAttribute("isAdmin", isAdmin);
        session.setAttribute("loginUserData", loginUserData);
        return "exam/examlist.html";
    }

    /**
     * 登出接口
     *
     * @param session 会话对象
     * @return java.lang.String
     * @author 范颂扬
     * @date 2022-02-23 16:33
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("loginUserData");
        return "redirect:/index.html";
    }

}
