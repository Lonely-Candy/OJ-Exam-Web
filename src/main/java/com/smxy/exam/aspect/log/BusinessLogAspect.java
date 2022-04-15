package com.smxy.exam.aspect.log;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * 业务日志切面
 *
 * @author 范颂扬
 * @create 2022-04-13 16:55
 */
@Aspect
@Component
public class BusinessLogAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger("business");

    /**
     * 请求接口日志记录
     *
     * @param
     * @return void
     * @author 范颂扬
     * @date 2022-04-13 17:23
     */
    @Before("execution(public * com.smxy.exam.controller.*.*(..))")
    public void requestLog(JoinPoint joinPoint) {
        // 执行方法的名字
        String methodName = joinPoint.getSignature().getName();
        LOGGER.info("carry out method name: " + methodName);
        // 获取 Request 对象
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        // 请求地址、请求方法、ip
        LOGGER.info("ip: " + request.getRemoteAddr()
                + "    url: " + request.getRequestURI()
                + "    method: " + request.getMethod());
        // 遍历请求参数
        Enumeration<String> paramNames = request.getParameterNames();
        int index = 1;
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            String paramVal = request.getParameter(paramName);
            LOGGER.info("param" + (index++) + "    " + paramName + "：" + paramVal);
        }
    }

    /**
     * 创建测试数据日志
     *
     * @param joinPoint 切面对象
     * @param result    执行结果
     * @return void
     * @author 范颂扬
     * @date 2022-04-13 19:48
     */
    @AfterReturning(value = "execution(private boolean com.smxy.exam.controller.ProblemBankController.editInputAndOutputTestDataFile(..))"
            , returning = "result")
    public void createTestDataLog(JoinPoint joinPoint, boolean result) {
        // 参数名
        String[] parameterNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
        // 参数值
        Object[] parameterValues = joinPoint.getArgs();
        // 打印参数
        for (int i = 0; i < parameterNames.length; i++) {
            String paramName = parameterNames[i];
            String paramValue = (String) parameterValues[i];
            switch (paramName) {
                case "folderPath":
                    LOGGER.info("change folder: " + paramValue);
                    continue;
                case "inputFilePath":
                    LOGGER.info("write input file: " + paramValue);
                    continue;
                case "outputFilePath":
                    LOGGER.info("write output file: " + paramValue);
                    continue;
                case "proId":
                    LOGGER.info("change problem id:" + paramValue);
                    continue;
                case "inputData":
                    LOGGER.info("write input file content: " + paramValue);
                    continue;
                case "outputData":
                    LOGGER.info("write output file content: " + paramValue);
                    continue;
                default:
                    continue;
            }
        }
        // 打印结果
        LOGGER.info("create test data file executive outcome: " + result);
    }

}
