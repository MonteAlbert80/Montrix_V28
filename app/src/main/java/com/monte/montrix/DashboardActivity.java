package com.monte.montrix;

import android.app.*;import android.os.*;import android.Manifest;import android.location.Location;import android.location.LocationListener;import android.location.LocationManager;import android.content.*;import android.content.pm.*;import android.database.Cursor;import android.graphics.*;import android.graphics.drawable.*;import android.media.*;import android.net.Uri;import android.provider.MediaStore;import android.view.*;import android.widget.*;import java.text.*;import java.util.*;

public class DashboardActivity extends Activity implements LocationListener {
 // Montrix modules: keep persistence and RDS presentation outside this legacy activity.
 private com.monte.montrix.trips.TripRecorder tripRecorder;

 LinearLayout root,rail,body,mediaText; TextView clock,date,source,title,detail,pos,dur,mapStatus; ImageView art,family; SeekBar progress; boolean musicUserSeeking=false; android.media.session.MediaSession musicSession; AudioManager audio; View mapHost; FytEmbeddedActivityHost fytHost; OfflineTexasMapView offlineMap; VehicleTemperatureProvider tempProvider; TextView tempView; TextView simStatus, speedPod, speedLimitSign, warningPod, shiftStartButton, shiftStopButton; SimVehicleState simState=new SimVehicleState(); final java.util.LinkedHashMap<String,String> activeSimWarnings=new java.util.LinkedHashMap<>(); final java.util.HashSet<String> suppressedSimWarnings=new java.util.HashSet<>(); final java.util.ArrayDeque<String> queuedSimWarnings=new java.util.ArrayDeque<>(); AlertDialog simWarningDialog; String displayedSimWarningKey=null; BroadcastReceiver simReceiver; MediaPlayer player; ArrayList<Song> songs=new ArrayList<>(); int song=-1; boolean radio=true; BroadcastReceiver radioRx; Handler h=new Handler(); LocationManager locationManager; boolean followVehicle=true; Location lastMapLocation; boolean guestMode=false; boolean isBtMode=false; boolean shuffle=false; java.util.ArrayList<Long> playHistory=new java.util.ArrayList<>(); int historyCursor=-1; java.security.SecureRandom random=new java.security.SecureRandom(); TextView shuffleButton,seekBackButton,seekForwardButton,musicBackButton,musicPlayButton,musicNextButton; boolean liveShift=false; double liveShiftMiles=0; Location lastShiftFix; long lastMediaLogAt=0; AudioManager.OnAudioFocusChangeListener focusListener=change->{DiagnosticLogger.event(this,"AUDIO_FOCUS_CHANGE","change="+change+" playerPlaying="+(player!=null&&player.isPlaying()));if(change==AudioManager.AUDIOFOCUS_LOSS && player!=null && player.isPlaying())player.pause();};
 static final int BLUE=Color.rgb(40,150,255), PANEL=Color.rgb(7,15,23), PANEL2=Color.rgb(12,23,34);
 static class Song{long id,albumId;String title,artist,path;Song(long i,long a,String t,String ar,String p){id=i;albumId=a;title=t;artist=ar;path=p;}}
 @Override public void onCreate(Bundle b){super.onCreate(b);tripRecorder=new com.monte.montrix.trips.TripRecorder(this);offlineSpeedLimits=new OfflineSpeedLimitReader(this);getWindow().setStatusBarColor(Color.BLACK);getWindow().setNavigationBarColor(Color.BLACK);hideSystemBars();audio=(AudioManager)getSystemService(AUDIO_SERVICE);song=getSharedPreferences("music_resume",0).getInt("index",-1);shuffle=getSharedPreferences("music_resume",0).getBoolean("shuffle",false);loadHistory();liveShift=getSharedPreferences("shift_live",0).getBoolean("active",false);liveShiftMiles=Double.longBitsToDouble(getSharedPreferences("shift_live",0).getLong("miles_bits",Double.doubleToLongBits(0)));DiagnosticLogger.event(this,"DASHBOARD_CREATE","radio_saved="+getSharedPreferences("audio_mode",0).getBoolean("radio",false)+" shift="+liveShift);build();setupMusicMediaSession();initializeSpeedFeature();scanMusic();/* MONTRIX_MUSIC_PERMISSION_V28 */ if(android.os.Build.VERSION.SDK_INT>=23 && checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)!=android.content.pm.PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},702);}registerRadio();restoreAudioMode();installSimulator();tick();startMapLocation();}
 GradientDrawable bg(int color,float r,int stroke){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(r);if(stroke>0)g.setStroke(stroke,BLUE);return g;}
 TextView tv(String s,int sp){TextView v=new TextView(this);v.setText(s);v.setTextColor(Color.WHITE);v.setTextSize(sp);v.setGravity(Gravity.CENTER_VERTICAL);return v;}
 TextView chip(String s){TextView v=tv(s,13);v.setGravity(Gravity.CENTER);v.setPadding(10,5,10,5);v.setBackground(bg(Color.rgb(10,34,54),15,1));return v;}
 Button button(String s,View.OnClickListener l){Button b=new Button(this);b.setText(s);b.setTextColor(Color.WHITE);b.setTextSize(14);b.setAllCaps(false);b.setBackground(new InstrumentDrawable(16,false));b.setOnClickListener(l);return b;}
 Space gap(int px){Space s=new Space(this);s.setLayoutParams(new LinearLayout.LayoutParams(px,px));return s;}
 void build(){
  root=new LinearLayout(this);root.setOrientation(LinearLayout.HORIZONTAL);root.setBackgroundResource(R.drawable.dashboard_background);setContentView(root);
  buildRail();
  body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(12,8,12,8);root.addView(body,new LinearLayout.LayoutParams(0,-1,1));
  buildHeader();buildCenter();buildMedia();
 }
 void buildRail(){rail=new LinearLayout(this);rail.setOrientation(LinearLayout.VERTICAL);rail.setPadding(10,14,10,12);rail.setBackgroundColor(Color.TRANSPARENT);root.addView(rail,new LinearLayout.LayoutParams(165,-1));
  TextView brand=tv(com.monte.montrix.branding.Brand.NAME.toUpperCase(java.util.Locale.US),24);brand.setGravity(Gravity.CENTER);brand.setTextColor(Color.rgb(181,224,247));rail.addView(brand,new LinearLayout.LayoutParams(-1,58));
  rail.addView(nav("ᛒ", "Guest BT",v->guestBluetooth()) ,new LinearLayout.LayoutParams(-1,0,1));rail.addView(nav("▦", "Apps",v->showApps()),new LinearLayout.LayoutParams(-1,0,1));rail.addView(nav("⚙", "Options",v->launch("com.android.settings")),new LinearLayout.LayoutParams(-1,0,1));rail.addView(nav("▤", "Vehicle",v->vehicleDiagnostics()),new LinearLayout.LayoutParams(-1,0,1));shiftStartButton=nav("▶", "Start Shift",v->shiftAction(true));rail.addView(shiftStartButton,new LinearLayout.LayoutParams(-1,0,1));shiftStopButton=nav("■", "Stop Shift",v->shiftAction(false));rail.addView(shiftStopButton,new LinearLayout.LayoutParams(-1,0,1));rail.addView(nav("➤", "Navigate",v->openMaps()),new LinearLayout.LayoutParams(-1,0,1));
 }
 TextView nav(String icon,String label,View.OnClickListener l){TextView v=tv(icon+"   "+label,16);v.setGravity(Gravity.CENTER_VERTICAL);v.setPadding(13,0,5,0);v.setBackground(new InstrumentDrawable(16,false));v.setOnClickListener(l);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-1);lp.setMargins(0,5,0,5);v.setLayoutParams(lp);return v;}
 void buildHeader(){FrameLayout header=new FrameLayout(this);header.setBackgroundColor(Color.TRANSPARENT);body.addView(header,new LinearLayout.LayoutParams(-1,132));
  LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setGravity(Gravity.CENTER);header.addView(c,new FrameLayout.LayoutParams(-1,-1));clock=tv("",48);clock.setGravity(Gravity.CENTER);date=tv("",16);date.setGravity(Gravity.CENTER);c.addView(clock,new LinearLayout.LayoutParams(-1,65));c.addView(date,new LinearLayout.LayoutParams(-1,36));
  // Three compact top-right instruments: live speed, regulatory-style limit, temperature.
  // Speed uses colored digits only; the speed-limit sign stays black on white.
  LinearLayout rightInstruments=new LinearLayout(this);rightInstruments.setOrientation(LinearLayout.HORIZONTAL);rightInstruments.setGravity(Gravity.CENTER_VERTICAL);
  FrameLayout.LayoutParams rightLp=new FrameLayout.LayoutParams(-2,92,Gravity.RIGHT|Gravity.CENTER_VERTICAL);rightLp.rightMargin=16;header.addView(rightInstruments,rightLp);
  LinearLayout speedBox=new LinearLayout(this);speedBox.setOrientation(LinearLayout.VERTICAL);speedBox.setGravity(Gravity.CENTER);
  speedBox.setBackground(new InstrumentDrawable(16,true));
  TextView speedLabel=tv("CURRENT SPEED",9);speedLabel.setTextColor(0xff91bbd2);speedLabel.setGravity(Gravity.CENTER);
  speedPod=tv("0",35);speedPod.setGravity(Gravity.CENTER);speedPod.setTextColor(0xff52db89);
  TextView speedUnit=tv("MPH",10);speedUnit.setTextColor(0xff91bbd2);speedUnit.setGravity(Gravity.CENTER);
  speedBox.addView(speedLabel,new LinearLayout.LayoutParams(-1,17));speedBox.addView(speedPod,new LinearLayout.LayoutParams(-1,46));speedBox.addView(speedUnit,new LinearLayout.LayoutParams(-1,15));
  rightInstruments.addView(speedBox,new LinearLayout.LayoutParams(104,86));
  speedLimitSign=tv("--",29);speedLimitSign.setGravity(Gravity.CENTER);speedLimitSign.setTextColor(Color.BLACK);
  speedLimitSign.setTypeface(android.graphics.Typeface.DEFAULT,android.graphics.Typeface.BOLD);
  speedLimitSign.setContentDescription("Speed limit unavailable");
  LinearLayout signStack=new LinearLayout(this);signStack.setOrientation(LinearLayout.VERTICAL);signStack.setGravity(Gravity.CENTER);signStack.setBackground(speedSignBackground());
  TextView signTitle=tv("SPEED",11);signTitle.setTextColor(Color.BLACK);signTitle.setTypeface(android.graphics.Typeface.DEFAULT,1);signTitle.setGravity(Gravity.CENTER);
  TextView signSubtitle=tv("LIMIT",11);signSubtitle.setTextColor(Color.BLACK);signSubtitle.setTypeface(android.graphics.Typeface.DEFAULT,1);signSubtitle.setGravity(Gravity.CENTER);
  signStack.addView(signTitle,new LinearLayout.LayoutParams(-1,17));signStack.addView(signSubtitle,new LinearLayout.LayoutParams(-1,15));signStack.addView(speedLimitSign,new LinearLayout.LayoutParams(-1,45));
  LinearLayout.LayoutParams signLp=new LinearLayout.LayoutParams(84,86);signLp.leftMargin=9;rightInstruments.addView(signStack,signLp);
  LinearLayout tempPod=new LinearLayout(this);tempPod.setOrientation(LinearLayout.VERTICAL);tempPod.setGravity(Gravity.CENTER);tempPod.setPadding(14,4,14,4);tempPod.setBackground(new InstrumentDrawable(18,true));LinearLayout.LayoutParams th=new LinearLayout.LayoutParams(142,82);th.leftMargin=9;rightInstruments.addView(tempPod,th);TextView tl=tv("OUTSIDE",10);tl.setTextColor(0xff7fcfff);tl.setGravity(Gravity.CENTER);tempView=tv("--°F",24);tempView.setGravity(Gravity.CENTER);tempPod.addView(tl,new LinearLayout.LayoutParams(-1,24));tempPod.addView(tempView,new LinearLayout.LayoutParams(-1,44));tempProvider=new VehicleTemperatureProvider(this,(text,live)->runOnUiThread(()->{tempView.setText(text);tempView.setTextColor(live?0xffffffff:0xff9eb2c2);}));tempProvider.start();
 }
 GradientDrawable speedSignBackground(){GradientDrawable d=new GradientDrawable();d.setColor(Color.WHITE);d.setCornerRadius(7);d.setStroke(3,Color.BLACK);return d;}
 void buildCenter(){LinearLayout center=new LinearLayout(this);center.setOrientation(LinearLayout.HORIZONTAL);center.setPadding(0,10,0,10);body.addView(center,new LinearLayout.LayoutParams(-1,0,1));
  FrameLayout fp=new FrameLayout(this);fp.setPadding(8,8,8,8);fp.setBackground(new InstrumentDrawable(22,false));center.addView(fp,new LinearLayout.LayoutParams(0,-1,47));family=new ImageView(this);family.setImageResource(R.drawable.family_3gen_real);family.setScaleType(ImageView.ScaleType.CENTER_CROP);family.setBackground(bg(Color.BLACK,18,0));family.setClipToOutline(true);family.setOutlineProvider(ViewOutlineProvider.BACKGROUND);fp.addView(family,new FrameLayout.LayoutParams(-1,-1));
  TextView cap=chip("THREE GENERATIONS");FrameLayout.LayoutParams cp=new FrameLayout.LayoutParams(-2,34,Gravity.LEFT|Gravity.BOTTOM);cp.setMargins(18,0,0,18);fp.addView(cap,cp);
  center.addView(new Space(this),new LinearLayout.LayoutParams(12,1));
  FrameLayout mp=new FrameLayout(this);mp.setPadding(7,7,7,7);mp.setBackground(new InstrumentDrawable(22,true));mp.setClipToOutline(true);mp.setOutlineProvider(ViewOutlineProvider.BACKGROUND);center.addView(mp,new LinearLayout.LayoutParams(0,-1,53));
  mapStatus=chip("MAP");mapStatus.setVisibility(View.GONE);
  installOfflineMap(mp);
  LinearLayout tools=new LinearLayout(this);tools.setOrientation(LinearLayout.VERTICAL);tools.setGravity(Gravity.CENTER_HORIZONTAL);tools.setPadding(0,8,0,0);FrameLayout.LayoutParams tp=new FrameLayout.LayoutParams(58,-2,Gravity.RIGHT|Gravity.TOP);tp.setMargins(0,8,14,0);mp.addView(tools,tp);
  TextView plus=mapTool("+");TextView minus=mapTool("−");TextView locate=mapTool("âŒ–");TextView full=mapTool("â†—");LinearLayout.LayoutParams mb=new LinearLayout.LayoutParams(46,46);mb.setMargins(0,0,0,6);tools.addView(plus,mb);
  LinearLayout.LayoutParams mb2=new LinearLayout.LayoutParams(46,46);mb2.setMargins(0,0,0,6);tools.addView(minus,mb2);
  LinearLayout.LayoutParams mb3=new LinearLayout.LayoutParams(46,46);mb3.setMargins(0,0,0,6);tools.addView(locate,mb3);
  LinearLayout.LayoutParams mb4=new LinearLayout.LayoutParams(46,46);tools.addView(full,mb4);
  plus.setOnClickListener(v->{if(offlineMap!=null)offlineMap.zoomBy(1);});minus.setOnClickListener(v->{if(offlineMap!=null)offlineMap.zoomBy(-1);});locate.setOnClickListener(v->locateMap());full.setOnClickListener(v->openMaps());
 }
 void installOfflineMap(FrameLayout mp){
  offlineMap=new OfflineTexasMapView(this);
  offlineMap.setOnManualMove(()->{followVehicle=false;});
  mapHost=offlineMap;
  mp.addView(offlineMap,0,new FrameLayout.LayoutParams(-1,-1));
 }
 void locateMap(){
  // The emulator must never jump to the generic AVD's cached Google location.
  if(!isRealFyt()){followVehicle=true;lastMapLocation=null;if(offlineMap!=null)offlineMap.centerHome();Toast.makeText(this,"Simulator GPS: home",Toast.LENGTH_SHORT).show();return;}
  if(Build.VERSION.SDK_INT<23||checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)centerOfflineOnDevice();else requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},701);
 }
 void centerOfflineOnDevice(){
  if(offlineMap==null)return;
  try{
   LocationManager lm=(LocationManager)getSystemService(LOCATION_SERVICE);Location best=null;
   // Prefer a genuine GPS fix. Network/passive fixes are accepted only if they point inside Texas.
   String[] order={LocationManager.GPS_PROVIDER,LocationManager.NETWORK_PROVIDER,LocationManager.PASSIVE_PROVIDER};
   for(String pr:order){try{Location q=lm.getLastKnownLocation(pr);if(q!=null&&OfflineTexasMapView.validTexas(q.getLatitude(),q.getLongitude())&&(best==null||q.getAccuracy()<best.getAccuracy()))best=q;}catch(Throwable ignored){}}
   if(best!=null){followVehicle=true;lastMapLocation=best;offlineMap.showPosition(best.getLatitude(),best.getLongitude(),true);}
   else {followVehicle=true;Toast.makeText(this,"Waiting for vehicle GPS fix",Toast.LENGTH_SHORT).show();startMapLocation();}
  }catch(Throwable e){Toast.makeText(this,"GPS unavailable",Toast.LENGTH_SHORT).show();}
 }
  // Current speed comes exclusively through the selected telemetry provider.
  /** Selected telemetry source, initialized once during Activity creation. */
  private com.monte.montrix.service.vehicle.VehicleDataProvider vehicleProvider;
  /** Single telemetry gateway for speed and future CAN features. */
  private com.monte.montrix.service.vehicle.CanBusService canBusService;
  /** Speed business logic, independent of physical or simulated source. */
  private com.monte.montrix.service.speed.CurrentSpeedService currentSpeedService;
  /** Presentation-only speed widget adapter. */
  private com.monte.montrix.view.speed.CurrentSpeedView currentSpeedView;
  /** Initializes the speed feature and its selected provider. */
  private void initializeSpeedFeature(){
    vehicleProvider=com.monte.montrix.service.runtime.RuntimeProviderFactory.createVehicleProvider();
    canBusService=new com.monte.montrix.service.vehicle.CanBusService(vehicleProvider);
    currentSpeedService=new com.monte.montrix.service.speed.CurrentSpeedService(canBusService);
    currentSpeedView=new com.monte.montrix.view.speed.CurrentSpeedView(speedPod);
    currentSpeedService.start(state->runOnUiThread(()->currentSpeedView.render(state)));
    canBusService.start();
  }

  private OfflineSpeedLimitReader offlineSpeedLimits;
  private final java.util.concurrent.ExecutorService speedLimitWorker = java.util.concurrent.Executors.newSingleThreadExecutor();
  private volatile long speedLimitSequence = 0L;
  private void lookupOfflineLimit(double latitude,double longitude,float accuracy) { lookupOfflineLimit(latitude,longitude,accuracy,android.os.SystemClock.elapsedRealtime()); }
  private void lookupOfflineLimit(double latitude,double longitude,float accuracy,long fixTimeMs) {
   if(offlineSpeedLimits==null)return;
   final long sequence=++speedLimitSequence;
   speedLimitWorker.execute(()->{
    OfflineSpeedLimitReader.Result result=offlineSpeedLimits.lookup(latitude,longitude,accuracy,fixTimeMs);
    runOnUiThread(()->{
     if(sequence!=speedLimitSequence || isFinishing())return;
     simState.speedLimitMph=result.mph;
     if(speedLimitSign!=null){new com.monte.montrix.view.speedlimit.SpeedLimitView(speedLimitSign).render(new com.monte.montrix.service.speedlimit.SpeedLimitService().snapshot(result.mph));
      speedLimitSign.setContentDescription(result.mph>0?"Offline speed limit "+result.mph+" mph":"Speed limit unavailable");}
     android.util.Log.i("MONTRIX_ROADPACK","source=SQLITE result="+result.reason+" mph="+result.mph+" road="+result.road);
     if(!isRealFyt())refreshSimUi();
    });
   });
  }

 void startMapLocation(){
  // Emulator GPS is intentionally subscribed as well as custom SIM_EVENT broadcasts.
  // Keep trip recording restricted to physical FYT hardware.
  try{locationManager=(LocationManager)getSystemService(LOCATION_SERVICE);if(Build.VERSION.SDK_INT<23||checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
   try{if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,0f,this);}catch(Throwable ignored){}
   try{if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,3000,0f,this);}catch(Throwable ignored){}
  }}catch(Throwable ignored){}
 }
 // MONTRIX_NAV_V6_SINGLE_SOURCE: ADB route broadcasts own navigation while streaming.
 long lastSimGpsBroadcastMs=Long.MIN_VALUE/2;
 @Override public void onLocationChanged(Location l){
  if(l==null)return;
  // Ignore competing emulator GPS callbacks while RoadPack replay is active.
  if(!isRealFyt() && android.os.SystemClock.elapsedRealtime()-lastSimGpsBroadcastMs<5000L)return;
  // GPS is used for navigation and speed-limit location only; never vehicle speed.
  lookupOfflineLimit(l.getLatitude(),l.getLongitude(),l.hasAccuracy()?l.getAccuracy():-1f,l.getElapsedRealtimeNanos()/1000000L);
  // Record every actual vehicle fix, regardless of shift-button state.
  // Emulator fixes are never allowed into the real driving journal.
  if(isRealFyt() && tripRecorder!=null) {
   try { tripRecorder.record(l); }
   catch(Throwable error) { android.util.Log.e("MONTRIX_TRIPS","GPS journal write failed",error); }
  }
  if(liveShift && l.hasAccuracy() && l.getAccuracy()<=50f){
   if(lastShiftFix!=null){long dt=l.getTime()-lastShiftFix.getTime();float meters=l.distanceTo(lastShiftFix);
    if(dt>0&&dt<=120000&&meters>=4f&&meters<=Math.max(30,dt*0.065f)){liveShiftMiles+=meters/1609.344;getSharedPreferences("shift_live",0).edit().putLong("miles_bits",Double.doubleToLongBits(liveShiftMiles)).apply();}}
   lastShiftFix=new Location(l);
  }
  DiagnosticLogger.event(this,"GPS_FIX","provider="+l.getProvider()+" lat="+l.getLatitude()+" lon="+l.getLongitude()+" accuracy="+(l.hasAccuracy()?l.getAccuracy():-1)+" speedMps="+(l.hasSpeed()?l.getSpeed():-1)+" shift="+liveShift+" miles="+String.format(Locale.US,"%.3f",liveShiftMiles));
  if(offlineMap==null||!OfflineTexasMapView.validTexas(l.getLatitude(),l.getLongitude()))return;
   if(!isRealFyt()){
     // MONTRIX_NAV_V4_GPS_BRIDGE: emulator GPS supplies position; simulator events supply road limits.
     float displacement=lastMapLocation==null?0f:lastMapLocation.distanceTo(l);
     long dt=lastMapLocation==null?0L:l.getElapsedRealtimeNanos()-lastMapLocation.getElapsedRealtimeNanos();
     // MONTRIX_NAV_V5_NO_GPS_SPEED: navigation must never write vehicle speed.
     // Vehicle speed remains owned by the existing CAN-bus path.
     boolean moving=(displacement>=8f&&dt>0)||simState.gpsMoving;
     float bearing=l.hasBearing()&&moving?l.getBearing():
         (moving&&lastMapLocation!=null?lastMapLocation.bearingTo(l):simState.headingDegrees);
     simState.latitude=l.getLatitude();simState.longitude=l.getLongitude();
     if(moving){simState.headingDegrees=(bearing+360f)%360f;simState.hasHeadingFix=true;}
     if(displacement>=8f&&dt>0)simState.gpsMoving=true;
     // No GPS-derived writes to simState.speedMph.
     // Do not reset speed when GPS stops or repeats a fix.
     offlineMap.setHeading(simState.headingDegrees,simState.gpsMoving);
     refreshSimUi();
     android.util.Log.i("MONTRIX_EMU_GPS","v4 fix lat="+l.getLatitude()+" lon="+l.getLongitude()+" displacement="+displacement+" bearing="+simState.headingDegrees+" moving="+moving+" speedMph="+simState.speedMph+" limitMph="+simState.speedLimitMph);
   }
  // Always move the blue live-location dot. Recenter only after meaningful vehicle movement.
  boolean moveCenter=followVehicle&&(lastMapLocation==null||l.distanceTo(lastMapLocation)>=12f);
  offlineMap.showPosition(l.getLatitude(),l.getLongitude(),moveCenter);
  if(moveCenter || !isRealFyt())lastMapLocation=new Location(l);
 }
 @Override public void onProviderEnabled(String p){} @Override public void onProviderDisabled(String p){} @Override public void onStatusChanged(String p,int s,Bundle b){}
 TextView mapTool(String s){TextView v=tv(s,21);v.setGravity(Gravity.CENTER);v.setPadding(0,0,0,1);v.setBackground(new InstrumentDrawable(14,false));v.setTextColor(Color.WHITE);return v;}
 @Override public void onRequestPermissionsResult(int r,String[] p,int[] g){super.onRequestPermissionsResult(r,p,g);if(r==701)locateMap(); if(r==702 && g.length>0 && g[0]==android.content.pm.PackageManager.PERMISSION_GRANTED){scanMusic();if(!radio && !isBtMode)showSong();}}
 void installSimulator(){
  if(isRealFyt())return;
  LinearLayout instruments=new LinearLayout(this);instruments.setGravity(Gravity.CENTER_VERTICAL);instruments.setPadding(6,2,6,2);
  warningPod=tv("VEHICLE OK Â· SIMULATION",17);warningPod.setGravity(Gravity.CENTER);warningPod.setTextColor(0xff99dcb3);warningPod.setBackground(new InstrumentDrawable(12,false));warningPod.setOnClickListener(v->showWarningList());
  LinearLayout.LayoutParams wp=new LinearLayout.LayoutParams(-1,55);instruments.addView(warningPod,wp);
  body.addView(instruments,new LinearLayout.LayoutParams(-1,60));
  simStatus=tv("SIM • ACC ON • 0 mph • fuel 70% • no warnings",13);
  simStatus.setTextColor(0xff65d9ff);
  simStatus.setSingleLine(true);
  body.addView(simStatus,new LinearLayout.LayoutParams(-1,32));
  simReceiver=new BroadcastReceiver(){@Override public void onReceive(Context c,Intent i){
    if(!"com.monte.cwgradio.SIM_EVENT".equals(i.getAction()))return;
    String event=i.getStringExtra("event");
    if("gps".equals(event))lastSimGpsBroadcastMs=android.os.SystemClock.elapsedRealtime();
    if("speed_limit".equals(event))return; // SQLite is the only speed-limit source.
    simState.apply(event,i);
    if("speed".equals(event) && vehicleProvider instanceof com.monte.montrix.service.vehicle.SimulatedCanBusProvider) ((com.monte.montrix.service.vehicle.SimulatedCanBusProvider)vehicleProvider).acceptRouteSpeed(simState.speedMph);
    if("gps".equals(event))lookupOfflineLimit(simState.latitude,simState.longitude,10f);
    updateWarningEvents(event,i);
    if("gps".equals(event)||"heading".equals(event)){
      if(offlineMap!=null){offlineMap.showPosition(simState.latitude,simState.longitude,followVehicle);offlineMap.setHeading(simState.headingDegrees,simState.gpsMoving);}
    }
    if("acc_off".equals(event)||"sleep".equals(event)){
      if(player!=null && player.isPlaying()) {player.pause();simState.pausedBySleep=true;}
      else simState.pausedBySleep=false;
    }else if("acc_on".equals(event)||"wake".equals(event)){
      if(simState.pausedBySleep && player!=null && !radio){try{requestMusicFocus();player.start();}catch(Exception ignored){}}
      simState.pausedBySleep=false;
    }
    // Cranking is not sleep: never pause or restart playback on crank events.
    if("manual_pause".equals(event)){if(player!=null&&player.isPlaying())player.pause();simState.pausedBySleep=false;}
    refreshSimUi();
    if(simState.temperatureF!=null && tempView!=null)new com.monte.montrix.view.temperature.TemperatureView(tempView).render(new com.monte.montrix.service.temperature.TemperatureService().snapshot(simState.temperatureF));
    android.util.Log.i("ASURE_SIM",simState.json());
  }};
  IntentFilter filter=new IntentFilter("com.monte.cwgradio.SIM_EVENT");
  if(android.os.Build.VERSION.SDK_INT>=33)registerReceiver(simReceiver,filter,Context.RECEIVER_EXPORTED);
  else registerReceiver(simReceiver,filter);
  refreshSimUi();
 }
 void refreshSimUi(){
  if(simStatus==null)return;
  simState.accumulate();simStatus.setText(simState.summary()+String.format(java.util.Locale.US," • %.2f mi",simState.tripMiles));
  // Speed rendering is exclusively owned by CurrentSpeedView.
  if(speedLimitSign!=null){speedLimitSign.setText(simState.speedLimitMph>0?String.valueOf(simState.speedLimitMph):"--");speedLimitSign.setContentDescription(simState.speedLimitMph>0?"Speed limit "+simState.speedLimitMph+" miles per hour":"Speed limit unavailable");}
  if(warningPod!=null){boolean alarm=!activeSimWarnings.isEmpty();warningPod.setText(alarm?"âš   "+activeSimWarnings.size()+" ACTIVE ALERT"+(activeSimWarnings.size()==1?"":"S")+"   •   "+activeSimWarnings.values().iterator().next()+"     ›":"VEHICLE CAN STATUS   •   NOT VERIFIED   ›");warningPod.setTextColor(alarm?0xffffb17a:0xff9ae5b7);}
  if(shiftStartButton!=null){boolean active=isRealFyt()?liveShift:simState.shift;shiftStartButton.setVisibility(active?View.GONE:View.VISIBLE);shiftStopButton.setVisibility(active?View.VISIBLE:View.GONE);}
  if(simStatus!=null)simStatus.setText(simState.summary()+String.format(java.util.Locale.US," • trip %.2f mi • ODO %.1f mi",simState.tripMiles,simState.odometerMiles));
 }
 /** Warning notifications are emulator-only. Suppressing a popup never clears a fault. */
 void updateWarningEvents(String event,Intent i){
  if(event==null)return;
  if("clear_warning".equals(event)){
   activeSimWarnings.clear();suppressedSimWarnings.clear();queuedSimWarnings.clear();
   if(simWarningDialog!=null){simWarningDialog.dismiss();simWarningDialog=null;displayedSimWarningKey=null;}
   return;
  }
  String key=null,value=null;
  if("warning".equals(event)){key="generic";value=simState.warning;}
  else if("door".equals(event)){key="door";value=simState.door;}
  else if("tpms".equals(event)){key="tpms";value=simState.tpms;}
  else if("battery".equals(event)){key="battery";value=simState.battery;}
  if(key==null)return;
  boolean normal=value==null||value.isEmpty()||"none".equalsIgnoreCase(value)||"normal".equalsIgnoreCase(value)||"closed".equalsIgnoreCase(value)||"ok".equalsIgnoreCase(value);
  if(normal){activeSimWarnings.remove(key);suppressedSimWarnings.remove(key);if(key.equals(displayedSimWarningKey)&&simWarningDialog!=null)simWarningDialog.dismiss();return;}
  String label=("generic".equals(key)?"VEHICLE":key.toUpperCase(java.util.Locale.US))+": "+value.replace('_',' ');
  String old=activeSimWarnings.put(key,label);
  // Repeated events refresh the alert; suppression applies only until a new occurrence.
  if(label.equals(old))suppressedSimWarnings.remove(key);
  if(!suppressedSimWarnings.contains(key))showSimWarning(key,label);
 }
 void showSimWarning(String key,String label){
  if(isFinishing())return;
  if(simWarningDialog!=null&&simWarningDialog.isShowing()){
    if(!key.equals(displayedSimWarningKey)&&!queuedSimWarnings.contains(key))queuedSimWarnings.addLast(key);
    return;
  }
  displayedSimWarningKey=key;
  LinearLayout panel=new LinearLayout(this);panel.setOrientation(LinearLayout.VERTICAL);panel.setPadding(28,24,28,16);panel.setBackground(bg(0xff142332,20,0));
  TextView header=tv("âš   VEHICLE ALERT  Â·  SIMULATION",19);header.setTextColor(0xffffbf82);panel.addView(header);
  TextView message=tv(label,24);message.setPadding(0,24,0,18);message.setTextColor(Color.WHITE);panel.addView(message);
  TextView explanation=tv("Suppress hides this popup only. The fault remains in Vehicle Status until cleared.",15);explanation.setTextColor(0xffbbcad6);panel.addView(explanation);
  simWarningDialog=new AlertDialog.Builder(this).setView(panel)
    .setPositiveButton("SUPPRESS",(d,w)->{suppressedSimWarnings.add(key);android.util.Log.i("ASURE_SIM","suppressed_popup="+key);})
    .setNegativeButton("ALL ALERTS",(d,w)->h.post(this::showWarningList)).create();
  simWarningDialog.setOnDismissListener(d->{simWarningDialog=null;displayedSimWarningKey=null;
    while(!queuedSimWarnings.isEmpty()){
      String next=queuedSimWarnings.removeFirst();String msg=activeSimWarnings.get(next);
      if(msg!=null&&!suppressedSimWarnings.contains(next)){h.post(()->showSimWarning(next,msg));break;}
    }
  });
  simWarningDialog.show();
  if(simWarningDialog.getWindow()!=null)simWarningDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
 }
 void showWarningList(){
  LinearLayout panel=new LinearLayout(this);panel.setOrientation(LinearLayout.VERTICAL);panel.setPadding(28,24,28,22);panel.setBackground(bg(0xff142332,18,0));
  TextView head=tv("VEHICLE STATUS  Â·  SIMULATION",22);head.setTextColor(0xff8bd4ff);panel.addView(head);
  if(activeSimWarnings.isEmpty()){
    TextView clear=tv("✓  ALL CLEAR",22);clear.setTextColor(0xff8fe7a8);clear.setPadding(0,25,0,20);panel.addView(clear);
  }else{
    for(String message:activeSimWarnings.values()){
      TextView item=tv("âš   "+message,18);item.setTextColor(0xffffbd89);item.setPadding(10,18,10,18);item.setBackground(bg(0xff243344,10,0));
      LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.topMargin=9;panel.addView(item,lp);
    }
  }
  TextView foot=tv("Suppressing popups never clears vehicle faults.",13);foot.setTextColor(0xff9ab0c1);foot.setPadding(0,18,0,0);panel.addView(foot);
  AlertDialog d=new AlertDialog.Builder(this).setView(panel).setPositiveButton("CLOSE",null).create();d.show();
  if(d.getWindow()!=null)d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
 }
 void shiftAction(boolean start){
  if(isRealFyt()){
   liveShift=start;lastShiftFix=null;if(start)liveShiftMiles=0;
   getSharedPreferences("shift_live",0).edit().putBoolean("active",liveShift).putLong("miles_bits",Double.doubleToLongBits(liveShiftMiles)).apply();
   DiagnosticLogger.event(this,"SHIFT_CHANGE","live=true active="+liveShift+" miles="+liveShiftMiles);refreshSimUi();
   Toast.makeText(this,start?"Shift started — GPS distance tracking":"Shift stopped — "+String.format(Locale.US,"%.2f",liveShiftMiles)+" mi",Toast.LENGTH_LONG).show();return;
  }
  simState.accumulate();simState.shift=start;if(start){simState.tripMiles=0;simState.shiftStartOdometer=simState.odometerMiles;}refreshSimUi();android.util.Log.i("ASURE_SIM",simState.json());DiagnosticLogger.event(this,"SHIFT_SIM","active="+start+" state="+simState.json());
 }
 void buildMedia(){LinearLayout bar=new LinearLayout(this);bar.setOrientation(LinearLayout.HORIZONTAL);bar.setGravity(Gravity.CENTER_VERTICAL);bar.setPadding(10,7,10,7);bar.setBackground(new InstrumentDrawable(20,false));body.addView(bar,new LinearLayout.LayoutParams(-1,178));
  FrameLayout artFrame=new FrameLayout(this);artFrame.setPadding(4,4,4,4);artFrame.setBackground(new InstrumentDrawable(16,false));bar.addView(artFrame,new LinearLayout.LayoutParams(132,132));art=new ImageView(this);art.setScaleType(ImageView.ScaleType.CENTER_CROP);art.setBackground(bg(Color.BLACK,14,0));art.setClipToOutline(true);art.setOutlineProvider(ViewOutlineProvider.BACKGROUND);artFrame.addView(art,new FrameLayout.LayoutParams(-1,-1));
  mediaText=new LinearLayout(this);mediaText.setOrientation(LinearLayout.VERTICAL);mediaText.setPadding(15,0,10,0);bar.addView(mediaText,new LinearLayout.LayoutParams(0,-1,1));source=tv("MUSIC",11);source.setTextColor(Color.rgb(90,190,255));title=tv("",19);title.setSingleLine(true);title.setEllipsize(android.text.TextUtils.TruncateAt.MARQUEE);title.setMarqueeRepeatLimit(-1);title.setSelected(true);detail=tv("",18);detail.setTextColor(0xffc1ccd4);detail.setSingleLine(true);detail.setEllipsize(android.text.TextUtils.TruncateAt.MARQUEE);detail.setMarqueeRepeatLimit(-1);detail.setSelected(true);com.monte.montrix.radio.RadioTextPresenter.configureMarquee(title);
  com.monte.montrix.radio.RadioTextPresenter.configureMarquee(detail);
  mediaText.addView(source,new LinearLayout.LayoutParams(-1,34));mediaText.addView(title,new LinearLayout.LayoutParams(-1,75));mediaText.addView(detail,new LinearLayout.LayoutParams(-1,35));LinearLayout times=new LinearLayout(this);pos=tv("00:00",12);dur=tv("00:00",12);times.addView(pos,new LinearLayout.LayoutParams(0,23,1));dur.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);times.addView(dur,new LinearLayout.LayoutParams(0,23,1));mediaText.addView(times);/* MONTRIX_V28_MEDIA_SEEKBAR: SeekBar supplies user intent; MediaPlayer owns timing. */
