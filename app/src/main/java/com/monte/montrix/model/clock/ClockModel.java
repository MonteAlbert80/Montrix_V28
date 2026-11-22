package com.monte.montrix.model.clock;
/** Immutable, presentation-ready state for the Clock dashboard component.
 * All values are supplied by the corresponding service; this class performs no calculations.
 */
public final class ClockModel {
    /** Preformatted text supplied by the service layer. */
    public final String clockText;
    /** Creates a state snapshot without modifying its data.
     * @param clockText preformatted service-provided value
     */
    public ClockModel(String clockText) {
        this.clockText = clockText == null ? "" : clockText;
    }
}
