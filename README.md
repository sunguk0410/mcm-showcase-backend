# MCM AI Journey — Backend

> 고객보다 한발 먼저 취향을 이해하고, 다음 방문까지 기억하는 Interactive Retail Experience

<img width="1920" height="1080" alt="12" src="https://github.com/user-attachments/assets/628bcef9-1400-41a1-8035-5073e4bdd825" />

MCM HAUS 청담의 오프라인 쇼핑 경험을 온라인과 연결하는 **MCM AI Journey의 백엔드 서버**입니다.

매장 동선, AR 상품 선택, 위시리스트, 실물 피팅 요청 등 고객의 쇼핑 과정에서 발생하는 데이터를 하나의 세션으로 연결하고, 이를 AI 추천 서버와 연계하여 Personalized Recommendation, Personalized Comment, Avatar Look, Digital Closet으로 이어지는 전체 쇼핑 여정을 관리합니다.

## Demo

* MCM Storefront: https://www.mcm-showcase.com
* AR Fitting: https://www.mcm-showcase.com/ar
* Backend API: https://api.mcm-showcase.com
* Swagger: https://api.mcm-showcase.com/swagger-ui/index.html

## 서비스 배경

럭셔리 매장의 고객은 각자 다른 동선과 관심 상품을 가지고 매장을 탐색합니다. 하지만 기존 오프라인 쇼핑에서는 고객이 어떤 공간에 오래 머물렀는지, 어떤 상품을 비교했는지, 무엇을 피팅하고 찜했는지와 같은 행동이 구매하지 않는 순간 대부분 사라집니다.

MCM AI Journey Backend는 이러한 오프라인 행동을 하나의 **Customer Journey Data**로 연결하고, 고객의 현재 행동과 이전 취향을 AI 추천에 활용할 수 있도록 설계했습니다.

1. 고객이 매장에 입장하면 Customer Session을 생성합니다.
2. 매장 내 Zone 이동과 체류 시간을 기록합니다.
3. AR Fitting을 시작하면 AR Session을 생성하고 회원 또는 게스트 정보를 연결합니다.
4. 상품 선택·해제·위시리스트·실물 피팅 요청 등의 행동을 순서대로 저장합니다.
5. 축적된 Zone, Wishlist, AR Interaction 데이터를 AI 추천 서버로 전달합니다.
6. 행동 변화에 따라 추천 결과를 갱신하고 Personalized Comment를 제공합니다.
7. 세션 종료 시 고객의 선택을 기반으로 Avatar Look을 생성합니다.
8. 생성된 결과를 Style Profile과 Digital Closet에 저장해 다음 방문까지 이어지는 고객 경험을 만듭니다.

## 주요 기능

### Customer Journey Tracking

* 고객 방문 단위 `CustomerSession` 관리
* 회원 및 비회원 세션 지원
* 매장 Zone 입장·퇴장 시간 기록
* Zone별 체류 시간 저장
* 고객이 관심을 보인 매장 영역을 추천 신호로 활용
* AR Session과 Customer Session 연결

### AR Session & Interaction

* AR Fitting 세션 생성 및 종료
* 회원 연결 및 성별 정보 관리
* AR에서 발생한 상품 행동 순차 저장
* 상품 선택 및 해제 기록
* 위시리스트 추가 및 삭제 기록
* 실물 피팅 요청 기록
* 행동 Sequence를 유지하여 AI 추천 서버에 전달

주요 Interaction Type:

```text
PRODUCT_SELECT
PRODUCT_DESELECT
FITTING
WISHLIST_ADD
WISHLIST_REMOVE
```

### Personalized Recommendation

고객의 매장 경험과 AR 행동을 AI Recommendation Server와 연결하여 현재 세션에 맞는 상품 추천을 제공합니다.

추천에는 다음 정보가 활용됩니다.

* 고객 성별
* Zone별 체류 기록
* 회원 Wishlist
* AR에서 선택한 상품
* AR 행동 순서
* 상품 Category
* 이전 행동을 기반으로 한 추천 결과

AR 행동이 새롭게 발생하면 추천 결과를 갱신하여 고객의 현재 관심 변화가 다음 추천에 반영되도록 구성했습니다.

```text
Zone Interaction
       +
Member Wishlist
       +
AR Interaction
       ↓
Spring Backend
       ↓
AI Recommendation Server
       ↓
Personalized Product Ranking
```

### Personalized Comment

AR Fitting 과정에서 단순 상품 추천뿐 아니라 고객 행동에 맞는 상황별 메시지를 제공합니다.

* 첫 상품 피팅 감지
* 특정 Zone에 대한 높은 관심 감지
* 여러 Category 탐색 감지
* 액세서리 등 보완 Category 제안
* Wishlist 행동 기반 구매 관심도 감지

