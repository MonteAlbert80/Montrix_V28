package com.monte.montrix.service.vehicle;
import android.os.SystemClock;
/** Emulator vehicle telemetry input, accepting route-replay speed events. */
public final class SimulatedCanBusProvider extends AbstractSpeedProvider {
 /** Whether replay input is accepted. */ private boolean running;
 /** Starts the emulator input. */ @Override public synchronized void start(){running=true;}
 /** Stops emulator input. */ @Override public synchronized void stop(){running=false;}
 /** Injects a route-calculated speed; no speed calculation occurs in Views.
  * @param mph route speed in miles per hour */
 public synchronized void acceptRouteSpeed(int mph){if(running&&mph>=0&&mph<=200)publish(new VehicleSpeedEvent(mph,SystemClock.elapsedRealtime(),VehicleSpeedEvent.Quality.VALID));}
}
