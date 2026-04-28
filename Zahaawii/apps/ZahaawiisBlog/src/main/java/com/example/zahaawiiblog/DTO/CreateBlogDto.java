package com.example.zahaawiiblog.DTO;

import java.sql.Date;

public record CreateBlogDto(String subject, String body, String category, Long userId, Date publishDate) {
}
