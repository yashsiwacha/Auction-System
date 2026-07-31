package com.auction.infrastructure.adapter.out.messaging;

import com.auction.domain.model.Bid;
import com.auction.domain.port.out.EventPublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventPublisherAdapter implements EventPublisherPort {

    @Autowired(required = false)
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired(required = false)
    private SimpMessagingTemplate messagingTemplate;

    private static final String BIDS_TOPIC = "auction.bid.placed";
    private static final String AUCTION_ENDED_TOPIC = "auction.ended";

    @Override
    public void publishBidPlacedEvent(Bid bid) {
        log.info("Publishing BidPlacedEvent for auctionId: {}, amount: {}", bid.getAuctionId(), bid.getAmount());

        // 1. Publish to Kafka
        if (kafkaTemplate != null) {
            try {
                kafkaTemplate.send(BIDS_TOPIC, String.valueOf(bid.getAuctionId()), bid);
            } catch (Exception e) {
                log.warn("Kafka unavailable. Could not send message to topic {}: {}", BIDS_TOPIC, e.getMessage());
            }
        }

        // 2. Broadcast via WebSockets STOMP broker
        if (messagingTemplate != null) {
            try {
                String destination = "/topic/auctions/" + bid.getAuctionId();
                messagingTemplate.convertAndSend(destination, bid);
                log.debug("Broadcasted WebSocket bid update to destination: {}", destination);
            } catch (Exception e) {
                log.warn("Failed to broadcast WebSocket update: {}", e.getMessage());
            }
        }
    }

    @Override
    public void publishAuctionEndedEvent(Long auctionId, Long winnerId) {
        log.info("Publishing AuctionEndedEvent for auctionId: {}, winnerId: {}", auctionId, winnerId);

        if (kafkaTemplate != null) {
            try {
                kafkaTemplate.send(AUCTION_ENDED_TOPIC, String.valueOf(auctionId), "Winner: " + winnerId);
            } catch (Exception e) {
                log.warn("Kafka unavailable for topic {}: {}", AUCTION_ENDED_TOPIC, e.getMessage());
            }
        }

        if (messagingTemplate != null) {
            try {
                String destination = "/topic/auctions/" + auctionId + "/ended";
                messagingTemplate.convertAndSend(destination, "Winner: " + winnerId);
            } catch (Exception e) {
                log.warn("Failed to broadcast WebSocket auction ended event: {}", e.getMessage());
            }
        }
    }
}
