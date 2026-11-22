package com.monte.montrix.model.map;
/** Immutable, presentation-ready state for the Map dashboard component.
 * All values are supplied by the corresponding service; this class performs no calculations.
 */
public final class MapModel {
    /** Preformatted text supplied by the service layer. */
    public final String statusText;
    /** Creates a state snapshot without modifying its data.
     * @param statusText preformatted service-provided value
     */
    public MapModel(String statusText) {
        this.statusText = statusText == null ? "" : statusText;
    }
}
