# URL Modifier

긴 URL을 짧게 단축하고 QR 코드를 자동 생성하는 서비스입니다.
로그인 사용자에게는 커스텀 URL 슬러그, 만료 조건 설정, 클릭 통계 기능을 추가 제공합니다.

---

## 주요 기능

| 기능 | 비회원 | 회원 |
|---|:---:|:---:|
| URL 단축 | O | O |
| QR 코드 자동 생성 | O | O |
| 커스텀 URL 슬러그 | X | O |
| 만료 시각 설정 | X | O |
| 최대 클릭 수 설정 | X | O |
| 내 URL 목록 관리 | X | O |
| 클릭 통계 (일별 차트) | X | O |

**URL 제약 조건**
- 단축 URL의 재단축 불가 (`bit.ly`, `tinyurl.com`, `t.co` 및 자기 자신 도메인)
- 커스텀 슬러그: 1자 이상 30자 이하
- 비회원 생성 URL은 동일 원본 URL 재요청 시 기존 결과 재사용
- 회원 생성 URL은 동일 원본 URL 재요청 시 기존 결과 재사용

**JWT 토큰 정책**
- Access Token 유효시간: 1시간
- Refresh Token 유효시간: 7일 (Redis에 저장)
- 로그아웃 시 Access Token Redis 블랙리스트 등록 (잔여 만료 시간까지 유지)

---

## 기술 스택

### Backend
| 항목 | 버전 |
|---|---|
| Java | 17 |
| Spring Boot | 3.5.3 |
| Spring Security | - |
| Spring Data JPA | - |
| PostgreSQL | 14 |
| Redis | 7 |
| jjwt | 0.12.6 |
| ZXing (QR Code) | 3.5.3 |
| Springdoc OpenAPI (Swagger) | 2.8.9 |

### Frontend
| 항목 | 버전 |
|---|---|
| Vue | 3.5.17 |
| Vite | 7 |
| Axios | 1.10.0 |
| Chart.js | 4.4.0 |
| chartjs-plugin-zoom | 2.0.1 |

### Infra
| 항목 | 내용 |
|---|---|
| FE 서빙 | Nginx 1.27-alpine |
| 컨테이너 | Docker / Docker Compose |

---

## 프로젝트 구조

```
URLModifier/
├── docker-compose.yaml              # 전체 스택 오케스트레이션
├── Dockerfile                       # BE 멀티스테이지 빌드
├── .env.example                     # 환경변수 템플릿
├── .dockerignore
│
├── URLModifierBackend/
│   ├── build.gradle
│   ├── settings.gradle
│   └── src/main/
│       ├── resources/
│       │   └── application.yaml
│       └── java/bigmac/urlmodifierbackend/
│           ├── domain/
│           │   ├── url/
│           │   │   ├── controller/  URLController
│           │   │   ├── service/     URLService, URLServiceImpl, URLValidateServiceImpl
│           │   │   ├── model/       URL, ClickEvent
│           │   │   ├── repository/  URLRepository, ClickEventRepository
│           │   │   └── dto/         request/, response/
│           │   └── user/
│           │       ├── controller/  AuthController, MyPageController
│           │       ├── service/     AuthService, AuthServiceImpl, MyPageService, MyPageServiceImpl
│           │       ├── model/       User
│           │       ├── repository/  UserRepository
│           │       └── dto/         request/, response/
│           └── global/
│               ├── config/   SecurityConfig, WebConfig, RedisConfig, IdGeneratorConfig
│               ├── util/     JwtUtil, JwtAuthenticationFilter, SnowflakeIdGenerator, Base62, QRCodeUtil
│               ├── dto/      ErrorResponse
│               └── handler/  GlobalExceptionHandler
│
└── URLModifierFrontend/
    ├── Dockerfile                   # FE 멀티스테이지 빌드
    ├── nginx.conf                   # Nginx 프록시 설정
    ├── .dockerignore
    ├── vite.config.js
    └── src/
        ├── main.js
        ├── App.vue                  # 단일 SPA (Vue Router 미사용)
        └── assets/
```

