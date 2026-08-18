# Next-item 추천 평가 페르소나 P1~P9

## 평가 목적

매장 Zone 체류, AR 상품 행동, 회원 Wishlist를 조합했을 때 모델이 다음으로 적절한 상품을 추천하는지 검증한다.

- AR에서 이미 본 상품은 추천 후보에서 제외한다.
- Wishlist 상품 자체도 추천 후보에서 제외하되 유사 상품을 찾는 선호 신호로 사용한다.
- Ground Truth에는 다음 추천으로 기대하는 상품만 넣는다.
- 중요도는 `3 > 2 > 1`로 표현한다.
- AR 행동이 2개 이하면 `AI 70% + Preference 30%`를 적용한다.
- 회원의 Preference 30%는 `Zone 21% + Wishlist 9%`로 구성된다.
- 비회원의 Preference 30%는 모두 Zone 점수로 사용한다.
- AR 행동 3~5개는 AI 75%, 6개 이상은 AI 80%를 적용한다.
- EXPLORATORY 전체 추천은 Top 5에서 카테고리당 최대 2개를 허용하고 보완 카테고리에 가점을 준다.
- AR·Wishlist 상품과 후보 상품의 제품군 키워드, 세부 카테고리, 색상, Zone 유사도를 콘텐츠 보조 점수로 반영한다.

## 전체 구성

| ID | 유형 | 회원 | 핵심 관심 | AR 입력 | 평가 포인트 |
| --- | --- | --- | --- | --- | --- |
| P1 | CONFIDENT | 회원 | Travel BAG | #43 | Travel 가방과 Wishlist 결합 |
| P2 | CONFIDENT | 회원 | New TOP·Collaboration | #76 | 협업 컬렉션 prior 반영 |
| P3 | EXPLORATORY | 비회원 | Classic 중심 다중 카테고리 | #47 | 유사 Classic 가방 순위 |
| P4 | CONFIDENT | 비회원 | Travel 러기지 | #69 | 여행 가방에서 여행용품 확장 |
| P5 | CONFIDENT | 회원 | New·Bold·Collaboration BAG | #60 | ATEEZ Wishlist와 statement bag 결합 |
| P6 | CONFIDENT | 회원 | Classic Cognac BAG | #64 | Wishlist와 소재·컬러 유사성 |
| P7 | EXPLORATORY | 비회원 | Travel BAG·ACCESSORIES | #49, #120 | 여행 소품 교차 추천 |
| P8 | EXPLORATORY | 회원 | New 풀룩 | #55, #94 | 가방·하의에서 상의·벨트·신발 확장 |
| P9 | EXPLORATORY | 비회원 | Classic 풀룩 | #57, #125 | 가방·신발에서 의류·벨트 확장 |

---

## P1. Travel BAG Cold Start 회원

### 시나리오

온라인에서 Weekender 두 개를 저장했고, 매장에서는 Travel Zone과 BAG을 가장 오래 탐색했다. AR에서 Travel Backpack #43을 선택하고 피팅했다.

### 주요 신호

- Wishlist: #70 Ottomar 다이아몬드 퀼팅 레더 위켄더
- Wishlist: #50 Ottomar 비세토스 위켄더
- Zone 합계: New 55초 / Classic 115초 / Travel 375초
- BAG 체류: Travel 195초 / Classic 95초
- AR: #43 Aren 비세토스 드로우스트링 백팩 선택·피팅
- 가중치: AI 70% / Zone 21% / Wishlist 9%

### Ground Truth

| 중요도 | 상품 | 선정 이유 |
| ---: | --- | --- |
| 3 | #52 Ottomar 비세토스 오토마 위켄더 | Travel BAG과 Weekender Wishlist를 모두 만족 |
| 2 | #62 Stark 사이드 스터드 비세토스 백팩 | AR의 Travel Backpack 관심과 직접 연결 |
| 1 | #72 Aren Nova 모노그램 ECONYL 위켄더 | Travel·Aren·Weekender 신호를 함께 반영 |

## P2. New Collaboration Cold Start 회원

### 시나리오

온라인에서 ATEEZ 협업 가방을 저장했고, 매장에서는 New Zone을 반복 방문했다. AR에서 화려한 New 티셔츠 #76을 선택하고 피팅했다.

### 주요 신호

- Wishlist: #46 MCM x ATEEZ Ella 보스턴
- Zone 합계: New 330초 / Classic 60초 / Travel 45초
- New TOP 체류: 110초
- AR: #76 디스코 시퀸 티셔츠 선택·피팅
- 가중치: AI 70% / Zone 21% / Wishlist 9%

