# URLcut — 모노크롬 프론트엔드 리디자인 & 백엔드 하드닝 설계

- 날짜: 2026-09-02
- 범위: `URLModifierFrontend` 전면 리디자인 + 컴포넌트 분리, `URLModifierBackend` 우선순위 높은 수정
- 핵심 제약: **기존 API 계약(엔드포인트·요청/응답 필드명)을 100% 보존한다. 동작 회귀 금지.**

## 1. 확정된 결정

| 항목 | 결정 |
|------|------|
| 미감 | 미니멀 모노크롬 (Linear/Vercel 풍) |
| 액센트 | 순수 모노크롬 — 액센트도 검정(`#111`), danger만 예외 |
| 타이포 | Geist Sans + Geist Mono 자체 호스팅. 숫자/URL은 모노스페이스 |
| 아이콘 | 이모지 제거 → 인라인 SVG 라인 아이콘 |
| 레이아웃 | 기존 2컬럼(메인 + 사이드) 유지, 플랫하게 정돈. 슬라이딩 → 짧은 페이드 |
| 다크 모드 | **미적용** (라이트 전용) |
| API 스타일 | `<script setup>` + Composition API로 이관 |
| 상태 공유 | 새 의존성 없이 반응형 싱글턴 composable |
| 코드 구조 | App.vue(2371줄) → 컴포넌트 분리 |

## 2. 절대 보존해야 할 API 계약 (회귀 방지 안전장치)

FE가 호출하는 모든 엔드포인트와 **정확한 필드명**. 재작성 시 이 표와 1:1로 대조한다.

### 인증
- `POST /auth/login` — req `{ email, password }` → `{ userId, email, nickName, jwtResponse: { accessToken, refreshToken } }`
- `POST /auth/register` — req `{ email, nickName, password }` → 201, 본문 없음
- `POST /auth/refresh` — req `{ refreshToken }` → `{ accessToken, refreshToken, ... }` (JwtResponse extends User)
- `POST /auth/logout` — 헤더 Authorization 필요, 본문 없음
- `DELETE /auth/withdraw` — req body `{ password }` (axios `delete(url, { data })`)

### URL
- `POST /short-urls` — req `{ url, expiresAt, maxClicks }` → URLResponse
- `POST /short-urls/custom` — req `{ originURL, customURL, expiresAt, maxClicks }` → URLResponse
- `GET /me` → `{ email, nickname, urls: URLResponse[] }`
- `GET /urls/{id}` → URLDetailResponse
- `DELETE /urls/{id}` → 202

### DTO 필드명 (⚠️ 대소문자 함정)
- **URLResponse** (생성·목록): `id, originUrl, shortenedUrl, qrCode, expiresAt, maxClicks, clickCount, expired` — **url 소문자**
- **URLDetailResponse** (상세): `id, originURL, shortenedURL, qrCode, createdAt, expiresAt, maxClicks, totalClicks, dailyClicks` — **URL 대문자**
- `MyPageResponse`: `email, nickname, urls[]`
- 에러 응답: `{ errorCode, message }` — FE `getSafeErrorMessage`가 `message` 우선 사용

### 보존해야 할 검증된 로직 (리팩터링 없이 이동만)
- axios refresh 동시성 제어(`refreshPromise` 싱글 플라이트), 401 인터셉터, `_isRetry` 가드
- `isTokenExpired`(JWT exp 파싱), 부팅 시 refresh 흐름(`created`)
- 통계 요청 경쟁조건 방지(`_reqVersion` 버전 카운터)
- 차트 날짜 파싱(로컬 자정 기준, YYYY-MM-DD 문자열 키 유지 — UTC 변환 금지)
- 차트 파괴/재생성 타이밍(`onPaneAfterEnter`), `beforeUnmount` 정리
- `?expired=` 쿼리 처리
- localStorage 키: `user`, `accessToken`, `refreshToken`

## 3. 디자인 시스템

### 3.1 토큰 (`src/assets/tokens.css`)
```
--bg:#FAFAFA  --surface:#FFF  --surface-2:#F5F5F5
--border:#E5E5E5  --border-strong:#D4D4D4
--text:#171717  --text-2:#6B6B6B  --text-3:#9B9B9B
--accent:#111111  --accent-hover:#000
--danger:#B42318  --danger-bg:#FEF3F2
--radius-sm:8px  --radius:12px
--shadow-sm: 0 1px 2px rgba(0,0,0,.04)
--font-sans, --font-mono
```
제거: 그라데이션 전부, 발광 그림자 전부, 인디고/바이올렛/핑크 계열 전부.