---

## 네트워크 구조

Docker에서는 FE(Nginx) 포트만 외부에 열리며, BE/DB/Redis는 내부 네트워크(`app-network`)에서만 접근 가능합니다.

```
외부 (인터넷)
      │
   :80 (fe / Nginx)   ← 유일하게 열린 포트
      │
      └── app-network (Docker 내부)
              ├── app (BE)  :8080   포트 비노출
              │       ├── db     :5432   포트 비노출
              │       └── redis  :6379   포트 비노출
```

**Nginx 요청 처리 흐름**

| 요청 경로 | 처리 |
|---|---|
| `GET /` | Vue SPA `index.html` 서빙 |
| `/assets/*` | 정적 파일 서빙 (캐시 1년) |
| `/auth/*`, `/short-urls*`, `/me`, `/urls/*` | BE 프록시 (REST API) |
| `/{slug}` | BE 프록시 → 원본 URL로 302 리다이렉트 |

---

## Docker 배포

### 사전 요구사항

- Docker Engine
- Docker Compose

### 1. 환경변수 파일 설정

```bash
cp .env.example .env
```

`.env` 파일에서 아래 값을 채웁니다.

| 변수 | 설명 | 예시 |
|---|---|---|
| `PUBLIC_URL` | 외부 접근 도메인/IP (끝에 `/` 없음) | `http://mysite.com` |
| `DB_USERNAME` | PostgreSQL 사용자 이름 | `urluser` |
| `DB_PASSWORD` | PostgreSQL 비밀번호 | `changeme` |
| `REDIS_PASSWORD` | Redis 비밀번호 (없으면 빈칸) | |
| `JWT_SECRET` | JWT 서명 키 (Base64 인코딩, 32바이트 이상 권장) | |

> **JWT_SECRET 생성 예시**
> ```bash
> openssl rand -base64 32
> ```

`PUBLIC_URL`은 단축 URL 생성, QR 코드, 만료 링크 리다이렉트에 모두 사용됩니다.
예를 들어 `http://mysite.com`으로 설정하면 생성된 단축 URL이 `http://mysite.com/aB3cD4` 형태가 됩니다.

### 2. 빌드 및 실행

```bash
docker compose up -d --build
```

BE와 FE 모두 이미지 빌드부터 실행까지 자동으로 처리됩니다.

### 3. 종료

```bash
# 컨테이너만 종료
docker compose down

# 컨테이너 + DB 볼륨까지 삭제
docker compose down -v
```

---

## 로컬 개발 환경

### 사전 요구사항

| 도구 | 버전 | 용도 |
|---|---|---|
| Java | 17 | BE 실행 |
| Node.js | 18 이상 | FE 빌드/실행 |
| Docker | - | DB, Redis 실행 |

### 1. 인프라 실행 (DB, Redis만)

```bash
# 프로젝트 루트에서
docker compose up -d db redis
```

- PostgreSQL: `localhost:5432`, DB명 `urlModifierDB`, 기본 계정 `test`/`test`
- Redis: `localhost:6379`

### 2. 백엔드 실행

```bash
cd URLModifierBackend
./gradlew bootRun
```

- 실행 주소: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- `application.yaml` 기본값이 위 Docker 컨테이너 설정과 일치하므로 별도 환경변수 설정 불필요

### 3. 프론트엔드 실행

```bash
cd URLModifierFrontend
npm install
npm run dev
```

- 실행 주소: `http://localhost:5173`
- BE 주소를 변경하려면 `URLModifierFrontend/.env.local` 파일을 생성합니다.

```
VITE_API_URL=http://localhost:8080
```

---

## 환경변수 전체 목록

### Backend (`application.yaml` 참조)

