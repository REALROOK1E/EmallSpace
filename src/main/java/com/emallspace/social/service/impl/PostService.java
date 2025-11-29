package com.emallspace.social.service.impl;

import com.emallspace.common.util.SensitiveWordFilter;
import com.emallspace.common.util.SnowflakeIdGenerator;
import com.emallspace.social.entity.Post;
import com.emallspace.social.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

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
