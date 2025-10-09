# Personal Accounting System

> AI 대화 기반 복식부기 가계부 시스템

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7-red.svg)](https://redis.io/)

---

## 프로젝트 소개

**"오늘 5만원 벌었어"** → AI가 자동으로 복식부기 장부 작성

프리랜서와 개인사업자를 위한 AI 대화형 가계부 시스템입니다.
- 자연어로 대화만 하면 복식부기 자동 생성
- 정확한 재무제표로 세무사에게 바로 전달 가능
- 일정관리와 재무관리를 하나의 플랫폼에서

---

## 주요 기능

### AI 대화형 입력
```
사용자: "오늘 5만원 벌었어"
AI: "어떤 수입인가요?"
사용자: "용돈"
AI: "2025년 10월 09일 / 현금: 50,000 | 용돈수입: 50,000
     입력이 완료되었습니다."
```
복식부기를 몰라도 자연어로 대화만 하면 자동으로 거래가 기록됩니다.

### 복식부기 자동 처리
- 사용자 입력을 차변/대변으로 자동 변환
- 대차평형 자동 검증
- 정확한 재무제표 생성 (손익계산서, 재무상태표, 현금흐름표)

### 세무 신고 보조
- 세무사에게 전달 가능한 정확한 장부
- 직접 세무 신고도 가능 (참고용)
- 세무사는 장부 정리 없이 세금 절약 전략에만 집중 가능

### 일정관리
- 프로젝트 마감일 관리
- 수입 일정과 연동
- 달력 형식의 직관적인 UI

---

## 기술 스택

### Backend
- **Java 21** - LTS 버전
- **Spring Boot 3.5.6** - 최신 안정 버전
- **Spring Data JPA** - ORM
- **Spring Security** - 인증/인가
- **QueryDSL** - 타입 안전 쿼리

### Database & Cache
- **PostgreSQL 16** - 메인 데이터베이스
- **Redis 7** - 세션 관리, 캐시

### AI
- **OpenAI GPT-4o-mini** - 자연어 처리
- **WebClient** - API 호출

### DevOps
- **Docker & Docker Compose** - 컨테이너화
- **GitHub Actions** - CI/CD
- **AWS ECS Fargate** - 컨테이너 오케스트레이션
- **AWS RDS** - 관리형 PostgreSQL
- **AWS ElastiCache** - 관리형 Redis

### Tools
- **Gradle** - 빌드 도구
- **SpringDoc OpenAPI** - API 문서화 (Swagger)
- **Lombok** - 보일러플레이트 제거
- **JUnit 5** - 테스트

---

## API 명세

### 인증
- `POST /api/v1/auth/signup` - 회원가입
- `POST /api/v1/auth/login` - 로그인
- `POST /api/v1/auth/logout` - 로그아웃
- `POST /api/v1/auth/refresh` - 토큰 갱신

### 장부 관리
- `POST /api/v1/books` - 장부 생성
- `GET /api/v1/books` - 장부 목록
- `GET /api/v1/books/{id}` - 장부 상세

### AI 대화
- `POST /api/v1/ai/chat` - AI와 대화

### 거래
- `POST /api/v1/transactions` - 거래 생성
- `GET /api/v1/transactions` - 거래 목록
- `GET /api/v1/transactions/{id}` - 거래 상세

### 통계
- `GET /api/v1/statistics/summary` - 월별 요약
- `GET /api/v1/statistics/category` - 카테고리별 통계

### 보고서
- `GET /api/v1/reports/income-statement` - 손익계산서
- `GET /api/v1/reports/balance-sheet` - 재무상태표
- `GET /api/v1/reports/cash-flow` - 현금흐름표

전체 API 명세: `/swagger-ui.html`

---

## 프로젝트 구조

```
src/main/java/com/personalaccount/
├── common/                  # 공통 (BaseEntity, CommonResponse, Exception)
├── config/                  # 설정 (Security, JPA, Redis, Swagger)
├── auth/                    # 인증 (JWT)
├── user/                    # 사용자
├── book/                    # 장부
├── account/                 # 계정과목
├── transaction/             # 거래 (복식부기)
├── ai/                      # AI 대화
├── schedule/                # 일정관리
├── statistics/              # 통계
└── report/                  # 보고서
```

각 도메인은 계층형 구조:
```
도메인/
├── entity/                  # JPA 엔티티
├── repository/              # 데이터 접근
├── service/                 # 비즈니스 로직
├── controller/              # REST API
└── dto/                     # 요청/응답 DTO
```

---

## 시작하기

### 사전 요구사항
- Java 21
- Docker & Docker Compose
- PostgreSQL 16 (Docker 사용 권장)
- Redis 7 (Docker 사용 권장)

### 설치 및 실행

1. **저장소 클론**
```bash
git clone https://github.com/leejiyun1/personal_accounting.git
cd personal_accounting
```

2. **Docker 컨테이너 실행**
```bash
docker-compose up -d
```

3. **환경변수 설정**
```bash
cp .env.example .env
# .env 파일 수정 (OPENAI_API_KEY 등)
```

4. **애플리케이션 실행**
```bash
./gradlew bootRun
```

5. **접속**
- API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html

---

## 개발 환경

### 로컬 개발
```bash
# PostgreSQL & Redis 실행
docker-compose up -d

# Spring Boot 실행
./gradlew bootRun

# 테스트
./gradlew test
```

### 빌드
```bash
./gradlew build
```

---

## 테스트

```bash
# 전체 테스트
./gradlew test

# 테스트 커버리지
./gradlew jacocoTestReport
```

목표 커버리지: 80%+

---

## 배포

### Docker 이미지 빌드
```bash
docker build -t personal-accounting .
```

### AWS ECS 배포
```bash
# GitHub Actions를 통한 자동 배포
git push origin main
```

---

## 디자인 패턴

프로젝트에 적용된 주요 디자인 패턴:

- **DTO Pattern**: Entity 직접 노출 방지
- **Builder Pattern**: 객체 생성 (Lombok @Builder)
- **Strategy Pattern**: 통계 계산, AI 파싱
- **Factory Pattern**: DTO/Entity 변환
- **Repository Pattern**: 데이터 접근 계층
- **Service Layer Pattern**: 비즈니스 로직 캡슐화
- **Exception Handling Pattern**: 전역 예외 처리

---

## 라이선스

This project is licensed under the MIT License.

---

## 개발자

**이지윤** (Jiyun Lee)
- Email: poi20701556@gmail.com
- GitHub: [@leejiyun1](https://github.com/leejiyun1)

---

## 개발 일정

- **Phase 1**: 인증 + 사용자 (1주)
- **Phase 2**: 장부 + 계정과목 + 거래 (2주)
- **Phase 3**: AI 대화 시스템 (2주)
- **Phase 4**: 통계 + 보고서 (2주)
- **Phase 5**: 일정관리 (1주)
- **Phase 6**: 배포 + 문서화 (1주)

**총 개발 기간**: 9주 (약 2.5개월)