### Ground Truth

| 중요도 | 상품 | 선정 이유 |
| ---: | --- | --- |
| 3 | #81 MCM x ATEEZ with Mingi 코튼 로고 티셔츠 | TOP 행동과 ATEEZ Wishlist를 동시에 만족 |
| 2 | #80 디스코 패치 티셔츠 | #76의 Disco·New TOP 성향과 직접 연결 |
| 1 | #114 MCM x ATEEZ with MINGI 로고 캡 | 협업 관심을 액세서리로 확장 |

## P3. Classic 다중 탐색 비회원

### 시나리오

여러 카테고리를 둘러보지만 Classic Zone 체류가 가장 길다. AR에서는 Classic BAG #47을 처음 선택하고 피팅했다.

### 주요 신호

- Zone 합계: New 70초 / Classic 320초 / Travel 50초
- Classic에서 BAG·TOP·BOTTOM·SHOES·ACCESSORIES를 고르게 탐색
- AR: #47 Aren 비세토스 숄더백 선택·피팅
- 가중치: AI 70% / Zone 30%

### Ground Truth

| 중요도 | 상품 | 선정 이유 |
| ---: | --- | --- |
| 3 | #40 Aren 비세토스 E/W 숄더백 | Classic·Aren·숄더백 속성이 가장 직접적 |
| 2 | #64 Aren 비세토스 듀오 호보 | Classic Aren 계열의 인접 실루엣 |
| 1 | #57 Aren 비세토스 스쿨 토트 | Classic Aren BAG 관심의 확장 후보 |

## P4. Travel 러기지 집중 비회원

### 시나리오

Travel Zone에서 가방과 여행 액세서리에 집중하고 AR에서 캐빈 트롤리를 피팅한 출장·여행 목적 고객이다.

### 주요 신호

- Travel BAG 체류: 285초
- Travel ACCESSORIES 체류: 100초
- AR: #69 Ottomar 캐빈 트롤리 선택·피팅
- 가중치: AI 70% / Zone 30%

### Ground Truth

| 중요도 | 상품 | 선정 이유 |
| ---: | --- | --- |
| 3 | #12 Ottomar 비세토스 가먼트 백 | 캐빈 트롤리와 직접 보완되는 출장용 가방 |
| 2 | #52 Ottomar 비세토스 오토마 위켄더 | 동일 Ottomar Travel 러기지 확장 |
| 1 | #120 Ottomar 비세토스 러기지 택 | 트롤리와 결합되는 대표 여행 액세서리 |

## P5. New Statement BAG 회원

### 시나리오

ATEEZ 협업 가방을 저장했고 New Zone의 개성 강한 가방을 집중 탐색한다. AR에서는 스터드 장식 탬버린 백을 피팅했다.

### 주요 신호

- Wishlist: #66 MCM x ATEEZ Toni 탑 지퍼 쇼퍼
- New BAG 체류: 250초
- AR: #60 Pina 스터드 장식 탬버린 백 선택·피팅
- 가중치: AI 70% / Zone 21% / Wishlist 9%

### Ground Truth

| 중요도 | 상품 | 선정 이유 |
| ---: | --- | --- |
| 3 | #46 MCM x ATEEZ Ella 보스턴 | Wishlist의 ATEEZ 협업 관심을 가장 직접적으로 반영 |
| 2 | #59 Ella 디스코 비세토스 보스턴 | Disco와 statement 디자인 연결 |
| 1 | #75 Aren 맥시 모노그램 레더 E/W 숄더백 | New Zone의 강한 디자인·컬러 확장 |

## P6. Classic Cognac BAG 회원

### 시나리오

클래식한 Cognac 가방을 선호하며, Aren 호보를 피팅하고 Aren 숄더백을 Wishlist에 저장한 고객이다.

### 주요 신호

- Wishlist: #40 Aren 비세토스 E/W 숄더백
- Classic BAG 체류: 285초
- AR: #64 Aren 비세토스 듀오 호보 선택·피팅
- 가중치: AI 70% / Zone 21% / Wishlist 9%

### Ground Truth

| 중요도 | 상품 | 선정 이유 |
| ---: | --- | --- |
| 3 | #73 Aren 트라이앵글 크로스바디 | Classic·Cognac·Aren 속성을 공유 |
| 2 | #38 Dia 비세토스 레더 믹스 쇼퍼 | Classic Cognac BAG의 실용적 확장 |
| 1 | #67 Aren 맥시 모노그램 레더 체인 크로스바디 | Wishlist의 Aren 관심을 다른 실루엣으로 확장 |

