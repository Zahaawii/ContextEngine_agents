package com.example.zahaawiiblog.repositories;

import com.example.zahaawiiblog.entity.Blog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {
    List<Blog> findByBlogId(Long blogId);
    List<Blog> findAllByUserInfo_UserId(Long userInfoUserId);
    List<Blog> findAllByUserInfo_Name(String username);
    Blog findBlogByBlogId(Long id);
}
