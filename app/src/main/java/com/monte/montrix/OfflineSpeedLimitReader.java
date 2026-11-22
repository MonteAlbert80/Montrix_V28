package com.monte.montrix;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;

/** Conservative offline trajectory matcher. Does not provide actual vehicle speed. */
public final class OfflineSpeedLimitReader {
 private final File file;
 private SQLiteDatabase db;
 private final ArrayDeque<Fix> history=new ArrayDeque<>();
 private String committedRoad="";
 private int committedLimit=0;
 private long committedAt=0;
 private static final double EARTH_LAT=111195d;
 private static final String TAG="MONTRIX_ROADPACK";
 public static final class Result {
  public final int mph; public final String reason,road;
  Result(int m,String why,String name){mph=m;reason=why;road=name;}
 }
 private static final class Fix {
  final double lat,lon;final long time;
  Fix(double a,double b,long t){lat=a;lon=b;time=t;}
 }
 private static final class Candidate {
  final String road;final int limit,id;final double distance,bearing;
  double score;
  Candidate(String n,int v,int i,double d,double b){road=n;limit=v;id=i;distance=d;bearing=b;}
 }
 public OfflineSpeedLimitReader(Context context){
  file=context.getDatabasePath("txdot_central_texas_segments.sqlite");
  provisionRoadPack(context);
}
/** MONTRIX_ROADPACK_ASSET_V278: copy bundled official TxDOT data to app-private storage. */
private void provisionRoadPack(Context context){
  java.io.File tmp=null;
  try {
    // Preserve a previously installed database if its size matches this bundled version.
    long assetSize=0;
    try(java.io.InputStream sizeIn=context.getAssets().open("txdot_central_texas_segments.sqlite")){
      byte[] buffer=new byte[65536];int n;
      while((n=sizeIn.read(buffer))!=-1)assetSize+=n;
    }
    if(file.isFile() && file.length()==assetSize) return;
    java.io.File parent=file.getParentFile();
    if(parent==null || (!parent.isDirectory() && !parent.mkdirs())) throw new java.io.IOException("Cannot create database directory");
    tmp=new java.io.File(parent,"txdot_central_texas_segments.sqlite.tmp");
    try(java.io.InputStream in=context.getAssets().open("txdot_central_texas_segments.sqlite");
        java.io.FileOutputStream out=new java.io.FileOutputStream(tmp)){
      byte[] buffer=new byte[65536];int n;
      while((n=in.read(buffer))!=-1)out.write(buffer,0,n);
      out.getFD().sync();
    }
    if(tmp.length()!=assetSize) throw new java.io.IOException("Roadpack copy size mismatch");
    if(!tmp.renameTo(file)) throw new java.io.IOException("Roadpack rename failed");
    android.util.Log.i("MONTRIX_ROADPACK","ASSET_READY bytes="+file.length());
  } catch(Exception e) {
    android.util.Log.e("MONTRIX_ROADPACK","ASSET_PROVISION_FAILED",e);
  } finally {if(tmp!=null && tmp.exists()) tmp.delete();}
}
 private static Result unknown(String why){return new Result(0,why,"");}
 private static double headingDelta(double a,double b){double d=Math.abs((a-b+540d)%360d-180d);return Math.min(d,180d-d<0?d:d);}
 private static double bearing(double dx,double dy){return (Math.toDegrees(Math.atan2(dx,dy))+360d)%360d;}
 private static double headingError(double a,double b){double d=Math.abs((a-b+540d)%360d-180d);return Math.min(d,Math.abs(180d-d));}
 private void reset(){history.clear();committedRoad="";committedLimit=0;committedAt=0;}
 /** timestamp is elapsedRealtimeNanos converted to ms; zero uses monotonic system clock. */
 public synchronized Result lookup(double lat,double lon,float accuracy,long timeMs){
  if(!Double.isFinite(lat)||!Double.isFinite(lon)||lat<29.2||lat>31.1||lon< -99.3||lon> -96.65){reset();return unknown("OUTSIDE_COVERAGE");}
  if(accuracy>35f){reset();return unknown("GPS_INACCURATE");}
  if(!file.isFile()){reset();return unknown("DATABASE_MISSING");}
  long now=timeMs>0?timeMs:android.os.SystemClock.elapsedRealtime();
  if(!history.isEmpty()&&(now<=history.getLast().time||now-history.getLast().time>12000)){reset();}
  if(!history.isEmpty()){
   Fix last=history.getLast();double m=distanceMeters(lat,lon,last.lat,last.lon);
   if(m>Math.max(200d,(now-last.time)*0.09d+75d))reset();
  }
  history.addLast(new Fix(lat,lon,now));while(history.size()>15)history.removeFirst();
  while(history.size()>1&&now-history.getFirst().time>20000)history.removeFirst();
  double travel=0,travelBearing=0;
  if(history.size()>1){Fix first=history.getFirst();travel=distanceMeters(lat,lon,first.lat,first.lon);
   travelBearing=bearing((lon-first.lon)*EARTH_LAT*Math.cos(Math.toRadians(lat)),(lat-first.lat)*EARTH_LAT);
  }
  boolean directional=travel>=22d;
  try{
   if(db==null||!db.isOpen())db=SQLiteDatabase.openDatabase(file.getAbsolutePath(),null,SQLiteDatabase.OPEN_READONLY);
   double metersLon=EARTH_LAT*Math.cos(Math.toRadians(lat));double radius=70d;
   String[] args={String.valueOf(lon-radius/metersLon),String.valueOf(lon+radius/metersLon),String.valueOf(lat-radius/EARTH_LAT),String.valueOf(lat+radius/EARTH_LAT)};
   ArrayList<Candidate> candidates=new ArrayList<>();
   Cursor c=db.rawQuery("SELECT objectid,rte_nm,spd_lmt,geometry_json FROM txdot_segments WHERE maxlon>=? AND minlon<=? AND maxlat>=? AND minlat<=? AND spd_lmt>0",args);
   try{while(c.moveToNext()){
    int limit=c.getInt(2);if(limit<5||limit>90)continue;
    JSONObject geom=new JSONObject(c.getString(3));JSONArray paths=geom.optJSONArray("paths");if(paths==null)continue;
    double nearest=Double.POSITIVE_INFINITY,nearestBearing=0;
    for(int p=0;p<paths.length();p++){
     JSONArray path=paths.optJSONArray(p);if(path==null)continue;
     double px=0,py=0;boolean prior=false;
     for(int j=0;j<path.length();j++){
      JSONArray point=path.optJSONArray(j);if(point==null||point.length()<2)continue;
      double x=(point.getDouble(0)-lon)*metersLon,y=(point.getDouble(1)-lat)*EARTH_LAT;
      if(prior){double vx=x-px,vy=y-py,len=vx*vx+vy*vy;
       double t=len>0?Math.max(0,Math.min(1,-(px*vx+py*vy)/len)):0;
       double d=Math.hypot(px+t*vx,py+t*vy);
       if(d<nearest){nearest=d;nearestBearing=bearing(vx,vy);}
      }else if(Math.hypot(x,y)<nearest){nearest=Math.hypot(x,y);}
      px=x;py=y;prior=true;
     }
    }
    if(nearest<=55d){String road=c.getString(1);if(road==null)road="";
     Candidate candidate=new Candidate(road,limit,c.getInt(0),nearest,nearestBearing);
     double headingPenalty=directional?headingError(travelBearing,nearestBearing)*0.35d:0d;
     double continuityBonus=road.equals(committedRoad)&&limit==committedLimit?Math.min(8d,travel/10d):0d;
     candidate.score=nearest+headingPenalty-continuityBonus;
     candidates.add(candidate);
    }
   }}finally{c.close();}
   candidates.sort(Comparator.comparingDouble(a->a.score));
   if(candidates.isEmpty()||candidates.get(0).distance>30d){committedRoad="";committedLimit=0;return unknown("NO_NEARBY_ROAD");}
   Candidate best=candidates.get(0);
   Candidate conflicting=null;
   for(Candidate other:candidates){if(other.limit!=best.limit && other.distance<=40d && other.score-best.score<16d){conflicting=other;break;}}
   if(conflicting!=null){
    Log.i(TAG,String.format(Locale.US,"candidates best=%d/%s/%dmph/%.1fm/%.1fscore competing=%d/%s/%dmph/%.1fm/%.1fscore trajectory=%.1fm",best.id,best.road,best.limit,best.distance,best.score,conflicting.id,conflicting.road,conflicting.limit,conflicting.distance,conflicting.score,travel));
    // A stationary GPS observation cannot distinguish stacked or parallel roads.
    // A history bonus alone is never allowed to override a close conflicting candidate.
    if(!directional || conflicting.score-best.score<8d){committedRoad="";committedLimit=0;return unknown("AMBIGUOUS_ROADS");}
   }
   // A single new fix near a ramp must not immediately switch to a different limit.
   if(committedLimit>0&&best.limit!=committedLimit&&travel<22d){committedRoad="";committedLimit=0;return unknown("TRANSITION_UNCERTAIN");}
   committedRoad=best.road;committedLimit=best.limit;committedAt=now;
   return new Result(best.limit,"MATCH_"+Math.round(best.distance)+"M"+(directional?"_TRAJECTORY":"_POSITION"),best.road);
  }catch(Exception ex){Log.e(TAG,"SQLite trajectory lookup failed",ex);return unknown("DATABASE_ERROR");}
 }
 public synchronized Result lookup(double lat,double lon,float accuracy){return lookup(lat,lon,accuracy,0L);}
 private static double distanceMeters(double a,double b,double c,double d){
  double y=(a-c)*EARTH_LAT,x=(b-d)*EARTH_LAT*Math.cos(Math.toRadians((a+c)*0.5));return Math.hypot(x,y);
 }
 public synchronized void close(){if(db!=null){db.close();db=null;}reset();}
}
