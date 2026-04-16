# Concurrent Bid Load Test

This load test simulates 50-100 concurrent buyers bidding on a single auction.

## Prerequisites

- Running auction application
- Existing auction in `APPROVED` status and currently active
- Buyer users in DB with continuous IDs (example: `3..102`)
- k6 installed

## Run

```bash
k6 run \
  -e BASE_URL=http://localhost:9090 \
  -e AUCTION_ID=1 \
  -e START_BIDDER_ID=3 \
  -e BIDDER_COUNT=100 \
  -e START_AMOUNT=1000 \
  -e VUS=75 \
  -e DURATION=45s \
  load-tests/concurrent-bidders.js
```

## Expected Behavior

- A large portion of requests should return `201` (accepted winning bid at that moment) or `409` (lost race / lower than latest highest).
- No `5xx` errors under normal DB conditions.
- `GET /api/auctions/{id}/highest-bid` should remain consistent and monotonic.

## Why 409 Is Valid Here

The system enforces strong consistency for each bid transaction using row-level product locks. Under concurrency, many bids become stale by the time they are evaluated. Those stale bids are rejected with `409` instead of corrupting auction state.
