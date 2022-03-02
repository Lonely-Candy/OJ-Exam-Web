package com.smxy.exam.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 登录拦截器
 *
 * @author 范颂扬
 * @create 2022-02-23 16:11
 */
public class LoginLerInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        Object loginUserData = session.getAttribute("loginUserData");
        if(loginUserData == null) {
            request.setAttribute("message", "no_login");
            request.getRequestDispatcher("/login.html").forward(request, response);
            return false;
        }
        return true;
    }
}
