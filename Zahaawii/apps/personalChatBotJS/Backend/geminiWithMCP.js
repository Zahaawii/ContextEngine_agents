import { CloudClient } from "chromadb";
import { GoogleGeminiEmbeddingFunction } from "@chroma-core/google-gemini";
import { GoogleGenAI, mcpToTool } from "@google/genai";
import express from "express";
import cors from "cors";
import path from "path";
import { fileURLToPath } from "url";
import { dirname } from "path";
import { Client } from "@modelcontextprotocol/sdk/client/index.js";
import { StreamableHTTPClientTransport } from "@modelcontextprotocol/sdk/client/streamableHttp.js";
import {
  buildMcpUserContent,
  buildUserFacingError,
  extractQuestionText,
  formatVectorContext,
  resolveStaticAssetsDir
} from "./chatbotHelpers.js";

const app = express();
const port = Number(process.env.PORT || 8181);
const mcpBaseUrl = (process.env.MCP_BASE_URL || "http://127.0.0.1:8282").replace(/\/+$/, "");
const embeddingModel = process.env.GEMINI_EMBEDDING_MODEL || "gemini-embedding-001";
const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);
const appRoot = path.resolve(__dirname, "..");
const staticDir = resolveStaticAssetsDir(appRoot);

app.use(express.urlencoded({ extended: true }));
app.use("/llm", express.static(staticDir));
app.use(cors());
app.use(express.json());

function logging(text) {
  const currentDate = new Date();
  console.log(
    `${currentDate.getDate()}/${currentDate.getMonth() + 1}/${currentDate.getFullYear()} ${currentDate.getHours()}:${currentDate.getMinutes()}:${currentDate.getSeconds()}: ${text}`
  );
  console.log("=============================================================");
}

const mcpClient = new Client({
  name: "example-client",
  version: "1.0.0"
});

const transport = new StreamableHTTPClientTransport(new URL(`${mcpBaseUrl}/mcp`));

try {
  await mcpClient.connect(transport);
  logging(`Connected to MCP server at ${mcpBaseUrl}/mcp`);
} catch (error) {
  logging(`Could not connect to MCP server: ${error?.message || error}`);
  throw error;
}

const ai = new GoogleGenAI({ apiKey: process.env.GOOGLE_API_KEY });

const client = new CloudClient({
  apiKey: process.env.CHROMADB_API_KEY,
  tenant: "37f4ff04-8a40-4a5c-ba87-77b9e4b5d60d",
  database: "Test"
});

const embedder = new GoogleGeminiEmbeddingFunction({
  apiKey: process.env.GOOGLE_API_KEY,
  modelName: embeddingModel
});

const collection = await client.getOrCreateCollection({
  name: "test_api",
  embeddingFunction: embedder
});

app.get("/", (_req, res) => {
  res.sendFile(path.join(appRoot, "index.html"));
});

app.get("/healthz", (_req, res) => {
  res.status(200).send("ok");
});

app.post("/api/question", async (req, res) => {
  logging("Entering the POST request /api/question");

  const questionText = extractQuestionText(req.body?.text);
  if (!questionText) {
    logging("Rejected question request: missing or empty text");
    return res.status(400).json({ message: "Question text is required." });
  }

  logging(`Question accepted: ${questionText}`);

  try {
    const results = await collection.query({
      queryTexts: [questionText],
      nResults: 5
    });

    const vectorContext = formatVectorContext(results);
    const content = [{ text: buildMcpUserContent(questionText, vectorContext) }];

    const response = await ai.models.generateContent({
      model: "gemini-flash-latest",
      contents: content,
      config: {
        tools: [mcpToTool(mcpClient)],
        systemInstruction: `
              You are KnowledgeBot.

              Your behavior is governed by three input sources, handled in this order of priority:

              1. MCP Server Tools: For any business-related, technical, operational, or knowledge-dependent question, query connected MCP tools first.
              2. Vector Database Knowledge: If the MCP tool returns vector search results or stored knowledge, use that content as factual grounding.
              3. Simple Conversation: If the user asks casual, non-business questions (hello, who are you, small talk), respond naturally without invoking MCP or the vector store.

              How you must use the data:
              - When you receive text from MCP tools or vector search, interpret it and rewrite it in polished, clear natural language.
              - Do not output raw IDs, embeddings, metadata, scores, or unprocessed text chunks.
              - Extract the meaning, summarize what matters, and answer like a human who understands the topic.
              - You may reorganize, rephrase, combine, simplify, and format the content.

              Rules:
              - Stay strictly grounded in information provided by MCP tools or vector data.
              - Do not hallucinate missing details, invent facts, or rely on external world knowledge.
              - If the user asks a business-related question that cannot be answered by available MCP tools or stored data, respond with:
                "The answer is not within my current knowledge. Please ask the system administrator to upload a relevant knowledge base article so I can assist you."
              - For conversation questions (hello, who are you, etc.), answer normally and politely.
              - For article-writing requests, write only from MCP/vector results relevant to the topic.
              `
      }
    });

    logging("Generated answer successfully");
    return res.send(typeof response.text === "string" ? response.text : "");
  } catch (error) {
    logging(`Failed to process /api/question: ${error?.message || error}`);
    console.error(error);
    return res.status(500).send(buildUserFacingError(error));
  }
});

app.post("/api/database", async (req, res) => {
  logging("Entering the POST request /api/database");

  const id = extractQuestionText(req.body?.id);
  const document = extractQuestionText(req.body?.document);

  if (!id || !document) {
    logging("Rejected database request: missing id or document");
    return res.status(400).send("Document id and document text are required.");
  }

  try {
    await collection.add({
      ids: [id],
      documents: [document],
      metadatas: [req.body?.metadata ?? {}]
    });
    logging(`Stored KBA entry with id ${id}`);
    return res.send(`The KBA has been stored in the database with ${id}`);
  } catch (error) {
    logging(`Failed to upload KBA entry: ${error?.message || error}`);
    console.error(error);
    return res.status(500).send("There has been an error with uploading the text. Save the text and refresh the page.");
  }
});

app.use((err, _req, res, _next) => {
  logging(`Unhandled error: ${err?.message || err}`);
  if (res.headersSent) {
    return;
  }
  res.status(500).json({
    message: "Something went wrong! Please try again later."
  });
});

app.listen(port, () => {
  logging(`REST API server running on port ${port}`);
});
