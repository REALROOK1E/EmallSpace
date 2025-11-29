package com.emallspace.social.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Data
@Entity
@Table(name = "comment", indexes = {
    @Index(name = "idx_post_create", columnList = "post_id, create_time"),
    @Index(name = "idx_path_level", columnList = "path, level")
})
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    @Column(name = "post_id")
    private Long postId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "parent_id")
    private Long parentId;

    private String content;

    private String path; // e.g., "1/2/3"

    private Integer level;

    @Column(name = "create_time")
    private Date createTime;
}
