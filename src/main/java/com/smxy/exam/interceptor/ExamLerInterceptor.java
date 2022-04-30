package com.smxy.exam.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 考试拦截器
 *
 * @author 范颂扬
 * @create 2022-04-30 0:20
 */
public class ExamLerInterceptor implements HandlerInterceptor {

    /**
     * 考试拦截
     *
     * @param request
     * @param response
     * @param handler
     * @return boolean
     * @author 范颂扬
     * @date 2022-04-30 0:21
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        boolean isAdmin = (boolean) session.getAttribute("isAdmin");
        if (isAdmin) {
            request.setAttribute("message", "not_a_student");
            request.getRequestDispatcher("/login.html").forward(request, response);
            return false;
        }
        return true;
    }
}
