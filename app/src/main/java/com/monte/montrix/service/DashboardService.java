package com.monte.montrix.service;
import com.monte.montrix.model.DashboardModel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
/** Supplies dashboard-wide formatted values without exposing calculations to views. */
public final class DashboardService {
 /** Produces a timestamped dashboard snapshot. @return presentation state */
 public DashboardModel snapshot(){Date now=new Date();return new DashboardModel(new SimpleDateFormat("h:mm",Locale.US).format(now),new SimpleDateFormat("EEEE, d MMMM",Locale.US).format(now));}
}
