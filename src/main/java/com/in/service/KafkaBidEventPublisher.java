package com.in.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.in.dto.AuctionBidEvent;
import com.in.entity.Bid;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.math.BigDecimal;
import java.util.Properties;

@Service
public class KafkaBidEventPublisher {

    @Value("${infra.kafka.enabled:false}")
    private boolean kafkaEnabled;

    @Value("${infra.kafka.bootstrap-servers:localhost:9092}")
    private String kafkaBootstrapServers;

    @Value("${infra.kafka.topic.bid-events:auction-bid-events}")
    private String bidEventsTopic;

    private Producer<String, String> producer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        if (!kafkaEnabled) {
            return;
        }

        try {
            Properties props = new Properties();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            props.put(ProducerConfig.ACKS_CONFIG, "all");
            props.put(ProducerConfig.RETRIES_CONFIG, 3);
            props.put(ProducerConfig.LINGER_MS_CONFIG, 5);
            props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);

            producer = new KafkaProducer<>(props);
        } catch (Exception ex) {
            kafkaEnabled = false;
            producer = null;
            System.err.println("[KafkaBidEventPublisher] Kafka disabled due to init error: " + ex.getMessage());
        }
    }

    @PreDestroy
    public void shutdown() {
        if (producer != null) {
            try {
                producer.flush();
            } catch (Exception ignored) {
            }
            producer.close();
        }
    }

    public void publishBidPlaced(Bid bid, BigDecimal previousHighestBid, long totalBidCount) {
        if (!isOperational() || bid == null || bid.getProduct() == null || bid.getBidder() == null) {
            return;
        }

        try {
            AuctionBidEvent event = new AuctionBidEvent();
            event.setBidId(bid.getId());
            event.setProductId(bid.getProduct().getId());
            event.setBidderId(bid.getBidder().getId());
            event.setBidderName(bid.getBidder().getFullName());
            event.setBidAmount(bid.getBidAmount());
            event.setPreviousHighestBid(previousHighestBid);
            event.setCurrentHighestBid(bid.getProduct().getCurrentHighestBid());
            event.setTotalBidCount(totalBidCount);
            event.setProductVersion(bid.getProduct().getVersion());
            event.setBidTime(bid.getBidTime());

            String payload = objectMapper.writeValueAsString(event);
            String key = "auction-" + event.getProductId();
            producer.send(new ProducerRecord<>(bidEventsTopic, key, payload));
        } catch (Exception ignored) {
        }
    }

    private boolean isOperational() {
        return kafkaEnabled && producer != null;
    }
}

