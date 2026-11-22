package com.monte.montrix.view.temperature;
import android.widget.TextView;
import com.monte.montrix.model.temperature.TemperatureModel;
/** Renders the Temperature component without business logic or hardware access. */
public final class TemperatureView {
    /** Widget owned by the dashboard composition. */
    private final TextView label;
    /** Attaches a component view to its widget.
     * @param label existing dashboard label
     */
    public TemperatureView(TextView label) { this.label = label; }
    /** Applies a service-prepared snapshot to the widget.
     * @param state immutable state snapshot
     */
    public void render(TemperatureModel state) { label.setText(state.temperatureText); }
}
