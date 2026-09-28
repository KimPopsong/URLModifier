# HTTPS 인증서 자동 갱신과 적용

Certbot 컨테이너는 12시간마다 `certbot renew`를 실행한다. 인증서 파일 갱신만으로는
이미 실행 중인 Nginx가 새 인증서를 제공하지 않으므로 reload도 필요하다.

`nginx/start-nginx.sh`는 공유된 `fullchain.pem`의 SHA-256을 60초마다 확인한다.
변경을 감지하면 `nginx -t`로 검사한 다음 `nginx -s reload`를 실행한다.
파일 읽기, 검사 또는 reload 명령이 실패하면 로그를 남기고 다음 주기에 재시도한다.
Nginx의 기존 entrypoint 초기화와 종료 신호 처리는 유지한다.

## 서버 적용

이 저장소는 `main`에 푸시하면 GitHub Actions의 백엔드·프론트엔드 검사를 거쳐
서버에서 `git pull`과 `docker compose up -d --build`를 실행한다.
배포가 성공하면 이번 Compose 설정과 감시 스크립트도 자동 적용되므로 별도 수동 적용은 필요 없다.
이후 인증서 갱신과 reload는 컨테이너에서 처리하므로 CI/CD 실행이 필요 없다.

자동 배포를 사용하지 않고 수동으로 적용할 때만, 수정한 파일을 서버에 전달한 뒤
Ubuntu 터미널에서 아래 명령을 실행한다.

```bash
cd ~/URLModifier
docker compose config --quiet
docker compose up -d --no-deps nginx
docker compose logs --tail=50 nginx
curl -I https://urlcut.kr
```

Compose 설정 변경으로 Nginx 컨테이너가 재생성되므로 적용 시 잠깐 연결이 끊길 수 있다.
시작 시 현재 인증서를 읽고, 이후 갱신은 reload로 적용한다.
로그에 `[certificate-watch] Checking for certificate changes every 60s.`가 있어야 한다.
단순 `docker compose restart nginx`만으로는 Compose 설정 변경이 반영되지 않는다.

## 확인 및 장애 진단

```bash
docker compose ps certbot nginx
docker compose logs --tail=100 certbot nginx
docker compose exec certbot certbot certificates
docker compose exec certbot certbot renew --dry-run
```

`--dry-run`은 인증서 갱신 가능 여부를 검사하며 실제 인증서를 바꾸지 않으므로
변경 감지에 따른 reload는 발생하지 않는다. 실제 갱신 후에는 Nginx 로그에
`Certificate changed; nginx reload requested.`가 출력되는지와 외부 인증서 만료일을 확인한다.

```bash
echo | openssl s_client -connect urlcut.kr:443 -servername urlcut.kr 2>/dev/null |
  openssl x509 -noout -dates
```

Certbot 갱신 자체가 실패하는 문제는 이 감시 스크립트가 해결하지 않는다.
그 경우 Certbot 로그에서 도메인 검증, 네트워크, 인증서 갱신 설정을 확인한다.
