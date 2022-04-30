package com.smxy.exam.service;

import java.util.concurrent.Future;

/**
 * @author 范颂扬
 * @create 2022-04-30 1:45
 */
public interface ExamRunCodeAsyncService {

    /**
     * 运行代码异步处理
     *
     * @param
     * @return void
     * @author 范颂扬
     * @date 2022-04-29 17:06
     */
    Future<Boolean> executeRunCodeAsync();

}
