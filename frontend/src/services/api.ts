import { Auction, Bid, User } from '../types';

const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1';

export async function fetchAuctions(): Promise<Auction[]> {
  try {
    const res = await fetch(`${API_BASE}/auctions`);
    if (!res.ok) throw new Error('Failed to fetch auctions');
    return await res.json();
  } catch (e) {
    console.warn('[API] Backend fallback mock data active');
    return mockAuctions;
  }
}

export async function placeBidApi(auctionId: number, amount: number, token?: string): Promise<Bid> {
  const headers: Record<string, string> = { 'Content-Type': 'application/json' };
  if (token) headers['Authorization'] = `Bearer ${token}`;

  const idempotencyKey = `bid-${auctionId}-${Date.now()}-${Math.random().toString(36).substring(2, 9)}`;
  headers['X-Idempotency-Key'] = idempotencyKey;

  const res = await fetch(`${API_BASE}/bids`, {
    method: 'POST',
    headers,
    body: JSON.stringify({ auctionId, amount })
  });

  if (!res.ok) {
    const errData = await res.json().catch(() => ({ message: 'Bid placement failed' }));
    throw new Error(errData.message || 'Bid placement failed');
  }

  return await res.json();
}

export async function createAuctionApi(data: Partial<Auction>, token?: string): Promise<Auction> {
  const headers: Record<string, string> = { 'Content-Type': 'application/json' };
  if (token) headers['Authorization'] = `Bearer ${token}`;

  const res = await fetch(`${API_BASE}/auctions`, {
    method: 'POST',
    headers,
    body: JSON.stringify(data)
  });

  if (!res.ok) {
    throw new Error('Failed to create auction');
  }

  return await res.json();
}

const mockAuctions: Auction[] = [
  {
    id: 1,
    title: 'NVIDIA RTX 4090 Founders Edition',
    description: 'Brand new, sealed in box flagship 24GB GDDR6X graphics card.',
    category: 'ELECTRONICS',
    startingPrice: 1200.00,
    currentPrice: 1550.00,
    sellerId: 2,
    status: 'ACTIVE',
    version: 4,
    startTime: new Date(Date.now() - 3600000).toISOString(),
    endTime: new Date(Date.now() + 86400000).toISOString(),
    imageUrl: 'https://images.unsplash.com/photo-1591488320449-011701bb6704?w=600&auto=format&fit=crop&q=80'
  },
  {
    id: 2,
    title: 'Vintage Rolex Submariner 1984',
    description: 'Rare vintage luxury timepiece with full papers and original box.',
    category: 'LUXURY',
    startingPrice: 8500.00,
    currentPrice: 10200.00,
    sellerId: 3,
    status: 'ACTIVE',
    version: 8,
    startTime: new Date(Date.now() - 7200000).toISOString(),
    endTime: new Date(Date.now() + 43200000).toISOString(),
    imageUrl: 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600&auto=format&fit=crop&q=80'
  },
  {
    id: 3,
    title: 'Apple MacBook Pro 16" M3 Max 64GB',
    description: 'Space Black top-tier performance laptop for developers.',
    category: 'COMPUTERS',
    startingPrice: 2800.00,
    currentPrice: 3150.00,
    sellerId: 2,
    status: 'ACTIVE',
    version: 5,
    startTime: new Date(Date.now() - 1800000).toISOString(),
    endTime: new Date(Date.now() + 172800000).toISOString(),
    imageUrl: 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=600&auto=format&fit=crop&q=80'
  }
];
