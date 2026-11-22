package com.monte.montrix.service.vehicle;
/** Immutable speed event; the value is miles per hour, timestamp is elapsed realtime. */
public final class VehicleSpeedEvent {
 /** Reading validity. */ public enum Quality { VALID, STALE, UNAVAILABLE }
 /** Vehicle speed in miles per hour; zero is meaningful only when quality is VALID. */ public final int mph;
 /** Monotonic event time in milliseconds. */ public final long elapsedMs;
 /** Whether the reading is usable. */ public final Quality quality;
 /** Creates a speed event. @param mph speed in miles per hour @param elapsedMs monotonic time @param quality reading quality */
 public VehicleSpeedEvent(int mph,long elapsedMs,Quality quality){this.mph=mph;this.elapsedMs=elapsedMs;this.quality=quality;}
}
