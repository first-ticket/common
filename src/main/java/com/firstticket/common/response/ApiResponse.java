package com.firstticket.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        Boolean success,
        String code,
        String message,
        LocalDateTime timestamp,
        T data
) {
    public ApiResponse(SuccessCode successCode, T data) {
        this(true, successCode.getName(), successCode.getMessage(), LocalDateTime.now(), data);
    }

    public ApiResponse(ErrorCode errorCode) {
        this(true, errorCode.getName(), errorCode.getMessage(), LocalDateTime.now(), null);
    }

    public static <T> ResponseEntity<ApiResponse<T>> success(SuccessCode successCode, T data) {
        return ResponseEntity.status(successCode.getStatus())
                .body(new ApiResponse<>(successCode, data));
    }

    public static <T> ResponseEntity<ApiResponse<Void>> success(SuccessCode successCode) {
        return ResponseEntity.status(successCode.getStatus())
                .body(new ApiResponse<>(successCode, null));
    }

    public static <T> ResponseEntity<ApiResponse<Void>> error(ErrorCode errorCode) {
        return ResponseEntity.status(errorCode.getStatus())
                .body(new ApiResponse<>(errorCode));
    }
}
