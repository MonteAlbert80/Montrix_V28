package com.monte.montrix; import android.app.*;import android.content.*;import android.os.*;
public class RadioActivity extends Activity{public void onCreate(Bundle b){super.onCreate(b);Intent i=new Intent(this,NativeRadioService.class).setAction(NativeRadioService.ACTION_RADIO);if(Build.VERSION.SDK_INT>=26)startForegroundService(i);else startService(i);finish();}}
