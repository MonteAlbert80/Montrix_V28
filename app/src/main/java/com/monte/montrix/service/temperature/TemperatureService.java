package com.monte.montrix.service.temperature;
import com.monte.montrix.model.temperature.TemperatureModel;
/** Converts raw temperature service inputs into presentation-ready immutable state. */
public final class TemperatureService {
    /** Creates a display snapshot from a measured value.
     * @param degreesF incoming measurement; null/zero represents unavailable
     * @return immutable component snapshot
     */
    public TemperatureModel snapshot(Integer degreesF) { return new TemperatureModel(degreesF == null ? "--°F" : degreesF + "°F"); }
}
