package com.monte.montrix;

import android.app.*;import android.content.*;import android.os.*;import android.view.*;import java.lang.reflect.*;

/** Uses FYT's modified hidden ActivityView when the ROM permits cross-app window hosting. */
public class FytEmbeddedActivityHost {
 public interface Listener{void onReady(View v);void onFailure(String reason);} private final Activity a;private final Listener l;private Object av;private Handler h=new Handler(Looper.getMainLooper());private boolean done;
 public FytEmbeddedActivityHost(Activity a,Listener l){this.a=a;this.l=l;}
 public void start(String pkg){h.post(()->{try{Class<?> c=Class.forName("android.app.ActivityView");Constructor<?> ct=c.getDeclaredConstructor(android.content.Context.class);ct.setAccessible(true);av=ct.newInstance(a);if(!(av instanceof View))throw new IllegalStateException("ActivityView is not a View");View v=(View)av;l.onReady(v);h.postDelayed(()->launch(c,pkg),900);h.postDelayed(()->{if(!done)fail("FYT host timeout");},4500);}catch(Throwable e){fail(shortMsg(e));}});}
 private void launch(Class<?> c,String pkg){try{Intent i=a.getPackageManager().getLaunchIntentForPackage(pkg);if(i==null)throw new IllegalStateException("Google Maps not installed");i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);Method m=c.getMethod("startActivity",Intent.class);m.invoke(av,i);done=true;}catch(Throwable e){fail(shortMsg(e));}}
 private String shortMsg(Throwable e){Throwable q=e instanceof InvocationTargetException&&((InvocationTargetException)e).getTargetException()!=null?((InvocationTargetException)e).getTargetException():e;String n=q.getClass().getSimpleName();String m=q.getMessage();return m==null?n:n+": "+(m.length()>42?m.substring(0,42):m);}
 private void fail(String s){if(done)return;done=true;l.onFailure(s);}
 public void release(){h.removeCallbacksAndMessages(null);if(av!=null)try{Method m=av.getClass().getMethod("release");m.invoke(av);}catch(Throwable ignored){}av=null;}
}
