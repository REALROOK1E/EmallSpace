package com.emallspace.social.service.impl;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LikeService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    // BufferTrigger: Key = "userId:postId", Value = 1 (Like) or 0 (Unlike)
    private final Map<String, Integer> buffer = new ConcurrentHashMap<>();
    private static final int BATCH_SIZE = 1000;

    // Evolution 3: Observability
    public LikeService(MeterRegistry registry) {
        Gauge.builder("like.buffer.size", buffer, Map::size)
             .description("Current size of the like buffer")
             .register(registry);
    }

    public void likePost(Long userId, Long postId, boolean isLike) {
        // 1. Write to Redis immediately for real-time display
        String redisKey = "post:likes:" + postId;
        String userLikeKey = "post:liked_users:" + postId;

        if (isLike) {
            redisTemplate.opsForSet().add(userLikeKey, userId.toString());
            redisTemplate.opsForValue().increment(redisKey);
        } else {
            redisTemplate.opsForSet().remove(userLikeKey, userId.toString());
            redisTemplate.opsForValue().decrement(redisKey);
        }

        // 2. Write to Buffer
        String bufferKey = userId + ":" + postId;
        buffer.put(bufferKey, isLike ? 1 : 0);

        // 3. Check buffer size
        if (buffer.size() >= BATCH_SIZE) {
            flushBuffer();
        }
    }

    @Scheduled(fixedRate = 5000) // 5 seconds time window
    public void flushBuffer() {
        if (buffer.isEmpty()) return;

        // In a real scenario, we would copy the buffer to a local variable and clear the main buffer 
        // in a thread-safe way to avoid blocking incoming requests.
        // For simplicity here:
        Map<String, Integer> snapshot = new ConcurrentHashMap<>(buffer);
        buffer.clear();

        // Batch write to MySQL
        // "INSERT INTO post_likes (user_id, post_id, status) VALUES ... ON DUPLICATE KEY UPDATE status = VALUES(status)"
        System.out.println("Flushing " + snapshot.size() + " like operations to MySQL...");
        
        // Mock DB interaction
        snapshot.forEach((key, status) -> {
            String[] parts = key.split(":");
            Long uId = Long.parseLong(parts[0]);
            Long pId = Long.parseLong(parts[1]);
            // repository.saveOrUpdate(uId, pId, status);
        });
    }
}
