# MCP Server Project

This project implements a **Model Context Protocol (MCP) server**.

The system is now fully integrated with the AssistantAI and provides dynamic tool execution, data management, authentication, CI/CD, and deployment.  
It has evolved into a feature-rich backend agent supporting real workflows across ChromaDB and your blog platform.

---

## 🚀 Current Capabilities

### 🧩 MCP Server
The server fully supports interaction with **ChromaDB** and your **Blog API**.

#### **ChromaDB Tools**
- Read documents
- Upload documents
- Delete documents
- AssistantAI can autonomously create/store new knowledge

#### **Blog Tools**
- Read all blog posts
- Read posts by author
- Log in and obtain a JWT token
- Delete blog posts
- Create structured blog posts

### 🤖 AssistantAI Integration
AssistantAI is fully connected and can:
- Upload new articles into ChromaDB
- Read, create, and delete blog posts
- Authenticate as a user or service account
- Perform actions as an intelligent agent

### 🔄 CI/CD + Testing
The project includes:
- **CI/CD pipeline** using GitHub Actions
- **Unit tests** (Mockito, JUnit)
- **Integration tests** validating MCP → Backend → ChromaDB flow

### 🌍 Deployment
The MCP server is live and available for external MCP clients at:

**http://mcp.zaak.dk/mcp**

---

## 📚 Resources Used During Development

- [MCP Introduction](https://modelcontextprotocol.io/docs/getting-started/intro)
- [Build an MCP Server (Java)](https://modelcontextprotocol.io/docs/develop/build-server#java)
- [Spring AI MCP Server Boot Docs](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html#_configuration_properties)
- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/index.html)
- [Spring AI MCP Weather Example](https://github.com/spring-projects/spring-ai-examples/blob/main/model-context-protocol/weather/manual-webflux-server/src/main/java/org/springframework/ai/mcp/sample/server/WeatherApiClient.java)
- [Dan Vega – DVaaS Example](https://github.com/danvega/dvaas)
- [Spring MCP Intro Blog](https://spring.io/blog/2025/09/16/spring-ai-mcp-intro-blog)
- [Spring MCP Streamable Server Docs](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-streamable-http-server-boot-starter-docs.html)
- [MCP Overview (YouTube)](https://www.youtube.com/watch?v=TSFkdlreRMQ)
- [OpenAI Tools & MCP Guide](https://platform.openai.com/docs/guides/tools-connectors-mcp)
- [ChromaDB Configuration (Spring AI)](https://docs.spring.io/spring-ai/reference/api/vectordbs/chroma.html#_configuration_properties)
- [Google GenAI Embeddings Docs](https://docs.spring.io/spring-ai/reference/api/embeddings/google-genai-embeddings-text.html)
- [ChromaApi JavaDocs](https://docs.spring.io/spring-ai/docs/current/api/org/springframework/ai/chroma/vectorstore/ChromaApi.html)
- [VectorStore JavaDocs](https://docs.spring.io/spring-ai/docs/current/api/org/springframework/ai/vectorstore/VectorStore.html)
- [ParameterizedTypeReference Guide](https://www.baeldung.com/spring-parameterized-type-reference)
- [AAA Testing Pattern](https://automationpanda.com/2020/07/07/arrange-act-assert-a-pattern-for-writing-good-tests/)
- [Mockito Verify](https://www.baeldung.com/mockito-verify)
- [Testing Void Methods with Mockito](https://dzone.com/articles/unit-testing-void-functions-with-mockito)
- [Mockito Issue #3014](https://github.com/mockito/mockito/issues/3014)
- [Mockito ANY Guide](https://www.browserstack.com/guide/using-mockito-any)
- [GitHub Actions – Env Variables](https://docs.github.com/en/actions/how-tos/write-workflows/choose-what-workflows-do/use-variables#using-the-env-context-to-access-environment-variable-values)
- [Ubuntu Deployment with GitHub Actions](https://blog.devops.dev/effortless-ubuntu-server-deployments-with-github-actions-%EF%B8%8F-3ded9ea699e8)

(And many additional references explored during implementation.)

---

## 🧭 Next Steps

### 1. Improve Backend Robustness & Error Handling
- Strong typed error responses
- Input validation
- Fallback logic
- Logging + tracing

### 2. Expand Toolset
- Blog comment tools
- User profile tools
- Multi-step workflows
- Combined ChromaDB + blog tools

### 3. Enhance AssistantAI Behavior
- Better tool-calling logic
- Planning + multi‑step reasoning
- Dynamic context retrieval
- More autonomous agent abilities

### 4. Optional Dashboard UI
- Browse ChromaDB
- Trigger tools manually
- View logs, metadata, and execution traces

---

## 📄 License

MIT License.