고객 행동이 발생할 때 현재 AR Session을 평가하여 조건이 충족된 경우에만 Personalized Comment를 반환합니다.

### Personalized Avatar

AR Fitting 과정에서 축적된 상품 선택과 추천 결과를 이용해 고객의 최종 Avatar Look을 생성합니다.

* AR 행동 기반 Avatar Look 상품 구성
* AI Recommendation Server와 Avatar Look 연동
* 선택된 상품 조합을 Style Profile로 저장
* 외부 Image Generation API를 이용한 Personalized Avatar 생성
* 생성된 이미지의 배경 제거 처리
* 생성된 Avatar URL을 Style Profile과 연결
* QR을 통한 Digital Closet 전달

### Digital Closet

AR Fitting이 단발성 매장 경험으로 끝나지 않도록 결과를 Digital Closet에 저장합니다.

* 회원별 Style Profile 목록 조회
* Style Profile 상세 조회
* Personalized Avatar 저장
* 해당 세션에서 확인한 상품 기록 제공
* QR을 통한 비회원 Style Profile 조회
* 비회원 결과를 로그인 회원 계정에 연결
* 다음 방문에서도 이전 스타일 기록 활용 가능

### Product

추천과 Digital Closet에서 사용하는 MCM 상품 데이터를 관리합니다.

* 상품 기본 정보 조회
* 상품 Category 연결
* 상품 이미지 URL 관리
* 상품 가격 및 상세 페이지 URL 관리
* 추천 결과를 실제 Product 정보와 결합하여 프론트엔드에 반환

## 사용자 여정

```text
매장 방문
    ↓
Customer Session 생성
    ↓
Zone 이동 및 체류 기록
    ↓
AR Fitting 시작
    ↓
AR Session 생성
    ↓
회원 연결 또는 게스트 진행
    ↓
Zone / Wishlist 기반 초기 추천
    ↓
상품 선택 · 해제 · 찜 · 실물 피팅 요청
    ↓
ArInteraction 저장
    ↓
AI Recommendation Server 추천 갱신
    ↓
Personalized Comment
    ↓
Avatar Look 생성
    ↓
Personalized Avatar 생성
    ↓
Style Profile 저장
    ↓
QR 발급
    ↓
Digital Closet
    ↓
다음 방문의 고객 취향 데이터로 연결
```

## 시스템 구조

```text
┌──────────────────────┐
│   React Frontend     │
│ Store / AR / Closet  │
└──────────┬───────────┘
           │ REST API
           ▼
┌──────────────────────────────┐
│      Spring Boot Backend     │
│                              │
│ Customer Session             │
│ Zone Interaction             │
│ AR Session / Interaction     │
│ Recommendation Orchestration │
│ Personalized Comment         │
│ Avatar / Style Profile       │
│ Digital Closet               │
└───────┬──────────────┬───────┘
        │              │
        │ JPA          │ REST API
        ▼              ▼
┌──────────────┐   ┌────────────────────┐
│   MySQL 8    │   │ Python AI Server   │
│              │   │                    │
│ Member       │   │ RecRec             │
│ Session      │   │ Recommendation     │
│ Interaction  │   │ Avatar Look        │
│ Product      │   └────────────────────┘
│ StyleProfile │
└──────────────┘
                       │
                       ▼
                ┌───────────────┐
                │ External APIs │
                │ Image Generate│
                └───────────────┘
```

## 기술 스택

* Java 17
* Spring Boot 4.1
* Spring Web MVC — REST API
* Spring Data JPA — 데이터 영속성 및 Repository
* Hibernate — ORM
* MySQL 8 — 서비스 데이터 저장
* Spring Validation — API Request 검증
* Spring REST Client — Python AI 및 외부 API 통신
* Spring Boot Actuator — 서버 상태 확인
* Springdoc OpenAPI — Swagger API 문서
* Lombok — 반복 코드 최소화
* Gradle — 빌드 및 의존성 관리
* Docker / Docker Compose — 애플리케이션 및 MySQL 컨테이너화
* GitHub Actions — CI/CD
* AWS EC2 — 백엔드 서버 운영
* AWS ECR — Docker Image 관리
* REST API — React Frontend 및 Python AI Server 연동

## 실행 방법

### 요구 사항

* Java 17
* Docker
* Docker Compose
* MySQL 8 또는 Docker MySQL
* 연결 가능한 MCM Recommendation Server

### Gradle 실행

```bash
./gradlew bootRun
```

Windows:

```bash
gradlew.bat bootRun
```

기본적으로 백엔드 서버는 다음 주소에서 실행됩니다.

```text
http://localhost:8080
```

### 빌드

```bash
./gradlew clean build
```

생성된 JAR 실행:

```bash
java -jar build/libs/*.jar
```

## Docker 실행

