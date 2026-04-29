# com.first-ticket.common

공통 예외 처리, 응답 형식, Feign/Web/JSON 설정을 제공하는 공통 모듈입니다.

---

## 🚀 배포 방법

> ⚠️ 배포 전 `.env` 파일에 GitHub 인증 정보가 설정되어 있어야 합니다.
> `.env.example`을 복사하여 `.env` 파일을 생성하고 값을 채워주세요.

```bash
cp .env.example .env
```

```
# .env
GITHUB_USER=깃허브유저명
GITHUB_TOKEN=ghp_xxxxxxxxxxxx
```

> `GITHUB_TOKEN`은 GitHub → Settings → Developer settings → Personal access tokens에서 `write:packages` 권한으로 발급합니다.

**1. `build.gradle`에서 버전 수정**

```groovy
// 기능 추가 시 버전 올리기
version = '0.0.1-SNAPSHOT'
version = '0.0.2-SNAPSHOT'
```

**2. 배포**

```bash
./gradlew publish
```

---

## 📝 버전

| 버전               | 변경 내용                                                                                                                                                                                                                                      |
|------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `0.0.1-SNAPSHOT` | • 공통 응답 구조 추가 (`ApiResponse`, `ErrorCode`, `SuccessCode`)<br>• 공통 예외 처리 추가 (`BusinessException`, `GlobalExceptionHandler`)<br>• JPA 기본 엔티티 및 설정 추가 (`BaseEntity`, `BaseUserEntity`, `JpaConfig`)<br>• 자동 설정 등록 (`CommonAutoConfiguration`) |
| `0.0.2-SNAPSHOT` | • Feign 설정 추가 (`FeignConfig`, `FeignErrorDecoder`)<br>• PageableResolver 추가 (`CustomPageableResolver`, `WebConfig`)<br>• `JsonUtil` 추가 (JSON 직렬화/역직렬화 정적 유틸)<br>• `ObjectMapper` 빈 등록 (`JavaTimeModule`, `FAIL_ON_UNKNOWN_PROPERTIES` 설정)  |
| `0.0.3-SNAPSHOT` | • JPA 관련 코드 제거 (`BaseEntity`, `BaseUserEntity`, `JpaConfig`) → `common-jpa`로 분리                                                                                                                                                            |
| `0.0.4-SNAPSHOT` | • `AuthContext` 추가 (Gateway 주입 헤더에서 인증 사용자 정보를 정적 메서드로 추출)<br>• `UserRole` enum 추가 (공통 역할 타입 — role별 분기 처리 시 사용)<br>• `AuthContext.getRole()` 반환 타입 `String → UserRole` 변경                                                                 |

---

## 📦 의존성 추가

> GitHub Packages는 public 패키지도 인증이 필요합니다.
> 팀원 각자 본인 GitHub 계정으로 토큰을 발급하여 `.env`에 넣어주세요.
> 토큰 발급 — GitHub → Settings → Developer settings → Personal access tokens → `read:packages` 권한

```groovy
def env = [:]
def envFile = rootProject.file(".env")
if (envFile.exists()) {
    envFile.eachLine { line ->
        if (line && !line.startsWith("#") && line.contains("=")) {
            def (key, value) = line.split("=", 2)
            env[key.trim()] = value.trim()
        }
    }
}

repositories {
    mavenCentral()
    maven {
        url = 'https://maven.pkg.github.com/first-ticket/common'
        credentials {
            username = env['GITHUB_USER']
            password = env['GITHUB_TOKEN']
        }
    }
}

dependencies {
    implementation 'com.first-ticket:common:0.0.3-SNAPSHOT'
}
```

> 서비스 루트에 `.env` 파일이 있어야 합니다.

```
# .env
GITHUB_USER=깃허브유저명
GITHUB_TOKEN=ghp_xxxxxxxxxxxx
```

---

## 🗂️ 패키지 구조

```
com.firstticket.common
├── exception
│   ├── BusinessException.java       ← 베이스 예외
│   └── GlobalExceptionHandler.java  ← 전역 예외 처리
├── response
│   ├── ApiResponse.java             ← 공통 응답 래퍼
│   ├── ErrorCode.java               ← 에러 코드 interface
│   ├── SuccessCode.java             ← 성공 코드 interface
│   ├── CommonErrorCode.java         ← 공통 에러 코드
│   └── CommonSuccessCode.java       ← 공통 성공 코드
├── feign
│   ├── FeignConfig.java             ← Feign 설정, 헤더 전파
│   └── FeignErrorDecoder.java       ← Feign 에러 처리
├── web
│   ├── WebConfig.java                ← CustomPageableResolver 등록
│   ├── CustomPageableResolver.java   ← 페이지 크기 강제
│   ├── AuthContext.java              ← Gateway 주입 헤더 → 정적 메서드로 추출
│   └── UserRole.java                 ← 공통 사용자 역할 enum (CUSTOMER / HOST / ADMIN)
└── json
    ├── JsonConfig.java              ← ObjectMapper 빈 등록
    └── JsonUtil.java                ← JSON 직렬화/역직렬화 정적 유틸
```

