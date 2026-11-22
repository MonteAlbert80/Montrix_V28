package com.monte.montrix.view.speedlimit;
import android.content.Context;
import android.graphics.Color;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.monte.montrix.model.speedlimit.SpeedLimitModel;
/** Visual-only SpeedLimit component. No services, hardware or business calculations. */
public final class SpeedLimitView extends LinearLayout {
 /** Bound presentation state. */ private final SpeedLimitModel model;
 /** Existing dashboard widget, when running legacy mode. */ private final TextView widget;
 /** Primary label. */ private final TextView primary;
 /** Secondary label. */ private final TextView secondary;
 /** Constructs the component. @param context Android context @param state observable state */
 public SpeedLimitView(Context context,SpeedLimitModel state){super(context);model=state;widget=null;setOrientation(VERTICAL);setPadding(12,8,12,8);setBackgroundColor(Color.rgb(12,23,34));TextView heading=new TextView(context);heading.setText("SpeedLimit");heading.setTextColor(Color.rgb(110,195,255));heading.setTextSize(13);addView(heading);primary=new TextView(context);primary.setTextColor(Color.WHITE);primary.setTextSize(20);addView(primary);secondary=new TextView(context);secondary.setTextColor(Color.LTGRAY);secondary.setTextSize(13);addView(secondary);model.addListener(this::render);render();}

 /** Binds to an existing legacy dashboard widget. */ public SpeedLimitView(TextView widget){super(widget.getContext());this.model=null;this.widget=widget;this.primary=null;this.secondary=null;}
 /** Applies an already formatted legacy service snapshot. */ public void render(SpeedLimitModel state){if(widget!=null){widget.setText(state.limitText);}else if(model!=null){model.update(state.primary,state.secondary);}}
 /** Renders state without transforming it. */ public void render(){primary.setText(model.primary);secondary.setText(model.secondary);}
}
