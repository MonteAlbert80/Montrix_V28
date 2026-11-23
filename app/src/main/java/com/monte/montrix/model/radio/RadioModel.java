package com.monte.montrix.model.radio;
import com.monte.montrix.model.ObservableModel;
/** Radio presentation state and UI intents; no hardware access. */
public final class RadioModel extends ObservableModel {
 public interface Actions { void seek(boolean up); void tune(int khz); void play(boolean enabled); }
 private Actions actions;
 public String primary="--"; public String secondary=""; public boolean playing=false;
 public void setActions(Actions a){actions=a;}
 public void requestSeek(boolean up){if(actions!=null)actions.seek(up);}
 public void requestTune(int khz){if(actions!=null)actions.tune(khz);}
 public void requestPlay(boolean enabled){if(actions!=null)actions.play(enabled);}
 public void update(String main,String detail){primary=main;secondary=detail;changed();}
 public void setPlaying(boolean p){playing=p;changed();}
}
