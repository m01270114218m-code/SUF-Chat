"use client";

import { useEffect, useRef } from "react";
import { supabase } from "@/lib/supabase";

export default function VoiceClient({roomId,userId,enabled}:{roomId:string;userId:string|null;enabled:boolean}){
 const channelRef=useRef<any>(null),streamRef=useRef<any>(null),peersRef=useRef<Record<string,any>>({}),audiosRef=useRef<Record<string,HTMLAudioElement>>({});
 useEffect(()=>{
  if(!enabled||!userId||typeof window==="undefined")return;
  let active=true;
  const signal=(payload:any)=>{if(channelRef.current)void channelRef.current.send({type:"broadcast",event:"voice",payload})};
  const makePeer=(remote:string,offer:boolean)=>{
   if(peersRef.current[remote])return peersRef.current[remote];
   const PC=(window as any).RTCPeerConnection;
   if(!PC||!streamRef.current)return null;
   const pc:any=new PC({iceServers:[{urls:"stun:stun.l.google.com:19302"},{urls:"stun:stun1.l.google.com:19302"}]});
   peersRef.current[remote]=pc;
   for(const track of streamRef.current.getTracks())pc.addTrack(track,streamRef.current);
   pc.onicecandidate=(e:any)=>{if(e.candidate)signal({kind:"candidate",from:userId,to:remote,candidate:e.candidate})};
   pc.ontrack=(e:any)=>{
    const s=e.streams&&e.streams[0];if(!s)return;
    let a=audiosRef.current[remote];
    if(!a){a=new Audio();a.autoplay=true;audiosRef.current[remote]=a}
    a.srcObject=s;void a.play().catch(()=>{});
   };
   pc.onconnectionstatechange=()=>{if(["failed","closed","disconnected"].includes(pc.connectionState)){pc.close();delete peersRef.current[remote];const a=audiosRef.current[remote];if(a){a.pause();a.srcObject=null;delete audiosRef.current[remote]}}};
   if(offer)void pc.createOffer().then((o:any)=>pc.setLocalDescription(o)).then(()=>signal({kind:"offer",from:userId,to:remote,sdp:pc.localDescription}));
   return pc;
  };
  const start=async()=>{
   try{
    streamRef.current=await (navigator as any).mediaDevices.getUserMedia({audio:{echoCancellation:true,noiseSuppression:true,autoGainControl:true},video:false});
    if(!active)return;
    const ch=supabase.channel("voice-"+roomId);channelRef.current=ch;
    ch.on("broadcast",{event:"voice"},async({payload}:any)=>{
     if(!payload||payload.from===userId||!payload.to||!(payload.to==="*"||payload.to===userId))return;
     if(payload.kind==="hello"){if(userId<payload.from)makePeer(payload.from,true);return}
     const pc=makePeer(payload.from,false);if(!pc)return;
     try{
      if(payload.kind==="offer"){await pc.setRemoteDescription(payload.sdp);const answer=await pc.createAnswer();await pc.setLocalDescription(answer);signal({kind:"answer",from:userId,to:payload.from,sdp:pc.localDescription})}
      else if(payload.kind==="answer")await pc.setRemoteDescription(payload.sdp);
      else if(payload.kind==="candidate"&&payload.candidate)await pc.addIceCandidate(payload.candidate);
     }catch{}
    }).subscribe((status)=>{if(status==="SUBSCRIBED")signal({kind:"hello",from:userId,to:"*"})});
   }catch{}
  };
  void start();
  return()=>{active=false;if(channelRef.current){supabase.removeChannel(channelRef.current);channelRef.current=null}Object.values(peersRef.current).forEach((p:any)=>p.close());peersRef.current={};Object.values(audiosRef.current).forEach((a)=>{a.pause();a.srcObject=null});audiosRef.current={};streamRef.current?.getTracks().forEach((t:any)=>t.stop());streamRef.current=null};
 },[roomId,userId,enabled]);
 return null;
}
