package com.smxy.exam.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.smxy.exam.beans.*;
import com.smxy.exam.config.StarJobInit;
import com.smxy.exam.service.*;
import com.smxy.exam.util.ResultDataUtil;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.websocket.server.PathParam;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 考试处理器
 *
 * @author 范颂扬
 * @create 2022-04-21 16:59
 */
@Controller
@RequestMapping("/exam")
public class ExamController {

    private IExamService examService;

    private IExamProcedureProblemService examProcedureProblemService;

    private IExamProcedureStatusService examProcedureStatusService;

    private IExamCompletionProblemService examCompletionProblemService;

    private IExamCompletionStatusService examCompletionStatusService;

    @Autowired
    public ExamController(IExamService examService, IExamProcedureProblemService examProcedureProblemService
            , IExamProcedureStatusService examProcedureStatusService, IExamCompletionStatusService examCompletionStatusService
            , IExamCompletionProblemService examCompletionProblemService) {
        this.examService = examService;
        this.examProcedureProblemService = examProcedureProblemService;
        this.examCompletionProblemService = examCompletionProblemService;
        this.examProcedureStatusService = examProcedureStatusService;
        this.examCompletionStatusService = examCompletionStatusService;
    }

    /**
     * 封装考试对象，用于界面解析
     */
    @Data
    @Accessors(chain = true)
    private class ProcessExamBean {
        /**
         * 考试ID
         */
        private Integer id;

        /**
         * 考试标题
         */
        private String title;

        /**
         * 考试开始时间
         */
        private String beginTime;

        /**
         * 考试结束时间
         */
        private String endTime;

        /**
         * 考试时长，单位：分钟
         */
        private Integer length;

        /**
         * 考试状态，-1未开始，1已开始
         */
        private Integer flag;

        /**
         * 创建人
         */
        private String author;

        /**
         * 是否开启监控，-1否，1是
         */
        private Integer isCheck;

        public ProcessExamBean(Exam exam) {
            this.author = exam.getAuthor();
            this.beginTime = exam.getBeginTime().toString().replace('T', ' ');
            this.endTime = exam.getEndTime().toString().replace('T', ' ');
            this.isCheck = exam.getIsCheck();
            this.flag = exam.getFlag();
            this.id = exam.getId();
            this.length = exam.getLength();
            this.title = exam.getTitle();
        }
    }

    /**
     * 获取所有的考试列表
     *
     * @param model
     * @return java.lang.String
     * @author 范颂扬
     * @date 2022-02-23 19:15
     */
    @GetMapping("/examList")
    public String getExamList(Model model) {
        List<Exam> examList = examService.list();
        // 转换封装类
        List<ProcessExamBean> exams = new ArrayList<>();
        for (Exam exam : examList) {
            ProcessExamBean examBean = new ProcessExamBean(exam);
            exams.add(examBean);
        }
        model.addAttribute("exams", exams == null ? new ArrayList<>() : exams);
        return "exam/examlist";
    }

    /**
     * 添加考试
     *
     * @param title     考试标题
     * @param datetimes 考试时间戳
     * @param length    考试时长
     * @param isCheck   是否开启监控
     * @param session   会话对象
     * @param model
     * @return java.lang.String
     * @author 范颂扬
     * @date 2022-03-11 16:18
     */
    @PostMapping("/addExam")
    public String addExam(@RequestParam("title") String title, @RequestParam(value = "id", required = false) Integer id
            , @RequestParam("datetimes") String datetimes, @RequestParam("length") int length
            , @RequestParam(value = "isCheck", defaultValue = " ", required = false) String isCheck
            , HttpSession session, Model model) {
        // 1. 获取创建的教师 id
        Admin userData = (Admin) session.getAttribute("loginUserData");
        String author = userData.getAdminid();
        // 2. 处理时间数据
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String[] times = datetimes.split(" - ");
        LocalDateTime beginTime = LocalDateTime.parse(times[0], df);
        LocalDateTime endTime = LocalDateTime.parse(times[1], df);
        LocalDateTime nowTime = LocalDateTime.now();
        int flag = (nowTime.compareTo(endTime) > 0) ? 0 : (nowTime.compareTo(beginTime) < 0 ? -1 : 1);
        // 4. 创建对象
        Exam exam = new Exam().setId(id).setAuthor(author).setIsCheck(isCheck.equals("on") ? 1 : -1)
                .setTitle(title).setBeginTime(beginTime).setEndTime(endTime).setFlag(flag)
                .setLength(length);
        // 5. 执行操作
        boolean res = examService.saveOrUpdate(exam);
        if (!res) {
            // 6. 插入数据库失败
            model.addAttribute("message", "创建失败");
            return "exam/addExam";
        }
        // 7. 数据插入成功
        if (flag != 0) {
            synchronized (StarJobInit.examMap) {
                StarJobInit.examMap.put(exam.getId(), exam);
            }
        }
        return "redirect:/exam/examList";
    }

    /**
     * 获取考试信息，进入修改界面
     *
     * @param id    考试 ID
     * @param model
     * @return java.lang.String
     * @author 范颂扬
     * @date 2022-03-11 17:41
     */
    @GetMapping("/editExam/{id}")
    public String editExam(@PathVariable("id") Integer id, Model model) {
        Exam exam = examService.getById(id);
        model.addAttribute("exam", new ProcessExamBean(exam));
        return "exam/addExam";
    }

    /**
     * 删除考试
     *
     * @param examId
     * @return com.smxy.exam.beans.ResultData
     * @author 范颂扬
     * @date 2022-04-21 17:30
     */
    @ResponseBody
    @Transactional(rollbackFor = {Exception.class})
    @GetMapping("/delExam/{examId}")
    public ResultData delExam(@PathVariable("examId") Integer examId) {
        // 删除考试
        boolean res = examService.removeById(examId);
        // 删除考试对应学生提交记录
        Wrapper<ExamProcedureStatus> procedureStatusQueryWrapper = new QueryWrapper<ExamProcedureStatus>()
                .eq("exam_id", examId);
        Wrapper<ExamCompletionStatus> completionStatusQueryWrapper = new QueryWrapper<ExamCompletionStatus>()
                .eq("exam_id", examId);
        examProcedureStatusService.remove(procedureStatusQueryWrapper);
        examCompletionStatusService.remove(completionStatusQueryWrapper);
        // 删除考试对应的题目
        Wrapper<ExamProcedureProblem> procedureProblemQueryWrapper = new QueryWrapper<ExamProcedureProblem>()
                .eq("exam_id", examId);
        Wrapper<ExamCompletionProblem> completionProblemQueryWrapper = new QueryWrapper<ExamCompletionProblem>()
                .eq("exam_id", examId);
        examProcedureProblemService.remove(procedureProblemQueryWrapper);
        examCompletionProblemService.remove(completionProblemQueryWrapper);
        return res ? ResultDataUtil.success() : ResultDataUtil.error(666, "删除失败");
    }

    /**
     * 手动改变考试状态
     *
     * @param examId
     * @return com.smxy.exam.beans.ResultData
     * @author 范颂扬
     * @date 2022-04-21 17:38
     */
    @GetMapping("/changeState/{examId}")
    public String stopExam(@PathVariable("examId") Integer examId) {
        // 关闭考试
        examService.updateById(new Exam().setId(examId).setFlag(0));
        return "redirect:/exam/examList";
    }

}
