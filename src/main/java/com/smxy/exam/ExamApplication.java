package com.smxy.exam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * SpringBoot 主启动类
 * EnableAspectJAutoProxy 开启切面编程
 *
 * @author 范颂扬
 * @create 2022-02-10 11:16
 */
@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class ExamApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExamApplication.class, args);
    }

}
