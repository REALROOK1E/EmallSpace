package com.emallspace.social.entity;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
@Table("post")
public class Post {

    @PrimaryKey
    private Long postId;

    @Column("user_id")
    private Long userId;

    private String title;

    private String content;

    @Column("image_urls")
    private List<String> imageUrls;

    @Column("topic_id")
    private Long topicId;

    @Column("like_count")
    private Integer likeCount;

    @Column("comment_count")
    private Integer commentCount;

    @Column("create_time")
    private Date createTime;

    private Integer status; // 0: Pending, 1: Published, 2: Rejected
}
