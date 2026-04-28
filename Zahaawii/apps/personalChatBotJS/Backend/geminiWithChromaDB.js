import { CloudClient } from "chromadb";
import { GoogleGeminiEmbeddingFunction } from "@chroma-core/google-gemini";
import { GoogleGenAI } from "@google/genai";
import express from "express";
import cors from "cors";
import path from "path";
import { fileURLToPath } from "url";
import { dirname } from "path";
import {
  buildChromaUserContent,
  buildUserFacingError,
  extractQuestionText,
  formatVectorContext,
  resolveStaticAssetsDir
} from "./chatbotHelpers.js";

const app = express();
const port = Number(process.env.PORT || 8181);
const embeddingModel = process.env.GEMINI_EMBEDDING_MODEL || "gemini-embedding-001";
const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);
const appRoot = path.resolve(__dirname, "..");
const staticDir = resolveStaticAssetsDir(appRoot);

app.use(express.urlencoded({ extended: true }));
app.use("/llm", express.static(staticDir));
app.use(cors());
app.use(express.json());

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
  console.log("Entering the POST request /api/question");

  const questionText = extractQuestionText(req.body?.text);
  if (!questionText) {
    return res.status(400).json({ message: "Question text is required." });
  }

  console.log(`Question accepted: ${questionText}`);

  try {
    const results = await collection.query({
      queryTexts: [questionText],
      nResults: 5
    });

    const vectorContext = formatVectorContext(results);
    const content = [{ text: buildChromaUserContent(questionText, vectorContext) }];

    const response = await ai.models.generateContent({
      model: "gemini-2.5-flash",
      contents: content,
      config: {
        systemInstruction: `
        You are a personal knowledge-base chatbot with the name KnowledgeBot.
        Your sole purpose is to answer questions strictly based on vector data returned from the query.

        Rules:
        1. Use only information in the query context.
        2. If a user asks a question not covered by the vector query:
           "The answer is not within my current knowledge. Please ask the system administrator to upload a relevant knowledge base article so I can assist you."
        3. Do not generate, assume, infer, or hallucinate content beyond user-provided data.
        4. Do not access external world knowledge.
        5. Keep answers concise and accurate.
      `,
        thinkingConfig: {
          thinkingBudget: 0
        }
      }
    });

    return res.send(typeof response.text === "string" ? response.text : "");
  } catch (error) {
    console.error(error);
    return res.status(500).send(buildUserFacingError(error));
  }
});

app.post("/api/database", async (req, res) => {
  console.log("Inside POST /api/database");

  const id = extractQuestionText(req.body?.id);
  const document = extractQuestionText(req.body?.document);

  if (!id || !document) {
    return res.status(400).send("Document id and document text are required.");
  }

  try {
    await collection.add({
      ids: [id],
      documents: [document],
      metadatas: [req.body?.metadata ?? {}]
    });

    return res.send(`The KBA has been stored in the database with ${id}`);
  } catch (error) {
    console.error(error);
    return res.status(500).send("There has been an error with uploading the text. Save the text and refresh the page.");
  }
});

app.use((err, _req, res, _next) => {
  console.error(err);
  if (res.headersSent) {
    return;
  }
  res.status(500).json({
    message: "Something went wrong! Please try again later."
  });
});

app.listen(port, () => {
  console.log(`REST API server running on port ${port}`);
});
