# URLModifier 프로젝트 문서

> AI Agent 및 개발자가 프로젝트를 빠르게 파악하기 위한 문서입니다.
> 실제 소스 코드를 기준으로 작성되었습니다.

## 프로젝트 개요

URLModifier는 URL 단축 서비스로, 긴 URL을 짧은 URL로 변환하고 QR 코드를 생성하며, 클릭 통계를 제공하는 풀스택 웹 애플리케이션입니다.

---

## 기술 스택

### 백엔드
- **언어**: Java 17
- **프레임워크**: Spring Boot 3.5.3
- **ORM**: Spring Data JPA
- **데이터베이스**: PostgreSQL 14
- **캐시**: Redis 7
- **메시지 큐**: RabbitMQ (Spring AMQP) — 현재 docker-compose에 미포함, 로컬 실행 시 별도 설정 필요
- **인증**: JWT (jjwt 0.12.6) — Access Token(1시간) + Refresh Token(7일)
- **API 문서**: SpringDoc OpenAPI 2.8.9 (Swagger UI: `/swagger-ui.html`)
- **빌드 도구**: Gradle (`bootJar` → `app.jar`)

### 프론트엔드
- **프레임워크**: Vue 3
- **빌드 도구**: Vite 7
- **UI 라이브러리**: Bootstrap 5.3, Bootstrap Vue 3
- **HTTP 클라이언트**: Axios 1.10
- **차트**: Chart.js 4.4 (통계 시각화)
- **구조**: `App.vue` 단일 컴포넌트로 모든 UI 구현, LocalStorage로 토큰 관리

### 인프라
- **컨테이너화**: Docker, Docker Compose
- **이미지**: `eclipse-temurin:17-jdk-slim`
- **docker-compose 서비스**: `db`(PostgreSQL 14), `redis`(Redis 7)

---

## 프로젝트 구조

```
URLModifier/
├── docker-compose.yaml              # DB + Redis 컨테이너 정의
├── Dockerfile                       # 백엔드 이미지 빌드
├── PROJECT_DOCUMENTATION.md        # 이 파일
├── README.md                        # 빠른 시작 가이드
├── URLModifierBackend/
│   ├── build.gradle
│   └── src/main/java/bigmac/urlmodifierbackend/
│       ├── domain/
│       │   ├── url/
│       │   │   ├── controller/URLController.java
│       │   │   ├── dto/
│       │   │   │   ├── request/     # URLRequest, CustomURLRequest
│       │   │   │   └── response/    # URLResponse, URLDetailResponse, URLInfoResponse, ClickEventResponse
│       │   │   ├── exception/       # URLException, URLExpiredException
│       │   │   ├── model/           # URL.java, ClickEvent.java
│       │   │   ├── repository/      # URLRepository, ClickEventRepository
│       │   │   └── service/         # URLService(인터페이스), URLServiceImpl, URLValidateServiceImpl
│       │   └── user/
│       │       ├── controller/      # AuthController, MyPageController
│       │       ├── dto/
│       │       │   ├── request/     # UserLoginRequest, UserRegisterRequest, UserWithdrawRequest, RefreshTokenRequest
│       │       │   └── response/    # UserLoginResponse, JwtResponse, MyPageResponse
│       │       ├── exception/       # EmailAlreadyExistsException, LoginFailException, URLFindException, WithdrawFailException
│       │       ├── model/User.java
│       │       ├── repository/UserRepository.java
│       │       └── service/         # AuthService(인터페이스), AuthServiceImpl, MyPageService, MyPageServiceImpl
│       └── global/
│           ├── config/              # SecurityConfig, RedisConfig, RabbitConfig, IdGeneratorConfig, WebConfig
│           ├── dto/ErrorResponse.java
│           ├── handler/GlobalExceptionHandler.java
│           └── util/                # JwtUtil, JwtAuthenticationFilter, Base62, QRCodeUtil, SnowflakeIdGenerator
└── URLModifierFrontend/
    └── src/
        ├── App.vue                  # 단일 컴포넌트 (모든 UI)
        └── main.js
```

---

## 데이터베이스 스키마

### `users` 테이블
| 컬럼 | 타입 | 설명 |
|------|------|------|
| `id` | BIGINT (PK) | Snowflake 알고리즘 생성 |
| `nick_name` | VARCHAR | 닉네임 |
| `email` | VARCHAR (UNIQUE) | 이메일 |
| `password` | VARCHAR | BCrypt 암호화된 비밀번호 |

