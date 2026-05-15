package com.firstticket.common.feign;

import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
@EnableFeignClients(basePackages = "com.firstticket")
public class FeignConfig {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_ROLE = "X-User-Role";
    private static final String HEADER_TRACE_ID = "X-Trace-Id";

    @Bean
    @ConditionalOnMissingBean(ErrorDecoder.class)
    public ErrorDecoder errorDecoder() {
        return new FeignErrorDecoder();
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
                HttpServletRequest request = attributes.getRequest();
                requestTemplate.header(HEADER_USER_ID, request.getHeader(HEADER_USER_ID));
                requestTemplate.header(HEADER_USER_ROLE, request.getHeader(HEADER_USER_ROLE));
                String traceId = request.getHeader(HEADER_TRACE_ID);
                if (traceId != null && !traceId.isBlank()) {
                    requestTemplate.header(HEADER_TRACE_ID, traceId);
                }
            }
        };
    }
}
