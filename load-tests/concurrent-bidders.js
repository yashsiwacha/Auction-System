import http from 'k6/http';
import { check, sleep } from 'k6';

const baseUrl = __ENV.BASE_URL || 'http://localhost:9090';
const auctionId = Number(__ENV.AUCTION_ID || 1);
const startBidderId = Number(__ENV.START_BIDDER_ID || 3);
const bidderCount = Number(__ENV.BIDDER_COUNT || 100);
const startAmount = Number(__ENV.START_AMOUNT || 1000);

export const options = {
  scenarios: {
    concurrent_bidders: {
      executor: 'constant-vus',
      vus: Number(__ENV.VUS || 75),
      duration: __ENV.DURATION || '45s',
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<800'],
    http_req_failed: ['rate<0.2'],
  },
};

function nextBidderId() {
  const offset = (__VU + __ITER) % bidderCount;
  return startBidderId + offset;
}

function nextBidAmount() {
  // High-variance bid values create realistic contention and 409 conflicts.
  return startAmount + (__ITER * 2) + (__VU * 7) + Math.random();
}

export default function () {
  const bidderId = nextBidderId();
  const bidAmount = Number(nextBidAmount().toFixed(2));

  const payload = JSON.stringify({
    bidderId,
    bidAmount,
  });

  const placeBidRes = http.post(
    `${baseUrl}/api/auctions/${auctionId}/bids`,
    payload,
    {
      headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json',
      },
      timeout: '10s',
    }
  );

  check(placeBidRes, {
    'place bid responded': (r) => r.status === 201 || r.status === 409,
    'no server error on bid': (r) => r.status < 500,
  });

  const highestRes = http.get(`${baseUrl}/api/auctions/${auctionId}/highest-bid`, {
    headers: { Accept: 'application/json' },
  });

  check(highestRes, {
    'highest bid endpoint ok': (r) => r.status === 200,
  });

  sleep(0.1);
}