### `url` 테이블
| 컬럼 | 타입 | 설명 |
|------|------|------|
| `id` | BIGINT (PK) | Snowflake 알고리즘 생성 |
| `users` | BIGINT (FK, nullable) | 사용자 ID (비회원은 null) |
| `origin_url` | TEXT | 원본 URL |
| `shortened_url` | VARCHAR(30) (UNIQUE) | Base62 인코딩된 단축 코드 |
| `qr_code` | TEXT | Base64 PNG 이미지 |
| `created_at` | TIMESTAMP | 생성 일시 |
| `expires_at` | TIMESTAMP (nullable) | 만료 일시 (null이면 무제한) |
| `max_clicks` | INTEGER (nullable) | 최대 클릭 수 (null이면 무제한) |

**인덱스**: `idx_url_origin_url`, `idx_url_user_origin`(users, origin_url), `idx_url_user`(users), `idx_url_origin_user_null`(origin_url)

### `click_event` 테이블
| 컬럼 | 타입 | 설명 |
|------|------|------|
| `id` | BIGINT (PK) | 이벤트 ID |
| `url_id` | BIGINT (FK) | URL ID |
| `referrer` | TEXT (nullable) | 리퍼러 URL |
| `user_agent` | TEXT (nullable) | User-Agent 문자열 |
| `ip_address` | VARCHAR (nullable) | IP 주소 |
| `clicked_at` | TIMESTAMP | 클릭 일시 |

---

## API 엔드포인트

> **인증 방식**: `Authorization: Bearer {accessToken}` 헤더

### 인증 관련 (`/auth`)

#### POST `/auth/register` — 회원가입
```json
// Request
{ "email": "user@example.com", "nickName": "사용자명", "password": "password123" }
// Response: 201 CREATED
```

#### POST `/auth/login` — 로그인
```json
// Request
{ "email": "user@example.com", "password": "password123" }
// Response: 200 OK
{
  "userId": "123456789",
  "email": "user@example.com",
  "nickName": "사용자명",
  "jwtResponse": { "accessToken": "eyJ...", "refreshToken": "eyJ..." }
}
```

#### POST `/auth/refresh` — 토큰 재발급
- refreshToken을 body 또는 쿠키로 전달 → 새 accessToken + refreshToken 반환

#### POST `/auth/logout` — 로그아웃
- `Authorization: Bearer {accessToken}` 필수
- Access Token 블랙리스트 등록, Refresh Token 삭제

#### DELETE `/auth/withdraw` — 회원탈퇴
- `Authorization: Bearer {accessToken}` 필수
- 비밀번호 재확인 후 사용자의 모든 URL, ClickEvent, User 삭제
- Access Token 블랙리스트 등록, Refresh Token 삭제

### 사용자 관련

#### GET `/me` — 마이페이지
- **인증 필수**
```json
{
  "nickname": "사용자명",
  "email": "user@example.com",
  "urls": [
    { "id": "123456789", "originUrl": "...", "shortenedUrl": "http://localhost:8080/abc123", "createdAt": "..." }
  ]
}
```

### URL 관련

#### POST `/short-urls` — 단축 URL 생성 (비회원 가능)
```json
// Request
{
  "url": "https://example.com/very/long/url",
  "expiresAt": "2026-12-31T23:59:59",   // 선택, null이면 무제한
  "maxClicks": 100                         // 선택, null이면 무제한
}
// Response: 201 CREATED
{
  "id": "123456789",
  "originUrl": "https://example.com/very/long/url",
  "shortenedUrl": "http://localhost:8080/abc123",
  "qrCode": "iVBORw0KGgoAAAANSUhEUg...",
  "expiresAt": "2026-12-31T23:59:59",
  "maxClicks": 100,
  "clickCount": 0,
  "expired": false
}
```

#### POST `/short-urls/custom` — 커스텀 단축 URL 생성 (회원 전용)
```json
// Request
{
  "originURL": "https://example.com/very/long/url",
  "customURL": "my-custom-link",         // 1~30자
  "expiresAt": "2026-12-31T23:59:59",   // 선택
  "maxClicks": 100                        // 선택
}
// Response: 201 CREATED (위와 동일한 형식)
```

