# URLModifierFrontend

URL Modifier 서비스의 프론트엔드입니다.
Vue 3 SPA로 구성되어 있으며, Nginx 컨테이너로 서빙됩니다.

## 기술 스택

| 항목 | 버전 |
|---|---|
| Vue | 3.5.17 |
| Vite | 7 |
| Axios | 1.10.0 |
| Chart.js | 4.4.0 |
| chartjs-plugin-zoom | 2.0.1 |

## 로컬 개발

```bash
npm install
npm run dev   # http://localhost:5173
```

BE 주소를 변경하려면 `.env.local` 파일을 생성합니다.

```
VITE_API_URL=http://localhost:8080
```

## 빌드

```bash
npm run build   # dist/ 폴더에 결과물 생성
```

## Docker 빌드 (docker-compose 사용 시 자동 처리)

```bash
docker build \
  --build-arg VITE_API_URL=http://mysite.com \
  -t urlmodifier-fe .
```

`VITE_API_URL`은 빌드 시점에 번들에 포함됩니다.
Docker Compose로 배포 시에는 루트의 `.env`에서 `PUBLIC_URL`을 통해 자동 주입됩니다.

## 환경변수

| 변수 | 설명 | 기본값 |
|---|---|---|
| `VITE_API_URL` | BE API 기준 URL | `http://localhost:8080` |

## Nginx 프록시 구조

`nginx.conf`에 의해 아래와 같이 요청이 처리됩니다.

| 요청 | 처리 |
|---|---|
| `GET /` | `index.html` 서빙 (Vue SPA) |
| `/assets/*` | 정적 파일 (캐시 1년) |
| 그 외 | BE(`http://be:8080`) 프록시 |
