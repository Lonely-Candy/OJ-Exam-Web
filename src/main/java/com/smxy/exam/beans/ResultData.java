package com.smxy.exam.beans;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author 范颂扬
 * @create 2022-03-29 17:46
 */
@Data
@Accessors(chain = true)
public class ResultData<T> {

    /**
     * 错误代码
     * 777 - 成功处理
     * 666 - 数据库处理错误
     *
     */
    private Integer code;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 具体内容
     */
    private T data;
}
