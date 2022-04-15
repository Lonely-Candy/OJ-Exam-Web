package com.smxy.exam.aspect.log;

import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 业务日志切面
 *
 * @author 范颂扬
 * @create 2022-04-13 16:55
 */
@Aspect
@Component
public class BussinessLogAspect {

    public final Logger logger = LoggerFactory.getLogger("bussiness");

}
