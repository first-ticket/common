package com.firstticket.common.web;

import com.firstticket.common.exception.BusinessException;
import com.firstticket.common.response.CommonErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

/**
 * Gateway 주입 헤더에서 인증 사용자 정보를 꺼내는 정적 유틸리티
 *
 * 설계 결정 사항
 * - SecurityContextHolder 대신 RequestContextHolder를 사용하는 이유:
 *   본 프로젝트는 Spring Security를 사용하지 않음
 *   Gateway가 JWT를 검증하고 X-User-Id, X-User-Role 헤더로 사용자 정보를 주입하는 형태
 *   따라서 인증 정보의 출처는 SecurityContext가 아닌 현재 HTTP 요청 헤더
 *
 * - 정적 메서드를 사용하는 이유:
 *   컨트롤러 파라미터 선언 없이 필요한 시점에 어디서든 호출 가능
 *
 * - 주의 사항:
 *   HTTP 요청 컨텍스트(RequestScope) 안에서만 동작
 *   비동기 스레드(@Async), Kafka 컨슈머, 스케줄러에서는 사용 불가
 *   단위 테스트 시 RequestContextHolder를 직접 세팅해야 합니다.
 */
public final class AuthContext {

    private static final String HEADER_USER_ID   = "X-User-Id";
    private static final String HEADER_USER_ROLE = "X-User-Role";

    // 유틸리티 클래스 - 인스턴스 생성 금지
    private AuthContext() {}

    /**
     * 현재 요청의 인증 사용자 UUID를 반환
     *
     * @throws BusinessException UNAUTHORIZED(401) - 헤더 누락 또는 UUID 형식 오류
     */
    public static UUID getUserId() {
        String value = getRequiredHeader(HEADER_USER_ID);
        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException e) {
            // Gateway 오동작 또는 Gateway 우회 요청
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
        }
    }

    /**
     * 현재 요청의 인증 사용자 역할을 UserRole enum으로 반환
     *
     * 설계 결정 사항
     * - 반환 타입을 String이 아닌 UserRole로 선택한 이유:
     *   1. 각 서비스에서 UserRole.valueOf() 변환 코드를 반복 작성할 필요 없음
     *   2. 정의되지 않은 role 값에 대한 예외 처리를 이 한 곳에서 통일
     *   3. 호출 측에서 오타가 컴파일 오류로 즉시 감지됨 (타입 안전성)
     *
     * @throws BusinessException UNAUTHORIZED(401) — 헤더 누락 또는 정의되지 않은 역할 값
     */
    public static UserRole getRole() {
        String roleStr = getRequiredHeader(HEADER_USER_ROLE);
        try {
            // "ADMIN" → UserRole.ADMIN 변환
            // Gateway 계약 위반(정의되지 않은 role)은 UNAUTHORIZED로 통일 처리
            return UserRole.valueOf(roleStr);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
        }
    }
    /**
     * 필수 헤더 추출 공통 메서드
     * 누락 또는 빈 값이면 UNAUTHORIZED 예외 발생
     */
    private static String getRequiredHeader(String headerName) {
        String value = getCurrentRequest().getHeader(headerName);
        if (value == null || value.isBlank()) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
        }
        return value.trim();
    }

    /**
     * RequestContextHolder에서 현재 HTTP 요청 객체 추출
     * HTTP 요청 컨텍스트 밖에서 호출 시 UNAUTHORIZED 예외 발생
     */
    private static HttpServletRequest getCurrentRequest() {
        if (!(RequestContextHolder.getRequestAttributes()
            instanceof ServletRequestAttributes attributes)) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
        }
        return attributes.getRequest();
    }

    /**
     * 현재 요청의 사용자 역할이 required와 일치하는지 검증
     *
     * 설계 결정 사항
     * - 여러 컨트롤러에서 동일한 역할 검증 로직이 반복되는 것을 방지합니다.
     * - ADMIN 전용 컨트롤러뿐 아니라 HOST 전용, CUSTOMER 전용 엔드포인트에도 재사용 가능합니다.
     *
     * @param required 허용할 역할
     * @throws BusinessException FORBIDDEN(403) - 역할 불일치 시
     */
    public static void requireRole(UserRole required) {
        // getRole()이 내부적으로 헤더 누락 시 401을 던지므로 별도 null 체크 불필요
        if (getRole() != required) {
            throw new BusinessException(CommonErrorCode.FORBIDDEN);
        }
    }
}
