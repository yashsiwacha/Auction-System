import React, { useState, useEffect } from 'react';
import { Clock, TrendingUp, ShieldCheck, Zap } from 'lucide-react';
import { Auction } from '../types';

interface AuctionCardProps {
  auction: Auction;
  onPlaceBid: (auction: Auction) => void;
}

export const AuctionCard: React.FC<AuctionCardProps> = ({ auction, onPlaceBid }) => {
  const [timeLeft, setTimeLeft] = useState<string>('');

  useEffect(() => {
    const updateTimer = () => {
      const diff = new Date(auction.endTime).getTime() - Date.now();
      if (diff <= 0) {
        setTimeLeft('AUCTION ENDED');
        return;
      }
      const hours = Math.floor(diff / (1000 * 60 * 60));
      const mins = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
      const secs = Math.floor((diff % (1000 * 60)) / 1000);
      setTimeLeft(`${hours}h ${mins}m ${secs}s`);
    };

    updateTimer();
    const interval = setInterval(updateTimer, 1000);
    return () => clearInterval(interval);
  }, [auction.endTime]);

  return (
    <div className="glass-panel" style={{ display: 'flex', flexDirection: 'column', overflow: 'hidden', height: '100%' }}>
      {/* Product Image */}
      <div style={{ position: 'relative', width: '100%', height: '200px', backgroundColor: '#1e293b', overflow: 'hidden' }}>
        <img 
          src={auction.imageUrl || 'https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?w=600&auto=format&fit=crop&q=80'} 
          alt={auction.title}
          style={{ width: '100%', height: '100%', objectFit: 'cover', transition: 'transform 0.5s ease' }}
          onMouseOver={(e) => (e.currentTarget.style.transform = 'scale(1.05)')}
          onMouseOut={(e) => (e.currentTarget.style.transform = 'scale(1)')}
        />
        <div style={{ position: 'absolute', top: '12px', right: '12px' }}>
          <span style={{
            background: 'rgba(10, 12, 16, 0.75)',
            backdropFilter: 'blur(8px)',
            padding: '4px 10px',
            borderRadius: '20px',
            fontSize: '0.75rem',
            fontWeight: 700,
            color: '#06b6d4',
            border: '1px solid rgba(6, 182, 212, 0.3)'
          }}>
            {auction.category}
          </span>
        </div>
      </div>

      {/* Card Content */}
      <div style={{ padding: '1.25rem', display: 'flex', flexDirection: 'column', flexGrow: 1, justifyContent: 'space-between' }}>
        <div>
          <h3 style={{ fontSize: '1.15rem', fontWeight: 700, marginBottom: '0.5rem', color: '#f8fafc' }}>
            {auction.title}
          </h3>
          <p style={{ fontSize: '0.85rem', color: '#94a3b8', marginBottom: '1.25rem', lineHeight: '1.4' }}>
            {auction.description}
          </p>
        </div>

        <div>
          {/* Price details grid */}
          <div style={{
            display: 'grid',
            gridTemplateColumns: '1fr 1fr',
            gap: '12px',
            background: 'rgba(255, 255, 255, 0.03)',
            padding: '12px',
            borderRadius: '12px',
            border: '1px solid var(--border-subtle)',
            marginBottom: '1rem'
          }}>
            <div>
              <span style={{ fontSize: '0.75rem', color: '#64748b', display: 'block' }}>Highest Bid</span>
              <span style={{ fontSize: '1.2rem', fontWeight: 800, color: '#10b981' }}>
                ${auction.currentPrice.toLocaleString('en-US', { minimumFractionDigits: 2 })}
              </span>
            </div>
            <div>
              <span style={{ fontSize: '0.75rem', color: '#64748b', display: 'block' }}>Time Remaining</span>
              <div style={{ display: 'flex', alignItems: 'center', gap: '4px', marginTop: '2px' }}>
                <Clock size={14} color="#f59e0b" />
                <span style={{ fontSize: '0.85rem', fontWeight: 700, color: '#f59e0b' }}>
                  {timeLeft}
                </span>
              </div>
            </div>
          </div>

          {/* Action button & Lock Badge */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <button 
              className="btn btn-primary" 
              style={{ flexGrow: 1 }}
              onClick={() => onPlaceBid(auction)}
            >
              <Zap size={16} />
              <span>Place Instant Bid</span>
            </button>
            <div title="Protected by Redisson Distributed Lock" style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '10px', background: 'rgba(99, 102, 241, 0.1)', borderRadius: '10px', border: '1px solid rgba(99, 102, 241, 0.2)' }}>
              <ShieldCheck size={18} color="#6366f1" />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
