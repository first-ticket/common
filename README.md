# com.first-ticket.common

공통 예외 처리, 응답 형식, JPA 기본 엔티티를 제공하는 공통 모듈입니다.

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

| 버전 | 변경 내용 |
|------|-----------|
| `0.0.1-SNAPSHOT` | • 공통 응답 구조 추가 (`ApiResponse`, `ErrorCode`, `SuccessCode`)<br>• 공통 예외 처리 추가 (`BusinessException`, `GlobalExceptionHandler`)<br>• JPA 기본 엔티티 및 설정 추가 (`BaseEntity`, `BaseUserEntity`, `JpaConfig`)<br>• 자동 설정 등록 (`CommonAutoConfiguration`) |

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
    implementation 'com.first-ticket:common:0.0.1-SNAPSHOT'
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
└── jpa
    ├── JpaConfig.java               ← EntityScan, JPAQueryFactory 설정
    ├── BaseEntity.java              ← 생성/수정/삭제 시간
    └── BaseUserEntity.java          ← BaseEntity + 생성/수정/삭제 유저
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

## 🗄️ JpaConfig

`com.firstticket` 하위 패키지를 자동으로 스캔하여 JPA 엔티티와 Repository를 등록합니다.
`JPAQueryFactory` 빈을 자동으로 등록하여 QueryDSL을 바로 사용할 수 있습니다.

> ⚠️ 모든 서비스의 베이스 패키지가 `com.firstticket`으로 시작해야 합니다.

```java
// 서비스 엔티티 자동 스캔됨
package com.firstticket.sampleservice.domain;

@Entity
public class Sample extends BaseEntity { ... }
```

QueryDSL 사용 시 `JPAQueryFactory`를 주입받아 바로 사용합니다.

```java
@Repository
@RequiredArgsConstructor
public class SampleQueryRepositoryImpl implements SampleQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Sample> findBySpec(SampleSearchSpec spec) {
        return queryFactory
            .selectFrom(sample)
            .where(...)
            .fetch();
    }
}
```

---

## 🏗️ BaseEntity / BaseUserEntity

### BaseEntity

생성 시간, 수정 시간, 삭제 시간을 자동으로 관리합니다.

| 필드 | 타입 | 설명 |
|------|------|------|
| `createdAt` | `LocalDateTime` | 생성 시간 (자동) |
| `updatedAt` | `LocalDateTime` | 수정 시간 (자동, insert 시 null) |
| `deletedAt` | `LocalDateTime` | 삭제 시간 |

```java
@Entity
public class Sample extends BaseEntity {
    // createdAt, updatedAt, deletedAt 자동 포함
}
```

### BaseUserEntity

`BaseEntity`를 상속받아 생성/수정/삭제 유저 UUID를 추가로 관리합니다.

| 필드 | 타입 | 설명 |
|------|------|------|
| `createdBy` | `UUID` | 생성 유저 (자동) |
| `updatedBy` | `UUID` | 수정 유저 (자동, insert 시 null) |
| `deletedBy` | `UUID` | 삭제 유저 |

```java
@Entity
public class Sample extends BaseUserEntity {
    // createdAt, updatedAt, deletedAt, createdBy, updatedBy, deletedBy 자동 포함
}
```

소프트 삭제가 필요한 경우 `delete()` 메서드를 호출합니다.

```java
// 외부에서 바로 호출 — BaseUserEntity.delete() 동작
sample.delete(userId);  // deletedAt, deletedBy 자동 설정
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
