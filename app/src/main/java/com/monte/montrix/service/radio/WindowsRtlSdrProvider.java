package com.monte.montrix.service.radio;
import com.monte.montrix.radio.backend.EmulatorSdrBackend;
/** Android emulator adapter to the existing Windows RTL-SDR HTTP bridge.
 * The Windows host must provide /state and /command on port 8765 and
 * expose the port with adb reverse tcp:8765 tcp:8765.
 */
public final class WindowsRtlSdrProvider implements RadioHardwareProvider {
 /** Existing SDR backend bridge. */ private final EmulatorSdrBackend backend;
 /** Creates the adapter. @param listener consumer of radio state */
 public WindowsRtlSdrProvider(Listener listener){backend=new EmulatorSdrBackend(listener::onState);}
 /** Starts backend polling. */ @Override public void start(){backend.start();}
 /** Stops backend polling. */ @Override public void stop(){backend.stop();}
 /** Tunes FM. @param frequencyKhz requested frequency */ @Override public void tune(int frequencyKhz){backend.tune(frequencyKhz);}
 /** Seeks FM. @param upward direction */ @Override public void seek(boolean upward){backend.seek(upward);}
}
