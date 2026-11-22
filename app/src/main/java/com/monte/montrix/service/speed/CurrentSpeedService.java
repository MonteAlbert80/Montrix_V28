package com.monte.montrix.service.speed;
import com.monte.montrix.model.speed.CurrentSpeedModel;
import com.monte.montrix.service.vehicle.*;
/** Converts vehicle telemetry to presentation-ready speed state. */
public final class CurrentSpeedService {
 /** Source of CAN-shaped telemetry. */ private final CanBusService canBus;
 /** Active subscription, if any. */ private VehicleDataProvider.Subscription subscription;
 /** Receives prepared speed display state. */ public interface Listener { /** Handles new state. @param state immutable display state */ void onSpeed(CurrentSpeedModel state); }
 /** Creates a speed feature service. @param canBus vehicle telemetry gateway */
 public CurrentSpeedService(CanBusService canBus){this.canBus=canBus;}
 /** Subscribes to telemetry. @param listener display-state observer */
 public void start(Listener listener){stop();subscription=canBus.observeSpeed(e->{String value=e.quality==VehicleSpeedEvent.Quality.VALID?Integer.toString(e.mph):"--";int color=e.quality==VehicleSpeedEvent.Quality.VALID?0xff52db89:0xffc8d5df;listener.onSpeed(new CurrentSpeedModel(value,color,e.quality==VehicleSpeedEvent.Quality.VALID?value+" miles per hour":"Vehicle speed unavailable",e.quality));});}
 /** Unsubscribes from telemetry. */ public void stop(){if(subscription!=null){subscription.close();subscription=null;}}
}
