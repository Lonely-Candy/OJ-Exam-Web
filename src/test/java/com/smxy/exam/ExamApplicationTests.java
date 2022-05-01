package com.smxy.exam;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.smxy.exam.beans.ExamCompletionStatus;
import com.smxy.exam.beans.ExamProcedureProblem;
import com.smxy.exam.beans.ExamProcedureStatus;
import com.smxy.exam.beans.ExamRecord;
import com.smxy.exam.service.*;
import com.smxy.exam.util.FileUtil;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.*;

@SpringBootTest
class ExamApplicationTests {

    public static final Logger LOGGER = LoggerFactory.getLogger("lead");

    @Autowired
    private IExamCompletionBankService examCompletionBankService;

    @Autowired
    private IExamProcedureProblemService examProcedureProblemService;

    @Autowired
    private IExamProcedureStatusService examProcedureStatusService;

    @Autowired
    private IExamCompletionStatusService examCompletionStatusService;

    @Autowired
    private IExamRecordService examRecordService;

    @Test
    void testGenerator() {
        FastAutoGenerator.create("jdbc:mysql://localhost:3307/smuoj"
                , "root", "fsy123")
                .globalConfig(builder -> {
                    builder.author("fsy").fileOverride()
                            .outputDir("D:\\Projcets\\ideaProjects\\exam\\src\\main\\java");
                })
                .packageConfig(builder -> {
                    builder.parent("com.smxy.exam").mapper("mapper").service("service")
                            .controller("controller").entity("beans").xml("mapper.xml");
                })
                .strategyConfig(builder -> {
                    builder.addInclude("admin", "user", "exam"
                            , "exam_completion_bank", "exam_completion_problem"
                            , "exam_completion_status"
                            , "exam_procedure_bank", "exam_procedure_problem"
                            , "exam_procedure_status"
                            , "exam_record")
                            .entityBuilder().enableLombok().enableChainModel()
                            .controllerBuilder().enableRestStyle()
                            .mapperBuilder().enableMapperAnnotation();
                }).execute();
    }

    @Test
    void testLog() {
        LOGGER.error("xxxx");
    }

    @Test
    void testFile() {
        File[] folders = FileUtil.getFolderBelowFiles("D:\\OnlineJudge\\exam\\testData\\programmes\\1");
        String[] folderNames = new String[folders.length];
        for (int i = 0; i < folders.length; i++) {
            folderNames[i] = folders[i].getName();
        }
        stringSort(folderNames);
        for (String name : folderNames) {
            System.out.println(name);
        }
    }

    private void stringSort(String[] names) {
        Arrays.sort(names, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.length() != o2.length() ?
                        Integer.compare(o1.length(), o2.length())
                        : o1.compareTo(o2);
            }
        });
    }

    @Test
    void testDelFile() {
        boolean res = FileUtil.deleteFileGeneral("D:\\OnlineJudge\\exam\\testData\\programmes\\1\\test-1");
        System.out.println(res);
    }

    @Test
    void testGetParentPath() {
        File file = new File("D:\\OnlineJudge\\exam\\testData\\programmes\\1\\test-0");
        String parent = file.getParent();
        System.out.println(parent);
    }

    @Test
    void testMyBatisTool() {
//        Wrapper<ExamCompletionBank> queryWrapper = new QueryWrapper<ExamCompletionBank>().in("id"
//                , new int[]{9, 10, 11});
//        List<Map<String, Object>> maps = examCompletionBankService.listMaps();
//        for (int i = 0; i < maps.size(); i++) {
//            System.out.println(maps.get(i));
//        }
        Wrapper<ExamProcedureProblem> procedureQueryWrapper = new QueryWrapper<ExamProcedureProblem>()
                .eq("exam_id", 1).select("score");
        List<Object> procedureProblems = examProcedureProblemService.listObjs(procedureQueryWrapper);
        System.out.println(procedureProblems);
    }

    @Test
    void testMybatisPlusInsert() {
//        List<ExamRecord> records = new ArrayList<>();
//        records.add(new ExamRecord().setExamId(1).setUserName("xxx").setBeginTime(new Date()).setUserId("1231231"));
//        records.add(new ExamRecord().setExamId(1).setUserName("xxx").setBeginTime(new Date()).setUserId("1231231"));
//        records.add(new ExamRecord().setExamId(1).setUserName("xxx").setBeginTime(new Date()).setUserId("1231231"));
//        records.add(new ExamRecord().setExamId(1).setUserName("xxx").setBeginTime(new Date()).setUserId("1231231"));
//        examRecordService.saveBatch(records);
//        for (int i = 0; i < records.size(); i++) {
//            System.out.println(records.get(i));
//        }
        Wrapper<ExamProcedureStatus> programmeQueryWrapper = new QueryWrapper<ExamProcedureStatus>().eq("exam_id", 1)
                .select("user_id, problem_id, case_test_data_id, max(score) as score")
                .groupBy("user_id","problem_id", "case_test_data_id");
        List<ExamProcedureStatus> procedureStatuses = examProcedureStatusService.list(programmeQueryWrapper);
        for (ExamProcedureStatus procedureStatus : procedureStatuses) {
            System.out.println(procedureStatus);
        }
    }

}
