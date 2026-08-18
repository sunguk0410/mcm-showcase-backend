# Next-item 추천 검증 페르소나 P1~P9

## 검증 기준

- `최종 최애템 / Anchor`는 AR에서 가장 강하게 선택한 입력 상품이다.
- Anchor 자체는 다음 추천 정답이 아니며 추천 후보에서 제외한다.
- Wishlist 상품 자체도 후보에서 제외하고 취향 신호로만 사용한다.
- Ground Truth는 아직 보지 않은 다음 추천 상품만 포함한다.
- 중요도는 `3 > 2 > 1`이다.
- 이 문서와 JSON을 고정한 뒤 결과를 보고 Ground Truth를 바꾸지 않는다.

## 페르소나 요약

| P | Persona | 고객 설정 | 선호 특성 | 최종 최애템 / Anchor |
| --- | --- | --- | --- | --- |
| **P1** | **Travel × 패션 탐색형** | 여행용 제품을 찾지만 실용성만큼 디자인도 중요. Travel 안에서 여러 스타일과 카테고리를 탐색 | **Travel ↑ / Refined·Experimental / 카테고리 확장형** | **#70 Ottomar 다이아몬드 퀼팅 레더 위켄더** |
| **P2** | **New × 패션 탐색형** | 신상품과 컬래버레이션을 적극적으로 발견하고 새로운 디자인을 넓게 탐색 | **New ↑ / Bold·Experimental / 다양한 카테고리** | **#46 MCM x ATEEZ Ella 보스턴** |
| **P3** | **Classic × 패션 탐색형** | MCM다운 모노그램과 Visetos를 좋아하지만 가방뿐 아니라 전체 룩을 탐색 | **Classic ↑ / Classic·Refined / BAG+의류+신발** | **#40 Aren 비세토스 E/W 숄더백** |
| **P4** | **Travel × 확신 구매형·회원** | 여행 준비 목적이 명확하고 온라인에서도 Travel 상품을 저장한 뒤 방문 | **Travel 매우 강함 / Minimal·Urban / 소수 상품 집중** | **#72 Aren Nova 모노그램 ECONYL 위켄더** |
| **P5** | **New × 확신 구매형** | 신상품 하나에 강하게 끌리면 여러 상품을 비교하지 않고 빠르게 TRY/WANT | **New 매우 강함 / Bold·Experimental / 유사 제품 집중** | **#60 Pina 스터드 장식 탬버린 백** |
| **P6** | **Classic × 확신 구매형** | MCM 대표 디자인을 이미 알고 있고 익숙한 Classic 가방 구매가 목적 | **Classic 매우 강함 / Classic·Refined / BAG 집중** | **#64 Aren 비세토스 듀오 호보** |
| **P7** | **Travel × 탐색형** | 여행 관련 니즈는 있지만 구매 상품은 정하지 못해 여러 Zone을 비교 | **Travel 우세 / Minimal·Urban / 낮은 행동 강도** | **#42 Aren ECONYL 드로우스트링 백팩** |
| **P8** | **New × 탐색형·회원** | 과거에는 Classic 제품을 저장했지만 오늘은 New 제품을 흥미롭게 탐색 | **현재 New > 과거 Classic / Classic 취향이 섞인 New** | **#55 Diamond 비세토스 레더 믹스 숄더백** |
| **P9** | **Classic × 탐색형** | 뚜렷한 목적 없이 둘러보지만 반복적으로 Classic·Visetos에 돌아옴 | **Classic 우세 / Classic·Urban·Refined / 낮은 행동 강도** | **#57 Aren 비세토스 스쿨 토트** |

## P1. Travel × 패션 탐색형

- Persona Type: `EXPLORATORY`
- 회원 여부: 비회원
- 핵심 동선: Travel에서 BAG·ACCESSORIES·SHOES·BOTTOM·TOP 탐색
- AR Anchor: #70 Ottomar 다이아몬드 퀼팅 레더 위켄더

| Relevance | Next-item Ground Truth | 선정 이유 |
| ---: | --- | --- |
| 3 | #52 Ottomar 비세토스 오토마 위켄더 | 동일 Travel·Ottomar 계열의 디자인 확장 |
| 2 | #120 Ottomar 비세토스 러기지 태그 | 여행 가방과 직접 연결되는 액세서리 |
| 1 | #19 ECONYL 모노그램 프린트 쇼츠 | Travel 관심을 의류로 확장 |

## P2. New × 패션 탐색형

- Persona Type: `EXPLORATORY`
- 회원 여부: 비회원
- 핵심 동선: New의 BAG·TOP·BOTTOM·ACCESSORIES·SHOES를 넓게 탐색
- AR Anchor: #46 MCM x ATEEZ Ella 보스턴

| Relevance | Next-item Ground Truth | 선정 이유 |
| ---: | --- | --- |
| 3 | #81 MCM x ATEEZ with Mingi 코튼 로고 티셔츠 | ATEEZ 협업 관심을 TOP으로 확장 |
| 2 | #114 MCM x ATEEZ with MINGI 로고 캡 | ATEEZ 협업 관심을 ACCESSORIES로 확장 |
| 1 | #66 MCM x ATEEZ Toni 탑 지퍼 쇼퍼 | 같은 협업 라인의 BAG 후보 |

## P3. Classic × 패션 탐색형

- Persona Type: `EXPLORATORY`
- 회원 여부: 비회원
- 핵심 동선: Classic의 BAG·TOP·BOTTOM·SHOES·ACCESSORIES 전체 탐색
- AR Anchor: #40 Aren 비세토스 E/W 숄더백

