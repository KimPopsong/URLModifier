# URLModifier 프로젝트 문서

## 프로젝트 개요

URLModifier는 URL 단축 서비스로, 긴 URL을 짧은 URL로 변환하고 QR 코드를 생성하며, 클릭 통계를 제공하는 풀스택 웹 애플리케이션입니다.

## 기술 스택

### 백엔드
- **언어**: Java 17
- **프레임워크**: Spring Boot 3.5.3
- **ORM**: Spring Data JPA
- **데이터베이스**: PostgreSQL 14
- **캐시**: Redis 7
- **메시지 큐**: RabbitMQ (Spring AMQP)
- **인증**: JWT (Access Token + Refresh Token)
- **API 문서**: Swagger (SpringDoc OpenAPI)
- **빌드 도구**: Gradle

### 프론트엔드
- **프레임워크**: Vue 3
- **빌드 도구**: Vite 7
- **UI 라이브러리**: Bootstrap 5.3, Bootstrap Vue 3
- **HTTP 클라이언트**: Axios 1.10
- **차트**: Chart.js 4.4 (통계 시각화)
- **스타일링**: CSS (Scoped Styles)

### 인프라
- **컨테이너화**: Docker, Docker Compose
- **이미지**: eclipse-temurin:17-jdk-slim

## 프로젝트 구조

```
URLModifier/
├── URLModifierBackend/          # Spring Boot 백엔드
│   ├── src/main/java/
│   │   └── bigmac/urlmodifierbackend/
│   │       ├── domain/          # 도메인별 패키지
│   │       │   ├── url/         # URL 도메인
│   │       │   │   ├── controller/   # URLController
│   │       │   │   ├── dto/          # Request/Response DTOs
│   │       │   │   ├── exception/    # URLException
│   │       │   │   ├── model/        # URL, ClickEvent 엔티티
│   │       │   │   ├── repository/   # URLRepository, ClickEventRepository
│   │       │   │   └── service/      # URLService, URLValidateService
│   │       │   └── user/         # 사용자 도메인
│   │       │       ├── controller/   # AuthController, MyPageController
│   │       │       ├── dto/          # Request/Response DTOs
│   │       │       ├── exception/    # 커스텀 예외들
│   │       │       ├── model/        # User 엔티티
│   │       │       ├── repository/   # UserRepository
│   │       │       └── service/      # AuthService, MyPageService
│   │       ├── global/          # 글로벌 설정 및 유틸리티
│   │       │   ├── config/      # 설정 클래스들
│   │       │   ├── dto/         # ErrorResponse
│   │       │   ├── handler/     # GlobalExceptionHandler
│   │       │   ├── sql/         # SQL 스키마 파일들
│   │       │   └── util/        # 유틸리티 클래스들
│   │       └── UrlModifierBackendApplication.java
│   ├── src/main/resources/
│   │   └── application.yaml     # 애플리케이션 설정
│   └── build.gradle             # 의존성 관리
│
├── URLModifierFrontend/         # Vue.js 프론트엔드
│   ├── src/
│   │   ├── App.vue              # 메인 컴포넌트 (단일 페이지 앱)
│   │   ├── assets/              # 정적 자산
│   │   └── main.js              # 진입점
│   ├── package.json             # NPM 의존성
│   └── vite.config.js           # Vite 설정
│
├── docker-compose.yaml          # Docker Compose 설정
├── Dockerfile                   # 백엔드 이미지 빌드
└── README.md                    # 프로젝트 소개
```

## 데이터베이스 스키마

### users 테이블
- `id` (BIGINT, PK): 사용자 고유 ID (Snowflake 알고리즘)
- `email` (VARCHAR, UNIQUE): 이메일 주소
- `nick_name` (VARCHAR): 닉네임
- `password` (VARCHAR): 암호화된 비밀번호

### url 테이블
- `id` (BIGINT, PK): URL 고유 ID (Snowflake 알고리즘)
- `users` (BIGINT, FK): 사용자 ID (nullable, 비회원도 가능)
- `origin_url` (TEXT): 원본 URL
- `shortened_url` (VARCHAR(30), UNIQUE): 단축된 URL (Base62 인코딩)
- `qr_code` (TEXT): QR 코드 이미지 (Base64 인코딩)
- `created_at` (TIMESTAMP): 생성 일시

