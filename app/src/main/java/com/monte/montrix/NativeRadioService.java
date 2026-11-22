package com.monte.montrix;

import android.app.*;import android.content.*;import android.graphics.*;import android.media.*;import android.media.session.*;import android.os.*;import android.view.KeyEvent;import android.util.Log;

/** Backend only. FYT radio is exposed as a real controllable Android media session. */
public class NativeRadioService extends Service implements SyuRadioClient.Listener {
 public static final String ACTION_TEST="com.monte.cwgradio.TEST_RADIO",ACTION_RADIO="com.monte.cwgradio.RADIO",ACTION_MUSIC="com.monte.cwgradio.MUSIC",ACTION_STATE="com.monte.cwgradio.RADIO_STATE",ACTION_RADIO_PREV="com.monte.cwgradio.RADIO_PREV",ACTION_RADIO_NEXT="com.monte.cwgradio.RADIO_NEXT",ACTION_RADIO_TOGGLE="com.monte.cwgradio.RADIO_TOGGLE",ACTION_TUNE="com.monte.cwgradio.TUNE";
 private final Runnable diagnosticTick=new Runnable(){public void run(){DiagnosticLogger.snapshot(NativeRadioService.this,"periodic");mainHandler.postDelayed(this,30000);}}; private BroadcastReceiver diagnosticReceiver; private MediaSession session; private SyuRadioClient syu; private AudioManager audio; private boolean oemRadioLaunched=false; private final Handler mainHandler=new Handler(Looper.getMainLooper());
 private static final String[] DEMO_FREQ={"98.9 FM","101.5 FM","93.3 FM","90.5 FM"};
 private static final String[] DEMO_PS={"KUTX","KROX","KGSR","KUT"};
 private static final String[] DEMO_RT={"Live radio — V9 simulator","Demo station — next/previous now behave as radio tuning","RadioText can be long and CWG can marquee it","News and information"};
 private com.monte.montrix.radio.backend.EmulatorSdrBackend emulatorBackend;
 private boolean emulatorMode(){return android.os.Build.FINGERPRINT.contains("generic") || android.os.Build.HARDWARE.contains("ranchu") || android.os.Build.HARDWARE.contains("goldfish");}
 private final NativeRdsReader nativeRds=new NativeRdsReader();
 private final RdsStateGate rdsGate=new RdsStateGate();
  private final java.util.concurrent.ExecutorService rdsExecutor=java.util.concurrent.Executors.newSingleThreadExecutor();
  private final Runnable rdsTick=new Runnable(){@Override public void run(){
    if(radio && isRealFyt() && !rdsExecutor.isShutdown()) {
      rdsExecutor.execute(()->{
        final long epoch=rdsGate.epoch();
        final String p=nativeRds.ps(),r=nativeRds.rt();
        if(p!=null || r!=null) mainHandler.post(()->{
          if(!radio || epoch!=rdsGate.epoch())return;
          if(p!=null)rdsGate.acceptPs(p,"sqlfmservice:27");
          if(r!=null)rdsGate.acceptRt(r,"sqlfmservice:28");
          ps=rdsGate.ps(); rt=rdsGate.rt();
          getSharedPreferences("radio_cache",0).edit().putString("ps",ps).putString("rt",rt).apply();
          if(r!=null)Log.i("ASURE_RDS","RT_RECEIVED length="+r.length());
          publish();
        });
      });
    }
    mainHandler.postDelayed(this,1500);
  }};
  private int pendingSeekFromKhz=0; private String pendingSeekDirection=""; private boolean liveFrequencyReceived=false; private int tuneTargetKhz=0,tuneAttempts=0; private long lastTuneCommandMs=0; private String tuneStatus="";
 private static int parseKhz(String value){if(value==null)return 0;java.util.regex.Matcher m=java.util.regex.Pattern.compile("(\\d{2,3}\\.\\d)").matcher(value);if(!m.find())return 0;try{return Math.round(Float.parseFloat(m.group(1))*1000f);}catch(Exception e){return 0;}}
 private void startPresetTune(int target){
  if(emulatorMode()){radio=true;if(emulatorBackend!=null)emulatorBackend.tune(target);else tuneStatus="SDR backend unavailable";return;}
  if(target<87500||target>108000||target%200!=100){Log.w("ASURE_QA","TUNE_REJECTED invalid_frequency="+target);return;}
  if(syu==null||!syu.connected()){tuneStatus="Tuner unavailable";Log.w("ASURE_QA","TUNE_FAILED toolkit_not_connected");publish();return;}
  tuneTargetKhz=target;
  tuneStatus="Tuning to "+String.format(java.util.Locale.US,"%.1f",target/1000f)+" FM";
  boolean dispatched=syu.tuneFrequencyKhz(target);
  Log.i("ASURE_QA","DIRECT_PRESET_TUNE target="+target+" dispatched="+dispatched);
  DiagnosticLogger.event(this,"DIRECT_PRESET_TUNE","target="+target+" dispatched="+dispatched);
  if(!dispatched){tuneStatus="Preset command rejected";tuneTargetKhz=0;publish();return;}
  publish();
  mainHandler.postDelayed(()->{
   if(tuneTargetKhz!=target)return;
   int observed=parseKhz(freq);
   if(liveFrequencyReceived&&observed==target){
    tuneStatus="";Log.i("ASURE_QA","DIRECT_PRESET_VERIFIED target="+target);
   }else{
    tuneStatus="Preset sent; tuner confirmation unavailable";
    Log.w("ASURE_QA","DIRECT_PRESET_UNCONFIRMED target="+target+" observed="+observed+" live="+liveFrequencyReceived);
   }
   tuneTargetKhz=0;publish();
  },3000);
 }
 private String freq="",ps="FM radio",rt="Waiting for FYT radio data"; private boolean radio; private int testStep=0;
 @Override public void onCreate(){super.onCreate();audio=(AudioManager)getSystemService(AUDIO_SERVICE);ensureChannel();
  session=new MediaSession(this,"FYT Radio"); session.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS|MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
  session.setCallback(new MediaSession.Callback(){
   @Override public boolean onMediaButtonEvent(Intent i){return super.onMediaButtonEvent(i);}
   @Override public void onPlay(){radio=true;session.setActive(true);publish();}
   @Override public void onPause(){Log.i("ASURE_QA","Radio pause ignored");}
   @Override public void onSkipToNext(){if(isRealFyt()){if(syu==null||!syu.seekNext())Log.w("SYU_RADIO","OEM-disabled: media next not dispatched");}else{testStep=(testStep+1)%DEMO_FREQ.length;freq=DEMO_FREQ[testStep];ps=DEMO_PS[testStep];rt=DEMO_RT[testStep];publish();}}
   @Override public void onSkipToPrevious(){if(isRealFyt()){if(syu==null||!syu.seekPrevious())Log.w("SYU_RADIO","OEM-disabled: media previous not dispatched");}else{testStep=(testStep-1+DEMO_FREQ.length)%DEMO_FREQ.length;freq=DEMO_FREQ[testStep];ps=DEMO_PS[testStep];rt=DEMO_RT[testStep];publish();}}
   @Override public void onStop(){setState(PlaybackState.STATE_STOPPED);}
  });
  if(emulatorMode()){emulatorBackend=new com.monte.montrix.radio.backend.EmulatorSdrBackend((f,p,r,status)->{if(f!=null)onRadio(f,p,r);if(status!=null&&!status.isEmpty()){tuneStatus=status;publish();}});emulatorBackend.start();}else{syu=new SyuRadioClient(this,this);syu.start();}startForeground(61,note("Ready"));
  DiagnosticLogger.event(this,"SERVICE_CREATE","version=26.16.0-direct-preset-candidate");
  android.content.IntentFilter filter=new android.content.IntentFilter();
  for(String action:new String[]{Intent.ACTION_SCREEN_ON,Intent.ACTION_SCREEN_OFF,Intent.ACTION_POWER_CONNECTED,Intent.ACTION_POWER_DISCONNECTED,Intent.ACTION_SHUTDOWN,Intent.ACTION_TIME_CHANGED,AudioManager.ACTION_AUDIO_BECOMING_NOISY})filter.addAction(action);
  diagnosticReceiver=new BroadcastReceiver(){public void onReceive(Context c,Intent i){DiagnosticLogger.snapshot(c,"broadcast:"+i.getAction());DiagnosticLogger.event(c,"POWER_TRANSITION","action="+i.getAction()+" radio="+radio+" uptimeMs="+android.os.SystemClock.uptimeMillis());}};
  registerReceiver(diagnosticReceiver,filter);mainHandler.post(diagnosticTick);mainHandler.post(rdsTick);
 }
 @Override public int onStartCommand(Intent i,int flags,int id){String a=i==null?ACTION_RADIO:i.getAction();
  DiagnosticLogger.event(this,"SERVICE_COMMAND","action="+a+" flags="+flags+" id="+id);if(ACTION_MUSIC.equals(a)) selectMusic();
  else if(ACTION_RADIO_PREV.equals(a) && emulatorMode()){radio=true;if(emulatorBackend!=null)emulatorBackend.seek(false);}
  else if(ACTION_RADIO_NEXT.equals(a) && emulatorMode()){radio=true;if(emulatorBackend!=null)emulatorBackend.seek(true);}
  else if(ACTION_RADIO_PREV.equals(a)){tuneTargetKhz=0;radio=true;int before=parseKhz(freq);boolean ok=syu!=null&&syu.seekPrevious();Log.i("ASURE_QA","SEEK_PREVIOUS dispatched="+ok+" before="+before);DiagnosticLogger.event(this,"TUNER_SEEK","direction=previous dispatched="+ok+" before="+before);if(ok)verifySeekChange(before,"previous");else Log.w("SYU_RADIO","OEM-disabled: seek previous fallback suppressed");}
  else if(ACTION_RADIO_NEXT.equals(a)){tuneTargetKhz=0;radio=true;int before=parseKhz(freq);boolean ok=syu!=null&&syu.seekNext();Log.i("ASURE_QA","SEEK_NEXT dispatched="+ok+" before="+before);DiagnosticLogger.event(this,"TUNER_SEEK","direction=next dispatched="+ok+" before="+before);if(ok)verifySeekChange(before,"next");else Log.w("SYU_RADIO","OEM-disabled: seek next fallback suppressed");}
  else if(ACTION_TUNE.equals(a)){radio=true;int khz=i.getIntExtra("frequency_khz",0);DiagnosticLogger.event(this,"TUNER_TUNE_REQUEST","khz="+khz+" connected="+(syu!=null&&syu.connected()));startPresetTune(khz);}
  else if(ACTION_RADIO_TOGGLE.equals(a)){Log.i("ASURE_QA","IGNORED_RADIO_TOGGLE: radio has no play/pause control");}
  else {if(ACTION_TEST.equals(a)&&!isRealFyt()){testStep=0;freq=DEMO_FREQ[0];ps=DEMO_PS[0];rt=DEMO_RT[0];}selectRadio();}
  return START_STICKY; }
 private void verifySeekChange(final int before,final String direction){
  mainHandler.postDelayed(()->{
   int after=parseKhz(freq);
   if(before>0&&after>0&&before!=after){Log.i("ASURE_QA","SEEK_VERIFIED direction="+direction+" before="+before+" after="+after);DiagnosticLogger.event(this,"SEEK_VERIFIED","direction="+direction+" before="+before+" after="+after);}
   else{Log.w("ASURE_QA","SEEK_UNCONFIRMED direction="+direction+" before="+before+" after="+after+" (binder dispatch is not tuner success)");DiagnosticLogger.event(this,"SEEK_UNCONFIRMED","direction="+direction+" before="+before+" after="+after);}
  },2800);
 }
 private void selectRadio(){DiagnosticLogger.snapshot(this,"selectRadio");Log.i("SYU_RADIO","selectRadio: attempting direct FYT tuner; OEM launch suppressed; direct toolkit required");
  // Do not send a global MEDIA_PAUSE: on FYT this can pause the OEM radio or music app unexpectedly.
  DiagnosticLogger.event(this,"RADIO_ACTIVATE","global_media_pause_suppressed");
  radio=true; session.setActive(true);
  if(emulatorMode()){tuneStatus="";publish();return;}
  if(isRealFyt()){
   freq=getSharedPreferences("radio_cache",0).getString("freq",""); ps=getSharedPreferences("radio_cache",0).getString("ps","FM radio"); rt=getSharedPreferences("radio_cache",0).getString("rt","Waiting for live RDS"); rdsGate.tuned(freq); if(syu==null || !syu.connected() || !syu.activate()){
    Log.w("SYU_RADIO","DIRECT activation unavailable; will retry after service binding, then use OEM fallback");
    mainHandler.postDelayed(()->{if(!radio)return;if(syu!=null&&syu.connected()&&syu.activate()){
      Log.i("SYU_RADIO","DIRECT activation succeeded after delayed binding");
    }else {Log.w("SYU_RADIO","Direct tuner activation unavailable; open factory radio using RADIO app if needed");DiagnosticLogger.event(this,"RADIO_ACTIVATION_FAILED","direct toolkit unavailable");}},1600);
   }else{Log.i("SYU_RADIO","DIRECT FYT activation succeeded; no OEM activity launch");}
  }
  publish();
 }
 private void launchOemRadio(){
  try {
   Intent launch=getPackageManager().getLaunchIntentForPackage("com.syu.radio");
   if(launch==null){Log.e("SYU_RADIO","OEM radio has no accessible launch intent; cannot activate tuner");rt="OEM radio launcher unavailable";return;}
   Log.i("SYU_RADIO","OEM launch component="+launch.getComponent());
   launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
   startActivity(launch);
   oemRadioLaunched=true;
   // OEM activity initializes FYT audio. Bring our dashboard back without stopping FM.
   mainHandler.postDelayed(()->{try{Intent back=new Intent(this,DashboardActivity.class);
    back.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_REORDER_TO_FRONT|Intent.FLAG_ACTIVITY_SINGLE_TOP);
    startActivity(back);}catch(Throwable e){Log.e("SYU_RADIO","dashboard return failed",e);}},1200);
   Log.i("SYU_RADIO","OEM radio launch requested; Android may show factory UI");
  } catch(Throwable error){Log.e("SYU_RADIO","OEM radio launch failed",error);rt="OEM radio launch failed";}
 }
 private boolean isRealFyt(){String h=android.os.Build.HARDWARE==null?"":android.os.Build.HARDWARE.toLowerCase();String d=android.os.Build.DEVICE==null?"":android.os.Build.DEVICE.toLowerCase();return h.contains("uis")||d.contains("uis8581")||d.contains("fyt");}
 private void publishPaused(){Bitmap art=BitmapFactory.decodeResource(getResources(),R.drawable.radio_art);MediaMetadata md=new MediaMetadata.Builder().putString(MediaMetadata.METADATA_KEY_MEDIA_ID,"fyt-radio").putString(MediaMetadata.METADATA_KEY_TITLE,ps+"  •  "+freq).putString(MediaMetadata.METADATA_KEY_ARTIST,rt).putString(MediaMetadata.METADATA_KEY_ALBUM,"FYT Radio").putString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE,ps+"  •  "+freq).putString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE,rt).putBitmap(MediaMetadata.METADATA_KEY_ART,art).putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART,art).putBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON,art).build();session.setMetadata(md);setState(PlaybackState.STATE_PAUSED);}

 private void selectMusic(){DiagnosticLogger.snapshot(this,"selectMusic");radio=false;setState(PlaybackState.STATE_STOPPED);session.setActive(false);try{audio.abandonAudioFocus(null);}catch(Throwable ignored){}
  // V26.1: DashboardActivity owns the visible UI. Do not launch FYT Music or CarWebGuru.
 }
 private void returnToCwg(){ /* V26.1: intentionally no-op */ }
 private void mediaKey(int code){try{audio.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,code));audio.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,code));}catch(Throwable ignored){}}
 private void radioCommand(String a){try{Log.i("SYU_RADIO","unverified OEM tuner broadcast "+a);DiagnosticLogger.event(this,"TUNER_FALLBACK_BROADCAST","action="+a+" unverified=true");sendBroadcast(new Intent(a).setPackage("com.syu.radio"));}catch(Throwable error){Log.e("SYU_RADIO","broadcast tuner command failed",error);}}
 private void setState(int s){session.setPlaybackState(new PlaybackState.Builder().setActions(PlaybackState.ACTION_SKIP_TO_NEXT|PlaybackState.ACTION_SKIP_TO_PREVIOUS).setState(s,PlaybackState.PLAYBACK_POSITION_UNKNOWN,1f).build());}
 private void publish(){Bitmap art=BitmapFactory.decodeResource(getResources(),R.drawable.radio_art);
  MediaMetadata md=new MediaMetadata.Builder().putString(MediaMetadata.METADATA_KEY_MEDIA_ID,"fyt-radio").putString(MediaMetadata.METADATA_KEY_TITLE,ps+"  •  "+freq).putString(MediaMetadata.METADATA_KEY_ARTIST,rt).putString(MediaMetadata.METADATA_KEY_ALBUM,"FYT Radio").putString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE,ps+"  •  "+freq).putString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE,rt).putString(MediaMetadata.METADATA_KEY_DISPLAY_DESCRIPTION,"FYT Radio").putBitmap(MediaMetadata.METADATA_KEY_ART,art).putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART,art).putBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON,art).build();
  session.setMetadata(md);setState(PlaybackState.STATE_PLAYING);
  // Metadata fallback for CWG's built-in Spotify-compatible receiver.
  Intent m=new Intent("com.spotify.music.metadatachanged");m.putExtra("id","fyt-radio");m.putExtra("artist",ps);m.putExtra("album",freq);m.putExtra("track",rt);m.putExtra("length",0);sendBroadcast(m);
  Intent p=new Intent("com.spotify.music.playbackstatechanged");p.putExtra("playing",true);p.putExtra("playbackPosition",0);sendBroadcast(p);
  Intent st=new Intent(ACTION_STATE).setPackage(getPackageName());st.putExtra("freq",freq);st.putExtra("ps",ps);st.putExtra("rt",rt);st.putExtra("tune_status",tuneStatus);sendBroadcast(st); NotificationManager nm=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);if(nm!=null)nm.notify(61,note(ps+" • "+freq));
 }
 @Override public void onRadio(String f,String p,String r){Log.i("SYU_RADIO","onRadio freq="+f+" ps="+p+" rt="+r);DiagnosticLogger.event(this,"TUNER_CALLBACK","frequency="+f+" ps="+p+" rt="+r);if(f!=null&&!f.trim().isEmpty()){liveFrequencyReceived=true;Log.i("ASURE_QA","LIVE_FREQUENCY_CALLBACK value="+f);}
  if(f!=null&&!f.trim().isEmpty()&&!f.equals(freq)){freq=f;rdsGate.tuned(f);ps=rdsGate.ps();rt=rdsGate.rt();Log.i("ASURE_QA","FREQUENCY_CHANGED "+f);}if(p!=null)rdsGate.acceptPs(p,"syu-callback");if(r!=null)rdsGate.acceptRt(r,"syu-callback");ps=rdsGate.ps();rt=rdsGate.rt();getSharedPreferences("radio_cache",0).edit().putString("freq",freq).putString("ps",ps).putString("rt",rt).apply();DiagnosticLogger.event(this,"RDS_UPDATE","freq="+freq+" ps="+ps+" rt="+rt);if(radio)publish();}
 private Notification note(String txt){PendingIntent pi=PendingIntent.getActivity(this,0,new Intent(this,RadioActivity.class),PendingIntent.FLAG_UPDATE_CURRENT|(Build.VERSION.SDK_INT>=23?PendingIntent.FLAG_IMMUTABLE:0));Notification.Builder b=new Notification.Builder(this,Build.VERSION.SDK_INT>=26?"radio_bridge":"").setSmallIcon(android.R.drawable.ic_media_play).setContentTitle("FYT Radio").setContentText(txt).setContentIntent(pi).setOngoing(true);if(Build.VERSION.SDK_INT>=21)b.setStyle(new Notification.MediaStyle().setMediaSession(session==null?null:session.getSessionToken()).setShowActionsInCompactView(0,1,2)).addAction(android.R.drawable.ic_media_previous,"Previous",mediaPI(KeyEvent.KEYCODE_MEDIA_PREVIOUS,1)).addAction(android.R.drawable.ic_media_pause,"Pause",mediaPI(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,2)).addAction(android.R.drawable.ic_media_next,"Next",mediaPI(KeyEvent.KEYCODE_MEDIA_NEXT,3));return b.build();}
 private PendingIntent mediaPI(int key,int req){Intent i=new Intent(Intent.ACTION_MEDIA_BUTTON);i.setPackage(getPackageName());i.putExtra(Intent.EXTRA_KEY_EVENT,new KeyEvent(KeyEvent.ACTION_DOWN,key));return PendingIntent.getBroadcast(this,req,i,PendingIntent.FLAG_UPDATE_CURRENT|(Build.VERSION.SDK_INT>=23?PendingIntent.FLAG_IMMUTABLE:0));}
 private void ensureChannel(){if(Build.VERSION.SDK_INT>=26){NotificationManager n=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);n.createNotificationChannel(new NotificationChannel("radio_bridge","FYT Radio",NotificationManager.IMPORTANCE_LOW));}}
 @Override public IBinder onBind(Intent i){return null;} @Override public void onDestroy(){DiagnosticLogger.event(this,"SERVICE_DESTROY","radio="+radio);mainHandler.removeCallbacks(diagnosticTick);mainHandler.removeCallbacks(rdsTick);rdsExecutor.shutdownNow();if(diagnosticReceiver!=null)unregisterReceiver(diagnosticReceiver);if(syu!=null)syu.stop();if(emulatorBackend!=null)emulatorBackend.stop();if(session!=null){session.setActive(false);session.release();}super.onDestroy();}
}
