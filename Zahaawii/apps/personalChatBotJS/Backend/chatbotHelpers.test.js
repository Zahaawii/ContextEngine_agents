import test from "node:test";
import assert from "node:assert/strict";
import fs from "fs";
import os from "os";
import path from "path";
import {
  buildMcpUserContent,
  buildUserFacingError,
  extractQuestionText,
  formatVectorContext,
  resolveStaticAssetsDir
} from "./chatbotHelpers.js";

test("resolveStaticAssetsDir prefers llm directory", () => {
  const baseDir = fs.mkdtempSync(path.join(os.tmpdir(), "chatbot-helpers-"));
  const llmDir = path.join(baseDir, "llm");
  const frontendDir = path.join(baseDir, "Frontend");

  fs.mkdirSync(llmDir);
  fs.mkdirSync(frontendDir);

  try {
    assert.equal(resolveStaticAssetsDir(baseDir), llmDir);
  } finally {
    fs.rmSync(baseDir, { recursive: true, force: true });
  }
});

test("resolveStaticAssetsDir falls back to Frontend directory", () => {
  const baseDir = fs.mkdtempSync(path.join(os.tmpdir(), "chatbot-helpers-"));
  const frontendDir = path.join(baseDir, "Frontend");

  fs.mkdirSync(frontendDir);

  try {
    assert.equal(resolveStaticAssetsDir(baseDir), frontendDir);
  } finally {
    fs.rmSync(baseDir, { recursive: true, force: true });
  }
});

test("extractQuestionText validates and trims input", () => {
  assert.equal(extractQuestionText("  hello world  "), "hello world");
  assert.equal(extractQuestionText(""), null);
  assert.equal(extractQuestionText("   "), null);
  assert.equal(extractQuestionText(undefined), null);
});

test("formatVectorContext flattens and formats query results", () => {
  const formatted = formatVectorContext({
    documents: [["alpha"], ["beta", "gamma"]]
  });

  assert.match(formatted, /\[1\] alpha/);
  assert.match(formatted, /\[2\] beta/);
  assert.match(formatted, /\[3\] gamma/);
});

test("buildMcpUserContent includes question and context without hardcoded credentials", () => {
  const content = buildMcpUserContent("How many posts exist?", "[1] blog data");
  assert.match(content, /How many posts exist\?/);
  assert.match(content, /\[1\] blog data/);
  assert.equal(content.includes("McpTest"), false);
  assert.equal(content.includes("1234"), false);
});

test("buildUserFacingError maps known model errors and falls back safely", () => {
  assert.equal(
    buildUserFacingError(new Error("The model is overloaded")),
    "The model is overloaded, please wait a couple of seconds."
  );
  assert.equal(
    buildUserFacingError(new Error("You exceeded your current quota")),
    "You exceeded your current quota, please wait a minute to refresh the quota."
  );
  assert.equal(
    buildUserFacingError(new Error("random failure")),
    "Something went wrong while processing your request. Please try again later."
  );
});
