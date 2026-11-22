package com.monte.montrix;

import android.os.IBinder;
import android.os.Parcel;
import android.util.Log;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

/** Optional, read-only FYT sqlfmservice reader. Never tunes or controls the tuner.
 * getText (28) consumes its buffer: call only while our radio is selected.
 * Hidden ServiceManager access can be blocked for ordinary apps; failure is logged.
 */
final class NativeRdsReader {
  private static final String TAG="ASURE_RDS";
  private IBinder binder;
  private boolean warned;
  private long lastEmptyRtLog=0;
  private long lastRawRtLog=0;
  private IBinder service() {
    if (binder!=null && binder.isBinderAlive()) return binder;
    try {
      Class<?> cls=Class.forName("android.os.ServiceManager");
      Method get=cls.getDeclaredMethod("getService",String.class);
      binder=(IBinder)get.invoke(null,"sqlfmservice");
      if (binder==null) throw new IllegalStateException("sqlfmservice not visible");
      if (warned) Log.i(TAG,"sqlfmservice recovered");
      warned=false;
      return binder;
    } catch (Throwable e) {
      if (!warned) Log.w(TAG,"Binder access unavailable (possibly hidden API or SELinux): "+e);
      warned=true; return null;
    }
  }
  private String read(int transaction, int maxBytes) {
    IBinder b=service(); if(b==null)return null;
    Parcel data=Parcel.obtain(), reply=Parcel.obtain();
    data.writeInterfaceToken("sqlfmserver.ISqlFMService");
    try {
      if (!b.transact(transaction,data,reply,0)) {Log.w(TAG,"transaction "+transaction+" not handled");return null;}
      // FYT sqlfmserver writes its native character buffer directly into the
      // reply Parcel. It does NOT prepend Java Binder writeNoException().
      // Calling readException() here interprets ASCII station text as an
      // exception code (seen in V3 QA as 544105805 / 1802661700).
      byte[] raw=reply.marshall();
      // sqlfmservice replies are raw native parcels, not Java exception parcels.
      // Dump bounded hex for both calls to determine whether the first four bytes
      // are a length/status field and how FYT encodes the 8/64-byte payload.
      StringBuilder hex=new StringBuilder();
      for(int j=0;j<Math.min(raw.length,80);j++)hex.append(String.format(java.util.Locale.US,"%02X",raw[j]&255));
      if(transaction!=28 || android.os.SystemClock.elapsedRealtime()-lastRawRtLog>10000){Log.i(TAG,"RDS_RAW transaction="+transaction+" bytes="+raw.length+" hex="+hex);if(transaction==28)lastRawRtLog=android.os.SystemClock.elapsedRealtime();}
      // V4 evidence: PS reply is 8 ASCII bytes followed by 4 zero bytes.
      // Example: 4D616A696339202000000000 = "Majic9  " + padding.
      // RT reply is 64 data bytes followed by 4 zero bytes. No 4-byte prefix.
      // V5 skipped the first four characters, displaying "c9" and "oppa".
      int expected=transaction==27?8:64;
      if(raw.length<expected){Log.i(TAG,"RDS_INVALID_SIZE transaction="+transaction);return null;}
      int end=expected;
      while(end>0 && (raw[end-1]==0 || raw[end-1]==32))end--;
      if(end==0){if(transaction!=28 || android.os.SystemClock.elapsedRealtime()-lastEmptyRtLog>10000){Log.i(TAG,"RDS_EMPTY transaction="+transaction);if(transaction==28)lastEmptyRtLog=android.os.SystemClock.elapsedRealtime();}return null;}
      for(int j=0;j<end;j++){
        int c=raw[j]&255;
        if(c<32||c>126){Log.i(TAG,"RDS_BINARY_UNVERIFIED transaction="+transaction);return null;}
      }
      String candidate=new String(raw,0,end,StandardCharsets.US_ASCII).trim();
      if(candidate.length()<2)return null;
      Log.i(TAG,"RDS_DECODED transaction="+transaction+" text="+candidate);
      return candidate;
    } catch(Throwable e) {binder=null;Log.w(TAG,"transaction "+transaction+" failed",e);return null;}
    finally {reply.recycle();data.recycle();}
  }
  String ps(){return read(27,8);}
  String rt(){return read(28,64);}
}
