package com.monte.montrix.service.runtime;
import android.os.Build;
import com.monte.montrix.service.vehicle.*;
/** Selects the runtime telemetry adapter once; consumers never inspect runtime mode. */
public final class RuntimeProviderFactory {
 /** Utility-only class; not instantiable. */ private RuntimeProviderFactory(){}
 /** Creates the telemetry provider from device identity.
  * @return physical or emulator provider */
 public static VehicleDataProvider createVehicleProvider(){String h=Build.HARDWARE==null?"":Build.HARDWARE.toLowerCase(java.util.Locale.US);String d=Build.DEVICE==null?"":Build.DEVICE.toLowerCase(java.util.Locale.US);return h.contains("uis")||d.contains("uis8581")||d.contains("fyt")?new FytCanBusProvider():new SimulatedCanBusProvider();}
}
