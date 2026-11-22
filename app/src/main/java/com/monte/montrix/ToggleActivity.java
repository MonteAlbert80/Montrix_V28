package com.monte.montrix;
import android.app.Activity;import android.content.*;import android.os.Bundle;
/** Assign this app as a CWG shortcut: each tap toggles Radio <-> Music without opening a player screen. */
public class ToggleActivity extends Activity{
 @Override public void onCreate(Bundle b){super.onCreate(b);boolean radio=!getSharedPreferences("mode",0).getBoolean("radio",false);getSharedPreferences("mode",0).edit().putBoolean("radio",radio).apply();Intent i=new Intent(this,NativeRadioService.class).setAction(radio?NativeRadioService.ACTION_RADIO:NativeRadioService.ACTION_MUSIC);if(android.os.Build.VERSION.SDK_INT>=26)startForegroundService(i);else startService(i);finish();}
}
