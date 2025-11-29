package com.emallspace.social.service.impl;

import com.emallspace.common.util.SensitiveWordFilter;
import com.emallspace.common.util.SnowflakeIdGenerator;
import com.emallspace.social.entity.Post;
import com.emallspace.social.repository.PostRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private SnowflakeIdGenerator snowflakeIdGenerator;

    @Autowired
    private SensitiveWordFilter sensitiveWordFilter;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private Cache<String, Object> localPostCache;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Evolution 1: Multi-Level Cache Read (Caffeine -> Redis -> Cassandra)
    public Post getPostDetail(Long postId) {
        String cacheKey = "post:" + postId;

        // 1. L1: Local Cache (Caffeine)
        Post localPost = (Post) localPostCache.getIfPresent(cacheKey);
        if (localPost != null) {
            return localPost;
        }

        // 2. L2: Distributed Cache (Redis)
        String redisVal = redisTemplate.opsForValue().get(cacheKey);
        if (redisVal != null) {
            try {
                Post redisPost = objectMapper.readValue(redisVal, Post.class);
                // Backfill L1
                localPostCache.put(cacheKey, redisPost);
                return redisPost;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 3. L3: Database (Cassandra)
        // Use a distributed lock here in production to prevent Cache Stampede (Thundering Herd)
        Post dbPost = postRepository.findById(postId).orElse(null);
        
        if (dbPost != null) {
            // Backfill L2 (Redis) with random TTL to prevent Avalanche
            long ttl = 30 + (long) (Math.random() * 10); // 30-40 mins
            try {
                redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(dbPost), ttl, TimeUnit.MINUTES);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Backfill L1
            localPostCache.put(cacheKey, dbPost);
        }

        return dbPost;
    }

    public Post createPost(Long userId, String title, String content, List<String> imageUrls, Long topicId) {
        // 1. Validation
        if (title.length() > 50) throw new IllegalArgumentException("Title too long");
        if (content.length() > 5000) throw new IllegalArgumentException("Content too long");
        if (imageUrls.size() > 9) throw new IllegalArgumentException("Too many images");

        // 2. Sensitive Word Check
        if (sensitiveWordFilter.containsSensitiveWord(title) || sensitiveWordFilter.containsSensitiveWord(content)) {
            throw new IllegalArgumentException("Contains sensitive content");
        }

        // 3. Create Post Object
        Post post = new Post();
        post.setPostId(snowflakeIdGenerator.nextId());
        post.setUserId(userId);
        post.setTitle(title);
        post.setContent(content);
        post.setImageUrls(imageUrls);
        post.setTopicId(topicId);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setCreateTime(new Date());
        post.setStatus(1); // Default published for simplicity, logic mentions pending for new users

        // 4. Save to Cassandra
        // In real scenario: try-catch with local message table fallback
        postRepository.save(post);

        // 5. Send Event to Kafka
        kafkaTemplate.send("post_events", "Post Created: " + post.getPostId());

        return post;
    }
}
