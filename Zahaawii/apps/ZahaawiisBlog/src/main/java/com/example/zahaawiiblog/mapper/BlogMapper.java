package com.example.zahaawiiblog.mapper;

import com.example.zahaawiiblog.DTO.AuthorDTO;
import com.example.zahaawiiblog.DTO.BlogDTO;
import com.example.zahaawiiblog.entity.Blog;

public final class BlogMapper {

    private BlogMapper() {

    }

    public static BlogDTO toDTO(Blog b) {
        AuthorDTO author = new AuthorDTO(
                b.getUserInfo().getUserId(),
                b.getUserInfo().getName(),
                b.getUserInfo().getImgPath()
        );

        return new BlogDTO(
                b.getBlogId(),
                b.getSubject(),
                b.getBody(),
                b.getCategory(),
                b.getPublishDate(),
                author
        );
    }
}