## P7. Travel 소품 탐색 비회원

### 시나리오

여행용 소형 가방과 러기지 액세서리를 함께 살펴본다. AR에서 Travel 파우치와 러기지 태그를 연속 선택했다.

### 주요 신호

- Travel BAG 체류: 135초
- Travel ACCESSORIES 체류: 70초
- Travel BOTTOM 체류: 45초
- AR: #49 Aren 비세토스 크로스바디 파우치 선택·피팅
- AR: #120 Ottomar 비세토스 러기지 택 선택
- 행동 3개로 AI 75% / Zone 25%

### Ground Truth

| 중요도 | 상품 | 선정 이유 |
| ---: | --- | --- |
| 3 | #123 Ottomar 비세토스 트래블 파우치 | 파우치와 여행 액세서리 관심을 동시에 만족 |
| 2 | #121 Ottomar 비세토스 여권 케이스 | 러기지 태그와 직접 연결되는 여행 소품 |
| 1 | #19 ECONYL 모노그램 프린트 쇼츠 | Travel 관심을 의류로 확장하는 탐색 후보 |

## P8. New 풀룩 탐색 회원

### 시나리오

New Zone에서 가방, 하의, 상의, 액세서리, 신발을 고르게 탐색한다. AR에서 가방과 팬츠를 피팅했고 선글라스를 저장했다.

### 주요 신호

- Wishlist: #107 지오메트릭 쉴드 선글라스
- New 카테고리 체류: BAG 70 / BOTTOM 65 / TOP 55 / ACCESSORIES 45 / SHOES 40초
- AR: #55 Diamond 비세토스 레더 믹스 숄더백 선택·피팅
- AR: #94 루렉스 데님 플레어 팬츠 선택·피팅
- 행동 4개로 AI 75% / Zone 17.5% / Wishlist 7.5%

### Ground Truth

| 중요도 | 상품 | 선정 이유 |
| ---: | --- | --- |
| 3 | #90 모노그램 레더 벨트 데님 재킷 | #94 데님 팬츠와 연결되는 New 셋업 후보 |
| 2 | #116 Aren 스터드 가죽 벨트 | 가방·하의 룩을 완성하는 New 액세서리 |
| 1 | #126 Federlite 퀼팅 가죽 로우탑 슬립온 스니커즈 | New 풀룩을 완성하는 신발 후보 |

## P9. Classic 풀룩 탐색 비회원

### 시나리오

Classic Zone에서 가방, 신발, 상의, 하의, 액세서리를 고르게 탐색한다. AR에서는 Classic 토트와 Cognac 스니커즈를 피팅했다.

### 주요 신호

- Classic 체류: BAG 65 / SHOES 55 / TOP 85 / BOTTOM 45 / ACCESSORIES 40초
- AR: #57 Aren 비세토스 스쿨 토트 선택·피팅
- AR: #125 비세토스 네오 터레인 로 스니커즈 선택·피팅
- 행동 4개로 AI 75% / Zone 25%

### Ground Truth

| 중요도 | 상품 | 선정 이유 |
| ---: | --- | --- |
| 3 | #84 모노그램 플록 포켓 웨스턴 셔츠 | Classic 풀룩의 상의 후보 |
| 2 | #103 울 트윌 모노그램 팬츠 | Classic 상의·신발과 조합 가능한 하의 |
| 1 | #115 M ART 비세토스 벨트 1인치 | Cognac 신발과 연결되는 마무리 액세서리 |

## API 실행

```http
POST /api/evaluations/recommendations
Content-Type: application/json
```

요청 본문은 `docs/recommendation-evaluation-p1-p9.json`을 사용한다.

## 해석 시 주의사항

- P1~P6는 동일 Zone 또는 동일 카테고리 중심으로 상대적으로 쉬운 시나리오다.
- P7~P9는 보완 상품과 풀룩을 요구하므로 더 어려운 교차 카테고리 시나리오다.
- Ground Truth는 현재 모델 출력이 아니라 카탈로그 속성과 비즈니스 시나리오로 정의했다.
- 따라서 P7~P9가 낮게 나오면 평가 데이터 오류보다는 모델이 교차 카테고리 관계를 충분히 학습하지 못했다는 신호로 해석한다.
- `personaType`은 평가 그룹 라벨이며 현재 추천 점수 계산에는 직접 사용되지 않는다.
