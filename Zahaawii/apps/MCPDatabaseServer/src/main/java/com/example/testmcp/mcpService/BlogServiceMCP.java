package com.example.testmcp.mcpService;

import com.example.testmcp.BlogFeature.DTO.BlogDTO;
import com.example.testmcp.BlogFeature.Service.BlogServiceAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.util.List;

@Component
public class BlogServiceMCP {


    private static final Logger log = LoggerFactory.getLogger(KBAServiceMCP.class);

    private final BlogServiceAPI blogApi;

    public BlogServiceMCP(BlogServiceAPI blogApi) {
        this.blogApi = blogApi;
    }


    @McpTool(name = "GetAllBlogPost", description = "Gets all blog post")
    public List<BlogDTO> getAllBlogPost() {
        try {
            log.info("This method is being accessed trying to find all blog post");
            return blogApi.getAllBlogPost();
        } catch (Exception e) {
            log.error("Failed to retrieve all the data from the method: getAllBlogPost");
            throw new RuntimeException(e);
        }
    }

    @McpTool(name = "AuthorPost", description = "Get all blog post by author")
    public List<BlogDTO> getAllBlogPostByAuthor(@McpToolParam String author) {
        try {
            log.info("This method is being accessed trying to find author: {}", author);
            return blogApi.getAllByAuthor(author);
        } catch (Exception e) {
            log.error("Failed to retrieve data from method: GetAllBlogPostByAuthor:  {}", author);
            throw new RuntimeException(e);
        }
    }

    @McpTool(name = "DeletePost", description = "Gives the possibility to delete a blog post with an id")
    public String deletePostById(@McpToolParam int blogId, String jwtToken) {
        try {
            log.info("This method is being accesed trying to delete a blog post: {}", blogId);
            return blogApi.deletePostById(blogId, jwtToken);
        } catch (Exception e) {
            log.error("Error deleting the blog post: {}", blogId);
            throw new RuntimeException(e);
        }
    }

    @McpTool(name = "CreateBlogPost", description = "Gives the possibility to upload a blog post")
    public String createBlogPost(@McpToolParam String subject, String body, String category, Long userId, Date publishDate, String jwtToken) {
        try {
            log.info("Trying to upload a blog post: {}", subject);
            return blogApi.createBlogPost(subject, body, category, userId, publishDate, jwtToken);
        } catch (Exception e) {
            log.error("Couldn't upload blog post");
            throw new RuntimeException(e);
        }
}

    @McpTool(name = "loginToExecute", description = "This methods needs to be executed first to get the JWT token before being able to delete")
    public String login(@McpToolParam String username, String password) {
        try {
            log.info("Trying to log in with username: {}", username);
            return blogApi.login(username, password);
        } catch (Exception e) {
            log.error("Couldn't log in");
            throw new RuntimeException(e);
        }
    }

}
