# 🤖 AI 기반 복식부기 회계 시스템

> 금융 데이터 자동 분류를 목표로 시작했으나,  
> API 제약으로 AI 대화 기반 복식부기 시스템으로 피벗한 프로젝트

<!-- 데모 GIF/영상 -->
[데모 영상 자리]

---

## ⚡ 핵심 기능

### 1. AI 대화형 거래 입력
<!-- 스크린샷 1 -->
[스크린샷 자리]

"오늘 50만원 받았어" → AI가 복식부기 자동 생성

### 2. 복식부기 자동 계산
<!-- 스크린샷 2 -->
[스크린샷 자리]

차변/대변 자동 계산 + 대차평형 원칙 검증

### 3. 재무제표 자동 생성
<!-- 스크린샷 3 -->
[스크린샷 자리]

손익계산서 + 재무상태표 실시간 조회

---

## 🛠️ 기술 스택

**Core Stack**
- Spring Boot 3.5.6 | Java 21 | PostgreSQL 16 | Redis 7

**Key Technologies**
- **QueryDSL 5.0** - CQRS 패턴, 타입 안전한 동적 쿼리
- **MapStruct 1.6** - 컴파일 타임 DTO 변환 (Reflection 없음)
- **Flyway** - DB 스키마 버전 관리
- **WebFlux** - Gemini AI 비동기 호출
- **Bucket4j 8.10** - Rate Limiting (로그인 제한)

**Security**
- JWT (Access + Refresh Token Rotation)
- Spring Security 6.5

**Architecture**
- Clean Architecture
- DDD (Domain-Driven Design)
- CQRS Pattern

**DevOps & Test**
- Docker & Docker Compose
- JUnit 5, Mockito, JaCoCo
- Swagger (SpringDoc OpenAPI 2.7)

---

## 🏗️ 아키텍처

<!-- 아키텍처 다이어그램 -->
graph LR
A[Frontend<br/>React TS] -->|HTTP| B[Backend<br/>Spring Boot]
B -->|API| C[Gemini AI]
B -->|Query| D[(PostgreSQL)]
B -->|Cache| E[(Redis)]

    style B fill:#4CAF50
    style C fill:#FFA726
    style D fill:#42A5F5
    style E fill:#EF5350

graph TB
subgraph Presentation
A[Controller<br/>REST API]
end

    subgraph Application
        B[AI Chat Service<br/>Report Service]
    end
    
    subgraph Domain
        C[Entity & Business Logic<br/>Transaction, Book, Account]
    end
    
    subgraph Infrastructure
        D[Repository<br/>AI Client<br/>Redis]
    end
    
    A -->|의존| B
    B -->|의존| C
    C -.구현.-> D
    
    style A fill:#E3F2FD
    style B fill:#C5E1A5
    style C fill:#FFE082
    style D fill:#FFCCBC

---

## 📚 더 알아보기

- 📄 **[프로젝트 상세 설명 (PPT)]** - 기술적 도전과 해결 과정
- 💻 **[API 문서 (Swagger)]** - http://localhost:8080/swagger-ui.html
- 📝 **[기술 블로그]** - 개발 과정 상세 기록

---

## 🚀 Quick Start

```bash
# 1. 저장소 클론
git clone https://github.com/leejiyun1/personal-accounting-system.git

# 2. Docker 컨테이너 실행
docker-compose up -d

# 3. 애플리케이션 실행
./gradlew bootRun

# 4. 접속
# API: http://localhost:8080
# Swagger: http://localhost:8080/swagger-ui.html
```

---

## 👨‍💻 Contact

**이지윤** (Jiyun Lee)

- 📧 Email: poi20701556@gmail.com
- 🐙 GitHub: [@leejiyun1](https://github.com/leejiyun1)

---

**개발 기간**: 2025.09 ~ 2026.01 (4개월)