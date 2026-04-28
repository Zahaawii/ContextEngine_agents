package com.example.testmcp.BlogFeature.DTO;

import java.sql.Date;

public record BlogDTO(int blogId, String subject, String body, String category, Date publishDate, AuthorDTO author) {
}