| 환경변수 | 설명 | 기본값 |
|---|---|---|
| `DB_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/urlModifierDB` |
| `DB_USERNAME` | DB 사용자명 | `test` |
| `DB_PASSWORD` | DB 비밀번호 | `test` |
| `REDIS_HOST` | Redis 호스트 | `localhost` |
| `REDIS_PASSWORD` | Redis 비밀번호 | (없음) |
| `JWT_SECRET` | JWT 서명 키 (Base64) | (개발용 기본값, 프로덕션 변경 필수) |
| `BE_URL` | 단축 URL 생성에 사용되는 BE 공개 URL | `http://localhost:8080/` |
| `FE_URL` | 만료 링크 리다이렉트에 사용되는 FE 공개 URL | `http://localhost:5173/` |

### Frontend (Vite 빌드 시 주입)

| 환경변수 | 설명 | 기본값 |
|---|---|---|
| `VITE_API_URL` | BE API 호출 기준 URL | `http://localhost:8080` |

---

## API 명세

모든 인증 필요 요청은 `Authorization: Bearer {accessToken}` 헤더를 포함해야 합니다.

Swagger UI: `http://localhost:8080/swagger-ui.html`

### 인증 (`/auth`)

| 메서드 | 경로 | 인증 필요 | 설명 |
|---|---|:---:|---|
| POST | `/auth/register` | X | 회원가입 |
| POST | `/auth/login` | X | 로그인 |
| POST | `/auth/refresh` | X | Access Token 재발급 |
| POST | `/auth/logout` | O | 로그아웃 |
| DELETE | `/auth/withdraw` | O | 회원 탈퇴 |

**POST /auth/register**
```json
// Request
{ "email": "user@example.com", "nickName": "홍길동", "password": "password123" }
// Response: 201 Created
```

**POST /auth/login**
```json
// Request
{ "email": "user@example.com", "password": "password123" }

// Response 200
{
  "userId": "123456789",
  "email": "user@example.com",
  "nickName": "홍길동",
  "jwtResponse": {
    "accessToken": "eyJ...",
    "refreshToken": "eyJ..."
  }
}
```

**POST /auth/refresh**
```json
// Request
{ "refreshToken": "eyJ..." }

// Response 200
{ "accessToken": "eyJ...", "refreshToken": "eyJ..." }
```

**POST /auth/logout**
```
Authorization: Bearer {accessToken}
// Response: 200 OK
```

**DELETE /auth/withdraw**
```json
// Authorization: Bearer {accessToken}
// Request Body
{ "password": "password123" }
// Response: 200 OK
// 처리 내용: 해당 사용자의 모든 URL 및 클릭 이벤트 삭제 후 회원 탈퇴
```

---

### URL (`/short-urls`, `/{slug}`, `/urls`)

| 메서드 | 경로 | 인증 필요 | 설명 |
|---|---|:---:|---|
| POST | `/short-urls` | 선택 | URL 단축 |
| POST | `/short-urls/custom` | O | 커스텀 URL 생성 |
| GET | `/short-urls/{slug}` | X | 슬러그 유효성 확인 |
| GET | `/{slug}` | X | 원본 URL로 리다이렉트 |
| GET | `/urls/{urlId}` | O | URL 상세 및 클릭 통계 조회 |
| DELETE | `/urls/{urlId}` | O | URL 삭제 |

**POST /short-urls**
```json
// Request
{
  "url": "https://example.com/very/long/url",
  "expiresAt": "2025-12-31T23:59:59",  // 선택 (로그인 시에만 저장)
  "maxClicks": 100                       // 선택 (로그인 시에만 저장)
}

// Response 201
{
  "id": "123456789",
  "originUrl": "https://example.com/very/long/url",
  "shortenedUrl": "http://mysite.com/aB3cD4",
  "qrCode": "iVBORw0KGgo...",
  "expiresAt": "2025-12-31T23:59:59",
  "maxClicks": 100,
  "clickCount": 0,
  "expired": false
}
```

