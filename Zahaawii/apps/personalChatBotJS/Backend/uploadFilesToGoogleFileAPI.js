import { createPartFromUri, GoogleGenAI } from "@google/genai";
import * as fs from 'fs';

console.log("Hello, this is a chatbot, I am uploading the files");

console.log("");

const ai = new GoogleGenAI({ apiKey: "" });

  const file = await ai.files.upload ({
    file: 'KBA/fly.pdf',
    config: {
      displayName: 'fly.pdf',
    },
  });

  let getFile = await ai.files.get({ name: file.name });
    while (getFile.state === 'PROCESSING') {
      getFile = await ai.files.get( {name: file.name });
      console.log(`Current file status: ${getFile.state}`);
      console.log(`File is still processing, retrying in 5 seconds`);

      await new Promise((resolve) => {
        setTimeout(resolve, 5000);
      });
    }

    if(file.state === `FAILED`) {
      throw new Error(`File processing failed`);
    }

    console.log(`File uploaded and ready: ${file.name}`);

      const listResponse = await ai.files.list({ config: { pageSize: 10 } });
      for await (const file of listResponse) {
       console.log(file.name);
  }
