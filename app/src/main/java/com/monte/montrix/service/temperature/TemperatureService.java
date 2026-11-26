package com.monte.montrix.service.temperature;
import com.monte.montrix.model.temperature.TemperatureModel;
/** Decodes SYU main-module outside temperature; no integer rounding. */
public final class TemperatureService {
    public TemperatureModel snapshot(Number degreesF) {
        double f = degreesF == null ? Double.NaN : degreesF.doubleValue();
        return new TemperatureModel(Double.isNaN(f) || Double.isInfinite(f) || f < -100 || f > 160 ? null : f);
    }
    public TemperatureModel decodeSyu(int raw) {
        if (raw == 0 || (raw >>> 30) != 0) return snapshot(null);
        double nativeValue = ((raw & 0x0FFFFFFF) - 1000) / 10.0;
        return snapshot((raw & 0x10000000) != 0 ? nativeValue : nativeValue * 9.0 / 5.0 + 32.0);
    }
}
