package com.example.zahaawiiblog.commentsFeature.Controller;

import com.example.zahaawiiblog.commentsFeature.DTO.CommentsDTO;
import com.example.zahaawiiblog.commentsFeature.service.CommentsService;
import com.example.zahaawiiblog.logginFeature.service.LoggingService;
import com.example.zahaawiiblog.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CommentsControllerTest {

    private TrackingCommentsService commentsService;
    private NoOpUserService userService;
    private NoOpLoggingService loggingService;
    private CommentsController controller;

    @BeforeEach
    void setUp() {
        commentsService = new TrackingCommentsService();
        userService = new NoOpUserService();
        loggingService = new NoOpLoggingService();
        controller = new CommentsController(commentsService, userService, loggingService);
    }

    @AfterEach
    void tearDown() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void addCommentRejectsAnonymousRequestWithoutNpe() {
        CommentsDTO request = new CommentsDTO(1L, "hello", 2L, null, new Date(System.currentTimeMillis()));

        ResponseEntity<?> response = controller.addComment(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("User not logged in", response.getBody());
        assertFalse(commentsService.addCalled);
    }

    @Test
    void deleteCommentRejectsAnonymousRequestWithoutNpe() {
        ResponseEntity<?> response = controller.deleteComment(4L);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("User not logged in", response.getBody());
        assertFalse(commentsService.deleteCalled);
    }

    private static final class TrackingCommentsService extends CommentsService {
        private boolean addCalled;
        private boolean deleteCalled;

        private TrackingCommentsService() {
            super(null, null, null);
        }

        @Override
        public CommentsDTO add(CommentsDTO comment) {
            addCalled = true;
            return comment;
        }

        @Override
        public Long deleteByCommentId(Long id) {
            deleteCalled = true;
            return id;
        }
    }

    private static final class NoOpUserService extends UserService {
        private NoOpUserService() {
            super(null);
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