#### GET `/short-urls/{shortUrl}` — 유효성 확인
```json
{ "isValid": true, "originURL": "https://example.com/very/long/url" }
// 또는
{ "isValid": false, "originURL": null }
```

#### GET `/{shortenedUrl}` — 리디렉션
- 클릭 이벤트 기록 후 원본 URL로 **302 리디렉션**
- 만료(시간 초과 또는 클릭 수 초과) 시 프론트엔드로 `?expired={shortenedUrl}` 리디렉션

#### GET `/urls/{urlId}` — URL 상세 + 통계 (회원 전용)
```json
{
  "id": "123456789",
  "originURL": "https://...",
  "shortenedURL": "abc123",
  "qrCode": "iVBORw0KGgo...",
  "createdAt": "2024-01-01T00:00:00",
  "expiresAt": "2026-12-31T23:59:59",
  "maxClicks": 100,
  "clickEventList": [
    { "id": 987654321, "referrer": "...", "userAgent": "...", "ipAddress": "...", "clickedAt": "..." }
  ]
}
```

#### DELETE `/urls/{urlId}` — 단축 URL 삭제 (회원 전용)
- 해당 URL의 모든 ClickEvent 먼저 삭제 후 URL 삭제
- Response: `202 ACCEPTED`

---

## 핵심 비즈니스 로직

### URL 단축 흐름 (`URLServiceImpl.makeURLShort`)
1. 로그인 사용자: 동일 `originURL`이 이미 있으면 기존 URL 반환
2. 비회원: 익명(user=null) 중 동일 `originURL` 존재 시 기존 반환
3. `SnowflakeIdGenerator.nextId()` → `Base62.encode(id)` → 충돌 시 재시도(do-while)
4. `BE_BASE_URL + shortenedURL` 기준으로 QR 코드 생성 (ZXing, 200×200 PNG → Base64)
5. `expiresAt`, `maxClicks` 선택적으로 저장

### 링크 만료 확인 (`redirectToOriginal`)
```
expiresAt != null && now.isAfter(expiresAt) → URLExpiredException
maxClicks != null && clickCount >= maxClicks → URLExpiredException
→ FE_BASE_URL + "?expired=" + shortenedUrl 로 리디렉션
```

### JWT 인증 구조
- **Access Token**: 1시간, 로그아웃 시 Redis `blacklist:{token}` 키에 남은 유효시간 동안 저장
- **Refresh Token**: 7일, Redis `refresh:{email}` 키로 저장 (재발급 시 기존 삭제 후 새로 저장)
- **`JwtAuthenticationFilter`**: 매 요청마다 블랙리스트 확인 + 유효성 검사 → `SecurityContext` 설정
- **`SecurityConfig`**: `anyRequest().permitAll()` — 엔드포인트 보호는 서비스 레이어에서 수동 처리

---

## 환경 변수

| 변수 | 기본값 | 설명 |
|------|--------|------|
| `DB_URL` | `jdbc:postgresql://localhost:5432/urlModifierDB` | PostgreSQL JDBC URL |
| `DB_USERNAME` | `test` | DB 사용자명 |
| `DB_PASSWORD` | `test` | DB 비밀번호 |
| `REDIS_HOST` | `localhost` | Redis 호스트 |
| `REDIS_PASSWORD` | (없음) | Redis 비밀번호 |
| `JWT_SECRET` | (개발 기본값) | JWT 서명용 시크릿 키 (Base64 인코딩) |
| `BE_URL` | `http://localhost:8080/` | 백엔드 Base URL (단축 URL 접두어, QR 코드 기준) |
| `FE_URL` | `http://localhost:5173/` | 프론트엔드 Base URL (만료 리디렉션 대상) |

---

## 실행 방법

### 로컬 개발 (권장)
```bash
# 1. 인프라 (DB + Redis)
docker compose up -d db redis

# 2. 백엔드
cd URLModifierBackend
./gradlew bootRun
# → http://localhost:8080  |  Swagger: http://localhost:8080/swagger-ui.html

# 3. 프론트엔드
cd URLModifierFrontend
npm install
npm run dev
# → http://localhost:5173
```

### Docker 전체 실행 (백엔드 JAR 필요)
```bash
cd URLModifierBackend && ./gradlew bootJar
cd ..
docker compose up -d
```

