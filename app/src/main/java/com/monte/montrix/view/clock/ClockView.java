package com.monte.montrix.view.clock;
import android.widget.TextView;
import com.monte.montrix.model.clock.ClockModel;
/** Renders the Clock component without business logic or hardware access. */
public final class ClockView {
    /** Widget owned by the dashboard composition. */
    private final TextView label;
    /** Attaches a component view to its widget.
     * @param label existing dashboard label
     */
    public ClockView(TextView label) { this.label = label; }
    /** Applies a service-prepared snapshot to the widget.
     * @param state immutable state snapshot
     */
    public void render(ClockModel state) { label.setText(state.clockText); }
}