**POST /short-urls/custom** (로그인 필수)
```json
// Request
{
  "originURL": "https://example.com/very/long/url",
  "customURL": "my-link",              // 1~30자
  "expiresAt": "2025-12-31T23:59:59", // 선택
  "maxClicks": 100                     // 선택
}
// Response 201: POST /short-urls 와 동일한 형식
```

**GET /short-urls/{slug}**
```json
// Response 200
{ "valid": true,  "originUrl": "https://example.com/..." }
{ "valid": false, "originUrl": null }
```

**GET /{slug}**
```
유효한 슬러그 → 302 Found, Location: {원본 URL}
만료된 슬러그 → 302 Found, Location: {FE_URL}?expired={slug}
없는 슬러그   → 404 Not Found
```

**GET /urls/{urlId}** (본인 URL만 조회 가능)
```json
// Response 200
{
  "id": "123456789",
  "originURL": "https://example.com/...",
  "shortenedURL": "aB3cD4",
  "qrCode": "iVBORw0KGgo...",
  "createdAt": "2025-01-01T00:00:00",
  "expiresAt": "2025-12-31T23:59:59",
  "maxClicks": 100,
  "clickEventList": [
    {
      "clickedAt": "2025-06-01T12:00:00",
      "referrer": "https://google.com",
      "userAgent": "Mozilla/5.0 ...",
      "ipAddress": "1.2.3.4"
    }
  ]
}
```

**DELETE /urls/{urlId}** (본인 URL만 삭제 가능)
```
Response: 202 Accepted
처리 내용: 해당 URL의 클릭 이벤트 전체 삭제 후 URL 삭제
```

---

### 마이페이지 (`/me`)

| 메서드 | 경로 | 인증 필요 | 설명 |
|---|---|:---:|---|
| GET | `/me` | O | 내 정보 및 URL 목록 조회 |

**GET /me**
```json
// Response 200
{
  "email": "user@example.com",
  "nickname": "홍길동",
  "urls": [
    {
      "id": "123456789",
      "originUrl": "https://example.com/...",
      "shortenedUrl": "http://mysite.com/aB3cD4",
      "qrCode": "iVBORw0KGgo...",
      "expiresAt": null,
      "maxClicks": null,
      "clickCount": 42,
      "expired": false
    }
  ]
}
```

---

## 에러 응답 형식

```json
{
  "errorCode": "ERROR_CODE",
  "message": "사용자에게 표시할 메시지"
}
```

---

## 데이터베이스 스키마

### `users`
| 컬럼 | 타입 | 설명 |
|---|---|---|
| `id` | BIGINT (PK) | Snowflake ID |
| `nick_name` | VARCHAR | 닉네임 |
| `email` | VARCHAR (unique) | 이메일 |
| `password` | VARCHAR | BCrypt 해시 |

### `url`
| 컬럼 | 타입 | 설명 |
|---|---|---|
| `id` | BIGINT (PK) | Snowflake ID |
| `users` | BIGINT (FK, nullable) | 생성한 사용자 (비회원이면 NULL) |
| `origin_url` | TEXT | 원본 URL |
| `shortened_url` | VARCHAR(30) (unique) | Base62 인코딩 슬러그 |
| `qr_code` | TEXT | QR 코드 (Base64 PNG) |
| `created_at` | TIMESTAMP | 생성 일시 |
| `expires_at` | TIMESTAMP (nullable) | 만료 일시 |
| `max_clicks` | INTEGER (nullable) | 최대 클릭 수 |

### `click_event`
| 컬럼 | 타입 | 설명 |
|---|---|---|
| `id` | BIGINT (PK, auto) | |
| `url_id` | BIGINT (FK) | 클릭된 URL |
| `clicked_at` | TIMESTAMP | 클릭 일시 |
| `referrer` | VARCHAR (nullable) | 유입 경로 |
| `user_agent` | VARCHAR (nullable) | 브라우저 정보 |
| `ip_address` | VARCHAR (nullable) | 클라이언트 IP |