| Relevance | Next-item Ground Truth | 선정 이유 |
| ---: | --- | --- |
| 3 | #64 Aren 비세토스 듀오 호보 | Classic·Aren 가방 확장 |
| 2 | #84 모노그램 플록 포켓 웨스턴 셔츠 | Classic 전체 룩의 TOP 후보 |
| 1 | #125 비세토스 네오 터레인 로 스니커즈 | Visetos 룩을 완성하는 SHOES 후보 |

## P4. Travel × 확신 구매형·회원

- Persona Type: `CONFIDENT`
- Wishlist: #70 Ottomar 다이아몬드 퀼팅 레더 위켄더, #121 Ottomar 비세토스 여권 케이스
- 핵심 동선: Travel BAG에 매우 집중
- AR Anchor: #72 Aren Nova 모노그램 ECONYL 위켄더

| Relevance | Next-item Ground Truth | 선정 이유 |
| ---: | --- | --- |
| 3 | #52 Ottomar 비세토스 오토마 위켄더 | Wishlist 취향과 위켄더 구매 목적을 반영 |
| 2 | #69 Ottomar 캐빈 트롤리 | 여행 목적이 뚜렷한 상위 러기지 후보 |
| 1 | #120 Ottomar 비세토스 러기지 태그 | Travel BAG과 연결되는 액세서리 |

## P5. New × 확신 구매형

- Persona Type: `CONFIDENT`
- 회원 여부: 비회원
- 핵심 동선: New BAG에 집중하고 빠르게 WANT까지 진행
- AR Anchor: #60 Pina 스터드 장식 탬버린 백

| Relevance | Next-item Ground Truth | 선정 이유 |
| ---: | --- | --- |
| 3 | #59 Ella 디스코 비세토스 보스턴 | New·Bold 성향의 직접적인 유사 후보 |
| 2 | #75 Aren 맥시 모노그램 레더 E/W 숄더백 | 강한 디자인의 New BAG 확장 |
| 1 | #61 Diamond 카프 레더 숄더백 | New 숄더백 비교 후보 |

## P6. Classic × 확신 구매형

- Persona Type: `CONFIDENT`
- 회원 여부: 비회원
- 핵심 동선: Classic BAG에 매우 집중
- AR Anchor: #64 Aren 비세토스 듀오 호보

| Relevance | Next-item Ground Truth | 선정 이유 |
| ---: | --- | --- |
| 3 | #40 Aren 비세토스 E/W 숄더백 | Classic·Aren·Cognac 유사 상품 |
| 2 | #73 Aren 트라이앵글 크로스바디 | Classic·Aren 가방 확장 |
| 1 | #38 Dia 비세토스 레더 믹스 쇼퍼 | Classic Cognac BAG 비교 후보 |

## P7. Travel × 탐색형

- Persona Type: `EXPLORATORY`
- 회원 여부: 비회원
- 핵심 동선: Travel이 우세하지만 New와 Classic도 비교
- AR Anchor: #42 Aren ECONYL 드로우스트링 백팩
- 행동 강도: PRODUCT_SELECT 1회

| Relevance | Next-item Ground Truth | 선정 이유 |
| ---: | --- | --- |
| 3 | #62 Stark 사이드 스터드 비세토스 백팩 | Travel Backpack 비교 후보 |
| 2 | #120 Ottomar 비세토스 러기지 태그 | 여행 액세서리 확장 |
| 1 | #19 ECONYL 모노그램 프린트 쇼츠 | ECONYL·Travel 의류 확장 |

## P8. New × 탐색형·회원

- Persona Type: `EXPLORATORY`
- Wishlist: #40 Aren 비세토스 E/W 숄더백, #125 비세토스 네오 터레인 로 스니커즈
- 현재 동선: New의 모든 카테고리를 넓게 탐색
- AR Anchor: #55 Diamond 비세토스 레더 믹스 숄더백

| Relevance | Next-item Ground Truth | 선정 이유 |
| ---: | --- | --- |
| 3 | #61 Diamond 카프 레더 숄더백 | Anchor의 Diamond·New BAG 관심 반영 |
| 2 | #90 모노그램 레더 벨트 데님 재킷 | Classic 취향이 섞인 New TOP 후보 |
| 1 | #126 Federlite 퀼팅 가죽 로우탑 슬립온 스니커즈 | New 전체 룩을 완성하는 SHOES 후보 |

## P9. Classic × 탐색형

- Persona Type: `EXPLORATORY`
- 회원 여부: 비회원
- 핵심 동선: 여러 카테고리를 보지만 Classic BAG에 반복적으로 복귀
- AR Anchor: #57 Aren 비세토스 스쿨 토트
- 행동 강도: PRODUCT_SELECT 1회

| Relevance | Next-item Ground Truth | 선정 이유 |
| ---: | --- | --- |
| 3 | #40 Aren 비세토스 E/W 숄더백 | Classic·Aren·Visetos 핵심 선호 |
| 2 | #125 비세토스 네오 터레인 로 스니커즈 | Classic Visetos를 SHOES로 확장 |
| 1 | #115 M ART 비세토스 벨트 1인치 | Classic Visetos 액세서리 확장 |

## API 실행

```http
POST /api/evaluations/recommendations
Content-Type: application/json
```

요청 본문은 `docs/recommendation-evaluation-p1-p9.json`을 사용한다.
