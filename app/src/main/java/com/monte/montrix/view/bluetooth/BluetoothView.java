package com.monte.montrix.view.bluetooth;
import android.content.Context;
import android.graphics.Color;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.monte.montrix.model.bluetooth.BluetoothModel;
/** Visual-only Bluetooth component. No services, hardware or business calculations. */
public final class BluetoothView extends LinearLayout {
 /** Bound presentation state. */ private final BluetoothModel model;
 /** Primary label. */ private final TextView primary;
 /** Secondary label. */ private final TextView secondary;
 /** Constructs the component. @param context Android context @param state observable state */
 public BluetoothView(Context context,BluetoothModel state){super(context);model=state;setOrientation(VERTICAL);setPadding(12,8,12,8);setBackgroundColor(Color.rgb(12,23,34));TextView heading=new TextView(context);heading.setText("Bluetooth");heading.setTextColor(Color.rgb(110,195,255));heading.setTextSize(13);addView(heading);primary=new TextView(context);primary.setTextColor(Color.WHITE);primary.setTextSize(20);addView(primary);secondary=new TextView(context);secondary.setTextColor(Color.LTGRAY);secondary.setTextSize(13);addView(secondary);model.addListener(this::render);render();}
 /** Renders state without transforming it. */ public void render(){primary.setText(model.primary);secondary.setText(model.secondary);}
}
