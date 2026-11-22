package com.monte.montrix.model.vehicle;
import com.monte.montrix.model.ObservableModel;
/** Observable presentation state for VehicleStatus; receives prepared values from a coordinator. */
public final class VehicleStatusModel extends ObservableModel {
 /** Display-ready primary text. */ public String primary="--";
 /** Display-ready secondary text. */ public String secondary="";
 /** Updates the state without calculations. @param main primary text @param detail secondary text */
 public void update(String main,String detail){primary=main;secondary=detail;changed();}
}
