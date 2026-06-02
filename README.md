# spring-boot + DB + NGINX

## License

This project is licensed under the **BSD 2-Clause "Simplified" License**.
Copyright (c) [2026], [너임마청년]

## ERD
[ERDCloud에서 크게 보기](https://www.erdcloud.com/d/gSySuPh2NCXxgNaoC)

---
# 1. 주요 API 명세

## Auth (인증)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/auth/register` | 자체 JWT 기반 회원가입 |
| `POST` | `/api/v1/auth/login` | 로그인 및 AccessToken 발급 |

## Garment (의류)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/garments` | 의류 목록 조회 (Naver API + DB 혼합) |
| `POST` | `/api/v1/garments` | 관리자 의류 로컬 등록 |

## Try-On (가상 피팅)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/tryons` | 가상 피팅 작업 생성 (Job ID 반환) |
| `GET` | `/api/v1/tryons/{jobId}` | 가상 피팅 상태 폴링 (Polling) 조회 |
| `PATCH` | `/api/internal/jobs/{tryonId}/status` | *(내부망)* Python 워커의 상태 업데이트 보고 |

***


---
# 2. 셋업 및 실행 방법 (Docker)

캡스톤 시연 환경을 위해 Docker Compose로 쉽게 구동할 수 있도록 구성되어 있습니다.

```bash
# 1. 저장소 클론
git clone https://github.com/DEU-CS-26-03/backend-spring-tryon.git
cd backend-spring-tryon

# 2. 환경변수 설정 (.env 파일 생성)
cp .env.example .env
# .env 파일 내의 MARIADB_ROOT_PASSWORD, JWT_SECRET, NAVER_CLIENT_ID 등을 설정하세요.
# PYTHON_INFERENCE_BASE_URL 에 Ngrok 도메인을 입력해야 AI 추론이 동작합니다.

# 3. Docker Compose 빌드 및 실행
docker-compose up -d --build
```