---

## 개발 시 주의사항

- **ID 생성**: 모든 PK는 `SnowflakeIdGenerator`로 직접 생성. JPA `@GeneratedValue` 미사용.
- **트랜잭션**: 서비스 클래스 기본 `@Transactional(readOnly = true)`, 쓰기 메서드에만 `@Transactional` 추가.
- **보안 처리**: Spring Security는 전체 허용, 인증 강제는 서비스 레이어의 `checkUser()` 메서드로 수동 처리.
- **QR 코드**: `BE_BASE_URL + shortenedURL` 기준 생성 — 배포 환경 URL 변경 시 기존 QR 무효화 주의.
- **단축 URL 충돌**: Snowflake 특성상 사실상 충돌 없음. 안전망으로 `do-while` 재시도 로직 존재.
- **RabbitMQ**: `build.gradle`에 의존성은 있지만 `docker-compose.yaml`에 서비스 미포함. 로컬 실행 시 관련 설정 확인 필요.
- **프론트엔드**: 모든 UI가 `App.vue` 한 파일에 구현됨. 기능 분리 시 컴포넌트 분리 고려.

---

## 아키텍처 패턴

- **레이어드 아키텍처**: Controller → Service(인터페이스 + Impl) → Repository
- **도메인 주도 설계**: `url`, `user` 도메인으로 패키지 분리
- **DTO 패턴**: Request/Response 분리
- **전역 예외 처리**: `GlobalExceptionHandler`에서 커스텀 예외 → HTTP 응답 변환

---

## 미니 PC 서버 구축 현황

> OS: Windows, 서버 전용으로 사용

### 완료된 작업
- [x] WSL2 + Ubuntu 22.04 설치
- [x] Docker Engine 29.4.0 / Docker Compose 5.1.3 설치
- [x] WSL2 자동 시작 등록 (Windows 작업 스케줄러)
- [x] Windows 자동 업데이트 비활성화
- [x] 프로젝트 git clone (`~/URLModifier`)
- [x] Java 17 설치 (`openjdk-17-jdk`)
- [x] 백엔드 JAR 빌드 (`./gradlew bootJar`)
- [x] `docker compose up -d` 실행 성공 (app + db + redis)

### 서버 실행 방법 (Ubuntu 터미널)
```bash
cd ~/URLModifier

# 빌드 후 실행 (코드 변경 시)
cd URLModifierBackend && ./gradlew bootJar && cd ..
docker compose up -d --build

# 재시작만 할 경우
docker compose up -d

# 상태 확인
docker compose ps

# 로그 확인
docker compose logs -f app
```

### 진행 중인 작업
- [ ] 포트 포워딩 (공유기 설정) — 미니 PC 내부 IP 확인 후 진행
- [ ] `.env` 파일 작성 (운영용 JWT_SECRET, DB_PASSWORD 등으로 교체)
  - 필요 항목: `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `BE_URL`, `FE_URL`, `REDIS_PASSWORD`
  - `.env` 파일은 git에 커밋하지 않도록 `.gitignore` 확인 필요

---

## 향후 작업 (TODO)

### 1. 포트 포워딩
- 미니 PC 내부 IP 확인: `hostname -I`
- 공유기 관리 페이지에서 포트 포워딩 설정
  - 외부 `8080` → 미니 PC `8080` (백엔드 API)
  - 외부 `80`, `443` → 미니 PC `80` (Nginx, 나중에)

### 2. CI/CD 구축
- 미니 PC에서 동작하는 CI/CD 파이프라인 구축
- 예상 구성: 코드 Push → 빌드(`./gradlew bootJar`) → Docker 이미지 재빌드 → 컨테이너 재시작

### 3. 환경 변수 파일 (.env) 작성
- 현재 JWT_SECRET은 개발용 기본값 사용 중 — 운영 전 반드시 교체 필요
- `.env` 파일 생성 후 `docker compose up -d` 재실행하면 자동 적용

### 4. 도메인 및 HTTPS 적용 (선택)
- 도메인 구매 후 DNS 설정 → 공인 IP 연결
- Nginx 리버스 프록시 + Let's Encrypt 인증서로 HTTPS 적용
- 적용 후 `BE_URL`, `FE_URL` 환경 변수 도메인으로 변경 (QR 코드 기준 URL 영향)
