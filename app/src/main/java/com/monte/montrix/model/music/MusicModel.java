package com.monte.montrix.model.music;
/** Immutable, presentation-ready state for the Music dashboard component.
 * All values are supplied by the corresponding service; this class performs no calculations.
 */
public final class MusicModel {
    /** Preformatted text supplied by the service layer. */
    public final String trackText;
    /** Creates a state snapshot without modifying its data.
     * @param trackText preformatted service-provided value
     */
    public MusicModel(String trackText) {
        this.trackText = trackText == null ? "" : trackText;
    }
}
