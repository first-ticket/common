package com.firstticket.common;

import com.firstticket.common.exception.GlobalExceptionHandler;
import com.firstticket.common.feign.FeignConfig;
import com.firstticket.common.json.JsonConfig;
import com.firstticket.common.web.WebConfig;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({
        GlobalExceptionHandler.class,
        WebConfig.class,
        FeignConfig.class,
        JsonConfig.class
})
public class CommonAutoConfiguration {
}
