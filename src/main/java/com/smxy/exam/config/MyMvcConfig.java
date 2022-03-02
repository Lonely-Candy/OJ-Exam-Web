package com.smxy.exam.config;

import com.smxy.exam.interceptor.LoginLerInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * MVC 配置类
 *
 * @author 范颂扬
 * @create 2022-02-14 14:16
 */
@Configuration
public class MyMvcConfig implements WebMvcConfigurer {

    /**
     * 视图解析器
     *
     * @param registry
     * @return void
     * @author 范颂扬
     * @date 2022-02-14 14:18
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("index");
        registry.addViewController("/index.html").setViewName("index");
        registry.addViewController("/login.html").setViewName("login");
        registry.addViewController("/exam/addExam.html").setViewName("exam/addExam");
        registry.addViewController("/exam/examlist.html").setViewName("exam/examlist");
    }

    /**
     * 添加拦截器
     *
     * @param registry
     * @return void
     * @author 范颂扬
     * @date 2022-02-23 16:18
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginLerInterceptor()).addPathPatterns("/**")
                // 排除不需要登录拦截的页面和请求
                .excludePathPatterns("/index.html", "/", "/login.html", "/login")
                // 排除静态资源
                .excludePathPatterns("/css/**", "/image/**", "/webjars/**");
    }
}
