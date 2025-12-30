package com.agrimatch.common.exception;

import com.agrimatch.common.api.Result;
import com.agrimatch.common.api.ResultCode;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public Result<Void> handleApiException(ApiException e) {
        log.warn("ApiException: code={}, message={}", e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public Result<Void> handleNotFound(Exception e) {
        return Result.fail(ResultCode.NOT_FOUND.getCode(), ResultCode.NOT_FOUND.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class, ConstraintViolationException.class})
    public Result<Void> handleValidationException(Exception e) {
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), ResultCode.PARAM_ERROR.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("Unhandled exception", e);
        String msg = ResultCode.SERVER_ERROR.getMessage();
        if (e != null) {
            String detail = e.getMessage();
            String type = e.getClass().getSimpleName();
            if (detail != null && !detail.isBlank()) {
                msg = msg + ": " + type + ": " + detail;
            } else {
                msg = msg + ": " + type;
            }
        }
        return Result.fail(ResultCode.SERVER_ERROR.getCode(), msg);
    }
}


