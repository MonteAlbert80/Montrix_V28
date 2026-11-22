package com.monte.montrix.view.music;
import android.widget.TextView;
import com.monte.montrix.model.music.MusicModel;
/** Renders the Music component without business logic or hardware access. */
public final class MusicView {
    /** Widget owned by the dashboard composition. */
    private final TextView label;
    /** Attaches a component view to its widget.
     * @param label existing dashboard label
     */
    public MusicView(TextView label) { this.label = label; }
    /** Applies a service-prepared snapshot to the widget.
     * @param state immutable state snapshot
     */
    public void render(MusicModel state) { label.setText(state.trackText); }
}
