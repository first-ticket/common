package com.firstticket.common.response;

import org.springframework.http.HttpStatus;

public interface SuccessCode {
    HttpStatus getStatus();
    String getMessage();

    default String getName() {
        if (this instanceof Enum<?> enumCode) {
            return enumCode.name();
        }
        return null;
    }
}
