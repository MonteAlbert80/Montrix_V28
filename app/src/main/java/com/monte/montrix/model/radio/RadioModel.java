package com.monte.montrix.model.radio;
/** Immutable, presentation-ready state for the Radio dashboard component.
 * All values are supplied by the corresponding service; this class performs no calculations.
 */
public final class RadioModel {
    /** Preformatted text supplied by the service layer. */
    public final String frequencyText;
    /** Creates a state snapshot without modifying its data.
     * @param frequencyText preformatted service-provided value
     */
    public RadioModel(String frequencyText) {
        this.frequencyText = frequencyText == null ? "" : frequencyText;
    }
}
