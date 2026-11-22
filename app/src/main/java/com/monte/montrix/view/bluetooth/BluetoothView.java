package com.monte.montrix.view.bluetooth;
import android.widget.TextView;
import com.monte.montrix.model.bluetooth.BluetoothModel;
/** Renders the Bluetooth component without business logic or hardware access. */
public final class BluetoothView {
    /** Widget owned by the dashboard composition. */
    private final TextView label;
    /** Attaches a component view to its widget.
     * @param label existing dashboard label
     */
    public BluetoothView(TextView label) { this.label = label; }
    /** Applies a service-prepared snapshot to the widget.
     * @param state immutable state snapshot
     */
    public void render(BluetoothModel state) { label.setText(state.connectionText); }
}
