package com.monte.montrix.service.radio;
/** Common radio command contract for FYT and Windows-hosted RTL-SDR. */
public interface RadioHardwareProvider {
 /** Receives radio state. */ interface Listener { /** Handles a radio update. @param frequency frequency text @param ps RDS program service @param rt RDS radio text @param status backend status */ void onState(String frequency,String ps,String rt,String status); }
 /** Starts radio updates. */ void start();
 /** Stops radio updates. */ void stop();
 /** Tunes FM. @param frequencyKhz frequency in kHz */ void tune(int frequencyKhz);
 /** Seeks a station. @param upward true for higher frequency */ void seek(boolean upward);
}
