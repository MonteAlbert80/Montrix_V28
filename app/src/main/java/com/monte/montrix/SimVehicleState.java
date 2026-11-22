package com.monte.montrix;
import android.content.Intent;
import org.json.JSONObject;
import java.util.Locale;
/** Emulator-only data; never transmits to vehicle or FYT MCU. */
public final class SimVehicleState {
 public boolean acc=true,cranking=false,pausedBySleep=false,shift=false;
 public int speedMph=0,fuelPercent=70,oilLifePercent=65;
 public Integer temperatureF=75;
 public double tripMiles=0, odometerMiles=0, shiftStartOdometer=0;
 public double latitude=30.49603, longitude=-97.57347; public float headingDegrees=0; public boolean gpsMoving=false, odometerSeen=false;
 public int speedLimitMph=0; public String door="closed",tpms="normal",battery="normal";
 public long lastTickMs=android.os.SystemClock.elapsedRealtime();
 public boolean hasHeadingFix=false;
 public double headingAnchorLat=Double.NaN, headingAnchorLon=Double.NaN;
 public static final double MIN_HEADING_DISPLACEMENT_METERS=8.0;
 public String warning="none",station="101.5",rds="Simulation ready";
 public void accumulate(){long now=android.os.SystemClock.elapsedRealtime();double hours=Math.max(0,Math.min(now-lastTickMs,30000))/3600000.0;if(shift&&acc&&!cranking&&!odometerSeen)tripMiles+=speedMph*hours;lastTickMs=now;}
 public void apply(String event,Intent i){
  accumulate();
  if(event==null)return;
  switch(event){
   case "acc_off":case "sleep":acc=false;cranking=false;break;
   case "acc_on":case "wake":acc=true;cranking=false;break;
   case "crank_start":cranking=true;break;
   case "crank_end":cranking=false;break;
   case "speed_limit":speedLimitMph=clamp(i.getIntExtra("value",speedLimitMph),0,100);break;
   case "door":door=safe(i.getStringExtra("text"));break;
   case "tpms":tpms=safe(i.getStringExtra("text"));break;
   case "battery":battery=safe(i.getStringExtra("text"));break;
   case "speed":speedMph=clamp(i.getIntExtra("value",speedMph),0,200);break;
   case "fuel":fuelPercent=clamp(i.getIntExtra("value",fuelPercent),0,100);break;
   case "oil_life":oilLifePercent=clamp(i.getIntExtra("value",oilLifePercent),0,100);break;
   case "temperature":temperatureF=clamp(i.getIntExtra("value",temperatureF),-60,150);break;
   case "warning":warning=safe(i.getStringExtra("text"));break;
   case "clear_warning":warning="none";break;
   case "radio":station=safe(i.getStringExtra("station"));rds=safe(i.getStringExtra("text"));break;
   case "shift_start":shift=true;tripMiles=0;shiftStartOdometer=odometerMiles;break;
   case "shift_stop":shift=false;break;
   case "trip":tripMiles=Math.max(0,readDecimal(i,"value",tripMiles));break;
   case "odometer":{
    double reading=readDecimal(i,"value",odometerMiles);
    if(Double.isFinite(reading)&&reading>=0&&reading<2000000){
     if(odometerSeen && reading>=odometerMiles && shift)tripMiles+=reading-odometerMiles;
     odometerMiles=reading;odometerSeen=true;
    }break;}
   case "gps":{
    double lat=readDecimal(i,"lat",latitude),lon=readDecimal(i,"lon",longitude);
    if(Double.isFinite(lat)&&Double.isFinite(lon)&&OfflineTexasMapView.validTexas(lat,lon)){
     // Position always follows raw GPS. Do not snap parked cars to a road.
     // Heading uses a distance-based anchor rather than adjacent jittery fixes.
     if(Double.isNaN(headingAnchorLat)){headingAnchorLat=lat;headingAnchorLon=lon;}
     double displacement=distance(headingAnchorLat,headingAnchorLon,lat,lon);
     if(displacement>=MIN_HEADING_DISPLACEMENT_METERS){
       float target=(float)bearing(headingAnchorLat,headingAnchorLon,lat,lon);
       if(!hasHeadingFix){headingDegrees=target;hasHeadingFix=true;}
       else {float delta=((target-headingDegrees+540f)%360f)-180f;
             headingDegrees=((headingDegrees+Math.max(-55f,Math.min(55f,delta))*0.65f)%360f+360f)%360f;}
       headingAnchorLat=lat;headingAnchorLon=lon;
     }
     gpsMoving=hasHeadingFix;
     latitude=lat;longitude=lon;
    }break;}
   case "heading":headingDegrees=((i.getIntExtra("value",0)%360)+360)%360;hasHeadingFix=true;gpsMoving=true;break;
  }
 }
 // Android am broadcast supports --es but not --ed. Parse decimal extras sent as strings.
 static double readDecimal(Intent intent,String key,double fallback){
  try {
   Object raw=intent.getExtras()==null?null:intent.getExtras().get(key);
   if(raw instanceof Number)return ((Number)raw).doubleValue();
   if(raw instanceof String)return Double.parseDouble(((String)raw).trim());
  }catch(Exception ignored){}
  return fallback;
 }
 static double distance(double a,double b,double c,double d){double r=6371000,da=Math.toRadians(c-a),db=Math.toRadians(d-b);double x=Math.sin(da/2)*Math.sin(da/2)+Math.cos(Math.toRadians(a))*Math.cos(Math.toRadians(c))*Math.sin(db/2)*Math.sin(db/2);return 2*r*Math.atan2(Math.sqrt(x),Math.sqrt(1-x));}
 static double bearing(double a,double b,double c,double d){double x=Math.sin(Math.toRadians(d-b))*Math.cos(Math.toRadians(c)),y=Math.cos(Math.toRadians(a))*Math.sin(Math.toRadians(c))-Math.sin(Math.toRadians(a))*Math.cos(Math.toRadians(c))*Math.cos(Math.toRadians(d-b));return (Math.toDegrees(Math.atan2(x,y))+360)%360;}
 static int clamp(int x,int a,int b){return Math.max(a,Math.min(b,x));}
 static String safe(String s){return s==null?"":s.replaceAll("[\\r\\n]"," ").substring(0,Math.min(s.length(),120));}
 public String summary(){return String.format(Locale.US,"SIM • %s%s • %d mph • %d°F • fuel %d%% • oil %d%% • %s • %s",acc?"ACC ON":"SLEEP",cranking?" / CRANK":"",speedMph,temperatureF,fuelPercent,oilLifePercent,shift?"SHIFT ON":"SHIFT OFF",warning);}
 public String json(){try{JSONObject j=new JSONObject();j.put("acc",acc);j.put("cranking",cranking);j.put("speedMph",speedMph);j.put("fuelPercent",fuelPercent);j.put("oilLifePercent",oilLifePercent);j.put("temperatureF",temperatureF);j.put("warning",warning);j.put("shift",shift);j.put("tripMiles",tripMiles);j.put("odometerMiles",odometerMiles);j.put("latitude",latitude);j.put("longitude",longitude);j.put("headingDegrees",headingDegrees);j.put("speedLimitMph",speedLimitMph);j.put("door",door);j.put("tpms",tpms);j.put("battery",battery);j.put("station",station);j.put("rds",rds);j.put("pausedBySleep",pausedBySleep);return j.toString();}catch(Exception e){return summary();}}
}