**인덱스**:
- `idx_url_origin_url`: origin_url
- `idx_url_user_origin`: users, origin_url
- `idx_url_user`: users
- `idx_url_origin_user_null`: origin_url

### click_event 테이블
- `id` (BIGINT, PK): 이벤트 고유 ID
- `url_id` (BIGINT, FK): URL ID
- `referrer` (TEXT): 리퍼러 URL (nullable)
- `user_agent` (TEXT): User-Agent 문자열 (nullable)
- `ip_address` (VARCHAR): IP 주소 (nullable)
- `clicked_at` (TIMESTAMP): 클릭 일시

## API 엔드포인트

### 인증 관련 (`/auth`)

#### POST `/auth/register`
회원가입
- **Request Body**:
  ```json
  {
    "email": "user@example.com",
    "nickName": "사용자명",
    "password": "password123"
  }
  ```
- **Response**: `201 CREATED`

#### POST `/auth/login`
로그인
- **Request Body**:
  ```json
  {
    "email": "user@example.com",
    "password": "password123"
  }
  ```
- **Response**:
  ```json
  {
    "userId": 123456789,
    "email": "user@example.com",
    "jwtResponse": {
      "accessToken": "eyJhbGc...",
      "refreshToken": "eyJhbGc..."
    }
  }
  ```

#### POST `/auth/refresh`
JWT 토큰 갱신
- **Request**: HttpOnly 쿠키에서 refreshToken 추출
- **Response**: 새로운 accessToken, refreshToken

#### POST `/auth/logout`
로그아웃
- **Headers**: `Authorization: Bearer {accessToken}`
- **Response**: `200 OK`

### 사용자 관련

#### GET `/me`
마이페이지 정보 조회
- **인증**: Bearer Token 필요
- **Response**:
  ```json
  {
    "nickname": "사용자명",
    "email": "user@example.com",
    "urls": [
      {
        "id": "123456789",
        "originUrl": "https://example.com/long/url",
        "shortenedUrl": "http://localhost:8080/abc123",
        "createdAt": "2024-01-01T00:00:00"
      }
    ]
  }
  ```

### URL 관련

#### POST `/short-urls`
단축 URL 생성 (비회원 가능)
- **Request Body**:
  ```json
  {
    "url": "https://example.com/very/long/url"
  }
  ```
- **Headers**: `Authorization: Bearer {accessToken}` (선택사항, 로그인 시에만 전송)
- **Response**: `201 CREATED`
  ```json
  {
    "id": "123456789",
    "originUrl": "https://example.com/very/long/url",
    "shortenedUrl": "http://localhost:8080/abc123",
    "qrCode": "iVBORw0KGgoAAAANSUhEUg..."
  }
  ```

#### POST `/short-urls/custom`
커스텀 단축 URL 생성 (회원 전용)
- **인증**: Bearer Token 필요
- **Request Body**:
  ```json
  {
    "originURL": "https://example.com/very/long/url",
    "customURL": "my-custom-link"
  }
  ```
- **Response**: `201 CREATED` (POST `/short-urls`와 동일한 형식)
- **에러**: 커스텀 URL이 이미 존재하거나 유효하지 않은 경우 `400 Bad Request`

#### GET `/short-urls/{shortUrl}`
단축 URL 유효성 확인
- **Response**:
  ```json
  {
    "isValid": true,
    "originURL": "https://example.com/very/long/url"
  }
  ```
  또는
  ```json
  {
    "isValid": false,
    "originURL": null
  }
  ```

#### GET `/{shortenedUrl}`
원본 URL로 리디렉션
- **동작**: 
  - 클릭 이벤트 기록 (referrer, userAgent, ipAddress 수집)
  - 원본 URL로 302 리디렉션
- **Response**: `302 FOUND`, `Location: {originURL}`

#### GET `/urls/{urlId}`
URL 상세 정보 및 통계 (회원 전용)
- **인증**: Bearer Token 필요
- **Response**:
  ```json
  {
    "id": "123456789",
    "originURL": "https://example.com/very/long/url",
    "shortenedURL": "abc123",
    "qrCode": "iVBORw0KGgo...",
    "createdAt": "2024-01-01T00:00:00",
    "clickEventList": [
      {
        "id": 987654321,
        "referrer": "https://google.com",
        "userAgent": "Mozilla/5.0...",
        "ipAddress": "192.168.1.1",
        "clickedAt": "2024-01-01T12:00:00"
      }
    ]
  }
  ```

