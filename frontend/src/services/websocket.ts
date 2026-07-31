import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { BidEvent } from '../types';

export class WebSocketService {
  private client: Client | null = null;
  private isConnected: boolean = false;

  public connect(onBidEvent: (event: BidEvent) => void, onStatusChange: (connected: boolean) => void) {
    const socketUrl = import.meta.env.VITE_API_URL 
      ? `${import.meta.env.VITE_API_URL}/ws-auction` 
      : 'http://localhost:8080/ws-auction';

    this.client = new Client({
      webSocketFactory: () => new SockJS(socketUrl),
      reconnectDelay: 3000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        this.isConnected = true;
        onStatusChange(true);
        console.log('[WebSocket] Connected to STOMP Broker');

        this.client?.subscribe('/topic/auctions/bids', (message) => {
          if (message.body) {
            try {
              const event: BidEvent = JSON.parse(message.body);
              onBidEvent(event);
            } catch (e) {
              console.error('[WebSocket] Failed to parse bid event:', e);
            }
          }
        });
      },
      onDisconnect: () => {
        this.isConnected = false;
        onStatusChange(false);
        console.log('[WebSocket] Disconnected');
      },
      onStompError: (frame) => {
        console.error('[WebSocket] STOMP error:', frame.headers['message']);
      }
    });

    this.client.activate();
  }

  public disconnect() {
    if (this.client) {
      this.client.deactivate();
      this.isConnected = false;
    }
  }

  public getConnected(): boolean {
    return this.isConnected;
  }
}

export const wsService = new WebSocketService();