progress=new SeekBar(this);progress.setMax(1000);
progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
 /** Receives touch and programmatic changes; never seeks during timer updates. */
 public void onProgressChanged(SeekBar bar,int value,boolean fromUser){if(fromUser && player!=null && !radio && !isBtMode){try{int d=player.getDuration();pos.setText(fmt((int)((long)d*value/1000L)));}catch(Exception e){android.util.Log.w("MontrixMusic","seek preview",e);}}}
 /** Temporarily prevents the timer from overwriting the user's drag. */
 public void onStartTrackingTouch(SeekBar bar){musicUserSeeking=true;}
 /** Seeks on release, avoiding repeated decoder operations while dragging. */
 public void onStopTrackingTouch(SeekBar bar){if(player!=null && !radio && !isBtMode){try{int d=player.getDuration();if(d>0)player.seekTo((int)((long)d*bar.getProgress()/1000L));}catch(Exception e){android.util.Log.e("MontrixMusic","seek failed",e);}}musicUserSeeking=false;}
});
mediaText.addView(progress,new LinearLayout.LayoutParams(-1,38));
  LinearLayout ctl=new LinearLayout(this);ctl.setGravity(Gravity.CENTER);bar.addView(ctl,new LinearLayout.LayoutParams(292,-1));seekBackButton=round("SEEK −",v->previous(),54);seekForwardButton=round("SEEK +",v->next(),54);musicBackButton=round("◀◀",v->previous(),48);musicPlayButton=round("▶/Ⅱ",v->playPause(),48);musicNextButton=round("▶▶",v->next(),48);shuffleButton=round("SHUF",v->toggleShuffle(),45);shuffleButton.setTextSize(13);for(TextView control:new TextView[]{seekBackButton,seekForwardButton,musicBackButton,musicPlayButton,musicNextButton,shuffleButton})ctl.addView(control,new LinearLayout.LayoutParams(0,68,1));updateShuffleButton();
  LinearLayout modes=new LinearLayout(this);modes.setOrientation(LinearLayout.VERTICAL);modes.setPadding(6,0,0,0);bar.addView(modes,new LinearLayout.LayoutParams(132,-1));modes.addView(sourceButton("â—  RADIO",v->selectRadio()),new LinearLayout.LayoutParams(-1,0,1));modes.addView(sourceButton("♫  MUSIC",v->selectMusic()),new LinearLayout.LayoutParams(-1,0,1));modes.addView(sourceButton("ᛒ  BT AUDIO",v->bluetooth()),new LinearLayout.LayoutParams(-1,0,1));
  installPresetStrip();
 }
 void installPresetStrip(){
  LinearLayout strip=new LinearLayout(this);strip.setGravity(Gravity.CENTER_VERTICAL);strip.setPadding(6,2,6,2);
  body.addView(strip,new LinearLayout.LayoutParams(-1,55));
  final int[] defaults={90500,93300,94700,95500,98900,101500};
  android.content.SharedPreferences presets=getSharedPreferences("fm_presets",MODE_PRIVATE);
  for(int index=0;index<defaults.length;index++){
    final int slot=index;
    final int khz=presets.getInt("slot_"+slot,defaults[slot]);
    final TextView b=sourceButton(String.format(java.util.Locale.US,"%.1f",khz/1000f),v->{
      int saved=getSharedPreferences("fm_presets",MODE_PRIVATE).getInt("slot_"+slot,defaults[slot]);
      if(!radio)selectRadio();
      Intent command=new Intent(this,NativeRadioService.class).setAction(NativeRadioService.ACTION_TUNE).putExtra("frequency_khz",saved);
      if(Build.VERSION.SDK_INT>=26)startForegroundService(command);else startService(command);
      android.util.Log.i("ASURE_QA","PRESET_RECALL slot="+slot+" khz="+saved);
      Toast.makeText(this,"Seeking preset "+(slot+1)+"; watch frequency confirmation",Toast.LENGTH_SHORT).show();
    });
    b.setOnLongClickListener(v->{
      String frequency=getSharedPreferences("radio_cache",MODE_PRIVATE).getString("freq","");
      java.util.regex.Matcher m=java.util.regex.Pattern.compile("(\\d{2,3}\\.\\d)").matcher(frequency);
      if(!m.find()){Toast.makeText(this,"No verified current frequency; preset not saved",Toast.LENGTH_SHORT).show();return true;}
      int current=Math.round(Float.parseFloat(m.group(1))*1000);
      if(current<87500||current>108000){Toast.makeText(this,"Invalid tuner frequency",Toast.LENGTH_SHORT).show();return true;}
      getSharedPreferences("fm_presets",MODE_PRIVATE).edit().putInt("slot_"+slot,current).apply();
      b.setText(String.format(java.util.Locale.US,"%.1f",current/1000f));
      b.setContentDescription("FM preset "+(slot+1)+" saved "+b.getText());
      android.util.Log.i("ASURE_QA","PRESET_SAVED slot="+slot+" khz="+current);
      Toast.makeText(this,"Saved "+b.getText()+" to preset "+(slot+1),Toast.LENGTH_SHORT).show();return true;
    });
    b.setContentDescription("FM preset "+(slot+1)+" "+b.getText()+"; long press to store current station");
    strip.addView(b,new LinearLayout.LayoutParams(0,48,1));
  }
 }
 TextView round(String s,View.OnClickListener l,int size){TextView v=tv(s,20);v.setGravity(Gravity.CENTER);v.setBackground(new InstrumentDrawable(size/2,false));v.setOnClickListener(l);return v;}
 TextView sourceButton(String s,View.OnClickListener l){TextView v=tv(s,13);v.setGravity(Gravity.CENTER);v.setBackground(new InstrumentDrawable(13,false));v.setOnClickListener(l);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-1);lp.setMargins(0,3,0,3);v.setLayoutParams(lp);return v;}
 void tick(){updateMusicSession();if(simStatus!=null)refreshSimUi();new com.monte.montrix.view.DashboardView(clock,date).render(new com.monte.montrix.service.DashboardService().snapshot());if(!radio&&player!=null){try{int p=player.getCurrentPosition(),d=player.getDuration();pos.setText(fmt(p));dur.setText(fmt(d));if(!musicUserSeeking)progress.setProgress(d>0?(int)(1000L*p/d):0);}catch(Throwable ignored){}}h.postDelayed(this::tick,1000);}
 String fmt(int ms){int s=Math.max(0,ms/1000);return String.format(Locale.US,"%02d:%02d",s/60,s%60);}
 // PS is the short station/program-service field; RT is the long broadcast message.
 // Neither field alone proves an artist/title pair. Do not invert or label PS as a song.
 void registerRadio(){radioRx=new BroadcastReceiver(){public void onReceive(Context c,Intent i){
  if(!radio)return;
  android.content.SharedPreferences cache=getSharedPreferences("radio_cache",MODE_PRIVATE);
  String f=i.getStringExtra("freq"),p=i.getStringExtra("ps"),r=i.getStringExtra("rt");
  String tuningMessage=i.getStringExtra("tune_status");
  if(f==null||f.trim().isEmpty())f=cache.getString("freq","Frequency unavailable");
  if(p==null||p.trim().isEmpty())p=cache.getString("ps","");
  if(r==null||r.trim().isEmpty())r=cache.getString("rt","");
  f=f.trim();p=p.trim();r=r.trim();
  source.setText("RADIO");
  // Preferred presentation: frequency + RadioText on first line, PS underneath.
  // PS may rotate through fragments; it must never be presented as verified song title.
  com.monte.montrix.radio.RadioTextPresenter.show(source,title,detail,f,r,p);
  android.util.Log.i("ASURE_QA","RDS_DISPLAY freq="+f+" rt="+r+" ps="+p+" tune_status="+tuningMessage+" classification=unverified");
  pos.setText("");dur.setText("");progress.setProgress(0);
 }};IntentFilter f=new IntentFilter(NativeRadioService.ACTION_STATE);
 if(Build.VERSION.SDK_INT>=33)registerReceiver(radioRx,f,Context.RECEIVER_NOT_EXPORTED);else registerReceiver(radioRx,f);
 }
 void svc(String a){Intent i=new Intent(this,NativeRadioService.class).setAction(a);if(Build.VERSION.SDK_INT>=26)startForegroundService(i);else startService(i);}
 void restoreAudioMode(){
  boolean saved=getSharedPreferences("audio_mode",0).getBoolean("radio",false);
  if(saved){radio=true;isBtMode=false;updateShuffleButton();source.setText("RADIO");title.setText("FM radio");detail.setText("Returning to live tuner status…");art.setImageResource(R.drawable.radio_art);pos.setText("");dur.setText("");progress.setProgress(0);}
  else {radio=false;isBtMode=false;updateShuffleButton();source.setText("MUSIC");title.setText("Music ready");detail.setText("Tap MUSIC to resume playback");art.setImageResource(R.drawable.music_art);DiagnosticLogger.event(this,"MUSIC_RESTORE","deferred playback until user action");}
 }
 void selectRadio(){if(musicSession!=null)musicSession.setActive(false);isBtMode=false;radio=true;updateShuffleButton();DiagnosticLogger.event(this,"MODE_SELECT","radio");DiagnosticLogger.snapshot(this,"ui_radio_selected");getSharedPreferences("audio_mode",0).edit().putBoolean("radio",true).apply();radio=true;if(player!=null&&player.isPlaying())player.pause();if(audio!=null)audio.abandonAudioFocus(focusListener);svc(NativeRadioService.ACTION_RADIO);source.setText("RADIO");title.setText("FM radio");detail.setText("Connecting to FYT tuner / RDS…");art.setImageResource(R.drawable.radio_art);pos.setText("");dur.setText("");progress.setProgress(0);}
 void scanMusic(){songs.clear();String[] pr={MediaStore.Audio.Media._ID,MediaStore.Audio.Media.ALBUM_ID,MediaStore.Audio.Media.TITLE,MediaStore.Audio.Media.ARTIST,MediaStore.Audio.Media.DATA};try(Cursor c=getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,pr,MediaStore.Audio.Media.IS_MUSIC+"!=0",null,MediaStore.Audio.Media.TITLE+" COLLATE NOCASE")){if(c!=null)while(c.moveToNext())songs.add(new Song(c.getLong(0),c.getLong(1),c.getString(2),c.getString(3),c.getString(4)));}catch(Throwable error){android.util.Log.e("MontrixMusic","MediaStore scan failed",error);}}
 void selectMusic(){if(musicSession!=null)musicSession.setActive(true);com.monte.montrix.radio.RadioTextPresenter.restoreMusic(source,title,detail);isBtMode=false;radio=false;updateShuffleButton();DiagnosticLogger.event(this,"MODE_SELECT","music");DiagnosticLogger.snapshot(this,"ui_music_selected");getSharedPreferences("audio_mode",0).edit().putBoolean("radio",false).apply();radio=false;svc(NativeRadioService.ACTION_MUSIC);if(songs.isEmpty())scanMusic();if((song<0||song>=songs.size())&&songs.size()>0)song=0;if(song>=0){if(player==null || !player.isPlaying()){if(player==null)playSong();else {try{requestMusicFocus();player.start();}catch(Throwable e){playSong();}}}showSong();}else{source.setText("MUSIC");title.setText("No music indexed");detail.setText("Copy MP3s to Music and rescan media");art.setImageResource(R.drawable.music_art);}}
 void showSong(){com.monte.montrix.radio.RadioTextPresenter.restoreMusic(source,title,detail);if(song<0||song>=songs.size())return;Song s=songs.get(song);source.setText("MUSIC  •  "+(song+1)+" / "+songs.size());title.setText(s.title==null?"Unknown title":s.title);detail.setText(s.artist==null?"Unknown artist":s.artist);Bitmap b=albumArt(s.albumId);if(b!=null)art.setImageBitmap(b);else art.setImageResource(R.drawable.music_art);}
 Bitmap albumArt(long album){try{Uri u=Uri.parse("content://media/external/audio/albumart/"+album);return MediaStore.Images.Media.getBitmap(getContentResolver(),u);}catch(Throwable e){return null;}}
 void requestMusicFocus(){if(radio||isBtMode){DiagnosticLogger.event(this,"MUSIC_FOCUS_SKIPPED","radio="+radio+" bt="+isBtMode);return;}if(audio!=null){int result=audio.requestAudioFocus(focusListener,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);DiagnosticLogger.event(this,"MUSIC_FOCUS_REQUEST","result="+result+" state="+playerState());}}
 String playerState(){try{return "radio="+radio+" index="+song+" exists="+(player!=null)+" playing="+(player!=null&&player.isPlaying())+" positionMs="+(player==null?-1:player.getCurrentPosition())+" durationMs="+(player==null?-1:player.getDuration());}catch(Throwable t){return "playerError="+t.getClass().getSimpleName();}} void playSong(){if(song<0||song>=songs.size())return;try{requestMusicFocus();getSharedPreferences("music_resume",0).edit().putInt("index",song).apply();if(player!=null){player.release();player=null;}player=new MediaPlayer();player.setAudioStreamType(AudioManager.STREAM_MUSIC);player.setDataSource(songs.get(song).path);player.setOnCompletionListener(m->next());player.setOnErrorListener((m,what,extra)->{android.util.Log.e("CWG_MUSIC","MediaPlayer error "+what+"/"+extra);h.postDelayed(this::next,250);return true;});player.prepare();player.start();android.util.Log.i("CWG_MUSIC","started index="+song+" file="+songs.get(song).path);DiagnosticLogger.event(this,"MUSIC_STARTED","index="+song+" duration="+player.getDuration()+" playing="+player.isPlaying()+" position="+player.getCurrentPosition());showSong();}catch(Throwable e){android.util.Log.e("CWG_MUSIC","play failed",e);detail.setText("Cannot play this track: "+e.getClass().getSimpleName());}}
 void previous(){DiagnosticLogger.event(this,"PREVIOUS","radio="+radio+" bt="+isBtMode);if(radio){svc(NativeRadioService.ACTION_RADIO_PREV);return;}if(songs.isEmpty())return;if(shuffle && historyCursor>0){historyCursor--;int index=indexOfSongId(playHistory.get(historyCursor));if(index>=0){song=index;saveHistory();playSong();return;}}song=(song-1+songs.size())%songs.size();if(shuffle)recordHistory();playSong();}
 void next(){DiagnosticLogger.event(this,"NEXT","radio="+radio+" bt="+isBtMode);if(radio){svc(NativeRadioService.ACTION_RADIO_NEXT);return;}if(songs.isEmpty())return;if(shuffle){if(historyCursor>=0 && historyCursor<playHistory.size()-1){historyCursor++;int index=indexOfSongId(playHistory.get(historyCursor));if(index>=0){song=index;saveHistory();playSong();return;}}song=randomNext();recordHistory();}else song=(song+1)%songs.size();playSong();}
 void toggleShuffle(){if(radio||isBtMode)return;shuffle=!shuffle;DiagnosticLogger.event(this,"SHUFFLE","enabled="+shuffle);getSharedPreferences("music_resume",0).edit().putBoolean("shuffle",shuffle).apply();if(shuffle&&song>=0&&song<songs.size())recordHistory();updateShuffleButton();}
 void updateShuffleButton(){
  boolean showRadio=radio&&!isBtMode,showMusic=!radio&&!isBtMode;
  if(seekBackButton!=null)seekBackButton.setVisibility(showRadio?View.VISIBLE:View.GONE);
  if(seekForwardButton!=null)seekForwardButton.setVisibility(showRadio?View.VISIBLE:View.GONE);
  if(musicBackButton!=null)musicBackButton.setVisibility(showMusic?View.VISIBLE:View.GONE);
  if(musicPlayButton!=null)musicPlayButton.setVisibility(showMusic?View.VISIBLE:View.GONE);
  if(musicNextButton!=null)musicNextButton.setVisibility(showMusic?View.VISIBLE:View.GONE);
  if(shuffleButton!=null){shuffleButton.setVisibility(showMusic?View.VISIBLE:View.GONE);shuffleButton.setText(shuffle?"SHUF ON":"SHUFFLE");shuffleButton.setTextColor(shuffle?0xff65d9ff:Color.WHITE);}
  if(source!=null)DiagnosticLogger.event(this,"UI_MODE","radio="+radio+" bt="+isBtMode+" shufflePreference="+shuffle+" shuffleVisible="+(shuffleButton!=null&&shuffleButton.getVisibility()==View.VISIBLE)+" seekVisible="+(seekBackButton!=null&&seekBackButton.getVisibility()==View.VISIBLE)+" musicControlsVisible="+(musicBackButton!=null&&musicBackButton.getVisibility()==View.VISIBLE));
 }

 int indexOfSongId(long id){for(int i=0;i<songs.size();i++)if(songs.get(i).id==id)return i;return -1;}
 int randomNext(){int n=songs.size();if(n<=1)return 0;int candidate;int attempts=0;do{candidate=random.nextInt(n);attempts++;}while(candidate==song&&attempts<12);if(candidate==song)candidate=(song+1)%n;return candidate;}
 void recordHistory(){if(song<0||song>=songs.size())return;while(playHistory.size()>historyCursor+1)playHistory.remove(playHistory.size()-1);long id=songs.get(song).id;playHistory.add(id);while(playHistory.size()>100)playHistory.remove(0);historyCursor=playHistory.size()-1;saveHistory();}
 void saveHistory(){StringBuilder sb=new StringBuilder();for(long id:playHistory){if(sb.length()>0)sb.append(',');sb.append(id);}getSharedPreferences("music_resume",0).edit().putString("history",sb.toString()).putInt("history_cursor",historyCursor).apply();}
 void loadHistory(){playHistory.clear();String str=getSharedPreferences("music_resume",0).getString("history","");for(String part:str.split(",")){try{playHistory.add(Long.parseLong(part.trim()));}catch(Exception ignored){}}while(playHistory.size()>100)playHistory.remove(0);historyCursor=Math.min(playHistory.size()-1,Math.max(-1,getSharedPreferences("music_resume",0).getInt("history_cursor",playHistory.size()-1)));}
 void playPause(){if(radio||isBtMode)return;DiagnosticLogger.event(this,"MUSIC_TOGGLE","playing="+(player!=null&&player.isPlaying()));if(player==null){playSong();return;}if(player.isPlaying())player.pause();else {requestMusicFocus();player.start();}showSong();}
 /** Routes standard Android media transport events to the active Montrix source. */
