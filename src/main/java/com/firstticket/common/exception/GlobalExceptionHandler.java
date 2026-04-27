package com.firstticket.common.exception;

import com.firstticket.common.response.ApiResponse;
import com.firstticket.common.response.CommonErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.warn("[{}] status: {}, message: {}",
                e.getClass().getSimpleName(),
                e.getErrorCode().getStatus(),
                e.getMessage()
        );
        return ApiResponse.error(e.getErrorCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse(CommonErrorCode.INVALID_INPUT.getMessage());
        log.warn("[MethodArgumentNotValidException] message: {}", message);
        return ApiResponse.error(CommonErrorCode.INVALID_INPUT);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("[{}] message: {}", e.getClass().getSimpleName(), e.getMessage());
        return ApiResponse.error(CommonErrorCode.INVALID_INPUT);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.warn("[{}] message: {}", e.getClass().getSimpleName(), e.getMessage());
        return ApiResponse.error(CommonErrorCode.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFound(NoResourceFoundException e) {
        log.warn("[{}] message: {}", e.getClass().getSimpleName(), e.getMessage());
        return ApiResponse.error(CommonErrorCode.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnknownException(Exception e) {
        log.error("[{}] message: {}", e.getClass().getSimpleName(), e.getMessage(), e);
        return ApiResponse.error(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }
}