#### DELETE `/urls/{urlId}`
단축 URL 삭제 (회원 전용)
- **인증**: Bearer Token 필요
- **Response**: `202 ACCEPTED`

## 핵심 기능

### 1. URL 단축
- Snowflake 알고리즘으로 고유 ID 생성
- Base62 인코딩으로 단축 URL 생성
- 비회원도 사용 가능 (users 컬럼이 null)
- 회원이 생성한 URL은 자동으로 마이페이지에 연결

### 2. 커스텀 URL
- 회원만 사용 가능
- 사용자가 원하는 별칭으로 단축 URL 생성
- 중복 체크 및 유효성 검사 수행

### 3. QR 코드 생성
- ZXing 라이브러리 사용
- Base64 인코딩된 PNG 이미지로 저장 및 전송

### 4. 클릭 이벤트 추적
- 리디렉션 시 자동으로 클릭 이벤트 기록
- Referrer, User-Agent, IP 주소 수집
- RabbitMQ를 통한 비동기 처리 (추정)

### 5. 인증 및 인가
- JWT 기반 인증
- Access Token + Refresh Token 방식
- Spring Security로 보호된 엔드포인트
- JwtAuthenticationFilter로 요청별 인증 처리

### 6. 마이페이지
- 사용자가 생성한 모든 URL 목록
- 각 URL별 상세 정보 및 통계
- Chart.js를 통한 클릭 통계 시각화 (시간대별)
- URL 삭제 기능

## 설정 파일

### application.yaml
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/urlModifierDB
    username: ${DB_USERNAME:test}
    password: ${DB_PASSWORD:test}
  
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: 6379
      password: ${REDIS_PASSWORD:}

jwt:
  secret: ${JWT_SECRET:...}

custom:
  BE_BASE_URL: ${BE_URL:http://localhost:8080/}
  FE_BASE_URL: ${FE_URL:http://localhost:5173/}
```

### docker-compose.yaml
서비스:
- `app`: Spring Boot 애플리케이션 (포트 8080)
- `db`: PostgreSQL 14 (포트 5432)
- `redis`: Redis 7 (포트 6379)

## 주요 유틸리티 클래스

### 백엔드
- `SnowflakeIdGenerator`: 분산 환경에서 고유 ID 생성
- `Base62`: 숫자를 Base62 문자열로 인코딩/디코딩
- `QRCodeUtil`: QR 코드 이미지 생성
- `JwtUtil`: JWT 토큰 생성 및 검증
- `JwtAuthenticationFilter`: JWT 인증 필터

### 프론트엔드
- 단일 컴포넌트 앱 (`App.vue`): 모든 기능이 하나의 Vue 컴포넌트에 구현
- Axios를 통한 API 통신
- LocalStorage를 통한 토큰 관리
- Chart.js를 통한 통계 시각화

## 실행 방법

### Docker Compose로 실행
```bash
docker-compose up -d
```

### 백엔드 직접 실행
```bash
cd URLModifierBackend
./gradlew bootRun
```

### 프론트엔드 직접 실행
```bash
cd URLModifierFrontend
npm install
npm run dev
```

## 환경 변수

- `DB_URL`: PostgreSQL 연결 URL
- `DB_USERNAME`: PostgreSQL 사용자명
- `DB_PASSWORD`: PostgreSQL 비밀번호
- `REDIS_HOST`: Redis 호스트
- `REDIS_PASSWORD`: Redis 비밀번호
- `JWT_SECRET`: JWT 서명에 사용할 시크릿 키
- `BE_URL`: 백엔드 기본 URL
- `FE_URL`: 프론트엔드 기본 URL

## 보안 고려사항

- JWT 토큰 기반 인증
- 비밀번호 암호화 (BCrypt 추정)
- CORS 설정 (WebConfig)
- Spring Security를 통한 엔드포인트 보호
- 에러 메시지에서 민감한 정보 노출 방지 (GlobalExceptionHandler)

## 아키텍처 패턴

- **레이어드 아키텍처**: Controller → Service → Repository
- **도메인 주도 설계 (DDD)**: 도메인별로 패키지 분리 (url, user)
- **DTO 패턴**: Request/Response 객체로 데이터 전송
- **예외 처리**: 전역 예외 핸들러 사용

## 향후 개선 사항 (TODO)

- 회원 탈퇴 기능
- SMTP 설정 (이메일 인증 등)
