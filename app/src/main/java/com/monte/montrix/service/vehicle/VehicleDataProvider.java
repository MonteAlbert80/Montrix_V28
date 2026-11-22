package com.monte.montrix.service.vehicle;
/** Common contract for physical CAN and emulator telemetry providers. */
public interface VehicleDataProvider {
 /** Receives vehicle speed changes. */ interface SpeedListener { /** Handles a speed reading. @param event incoming reading */ void onSpeed(VehicleSpeedEvent event); }
 /** Unsubscribes from events. */ interface Subscription { /** Releases this listener. */ void close(); }
 /** Registers a speed observer. @param listener consumer @return closable subscription */ Subscription observeSpeed(SpeedListener listener);
 /** Starts acquisition. */ void start();
 /** Stops acquisition and releases resources. */ void stop();
}
