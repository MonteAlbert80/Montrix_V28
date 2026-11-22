package com.monte.montrix.view.radio;
import android.widget.TextView;
import com.monte.montrix.model.radio.RadioModel;
/** Renders the Radio component without business logic or hardware access. */
public final class RadioView {
    /** Widget owned by the dashboard composition. */
    private final TextView label;
    /** Attaches a component view to its widget.
     * @param label existing dashboard label
     */
    public RadioView(TextView label) { this.label = label; }
    /** Applies a service-prepared snapshot to the widget.
     * @param state immutable state snapshot
     */
    public void render(RadioModel state) { label.setText(state.frequencyText); }
}
