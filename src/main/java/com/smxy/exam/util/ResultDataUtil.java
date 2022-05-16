package com.smxy.exam.util;


import com.smxy.exam.processing.ResultData;

/**
 * 返回数据封装工具类
 *
 * @author 范颂扬
 * @create 2022-03-29 17:50
 */
public class ResultDataUtil {

    /**
     * 快速创建请求成功的数据封装类（带数据）
     *
     * @param o 数据
     * @return com.smxy.exam.processing.ResultData
     * @author 范颂扬
     * @date 2022-03-29 17:50
     */
    public static ResultData success(Object o) {
        ResultData resultData = new ResultData().setCode(777).setMessage("成功").setData(o);
        return resultData;
    }

    /**
     * 快速创建请求成功的数据封装类
     *
     * @return com.smxy.exam.processing.ResultData
     * @author 范颂扬
     * @date 2022-03-29 17:51
     */
    public static ResultData success() {
        return success(null);
    }

    /**
     * 快速创建请求失败的数据封装类（带提示信息）
     *
     * @param code    错误代码
     * @param message 提示信息
     * @return com.smxy.exam.processing.ResultData
     * @author 范颂扬
     * @date 2022-03-29 17:51
     */
    public static ResultData error(Integer code, String message) {
        ResultData resultData = new ResultData().setMessage(message).setCode(code);
        return resultData;
    }


}
