package com.monte.montrix.service.vehicle;
import android.os.SystemClock;
/** Physical FYT CAN adapter. No GPS fallback and no invented CAN decoding.
 * The supplied trace logs updateCode=1049 but does not establish the numeric
 * speed payload or the callable OEM interface. Until verified, emits UNAVAILABLE.
 */
public final class FytCanBusProvider extends AbstractSpeedProvider {
 /** Acquisition lifecycle flag. */ private boolean running;
 /** Starts acquisition; an unverified adapter must never invent vehicle speed. */
 @Override public synchronized void start(){running=true;publish(new VehicleSpeedEvent(0,SystemClock.elapsedRealtime(),VehicleSpeedEvent.Quality.UNAVAILABLE));}
 /** Stops acquisition. */ @Override public synchronized void stop(){running=false;}
 /** Receives speed only from a future verified OEM CAN decoder.
  * @param mph decoded vehicle speed in miles per hour */
 void acceptVerifiedCanSpeed(int mph){if(running&&mph>=0&&mph<=200)publish(new VehicleSpeedEvent(mph,SystemClock.elapsedRealtime(),VehicleSpeedEvent.Quality.VALID));}
}
