package com.smxy.exam.config;

import com.smxy.exam.interceptor.LoginLerInterceptor;
import org.springframework.context.annotation.Bean;
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
     * 添加一个配置类
     *
     * @return org.springframework.web.servlet.config.annotation.WebMvcConfigurer
     * @author 范颂扬
     * @date 2022-03-16 16:16
     */
    @Bean
    public WebMvcConfigurer getWebMvcConfigurer() {
        WebMvcConfigurer webMvcConfigurer = new WebMvcConfigurer() {
            @Override
            public void addViewControllers(ViewControllerRegistry registry) {
                registry.addViewController("/").setViewName("index");
                registry.addViewController("/index.html").setViewName("index");
                registry.addViewController("/login.html").setViewName("login");
                registry.addViewController("/main.html").setViewName("main");
                registry.addViewController("/exam/addExam.html").setViewName("exam/addExam");
                registry.addViewController("/exam/examlist.html").setViewName("exam/examlist");
                registry.addViewController("/exam/questionSet/content.html").setViewName("exam/questionSet/content");
                registry.addViewController("/exam/questionSet/problemlist.html").setViewName("exam/questionSet/problemlist");
                registry.addViewController("/exam/questionSet/ranking.html").setViewName("exam/questionSet/ranking");
                registry.addViewController("/exam/questionSet/studentlist.html").setViewName("exam/questionSet/studentlist");
                registry.addViewController("/exam/questionSet/submitCondition.html").setViewName("exam/questionSet/submitCondition");
                registry.addViewController("/exam/questionBank/completionBank.html").setViewName("exam/questionBank/completionBank");
                registry.addViewController("/exam/questionBank/addQuestion.html").setViewName("exam/questionBank/addQuestion");
            }

            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                // 排除不需要登录拦截的页面、请求和静态资源
                registry.addInterceptor(new LoginLerInterceptor()).addPathPatterns("/**")
                        .excludePathPatterns("/", "/index.html", "/login.html", "/login", "/error")
                        .excludePathPatterns("/css/**", "/image/**", "/webjars/**");
            }
        };
        return webMvcConfigurer;
    }

}
