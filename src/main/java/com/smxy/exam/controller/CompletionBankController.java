package com.smxy.exam.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smxy.exam.beans.Admin;
import com.smxy.exam.beans.ExamCompletionBank;
import com.smxy.exam.beans.ResultData;
import com.smxy.exam.service.IExamCompletionBankService;
import com.smxy.exam.util.ResultDataUtil;
import lombok.Data;
import lombok.experimental.Accessors;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.LocalDateTime;


/**
 * 题库控制器
 *
 * @author 范颂扬
 * @create 2022-03-29 17:44
 */
@Controller
public class CompletionBankController {

    private IExamCompletionBankService examCompletionBankService;

    @Autowired
    public CompletionBankController(IExamCompletionBankService examCompletionBankService) {
        this.examCompletionBankService = examCompletionBankService;
    }

    /**
     * 添加填空题到题库中
     *
     * @param answers     答案
     * @param title       题目
     * @param contentHtml 题干
     * @param scores      分数
     * @return com.smxy.exam.beans.ResultData
     * @author 范颂扬
     * @date 2022-03-29 17:59
     */
    @ResponseBody
    @PostMapping("/addCompletion")
    public ResultData addCompletionProblemToBank(@RequestParam("answers") String[] answers
            , @RequestParam(value = "title", required = false, defaultValue = "") String title
            , @RequestParam("contentHtml") String contentHtml, @RequestParam("scores") String[] scores
            , HttpSession session) {
        // 1. 替换html标签
        contentHtml = contentHtml.replaceAll("<u>(.*?)</u>", "<t></t>");
        // 2. 从 html 中获取纯文本作为空白标题内容
        String str = Jsoup.clean(contentHtml, "", Safelist.none());
        if (title == null || title.replaceAll("\\s*", "").equals("")) {
            title = str.substring(0, 100 > str.length() ? str.length() : 100);
        }
        // 3. 答案&分数处理
        String answersValue = "";
        String scoresValue = "";
        for (int i = 0; i < answers.length - 1; i++) {
            answersValue += answers[i] + "#";
            scoresValue += scores[i] + "#";
        }
        answersValue += answers[answers.length - 1];
        scoresValue += scores[answers.length - 1];
        // 4. 添加到数据库中
        Admin loginUserData = (Admin) session.getAttribute("loginUserData");
        ExamCompletionBank examCompletion = new ExamCompletionBank().setAdminid(loginUserData.getAdminid())
                .setTime(LocalDateTime.now()).setScore(scoresValue).setAnswer(answersValue)
                .setTitle(title).setContent(contentHtml);
        boolean res = examCompletionBankService.save(examCompletion);
        return res ? ResultDataUtil.success() : ResultDataUtil.error(666, "添加失败");
    }

    /**
     * 查询填空题库中题目，分页查询
     *
     * @return com.smxy.exam.beans.ResultData<com.smxy.exam.beans.ExamCompletionBank>
     * @author 范颂扬
     * @date 2022-03-31 16:00
     */
    @GetMapping("/getAllCompletion/{index}")
    public String getAllCompletionProblem(@PathVariable("index") Integer index, Model model) {
        // 分页查询
        Page<ExamCompletionBank> page = new Page<ExamCompletionBank>(index, 10);
        examCompletionBankService.page(page);
        model.addAttribute("problemData", page);
        return "exam/questionBank/completionBank";
    }

    /**
     * 根据题号查询题目
     *
     * @param proId 题号
     * @return com.smxy.exam.beans.ResultData
     * @author 范颂扬
     * @date 2022-04-05 18:54
     */
    @ResponseBody
    @GetMapping("/getCompletion/{proId}")
    public ResultData getCompletionProblemById(@PathVariable("proId") Integer proId) {
        ExamCompletionBank resultData = examCompletionBankService.getById(proId);
        if (resultData != null) {
            return ResultDataUtil.success(new CompletionShowData(resultData));
        }
        return ResultDataUtil.error(666, "数据库查询错误");
    }

    /**
     * 填空题显示数据类
     *
     * @author 范颂扬
     * @create 2022-04-07 15:40
     */
    @Data
    @Accessors(chain = true)
    private class CompletionShowData {
        /**
         * 题号
         */
        private Integer id;

        /**
         * 标题
         */
        private String title;

        /**
         * 题干
         */
        private String content;

        /**
         * 参考答案，每个空用#隔开
         */
        private String answer;

        /**
         * 得分，每个空用#隔开
         */
        private String score;

        /**
         * 创建人
         */
        private String adminid;

        /**
         * 创建时间
         */
        private String time;

        /**
         * 总分
         */
        private String totalPoints;

        /**
         * 替换后的文本内容-默认显示答案可编辑
         */
        private String replaceContext;

        /**
         * 替换后的文本内容-显示答案不可编辑
         */
        private String replaceContextShowAnswerNoEdit;

