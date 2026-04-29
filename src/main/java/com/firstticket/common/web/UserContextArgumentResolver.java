package com.firstticket.common.web;

import com.firstticket.common.exception.BusinessException;
import com.firstticket.common.response.CommonErrorCode;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.UUID;

/**
 * 컨트롤러 메서드의 UserContext 파라미터를 자동으로 주입하는 ArgumentResolver
 *
 * 사용 예시:
 *   @GetMapping("/me")
 *   public ResponseEntity<?> getMyInfo(UserContext userContext) { ... }
 */
public class UserContextArgumentResolver implements HandlerMethodArgumentResolver {

    // Gateway가 주입하는 헤더 키 - FeignConfig와 동일한 값으로 통일
    private static final String HEADER_USER_ID   = "X-User-Id";
    private static final String HEADER_USER_ROLE = "X-User-Role";

    /**
     * 해당 resolver가 처리할 파라미터 타입 선언
     * Controller 메서드에 UserContext 타입 파라미터가 있을 때만 동작
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(UserContext.class);
    }

    /**
     * 요청 헤더에서 사용자 정보를 추출하여 UserContext 객체로 변환
     *
     * @throws BusinessException UNAUTHORIZED(401) - X-User-Id 헤더 누락 시
     * @throws IllegalArgumentException - X-User-Id가 UUID 형식이 아닐 때 (Gateway 예외)
     */
    @Override
    public Object resolveArgument(MethodParameter parameter,
        ModelAndViewContainer mavContainer,
        NativeWebRequest webRequest,
        WebDataBinderFactory binderFactory) {

        // 1. X-User-Id 헤더 추출 - 필수값
        String userIdHeader = webRequest.getHeader(HEADER_USER_ID);
        if (userIdHeader == null || userIdHeader.isBlank()) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
        }

        // 2. String -> UUID 변환
        UUID userId = UUID.fromString(userIdHeader);

        // 3. X-User-Role 헤더 추출 - 선택값, nullable
        String role = webRequest.getHeader(HEADER_USER_ROLE);

        return new UserContext(userId, role);
    }
}