프로젝트는 Spring Boot Application과 MySQL을 Docker Compose로 함께 실행할 수 있습니다.

```bash
docker compose up -d --build
```

실행되는 기본 서비스:

```text
Spring Boot : 8080
MySQL       : 3306
```

컨테이너 확인:

```bash
docker compose ps
```

로그 확인:

```bash
docker compose logs -f app
```

종료:

```bash
docker compose down
```

MySQL 데이터는 Docker Volume에 저장되어 컨테이너를 다시 생성해도 유지됩니다.

## 환경 변수

실행 환경에 맞게 다음 값을 설정합니다.

```env
DB_USERNAME=root
DB_PASSWORD=your_password

RECOMMENDATION_BASE_URL=http://localhost:8000

FLUX_API_KEY=your_api_key
```

| 변수                        | 설명                                     |
| ------------------------- | -------------------------------------- |
| `DB_USERNAME`             | MySQL 사용자명                             |
| `DB_PASSWORD`             | MySQL 비밀번호                             |
| `RECOMMENDATION_BASE_URL` | Python AI Recommendation Server 주소     |
| `FLUX_API_KEY`            | Personalized Avatar 이미지 생성을 위한 API Key |

Docker 환경에서는 MySQL Database로 `mcm_showcase`를 사용합니다.

## 핵심 도메인

```text
Member
  │
  ├── MemberWishlist
  │
  └── CustomerSession
          │
          ├── ZoneInteraction
          │
          ├── ArSession
          │      │
          │      └── ArInteraction
          │
          └── StyleProfile
                 │
                 └── TodayLook

StoreZone
  │
  └── ZoneCategory
          │
          └── Category
                  │
                  └── Product
```

### CustomerSession

고객의 한 번의 매장 방문을 나타냅니다.

회원과 비회원 모두 생성할 수 있으며 Zone Interaction, AR Session, Style Profile 등 고객 여정 데이터의 기준이 됩니다.

### ZoneInteraction

고객이 어떤 매장 Zone에 얼마나 머물렀는지를 저장합니다.

체류 시간은 고객의 Category 관심도를 판단하고 초기 추천을 만드는 신호로 활용됩니다.

### ArSession

한 번의 AR Fitting 경험을 나타냅니다.

Customer Session과 연결되며 회원, 성별, AR Interaction 등의 정보를 관리합니다.

### ArInteraction

AR Fitting 과정에서 고객이 상품에 수행한 행동을 순서대로 기록합니다.

이 행동 데이터는 실시간 추천을 갱신하는 핵심 입력으로 사용됩니다.

### StyleProfile

한 번의 쇼핑 경험에서 생성된 최종 스타일 결과입니다.

Personalized Avatar 및 Digital Closet과 연결되어 고객의 쇼핑 경험을 다음 방문까지 유지합니다.

## 주요 API

### Member

```http
POST /api/members/login
```

* 회원 로그인

### Customer Session

```http
POST /api/customer-sessions
GET  /api/customer-sessions/{customerSessionId}
```

* 매장 방문 세션 생성 및 조회

### Zone Interaction

```http
POST /api/zone-interactions
```

* Zone 입장·퇴장 및 체류 기록 저장

### AR Session

```http
POST  /api/ar-sessions
GET   /api/ar-sessions/{arSessionId}
PATCH /api/ar-sessions/{arSessionId}/member
PATCH /api/ar-sessions/{arSessionId}/gender
```

* AR Session 생성
* 회원 연결 상태 조회
* 회원 연결
* 성별 저장

### AR Interaction

```http
POST /api/ar-interactions
```

AR 과정에서 발생한 상품 행동을 저장합니다.

```text
PRODUCT_SELECT
PRODUCT_DESELECT
FITTING
WISHLIST_ADD
WISHLIST_REMOVE
```

### Recommendation

```http
GET  /api/recommendations/ar-sessions/{arSessionId}/categories/{category}

POST /api/recommendations/ar-sessions/{arSessionId}/categories/{category}/refresh
```

* Category별 추천 조회
* 최신 AR 행동을 반영한 추천 갱신

### Avatar Look

```http
POST /api/recommendations/avatar-look/{arSessionId}
```

* 전체 AR 행동을 기반으로 최종 Avatar Look 생성

### Personalized Comment

```http
POST /api/ar-sessions/{arSessionId}/messages/evaluate
```

* 현재 AR Session 행동 패턴 평가
* 조건 충족 시 Personalized Comment 반환

### Digital Closet

```http
GET   /api/my-closet?memberId={memberId}
GET   /api/my-closet/{styleProfileId}
PATCH /api/my-closet/{styleProfileId}/member
```

* 회원별 Style Profile 목록 조회
* Style Profile 상세 조회
* QR로 생성된 결과를 회원 계정에 연결

