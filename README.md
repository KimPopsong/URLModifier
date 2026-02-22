# URLModifier

URL Shortener 서비스의 백엔드 프로젝트입니다.

## 기능

- **URL 단축**: 긴 URL을 입력받아 짧은 URL을 생성합니다.
- **리디렉션**: 짧은 URL로 접속 시 원래의 긴 URL로 리디렉션합니다.
- **QR 코드 생성**: 단축된 URL에 대한 QR 코드를 생성하여 제공합니다.

## 아키텍처

- **언어**: Java 17
- **프레임워크**: Spring Boot 3.5.3
- **데이터베이스**: PostgreSQL
- **ORM**: Spring Data JPA
- **의존성 관리**: Gradle

### 프로젝트 구조

```
URLModifierBackend
├── src
│   ├── main
│   │   ├── java
│   │   │   └── bigmac
│   │   │       └── urlmodifierbackend
│   │   │           ├── config         # 설정 클래스
│   │   │           ├── controller     # API 엔드포인트
│   │   │           ├── dto            # 데이터 전송 객체
│   │   │           ├── model          # 데이터베이스 엔티티
│   │   │           ├── repository     # 데이터베이스 접근
│   │   │           ├── service        # 비즈니스 로직
│   │   │           └── util           # 유틸리티 클래스
│   │   └── resources
│   │       └── application.yml  # 애플리케이션 설정
│   └── test
└── build.gradle               # 프로젝트 의존성 및 빌드 설정
```

## 데이터베이스

- **테이블명**: `URL`
- **컬럼**:
    - `id` (PK, BigInt): Snowflake 알고리즘으로 생성된 고유 ID
    - `origin_url` (VARCHAR): 원본 URL
    - `shortened_url` (VARCHAR): 단축된 URL (Base62 인코딩)
    - `qr_code` (VARCHAR): QR 코드 이미지 (Base64 인코딩된 문자열)

## 트러블슈팅

DB 연결 오류, WSL/Docker 포트 충돌 등은 [TROUBLESHOOTING.md](./TROUBLESHOOTING.md)를 참고하세요.

## API

### `POST /short-urls`

- **설명**: 새로운 단축 URL을 생성합니다.
- **Request Body**:
    ```json
    {
      "url": "https://example.com/very/long/url"
    }
    ```
- **Response Body**:
    ```json
    {
      "shortenedUrl": "http://localhost:8080/abcdefg",
      "qrCode": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUg..."
    }
    ```

### `GET /{shortUrl}`

- **설명**: `shortUrl`에 해당하는 원본 URL로 리디렉션합니다.
- **성공**: HTTP 302 Found, `Location` 헤더에 원본 URL 포함
- **실패**: HTTP 404 Not Found

## 환경 설정 (로컬 개발)

로컬에서 URL Modifier를 실행하려면 아래 도구들이 필요합니다.

### 필요 환경

| 구분 | 도구 | 용도 | 비고 |
|------|------|------|------|
| 필수 | **Docker** | PostgreSQL, Redis 실행 | DB/캐시를 로컬에 설치하지 않고 컨테이너로 사용 |
| 필수 | **Node.js** (v18 이상 권장) | 프론트엔드 빌드 및 개발 서버 | Vite + Vue 3 사용 |
| 필수 | **Java 17** | 백엔드 빌드 및 실행 | Spring Boot 3.x 요구 |
| 선택 | **Gradle** | 백엔드 빌드 | 프로젝트에 Gradle Wrapper 포함 (`./gradlew` 사용 가능) |

### 1. Docker 설치

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) 설치 후 실행
- 터미널에서 `docker --version`, `docker compose version` 으로 확인

### 2. Node.js 설치

- [Node.js 공식 사이트](https://nodejs.org/)에서 LTS 버전 설치 (v18 이상 권장)
- 확인: `node --version`, `npm --version`

### 3. Java 17 설치

- [Eclipse Temurin](https://adoptium.net/) 또는 [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) 등 Java 17 설치
- 확인: `java -version`

### 4. 로컬 실행 순서

#### 4-1. 인프라 실행 (Docker)

PostgreSQL과 Redis를 Docker Compose로 띄웁니다.

```bash
# 프로젝트 루트에서
docker compose up -d db redis
```

- PostgreSQL: `localhost:5432`, DB명 `urlModifierDB`, 사용자/비밀번호 `test`/`test`
- Redis: `localhost:6379` (비밀번호 없음)

#### 4-2. 백엔드 실행

```bash
cd URLModifierBackend
./gradlew bootRun
```

- 기본 주소: `http://localhost:8080`
- DB/Redis는 위에서 띄운 컨테이너에 자동 연결됩니다. (`application.yaml` 기본값 사용)

#### 4-3. 프론트엔드 실행

```bash
cd URLModifierFrontend
npm install
npm run dev
```

- 개발 서버: `http://localhost:5173` (Vite 기본 포트)

### 5. Docker로 전체 앱 실행 (백엔드 JAR 빌드 필요)

백엔드 JAR을 먼저 빌드한 뒤 Docker Compose로 앱까지 함께 실행할 수 있습니다.

```bash
# 백엔드 JAR 빌드
cd URLModifierBackend
./gradlew bootJar

# 프로젝트 루트로 이동 후 전체 서비스 실행
cd ..
docker compose up -d
```

- 백엔드: `http://localhost:8080`
- DB: `localhost:5432`, Redis: `localhost:6379`
- 프론트엔드는 별도로 `URLModifierFrontend`에서 `npm run dev`로 실행

### 6. 환경 변수 (선택)

`application.yaml`에서 다음 환경 변수로 오버라이드 가능합니다.

| 변수 | 설명 | 기본값 |
|------|------|--------|
| `DB_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/urlModifierDB` |
| `DB_USERNAME` | DB 사용자명 | `test` |
| `DB_PASSWORD` | DB 비밀번호 | `test` |
| `REDIS_HOST` | Redis 호스트 | `localhost` |
| `BE_BASE_URL` | 백엔드 Base URL | `http://localhost:8080/` |
| `FE_BASE_URL` | 프론트엔드 Base URL | `http://localhost:5173/` |
| `JWT_SECRET` | JWT 서명용 시크릿 | (개발용 기본값) |

---

## 데이터베이스 설정 (직접 설치 시)

Docker 없이 로컬에 PostgreSQL/Redis를 직접 설치한 경우, `URLModifierBackend/src/main/resources/application.yaml`에서 연결 정보를 수정합니다.

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/urlModifierDB
    username: your_username
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379
```
