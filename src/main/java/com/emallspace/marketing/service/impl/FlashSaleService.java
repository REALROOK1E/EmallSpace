package com.emallspace.marketing.service.impl;

import com.emallspace.marketing.repository.StockRepository;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
public class FlashSaleService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private RedissonClient redissonClient;

    // Evolution 2: Distributed Lock for Idempotency
    public boolean purchase(Long userId, Long productId) {
        String lockKey = "lock:purchase:" + userId + ":" + productId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // Try to acquire lock, wait 0s, lease 5s (auto release)
            if (lock.tryLock(0, 5, TimeUnit.SECONDS)) {
                
                // Check if user already bought (Idempotency check)
                String orderKey = "order:" + userId + ":" + productId;
                if (Boolean.TRUE.equals(redisTemplate.hasKey(orderKey))) {
                    return false; // Already purchased
                }

                // 1. Redis Atomic Decrement
                String stockKey = "product:stock:" + productId;
                Long stock = redisTemplate.opsForValue().decrement(stockKey);

                if (stock != null && stock < 0) {
                    // Stock empty, rollback Redis
                    redisTemplate.opsForValue().increment(stockKey);
                    return false;
                }

                // 2. Async DB Update
                try {
                    updateDbStock(productId);
                    // Mark as purchased in Redis
                    redisTemplate.opsForValue().set(orderKey, "1", 24, TimeUnit.HOURS);
                } catch (Exception e) {
                    System.err.println("DB Update failed for product " + productId);
                    redisTemplate.opsForValue().increment(stockKey); // Rollback
                    return false;
                }

                return true;
            } else {
                return false; // Duplicate request blocked
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Async
    @Transactional
    public void updateDbStock(Long productId) {
        int updatedRows = stockRepository.decreaseStock(productId);
        if (updatedRows == 0) {
            throw new RuntimeException("Optimistic lock failure or stock empty in DB");
        }
    }
}
