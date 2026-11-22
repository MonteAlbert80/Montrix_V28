package com.monte.montrix.view.map;
import android.widget.TextView;
import com.monte.montrix.model.map.MapModel;
/** Renders the Map component without business logic or hardware access. */
public final class MapView {
    /** Widget owned by the dashboard composition. */
    private final TextView label;
    /** Attaches a component view to its widget.
     * @param label existing dashboard label
     */
    public MapView(TextView label) { this.label = label; }
    /** Applies a service-prepared snapshot to the widget.
     * @param state immutable state snapshot
     */
    public void render(MapModel state) { label.setText(state.statusText); }
}
