package com.monte.montrix.service.speedlimit;
import com.monte.montrix.model.speedlimit.SpeedLimitModel;
/** Converts raw speedlimit service inputs into presentation-ready immutable state. */
public final class SpeedLimitService {
    /** Creates a display snapshot from a measured value.
     * @param mph incoming measurement; null/zero represents unavailable
     * @return immutable component snapshot
     */
    public SpeedLimitModel snapshot(int mph) { return new SpeedLimitModel(mph > 0 ? Integer.toString(mph) : "--"); }
}