---

## 🚀 사용 방법

### 1. 서비스 전용 예외 클래스 생성

`BusinessException`을 상속받아 서비스 전용 예외 클래스를 생성합니다.

```java
// domain/exception/SampleException.java
public class SampleException extends BusinessException {

    public SampleException(SampleErrorCode errorCode) {
        super(errorCode);
    }
}
```

### 2. 서비스 전용 에러 코드 생성

`ErrorCode` interface를 구현한 enum을 생성합니다.

```java
// domain/exception/SampleErrorCode.java
@Getter
@RequiredArgsConstructor
public enum SampleErrorCode implements ErrorCode {

    SAMPLE_NOT_FOUND(HttpStatus.NOT_FOUND, "샘플을 찾을 수 없습니다"),
    ALREADY_CANCELLED(HttpStatus.BAD_REQUEST, "이미 취소된 샘플입니다"),
    INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "유효하지 않은 금액입니다");

    private final HttpStatus status;
    private final String message;
}
```

### 3. 서비스 전용 성공 코드 생성

`SuccessCode` interface를 구현한 enum을 생성합니다.

```java
// presentation/SampleSuccessCode.java
@Getter
@RequiredArgsConstructor
public enum SampleSuccessCode implements SuccessCode {

    SAMPLE_CREATED(HttpStatus.CREATED, "샘플이 생성되었습니다"),
    SAMPLE_UPDATED(HttpStatus.OK, "샘플이 수정되었습니다"),
    SAMPLE_DELETED(HttpStatus.OK, "샘플이 삭제되었습니다"),
    SAMPLE_FOUND(HttpStatus.OK, "샘플을 조회했습니다");

    private final HttpStatus status;
    private final String message;
}
```

### 4. 예외 발생

```java
throw new SampleException(SampleErrorCode.ALREADY_CANCELLED);
```

### 5. 컨트롤러에서 응답 반환

```java
// presentation/SampleController.java
@RestController
@RequiredArgsConstructor
@RequestMapping("/samples")
public class SampleController {

    private final SampleService sampleService;

    @PostMapping
    public ResponseEntity<ApiResponse<SampleResponse>> create(
            @RequestBody SampleCreateRequest request) {
        SampleResult result = sampleService.create(request.toCommand());
        return ApiResponse.success(SampleSuccessCode.SAMPLE_CREATED,
                SampleResponse.from(result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        sampleService.delete(id);
        return ApiResponse.success(SampleSuccessCode.SAMPLE_DELETED);
    }
}
```

---

## 📋 응답 형식

### ✅ 성공 응답

```json
{
    "success": true,
    "code": "SAMPLE_CREATED",
    "message": "샘플이 생성되었습니다",
    "timestamp": "2024-01-01T00:00:00",
    "data": {
        "id": 1,
        "name": "샘플"
    }
}
```

### ❌ 에러 응답

```json
{
    "success": false,
    "code": "ALREADY_CANCELLED",
    "message": "이미 취소된 샘플입니다",
    "timestamp": "2024-01-01T00:00:00"
}
```

---

## 🔗 Feign

서비스 간 HTTP 통신을 위한 Feign 설정이 자동으로 적용됩니다.

### FeignClient 사용

```java
// infrastructure/client/UserClient.java
@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/users/{id}")
    UserResponse getUser(@PathVariable UUID id);
}
```

### 헤더 자동 전파

모든 Feign 요청에 아래 헤더가 자동으로 전파됩니다.

| 헤더 | 설명 |
|------|------|
| `X-User-Id` | 현재 요청한 유저 ID |
| `X-User-Role` | 현재 요청한 유저 권한 |

### Feign 에러 코드

Feign 호출 실패 시 아래 에러 코드로 변환됩니다.

| 코드 | HTTP 상태 | 메시지 |
|------|-----------|--------|
| `FEIGN_BAD_REQUEST` | 400 | 외부 서비스 요청이 올바르지 않습니다 |
| `FEIGN_UNAUTHORIZED` | 401 | 외부 서비스 인증에 실패했습니다 |
| `FEIGN_FORBIDDEN` | 403 | 외부 서비스 접근 권한이 없습니다 |
| `FEIGN_NOT_FOUND` | 404 | 외부 서비스 리소스를 찾을 수 없습니다 |
| `FEIGN_SERVER_ERROR` | 500 | 외부 서비스 오류가 발생했습니다 |

---

## 📄 CustomPageableResolver

허용된 페이지 크기 외 요청 시 기본값으로 강제 적용됩니다.

| 항목 | 값 |
|------|-----|
| 허용 페이지 크기 | 10, 30, 50 |
| 기본 페이지 크기 | 10 |

