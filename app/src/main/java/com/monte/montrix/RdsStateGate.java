package com.monte.montrix;

import android.util.Log;

/** One authority for radio text. Empty reads never erase previously received RDS.
 * Tune generations prevent late results from the previous station from overwriting the new one.
 * PS and RT are kept distinct, including provenance for future hardware validation.
 */
final class RdsStateGate {
  private static final String TAG="MONTRIX_RDS_GATE";
  private String station="", ps="", rt="";
  private String psSource="", rtSource="";
  private long generation=0;
  synchronized long epoch(){return generation;}
  synchronized void tuned(String frequency){
    if(frequency==null)frequency="";
    if(frequency.equals(station))return;
    station=frequency; generation++;
    ps="";rt="";psSource="";rtSource="";
    Log.i(TAG,"TUNE generation="+generation+" frequency="+station);
  }
  synchronized void acceptPs(String candidate,String source){
    String value=clean(candidate,8);
    if(value==null)return;
    if(!value.equals(ps)){ps=value;psSource=source;Log.i(TAG,"PS source="+source+" text="+ps);}
  }
  synchronized void acceptRt(String candidate,String source){
    String value=clean(candidate,64);
    if(value==null)return;
    if(!value.equals(rt)){rt=value;rtSource=source;Log.i(TAG,"RT source="+source+" text="+rt);}
  }
  synchronized String ps(){return ps.isEmpty()?"FM radio":ps;}
  synchronized String rt(){return rt.isEmpty()?"Waiting for RadioText":rt;}
  synchronized String psSource(){return psSource;}
  synchronized String rtSource(){return rtSource;}
  private static String clean(String input,int max){
    if(input==null)return null;
    String v=input.replace('\u0000',' ').trim();
    if(v.isEmpty()||v.length()>max)return null;
    for(int i=0;i<v.length();i++){char c=v.charAt(i);if(Character.isISOControl(c))return null;}
    return v;
  }
}
