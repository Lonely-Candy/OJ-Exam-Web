package com.smxy.exam.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 考试考试器
 *
 * @author 范颂扬
 * @create 2022-02-23 19:14
 */
@Controller
public class ExamController {

    /**
     * 获取所有的考试列表
     *
     * @param model
     * @return java.lang.String
     * @author 范颂扬
     * @date 2022-02-23 19:15
     */
    @GetMapping("/examlist")
    public String getExamList(Model model) {
        return "";
    }

    @PostMapping("/addExam")
    public String addExam() {
        return "";
    }

}
