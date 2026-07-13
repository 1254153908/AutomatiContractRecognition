package org.huhu.contract.common;

import lombok.Data;

/**
 * 统一响应结果封装
 */
@Data
public class R<T> {

    private int code;
    private String msg;
    private T data;

    private R(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> R<T> ok(T data) {
        return new R<>(200, "成功", data);
    }

    public static <T> R<T> ok() {
        return new R<>(200, "成功", null);
    }

    public static <T> R<T> ok(String msg, T data) {
        return new R<>(200, msg, data);
    }

    public static <T> R<T> fail(String msg) {
        return new R<>(500, msg, null);
    }

    public static <T> R<T> fail(int code, String msg) {
        return new R<>(code, msg, null);
    }
}
