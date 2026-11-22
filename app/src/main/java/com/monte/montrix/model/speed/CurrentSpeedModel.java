package com.monte.montrix.model.speed;
import com.monte.montrix.service.vehicle.VehicleSpeedEvent;
/** Immutable presentation-ready current-speed state. */
public final class CurrentSpeedModel {
 /** Speed display text. */ public final String text;
 /** Display color for the current speed. */ public final int color;
 /** Accessibility description. */ public final String description;
 /** Source quality. */ public final VehicleSpeedEvent.Quality quality;
 /** Creates a display state. @param text display string @param color ARGB color @param description accessibility description @param quality source quality */
 public CurrentSpeedModel(String text,int color,String description,VehicleSpeedEvent.Quality quality){this.text=text;this.color=color;this.description=description;this.quality=quality;}
}
