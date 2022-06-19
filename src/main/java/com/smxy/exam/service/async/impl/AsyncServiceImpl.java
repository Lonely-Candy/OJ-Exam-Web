package com.smxy.exam.service.async.impl;

import com.smxy.exam.beans.Exam;
import com.smxy.exam.config.StarJobInit;
import com.smxy.exam.service.IExamService;
import com.smxy.exam.service.async.AsyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * @author 范颂扬
 * @create 2022-05-01 22:54
 */
@Service
public class AsyncServiceImpl implements AsyncService {

    private IExamService examService;

    private static final Logger LOGGER = LoggerFactory.getLogger("business");

    @Autowired
    public AsyncServiceImpl(IExamService examService) {
        this.examService = examService;
    }

    /**
     * 启动异步线程，更新考试状态
     *
     * @param
     * @return void
     * @author 范颂扬
     * @date 2022-05-01 22:23
     */
    @Async("asyncExecutor")
    @Override
    public void asyncUpdateExamStatus() {
        LOGGER.info("update exam status async begin");
        while (true) {
            synchronized (StarJobInit.examMap) {
                for (Integer id : StarJobInit.examMap.keySet()) {
                    Exam exam = StarJobInit.examMap.get(id);
                    LocalDateTime nowTime = LocalDateTime.now();
                    LocalDateTime beginTime = exam.getBeginTime();
                    LocalDateTime endTime = exam.getEndTime();
                    Integer flag = exam.getFlag();
                    if (nowTime.compareTo(beginTime) > 0 && nowTime.compareTo(endTime) < 0 && flag == -1) {
                        // 考试开始
                        examService.updateById(exam.setFlag(1));
                        LOGGER.info("Exam ID: " + exam.getId() + " Status updated to start");
                    } else if (nowTime.compareTo(endTime) > 0 && flag == 1) {
                        // 考试结束
                        examService.updateById(exam.setFlag(0));
                        LOGGER.info("Exam ID: " + exam.getId() + " Status updated to Closed");
                        StarJobInit.examMap.remove(id);
                    }
                }
            }
        }
    }
}
