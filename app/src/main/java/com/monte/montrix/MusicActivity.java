package com.monte.montrix; import android.app.*;import android.content.*;import android.os.*;
public class MusicActivity extends Activity{public void onCreate(Bundle b){super.onCreate(b);Intent i=new Intent(this,NativeRadioService.class).setAction(NativeRadioService.ACTION_MUSIC);if(Build.VERSION.SDK_INT>=26)startForegroundService(i);else startService(i);finish();}}
