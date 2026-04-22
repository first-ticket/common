package com.firstticket.common;

import com.firstticket.common.exception.GlobalExceptionHandler;
import com.firstticket.common.persistence.JpaConfig;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({
        GlobalExceptionHandler.class,
        JpaConfig.class
})
public class CommonAutoConfiguration {
}
