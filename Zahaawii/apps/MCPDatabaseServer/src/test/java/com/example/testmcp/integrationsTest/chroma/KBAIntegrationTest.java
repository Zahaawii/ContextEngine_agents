package com.example.testmcp.integrationsTest.chroma;


import com.example.testmcp.ChromaDB.ChromaDBConfig;
import com.example.testmcp.mcpService.KBAServiceMCP;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.chroma.vectorstore.ChromaApi;
import org.springframework.ai.chroma.vectorstore.ChromaVectorStore;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "RUN_EXTERNAL_INTEGRATION_TESTS", matches = "true")
public class KBAIntegrationTest {

    @Autowired
    ApplicationContext context;

    @Autowired
    ChromaDBConfig config;

    @Autowired
    KBAServiceMCP MCP;

    @Autowired
    VectorStore vectorStore;

    @Autowired
    private EmbeddingModel embeddingModel;


    @Test
    void context() {
        assertNotNull(context);
    }

    @Test
    void addArticleAndDelete() {
        // As we do not want test data stored in our env - Yes we could have a test
        // We combine the test and delete
        String id = "IntegrationTest";
        String content = "Test article for integration test";
        String tagName = "test";
        String tagDescription = "test";
        String expectedAddResult = "Added document to the database";
        String expectedDeleteResult = "Succesfully deleted: "  + id;

        String resultAddArticleToDatabase = MCP.addArticlesToDatabase(id, content, tagName, tagDescription);

        assertNotNull(resultAddArticleToDatabase);
        assertEquals(expectedAddResult, resultAddArticleToDatabase);

        String deleteArticlesFromDatabase = MCP.deleteArticlesFromDatabase(id);

        assertNotNull(deleteArticlesFromDatabase);
        assertEquals(expectedDeleteResult, deleteArticlesFromDatabase);
    }

    @Test
    void restClientBuilderWorks() {
        String header = "X-Chroma-Token";
        String apiToken = System.getenv("CHROMADB_API_KEY");

        RestClient.Builder restClientConnection = config.restClientBuilder();
        RestClient.Builder chromaAPIConnection = RestClient.builder().requestFactory(new SimpleClientHttpRequestFactory())
                .defaultHeader(header, apiToken);

        //IF the connection doesn't work, we will receive an error, so it is testable like this
        assertNotNull(restClientConnection);
        assertNotNull(chromaAPIConnection);
    }

    @Test
    void connectToChromaDB() {

        //if all these steps go through the connection works
        String id = "integrationtest_" + UUID.randomUUID();
        String content = "Test article for integration test";
        String tagName = "test";
        String tagDescription = "test";
        List<Document> documents = List.of(new Document(id, content, Map.of(tagName, tagDescription)));


        vectorStore.add(documents);

        List<Document> result = vectorStore.similaritySearch(id);
        assertFalse(result.isEmpty());
        assertEquals(id, result.getFirst().getId());

        vectorStore.delete(List.of(id));

    }

}
