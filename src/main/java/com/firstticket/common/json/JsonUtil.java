package com.firstticket.common.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class JsonUtil {

    private static ObjectMapper objectMapper;

    @Autowired
    public void init(ObjectMapper objectMapper) {
        JsonUtil.objectMapper = objectMapper;
    }

    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("[JSON] 직렬화 실패: {}", e.getMessage(), e);
            return null;
        }
    }

    public static <T> T fromJson(String json, Class<T> type) {
        if (json == null) {
            return null;
        }

        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            log.error("[JSON] 역직렬화 실패: {}", e.getMessage(), e);
            return null;
        }
    }

    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        if (json == null) {
            return null;
        }

        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            log.error("[JSON] 역직렬화 실패: {}", e.getMessage(), e);
            return null;
        }
    }
}
