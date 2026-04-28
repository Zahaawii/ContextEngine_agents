import fs from "node:fs/promises"
import path from "node:path";
import { fileURLToPath } from "node:url";
import { CloudClient } from "chromadb";
import { GoogleGeminiEmbeddingFunction } from "@chroma-core/google-gemini";

//Connect to the chromaDB cloud (Might switch to local for performance and economy)
const client = new CloudClient({
  apiKey: process.env.CHROMADB_API_KEY,
  tenant: '37f4ff04-8a40-4a5c-ba87-77b9e4b5d60d',
  database: 'Test'
});

//Connect to Googles embedding function
const embeddingModel = process.env.GEMINI_EMBEDDING_MODEL || "gemini-embedding-001";
const embedder = new GoogleGeminiEmbeddingFunction({
  apiKey: process.env.GOOGLE_API_KEY,
  modelName: embeddingModel,
});
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const uploadSourceFile = process.env.CHROMA_UPLOAD_FILE || path.resolve(__dirname, "../KBA/test.txt");

//Take the txt file, read it line by line and add it to the String and then add it to the collection
let file = "";
try {
file = await fs.readFile(uploadSourceFile, 'utf8')
console.log(file);
} catch (error) {
    console.error(error);
}

//Get the "Database"/Collection from ChromaDB and store it
const collection = await client.getOrCreateCollection({
    name: "test_api",
    embeddingFunction: embedder, // <-- this parameter tells chromaDB when I add a new file to the DB it calls the embedder and stores it in vectors
})

//I am actually not quite sure why I have this, but the documentation used it
//const embeddings = await embedder.generate(file); - I found out since I use line 30 - this is not relevant anymore



//Add following documents to my chromaDB - Needs to be modified so it auto increment the id and add all the files that is stored in KBA folder
try {
    await collection.add({
    ids: ["id5" ],
    documents: [ file ],
});
console.log("Collection added");
} catch (err) {
    console.error(err);
}

//Control the query if you want to see what gets returned when you query a text
const results = await collection.query({
  queryTexts: ["Is it possible to delete a user?"],
  //Choose the result number, std is 10: 
  nResults: 3,
});

//Get the result from the query - its possible to write results.documents as it is only the text that is relevant
console.log(results);
