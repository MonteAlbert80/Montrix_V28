package com.monte.montrix;
import android.content.*;import android.os.*;import android.media.*;import android.util.Log;
import java.io.*;import java.text.*;import java.util.*;
/** Persistent, bounded, passive diagnostic logging; no audio controls. */
public final class DiagnosticLogger {
 private static final Object LOCK=new Object();private static final long LIMIT=8L*1024*1024;
 private static final String TAG="ASURE_DIAG";
 private DiagnosticLogger(){}
 public static void event(Context c,String type,String details){
  synchronized(LOCK){try{
   File dir=new File(c.getExternalFilesDir(null),"diagnostics");if(!dir.exists())dir.mkdirs();
   File log=new File(dir,"asure_12h.log");if(log.length()>LIMIT){File old=new File(dir,"asure_12h.previous.log");if(old.exists())old.delete();boolean rotated=log.renameTo(old);if(!rotated)Log.e(TAG,"rotation failed; retaining current log");}
   String timestamp=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ",Locale.US).format(new Date());
   String line=timestamp+" elapsedMs="+SystemClock.elapsedRealtime()+" event="+type+" "+details.replace('\n',' ') +"\n";
   try(FileOutputStream stream=new FileOutputStream(log,true)){stream.write(line.getBytes("UTF-8"));}
   Log.i(TAG,line.trim());
  }catch(Throwable t){Log.e(TAG,"log failed",t);}}
 }
 public static void snapshot(Context c,String reason){try{
  AudioManager a=(AudioManager)c.getSystemService(Context.AUDIO_SERVICE);
  String info="reason="+reason+" interactive="+((PowerManager)c.getSystemService(Context.POWER_SERVICE)).isInteractive()
   +" musicActive="+a.isMusicActive()+" musicVolume="+a.getStreamVolume(AudioManager.STREAM_MUSIC)
   +" mode="+a.getMode()+" speaker="+a.isSpeakerphoneOn()
   +" hardware="+Build.HARDWARE+" device="+Build.DEVICE;
  event(c,"SNAPSHOT",info);
 }catch(Throwable t){event(c,"SNAPSHOT_ERROR",t.toString());}}
}
