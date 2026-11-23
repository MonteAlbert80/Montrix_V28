package com.monte.montrix.view.radio;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.*;
import com.monte.montrix.model.radio.RadioModel;
/** Radio presentation and intents only; never opens hardware or audio streams. */
public final class RadioView extends LinearLayout {
 private final RadioModel model; private final TextView primary,secondary; private final Button play;
 public RadioView(Context context,RadioModel state){super(context);model=state;setOrientation(VERTICAL);setPadding(10,6,10,6);setBackgroundColor(Color.rgb(12,23,34));
 TextView heading=new TextView(context);heading.setText("RADIO · RTL-SDR");heading.setTextColor(Color.rgb(110,195,255));heading.setTextSize(13);addView(heading);
 primary=new TextView(context);primary.setTextSize(23);primary.setTextColor(Color.WHITE);addView(primary);
 secondary=new TextView(context);secondary.setTextSize(12);secondary.setSingleLine(true);secondary.setTextColor(Color.LTGRAY);addView(secondary);
 LinearLayout controls=new LinearLayout(context);controls.setGravity(Gravity.CENTER_VERTICAL);addView(controls);
 Button down=new Button(context);down.setText("◀ SEEK");down.setOnClickListener(v->model.requestSeek(false));controls.addView(down,new LayoutParams(0,54,1));
 play=new Button(context);play.setOnClickListener(v->model.requestPlay(!model.playing));controls.addView(play,new LayoutParams(0,54,1));
 Button up=new Button(context);up.setText("SEEK ▶");up.setOnClickListener(v->model.requestSeek(true));controls.addView(up,new LayoutParams(0,54,1));
 model.addListener(this::render);render();}
 public void render(){primary.setText(model.primary);secondary.setText(model.secondary);play.setText(model.playing?"STOP":"PLAY");}
}
