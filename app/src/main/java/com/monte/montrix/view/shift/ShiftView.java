package com.monte.montrix.view.shift;
import android.widget.TextView;
import com.monte.montrix.model.shift.ShiftModel;
/** Renders the Shift component without business logic or hardware access. */
public final class ShiftView {
    /** Widget owned by the dashboard composition. */
    private final TextView label;
    /** Attaches a component view to its widget.
     * @param label existing dashboard label
     */
    public ShiftView(TextView label) { this.label = label; }
    /** Applies a service-prepared snapshot to the widget.
     * @param state immutable state snapshot
     */
    public void render(ShiftModel state) { label.setText(state.statusText); }
}
