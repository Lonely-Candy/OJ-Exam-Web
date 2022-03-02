package com.smxy.exam;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SpringBoot 主启动类
 *
 * @author 范颂扬
 * @create 2022-02-10 11:16
 */
@MapperScan("com.smxy.exam.mapper")
@SpringBootApplication
public class ExamApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExamApplication.class, args);
    }

}
