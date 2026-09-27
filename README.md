# Campaign Concept Studio

Full-stack campaign concept studio using Next.js and the current OpenAI Responses API.

The browser sends campaign fields to POST /api/campaign. Only the server route imports the OpenAI SDK and reads OPENAI_API_KEY, so the key never reaches the client.

The server performs Responses API text generation, then Responses API image-generation tool calls. Generated images are returned as base64 data URLs; for production, store assets in object storage and return durable URLs.

Install: npm install, copy .env.example to .env.local, set OPENAI_API_KEY, then run npm run dev. Node.js 20.9+.

Environment: OPENAI_API_KEY, OPENAI_TEXT_MODEL=gpt-6-astra, OPENAI_IMAGE_MODEL=gpt-image-2.5-flare.

Deployment: deploy as a normal Next.js Node application. On Vercel, configure the same server environment variables. Never use NEXT_PUBLIC_OPENAI_API_KEY.

Validation: test empty fields, missing key, normal brief, image-generation failure, and production rate limiting/authentication. Before public launch add authentication, rate limits, usage controls, asset storage and request tracing.

Tune later: text model in OPENAI_TEXT_MODEL; image model in OPENAI_IMAGE_MODEL; prompts and JSON contract in app/api/campaign/route.ts; image size/quality/background in image_generation options; UI in app/globals.css.

OpenAI model guidance: https://developers.openai.com/api/docs/models
Text generation: https://developers.openai.com/api/docs/guides/text
Image generation: https://developers.openai.com/api/docs/guides/tools-image-generation
