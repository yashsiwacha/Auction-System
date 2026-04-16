package com.in.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.math.BigDecimal;
import java.util.Optional;

@Service
public class RedisBidCacheService {

    @Value("${infra.redis.enabled:false}")
    private boolean redisEnabled;

    @Value("${infra.redis.host:localhost}")
    private String redisHost;

    @Value("${infra.redis.port:6379}")
    private int redisPort;

    @Value("${infra.redis.password:}")
    private String redisPassword;

    @Value("${infra.redis.timeout-ms:2000}")
    private int redisTimeoutMs;

    private JedisPool jedisPool;

    @PostConstruct
    public void init() {
        if (!redisEnabled) {
            return;
        }

        try {
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(256);
            config.setMaxIdle(64);
            config.setMinIdle(8);
            config.setTestOnBorrow(true);

            if (redisPassword != null && !redisPassword.trim().isEmpty()) {
                jedisPool = new JedisPool(config, redisHost, redisPort, redisTimeoutMs, redisPassword);
            } else {
                jedisPool = new JedisPool(config, redisHost, redisPort, redisTimeoutMs);
            }
        } catch (Exception ex) {
            redisEnabled = false;
            jedisPool = null;
            System.err.println("[RedisBidCacheService] Redis disabled due to init error: " + ex.getMessage());
        }
    }

    @PreDestroy
    public void shutdown() {
        if (jedisPool != null) {
            jedisPool.close();
        }
    }

    public Optional<BigDecimal> getCurrentHighestBid(Integer productId) {
        if (!isOperational() || productId == null) {
            return Optional.empty();
        }

        try (Jedis jedis = jedisPool.getResource()) {
            String value = jedis.get(highestKey(productId));
            if (value == null || value.trim().isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(new BigDecimal(value));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    public Optional<Long> getBidCount(Integer productId) {
        if (!isOperational() || productId == null) {
            return Optional.empty();
        }

        try (Jedis jedis = jedisPool.getResource()) {
            String value = jedis.get(countKey(productId));
            if (value == null || value.trim().isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(Long.parseLong(value));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    public void cacheBidUpdate(Integer productId, BigDecimal currentHighestBid, long totalBidCount, Long productVersion) {
        if (!isOperational() || productId == null || currentHighestBid == null) {
            return;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.set(highestKey(productId), currentHighestBid.toPlainString());
            jedis.set(countKey(productId), String.valueOf(totalBidCount));
            if (productVersion != null) {
                jedis.set(versionKey(productId), String.valueOf(productVersion));
            }
        } catch (Exception ignored) {
        }
    }

    public boolean tryAcquireBidPermit(Integer productId, Integer bidderId, int limitPerSecond) {
        if (!isOperational() || productId == null || bidderId == null || limitPerSecond <= 0) {
            return true;
        }

        long secondBucket = System.currentTimeMillis() / 1000L;
        String key = "rate:auction:" + productId + ":bidder:" + bidderId + ":" + secondBucket;

        try (Jedis jedis = jedisPool.getResource()) {
            Long count = jedis.incr(key);
            if (count != null && count == 1L) {
                jedis.expire(key, 2);
            }
            return count != null && count <= limitPerSecond;
        } catch (Exception ignored) {
            // Fail-open for availability.
            return true;
        }
    }

    private boolean isOperational() {
        return redisEnabled && jedisPool != null;
    }

    private String highestKey(Integer productId) {
        return "auction:" + productId + ":highest";
    }

    private String countKey(Integer productId) {
        return "auction:" + productId + ":count";
    }

    private String versionKey(Integer productId) {
        return "auction:" + productId + ":version";
    }
}

