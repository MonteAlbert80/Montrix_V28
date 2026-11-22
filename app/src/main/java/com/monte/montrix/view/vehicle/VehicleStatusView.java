package com.monte.montrix.view.vehicle;
import android.widget.TextView;
import com.monte.montrix.model.vehicle.VehicleStatusModel;
/** Renders the VehicleStatus component without business logic or hardware access. */
public final class VehicleStatusView {
    /** Widget owned by the dashboard composition. */
    private final TextView label;
    /** Attaches a component view to its widget.
     * @param label existing dashboard label
     */
    public VehicleStatusView(TextView label) { this.label = label; }
    /** Applies a service-prepared snapshot to the widget.
     * @param state immutable state snapshot
     */
    public void render(VehicleStatusModel state) { label.setText(state.statusText); }
}
