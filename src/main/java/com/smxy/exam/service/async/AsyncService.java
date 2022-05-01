package com.smxy.exam.service.async;

/**
 * 异步服务
 *
 * @author 范颂扬
 * @create 2022-05-01 22:53
 */
public interface AsyncService {

    /**
     * 启动异步线程，更新考试状态
     *
     * @param
     * @return void
     * @author 范颂扬
     * @date 2022-05-01 22:23
     */
    void asyncUpdateExamStatus();

}
