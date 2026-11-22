package com.monte.montrix;

import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.os.*;
import android.widget.*;

/** Exported bootstrap activity. Starts the private service from inside our own UID. */
public class MainActivity extends Activity {
    private TextView status;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.VERTICAL);
        r.setPadding(34,26,34,26);
        r.setBackgroundColor(Color.rgb(12,16,20));
        r.addView(t("CWG Native FYT Radio Bridge V9",28));
        r.addView(t("No overlay. This activity bootstraps the private radio MediaSession safely, then returns to CarWebGuru.",17));

        Button start=b("START / REFRESH RADIO SESSION");
        start.setOnClickListener(v -> startRadio(true)); r.addView(start);
        Button music=b("MUSIC MODE");
        music.setOnClickListener(v -> go(NativeRadioService.ACTION_MUSIC)); r.addView(music);
        status=t("Starting V9 radio session…",16); r.addView(status);
        setContentView(r);

        // Installer launches this exported activity; service is started internally, avoiding ADB's
        // inability to start an exported=false service.
        startRadio(false);
    }

    private void startRadio(boolean user) {
        go(NativeRadioService.ACTION_TEST);
        status.setText("V9 session started. CWG should show KUTX • 98.9 FM.");
        new Handler(getMainLooper()).postDelayed(this::returnToCwg, user ? 500 : 900);
    }

    private void go(String a) {
        Intent i=new Intent(this,NativeRadioService.class).setAction(a);
        if(Build.VERSION.SDK_INT>=26) startForegroundService(i); else startService(i);
    }

    private void returnToCwg(){
        try{
            Intent x=getPackageManager().getLaunchIntentForPackage("com.softartstudio.carwebguru");
            if(x!=null){x.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);startActivity(x);}
        }catch(Throwable ignored){}
    }
    private TextView t(String s,float n){TextView v=new TextView(this);v.setText(s);v.setTextColor(Color.WHITE);v.setTextSize(n);v.setPadding(4,12,4,12);return v;}
    private Button b(String s){Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setLayoutParams(new LinearLayout.LayoutParams(-1,70));return b;}
}
