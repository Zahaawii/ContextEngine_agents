package com.example.testmcp.integrationsTest.blog;


import com.example.testmcp.BlogFeature.DTO.BlogDTO;
import com.example.testmcp.BlogFeature.Service.BlogServiceAPI;
import com.example.testmcp.mcpService.BlogServiceMCP;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;



@SpringBootTest
@EnabledIfEnvironmentVariable(named = "RUN_EXTERNAL_INTEGRATION_TESTS", matches = "true")
public class BlogIntegrationTest {

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    BlogServiceAPI blogServiceAPI;

    @Autowired
    BlogServiceMCP MCP;

    @Test
    void context() {
        assertNotNull(applicationContext);
    }

    @Test
    void getAllBlogPost() {

        List<BlogDTO> allBlogs = blogServiceAPI.getAllBlogPost();
        List<BlogDTO> allBlogsFromMCP = MCP.getAllBlogPost();

        assertNotNull(allBlogs);
        assertTrue(allBlogs.size() >= 1);

        assertNotNull(allBlogsFromMCP);
        assertTrue(allBlogsFromMCP.size() >= 1);

        assertEquals(allBlogs.size(), allBlogsFromMCP.size());
    }

    @Test
    void getAllBlogPostByAuthor() {
        List<BlogDTO> allBlogsByAuthor = blogServiceAPI.getAllByAuthor("Zahaawii");
        List<BlogDTO> allBlogsByAuthorFromMCP = MCP.getAllBlogPostByAuthor("Zahaawii");

        assertNotNull(allBlogsByAuthor);
        assertTrue(allBlogsByAuthor.size() >= 1);

        assertNotNull(allBlogsByAuthorFromMCP);
        assertTrue(allBlogsByAuthorFromMCP.size() >= 1);

        assertEquals(allBlogsByAuthor.size(), allBlogsByAuthorFromMCP.size());
    }

    @Test
    void login() {
        String username = "MCPtest";
        String password = "1234";


        String test = blogServiceAPI.login(username, password);

        assertNotNull(test);
        assertTrue(test.contains(username));
    }

    @Test
    void createBlogPostFail() {
        //Due to not wanting to create a blog post everytime I test, this will fail intentionally.
        assertThrows(RuntimeException.class, () -> MCP.createBlogPost(null, null, null, null, null, null));
        assertThrows(RuntimeException.class, () -> blogServiceAPI.createBlogPost(null, null, null, null, null, null));

    }

    @Test
    void deleteBlogById() {
        //Due to not wanting to delete a blog post, this will intentionally fail.
        assertThrows(RuntimeException.class, () -> MCP.deletePostById(1, null));
        assertThrows(RuntimeException.class, () -> blogServiceAPI.deletePostById(1, null));

    }
}
