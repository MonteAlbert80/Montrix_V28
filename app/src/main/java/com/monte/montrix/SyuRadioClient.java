package com.monte.montrix;

import android.content.*;
import android.os.*;
import android.util.Log;
import java.util.*;

/** FYT direct SYU toolkit bridge. OEM radio remains installed as safe fallback. */
public final class SyuRadioClient {
 static final String TAG="SYU_RADIO", TK="com.syu.ipc.IRemoteToolkit", MOD="com.syu.ipc.IRemoteModule", CB="com.syu.ipc.IModuleCallback";
 public interface Listener { void onRadio(String freq,String ps,String rt); }
 private final Context context; private final Listener listener;
 private volatile IBinder radio,main; private boolean bound;
 private final int[] updates={0,1,2,3,5,10,11,13,14,15,16,21,22,26,27};
 public SyuRadioClient(Context context,Listener listener){this.context=context.getApplicationContext();this.listener=listener;}
 private final Binder callback=new Binder(){ {attachInterface(null,CB);} @Override protected boolean onTransact(int code,Parcel data,Parcel reply,int flags)throws RemoteException {
  if(code==INTERFACE_TRANSACTION){if(reply!=null)reply.writeString(CB);return true;}
  if(code!=1){Log.i(TAG,"DIRECT callback unknown transaction="+code);return super.onTransact(code,data,reply,flags);}
  data.enforceInterface(CB);int id=data.readInt();int[] ints=data.createIntArray();float[] floats=data.createFloatArray();String[] strings=data.createStringArray();
  Log.i(TAG,"DIRECT RAW update="+id+" ints="+Arrays.toString(ints)+" floats="+Arrays.toString(floats)+" strings="+Arrays.toString(strings));
  try{ if(id==1&&ints!=null&&ints.length>0){int v=ints[0];String f=v>=7600&&v<=10800?String.format(Locale.US,"%.1f FM",v/100f):v>=760&&v<=1080?String.format(Locale.US,"%.1f FM",v/10f):v+" (raw)";listener.onRadio(f,null,null);}
   else if((id==14||id==10||id==26)&&strings!=null&&strings.length>0)listener.onRadio(null,strings[0],null);
   else if((id==13||id==11)&&strings!=null&&strings.length>0)listener.onRadio(null,null,strings[0]);
  }catch(Throwable e){Log.e(TAG,"Update handler error",e);}if(reply!=null)reply.writeNoException();return true;
 }};
 private final ServiceConnection connection=new ServiceConnection(){
  @Override public void onServiceConnected(ComponentName name,IBinder toolkit){
   Log.i(TAG,"DIRECT toolkit connected "+name);
   try{radio=module(toolkit,1);main=module(toolkit,0);Log.i(TAG,"DIRECT modules radio="+(radio!=null)+" main="+(main!=null));
    if(radio!=null){for(int id:updates)register(id); probeGetter(13); probeGetter(14); probeGetter(5);}
   }catch(Throwable e){Log.e(TAG,"DIRECT initialization failed",e);}
  }
  @Override public void onServiceDisconnected(ComponentName name){radio=null;main=null;Log.w(TAG,"DIRECT toolkit disconnected");}
 };
 public void start(){try{
  Intent intent=new Intent("com.syu.ms.toolkit").setPackage("com.syu.ms");
  android.content.pm.ResolveInfo ri=context.getPackageManager().resolveService(intent,0);
  Log.i(TAG,"DIRECT resolved service="+(ri==null?"NONE":ri.serviceInfo.packageName+"/"+ri.serviceInfo.name));
  bound=context.bindService(intent,connection,Context.BIND_AUTO_CREATE);
  Log.i(TAG,"DIRECT bindService="+bound);
  if(!bound) Log.e(TAG,"DIRECT bind returned false: no exported matching toolkit service");
 }catch(Throwable e){Log.e(TAG,"DIRECT bind failed",e);}}
 public void stop(){if(bound){try{context.unbindService(connection);}catch(Throwable ignored){}bound=false;}radio=null;main=null;}
 private IBinder module(IBinder toolkit,int id)throws RemoteException{Parcel d=Parcel.obtain(),r=Parcel.obtain();try{d.writeInterfaceToken(TK);d.writeInt(id);toolkit.transact(1,d,r,0);r.readException();return r.readStrongBinder();}finally{d.recycle();r.recycle();}}
 private void register(int id){IBinder b=radio;if(b==null)return;Parcel d=Parcel.obtain(),r=Parcel.obtain();try{d.writeInterfaceToken(MOD);d.writeStrongBinder(callback);d.writeInt(id);d.writeInt(0);b.transact(3,d,r,0);r.readException();Log.i(TAG,"DIRECT subscribed "+id);}catch(Throwable e){Log.w(TAG,"DIRECT subscribe "+id+" failed",e);}finally{d.recycle();r.recycle();}}
 /** Diagnostic ONLY: raw ModuleObject getter payload. No UI/metadata modification. */
 private void probeGetter(int dataId){IBinder b=radio;if(b==null)return;
  Parcel d=Parcel.obtain(),r=Parcel.obtain();try{
   d.writeInterfaceToken(MOD);d.writeInt(dataId);d.writeIntArray(null);d.writeFloatArray(null);d.writeStringArray(null);
   boolean accepted=b.transact(2,d,r,0);
   Log.i(TAG,"GETTER_PROBE id="+dataId+" accepted="+accepted+" replyBytes="+r.dataSize());
   if(!accepted)return;
   r.setDataPosition(0);r.readException();
   int offset=r.dataPosition();byte[] bytes=r.marshall();
   StringBuilder hex=new StringBuilder(),ascii=new StringBuilder();
   for(int i=offset;i<bytes.length&&i<offset+1024;i++){
    int v=bytes[i]&255;hex.append(String.format(Locale.US,"%02x",v));
    ascii.append(v>=32&&v<=126?(char)v:'.');
   }
   Log.i(TAG,"GETTER_PROBE id="+dataId+" offset="+offset+" hex="+hex+" ascii="+ascii);
  }catch(Throwable e){Log.e(TAG,"GETTER_PROBE id="+dataId+" error",e);}finally{d.recycle();r.recycle();}
 }
 private boolean cmd(IBinder b,int command,int[] args){if(b==null)return false;Parcel d=Parcel.obtain(),r=Parcel.obtain();try{d.writeInterfaceToken(MOD);d.writeInt(command);d.writeIntArray(args);d.writeFloatArray(null);d.writeStringArray(null);boolean accepted=b.transact(1,d,r,0);if(!accepted){Log.w(TAG,"DIRECT command transaction rejected="+command);return false;}r.readException();Log.i(TAG,"DIRECT command dispatched (NOT confirmed by tuner)="+command+" args="+Arrays.toString(args));return true;}catch(Throwable e){Log.e(TAG,"DIRECT command failed",e);return false;}finally{d.recycle();r.recycle();}}
 public boolean activate(){return cmd(main,0,new int[]{1});}
 public boolean seekNext(){return cmd(radio,5,null);}
 public boolean seekPrevious(){return cmd(radio,6,null);}
 /** Direct station frequency command, not station-search. FYT IPC radio module accepts frequency in 10-kHz units on this candidate path. */
 public boolean tuneFrequencyKhz(int khz){
  if(khz<87500||khz>108000||khz%200!=100){Log.w(TAG,"DIRECT_TUNE invalid khz="+khz);return false;}
  int units10khz=khz/10;
  Log.i(TAG,"DIRECT_TUNE request khz="+khz+" units10khz="+units10khz+" command=13 args=[3,freq]");
  return cmd(radio,13,new int[]{3,units10khz});
 }
 public boolean connected(){return radio!=null&&main!=null;}
}
