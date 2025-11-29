package com.emallspace.marketing.service.impl;

import com.emallspace.marketing.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FlashSaleService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private StockRepository stockRepository;

    public boolean purchase(Long userId, Long productId) {
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
        } catch (Exception e) {
            // Log error, trigger compensation/retry logic
            // For this example, we just print
            System.err.println("DB Update failed for product " + productId);
            // Rollback Redis (Compensating transaction)
            redisTemplate.opsForValue().increment(stockKey);
            return false;
        }

        return true;
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
