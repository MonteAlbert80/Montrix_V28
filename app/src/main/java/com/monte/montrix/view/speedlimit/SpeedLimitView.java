package com.monte.montrix.view.speedlimit;
import android.widget.TextView;
import com.monte.montrix.model.speedlimit.SpeedLimitModel;
/** Renders the SpeedLimit component without business logic or hardware access. */
public final class SpeedLimitView {
    /** Widget owned by the dashboard composition. */
    private final TextView label;
    /** Attaches a component view to its widget.
     * @param label existing dashboard label
     */
    public SpeedLimitView(TextView label) { this.label = label; }
    /** Applies a service-prepared snapshot to the widget.
     * @param state immutable state snapshot
     */
    public void render(SpeedLimitModel state) { label.setText(state.limitText); }
}
