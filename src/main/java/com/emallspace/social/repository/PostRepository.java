package com.emallspace.social.repository;

import com.emallspace.social.entity.Post;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends CassandraRepository<Post, Long> {
    
    // Secondary index query simulation (Cassandra usually needs explicit index creation)
    @Query("SELECT * FROM post WHERE user_id = ?0 ALLOW FILTERING")
    List<Post> findByUserId(Long userId);
}
