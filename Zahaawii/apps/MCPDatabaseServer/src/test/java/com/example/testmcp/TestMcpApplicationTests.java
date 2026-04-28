package com.example.testmcp;

import com.example.testmcp.mcpService.BlogServiceMCP;
import com.example.testmcp.mcpService.KBAServiceMCP;
import org.junit.jupiter.api.Test;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class TestMcpApplicationTests {

    /*
    *******************************************************
    FOLLOWING DAN VEGA - GITHUB REPO DVAAS FOR TEST OF MCP
    *******************************************************
     */

    @MockitoBean
    private BlogServiceMCP blogServiceMCP;

    @MockitoBean
    private KBAServiceMCP kbaServiceMCP;

    @MockitoBean
    private VectorStore vectorStore;

    @Test
    void contextLoads() {
    }

}
