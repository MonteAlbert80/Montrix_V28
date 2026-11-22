package com.monte.montrix.model.music;
import com.monte.montrix.model.ObservableModel;
/** Observable, presentation-ready music state. No Android UI or playback engine. */
public final class MusicModel extends ObservableModel {
 /** Requests routed by the coordinator, never by the View to a service. */ public interface Actions {void playPause();void next();void previous();void seekFraction(int fraction);}
 /** Current action receiver. */ private Actions actions;
 /** Display-ready title. */ public String title="No music indexed";
 /** Display-ready artist. */ public String artist="";
 /** Formatted elapsed time. */ public String elapsed="00:00";
 /** Formatted duration. */ public String duration="00:00";
 /** Seek progress from 0 to 1000. */ public int progress;
 /** Whether media is playing. */ public boolean playing;
 /** Whether seeking is available. */ public boolean seekable;
 /** Installs a command receiver. @param receiver coordinator commands */ public void setActions(Actions receiver){actions=receiver;}
 /** Requests playback toggle. */ public void requestPlayPause(){if(actions!=null)actions.playPause();}
 /** Requests next track. */ public void requestNext(){if(actions!=null)actions.next();}
 /** Requests previous track. */ public void requestPrevious(){if(actions!=null)actions.previous();}
 /** Requests seeking at a normalized position. @param fraction 0 to 1000 */ public void requestSeek(int fraction){if(actions!=null)actions.seekFraction(fraction);}
 /** Replaces presentation state. @param name title @param by artist @param elapsedText elapsed @param durationText duration @param value progress @param isPlaying playback @param canSeek seek availability */
 public void update(String name,String by,String elapsedText,String durationText,int value,boolean isPlaying,boolean canSeek){title=name;artist=by;elapsed=elapsedText;duration=durationText;progress=value;playing=isPlaying;seekable=canSeek;changed();}
}
