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

## 시작하기

### 사전 요구사항

- Java 17
- Gradle
- PostgreSQL

### 데이터베이스 설정

`src/main/resources/application.yml` 파일에서 데이터베이스 연결 정보를 수정합니다.

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/URLModifier
    username: your_username
    password: your_password
```

### 애플리케이션 실행

```bash
./gradlew bootRun
```
