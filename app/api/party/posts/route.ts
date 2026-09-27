import {NextResponse} from 'next/server';
import {createClient} from '@supabase/supabase-js';
const supabase=createClient(process.env.NEXT_PUBLIC_SUPABASE_URL!,process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY!);
export async function GET(){const {data,error}=await supabase.from('pharaoh_posts').select('*').order('created_at',{ascending:false}).limit(50);if(error)return NextResponse.json({posts:[],error:error.message},{status:500});return NextResponse.json({posts:data??[]},{headers:{'cache-control':'no-store'}})}
