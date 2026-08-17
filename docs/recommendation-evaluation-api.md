# Recommendation evaluation API

Call Spring once with all personas:

```http
POST /api/evaluations/recommendations
Content-Type: application/json
```

```json
{
  "personas": [
    {
      "personaId": "P1",
      "personaType": "CONFIDENT",
      "zoneInteractions": [
        {"zone": "NEW_COLLECTION", "category": "BAG", "dwellSeconds": 55, "sequenceNo": 1},
        {"zone": "CLASSIC", "category": "BAG", "dwellSeconds": 70, "sequenceNo": 2},
        {"zone": "TRAVEL", "category": "BAG", "dwellSeconds": 245, "sequenceNo": 3},
        {"zone": "CLASSIC", "category": "BAG", "dwellSeconds": 45, "sequenceNo": 4},
        {"zone": "TRAVEL", "category": "BAG", "dwellSeconds": 130, "sequenceNo": 5}
      ],
      "arInteractions": [
        {"productId": 70, "interactionType": "PRODUCT_SELECT", "sequenceNo": 1},
        {"productId": 70, "interactionType": "PRODUCT_SELECT", "sequenceNo": 2},
        {"productId": 70, "interactionType": "FITTING", "sequenceNo": 3},
        {"productId": 70, "interactionType": "WISHLIST_ADD", "sequenceNo": 4}
      ],
      "memberWishlists": [
        {"productId": 70},
        {"productId": 50}
      ],
      "groundTruth": {
        "anchorProductId": 70,
        "category": "BAG",
        "recommendations": [
          {"productId": 62, "relevance": 5},
          {"productId": 53, "relevance": 4},
          {"productId": 49, "relevance": 3}
        ]
      }
    }
  ]
}
```

For guests, always send `"memberWishlists": []`.

Document actions must be converted before the request:

| Document action | API interaction type |
|---|---|
| fitting | `PRODUCT_SELECT` |
| retry fitting | another `PRODUCT_SELECT` event |
| TRY | `FITTING` |
| WANT | `WISHLIST_ADD` |

Repeated events must not be deduplicated. The Anchor and Ground Truth products must all belong to `groundTruth.category`, and that category must occur among the AR-interacted products. Anchor Top-1, rankings, Recall@5, NDCG@5, and confidence metrics are calculated inside that category. AR-interacted and wishlisted products remain eligible because evaluation uses `exclude_seen=false`.
