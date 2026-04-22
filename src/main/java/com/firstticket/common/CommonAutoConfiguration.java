package com.firstticket.common;

import com.firstticket.common.exception.GlobalExceptionHandler;
import com.firstticket.common.persistence.JpaConfig;
import com.firstticket.common.web.WebConfig;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({
        GlobalExceptionHandler.class,
        JpaConfig.class,
        WebConfig.class
})
public class CommonAutoConfiguration {
}
