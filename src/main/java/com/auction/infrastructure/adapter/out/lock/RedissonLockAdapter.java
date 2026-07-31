package com.auction.infrastructure.adapter.out.lock;

import com.auction.domain.port.out.LockPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedissonLockAdapter implements LockPort {

    @Autowired(required = false)
    private RedissonClient redissonClient;

    @Override
    public <T> T executeWithLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit, Supplier<T> supplier) {
        if (redissonClient == null) {
            log.debug("RedissonClient unavailable. Executing supplier without distributed lock: {}", lockKey);
            return supplier.get();
        }

        RLock lock = redissonClient.getLock(lockKey);
        boolean isLocked = false;
        try {
            isLocked = lock.tryLock(waitTime, leaseTime, unit);
            if (!isLocked) {
                throw new IllegalStateException("Unable to acquire lock for key: " + lockKey + " after " + waitTime + " " + unit);
            }
            log.debug("Acquired distributed lock for key: {}", lockKey);
            return supplier.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Thread interrupted while acquiring lock: " + lockKey, e);
        } finally {
            if (isLocked && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("Released distributed lock for key: {}", lockKey);
            }
        }
    }
}
