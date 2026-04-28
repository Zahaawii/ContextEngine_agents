package com.example.zahaawiiblog.controller;

import com.example.zahaawiiblog.DTO.BlogDTO;
import com.example.zahaawiiblog.DTO.CreateBlogDto;
import com.example.zahaawiiblog.entity.Blog;
import com.example.zahaawiiblog.logginFeature.service.LoggingService;
import com.example.zahaawiiblog.securityFeature.Entity.UserInfo;
import com.example.zahaawiiblog.service.BlogService;
import com.example.zahaawiiblog.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class BlogControllerTest {

    private StubBlogService blogService;
    private StubUserService userService;
    private NoOpLoggingService loggingService;
    private BlogController controller;

    @BeforeEach
    void setUp() {
        blogService = new StubBlogService();
        userService = new StubUserService();
        loggingService = new NoOpLoggingService();
        controller = new BlogController(blogService, userService, loggingService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void removeBlogRejectsAuthenticatedNonOwner() {
        UserInfo owner = user(1L, "owner");
        UserInfo attacker = user(2L, "attacker");
        Blog blog = Blog.builder().blogId(42L).userInfo(owner).build();

        authenticate("attacker");
        userService.usersByName.put("attacker", attacker);
        blogService.blogById = Optional.of(blog);

        ResponseEntity<?> response = controller.removeBlog(42L);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("You are not allowed to delete this blog post", response.getBody());
        assertFalse(blogService.removeCalled);
    }

    @Test
    void updateBlogPostRejectsAuthenticatedNonOwner() {
        UserInfo owner = user(1L, "owner");
        UserInfo attacker = user(2L, "attacker");
        Blog blog = Blog.builder().blogId(7L).body("before").userInfo(owner).build();

        authenticate("attacker");
        userService.usersByName.put("attacker", attacker);
        blogService.blogById = Optional.of(blog);

        ResponseEntity<?> response = controller.updateBlogPost("after", 7L);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("You are not allowed to update this blog post", response.getBody());
        assertFalse(blogService.updateCalled);
    }

    @Test
    void addBlogReturnsBadRequestWhenAuthenticatedUserIsMissing() {
        CreateBlogDto newBlog = new CreateBlogDto(
                "Subject",
                "Body",
                "Category",
                99L,
                Date.valueOf(LocalDate.now())
        );

        authenticate("ghost");

        ResponseEntity<?> response = controller.addBlog(newBlog);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("User not found", response.getBody());
        assertFalse(blogService.addCalled);
    }

    private static UserInfo user(Long id, String username) {
        return UserInfo.builder()
                .userId(id)
                .name(username)
                .roles("USER")
                .build();
    }

    private static void authenticate(String username) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(username, "password", List.of()));
        SecurityContextHolder.setContext(context);
    }

    private static final class StubBlogService extends BlogService {
        private Optional<Blog> blogById = Optional.empty();
        private boolean removeCalled;
        private boolean updateCalled;
        private boolean addCalled;

        private StubBlogService() {
            super(null);
        }

        @Override
        public Optional<Blog> findById(long id) {
            return blogById;
        }

        @Override
        public void removeBlogPost(long deleteId) {
            removeCalled = true;
        }

        @Override
        public void updateBlog(Blog blog) {
            updateCalled = true;
        }

        @Override
        public BlogDTO addNewBlogPost(CreateBlogDto blog, UserInfo currentUser) {
            addCalled = true;
            return null;
        }
    }

    private static final class StubUserService extends UserService {
        private final Map<String, UserInfo> usersByName = new HashMap<>();

        private StubUserService() {
            super(null);
        }

        @Override
        public Optional<UserInfo> findUserByUsername(String username) {
            return Optional.ofNullable(usersByName.get(username));
        }
    }

    private static final class NoOpLoggingService extends LoggingService {
        private NoOpLoggingService() {
            super(null);
        }

        @Override
        public void log(Long userId, String test, String username, Long postId) {
        }
    }
}
