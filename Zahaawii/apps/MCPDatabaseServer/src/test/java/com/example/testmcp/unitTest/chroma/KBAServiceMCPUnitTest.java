package com.example.testmcp.unitTest.chroma;


import com.example.testmcp.mcpService.KBAServiceMCP;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Map;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@SpringBootTest
public class KBAServiceMCPUnitTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private KBAServiceMCP kbaServiceMCP;

    @MockitoBean
    VectorStore vectorStore;

    @Test
    void contextLoads() {
        assertNotNull(applicationContext);
    }

    @Test
    void getSimilaritySearch() throws Exception {

        // ARRANGE
        String question = "hey";
        List<Document> test = List.of(new Document(question));
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(test);

        // ACT
        List<Document> getAllTest = kbaServiceMCP.getSimiliaritySearch(question);

        // ASSERT
        assertNotNull(getAllTest);
        verify(vectorStore).similaritySearch(any(SearchRequest.class));
    }

    @Test
    void addArticleToDatabase() throws Exception {

        // ARRANGE
        String id = "id";
        String content = "content";
        String tagName = "tagName";
        String tagDescription = "tagName";
        String success = "Added document to the database";
        List<Document> documents = List.of(new Document(id, content, Map.of(tagName, tagDescription)));

        doNothing().when(vectorStore).add(documents);

        // ACT
        String result = kbaServiceMCP.addArticlesToDatabase(id, content, tagName, tagDescription);

        // ASSERT
        assertNotNull(result);
        assertEquals(success, result);
        verify(vectorStore).add(documents);

    }

    @Test
    void deleteArticleFromDatabase() throws Exception {

        // ARRANGE
        String id = "delete";
        doNothing().when(vectorStore).delete(List.of(id));

        // ACT
        String result = kbaServiceMCP.deleteArticlesFromDatabase(id);

        // ASSERT
        assertNotNull(result);
        assertEquals("Succesfully deleted: delete", result);
        verify(vectorStore).delete(List.of(id));
    }


}
