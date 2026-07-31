import React from 'react';
import { Gavel, Radio, PlusCircle, User, ShieldCheck, Wallet } from 'lucide-react';
import { User as UserType } from '../types';

interface NavbarProps {
  wsConnected: boolean;
  user: UserType | null;
  onOpenCreateAuction: () => void;
  onToggleUser: () => void;
}

export const Navbar: React.FC<NavbarProps> = ({ wsConnected, user, onOpenCreateAuction, onToggleUser }) => {
  return (
    <header className="glass-panel" style={{ borderRadius: '0 0 16px 16px', margin: '0 0 2rem 0', padding: '1rem 2rem' }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        {/* Brand logo */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <div style={{
            background: 'linear-gradient(135deg, #6366f1 0%, #06b6d4 100%)',
            padding: '10px',
            borderRadius: '12px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            boxShadow: '0 0 20px rgba(99, 102, 241, 0.4)'
          }}>
            <Gavel size={24} color="#fff" />
          </div>
          <div>
            <h1 style={{ fontSize: '1.4rem', fontWeight: 800, background: 'linear-gradient(90deg, #fff, #94a3b8)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
              AUCTION<span style={{ color: '#6366f1', WebkitTextFillColor: '#6366f1' }}>NEXUS</span>
            </h1>
            <p style={{ fontSize: '0.75rem', color: '#64748b', fontWeight: 600, letterSpacing: '0.05em' }}>
              ENTERPRISE DISTRIBUTED SYSTEM
            </p>
          </div>
        </div>

        {/* Live System Status & Action Controls */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '1.5rem' }}>
          {/* WebSocket Status Indicator */}
          <div className="pulse-badge" style={{
            background: wsConnected ? 'rgba(16, 185, 129, 0.12)' : 'rgba(244, 63, 94, 0.12)',
            borderColor: wsConnected ? 'rgba(16, 185, 129, 0.3)' : 'rgba(244, 63, 94, 0.3)',
            color: wsConnected ? '#10b981' : '#f43f5e'
          }}>
            <Radio size={14} className={wsConnected ? 'animate-pulse' : ''} />
            <span>{wsConnected ? 'LIVE WS CONNECTED' : 'WS RECONNECTING'}</span>
          </div>

          {/* Create Auction Button */}
          {user?.role === 'SELLER' && (
            <button className="btn btn-primary" onClick={onOpenCreateAuction}>
              <PlusCircle size={18} />
              <span>Create Auction</span>
            </button>
          )}

          {/* User Balance Card */}
          {user && (
            <div style={{
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              background: 'rgba(255, 255, 255, 0.04)',
              padding: '6px 14px',
              borderRadius: '10px',
              border: '1px solid var(--border-subtle)'
            }}>
              <Wallet size={16} color="#06b6d4" />
              <span style={{ fontSize: '0.85rem', color: '#94a3b8' }}>Balance:</span>
              <span style={{ fontSize: '0.95rem', fontWeight: 700, color: '#10b981' }}>
                ${user.balance.toLocaleString('en-US', { minimumFractionDigits: 2 })}
              </span>
            </div>
          )}

          {/* User Profile Switcher */}
          <button className="btn btn-secondary" onClick={onToggleUser} style={{ gap: '8px' }}>
            <User size={18} color="#6366f1" />
            <span style={{ fontSize: '0.9rem' }}>{user ? user.username : 'Switch Role'}</span>
            <ShieldCheck size={14} color="#10b981" />
          </button>
        </div>
      </div>
    </header>
  );
};
