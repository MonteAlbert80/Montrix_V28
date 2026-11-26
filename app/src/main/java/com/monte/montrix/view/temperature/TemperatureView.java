package com.monte.montrix.view.temperature;
import android.content.Context;
import android.graphics.Color;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.monte.montrix.model.temperature.TemperatureModel;
import java.util.Locale;
/** Presentation only. Rounds to one decimal at the display boundary. */
public final class TemperatureView extends LinearLayout {
    private final TemperatureModel model;
    private final TextView widget;
    public TemperatureView(Context context, TemperatureModel state) {
        super(context); model=state; setOrientation(VERTICAL); setPadding(12,8,12,8);
        setBackgroundColor(Color.rgb(12,23,34));
        TextView heading=new TextView(context); heading.setText("Temperature"); heading.setTextColor(Color.rgb(110,195,255)); heading.setTextSize(13); addView(heading);
        widget=new TextView(context); widget.setTextSize(20); addView(widget);
        model.addListener(this::render); render();
    }
    public TemperatureView(TextView widget) { super(widget.getContext()); this.widget=widget; this.model=null; }
    public void render(TemperatureModel state) {
        if(model!=null && model!=state) { model.update(state); return; }
        widget.setText(state.available ? String.format(Locale.US,"%.1f\u00b0F",state.degreesF) : "--\u00b0F");
        widget.setTextColor(state.available ? Color.WHITE : 0xff9eb2c2);
        widget.setContentDescription(state.available ? String.format(Locale.US,"Outside temperature %.1f degrees Fahrenheit",state.degreesF) : "Outside temperature unavailable");
    }
    public void render() { if(model!=null) render(model); }
}
