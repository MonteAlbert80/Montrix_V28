package com.monte.montrix.model.speedlimit;
/** Immutable, presentation-ready state for the SpeedLimit dashboard component.
 * All values are supplied by the corresponding service; this class performs no calculations.
 */
public final class SpeedLimitModel {
    /** Preformatted text supplied by the service layer. */
    public final String limitText;
    /** Creates a state snapshot without modifying its data.
     * @param limitText preformatted service-provided value
     */
    public SpeedLimitModel(String limitText) {
        this.limitText = limitText == null ? "" : limitText;
    }
}
