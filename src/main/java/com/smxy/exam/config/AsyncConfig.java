package com.smxy.exam.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 定义一个线程池
 *
 * @author 范颂扬
 * @create 2022-04-27 14:47
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 声明一个线程池(并指定线程池的名字)
     *
     * @param
     * @return Executor
     * @author 范颂扬
     * @date 2022-04-27 14:47
     */
    @Bean
    public Executor asyncJudgeExecutor() {
        ThreadPoolTaskExecutor executor = new VisiableThreadPoolTaskExecutor();
        // 核心线程数5：线程池创建时候初始化的线程数
        executor.setCorePoolSize(5);
        // 最大线程数5：线程池最大的线程数，只有在缓冲队列满了之后才会申请超过核心线程数的线程
        executor.setMaxPoolSize(5);
        // 缓冲队列500：用来缓冲执行任务的队列
        executor.setQueueCapacity(500);
        // 允许线程的空闲时间60秒：当超过了核心线程出之外的线程在空闲时间到达之后会被销毁
        executor.setKeepAliveSeconds(60);
        // 线程池名的前缀：设置好了之后可以方便我们定位处理任务所在的线程池
        executor.setThreadNamePrefix("judge-async---");
        // rejection-policy：当pool已经达到max size的时候，如何处理新任务
        // CALLER_RUNS：不在新线程中执行任务，而是有调用者所在的线程来执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 执行初始化
        executor.initialize();
        return executor;
    }

}