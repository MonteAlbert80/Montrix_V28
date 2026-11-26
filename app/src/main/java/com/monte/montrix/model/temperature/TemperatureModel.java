package com.monte.montrix.model.temperature;
import com.monte.montrix.model.ObservableModel;
/** Numeric ambient-temperature state, independent of UI formatting. */
public final class TemperatureModel extends ObservableModel {
    public Double degreesF;
    public Double degreesC;
    public boolean available;
    public TemperatureModel() { this((Double)null); }
    public TemperatureModel(Double f) { assign(f); }
    private void assign(Double f) {
        available = f != null && !Double.isNaN(f) && !Double.isInfinite(f);
        degreesF = available ? f : null;
        degreesC = available ? (f - 32.0) * 5.0 / 9.0 : null;
    }
    public void update(TemperatureModel state) { assign(state.degreesF); changed(); }
}
