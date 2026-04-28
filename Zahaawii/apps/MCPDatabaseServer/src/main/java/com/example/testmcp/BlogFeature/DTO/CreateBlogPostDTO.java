package com.example.testmcp.BlogFeature.DTO;

import java.sql.Date;

public record CreateBlogPostDTO(String subject, String body, String category, Long userId, Date publishDate){
}
