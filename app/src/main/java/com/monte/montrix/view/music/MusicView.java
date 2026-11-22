package com.monte.montrix.view.music;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.*;
import com.monte.montrix.model.music.MusicModel;
/** Pure presentation of MusicModel; never references services or MediaPlayer. */
public final class MusicView extends LinearLayout {
 /** Bound state. */ private final MusicModel model;
 /** Track title widget. */ private final TextView title;
 /** Artist widget. */ private final TextView artist;
 /** Position widget. */ private final TextView elapsed;
 /** Duration widget. */ private final TextView duration;
 /** User-controlled song progress. */ private final SeekBar seek;
 /** Playback button. */ private final Button play;
 /** Prevents playback refresh from moving the thumb while dragging. */ private boolean dragging;
 /** Creates a music panel. @param context Android context @param state observable music state */
 public MusicView(Context context,MusicModel state){super(context);model=state;setOrientation(VERTICAL);setPadding(14,8,14,8);setBackgroundColor(Color.rgb(12,23,34));title=label(context,20);artist=label(context,14);elapsed=label(context,12);duration=label(context,12);addView(title);addView(artist);seek=new SeekBar(context);seek.setMax(1000);addView(seek,new LayoutParams(-1,48));LinearLayout times=new LinearLayout(context);times.setOrientation(HORIZONTAL);elapsed.setGravity(Gravity.LEFT);duration.setGravity(Gravity.RIGHT);times.addView(elapsed,new LayoutParams(0,32,1));times.addView(duration,new LayoutParams(0,32,1));addView(times);LinearLayout controls=new LinearLayout(context);Button previous=button(context,"Previous");play=button(context,"Play");Button next=button(context,"Next");controls.addView(previous,new LayoutParams(0,52,1));controls.addView(play,new LayoutParams(0,52,1));controls.addView(next,new LayoutParams(0,52,1));addView(controls);previous.setOnClickListener(v->model.requestPrevious());play.setOnClickListener(v->model.requestPlayPause());next.setOnClickListener(v->model.requestNext());seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){/** Receives progress gestures. */public void onProgressChanged(SeekBar b,int p,boolean user){} /** Marks gesture start. */public void onStartTrackingTouch(SeekBar b){dragging=true;} /** Dispatches normalized seek request. */public void onStopTrackingTouch(SeekBar b){dragging=false;model.requestSeek(b.getProgress());}});model.addListener(this::render);render();}
 /** Creates a text widget. @param context Android context @param size text size @return label */ private static TextView label(Context context,int size){TextView t=new TextView(context);t.setTextColor(Color.WHITE);t.setTextSize(size);return t;}
 /** Creates a control. @param context Android context @param caption button text @return button */ private static Button button(Context context,String caption){Button b=new Button(context);b.setText(caption);return b;}
 /** Copies model state to widgets without performing business calculations. */ public void render(){title.setText(model.title);artist.setText(model.artist);elapsed.setText(model.elapsed);duration.setText(model.duration);seek.setEnabled(model.seekable);if(!dragging)seek.setProgress(model.progress);play.setText(model.playing?"Pause":"Play");}
}
