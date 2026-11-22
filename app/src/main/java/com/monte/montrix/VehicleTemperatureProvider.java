package com.monte.montrix;
import android.content.*;
import android.hardware.*;
import android.os.*;
import java.util.*;
/** Diagnostics-first ambient source: never substitutes CPU temperature for outdoor temperature. */
public class VehicleTemperatureProvider {
 public interface Listener { void onTemperature(String text,boolean live); }
 private final Context c; private final Listener listener; private BroadcastReceiver rx;
 VehicleTemperatureProvider(Context c,Listener l){this.c=c;this.listener=l;}
 public void start(){
  DiagnosticLogger.event(c,"TEMP_DISCOVERY","build=26.14.0 model="+Build.MODEL+" hardware="+Build.HARDWARE);
  try { SensorManager sm=(SensorManager)c.getSystemService(Context.SENSOR_SERVICE);
   for(Sensor s:sm.getSensorList(Sensor.TYPE_ALL)) DiagnosticLogger.event(c,"SENSOR_AVAILABLE","type="+s.getType()+" name="+s.getName()+" vendor="+s.getVendor());
   Sensor ambient=sm.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
   DiagnosticLogger.event(c,"TEMP_ANDROID_SENSOR","ambient="+(ambient==null?"ABSENT":ambient.getName()));
  }catch(Throwable e){DiagnosticLogger.event(c,"TEMP_SENSOR_ERROR",e.toString());}
  for(String pkg:new String[]{"com.syu.canbus","com.syu.ms","com.syu.radio"}){
   try {c.getPackageManager().getPackageInfo(pkg,0);DiagnosticLogger.event(c,"OEM_PACKAGE","package="+pkg+" present=true");}
   catch(Throwable e){DiagnosticLogger.event(c,"OEM_PACKAGE","package="+pkg+" present=false error="+e.getClass().getSimpleName());}
  }
  rx=new BroadcastReceiver(){public void onReceive(Context context,Intent intent){
   double f=intent.getDoubleExtra("fahrenheit",Double.NaN),cel=intent.getDoubleExtra("celsius",Double.NaN);
   DiagnosticLogger.event(c,"TEMP_BRIDGE_RECEIVED","action="+intent.getAction()+" fahrenheit="+f+" celsius="+cel);
   if(Double.isFinite(f)&&f>-100&&f<160)listener.onTemperature(Math.round(f)+"°F",true);
   else if(Double.isFinite(cel)&&cel>-75&&cel<72)listener.onTemperature(Math.round(cel*9/5+32)+"°F",true);
   else DiagnosticLogger.event(c,"TEMP_BRIDGE_REJECTED","invalid or absent reading");
  }};
  IntentFilter filter=new IntentFilter("com.monte.cwgradio.VEHICLE_TEMPERATURE");
  c.registerReceiver(rx,filter);
  listener.onTemperature("--°F",false);
  DiagnosticLogger.event(c,"TEMP_UNAVAILABLE","CAN callback not implemented; no verified external bridge; intentionally no fabricated reading");
 }
 public void stop(){try{if(rx!=null)c.unregisterReceiver(rx);}catch(Throwable ignored){}}
}