## 프로젝트 구조

```text
src/
├─ main/
│  ├─ java/
│  │  └─ likelion/mcmshowcase/
│  │     ├─ ar/
│  │     │  ├─ controller/
│  │     │  ├─ dto/
│  │     │  ├─ entity/
│  │     │  ├─ repository/
│  │     │  └─ service/
│  │     │
│  │     ├─ avatar/
│  │     │  ├─ client/
│  │     │  ├─ controller/
│  │     │  ├─ dto/
│  │     │  └─ service/
│  │     │
│  │     ├─ customer/
│  │     ├─ member/
│  │     ├─ product/
│  │     ├─ recommendation/
│  │     │  ├─ client/
│  │     │  ├─ controller/
│  │     │  ├─ dto/
│  │     │  └─ service/
│  │     │
│  │     ├─ style/
│  │     ├─ zone/
│  │     └─ McmShowcaseApplication.java
│  │
│  └─ resources/
│     ├─ application.yml
│     └─ data.sql
│
└─ test/
   └─ java/
      └─ likelion/mcmshowcase/
```

각 도메인은 기본적으로 다음 Layer 구조를 사용합니다.

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Entity / MySQL
```

외부 AI 서비스가 필요한 기능은 Client Layer를 통해 분리합니다.

```text
Controller
    ↓
Service
    ↓
RecommendationClient / FluxClient
    ↓
External API
```

## AI Recommendation 연동

Spring Backend는 고객 데이터를 직접 모델에 학습시키는 역할보다, 서비스에서 발생하는 데이터를 정리하여 Python AI Server에 전달하고 결과를 프론트엔드가 사용할 수 있는 Product 정보로 변환하는 역할을 담당합니다.

```text
Frontend
   │
   │ AR Interaction
   ▼
Spring Backend
   │
   ├─ CustomerSession 조회
   ├─ ZoneInteraction 조회
   ├─ Member Wishlist 조회
   ├─ ArInteraction 조회
   │
   ▼
Recommendation Request 생성
   │
   ▼
Python Recommendation Server
   │
   ▼
Product Ranking
   │
   ▼
Spring Product Data 결합
   │
   ▼
Frontend Response
```

이를 통해 서비스의 Business Logic과 AI Model을 분리하면서도 고객 행동이 실시간 추천에 반영될 수 있도록 구성했습니다.

## 데이터 흐름

```text
[Camera / Frontend]
        │
        ├─ Zone Interaction
        └─ AR Interaction
                │
                ▼
        [Spring Boot]
                │
        ┌───────┴────────┐
        │                │
        ▼                ▼
     [MySQL]       [Python RecRec]
        │                │
        │                ▼
        │        Personalized Ranking
        │                │
        └───────┬────────┘
                ▼
        [Spring Boot]
                │
        ├─ Recommendation
        ├─ Personalized Comment
        ├─ Avatar Look
        └─ Digital Closet
                │
                ▼
           [Frontend]
```

## 배포

Spring Boot Application은 Docker Image로 빌드하여 배포합니다.

Dockerfile은 Multi-stage build를 사용합니다.

```text
eclipse-temurin:17-jdk
        ↓
Gradle bootJar
        ↓
eclipse-temurin:17-jre
        ↓
Spring Boot Application
```

GitHub Actions를 이용하여 배포 Workflow를 관리하며, 운영 환경에서는 Docker 기반으로 Spring Boot와 MySQL 서비스를 실행합니다.

```text
GitHub
   ↓
GitHub Actions
   ↓
Docker Image
   ↓
AWS
   ↓
Docker Compose
   ↓
Spring Boot + MySQL
```

## API Documentation

Springdoc OpenAPI를 사용하여 API 명세를 제공합니다.

로컬 실행 시:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

## 해커톤에서 제안하는 가치

MCM AI Journey Backend는 고객에게 AI 기능을 별도로 사용하도록 요구하지 않습니다.

대신 고객이 매장에서 자연스럽게 남기는 **동선, 상품 탐색, 피팅, Wishlist와 같은 행동을 하나의 데이터 흐름으로 연결**하고, 이를 실시간 추천과 Personalized Avatar에 반영합니다.

또한 매장에서 끝날 수 있는 쇼핑 경험을 Digital Closet에 저장해 다음 방문까지 이어지도록 구성했습니다.

고객에게는 **“나를 이해하고 기억하는 매장”**을, 브랜드에는 **오프라인에서도 고객 취향과 관계가 지속적으로 축적되는 새로운 리테일 데이터 접점**을 제공합니다.

## TEAM

| 역할              | 담당       |
| --------------- | -------- |
| Product Manager | 이영서, 김민주 |
| Designer        | 홍지영      |
| Frontend        | 박서연, 조연우 |
| Backend         | 강성욱      |
