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

# 🔄 파이프라인

본 프로젝트는 **사용자 요청 → Spring Boot API → Python 추론 서버 → 상태 업데이트 → 결과 조회** 흐름으로 동작합니다.
백엔드는 작업 생성과 상태 관리를 담당하고, Python 워커는 실제 가상 피팅 추론을 수행합니다.

## 전체 흐름

```text
[Client]
   |
   | 1. 로그인 / 회원가입
   v
[Spring Boot API]
   |
   | 2. 의류 목록 조회
   | 3. 가상 피팅 요청 생성
   v
[MariaDB]
   |
   | 4. Try-On 작업 정보 저장
   v
[Python Inference Worker]
   |
   | 5. 착용 이미지 + 의류 정보 기반 추론 수행
   | 6. 결과 이미지 생성
   v
[Spring Internal API]
   |
   | 7. 작업 상태(status) 및 결과 경로 업데이트
   v
[Client Polling]
   |
   | 8. jobId 기준 상태 조회
   v
[Completed Result]
```
---
## 단계별 설명

### 1. 사용자 인증

사용자는 `/api/v1/auth/register` 또는 `/api/v1/auth/login`을 통해 인증을 수행합니다.
로그인 성공 시 AccessToken을 발급받고, 이후 보호된 API 요청에 해당 토큰을 사용할 수 있습니다.

### 2. 의류 데이터 조회

클라이언트는 `/api/v1/garments`를 호출하여 의류 목록을 조회합니다.
이 과정에서 의류 데이터는 내부 DB와 외부 Naver API 기반 데이터가 혼합되어 제공될 수 있습니다.

### 3. 가상 피팅 작업 생성

사용자는 선택한 의류와 입력 이미지를 바탕으로 `/api/v1/tryons`를 호출합니다.
서버는 새로운 Try-On 작업을 생성하고, 추후 상태 확인에 사용할 `jobId`를 반환합니다.

### 4. 작업 정보 저장

Spring Boot 서버는 요청받은 작업 정보를 DB에 저장합니다.
이때 작업 상태는 일반적으로 `PENDING` 또는 그에 준하는 초기 상태로 기록됩니다.

### 5. Python 추론 서버 처리

Python 워커 또는 추론 서버는 저장된 작업 정보를 바탕으로 가상 피팅 추론을 수행합니다.
실제 AI 모델 실행은 `PYTHON_INFERENCE_BASE_URL`로 연결된 외부 또는 별도 추론 환경에서 동작할 수 있습니다.

### 6. 상태 업데이트

추론이 진행되거나 완료되면 Python 워커는 `/api/internal/jobs/{tryonId}/status` 엔드포인트로 상태를 갱신합니다.
이 API는 내부망 전용으로 사용되며, 진행 상태, 성공 여부, 결과 이미지 경로 등의 메타데이터를 백엔드에 반영합니다.

### 7. 클라이언트 폴링

클라이언트는 `/api/v1/tryons/{jobId}`를 주기적으로 조회하여 현재 작업 상태를 확인합니다.
이 구조는 비동기 작업 처리에 적합하며, 긴 추론 시간을 사용자 경험 측면에서 안정적으로 처리할 수 있습니다.

### 8. 결과 반환

작업 상태가 `COMPLETED`가 되면 클라이언트는 최종 결과 이미지 또는 관련 메타데이터를 확인할 수 있습니다.
실패한 경우에는 상태값과 오류 메시지를 기반으로 재시도 또는 예외 처리를 수행할 수 있습니다.

## 백엔드 역할 분리

- **Spring Boot**: 인증, 의류 조회, 작업 생성, 상태 조회, 결과 관리
- **MariaDB**: 사용자/의류/가상 피팅 작업 메타데이터 저장
- **Python Worker**: AI 추론 실행, 상태 보고, 결과 생성
- **Client**: 작업 요청 및 Polling 기반 상태 확인

## 비동기 처리 구조

가상 피팅은 추론 시간이 길 수 있으므로 동기 응답 방식보다 비동기 Job 처리 방식이 더 적합합니다.
따라서 본 시스템은 작업 생성 시 즉시 `jobId`를 반환하고, 이후 클라이언트가 Polling으로 상태를 조회하는 구조를 사용합니다.
