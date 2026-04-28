import fs from "fs";
import path from "path";

export function resolveStaticAssetsDir(appRoot) {
  const llmDir = path.join(appRoot, "llm");
  if (fs.existsSync(llmDir)) {
    return llmDir;
  }

  const frontendDir = path.join(appRoot, "Frontend");
  if (fs.existsSync(frontendDir)) {
    return frontendDir;
  }

  return appRoot;
}

export function extractQuestionText(rawText) {
  if (typeof rawText !== "string") {
    return null;
  }

  const questionText = rawText.trim();
  return questionText.length > 0 ? questionText : null;
}

export function formatVectorContext(results) {
  const docs = Array.isArray(results?.documents)
    ? results.documents.flat(Infinity).filter((entry) => typeof entry === "string" && entry.trim().length > 0)
    : [];

  if (docs.length === 0) {
    return "No vector context available.";
  }

  return docs.slice(0, 5).map((doc, index) => `[${index + 1}] ${doc}`).join("\n\n");
}

export function buildMcpUserContent(questionText, vectorContext) {
  return `Take the given question and search MCP tools for the most relevant information.
Use vector context as factual grounding when relevant.
If authentication is required, use the available MCP login tool flow and never expose secrets.

VECTOR CONTEXT:
${vectorContext}

QUESTION:
${questionText}`;
}

export function buildChromaUserContent(questionText, vectorContext) {
  return `Take the provided vector context and answer the user question only with grounded information.

VECTOR CONTEXT:
${vectorContext}

QUESTION:
${questionText}`;
}

export function buildUserFacingError(error) {
  const message = String(error?.message || "");

  if (message.includes("The model is overloaded")) {
    return "The model is overloaded, please wait a couple of seconds.";
  }

  if (message.includes("You exceeded your current quota")) {
    return "You exceeded your current quota, please wait a minute to refresh the quota.";
  }

  return "Something went wrong while processing your request. Please try again later.";
}
