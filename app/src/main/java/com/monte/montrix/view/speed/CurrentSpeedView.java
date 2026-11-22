package com.monte.montrix.view.speed;
import android.widget.TextView;
import com.monte.montrix.model.speed.CurrentSpeedModel;
/** View adapter; displays state without calculating values. */
public final class CurrentSpeedView {
 /** Existing dashboard text widget. */ private final TextView widget;
 /** Creates the view adapter. @param widget speed display widget */
 public CurrentSpeedView(TextView widget){this.widget=widget;}
 /** Renders prepared state. @param model immutable presentation state */
 public void render(CurrentSpeedModel model){widget.setText(model.text);widget.setTextColor(model.color);widget.setContentDescription(model.description);}
}
