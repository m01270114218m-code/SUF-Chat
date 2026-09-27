import {NextResponse} from 'next/server';
import {createClient} from '@supabase/supabase-js';
const supabase=createClient(process.env.NEXT_PUBLIC_SUPABASE_URL!,process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY!);
export async function GET(){const {data,error}=await supabase.from('pharaoh_messages').select('*').order('created_at',{ascending:false}).limit(100);if(error)return NextResponse.json({messages:[],error:error.message},{status:500});return NextResponse.json({messages:data??[]},{headers:{'cache-control':'no-store'}})}
export async function POST(req:Request){const body=await req.json();const {data,error}=await supabase.from('pharaoh_messages').insert({sender_id:body.sender_id,receiver_id:body.receiver_id,body:body.body??''}).select().single();if(error)return NextResponse.json({error:error.message},{status:400});return NextResponse.json({message:data})}
