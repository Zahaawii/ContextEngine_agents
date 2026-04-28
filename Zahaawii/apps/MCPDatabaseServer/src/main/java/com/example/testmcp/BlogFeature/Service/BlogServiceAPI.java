package com.example.testmcp.BlogFeature.Service;


import com.example.testmcp.BlogFeature.DTO.AuthLogin;
import com.example.testmcp.BlogFeature.DTO.BlogDTO;
import com.example.testmcp.BlogFeature.DTO.CreateBlogPostDTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

/*
Since my blog is uploaded and deployed to the Cloud - we will test this way, instead of setting up the database again.
As the application is delpoyed on a server with the DB, we cannot access the same data, but its also available online
When deployed we will connect it to the backend instead of fetching the data for consistency and perfomance
 */

@Service
public class BlogServiceAPI {

    private final String baseApiUrl = Optional.ofNullable(System.getenv("BLOG_API_BASE_URL"))
            .orElse("https://zaak.dk/api")
            .replaceAll("/+$", "");
    private final String blogApiUrl = baseApiUrl + "/v1/blog/";

    public List<BlogDTO> getAllBlogPost() {
        try {
            return RestClient.builder().build()
                    .get()
                    .uri(blogApiUrl + "getallblogpost")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<BlogDTO>>(){});
        } catch (Exception e) {
            throw new RuntimeException("There was an error fetching the blog data: " + e.getMessage());
        }
    }

    public List<BlogDTO> getAllByAuthor(String author) {
        try {
            return RestClient.builder().build()
                    .get()
                    .uri(blogApiUrl + "getbyusername/" + author)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<BlogDTO>>() {});
        } catch (Exception e) {
            throw new RuntimeException("There was a issue fetching the data by an author: " + e.getMessage());
        }
    }

    public String deletePostById(int id, String jwtToken) {
        try {
            return RestClient.builder().build()
                    .delete()
                    .uri(blogApiUrl + "deletepost/" + id)
                    //Right now the method below only works with a JWT token - that I have not found out how to do yet - To be continued
                    //It works if I copy a jwt token when logged in... For now the MCP server can log in and retrieve the JWT token and upload it with the header
                    .header("Authorization","Bearer " + jwtToken)
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            throw new RuntimeException("Error deleting the blog post", e);
        }
    }

    //Testing to see if the LLM can retrieve data and use it
    public String login(String username, String password) {
        AuthLogin retrieveJWT = new AuthLogin(username, password);
        return RestClient.builder().build()
                .post()
                .uri(baseApiUrl + "/v1/users/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(retrieveJWT)
                .retrieve()
                .body(String.class);
    }

    public String createBlogPost(String subject, String body, String category, Long userId, Date publishDate, String jwtToken) {
        CreateBlogPostDTO createBlogPost = new CreateBlogPostDTO(subject, body, category, userId, publishDate);
        return RestClient.builder().build()
                .post()
                .uri(blogApiUrl + "saveblogpost")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(createBlogPost)
                .retrieve()
                .body(String.class);
    }
}
