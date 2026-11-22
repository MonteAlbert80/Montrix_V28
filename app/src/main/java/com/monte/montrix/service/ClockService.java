package com.monte.montrix.service;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
/** Supplies presentation-ready time and date strings. */
public final class ClockService {
 /** Returns formatted time. @return time */ public String time(){return new SimpleDateFormat("h:mm a",Locale.US).format(new Date());}
 /** Returns formatted date. @return date */ public String date(){return new SimpleDateFormat("EEE, MMM d",Locale.US).format(new Date());}
}
