package com.example.zahaawiiblog.commentsFeature.repository;

import com.example.zahaawiiblog.commentsFeature.entity.Comments;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentsRepository extends JpaRepository<Comments, Long> {
    List<Comments> findByBlog_BlogId(Long id);
}
