package com.example.zahaawiiblog.commentsFeature.Controller;


import com.example.zahaawiiblog.commentsFeature.DTO.CommentsDTO;
import com.example.zahaawiiblog.commentsFeature.service.CommentsService;
import com.example.zahaawiiblog.logginFeature.service.LoggingService;
import com.example.zahaawiiblog.securityFeature.Entity.UserInfo;
import com.example.zahaawiiblog.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/comments")
@CrossOrigin(origins = "*")
public class CommentsController {

    private final CommentsService commentsService;
    private final UserService userService;
    private final LoggingService loggingService;

    public CommentsController (CommentsService commentsService, UserService userService, LoggingService loggingService) {
        this.commentsService = commentsService;
        this.userService = userService;
        this.loggingService = loggingService;
    }

    @PostMapping("/addcomment")
    public ResponseEntity<?> addComment (@RequestBody CommentsDTO comment) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAuthenticated(auth)) {
            loggingService.log(0L, " an unathorized user tried to add a comment", "unathorized user", 4L);
            return new ResponseEntity<>("User not logged in", HttpStatus.UNAUTHORIZED);
        }
        UserInfo user = userService.findUserByUsername(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        commentsService.add(comment);
        loggingService.log(user.getUserId(), user.getName() + ": added a comment to blog id: " + comment.blogId() , user.getName(), 3L);
        return new ResponseEntity<>(comment,HttpStatus.OK);
    }

    @GetMapping("/getcomment/{blogId}")
    public ResponseEntity<?> test(@PathVariable Long blogId) {
        List<CommentsDTO> getAllComments = commentsService.findCommentsByBlogId(blogId);
        return new ResponseEntity<>(getAllComments, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{commentid}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentid) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAuthenticated(auth)) {
            loggingService.log(0L,"an unathorized user: tried to deleted a comment with id: " + commentid , "unathorized user", 4L);
            return new ResponseEntity<>("User not logged in", HttpStatus.UNAUTHORIZED);
        }
        UserInfo user = userService.findUserByUsername(auth.getName()).orElseThrow(() -> new RuntimeException("User not found"));
        commentsService.deleteByCommentId(commentid);
        loggingService.log(user.getUserId(), user.getName() + ": deleted a comment with id: " + commentid , user.getName(), 3L);
        return new ResponseEntity<>("Comment deleted", HttpStatus.OK);
    }

    private boolean isAuthenticated(Authentication auth) {
        return auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken);
    }

}
