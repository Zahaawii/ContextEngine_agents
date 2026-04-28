package com.example.zahaawiiblog.commentsFeature.mapper;

import com.example.zahaawiiblog.commentsFeature.DTO.CommentsDTO;
import com.example.zahaawiiblog.commentsFeature.entity.Comments;

public final class CommentsMapper {

    private CommentsMapper() {

    }

    public static CommentsDTO toDTO(Comments c) {
        return new CommentsDTO(
                c.getCommentId(),
                c.getUserComment(),
                c.getBlog().getBlogId(),
                c.getUser().getName(),
                c.getCreatedComment()
                );
    }
}
