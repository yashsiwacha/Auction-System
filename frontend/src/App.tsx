import React, { useState, useEffect } from 'react';
import { Navbar } from './components/Navbar';
import { AuctionCard } from './components/AuctionCard';
import { BidModal } from './components/BidModal';
import { CreateAuctionModal } from './components/CreateAuctionModal';
import { LiveFeed } from './components/LiveFeed';
import { Auction, BidEvent, User } from './types';
import { fetchAuctions, placeBidApi, createAuctionApi } from './services/api';
import { wsService } from './services/websocket';
import { Search, Filter, Server, Cpu, Database, Network } from 'lucide-react';

export default function App() {
  const [wsConnected, setWsConnected] = useState<boolean>(false);
  const [auctions, setAuctions] = useState<Auction[]>([]);
  const [liveEvents, setLiveEvents] = useState<BidEvent[]>([]);
  const [selectedAuction, setSelectedAuction] = useState<Auction | null>(null);
  const [isCreateOpen, setIsCreateOpen] = useState<boolean>(false);
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [selectedCategory, setSelectedCategory] = useState<string>('ALL');

  const [user, setUser] = useState<User>({
    id: 1,
    username: 'pro_trader',
    email: 'trader@auction.com',
    fullName: 'Alex River',
    role: 'BUYER',
    balance: 50000.00
  });

  useEffect(() => {
    fetchAuctions().then(setAuctions);

    wsService.connect(
      (bidEvent: BidEvent) => {
        setLiveEvents((prev) => [bidEvent, ...prev.slice(0, 19)]);
        setAuctions((prev) =>
          prev.map((a) =>
            a.id === bidEvent.auctionId
              ? { ...a, currentPrice: Math.max(a.currentPrice, bidEvent.amount), version: a.version + 1 }
              : a
          )
        );
      },
      (connected: boolean) => setWsConnected(connected)
    );

    return () => wsService.disconnect();
  }, []);

  const handleToggleUserRole = () => {
    setUser((prev) => ({
      ...prev,
      role: prev.role === 'BUYER' ? 'SELLER' : 'BUYER',
      username: prev.role === 'BUYER' ? 'tech_seller' : 'pro_trader'
    }));
  };

  const handlePlaceBid = async (auctionId: number, amount: number) => {
    try {
      await placeBidApi(auctionId, amount);
      // Optimistic update
      setAuctions((prev) =>
        prev.map((a) => (a.id === auctionId ? { ...a, currentPrice: amount } : a))
      );
      setLiveEvents((prev) => [
        {
          auctionId,
          bidderId: user.id,
          bidderUsername: user.username,
          amount,
          status: 'ACCEPTED',
          timestamp: new Date().toISOString()
        },
        ...prev.slice(0, 19)
      ]);
    } catch (err: any) {
      throw err;
    }
  };

  const handleCreateAuction = async (data: Partial<Auction>) => {
    const newAuction = await createAuctionApi(data);
    setAuctions((prev) => [newAuction, ...prev]);
  };

  const filteredAuctions = auctions.filter((a) => {
    const matchesSearch = a.title.toLowerCase().includes(searchQuery.toLowerCase()) || a.description.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesCat = selectedCategory === 'ALL' || a.category === selectedCategory;
    return matchesSearch && matchesCat;
  });

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <Navbar 
        wsConnected={wsConnected} 
        user={user} 
        onOpenCreateAuction={() => setIsCreateOpen(true)}
        onToggleUser={handleToggleUserRole}
      />

      <main style={{ maxWidth: '1400px', margin: '0 auto', width: '100%', padding: '0 1.5rem 3rem 1.5rem', flexGrow: 1 }}>
        {/* System Overview Dashboard Banner */}
        <section className="glass-panel" style={{ padding: '1.5rem 2rem', marginBottom: '2rem' }}>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '1.5rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
              <div style={{ background: 'rgba(99, 102, 241, 0.15)', padding: '10px', borderRadius: '10px' }}>
                <Server size={22} color="#6366f1" />
              </div>
              <div>
                <span style={{ fontSize: '0.75rem', color: '#64748b', display: 'block', fontWeight: 600 }}>BACKEND ARCHITECTURE</span>
                <span style={{ fontSize: '0.95rem', fontWeight: 700, color: '#f8fafc' }}>Spring Boot 3.3.5 / Java 21</span>
              </div>
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
              <div style={{ background: 'rgba(6, 182, 212, 0.15)', padding: '10px', borderRadius: '10px' }}>
                <Cpu size={22} color="#06b6d4" />
              </div>
              <div>
                <span style={{ fontSize: '0.75rem', color: '#64748b', display: 'block', fontWeight: 600 }}>DISTRIBUTED LOCKS</span>
                <span style={{ fontSize: '0.95rem', fontWeight: 700, color: '#06b6d4' }}>Redisson RLock & Redis 7</span>
              </div>
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
              <div style={{ background: 'rgba(16, 185, 129, 0.15)', padding: '10px', borderRadius: '10px' }}>
                <Database size={22} color="#10b981" />
              </div>
              <div>
                <span style={{ fontSize: '0.75rem', color: '#64748b', display: 'block', fontWeight: 600 }}>DATABASE & MIGRATIONS</span>
                <span style={{ fontSize: '0.95rem', fontWeight: 700, color: '#10b981' }}>PostgreSQL 16 & Flyway V1</span>
              </div>
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
              <div style={{ background: 'rgba(245, 158, 11, 0.15)', padding: '10px', borderRadius: '10px' }}>
                <Network size={22} color="#f59e0b" />
              </div>
              <div>
                <span style={{ fontSize: '0.75rem', color: '#64748b', display: 'block', fontWeight: 600 }}>EVENT STREAMING</span>
                <span style={{ fontSize: '0.95rem', fontWeight: 700, color: '#f59e0b' }}>Apache Kafka & STOMP</span>
              </div>
            </div>
          </div>
        </section>

        {/* Main Content Grid: Marketplace & Live Feed Sidebar */}
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 340px', gap: '2rem' }}>
          {/* Active Marketplace Section */}
          <div>
            {/* Filter / Search Bar */}
            <div style={{ display: 'flex', gap: '1rem', marginBottom: '1.5rem', flexWrap: 'wrap' }}>
              <div style={{ flexGrow: 1, position: 'relative', minWidth: '240px' }}>
                <Search size={18} color="#64748b" style={{ position: 'absolute', left: '14px', top: '50%', transform: 'translateY(-50%)' }} />
                <input 
                  type="text"
                  placeholder="Search live auctions by title or keywords..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  style={{
                    width: '100%',
                    padding: '12px 12px 12px 42px',
                    background: 'var(--bg-card)',
                    border: '1px solid var(--border-subtle)',
                    borderRadius: '12px',
                    color: '#fff',
                    fontSize: '0.9rem'
                  }}
                />
              </div>

              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <Filter size={16} color="#94a3b8" />
                {['ALL', 'ELECTRONICS', 'COMPUTERS', 'LUXURY'].map((cat) => (
                  <button
                    key={cat}
                    onClick={() => setSelectedCategory(cat)}
                    style={{
                      padding: '8px 14px',
                      borderRadius: '10px',
                      fontSize: '0.8rem',
                      fontWeight: 600,
                      cursor: 'pointer',
                      border: '1px solid var(--border-subtle)',
                      background: selectedCategory === cat ? 'var(--accent-primary)' : 'rgba(255, 255, 255, 0.04)',
                      color: selectedCategory === cat ? '#fff' : '#94a3b8'
                    }}
                  >
                    {cat}
                  </button>
                ))}
              </div>
            </div>

            {/* Auction Cards Grid */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: '1.5rem' }}>
              {filteredAuctions.map((auction) => (
                <AuctionCard 
                  key={auction.id} 
                  auction={auction} 
                  onPlaceBid={(a) => setSelectedAuction(a)}
                />
              ))}
            </div>
          </div>

          {/* Real-time Bid Stream Sidebar */}
          <aside style={{ height: 'calc(100vh - 240px)', position: 'sticky', top: '100px' }}>
            <LiveFeed events={liveEvents} />
          </aside>
        </div>
      </main>

      {/* Modals */}
      <BidModal 
        auction={selectedAuction}
        onClose={() => setSelectedAuction(null)}
        onSubmitBid={handlePlaceBid}
      />

      {isCreateOpen && (
        <CreateAuctionModal 
          onClose={() => setIsCreateOpen(false)}
          onCreate={handleCreateAuction}
        />
      )}
    </div>
  );
}
