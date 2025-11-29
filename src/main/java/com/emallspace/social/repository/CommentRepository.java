package com.emallspace.social.repository;

import com.emallspace.social.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Find level 1 comments for a post
    List<Comment> findByPostIdAndLevelOrderByCreateTimeDesc(Long postId, Integer level);

    // Find all descendants using path prefix
    @Query("SELECT c FROM Comment c WHERE c.path LIKE ?1%")
    List<Comment> findDescendants(String pathPrefix);
}
