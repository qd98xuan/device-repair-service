package com.example.repair.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一返回结果类
 */
@Data
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
     * 成功状态码
     */
    public static final Integer SUCCESS_CODE = 200;

    /**
     * 失败状态码
     */
    public static final Integer ERROR_CODE = 500;

    /**
     * 私有构造函数
     */
    private Result() {
    }

    /**
     * 成功返回（无数据）
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 成功返回（带数据）
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(SUCCESS_CODE);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    /**
     * 成功返回（带消息）
     */
    public static <T> Result<T> success(String message) {
        Result<T> result = new Result<>();
        result.setCode(SUCCESS_CODE);
        result.setMessage(message);
        result.setData(null);
        return result;
    }

    /**
     * 成功返回（带消息和数据）
     */
    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(SUCCESS_CODE);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    /**
     * 失败返回
     */
    public static <T> Result<T> error() {
        return error("操作失败");
    }

    /**
     * 失败返回（带消息）
     */
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(ERROR_CODE);
        result.setMessage(message);
        return result;
    }

    /**
     * 失败返回（带状态码和消息）
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return SUCCESS_CODE.equals(this.code);
    }
}
