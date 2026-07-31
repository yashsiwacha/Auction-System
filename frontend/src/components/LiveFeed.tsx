import React from 'react';
import { Activity, ArrowUpRight } from 'lucide-react';
import { BidEvent } from '../types';

interface LiveFeedProps {
  events: BidEvent[];
}

export const LiveFeed: React.FC<LiveFeedProps> = ({ events }) => {
  return (
    <div className="glass-panel" style={{ padding: '1.25rem', height: '100%', display: 'flex', flexDirection: 'column' }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <Activity size={18} color="#06b6d4" />
          <h3 style={{ fontSize: '1.05rem', fontWeight: 700 }}>Real-Time Bid Stream</h3>
        </div>
        <span style={{ fontSize: '0.75rem', color: '#64748b', fontWeight: 600 }}>STOMP / KAFKA</span>
      </div>

      <div className="custom-scrollbar" style={{ flexGrow: 1, overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: '10px' }}>
        {events.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '2rem 1rem', color: '#64748b', fontSize: '0.85rem' }}>
            Listening for incoming bids across distributed nodes...
          </div>
        ) : (
          events.map((ev, idx) => (
            <div 
              key={idx} 
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                padding: '10px 12px',
                background: 'rgba(255, 255, 255, 0.03)',
                borderRadius: '10px',
                border: '1px solid var(--border-subtle)',
                animation: 'fadeIn 0.3s ease'
              }}
            >
              <div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                  <span style={{ fontSize: '0.85rem', fontWeight: 700, color: '#f8fafc' }}>
                    {ev.bidderUsername || `Bidder #${ev.bidderId}`}
                  </span>
                  <span style={{ fontSize: '0.7rem', color: '#10b981', background: 'rgba(16, 185, 129, 0.1)', padding: '2px 6px', borderRadius: '4px' }}>
                    {ev.status}
                  </span>
                </div>
                <span style={{ fontSize: '0.75rem', color: '#64748b' }}>
                  Auction #{ev.auctionId} • {new Date(ev.timestamp).toLocaleTimeString()}
                </span>
              </div>

              <div style={{ display: 'flex', alignItems: 'center', gap: '4px', color: '#10b981', fontWeight: 800, fontSize: '0.95rem' }}>
                <span>${ev.amount.toFixed(2)}</span>
                <ArrowUpRight size={14} />
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
};
