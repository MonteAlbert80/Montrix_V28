package com.monte.montrix.model.vehicle;
/** Immutable, presentation-ready state for the VehicleStatus dashboard component.
 * All values are supplied by the corresponding service; this class performs no calculations.
 */
public final class VehicleStatusModel {
    /** Preformatted text supplied by the service layer. */
    public final String statusText;
    /** Creates a state snapshot without modifying its data.
     * @param statusText preformatted service-provided value
     */
    public VehicleStatusModel(String statusText) {
        this.statusText = statusText == null ? "" : statusText;
    }
}
