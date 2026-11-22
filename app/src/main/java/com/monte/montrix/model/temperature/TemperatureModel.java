package com.monte.montrix.model.temperature;
/** Immutable, presentation-ready state for the Temperature dashboard component.
 * All values are supplied by the corresponding service; this class performs no calculations.
 */
public final class TemperatureModel {
    /** Preformatted text supplied by the service layer. */
    public final String temperatureText;
    /** Creates a state snapshot without modifying its data.
     * @param temperatureText preformatted service-provided value
     */
    public TemperatureModel(String temperatureText) {
        this.temperatureText = temperatureText == null ? "" : temperatureText;
    }
}
