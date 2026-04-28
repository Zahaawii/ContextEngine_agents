package com.example.zahaawiiblog.controller;


import com.example.zahaawiiblog.DTO.BlogDTO;
import com.example.zahaawiiblog.DTO.CreateBlogDto;
import com.example.zahaawiiblog.entity.Blog;
import com.example.zahaawiiblog.logginFeature.service.LoggingService;
import com.example.zahaawiiblog.securityFeature.Entity.UserInfo;
import com.example.zahaawiiblog.service.BlogService;
import com.example.zahaawiiblog.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RequestMapping("/api/v1/blog")
@RestController
@CrossOrigin(origins = "*")
public class BlogController {

    private final BlogService blogService;
    private final UserService userService;
    private final LoggingService loggingService;

    public BlogController(BlogService blogService, UserService userService, LoggingService loggingService) {
        this.blogService = blogService;
        this.userService = userService;
        this.loggingService = loggingService;
    }

    @GetMapping("/getbyid/{id}")
    public ResponseEntity<Optional<Blog>> getBlogByBlogId(@PathVariable Long id) {
        return new ResponseEntity<>(blogService.findById(id), HttpStatus.OK);
    }

    @GetMapping("/getbyusername/{username}")
    public ResponseEntity<List<BlogDTO>> getAllByUsernamne(@PathVariable String username) {
        /* The one line of code should be changed to see who the user is if user is logged in.
        I am thinking that a possibility would be to create a post request where it sends information to the server
        Maybe use a method to receive user information by the jwt token so we can fill out all the table fields.
        we'll see what's the best solution */
        loggingService.log(0L, "a user accessed: " + username + "s" +" profile", username, 6L);
        List<BlogDTO> findAllBlogPostByUsername = blogService.findAllByUsername(username);
        return new ResponseEntity<>(findAllBlogPostByUsername, HttpStatus.OK);
    }

    @GetMapping("/getallblogpost")
    public ResponseEntity<List<BlogDTO>> getAll() {
        /* The two lines of codes should be changed to see who the user is if user is logged in.
        I am thinking that a possibility would be to create a post request where it sends information to the server
        Maybe use a method to receive user information by the jwt token so we can fill out all the table fields.
        we'll see what's the best solution */

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = isAuthenticated(auth) ? auth.getName() : "anonymous";
        loggingService.log(0L, "user accessed homepage", username, 5L);
        return new ResponseEntity<>(blogService.getAllBlogs(), HttpStatus.OK);
    }

    @DeleteMapping("/deletepost/{id}")
    public ResponseEntity<?> removeBlog(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(!isAuthenticated(auth)) {
            return new ResponseEntity<>("User not authenticated", HttpStatus.UNAUTHORIZED);
        }
        Optional<UserInfo> info = userService.findUserByUsername(auth.getName());
        if(info.isEmpty()) {
            loggingService.log(0L, auth.getName() + ": tried to delete blog post but user was not found", auth.getName(), 4L);
            return new ResponseEntity<>("User not found", HttpStatus.BAD_REQUEST);
        }

        Optional<Blog> existingBlog = blogService.findById(id);
        if(existingBlog.isEmpty()) {
            loggingService.log(4L,auth.getName() + " :tried to delete blog post: " + id + "but failed", info.get().getName(), 4L);
            return new ResponseEntity<>("Blog post does not exist", HttpStatus.BAD_REQUEST);
        }

        if(!isOwner(existingBlog.get(), info.get())) {
            loggingService.log(info.get().getUserId(), auth.getName() + ": tried to delete blog post without ownership: " + id, info.get().getName(), 4L);
            return new ResponseEntity<>("You are not allowed to delete this blog post", HttpStatus.FORBIDDEN);
        }

        blogService.removeBlogPost(id);
        loggingService.log(info.get().getUserId(),auth.getName() + ": deleted blog post with id: " + id, info.get().getName(), 3L);
        return new ResponseEntity<>("Blog post with id " + id + " was deleted", HttpStatus.OK);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateBlogPost(@RequestBody String updateBlog, @PathVariable long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(!isAuthenticated(auth)) {
            loggingService.log(0L, "Tried to update a blog post but fail", "User not logged in",  2L);
            return new ResponseEntity<>("Not able to update blog post, user not fount", HttpStatus.CONFLICT);
        }

        Optional<UserInfo> user = userService.findUserByUsername(auth.getName());
        if(user.isEmpty()) {
            loggingService.log(0L, auth.getName() + ": tried to update a blog post but user was not found", auth.getName(), 2L);
            return new ResponseEntity<>("User not found", HttpStatus.BAD_REQUEST);
        }

        Blog find = blogService.findById(id).orElseThrow(() -> new RuntimeException("Could not find blog"));
        if(!isOwner(find, user.get())) {
            loggingService.log(user.get().getUserId(), auth.getName() + ": tried to update blog post without ownership: " + id, user.get().getName(), 2L);
            return new ResponseEntity<>("You are not allowed to update this blog post", HttpStatus.FORBIDDEN);
        }

        find.setBody(updateBlog);
        blogService.updateBlog(find);
        loggingService.log(find.getUserInfo().getUserId(), "Updated blog post succesfully", user.get().getName(), 1L);
        return new ResponseEntity<>("Blog updated succesfully", HttpStatus.OK);
    }

    @PostMapping("/saveblogpost")
    public ResponseEntity<?> addBlog(@RequestBody CreateBlogDto newBlogPost) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(!isAuthenticated(auth)) {
            loggingService.log(newBlogPost.userId(), "tried to create a blog post but failed", "User not created", 2L);
            return new ResponseEntity<>("Not able to create blog post, user not found", HttpStatus.CONFLICT);
        }
        Optional<UserInfo> user = userService.findUserByUsername(auth.getName());
        if(user.isPresent()) {
            BlogDTO dto = blogService.addNewBlogPost(newBlogPost, user.get());
            loggingService.log(user.get().getUserId(), user.get().getName() + ": created a blog post",user.get().getName(), 1L);
            return new ResponseEntity<>(dto, HttpStatus.CREATED);
        } else {
            loggingService.log(0L, auth.getName() + ": tried to create a blog post but failed", auth.getName(), 2L);
            return new ResponseEntity<>("User not found", HttpStatus.BAD_REQUEST);
        }
    }

    private boolean isOwner(Blog blog, UserInfo currentUser) {
        return blog.getUserInfo() != null
                && blog.getUserInfo().getUserId() != null
                && blog.getUserInfo().getUserId().equals(currentUser.getUserId());
    }

    private boolean isAuthenticated(Authentication auth) {
        return auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken);
    }

}
