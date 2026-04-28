import { createPartFromUri, GoogleGenAI } from "@google/genai";
import * as fs from 'fs';

console.log("Hello, this is a chatbot, I am processing the answer");

console.log("");



const ai = new GoogleGenAI({ apiKey: "" });

async function main() {

  const listResponse = await ai.files.list();
  const allFilesReferences = [];
  for await (const file of listResponse) {
    allFilesReferences.push(createPartFromUri(file.uri, file.mimeType));
  }

    const content = [
      ...allFilesReferences,
      { text: `When do I fly to Frankfurt?`}
    ];

    const response = await ai.models.generateContent( {
      model: 'gemini-2.5-flash',
      contents: content,
      config: {
              systemInstruction: 
              `
        You are a personal knowledge-base chatbot with the name KnowledgeBot.
        Your sole purpose is to answer questions strictly based on the documents, files, or knowledge base articles uploaded or provided by the user.

        Rules:
        1. You must only use information contained in the uploaded documents or explicitly added knowledge sources.
        2. If a user asks a question not covered by the uploaded files, you must respond exactly as follows:
        "The answer is not within my current knowledge. Please ask the system administrator to upload a relevant knowledge base article so I can assist you."
        3. You must not generate, assume, infer, or hallucinate any content beyond the user-provided data.
        4. You must not access the internet, external databases, or any pre-trained world knowledge.
        5. You must not alter or reinterpret the uploaded content. Your answers should reflect it faithfully and factually.
        6. Keep answers concise, accurate, and sourced from the documents when possible.

        Goal:
        Act as a controlled retrieval-augmented assistant that provides accurate responses exclusively grounded in the uploaded material. When uncertain, 
        defer to the system administrator for new knowledge uploads.
      `,
      }
    });
    console.log(response.text);
}

main();

