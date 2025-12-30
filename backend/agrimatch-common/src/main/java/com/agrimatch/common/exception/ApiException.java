package com.agrimatch.common.exception;

import com.agrimatch.common.api.ResultCode;

public class ApiException extends RuntimeException {
    private final int code;

    public ApiException(ResultCode code) {
        super(code.getMessage());
        this.code = code.getCode();
    }

    public ApiException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}