void setupMusicMediaSession(){
 musicSession=new android.media.session.MediaSession(this,"Montrix Music");
 musicSession.setFlags(android.media.session.MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | android.media.session.MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
 musicSession.setCallback(new android.media.session.MediaSession.Callback(){
  /** Selects the next track. */
  @Override public void onSkipToNext(){if(!radio && !isBtMode)next();}
  /** Selects the previous track. */
  @Override public void onSkipToPrevious(){if(!radio && !isBtMode)previous();}
  /** Toggles playback. */
  @Override public void onPlay(){if(!radio && !isBtMode && (player==null || !player.isPlaying()))playPause();}
  /** Pauses playback. */
  @Override public void onPause(){if(!radio && !isBtMode && player!=null && player.isPlaying())playPause();}
  /** Moves to a requested position in milliseconds. */
  @Override public void onSeekTo(long position){if(!radio && !isBtMode && player!=null)player.seekTo((int)Math.max(0,Math.min(position,player.getDuration())));}
 });
 musicSession.setPlaybackState(new android.media.session.PlaybackState.Builder()
  .setActions(android.media.session.PlaybackState.ACTION_PLAY | android.media.session.PlaybackState.ACTION_PAUSE |
   android.media.session.PlaybackState.ACTION_SKIP_TO_NEXT | android.media.session.PlaybackState.ACTION_SKIP_TO_PREVIOUS |
   android.media.session.PlaybackState.ACTION_SEEK_TO).setState(android.media.session.PlaybackState.STATE_PAUSED,0,1).build());
}
/** Makes only the selected music source eligible for Android media keys. */
void updateMusicSession(){if(musicSession!=null){boolean active=!radio&&!isBtMode;musicSession.setActive(active);if(active){long p=0;int state=android.media.session.PlaybackState.STATE_PAUSED;try{if(player!=null){p=player.getCurrentPosition();if(player.isPlaying())state=android.media.session.PlaybackState.STATE_PLAYING;}}catch(Exception ignored){}musicSession.setPlaybackState(new android.media.session.PlaybackState.Builder().setActions(2|4|16|32|256).setState(state,p,1).build());}}}
 void key(int k){audio.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,k));audio.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,k));}
 void guestBluetooth(){
  new AlertDialog.Builder(this).setTitle("Guest Bluetooth music")
   .setMessage("Open the A-SURE Bluetooth pairing screen. Have the rider connect to the car, not your personal phone. After the ride, disconnect the guest phone and reconnect your phone in the A-SURE Bluetooth app. Automatic owner reconnection depends on FYT firmware and is not yet verified.")
   .setPositiveButton("Open pairing",(dialog,which)->{guestMode=true;isBtMode=true;radio=false;updateShuffleButton();if(player!=null&&player.isPlaying())player.pause();
     try{Intent x=new Intent();x.setClassName("com.syu.bt","com.syu.bt.BtAct");startActivity(x);}catch(Throwable e){launch("com.syu.bt");}
   }).setNeutralButton("Return to my phone",(dialog,which)->{guestMode=false;try{Intent x=new Intent();x.setClassName("com.syu.bt","com.syu.bt.BtAct");startActivity(x);}catch(Throwable e){launch("com.syu.bt");}})
   .setNegativeButton("Cancel",null).show();
 }
 void bluetooth(){if(musicSession!=null)musicSession.setActive(false);isBtMode=true;radio=false;updateShuffleButton();if(player!=null&&player.isPlaying())player.pause();
  source.setText("BLUETOOTH");title.setText("Bluetooth Audio");detail.setText("A-SURE BT AV source");art.setImageResource(R.drawable.music_art);pos.setText("");dur.setText("");progress.setProgress(0);
  if(isRealFyt()){try{Intent x=new Intent();x.setClassName("com.syu.bt","com.syu.bt.act.ActBtAvStart");startActivity(x);return;}catch(Throwable ignored){}try{Intent x=getPackageManager().getLaunchIntentForPackage("com.syu.bt");if(x!=null){startActivity(x);return;}}catch(Throwable ignored){}}
  detail.setText("Bluetooth simulator • real car uses FYT BT AV");
 }
 boolean isRealFyt(){String h=Build.HARDWARE==null?"":Build.HARDWARE.toLowerCase();String d=Build.DEVICE==null?"":Build.DEVICE.toLowerCase();return h.contains("uis")||d.contains("uis8581")||d.contains("fyt");}
 void bringDashboardFront(){try{Intent x=new Intent(this,DashboardActivity.class);x.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT|Intent.FLAG_ACTIVITY_SINGLE_TOP);startActivity(x);}catch(Throwable ignored){}}
 void openMaps(){try{Intent x=getPackageManager().getLaunchIntentForPackage("com.google.android.apps.maps");if(x!=null){x.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);startActivity(x);return;}}catch(Throwable ignored){}try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("geo:0,0?q=")));}catch(Throwable ignored){Toast.makeText(this,"Google Maps is not installed",Toast.LENGTH_SHORT).show();}}
 void launch(String p){try{Intent x=getPackageManager().getLaunchIntentForPackage(p);if(x!=null)startActivity(x);}catch(Throwable ignored){}}
 /** Opens the independently modeled application launcher. */
 void showApps(){com.monte.montrix.service.apps.AppListService apps = new com.monte.montrix.service.apps.AppListService(this); new com.monte.montrix.view.apps.AppListView(this, apps).show(apps.load());}
 void vehicleDiagnostics(){
  if(!isRealFyt())simState.accumulate();
  new AlertDialog.Builder(this).setTitle("Vehicle Diagnostics — "+(isRealFyt()?"Live (unverified)":"SIMULATION"))
   .setMessage(isRealFyt()?"CAN/OBD-II signals have not been validated for this Ford profile. No vehicle commands are issued.":simState.json()+"\n\nThese are injected emulator values, not actual Ford CAN messages.")
   .setPositiveButton("OK",null).show();
 }
 // Status bar: top-edge downward swipe reveals notifications, upward swipe dismisses.
 // Avoid IMMERSIVE_STICKY: on this FYT firmware it can trap or pin the status bar.
 private boolean statusBarShown=false;
 private float edgeStartY=-1f, edgeStartX=-1f;
 private final Runnable autoHideBars=new Runnable(){@Override public void run(){
   if(statusBarShown){if(hasWindowFocus()) hideSystemBars(); else h.postDelayed(this,1500);}
 }};
 private void hideSystemBars(){
   statusBarShown=false;
   h.removeCallbacks(autoHideBars);
   getWindow().getDecorView().setSystemUiVisibility(
     View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
     View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
     View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
 }
 private void showSystemBar(){
   statusBarShown=true;
   h.removeCallbacks(autoHideBars);
   getWindow().getDecorView().setSystemUiVisibility(
     View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
     View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
     View.SYSTEM_UI_FLAG_IMMERSIVE);
   h.postDelayed(autoHideBars,8000);
 }
 @Override public boolean dispatchTouchEvent(android.view.MotionEvent event){
   final int action=event.getActionMasked();
   if(action==android.view.MotionEvent.ACTION_DOWN){
     edgeStartY=event.getY();edgeStartX=event.getX();
   } else if(action==android.view.MotionEvent.ACTION_UP && edgeStartY>=0){
     float dy=event.getY()-edgeStartY;
     float dx=Math.abs(event.getX()-edgeStartX);
     if(dx<130 && edgeStartY<72 && dy>65){
       showSystemBar();edgeStartY=-1;return true;
     }
     if(dx<130 && statusBarShown && dy< -65){
       hideSystemBars();edgeStartY=-1;return true;
     }
     edgeStartY=-1;
   } else if(action==android.view.MotionEvent.ACTION_CANCEL){edgeStartY=-1;}
   return super.dispatchTouchEvent(event);
 }
 @Override protected void onResume(){super.onResume();hideSystemBars();DiagnosticLogger.snapshot(this,"dashboard_resume");DiagnosticLogger.event(this,"MUSIC_LIFECYCLE","resume "+playerState());DiagnosticLogger.event(this,"DASHBOARD_RESUME","radio="+radio+" shift="+liveShift);}
 @Override protected void onPause(){DiagnosticLogger.snapshot(this,"dashboard_pause");DiagnosticLogger.event(this,"MUSIC_LIFECYCLE","pause "+playerState());DiagnosticLogger.event(this,"DASHBOARD_PAUSE","radio="+radio+" shift="+liveShift);super.onPause();}
 @Override protected void onStop(){DiagnosticLogger.snapshot(this,"dashboard_stop");DiagnosticLogger.event(this,"MUSIC_LIFECYCLE","stop "+playerState());super.onStop();}

 @Override public void onBackPressed(){moveTaskToBack(true);}
 @Override protected void onDestroy(){if(musicSession!=null){musicSession.release();musicSession=null;}DiagnosticLogger.snapshot(this,"dashboard_destroy");DiagnosticLogger.event(this,"MUSIC_LIFECYCLE","destroy "+playerState());try{if(locationManager!=null)locationManager.removeUpdates(this);}catch(Throwable ignored){}if(fytHost!=null)fytHost.release();if(offlineMap!=null)offlineMap.destroy();if(tempProvider!=null)tempProvider.stop();if(currentSpeedService!=null)currentSpeedService.stop();if(canBusService!=null)canBusService.stop();h.removeCallbacksAndMessages(null);try{if(radioRx!=null)unregisterReceiver(radioRx);}catch(Throwable ignored){}try{if(simReceiver!=null)unregisterReceiver(simReceiver);}catch(Throwable ignored){}if(player!=null)player.release();speedLimitWorker.shutdownNow();if(offlineSpeedLimits!=null)offlineSpeedLimits.close();if(tripRecorder!=null)tripRecorder.close();if(audio!=null)audio.abandonAudioFocus(focusListener);super.onDestroy();}
}
