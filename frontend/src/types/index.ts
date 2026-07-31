export type UserRole = 'BUYER' | 'SELLER' | 'ADMIN';

export interface User {
  id: number;
  username: string;
  email: string;
  fullName: string;
  role: UserRole;
  balance: number;
  token?: string;
}

export type AuctionStatus = 'PENDING' | 'ACTIVE' | 'COMPLETED' | 'CANCELLED';

export interface Auction {
  id: number;
  title: string;
  description: string;
  category: string;
  startingPrice: number;
  currentPrice: number;
  sellerId: number;
  winnerId?: number;
  status: AuctionStatus;
  version: number;
  startTime: string;
  endTime: string;
  imageUrl?: string;
}

export type BidStatus = 'ACCEPTED' | 'REJECTED' | 'OUTBID' | 'WINNING';

export interface Bid {
  id: number;
  auctionId: number;
  bidderId: number;
  bidderUsername?: string;
  amount: number;
  status: BidStatus;
  timestamp: string;
}

export interface BidEvent {
  auctionId: number;
  bidderId: number;
  bidderUsername: string;
  amount: number;
  status: BidStatus;
  timestamp: string;
}
