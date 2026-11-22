package com.monte.montrix.service.vehicle;
/** Sole vehicle telemetry gateway exposed to feature services. */
public final class CanBusService {
 /** Selected runtime-specific provider. */ private final VehicleDataProvider provider;
 /** Creates a gateway. @param provider hardware or simulator provider */
 public CanBusService(VehicleDataProvider provider){this.provider=provider;}
 /** Starts vehicle event acquisition. */ public void start(){provider.start();}
 /** Stops vehicle event acquisition. */ public void stop(){provider.stop();}
 /** Observes speed. @param listener observer @return subscription */
 public VehicleDataProvider.Subscription observeSpeed(VehicleDataProvider.SpeedListener listener){return provider.observeSpeed(listener);}
}
