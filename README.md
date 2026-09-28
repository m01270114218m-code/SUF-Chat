# Campaign Concept Studio

Full-stack campaign concept studio using Next.js and the current OpenAI Responses API.

Repository replacement is being performed from the bundled project ZIP.

The browser sends campaign fields to POST /api/campaign. Only the server route imports the OpenAI SDK and reads OPENAI_API_KEY, so the key never reaches the client.

Install: npm install, copy .env.example to .env.local, set OPENAI_API_KEY, then run npm run dev. Node.js 20.9+.
