# Play Store용 API 배포 런북 (#117)

앱(Capacitor Android)이 호출할 **운영 HTTPS API**를 올리는 절차입니다.  
DB는 기존과 동일하게 **MySQL**입니다.

## 1. 로컬 Docker 스모크

```bash
cp env.deploy.example env.deploy
# env.deploy 시크릿·OAuth 값 채우기
docker compose --env-file env.deploy up --build
curl -s http://localhost:8080/api/health
# {"status":"UP"}
```

Swagger: http://localhost:8080/swagger-ui.html

## 2. PaaS (Railway / Render / Fly 등) 권장 구성

| 리소스 | 역할 |
|--------|------|
| MySQL | managed DB (또는 compose의 mysql) |
| Web service | 이 레포 Dockerfile 빌드, port 8080 |

필수 환경변수:

- `SPRING_PROFILES_ACTIVE=prod`
- `MYSQL_HOST`, `MYSQL_PORT`, `MYSQL_DATABASE`, `MYSQL_USER`, `MYSQL_PASSWORD`
- `JWT_SECRET` (32자 이상 권장)
- `GOOGLE_*`, `KAKAO_*`, `NAVER_*` client id/secret
- `GOOGLE_REDIRECT_URIS`, `KAKAO_REDIRECT_URIS` — 웹 + Android 딥링크 포함  
  예: `https://YOUR_WEB/auth/login/google/,myreview://auth/login/google/`
- `APP_CORS_ORIGINS` — Capacitor는 보통 `https://localhost` (웹뷰). 필요 시 추가 origin

배포 후 공개 URL 예: `https://api.example.com`  
→ 앱 빌드 시 `VITE_API_BASE_URL=https://api.example.com`

## 3. OAuth 콘솔

각 제공자 개발자 콘솔에 **서버 allowlist와 동일한** redirect URI 등록:

- 웹(로컬): `http://localhost:5173/auth/login/{provider}/`
- Android 딥링크: `myreview://auth/login/{provider}/`

trailing slash 필수.

## 4. 스모크

1. `GET https://api…/api/health` → UP  
2. 앱/웹에서 소셜 로그인 1회 (code → `POST /api/auth/...`)

## 5. 이 레포에서 하지 않는 것

- PaaS 계정 생성·결제·커스텀 도메인 DNS (운영자)
- Play Console 업로드 (앱 레포 `docs/play-store.md`)
