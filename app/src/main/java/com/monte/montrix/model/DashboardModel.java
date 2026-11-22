package com.monte.montrix.model;
/** Immutable dashboard-level presentation state. No dynamic values are calculated here. */
public final class DashboardModel {
 /** Ready-to-display clock label. */ public final String clockText;
 /** Ready-to-display calendar label. */ public final String dateText;
 /** Creates dashboard state from service-provided strings. @param clockText clock label @param dateText date label */
 public DashboardModel(String clockText,String dateText){this.clockText=clockText;this.dateText=dateText;}
}