> ⚠️ `CustomPageableResolver`가 적용되려면 컨트롤러 파라미터에 `org.springframework.data.domain.Pageable`을 사용해야 합니다.

```java
import org.springframework.data.domain.Pageable;

@GetMapping
public ResponseEntity<ApiResponse<SampleListResponse>> getList(Pageable pageable) {
    ...
}
```

### 요청 방법

```
GET /samples?page=0&size=30&sort=createdAt
```

허용되지 않은 사이즈 요청 시 기본값 `10`으로 처리됩니다.

```
GET /samples?size=100  →  size=10 으로 강제 적용
```

---

## 🔧 JsonUtil

JSON 직렬화/역직렬화를 정적 메서드로 제공합니다.  
`ObjectMapper` 빈이 자동으로 주입되므로 별도 설정 없이 바로 사용할 수 있습니다.

```java
// 직렬화
String json = JsonUtil.toJson(sampleDto);

// 역직렬화
SampleDto dto = JsonUtil.fromJson(json, SampleDto.class);

// 제네릭 타입 역직렬화
List<SampleDto> list = JsonUtil.fromJson(json, new TypeReference<List<SampleDto>>() {});
```

직렬화/역직렬화 실패 시 예외를 던지지 않고 `null`을 반환합니다.  
호출하는 쪽에서 null 체크가 필요합니다.

### ObjectMapper 설정

| 설정 | 값 | 설명                                                     |
|------|-----|--------------------------------------------------------|
| `JavaTimeModule` | 등록 | `LocalDateTime`, `Instant` 등 Java 8 날짜/시간 타입 지원        |
| `WRITE_DATES_AS_TIMESTAMPS` | false | 날짜를 ISO-8601 문자열로 직렬화                                  |
| `FAIL_ON_UNKNOWN_PROPERTIES` | false | 알 수 없는 필드 무시 (Feign, Kafka 통신 시 필요한 필드만 선택적으로 받을 수 있음) |


## 🔐 AuthContext

Gateway가 주입한 `X-User-Id`, `X-User-Role` 헤더를 정적 메서드로 추출합니다.  
컨트롤러 파라미터 선언 없이 필요한 시점에 어디서든 호출 가능합니다.

### 사용 방법

```java
UUID     userId = AuthContext.getUserId(); // X-User-Id  → UUID
UserRole role   = AuthContext.getRole();   // X-User-Role → UserRole
```

### 동작 규칙

| 헤더 | 필수 여부 | 누락 시 |
|------|-----------|---------|
| `X-User-Id`   | 필수 | `UNAUTHORIZED (401)` |
| `X-User-Role` | 필수 | `UNAUTHORIZED (401)` |

### 주의 사항

- HTTP 요청 컨텍스트 안에서만 동작합니다.
- `@Async`, Kafka 컨슈머, 스케줄러 등 요청 범위 밖에서는 사용할 수 없습니다.
- `X-User-Role` 헤더값이 `UserRole`에 정의되지 않은 값이면 `UNAUTHORIZED (401)` 발생합니다.

---

## 👤 UserRole

Gateway가 전파하는 `X-User-Role` 헤더값에 대응하는 공통 역할 enum입니다.  
동일 API에서 role별 동작이 달라지는 경우 타입 안전하게 분기 처리할 수 있습니다.

### 역할 목록

| 역할 | 설명               |
|------|------------------|
| `CUSTOMER` | 일반 사용자           |
| `HOST` | 주최자 |
| `ADMIN` | 관리자              |

### 사용 예시

```java
UserRole role = AuthContext.getRole();

switch (role) {
    case ADMIN    -> // 관리자 전용 로직
    case HOST     -> // 호스트 전용 로직
    case CUSTOMER -> // 일반 사용자 로직
}
```

---

## 🔴 공통 에러 코드

| 코드 | HTTP 상태 | 메시지 |
|------|-----------|--------|
| `INVALID_INPUT` | 400 | 입력값이 올바르지 않습니다 |
| `UNAUTHORIZED` | 401 | 인증에 실패했습니다 |
| `FORBIDDEN` | 403 | 접근 권한이 없습니다 |
| `NOT_FOUND` | 404 | 요청한 리소스를 찾을 수 없습니다 |
| `METHOD_NOT_ALLOWED` | 405 | 허용되지 않는 HTTP 메서드입니다 |
| `CONFLICT` | 409 | 이미 존재하는 리소스입니다 |
| `INTERNAL_SERVER_ERROR` | 500 | 서버 내부 오류가 발생했습니다 |

---

## 🟢 공통 성공 코드

| 코드 | HTTP 상태 | 메시지 |
|------|-----------|--------|
| `OK` | 200 | 요청이 성공했습니다 |
| `CREATED` | 201 | 리소스가 생성되었습니다 |
| `DELETED` | 200 | 리소스가 삭제되었습니다 |
