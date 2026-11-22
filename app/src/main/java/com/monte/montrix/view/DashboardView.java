package com.monte.montrix.view;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import com.monte.montrix.model.DashboardModel;
import com.monte.montrix.model.MediaSelectionModel;
import com.monte.montrix.view.music.MusicView;
import com.monte.montrix.view.radio.RadioView;
import com.monte.montrix.view.bluetooth.BluetoothView;
/** Dashboard layout: one shared media slot and right-side source selectors, matching legacy layout. */
public final class DashboardView extends ScrollView {
 private final LinearLayout content;
 /** Original dashboard TextView targets; null in preview mode. */
 private final TextView legacyClock,legacyDate;
 private final FrameLayout mediaSlot;
 private final Button radioButton,musicButton,bluetoothButton;
 private final MediaSelectionModel selection;
 private final View radioView,musicView,bluetoothView;
 public DashboardView(Context context,DashboardModel model){
  super(context);legacyClock=null;legacyDate=null;selection=model.media;setFillViewport(true);
  content=new LinearLayout(context);content.setOrientation(LinearLayout.VERTICAL);
  content.setPadding(8,8,8,8);content.setBackgroundColor(Color.rgb(5,12,20));addView(content);
  TextView brand=new TextView(context);brand.setText("MONTRIX V28");brand.setTextSize(21);brand.setTextColor(Color.WHITE);content.addView(brand);
  LinearLayout mediaBar=new LinearLayout(context);mediaBar.setOrientation(LinearLayout.HORIZONTAL);
  mediaBar.setGravity(Gravity.CENTER_VERTICAL);mediaBar.setBackgroundColor(Color.rgb(12,23,34));
  LinearLayout.LayoutParams barParams=new LinearLayout.LayoutParams(-1,196);barParams.bottomMargin=8;content.addView(mediaBar,barParams);
  mediaSlot=new FrameLayout(context);mediaBar.addView(mediaSlot,new LinearLayout.LayoutParams(0,-1,1));
  musicView=new MusicView(context,model.music);radioView=new RadioView(context,model.radio);
  bluetoothView=new BluetoothView(context,model.bluetooth);
  mediaSlot.addView(musicView,new FrameLayout.LayoutParams(-1,-1));
  mediaSlot.addView(radioView,new FrameLayout.LayoutParams(-1,-1));
  mediaSlot.addView(bluetoothView,new FrameLayout.LayoutParams(-1,-1));
  LinearLayout selectors=new LinearLayout(context);selectors.setOrientation(LinearLayout.VERTICAL);
  selectors.setPadding(4,4,4,4);mediaBar.addView(selectors,new LinearLayout.LayoutParams(140,-1));
  radioButton=sourceButton(context,"RADIO",MediaSelectionModel.Source.RADIO);
  musicButton=sourceButton(context,"MUSIC",MediaSelectionModel.Source.MUSIC);
  bluetoothButton=sourceButton(context,"BT AUDIO",MediaSelectionModel.Source.BLUETOOTH);
  selectors.addView(radioButton,new LinearLayout.LayoutParams(-1,0,1));
  selectors.addView(musicButton,new LinearLayout.LayoutParams(-1,0,1));
  selectors.addView(bluetoothButton,new LinearLayout.LayoutParams(-1,0,1));
  addTile(new com.monte.montrix.view.speed.CurrentSpeedView(context,model.speed));
  addTile(new com.monte.montrix.view.speedlimit.SpeedLimitView(context,model.speedlimit));
  addTile(new com.monte.montrix.view.temperature.TemperatureView(context,model.temperature));
  addTile(new com.monte.montrix.view.map.MapView(context,model.map));
  addTile(new com.monte.montrix.view.shift.ShiftView(context,model.shift));
  addTile(new com.monte.montrix.view.vehicle.VehicleStatusView(context,model.vehicle));
  addTile(new com.monte.montrix.view.clock.ClockView(context,model.clock));
  addTile(new com.monte.montrix.view.album.PictureAlbumView(context,model.album));
  selection.addListener(this::renderSelection);renderSelection();
 }
 /** Retains the original dashboard API without replacing its layout. */
 public DashboardView(TextView clock,TextView date){
  super(clock.getContext());legacyClock=clock;legacyDate=date;
  content=null;mediaSlot=null;radioButton=null;musicButton=null;bluetoothButton=null;
  selection=null;radioView=null;musicView=null;bluetoothView=null;
 }
 /** Updates original clock/date labels from a service snapshot. */
 public void render(DashboardModel snapshot){
  if(legacyClock!=null){legacyClock.setText(snapshot.clockText);legacyDate.setText(snapshot.dateText);}
 }
 private Button sourceButton(Context c,String label,MediaSelectionModel.Source source){
  Button button=new Button(c);button.setText(label);button.setTextSize(13);
  button.setAllCaps(false);button.setOnClickListener(v->selection.requestSelect(source));return button;
 }
 private void renderSelection(){
  MediaSelectionModel.Source source=selection.selected();
  radioView.setVisibility(source==MediaSelectionModel.Source.RADIO?View.VISIBLE:View.GONE);
  musicView.setVisibility(source==MediaSelectionModel.Source.MUSIC?View.VISIBLE:View.GONE);
  bluetoothView.setVisibility(source==MediaSelectionModel.Source.BLUETOOTH?View.VISIBLE:View.GONE);
  radioButton.setAlpha(source==MediaSelectionModel.Source.RADIO?1f:.65f);
  musicButton.setAlpha(source==MediaSelectionModel.Source.MUSIC?1f:.65f);
  bluetoothButton.setAlpha(source==MediaSelectionModel.Source.BLUETOOTH?1f:.65f);
  radioButton.setSelected(source==MediaSelectionModel.Source.RADIO);
  musicButton.setSelected(source==MediaSelectionModel.Source.MUSIC);
  bluetoothButton.setSelected(source==MediaSelectionModel.Source.BLUETOOTH);
 }
 private void addTile(View view){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=8;content.addView(view,p);}
}
