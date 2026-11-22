package com.monte.montrix.model.speed;
import com.monte.montrix.model.ObservableModel;
/** Observable presentation state for CurrentSpeed; receives prepared values from a coordinator. */
public final class CurrentSpeedModel extends ObservableModel {
 /** Legacy snapshot fields for the original dashboard. */
 public final String text;
 public final int color;
 public final String description;
 public final com.monte.montrix.service.vehicle.VehicleSpeedEvent.Quality quality;
 public CurrentSpeedModel(){this.text="--";this.color=0xffc8d5df;this.description="";this.quality=null;}
 public CurrentSpeedModel(String text,int color,String description,com.monte.montrix.service.vehicle.VehicleSpeedEvent.Quality quality){this.text=text;this.color=color;this.description=description;this.quality=quality;this.primary=text;this.secondary=description;}
 /** Display-ready primary text. */ public String primary="--";
 /** Display-ready secondary text. */ public String secondary="";
 /** Updates the state without calculations. @param main primary text @param detail secondary text */
 public void update(String main,String detail){primary=main;secondary=detail;changed();}
}
