package com.auction.domain.port.out;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public interface LockPort {
    <T> T executeWithLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit, Supplier<T> supplier);
}
