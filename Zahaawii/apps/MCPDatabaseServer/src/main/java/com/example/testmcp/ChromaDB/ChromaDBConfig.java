
package com.example.testmcp.ChromaDB;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chroma.vectorstore.ChromaApi;
import org.springframework.ai.chroma.vectorstore.ChromaVectorStore;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;


/*
This config is for my backend to talk with CHROMADB cloud as the standard spring solution doesnt work as inteded.
It gives me permission denied, whatever I try. I will come back to the standard method, when available.
 */

@Configuration
public class ChromaDBConfig {

    private final String chromaApiToken = System.getenv("CHROMADB_API_KEY");

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder()
                .requestFactory(new SimpleClientHttpRequestFactory())
                .defaultHeader("X-Chroma-Token", chromaApiToken); // <--- Claude Code gave me this to make it work, because Chroma do not use Bearer header
    }

    @Bean
    public ChromaApi chromaApi(RestClient.Builder restClientBuilder) {
        String chromaUrl = "https://api.trychroma.com";
        return new ChromaApi(chromaUrl, restClientBuilder, new ObjectMapper());
    }

    @Bean
    public VectorStore chromaVectorStore(EmbeddingModel embeddingModel, ChromaApi chromaApi) {
        return ChromaVectorStore.builder(chromaApi, embeddingModel)
                .tenantName("37f4ff04-8a40-4a5c-ba87-77b9e4b5d60d")
                .databaseName("Test")
                .collectionName("test_api")
                .initializeSchema(false)
                .build();
    }
}