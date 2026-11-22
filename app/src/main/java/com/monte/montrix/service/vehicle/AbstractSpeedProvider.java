package com.monte.montrix.service.vehicle;
import java.util.concurrent.CopyOnWriteArrayList;
/** Shared, thread-safe event dispatch for speed providers. */
abstract class AbstractSpeedProvider implements VehicleDataProvider {
 /** Registered observers. */ private final CopyOnWriteArrayList<SpeedListener> listeners=new CopyOnWriteArrayList<>();
 /** Subscribes an observer. @param listener observer @return cancellation handle */
 @Override public final Subscription observeSpeed(SpeedListener listener){if(listener==null)throw new IllegalArgumentException("listener");listeners.add(listener);return ()->listeners.remove(listener);}
 /** Publishes a speed event. @param event immutable reading */
 protected final void publish(VehicleSpeedEvent event){for(SpeedListener listener:listeners)listener.onSpeed(event);}
}