        /**
         * 替换后的文本内容-不显示答案可编辑
         */
        private String replaceContextNoAnswer;

        /**
         * 需要填空的空数
         */
        private Integer blankSum;

        public CompletionShowData(ExamCompletionBank examCompletionBank) {
            this.id = examCompletionBank.getId();
            this.title = examCompletionBank.getTitle();
            this.content = examCompletionBank.getContent();
            this.answer = examCompletionBank.getAnswer();
            this.score = examCompletionBank.getScore();
            this.time = examCompletionBank.getTime().toString().replace('T', ' ');
            this.adminid = examCompletionBank.getAdminid();
            // 计算总分
            String[] scores = score.split("#");
            this.blankSum = scores.length;
            double totalPoints = 0d;
            for (int i = 0; i < scores.length; i++) {
                totalPoints += Double.parseDouble(scores[i]);
            }
            this.totalPoints = new BigDecimal(String.valueOf(totalPoints))
                    .stripTrailingZeros().toPlainString();
            // 替换内容
            replaceContext = replaceContext(true, true, content, answer, score);
            replaceContextShowAnswerNoEdit = replaceContext(true, false, content, answer, score);
            replaceContextNoAnswer = replaceContext(false, true, content, answer, score);
        }

        /**
         * 将填空题显示的样式替换上去
         *
         * @param isShowAnswer 是否显示填空题答案
         * @param isEdit       是否可编辑
         * @param htmlText     html
         * @param answer       答案
         * @param score        分数
         * @return java.lang.String
         * @author 范颂扬
         * @date 2022-04-05 18:38
         */
        private String replaceContext(boolean isShowAnswer, boolean isEdit, String htmlText, String answer, String score) {
            int index = 0;
            while (htmlText.indexOf("<t></t>") != -1) {
                htmlText = htmlText.replaceFirst("<t></t>", "<div id=\"input_answer_"
                        + (index++) + "\" class=\"input-answer input-group m-b\"></div>");
            }
            htmlText = htmlText.replaceAll("<p>", "");
            htmlText = htmlText.replaceAll("</p>", "");
            String[] answers = answer.split("#");
            String[] scores = score.split("#");
            Document document = Jsoup.parse(htmlText);
            for (int i = 0; i < index; i++) {
                Element element = document.getElementById("input_answer_" + i);
                element.html("<input disabled value=\"" + answers[i] + "\" type=\"text\" class=\"form-control\"> " +
                        "<span class=\"input-answer-addon input-group-addon\">" + scores[i] + " 分</span>");
                Element inputTag = element.getElementsByTag("input").get(0);
                if (!isShowAnswer) {
                    inputTag.removeAttr("value");
                }
                if (isEdit) {
                    inputTag.removeAttr("disabled");
                }
            }
            return document.body().html();
        }
    }

    /**
     * 根据ID修改填空题（题库中修改答案）
     *
     * @return com.smxy.exam.beans.ResultData
     * @author 范颂扬
     * @date 2022-04-07 15:52
     */
    @ResponseBody
    @PostMapping("/editCompletion/{id}")
    public ResultData editCompletionProblemById(@PathVariable("id") Integer id
            , @RequestParam String answer) {
        ExamCompletionBank examCompletion = new ExamCompletionBank().setId(id).setAnswer(answer);
        boolean res = examCompletionBankService.updateById(examCompletion);
        return res ? ResultDataUtil.success()
                : ResultDataUtil.error(666, "修改失败");
    }

    /**
     * 根据ID删除填空题
     *
     * @param id 题目ID
     * @return com.smxy.exam.beans.ResultData
     * @author 范颂扬
     * @date 2022-04-07 17:10
     */
    @ResponseBody
    @GetMapping("/deleteCompletion/{id}")
    public ResultData deleteCompletionById(@PathVariable("id") Integer id) {
        boolean res = examCompletionBankService.removeById(id);
        return res ? ResultDataUtil.success()
                : ResultDataUtil.error(666, "数据库错误");
    }

    /**
     * 模糊查询填空题题目
     *
     * @param keyword 关键字
     * @param model
     * @return java.lang.String
     * @author 范颂扬
     * @date 2022-04-07 19:59
     */
    @PostMapping("/findCompletion")
    public String findCompletionByKeyword(@RequestParam String keyword, Model model) {
        long count = examCompletionBankService.count();
        Page<ExamCompletionBank> page = new Page<ExamCompletionBank>(1, count);
        Wrapper<ExamCompletionBank> querywrapper = new QueryWrapper<ExamCompletionBank>()
                .like("title", keyword).or()
                .like("content", keyword).or()
                .like("adminid", keyword);
        examCompletionBankService.page(page, querywrapper);
        model.addAttribute("problemData", page);
        model.addAttribute("keyword", keyword);
        return "exam/questionBank/completionBank";
    }
}
