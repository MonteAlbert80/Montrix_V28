package com.monte.montrix.coordinator;
import com.monte.montrix.model.music.MusicModel;
import com.monte.montrix.service.music.MusicService;
/** Binds music intent and service results to an observable model. */
public final class MusicCoordinator implements MusicModel.Actions {
 /** Playback engine. */private final MusicService service;
 /** Observable state. */private final MusicModel model;
 /** Creates and binds coordinator. @param service playback engine @param model state */public MusicCoordinator(MusicService service,MusicModel model){this.service=service;this.model=model;model.setActions(this);refresh();}
 /** Updates state from playback engine. */public void refresh(){int duration=service.duration();int position=service.position();int progress=duration<=0?0:(int)Math.min(1000,1000L*position/duration);model.update(service.title(),service.artist(),MusicService.format(position),MusicService.format(duration),progress,service.playing(),duration>0);}
 /** Toggles playback. */public void playPause(){service.toggle();refresh();}
 /** Advances to next track. */public void next(){service.next();refresh();}
 /** Selects previous track. */public void previous(){service.previous();refresh();}
 /** Seeks to normalized position. @param fraction normalized position */public void seekFraction(int fraction){service.seekFraction(fraction);refresh();}
 /** Closes playback. */public void close(){model.setActions(null);service.close();}
}
