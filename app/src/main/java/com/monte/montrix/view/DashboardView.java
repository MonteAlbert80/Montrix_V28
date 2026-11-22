package com.monte.montrix.view;
import android.widget.TextView;
import com.monte.montrix.model.DashboardModel;
/** Dashboard-level visual binding for composed child components. */
public final class DashboardView {
 /** Clock widget. */ private final TextView clock;
 /** Date widget. */ private final TextView date;
 /** Creates the dashboard binding. @param clock clock widget @param date date widget */
 public DashboardView(TextView clock,TextView date){this.clock=clock;this.date=date;}
 /** Renders service-supplied dashboard state. @param model ready-to-display snapshot */
 public void render(DashboardModel model){clock.setText(model.clockText);date.setText(model.dateText);}
}
