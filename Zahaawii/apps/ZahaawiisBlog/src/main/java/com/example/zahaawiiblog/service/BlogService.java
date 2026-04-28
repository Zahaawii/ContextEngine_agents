package com.example.zahaawiiblog.service;

import com.example.zahaawiiblog.DTO.BlogDTO;
import com.example.zahaawiiblog.DTO.CreateBlogDto;
import com.example.zahaawiiblog.entity.Blog;
import com.example.zahaawiiblog.mapper.BlogMapper;
import com.example.zahaawiiblog.repositories.BlogRepository;
import com.example.zahaawiiblog.securityFeature.Entity.UserInfo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class BlogService {

    private final BlogRepository blogRepository;

    public BlogService(BlogRepository blogRepository) {
        this.blogRepository = blogRepository;
    }

    public Optional<Blog> findById(long id) {
        return blogRepository.findById(id);
    }

    public List<Blog> findAllByBlogId(long id) {
        return blogRepository.findByBlogId(id);
    }

    public List<BlogDTO> findAllByUsername(String username) {
        return blogRepository.findAllByUserInfo_Name(username).stream().map(BlogMapper::toDTO).toList();
    }

    public BlogDTO addNewBlogPost(CreateBlogDto blog, UserInfo currentUser) {
        Blog b = new Blog();
        b.setSubject(blog.subject());
        b.setBody(blog.body());
        b.setCategory(blog.category());
        b.setPublishDate(blog.publishDate());
        b.setUserInfo(currentUser);
        Blog saved = blogRepository.save(b);
        return BlogMapper.toDTO(saved);
    }

    public void updateBlog(Blog blog) {
        blogRepository.save(blog);
    }

    public List<BlogDTO> getBlogsByUserId(Long userId) {
        var blogs = blogRepository.findAllByUserInfo_UserId(userId);
        return blogs.stream().map(BlogMapper::toDTO).toList();
    }

    public List<BlogDTO> getAllBlogs() {
      return blogRepository.findAll().stream().map(BlogMapper::toDTO).toList();
    }

    public void removeBlogPost(long deleteId) {
        blogRepository.deleteById(deleteId);
    }

}
