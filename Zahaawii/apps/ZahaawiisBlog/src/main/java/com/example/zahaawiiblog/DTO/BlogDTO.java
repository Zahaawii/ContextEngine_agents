package com.example.zahaawiiblog.DTO;

import java.sql.Date;

public record BlogDTO(Long blogId, String subject, String body, String category, Date publishDate, AuthorDTO author) {
}
