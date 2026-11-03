package com.store.service;

/**
 * 业务校验异常：用于向前端展示友好的提示信息。
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BusinessException(String message) {
        super(message);
    }
}
