package com.monte.montrix.coordinator;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.monte.montrix.model.DashboardModel;
import com.monte.montrix.model.MediaSelectionModel;
import com.monte.montrix.service.ClockService;
import com.monte.montrix.service.music.MusicService;
/** Wires service outputs into component models and owns service lifecycle. */
public final class DashboardCoordinator {
 /** Dashboard state aggregate. */ private final DashboardModel model;
 /** Playback service. */ private final MusicService musicService;
 /** Music command and state adapter. */ private final MusicCoordinator music;
 private final RadioCoordinator radio;
 /** Clock formatting service. */ private final ClockService clock=new ClockService();
 /** Main-thread scheduler. */ private final Handler handler=new Handler(Looper.getMainLooper());
 /** Whether the dashboard is active. */ private boolean active;
 /** Whether services have started. */ private boolean started;
 /** Periodic service-to-model refresh. */ private final Runnable refresh=new Runnable(){/** Refreshes and reschedules. */@Override public void run(){if(!active)return;music.refresh();model.clock.update(clock.time(),clock.date());handler.postDelayed(this,1000);}};
 /** Initializes services and model bindings. @param context Android context @param state dashboard state */
 public DashboardCoordinator(Context context,DashboardModel state){model=state;
 musicService=new MusicService(context);music=new MusicCoordinator(musicService,model.music);radio=new RadioCoordinator(model.radio);
 model.media.setActions(source->{
   if(source!=MediaSelectionModel.Source.MUSIC && model.music.playing)music.playPause();
   radio.select(source==MediaSelectionModel.Source.RADIO);
   model.media.update(source);
 });model.radio.update("-- FM","Connecting to RTL-SDR bridge");model.bluetooth.update("Bluetooth","No connected device data");model.speed.update("-- MPH","CAN speed decoder not verified");model.speedlimit.update("--","No road limit reading");model.map.update("Offline map","Map integration pending");model.shift.update("Shift idle","Trip tracking integration pending");model.vehicle.update("Vehicle status","No CAN status available");model.album.update("Picture album","Photo selection integration pending");}
 /** Starts media indexing and refresh. */ public void start(){if(!started){started=true;try{musicService.scan();}catch(SecurityException e){model.music.update("Music permission needed","","00:00","00:00",0,false,false);}music.refresh();radio.start();}resume();}
 /** Resumes periodic updates. */ public void resume(){if(!started||active)return;active=true;handler.post(refresh);}
 /** Suspends periodic updates. */ public void pause(){active=false;handler.removeCallbacks(refresh);}
 /** Releases services and observers. */ public void close(){pause();model.media.setActions(null);music.close();radio.close();}
}
