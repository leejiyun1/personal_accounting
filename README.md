# Personal Accounting System

> AI 기반 복식부기 개인 회계 시스템

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7-red.svg)](https://redis.io/)

---

## 📌 프로젝트 소개

**"오늘 50만원 벌었어"** → AI가 자동으로 복식부기 거래 생성

프리랜서와 개인사업자를 위한 AI 자연어 처리 기반 복식부기 회계 시스템입니다.

### 핵심 가치
- 🤖 **AI 자연어 처리**: 대화만으로 복잡한 회계 처리
- 📊 **정확한 복식부기**: 차변/대변 자동 계산 및 검증
- 📈 **실시간 재무제표**: 손익계산서·재무상태표 자동 생성
- 🔒 **안전한 인증**: JWT + Redis 기반 보안

---

## 🎯 주요 기능

### 1. AI 대화형 거래 입력
```
사용자: "오늘 프로젝트 완료하고 50만원 받았어"
AI: "축하드립니다! 어떤 결제 수단으로 받으셨나요?"
사용자: "은행 계좌로 받았어"
AI: "거래가 등록되었습니다.
     [차변] 보통예금 500,000원
     [대변] 사업수익 500,000원"
```
- Gemini API를 활용한 자연어 파싱
- 세션 기반 대화 컨텍스트 관리 (Redis)
- 사용자 입력 → 구조화된 거래 데이터 자동 변환

### 2. 복식부기 자동 처리

**3단계 거래 구조:**
```
Transaction (거래)
└── JournalEntry (분개)
    └── TransactionDetail (분개 상세)
        ├── 차변 (Debit)
        └── 대변 (Credit)
```

**자동 처리 로직:**
- ✅ 수입/지출 유형에 따른 차변/대변 자동 계산
- ✅ 대차평형 원칙 검증 (차변 합계 = 대변 합계)
- ✅ 계정과목 유형 검증 (수익/비용/자산/결제수단)
- ✅ 장부 타입 일치성 검증 (개인용/사업용)

### 3. 재무제표 자동 생성

**손익계산서 (Income Statement)**
- 총수입 (Revenue)
- 총지출 (Expense)
- 순이익 (Net Profit)
- 수익률 (Profit Rate)

**재무상태표 (Balance Sheet)**
- 총자산 (Assets)
- 총부채 (Liabilities)
- 총자본 (Equity = Assets - Liabilities)

**QueryDSL 기반 복잡한 집계 쿼리**
- 7개의 최적화된 재무 분석 쿼리
- 기간별/계정별 동적 조회

### 4. 보안 시스템

**JWT 인증**
- Access Token (15분)
- Refresh Token (7일)
- Bearer Token 방식

**Redis 기반 토큰 관리**
- Refresh Token 저장
- Blacklist (로그아웃 토큰 무효화)
- Rate Limiting (Bucket4j)

---

## 🏗️ 아키텍처

### 시스템 구조
```
┌─────────────┐      ┌─────────────┐      ┌─────────────┐
│   Frontend  │ HTTP │   Backend   │      │  Gemini API │
│  (React TS) │─────▶│ Spring Boot │─────▶│  (AI Chat)  │
└─────────────┘      └─────────────┘      └─────────────┘
                            │
                    ┌───────┴───────┐
                    ▼               ▼
              ┌──────────┐   ┌──────────┐
              │PostgreSQL│   │  Redis   │
              │  (Main)  │   │ (Cache)  │
              └──────────┘   └──────────┘
```

### 레이어 구조 (DDD)

```
├── Presentation Layer (Controller)
│   └── REST API 엔드포인트
│
├── Application Layer (Service)
│   ├── AI Chat Service (Gemini API)
│   └── Report Service (CQRS)
│
├── Domain Layer (Entity, Service)
│   ├── User (사용자)
│   ├── Book (장부)
│   ├── Account (계정과목)
│   └── Transaction (거래)
│       ├── JournalEntry (분개)
│       └── TransactionDetail (분개 상세)
│
└── Infrastructure Layer (Repository)
    ├── JPA Repository
    └── QueryDSL Repository (CQRS)
```

### 복식부기 데이터 모델

```sql
-- 거래 (Transaction)
transaction_id | book_id | date       | type   | amount  | memo
1              | 1       | 2025-11-20 | INCOME | 500000  | 프로젝트 대금

-- 분개 (JournalEntry)
journal_id | transaction_id | date       | description
1          | 1              | 2025-11-20 | 수입 - 사업수익 500000원

-- 분개 상세 (TransactionDetail)
detail_id | journal_id | account_id | detail_type | debit   | credit
1         | 1          | 101        | DEBIT       | 500000  | 0       -- 보통예금 (차변)
2         | 1          | 401        | CREDIT      | 0       | 500000  -- 사업수익 (대변)
```

---

## 🛠️ 기술 스택

### Backend
| 기술 | 버전 | 용도 |
|------|------|------|
| Java | 21 | 메인 언어 (LTS) |
| Spring Boot | 3.5.6 | 프레임워크 |
| Spring Security | 6.5.5 | 인증/인가 |
| Spring Data JPA | 3.5.6 | ORM |
| QueryDSL | 5.0.0 | 타입 안전 쿼리 |

### Database & Cache
| 기술 | 버전 | 용도 |
|------|------|------|
| PostgreSQL | 16 | 메인 DB |
| Redis | 7 | 세션/캐시/Rate Limiting |

### Security
| 기술 | 버전 | 용도 |
|------|------|------|
| JWT (JJWT) | 0.12.3 | 토큰 인증 |
| BCrypt | - | 비밀번호 암호화 |
| Bucket4j | 8.10.1 | Rate Limiting |

### AI Integration
| 기술 | 용도 |
|------|------|
| Google Gemini API | 자연어 처리 |
| WebFlux | 비동기 API 호출 |

### DevOps
| 기술 | 용도 |
|------|------|
| Docker | 컨테이너화 |
| Docker Compose | 로컬 환경 구성 |
| Gradle | 빌드 도구 |

### Docs & Tools
| 기술 | 용도 |
|------|------|
| SpringDoc OpenAPI | API 문서 (Swagger) |
| Lombok | 보일러플레이트 제거 |
| Spring Validation | 입력 검증 |

---

## 📡 API 명세

### 🔐 인증 (Auth)
```
POST   /api/v1/auth/login           # 로그인
POST   /api/v1/auth/refresh         # 토큰 갱신
POST   /api/v1/auth/logout          # 로그아웃
```

### 👤 사용자 (User)
```
POST   /api/v1/users                # 회원가입
GET    /api/v1/users/{id}           # 사용자 조회
PUT    /api/v1/users/{id}           # 사용자 수정
DELETE /api/v1/users/{id}           # 사용자 삭제 (소프트 삭제)
```

### 📚 장부 (Book)
```
POST   /api/v1/books                # 장부 생성 (기본 계정과목 자동 생성)
GET    /api/v1/books                # 장부 목록
GET    /api/v1/books/{id}           # 장부 상세
PUT    /api/v1/books/{id}           # 장부 수정
DELETE /api/v1/books/{id}           # 장부 삭제 (소프트 삭제)
```

### 🏷️ 계정과목 (Account)
```
GET    /api/v1/categories/income            # 수입 카테고리
GET    /api/v1/categories/expense           # 지출 카테고리
GET    /api/v1/categories/payment-methods   # 결제수단
GET    /api/v1/accounts                     # 전체 계정과목
GET    /api/v1/accounts/{id}                # 계정과목 상세
```

### 💰 거래 (Transaction)
```
POST   /api/v1/transactions                 # 거래 생성 (복식부기 자동 생성)
GET    /api/v1/transactions                 # 거래 목록 (필터링)
GET    /api/v1/transactions/{id}            # 거래 상세
GET    /api/v1/transactions/{id}/details    # 거래 상세 (분개 포함)
PUT    /api/v1/transactions/{id}            # 거래 수정
DELETE /api/v1/transactions/{id}            # 거래 삭제 (소프트 삭제)
```

### 🤖 AI 대화 (AI)
```
POST   /api/v1/ai/chat              # AI 대화 (거래 생성 요청)
```

### 📊 재무제표 (Ledger)
```
GET    /api/v1/ledger/statement/{bookId}              # 재무제표 조회
GET    /api/v1/ledger/account/{bookId}/{accountId}   # 계정별 원장
```

### 📈 경영 분석 (Analysis)
```
GET    /api/v1/analysis/{bookId}    # AI 경영 분석
```

**전체 API 문서**: `http://localhost:8080/swagger-ui.html`

---

## 🚀 시작하기

### 사전 요구사항
- ☕ Java 21
- 🐳 Docker & Docker Compose
- 🔑 Gemini API Key

### 설치 및 실행

**1. 저장소 클론**
```bash
git clone https://github.com/leejiyun1/personal-accounting.git
cd personal-accounting
```

**2. 환경변수 설정**
```bash
cp .env.example .env
```

`.env` 파일 수정:
```properties
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/personal_account
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your_password

# Redis
SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379

# JWT
JWT_SECRET_KEY=your_secret_key_at_least_256_bits
JWT_ACCESS_TOKEN_VALIDITY=900000      # 15분
JWT_REFRESH_TOKEN_VALIDITY=604800000  # 7일

# AI
GEMINI_API_KEY=your_gemini_api_key
GEMINI_API_URL=https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent
```

**3. Docker 컨테이너 실행**
```bash
docker-compose up -d
```

**4. 애플리케이션 실행**
```bash
./gradlew bootRun
```

**5. 접속**
- 🌐 API: http://localhost:8080
- 📖 Swagger: http://localhost:8080/swagger-ui.html

---

## 🧪 테스트

```bash
# 전체 테스트
./gradlew test

# 테스트 커버리지 리포트
./gradlew jacocoTestReport

# 빌드
./gradlew build
```

---

## 📁 프로젝트 구조

```
src/main/java/com/personalaccount/
│
├── auth/                           # 인증 (JWT)
│   ├── controller/                 # 로그인/로그아웃/토큰갱신
│   ├── service/
│   ├── dto/
│   └── security/                   # SecurityConfig, JwtFilter
│
├── domain/                         # 도메인 계층
│   ├── user/                       # 사용자
│   │   ├── entity/                 # User.java
│   │   ├── repository/
│   │   ├── service/
│   │   ├── controller/
│   │   └── dto/
│   │
│   ├── book/                       # 장부
│   │   ├── entity/                 # Book.java
│   │   └── ...
│   │
│   ├── account/                    # 계정과목
│   │   ├── entity/                 # Account.java
│   │   ├── constants/              # DefaultAccounts (기본 계정과목)
│   │   └── ...
│   │
│   └── transaction/                # 거래 (복식부기)
│       ├── entity/
│       │   ├── Transaction.java         # 거래
│       │   ├── JournalEntry.java        # 분개
│       │   ├── TransactionDetail.java   # 분개 상세
│       │   ├── TransactionType.java     # INCOME/EXPENSE
│       │   └── DetailType.java          # DEBIT/CREDIT
│       └── ...
│
├── application/                    # 애플리케이션 계층
│   ├── ai/                         # AI 대화 시스템
│   │   ├── service/
│   │   │   ├── AiChatService.java
│   │   │   └── GeminiApiService.java
│   │   └── ...
│   │
│   └── report/                     # 보고서 (CQRS)
│       ├── repository/
│       │   └── ReportQueryRepository.java  # QueryDSL
│       ├── service/
│       │   └── ReportService.java
│       └── dto/
│           ├── FinancialStatement.java     # 재무제표
│           ├── IncomeStatement.java        # 손익계산서
│           └── BalanceSheet.java           # 재무상태표
│
├── common/                         # 공통 모듈
│   ├── entity/                     # BaseEntity
│   ├── dto/                        # CommonResponse, ResponseFactory
│   ├── exception/                  # Custom Exceptions
│   └── util/                       # LogMaskingUtil
│
└── config/                         # 설정
    ├── SecurityConfig.java         # Spring Security
    ├── JpaConfig.java              # JPA Auditing
    ├── RedisConfig.java            # Redis
    ├── QueryDslConfig.java         # QueryDSL
    └── SwaggerConfig.java          # API 문서
```

---

## 🎨 주요 구현 패턴

### 1. 복식부기 자동 처리
```java
// 수입: 차변(결제수단) / 대변(수입 카테고리)
if (type == INCOME) {
    createDetail(journalEntry, paymentMethod, DEBIT, amount);   // 현금 증가
    createDetail(journalEntry, category, CREDIT, amount);        // 수익 발생
}
// 지출: 차변(지출 카테고리) / 대변(결제수단)
else {
    createDetail(journalEntry, category, DEBIT, amount);         // 비용 발생
    createDetail(journalEntry, paymentMethod, CREDIT, amount);   // 현금 감소
}

// 대차평형 검증
validateDoubleEntry(journalEntry);
```

### 2. CQRS 패턴 (재무제표 조회)
```java
// QueryDSL을 사용한 복잡한 집계 쿼리
public BigDecimal findTotalIncome(Long bookId, LocalDate start, LocalDate end) {
    return queryFactory
        .select(detail.creditAmount.sum())
        .from(transaction)
        .join(journalEntry).on(journalEntry.transaction.eq(transaction))
        .join(detail).on(detail.journalEntry.eq(journalEntry))
        .join(account).on(detail.account.eq(account))
        .where(
            transaction.book.id.eq(bookId),
            transaction.date.between(start, end),
            account.accountType.eq(REVENUE)
        )
        .fetchOne();
}
```

### 3. 소프트 삭제 (Soft Delete)
```java
@Entity
public class Transaction extends BaseEntity {
    private Boolean isActive = true;  // 논리적 삭제 플래그
    
    public void deactivate() {
        this.isActive = false;
    }
}

// Repository에서 isActive 필터링
List<Transaction> findByBookIdAndIsActive(Long bookId, Boolean isActive);
```

### 4. AI 세션 컨텍스트 관리
```java
// Redis에 대화 히스토리 저장
String sessionKey = "chat:session:" + userId + ":" + bookId;
redisTemplate.opsForValue().set(sessionKey, context, 30, TimeUnit.MINUTES);

// 토큰 절약: 요약 기반 컨텍스트
if (context.size() > 10) {
    context = summarizeContext(context);  // 최근 5개만 유지
}
```

---

## 🔒 보안

### 인증 흐름
```
1. 로그인
   → Access Token (15분) + Refresh Token (7일) 발급
   → Refresh Token을 Redis에 저장

2. API 요청
   → Header: Authorization: Bearer {access_token}
   → JwtAuthenticationFilter에서 토큰 검증

3. 토큰 만료
   → Refresh Token으로 재발급
   → 기존 Refresh Token 무효화 후 새 토큰 발급

4. 로그아웃
   → Access Token을 Blacklist에 추가 (Redis)
   → Refresh Token 삭제
```

### Rate Limiting
- Bucket4j를 사용한 요청 제한
- 사용자당 분당 60회 제한

---

## 📊 재무제표 계산 로직

### 손익계산서
```
총수입 = Σ(수익 계정의 대변)
총지출 = Σ(비용 계정의 차변)
순이익 = 총수입 - 총지출
수익률 = (순이익 / 총수입) × 100
```

### 재무상태표
```
총자산 = Σ(자산 계정의 차변 - 대변)
총부채 = Σ(부채 계정의 대변 - 차변)
총자본 = 총자산 - 총부채
```

---

## 🐳 Docker Compose

```yaml
services:
  postgres:
    image: postgres:16
    environment:
      POSTGRES_DB: personal_account
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: your_password
    ports:
      - "5432:5432"

  redis:
    image: redis:7
    ports:
      - "6379:6379"
```

---

## 📝 개발 원칙

### 코드 일관성 (92/100)
- ✅ 레이어 구조: Entity → Repository → Service → Controller
- ✅ DTO 패턴: @Getter, @Builder, @NoArgsConstructor, @AllArgsConstructor
- ✅ Service 패턴: @Transactional(readOnly = true) 기본
- ✅ 예외 처리: Custom Exception 체계
- ✅ 로깅: @Slf4j, 민감정보 마스킹

### 명명 규칙
- Entity: User, Book, Transaction
- DTO: UserResponse, BookCreateRequest
- Service: UserService, BookService
- Repository: UserRepository, BookRepository

---

## 🚧 개선 계획

### Phase 1 (현재 완료)
- ✅ 인증/인가 (JWT + Redis)
- ✅ 사용자/장부 관리
- ✅ 복식부기 거래 시스템
- ✅ AI 대화 기반 거래 생성
- ✅ 재무제표 생성

### Phase 2 (진행 예정)
- ⏳ 테스트 코드 작성 (80%+ 커버리지)
- ⏳ CI/CD 파이프라인 (GitHub Actions)
- ⏳ AWS 배포 (ECS Fargate)
- ⏳ 성능 최적화 (캐싱, 인덱싱)

### Phase 3 (향후 계획)
- 📅 일정관리 기능
- 📊 대시보드 차트
- 📱 모바일 앱 (React Native)

---

## 🤝 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 라이선스

This project is licensed under the MIT License.

---

## 👨‍💻 개발자

**이지윤** (Jiyun Lee)

- 📧 Email: poi20701556@gmail.com
- 🐙 GitHub: [@leejiyun1](https://github.com/leejiyun1)
- 📝 Portfolio: [이력서 링크]

---

## 📚 기술 블로그 (예정)

개발 과정에서의 기술적 도전과 해결 과정을 블로그에 기록할 예정입니다.

1. **Django에서 Spring Boot로 전환하기**
    - 프레임워크 철학 차이
    - DI, AOP, Transaction 관리

2. **복식부기 시스템 설계하기**
    - 회계 도메인 지식
    - 대차평형 원칙 구현

3. **AI API 효율적으로 사용하기**
    - 토큰 최적화 전략
    - 세션 컨텍스트 관리

4. **QueryDSL로 복잡한 재무 쿼리 작성하기**
    - 타입 안전 쿼리
    - 동적 쿼리 최적화

---

## ⭐ Star History

프로젝트가 마음에 드셨다면 ⭐ Star를 눌러주세요!

---

**개발 기간**: 2025.09 ~ 2025.11 (3개월)

**주요 학습 목표 달성**:
- ✅ Spring Boot 프레임워크 마스터
- ✅ 복잡한 도메인(회계) 구현
- ✅ AI API 통합 경험
- ✅ DDD + CQRS 아키텍처 설계