package com.smxy.exam.config;

import com.smxy.exam.beans.Exam;
import com.smxy.exam.service.IExamService;
import com.smxy.exam.service.async.AsyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 项目启动后，就立刻执行的业务
 *
 * @author 范颂扬
 * @create 2022-05-01 21:18
 */
@Component
public class StarJobInit {

    private IExamService examService;

    private AsyncService asyncService;

    private static final Logger LOGGER = LoggerFactory.getLogger("business");

    /**
     * 存储当前考试正在进行或未开启，键：examId, 值：考试对象
     */
    public static Map<Integer, Exam> examMap = new HashMap<>();

    @Autowired
    public StarJobInit(IExamService examService, AsyncService asyncService) {
        this.examService = examService;
        this.asyncService = asyncService;
    }

    /**
     * 更新考试状态，对于还没有开始的考试，设置定时启动任务和关闭考试任务
     *
     * @param
     * @return void
     * @author 范颂扬
     * @date 2022-05-01 21:32
     */
    @PostConstruct
    public void init() {
        synchronized (examMap) {
            LOGGER.info("project start");
            LOGGER.info("Get a list of exams in the database");
            List<Exam> examList = examService.list();
            for (Exam exam : examList) {
                LocalDateTime nowTime = LocalDateTime.now();
                LocalDateTime beginTime = exam.getBeginTime();
                LocalDateTime endTime = exam.getEndTime();
                Integer flag = exam.getFlag();
                if (nowTime.compareTo(beginTime) > 0 && nowTime.compareTo(endTime) < 0 && flag == -1) {
                    examService.updateById(exam.setFlag(1));
                    LOGGER.info("Exam ID: " + exam.getId() + " Status updated to start");
                }
                if (flag != 0 && nowTime.compareTo(endTime) < 0) {
                    examMap.put(exam.getId(), exam);
                }
            }
            // 启动更新考试线程
            asyncService.asyncUpdateExamStatus();
        }
    }

}
