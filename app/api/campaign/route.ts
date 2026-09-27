import OpenAI from "openai";
import { NextResponse } from "next/server";
export const runtime="nodejs";
const textModel=process.env.OPENAI_TEXT_MODEL||"gpt-6-astra";
const imageModel=process.env.OPENAI_IMAGE_MODEL||"gpt-image-2.5-flare";
function parse(raw:string){return JSON.parse(raw.trim())}
export async function POST(req:Request){
 if(!process.env.OPENAI_API_KEY)return NextResponse.json({error:"OPENAI_API_KEY is not configured on the server."},{status:500});
 try{
  const client=new OpenAI({apiKey:process.env.OPENAI_API_KEY});
  const b=await req.json();
  for(const k of ["brief","audience","product","tone","channels"])if(typeof b[k]!=="string"||!b[k].trim())return NextResponse.json({error:"Please complete every campaign field."},{status:400});
  const prompt="Create a concise marketing campaign direction. Return ONLY valid JSON shaped as concept:string, variants:[{headline:string,body:string} x3], checklist:string[] with 6 items, imagePrompts:string[] with 2 items. Make it practical and channel-aware. Do not invent unsupported product claims. Brief: "+b.brief+" Audience: "+b.audience+" Product: "+b.product+" Tone: "+b.tone+" Channels: "+b.channels;
  const text=await client.responses.create({model:textModel,reasoning:{effort:"low"},instructions:"You are a senior creative strategist. Be specific, concise and commercially useful.",input:prompt});
  const creative=parse(text.output_text);
  const prompts=Array.isArray(creative.imagePrompts)?creative.imagePrompts.slice(0,2):[];
  const images=await Promise.all(prompts.map(async(p:string)=>{
   const out=await client.responses.create({model:textModel,input:"Generate a polished campaign visual. "+p,tools:[{type:"image_generation",model:imageModel,quality:"medium",size:"1024x1024",background:"opaque"}],tool_choice:{type:"image_generation"}});
   const call=out.output.find((x:any)=>x.type==="image_generation_call") as any;
   return call?.result?"data:image/png;base64,"+call.result:null;
  }));
  return NextResponse.json({...creative,images:images.filter(Boolean)});
 }catch(e){console.error(e);return NextResponse.json({error:e instanceof Error?e.message:"OpenAI generation failed."},{status:500})}
}