package com.monte.montrix.radio.backend;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import org.json.JSONObject;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Emulator-only bridge. Host listens on 127.0.0.1:8765, exposed via adb reverse. */
public final class EmulatorSdrBackend {
 public interface Listener { void onState(String frequency, String ps, String rt, String status); }
 private final ExecutorService polling=Executors.newSingleThreadExecutor(); private final ExecutorService commands=Executors.newSingleThreadExecutor();
 private final Handler main=new Handler(Looper.getMainLooper());
 private final Listener listener;
 private volatile boolean running;
 private int lastVersion=-1;
 private static final String BASE="http://127.0.0.1:8765";
 public EmulatorSdrBackend(Listener listener){this.listener=listener;}
 public void start(){if(running)return;running=true;polling.execute(()->{while(running){try{JSONObject state=request("GET","/state",null);emit(state,"");Thread.sleep(1200);}catch(Exception ex){Log.w("MontrixSDR","poll: "+ex);try{Thread.sleep(2200);}catch(InterruptedException ignored){break;}}}});}
 public void stop(){running=false;polling.shutdownNow();commands.shutdownNow();}
 public void tune(int khz){if(khz<87500||khz>108000){Log.w("MontrixSDR","invalid tune "+khz);return;}command("tune",khz/10);}
 public void seek(boolean up){command(up?"seek_up":"seek_down",null);}
 private void command(String action,Integer value){commands.execute(()->{try{JSONObject req=new JSONObject().put("action",action);if(value!=null)req.put("value",value);JSONObject response=request("POST","/command",req);emit(response,"");}catch(Exception ex){Log.e("MontrixSDR","command "+action,ex);main.post(()->listener.onState(null,null,null,"SDR command failed: "+ex.getMessage()));}});}
 private void emit(JSONObject obj,String status){if(obj==null)return;int ver=obj.optInt("version",-1);if(ver==lastVersion && status.isEmpty())return;lastVersion=ver;int units=obj.optInt("frequency",0);if(units<8750||units>10800)return;String f=String.format(Locale.US,"%.1f FM",units/100.0);JSONObject rds=obj.optJSONObject("rds");String ps=rds==null?"":rds.optString("ps","");String rt=rds==null?"":rds.optString("rt","");main.post(()->listener.onState(f,ps,rt,status));}
 private static JSONObject request(String method,String path,JSONObject payload)throws Exception{HttpURLConnection c=(HttpURLConnection)new URL(BASE+path).openConnection();try{c.setConnectTimeout(1600);c.setReadTimeout(2300);c.setRequestMethod(method);if(payload!=null){c.setDoOutput(true);c.setRequestProperty("Content-Type","application/json");byte[] b=payload.toString().getBytes(StandardCharsets.UTF_8);try(OutputStream out=c.getOutputStream()){out.write(b);}}int code=c.getResponseCode();InputStream in=code>=200&&code<300?c.getInputStream():c.getErrorStream();byte[] buf=new byte[4096];StringBuilder sb=new StringBuilder();if(in!=null)try(InputStream stream=in){int n;while((n=stream.read(buf))!=-1)sb.append(new String(buf,0,n,StandardCharsets.UTF_8));}if(code<200||code>=300)throw new IllegalStateException("HTTP "+code+" "+sb);return new JSONObject(sb.toString());}finally{c.disconnect();}}
}
