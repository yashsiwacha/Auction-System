import React, { useState } from 'react';
import { X, Lock, ShieldCheck, DollarSign, CheckCircle2, AlertTriangle } from 'lucide-react';
import { Auction } from '../types';

interface BidModalProps {
  auction: Auction | null;
  onClose: () => void;
  onSubmitBid: (auctionId: number, amount: number) => Promise<void>;
}

export const BidModal: React.FC<BidModalProps> = ({ auction, onClose, onSubmitBid }) => {
  if (!auction) return null;

  const minAllowed = auction.currentPrice + 10;
  const [bidAmount, setBidAmount] = useState<number>(minAllowed);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<boolean>(false);

  const handleQuickAdd = (increment: number) => {
    setBidAmount((prev) => Math.max(minAllowed, prev + increment));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (bidAmount < minAllowed) {
      setError(`Bid must be at least $${minAllowed.toFixed(2)}`);
      return;
    }
    setError(null);
    setLoading(true);
    try {
      await onSubmitBid(auction.id, bidAmount);
      setSuccess(true);
      setTimeout(() => {
        setSuccess(false);
        onClose();
      }, 1200);
    } catch (err: any) {
      setError(err.message || 'Bid placement failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{
      position: 'fixed',
      top: 0,
      left: 0,
      right: 0,
      bottom: 0,
      backgroundColor: 'rgba(0, 0, 0, 0.75)',
      backdropFilter: 'blur(8px)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      zIndex: 1000,
      padding: '1rem'
    }}>
      <div className="glass-panel" style={{ width: '100%', maxWidth: '450px', padding: '1.75rem', position: 'relative' }}>
        <button 
          onClick={onClose}
          style={{ position: 'absolute', top: '16px', right: '16px', background: 'none', border: 'none', color: '#94a3b8', cursor: 'pointer' }}
        >
          <X size={20} />
        </button>

        <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '1rem' }}>
          <div style={{ background: 'rgba(99, 102, 241, 0.15)', padding: '10px', borderRadius: '12px' }}>
            <Lock size={20} color="#6366f1" />
          </div>
          <div>
            <h3 style={{ fontSize: '1.2rem', fontWeight: 700 }}>Place Concurrent Bid</h3>
            <span style={{ fontSize: '0.75rem', color: '#64748b' }}>REDISSON DISTRIBUTED LOCK ENABLED</span>
          </div>
        </div>

        <p style={{ fontSize: '0.9rem', color: '#94a3b8', marginBottom: '1.25rem' }}>
          Auction: <strong style={{ color: '#fff' }}>{auction.title}</strong>
        </p>

        {success ? (
          <div style={{ padding: '1.5rem', textAlign: 'center', background: 'rgba(16, 185, 129, 0.1)', borderRadius: '12px', border: '1px solid rgba(16, 185, 129, 0.3)' }}>
            <CheckCircle2 size={40} color="#10b981" style={{ margin: '0 auto 8px auto' }} />
            <h4 style={{ color: '#10b981', fontWeight: 700 }}>Bid Placed Successfully!</h4>
            <p style={{ fontSize: '0.8rem', color: '#94a3b8', marginTop: '4px' }}>Broadcasted via WebSocket & Kafka event stream</p>
          </div>
        ) : (
          <form onSubmit={handleSubmit}>
            <div style={{ marginBottom: '1rem' }}>
              <label style={{ fontSize: '0.8rem', color: '#94a3b8', display: 'block', marginBottom: '6px' }}>
                Your Bid Amount (Min: ${minAllowed.toFixed(2)})
              </label>
              <div style={{ position: 'relative' }}>
                <DollarSign size={18} color="#64748b" style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)' }} />
                <input 
                  type="number"
                  step="0.01"
                  min={minAllowed}
                  value={bidAmount}
                  onChange={(e) => setBidAmount(parseFloat(e.target.value) || minAllowed)}
                  style={{
                    width: '100%',
                    padding: '12px 12px 12px 36px',
                    background: 'rgba(0, 0, 0, 0.3)',
                    border: '1px solid var(--border-subtle)',
                    borderRadius: '10px',
                    color: '#fff',
                    fontSize: '1.1rem',
                    fontWeight: 700
                  }}
                />
              </div>
            </div>

            {/* Quick Increments */}
            <div style={{ display: 'flex', gap: '8px', marginBottom: '1.25rem' }}>
              {[10, 50, 100, 500].map((inc) => (
                <button
                  key={inc}
                  type="button"
                  onClick={() => handleQuickAdd(inc)}
                  style={{
                    flexGrow: 1,
                    padding: '6px',
                    background: 'rgba(255, 255, 255, 0.05)',
                    border: '1px solid var(--border-subtle)',
                    borderRadius: '6px',
                    color: '#94a3b8',
                    fontSize: '0.8rem',
                    fontWeight: 600,
                    cursor: 'pointer'
                  }}
                >
                  +${inc}
                </button>
              ))}
            </div>

            {error && (
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px', padding: '10px', background: 'rgba(244, 63, 94, 0.1)', borderRadius: '8px', border: '1px solid rgba(244, 63, 94, 0.3)', color: '#f43f5e', fontSize: '0.85rem', marginBottom: '1rem' }}>
                <AlertTriangle size={16} />
                <span>{error}</span>
              </div>
            )}

            <button 
              type="submit" 
              className="btn btn-primary" 
              disabled={loading}
              style={{ width: '100%', padding: '12px' }}
            >
              {loading ? 'Acquiring Lock & Submitting...' : 'Confirm & Place Bid'}
            </button>
          </form>
        )}
      </div>
    </div>
  );
};
