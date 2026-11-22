package com.monte.montrix.model.shift;
/** Immutable, presentation-ready state for the Shift dashboard component.
 * All values are supplied by the corresponding service; this class performs no calculations.
 */
public final class ShiftModel {
    /** Preformatted text supplied by the service layer. */
    public final String statusText;
    /** Creates a state snapshot without modifying its data.
     * @param statusText preformatted service-provided value
     */
    public ShiftModel(String statusText) {
        this.statusText = statusText == null ? "" : statusText;
    }
}
