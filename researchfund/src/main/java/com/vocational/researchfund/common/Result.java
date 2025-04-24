package com.vocational.researchfund.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * API响应结果封装
 * @param <T> 数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 状态码
     */
    private Integer code;
    
    /**
     * 消息
     */
    private String message;
    
    /**
     * 数据
     */
    private T data;
    
    /**
     * 成功结果
     * @param <T> 数据类型
     * @return 结果
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }
    
    /**
     * 成功结果
     * @param data 数据
     * @param <T> 数据类型
     * @return 结果
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }
    
    /**
     * 成功结果
     * @param message 消息
     * @param data 数据
     * @param <T> 数据类型
     * @return 结果
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }
    
    /**
     * 失败结果
     * @param code 状态码
     * @param message 消息
     * @param <T> 数据类型
     * @return 结果
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }
    
    /**
     * 失败结果
     * @param message 消息
     * @param <T> 数据类型
     * @return 结果
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }
    
    /**
     * 失败结果 (400 Bad Request)
     * @param message 消息
     * @param <T> 数据类型
     * @return 结果
     */
    public static <T> Result<T> fail(String message) {
        return new Result<>(400, message, null);
    }
} 