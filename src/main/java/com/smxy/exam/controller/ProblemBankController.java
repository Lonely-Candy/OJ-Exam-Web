package com.smxy.exam.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smxy.exam.beans.Admin;
import com.smxy.exam.beans.ExamCompletionBank;
import com.smxy.exam.beans.ExamProcedureBank;
import com.smxy.exam.beans.ResultData;
import com.smxy.exam.service.IExamCompletionBankService;
import com.smxy.exam.service.IExamProcedureBankService;
import com.smxy.exam.util.FileUtil;
import com.smxy.exam.util.ResultDataUtil;
import com.smxy.exam.util.StringUtil;
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
import javax.websocket.server.PathParam;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Matcher;


/**
 * 题库控制器
 *
 * @author 范颂扬
 * @create 2022-03-29 17:44
 */
@Controller
public class ProblemBankController {

    public static final String TEST_DATA_FILE_PATH = "D:\\OnlineJudge\\exam\\testData";

    public static final String TEST_DATA_INPUT_DATA_FILE_NAME = "input.in";

    public static final String TEST_DATA_OUTPUT_DATA_FILE_NAME = "output.out";

    public static final String PROGRAMME_PROBLEM_FILE_NAME_FILE_NAME = "programme";

    private IExamCompletionBankService examCompletionBankService;

    private IExamProcedureBankService examProcedureBankService;

    @Autowired
    public ProblemBankController(IExamCompletionBankService examCompletionBankService
            , IExamProcedureBankService examProcedureBankService) {
        this.examCompletionBankService = examCompletionBankService;
        this.examProcedureBankService = examProcedureBankService;
    }

    /**
     * 添加填空题和编程填空题到题库中
     *
     * @param answerStr   答案
     * @param title       题目
     * @param contentHtml 题干
     * @param scoreStr    分数
     * @param type        添加题目的类型
     * @return com.smxy.exam.beans.ResultData
     * @author 范颂扬
     * @date 2022-03-29 17:59
     */
    @ResponseBody
    @PostMapping("/addProblem")
    public ResultData addProblemToBank(@RequestParam(value = "id", required = false) Integer id
            , @RequestParam("answers") String answerStr, @RequestParam("scores") String scoreStr
            , @RequestParam("title") String title, @RequestParam("contentHtml") String contentHtml
            , @RequestParam("type") String type, @RequestParam("compiles") String compileStr
            , HttpSession session) {
        // 1. 替换html标签
        contentHtml = contentHtml.replaceAll("<u>(.*?)</u>", "<t></t>");
        // 2. 从 html 中获取纯文本作为空白标题内容
        String str = Jsoup.clean(contentHtml, "", Safelist.none());
        if (title == null || title.replaceAll("\\s*", "").equals("")) {
            title = str.substring(0, 100 > str.length() ? str.length() : 100);
        }
        // 3. 添加到数据库中
        boolean res = false;
        Admin loginUserData = (Admin) session.getAttribute("loginUserData");
        if ("completion".equals(type)) {
            ExamCompletionBank examCompletion = new ExamCompletionBank().setAdminid(loginUserData.getAdminid())
                    .setTime(LocalDateTime.now()).setScore(scoreStr).setAnswer(answerStr)
                    .setTitle(title).setContent(contentHtml).setId(id);
            res = examCompletionBankService.saveOrUpdate(examCompletion);
        } else if ("programme".equals(type)) {
            ExamProcedureBank examProcedure = new ExamProcedureBank().setAdminid(loginUserData.getAdminid())
                    .setTime(LocalDateTime.now()).setScore(scoreStr != null && scoreStr.equals("") ? null : scoreStr).setAnswer(answerStr)
                    .setTitle(title).setContent(contentHtml).setId(id).setCompile(compileStr);
            res = examProcedureBankService.saveOrUpdate(examProcedure);
        }
        return res ? ResultDataUtil.success().setMessage(id == null ? "添加成功" : "修改成功")
                : ResultDataUtil.error(666, id == null ? "添加失败" : "修改失败");
    }

