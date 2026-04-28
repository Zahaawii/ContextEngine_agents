package com.example.zahaawiiblog.commentsFeature.entity;

import com.example.zahaawiiblog.entity.Blog;
import com.example.zahaawiiblog.securityFeature.Entity.UserInfo;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "comments")
public class Comments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    private String userComment;

    @ManyToOne
    @JoinColumn(name = "blog_id")
    @JsonBackReference
    private Blog blog;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserInfo user;

    private Date createdComment;
}
