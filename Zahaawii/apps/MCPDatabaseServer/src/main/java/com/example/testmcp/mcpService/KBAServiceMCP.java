package com.example.testmcp.mcpService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class KBAServiceMCP {


    @Autowired
    VectorStore vectorStore;

    private static final Logger log = LoggerFactory.getLogger(KBAServiceMCP.class);

    /**
     * Method to get the most relevant items in the vector database
     * @param question for the AI that concludes it has to check the vector database
     * @return the five most relevant articles from the vector database
     * @throws Exception
     */

    @McpTool(name = "FindArticles", description = "Gets the nearest similarities of the question")
    public List<Document> getSimiliaritySearch(@McpToolParam String question) throws Exception {

        try {
            log.info("The method getSimilaritySearch is being accessed with question: {}", question);

            return this.vectorStore.similaritySearch(SearchRequest.builder()
                    .query(question)
                    .topK(5)
                    .build());
        } catch (Exception e) {
            log.error("Failed to retrieve data from the method getSimiliaritySearch: {}", question);
            throw new RuntimeException("Couldn't find anything: " + e.getMessage());
        }
    }

    /**
     * Makes the AI able to upload articles based on its own knowledge or what it has been giving of information
     * @param id for the article to identify it
     * @param content what the article is about
     * @param tagName makes it easier for the vector search
     * @param tagDescription makes it easier to vector search
     * @return approves if the article was added or not
     */
    @McpTool(name = "addArticles", description = "Gives access to add new articles to the vector database")
    public String addArticlesToDatabase(@McpToolParam String id, String content, String tagName, String tagDescription) {
        log.info("This addArticlesToDatabase is being accessed");
        try {
            log.info("Trying to upload document");
            List<Document> documents = List.of(new Document(id, content, Map.of(tagName, tagDescription)));
            vectorStore.add(documents);
            log.info("Success");
            return "Added document to the database";
        } catch (Exception e) {
            log.error("Failed to upload data from the method addArticleToDatabase");
            return "Error adding article to database";
        }
    }

    @McpTool(name = "DeleteArticle", description = "Gives access to delete a article in the vector database")
    public String deleteArticlesFromDatabase(@McpToolParam String id) {
        log.info("This method deleteArticlesFromDatabase is being acceesed");

        try {
            log.info("Trying to delete an article with id: {}", id);
            vectorStore.delete(List.of(id));
            log.info("Deleted the article");
            return "Succesfully deleted: "  + id;
        } catch (Exception e) {
            log.error("Could not delete the article: {} ", id);
            throw new RuntimeException(e);
        }
    }
}
