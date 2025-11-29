package com.emallspace.social.service.impl;

import com.emallspace.social.entity.Comment;
import com.emallspace.social.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    public Comment addComment(Long postId, Long userId, String content, Long parentId) {
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setCreateTime(new Date());
        comment.setParentId(parentId);

        if (parentId == null) {
            // Level 1 comment
            comment.setLevel(1);
            // Path will be set after ID generation or we use a temporary placeholder and update
            // For simplicity, let's assume we save first to get ID, then update path
            comment = commentRepository.save(comment);
            comment.setPath(String.valueOf(comment.getCommentId()));
        } else {
            // Reply
            Comment parent = commentRepository.findById(parentId).orElseThrow();
            if (parent.getLevel() >= 10) {
                // Flatten to level 1 if too deep, per requirements
                comment.setLevel(1);
                comment.setParentId(null);
                comment = commentRepository.save(comment);
                comment.setPath(String.valueOf(comment.getCommentId()));
            } else {
                comment.setLevel(parent.getLevel() + 1);
                comment.setPath(parent.getPath() + "/" + "TEMP"); // Placeholder
                comment = commentRepository.save(comment);
                comment.setPath(parent.getPath() + "/" + comment.getCommentId());
            }
        }
        
        return commentRepository.save(comment);
    }

    public List<Comment> getComments(Long postId) {
        // Get Level 1 comments
        return commentRepository.findByPostIdAndLevelOrderByCreateTimeDesc(postId, 1);
    }

    public List<Comment> getReplies(Long commentId) {
        Comment root = commentRepository.findById(commentId).orElseThrow();
        // Get all descendants
        return commentRepository.findDescendants(root.getPath() + "/");
    }
}
