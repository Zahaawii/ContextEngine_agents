package com.example.testmcp.unitTest.blog;


import com.example.testmcp.BlogFeature.DTO.BlogDTO;
import com.example.testmcp.BlogFeature.Service.BlogServiceAPI;
import com.example.testmcp.mcpService.BlogServiceMCP;
import org.junit.jupiter.api.Test;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class BlogServiceMCPUnitTest {

    @Autowired
    private ApplicationContext applicationContext;

    //MCP tools
    @Autowired
    private BlogServiceMCP blogServiceMCP;

    //Mocking the data
    @MockitoBean
    private BlogServiceAPI blogServiceAPI;

    @MockitoBean
    private VectorStore vectorStore;

    // Testing if the application have the correct beans and can boot up
    @Test
    void contextLoads() {
        assertNotNull(applicationContext);
    }

    // Testing to see if the blogService bean exist
    @Test
    void blogServiceAPI_shouldBeMockedBySpring() {
        assertNotNull(blogServiceAPI, "BlogService should be created");
    }

    // Testing to see if the bean exist
    @Test
    void blogServiceMCP_shouldBeCreated() {
        assertNotNull(blogServiceMCP, "BlogService should be created");
    }

    @Test
    void mcpGetAllBlogPost() {

        List<BlogDTO> blogsList = List.of(new BlogDTO(0, null, null, null, null, null));
        when(blogServiceAPI.getAllBlogPost()).thenReturn(blogsList);
        List<BlogDTO> getAllBlogsresult = blogServiceMCP.getAllBlogPost();

        assertNotNull(getAllBlogsresult);
        assertFalse(getAllBlogsresult.isEmpty());
        verify(blogServiceAPI).getAllBlogPost();

    }

    @Test
    void mcpGetAllBlogPostByAuthor() {

        List<BlogDTO> blogsList = List.of(new BlogDTO(0, null, null, null, null, null));
        String author = "Zahaawii";
        when(blogServiceAPI.getAllByAuthor(author)).thenReturn(blogsList);
        List<BlogDTO> getAllByAuthorResult = blogServiceMCP.getAllBlogPostByAuthor(author);

        //get all blog by author
        assertNotNull(getAllByAuthorResult);
        assertFalse(getAllByAuthorResult.isEmpty());
        verify(blogServiceAPI).getAllByAuthor(author);

    }

    @Test
    void mcpDeleteBlogPost() {

        int id = 100;
        String success = "Blog post deleted";
        String jwtToken = "jwtToken";

        when(blogServiceAPI.deletePostById(100, jwtToken)).thenReturn(success);

        assertEquals(success, blogServiceMCP.deletePostById(id, jwtToken));
        verify(blogServiceAPI).deletePostById(id, jwtToken);

    }

    @Test
    void mcpLoginToBlog() {
        String jwtToken = "jwtToken";
        String username = "MCPtest";
        String password = "1234";
        when(blogServiceAPI.login(username, password)).thenReturn(jwtToken);


        String loginResult = blogServiceMCP.login(username, password);

        assertNotNull(loginResult);
        assertEquals("jwtToken", loginResult);
        verify(blogServiceAPI).login(username, password);

    }

    @Test
    void mcpToolsAreScannable() {

        // Verify that our MCP tools are discoverable by the Spring AI MCP framework
        assertTrue(applicationContext.containsBean("blogServiceMCP"));

    }
}
