package com.firstticket.common.web;

import java.util.UUID;

/**
 * Gateway가 주입한 인증 사용자 정보를 담는 DTO
 *
 * 설계 결정 사항
 * - role을 String으로 보관하는 이유:
 *   common 모듈은 도메인을 모르는 순수 인프라 모듈이어야 함
 *   UserRole enum은 user-service의 도메인 개념이므로 common에 둘 수 없음
 *   각 서비스가 필요한 경우 UserRole.valueOf(userContext.role())로 직접 변환
 */
public record UserContext(
    UUID userId,
    String role
) {}