### 3.2 타이포
- `public/fonts/`에 Geist Sans / Geist Mono woff2 자체 호스팅, `@font-face`(CDN 미사용).
- 헤드라인 Bold 28–32 / 섹션 18 / 본문 14–15 / 캡션 12–13.
- URL·단축코드·클릭수·날짜 → `--font-mono`.

### 3.3 아이콘 (`ui/AppIcon.vue`)
인라인 SVG(stroke 1.5, currentColor): `link, qr, chart, clock, user, close, refresh` 등 필요분만.

## 4. 컴포넌트 아키텍처

```
src/
  api/client.js          # axios 인스턴스 + 인터셉터(현행 로직 이동)
  composables/
    useAuth.js           # 반응형 싱글턴: user, 토큰, login/register/logout/withdraw, 부팅 refresh
    useMyPage.js         # myPage, 상세, 차트 상태/액션
  components/
    AppHeader.vue
    ShortenForm.vue      # 단축 폼 + 결과 + QR
    FeaturePanel.vue     # 우측 소개(shorten 모드)
    MyPageView.vue       # URL 리스트
    UrlDetailPanel.vue   # 통계 + 차트(우측, mypage 모드)
    modals/AuthModal.vue · WithdrawModal.vue · ExpiredModal.vue
    ui/BaseButton.vue · BaseInput.vue · TagChip.vue · AppIcon.vue · SpinnerDot.vue
  assets/tokens.css · base.css(갱신)
  App.vue                # 레이아웃 셸 + 탭 상태 + 모달 오케스트레이션
```

원칙: **script 로직은 이동만, CSS/마크업만 새로 작성.** 모든 컴포넌트 `<script setup>`.

## 5. 백엔드 수정 범위 (API 응답 형태 불변)

1. **RedisConfig**: `activateDefaultTyping` 제거 → `URLCacheDto` 타입 지정 직렬화(`Jackson2JsonRedisSerializer`)로 교체. insecure-deserialization 제거. `getURLFromCache` 캐스팅 정리.
2. **캐시 쓰기 시점**: `makeURLShort`/`makeCustomURL`의 `cacheURL()`을 트랜잭션 커밋 이후(`TransactionSynchronization#afterCommit`)로 이동.
3. **origin_url TEXT 인덱스**: `URL` 엔티티의 TEXT 컬럼 B-tree 인덱스 제거/정리(중복 `idx_url_origin_url` 정리). 조회 경로는 유지하되 인덱스 방식 재검토(길이 제한 회피).

범위 외(이번엔 손대지 않음, API/동작 영향 큼): URLServiceImpl 분리, QR 온디맨드화, maxClicks 원자화, X-Forwarded-For. 필요 시 별도 작업.

## 6. 단계별 구현 계획

- **P0. 안전망**: FE 빌드 확인(`npm run build`) 기준선, BE 빌드 기준선.
- **P1. BE 수정**: RedisConfig → cache-after-commit → 인덱스. 각 단계 후 `./gradlew build`(테스트 포함) 통과 확인.
- **P2. FE 토대**: tokens.css, base.css, 폰트, `api/client.js`, `useAuth.js`, `useMyPage.js`, ui 프리미티브.
- **P3. FE 화면**: AppHeader → ShortenForm/FeaturePanel → MyPageView/UrlDetailPanel → 모달들 → App.vue 셸.
- **P4. 차트 모노크롬화**: 색만 토큰 기준으로 교체, 로직 불변.
- **P5. 검증**: `npm run build`, `eslint`, 수동 스모크(단축/커스텀/로그인/마이페이지/통계/삭제/탈퇴/만료).

## 7. 검증 계획 (동작 회귀 방지)

- FE: `npm run build` + `npm run lint` 통과. §2 표와 코드 대조.
- BE: `./gradlew build` (기존 테스트 통과).
- 수동 스모크 체크리스트: 익명 단축, 로그인 후 커스텀/만료 설정, 마이페이지 목록·통계 차트, 삭제, 로그아웃, 토큰 만료 refresh, `?expired=` 진입.