    /**
     * 查询题库中题目，分页查询
     *
     * @param index 显示的页号
     * @param type  题库类型
     * @return com.smxy.exam.beans.ResultData<com.smxy.exam.beans.ExamCompletionBank>
     * @author 范颂扬
     * @date 2022-03-31 16:00
     */
    @GetMapping("/getAllProblem/{index}")
    public String getAllProblem(@PathVariable("index") Integer index, @PathParam("type") String type, Model model) {
        Page page = null;
        String toPagePath = null;
        if ("completion".equals(type)) {
            toPagePath = "exam/questionBank/completionBank";
            page = new Page<ExamCompletionBank>(index, 10);
            examCompletionBankService.page(page);
        } else if ("programme".equals(type)) {
            toPagePath = "exam/questionBank/programmeBank";
            page = new Page<ExamProcedureBank>(index, 10);
            examProcedureBankService.page(page);
        }
        model.addAttribute("problemData", page);
        return toPagePath;
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
    @GetMapping("/getProblem/{proId}")
    public ResultData getProblemById(@PathVariable("proId") Integer proId
            , @PathParam("type") String type) {
        Object resultData = null;
        if ("completion".equals(type)) {
            resultData = examCompletionBankService.getById(proId);
        } else if ("programme".equals(type)) {
            resultData = examProcedureBankService.getById(proId);
        }
        if (resultData != null) {
            return ResultDataUtil.success(new ProblemShowData(resultData));
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
    public static class ProblemShowData {
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

        /**
         * 使用的编译器
         */
        private String compilers;

        public ProblemShowData(Object xxXBank) {
            if (xxXBank instanceof ExamCompletionBank) {
                createCompletionProblemShowData((ExamCompletionBank) xxXBank);
            }
            if (xxXBank instanceof ExamProcedureBank) {
                createProgrammeProblemShowData((ExamProcedureBank) xxXBank);
            }
        }

        public void createCompletionProblemShowData(ExamCompletionBank examCompletionBank) {
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
            this.totalPoints = StringUtil.getNumberNoInvalidZero(totalPoints);
            // 替换内容
            replaceContext = replaceContext(true, true, true
                    , content, answer, score);
            replaceContextShowAnswerNoEdit = replaceContext(true, false, true
                    , content, answer, score);
            replaceContextNoAnswer = replaceContext(false, true, true
                    , content, answer, score);
        }

        public void createProgrammeProblemShowData(ExamProcedureBank examProcedureBank) {
            this.id = examProcedureBank.getId();
            this.title = examProcedureBank.getTitle();
            this.content = examProcedureBank.getContent();
            this.answer = examProcedureBank.getAnswer();
            this.score = examProcedureBank.getScore();
            this.time = examProcedureBank.getTime().toString().replace('T', ' ');
            this.adminid = examProcedureBank.getAdminid();
            this.compilers = examProcedureBank.getCompile();
            // 计算总分
            String[] scores = score == null ? new String[answer.split("#").length] : score.split("#");
            this.blankSum = scores.length;
            double totalPoints = 0d;
            for (int i = 0; i < scores.length; i++) {
                totalPoints += Double.parseDouble(scores[i] == null ? "0" : scores[i]);
            }
            this.totalPoints = StringUtil.getNumberNoInvalidZero(totalPoints);
            // 替换内容
            replaceContext = replaceContext(true, true, false
                    , content, answer, score);
            replaceContextShowAnswerNoEdit = replaceContext(true, false, false
                    , content, answer, score);
            replaceContextNoAnswer = replaceContext(false, true, false
                    , content, answer, score);
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
        private String replaceContext(boolean isShowAnswer, boolean isEdit, boolean isShowScore
                , String htmlText, String answer, String score) {
            int index = 0;
            while (htmlText.indexOf("<t></t>") != -1) {
                htmlText = htmlText.replaceFirst("<t></t>", "<span id=\"input_answer_"
                        + (index++) + "\" class=\"input-answer input-group m-b\"></span>");
            }
            String[] answers = answer.split("#");
            String[] scores = score == null ? new String[answers.length] : score.split("#");
            Document document = Jsoup.parse(htmlText);
            // 关闭格式化
            document.outputSettings().indentAmount(0).prettyPrint(false);
            for (int i = 0; i < index; i++) {
                answers[i] = answers[i].replaceAll("\"", "&quot;");
                Element element = document.getElementById("input_answer_" + i);
                String htmlValue = "<input disabled value=\"" + answers[i] + "\" type=\"text\" class=\"form-control\"> ";
                if (isShowScore) {
                    htmlValue += "<span class=\"input-answer-addon input-group-addon\">" + scores[i] + " 分</span>";
                }
                element.html(htmlValue);
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
    @PostMapping("/editProblem/{id}")
    public ResultData editProblemById(@PathVariable("id") Integer id
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
    @GetMapping("/deleteProblem/{id}")
    public ResultData deleteById(@PathVariable("id") Integer id, @PathParam("type") String type) {
        boolean res = false;
        if ("compeltion".equals(type)) {
            res = examCompletionBankService.removeById(id);
        }
        if ("programme".equals(type)) {
            res = examProcedureBankService.removeById(id);
        }
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

    /**
     * 获取题目信息，解析到编辑界面，该编辑是详细的编辑界面
     *
     * @param id    题目ID
     * @param model
     * @return java.lang.String
     * @author 范颂扬
     * @date 2022-04-08 17:10
     */
    @GetMapping("/toEditPage/{id}")
    public String toEditPage(@PathVariable("id") Integer id, @PathParam("type") String type, Model model) {
        ExamCompletionBank completionProblem = null;
        ExamProcedureBank procedureProblem = null;
        String content = null;
        String[] answers = null;
        if ("completion".equals(type)) {
            completionProblem = examCompletionBankService.getById(id);
            content = completionProblem.getContent();
            answers = completionProblem.getAnswer().split("#");
        } else if ("programme".equals(type)) {
            procedureProblem = examProcedureBankService.getById(id);
            content = procedureProblem.getContent();
            answers = procedureProblem.getAnswer().split("#");
        }
        // 解析-将答案解析到题目中
        for (int i = 0; i < answers.length; i++) {
            // Matcher.quoteReplacement 过滤特殊字符
            content = content.replaceFirst("<t></t>"
                    , Matcher.quoteReplacement("<u>" + answers[i] + "</u>"));
        }
        if (completionProblem != null) {
            completionProblem.setContent(content);
        }
        if (procedureProblem != null) {
            procedureProblem.setContent(content);
        }
        model.addAttribute("type", type);
        model.addAttribute("problemData"
                , "completion".equals(type) ? completionProblem : procedureProblem);
        return "exam/questionBank/addQuestion";
    }

    /**
     * 添加编程填空题的测试数据
     *
     * @param inputData 输入数据
     * @param outData   输出数据
     * @param scoreData 分数
     * @param proId     题目ID
     * @return java.lang.String
     * @author 范颂扬
     * @date 2022-04-12 16:13
     */
    @ResponseBody
    @PostMapping("/addProgrammeTestData")
    public ResultData addProgrammeTestData(@RequestParam String inputData, @RequestParam String outData
            , @RequestParam String scoreData, @RequestParam Integer proId) {
        String programmeFolderPath = TEST_DATA_FILE_PATH + "\\" + PROGRAMME_PROBLEM_FILE_NAME_FILE_NAME + "\\" + proId;
        File[] files = FileUtil.getFolderBelowFiles(programmeFolderPath);
        String filePathPrefix = programmeFolderPath + "\\test-" + files.length + "\\";
        String inputFilePath = filePathPrefix + TEST_DATA_INPUT_DATA_FILE_NAME;
        String outputFilePath = filePathPrefix + TEST_DATA_OUTPUT_DATA_FILE_NAME;
        boolean fileExecuteResult = editInputAndOutputTestDataFile(filePathPrefix, inputFilePath
                , outputFilePath, inputData, outData);
        if (fileExecuteResult) {
            ExamProcedureBank procedureProblem = examProcedureBankService.getById(proId);
            String score = procedureProblem.getScore();
            if (score == null || score.trim().equals("")) {
                procedureProblem.setScore(scoreData);
            } else {
                score += ("#" + scoreData);
                procedureProblem.setScore(score);
            }
            boolean res = examProcedureBankService.updateById(procedureProblem);
            return res ? ResultDataUtil.success() : ResultDataUtil.error(666, "分数保存失败");
        }
        return ResultDataUtil.error(444, "文件写入失败");
    }

    /**
     * 创建测试数据文件
     *
     * @param folderPath     测试数据文件夹路径
     * @param inputFilePath  输入测试数据文件路径
     * @param outputFilePath 输出测试数据文件路径
     * @param inputData      输入数据
     * @param outputData     输出数据
     * @return boolean
     * @author 范颂扬
     * @date 2022-04-13 20:18
     */
    public boolean editInputAndOutputTestDataFile(String folderPath, String inputFilePath
            , String outputFilePath, String inputData, String outputData) {
        // 创建对应的文件夹和文件
        FileUtil.createFolder(folderPath);
        FileUtil.createFile(inputFilePath);
        FileUtil.createFile(outputFilePath);
        // 写入数据
        boolean inputFileResult = FileUtil.writeFile(inputFilePath, inputData);
        boolean outputFIleResult = FileUtil.writeFile(outputFilePath, outputData);
        return inputFileResult && outputFIleResult;
    }

    /**
     * 获取题目所有的测试数据
     *
     * @param proId
     * @return com.smxy.exam.beans.ResultData
     * @author 范颂扬
     * @date 2022-04-15 9:29
     */
    @ResponseBody
    @GetMapping("/getProgrammeAllTestData/{proId}")
    public ResultData getProgrammeTestData(@PathVariable("proId") Integer proId) {
        String programmeFolderPath = TEST_DATA_FILE_PATH + "\\" + PROGRAMME_PROBLEM_FILE_NAME_FILE_NAME + "\\" + proId;
        File[] files = FileUtil.getFolderBelowFiles(programmeFolderPath);
        // 获取文件中的测试数据
        String[] inputData = new String[files.length];
        String[] outputData = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            String inputFilePath = files[i].getPath() + "\\" + TEST_DATA_INPUT_DATA_FILE_NAME;
            String outputFilePath = files[i].getPath() + "\\" + TEST_DATA_OUTPUT_DATA_FILE_NAME;
            String[] fileContent = getInputAndOutputTestDataFileContent(inputFilePath, outputFilePath);
            inputData[i] = fileContent[0];
            outputData[i] = fileContent[1];
        }
        // 获取测试数据对应的分数
        ExamProcedureBank procedureProblem = examProcedureBankService.getById(proId);
        String score = procedureProblem.getScore();
        String[] scores = score == null ?
                new String[procedureProblem.getAnswer().split("#").length] : score.split("#");
        TestData testData = new TestData().setInputData(inputData).setOutputData(outputData)
                .setScores(scores).setSize(files.length);
        return ResultDataUtil.success(testData);
    }

    /**
     * 封装测试数据
     *
     * @author 范颂扬
     * @create 2022-04-15 9:29
     */
    @Data
    @Accessors(chain = true)
    private class TestData {
        private String[] inputData;
        private String[] outputData;
        private String[] scores;
        private int size;
    }

    /**
     * 读取测试数据内容
     *
     * @param inputFilePath
     * @param outputFilePath
     * @return java.lang.String[]
     * @author 范颂扬
     * @date 2022-04-15 9:30
     */
    public String[] getInputAndOutputTestDataFileContent(String inputFilePath, String outputFilePath) {
        String inputData = FileUtil.readFile(inputFilePath);
        String outputData = FileUtil.readFile(outputFilePath);
        return new String[]{inputData, outputData};
    }

    /**
     * 修改编程填空题的测试数据
     *
     * @param testId
     * @param proId
     * @param inputData
     * @param outputData
     * @param score
     * @return com.smxy.exam.beans.ResultData
     * @author 范颂扬
     * @date 2022-04-15 10:50
     */
    @ResponseBody
    @PostMapping("/alterProgrammeTestData")
    public ResultData alterProgrammeTestData(@RequestParam Integer testId, @RequestParam Integer proId
            , @RequestParam String inputData, @RequestParam String outputData, @RequestParam String score) {
        String programmeFolderPath = TEST_DATA_FILE_PATH + "\\" + PROGRAMME_PROBLEM_FILE_NAME_FILE_NAME + "\\"
                + proId + "\\test-" + testId;
        String inputFilePath = programmeFolderPath + "\\" + TEST_DATA_INPUT_DATA_FILE_NAME;
        String outputFilePath = programmeFolderPath + "\\" + TEST_DATA_OUTPUT_DATA_FILE_NAME;
        boolean fileResult = editInputAndOutputTestDataFile(programmeFolderPath, inputFilePath, outputFilePath, inputData, outputData);
        if (!fileResult) {
            return ResultDataUtil.error(444, "文件写入失败");
        }
        ExamProcedureBank procedureProblem = examProcedureBankService.getById(proId);
        String[] scores = procedureProblem.getScore().split("#");
        scores[testId] = score;
        StringBuffer scoreBuffer = new StringBuffer(scores[0]);
        for (int i = 1; i < scores.length; i++) {
            scoreBuffer.append(scores[i]);
            scoreBuffer.append('#');
        }
        boolean res = examProcedureBankService.updateById(procedureProblem.setScore(score));
        return res ? ResultDataUtil.success() : ResultDataUtil.error(666, "数据库写入分数失败");
    }

    /**
     * 删除测试数据
     *
     * @param proId
     * @param testId
     * @return com.smxy.exam.beans.ResultData
     * @author 范颂扬
     * @date 2022-04-15 11:52
     */
    @ResponseBody
    @GetMapping("/deleteProgrammeTestData/{proId}/{testId}")
    public ResultData deleteProgrammeTestData(@PathVariable("proId") Integer proId
            , @PathVariable("testId") Integer testId) {
        // 删除测试数据对应的分数
        ExamProcedureBank procedureProblem = examProcedureBankService.getById(proId);
        String[] scores = procedureProblem.getScore().split("#");
        StringBuffer scoreBuffer = new StringBuffer();
        for (int i = 0; i < scores.length; i++) {
            if (i == testId) {
                continue;
            }
            scoreBuffer.append(scores[i]);
            scoreBuffer.append("#");
        }
        scoreBuffer.deleteCharAt(scoreBuffer.lastIndexOf("#"));
        procedureProblem.setScore(scoreBuffer.toString());
        boolean delScoreResult = examProcedureBankService.updateById(procedureProblem);
        if (!delScoreResult) {
            return ResultDataUtil.error(666, "数据库删除分数失败！");
        }
        // 删除测试数据文件
        String programmeFolderPath = TEST_DATA_FILE_PATH + "\\" + PROGRAMME_PROBLEM_FILE_NAME_FILE_NAME + "\\" + proId;
        boolean delFileResult = FileUtil.deleteFileGeneral(programmeFolderPath + "\\test-" + testId);
        if (!delFileResult) {
            return ResultDataUtil.error(444, "测试数据文件删除失败");
        }
        File[] folders = FileUtil.getFolderBelowFiles(programmeFolderPath);
        stringSort(folders);
        for (int i = 0; i < folders.length; i++) {
            String fileName = "test-" + i;
            if (!folders[i].getName().equals(fileName)) {
                FileUtil.rename(folders[i].getPath(), fileName);
            }
        }
        return ResultDataUtil.success();
    }

    /**
     * 文件名字符串排序，先按长度排序从小到大排序，后按字典序
     *
     * @param folders
     * @author 范颂扬
     * @date 2022-04-15 14:04
     */
    private void stringSort(File[] folders) {
        Arrays.sort(folders, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                String name1 = o1.getName();
                String name2 = o2.getName();
                return name1.length() != name2.length() ?
                        Integer.compare(name1.length(), name2.length())
                        : name1.compareTo(name2);
            }
        });
    }
}
