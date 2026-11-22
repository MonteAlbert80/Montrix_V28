package com.monte.montrix.model.temperature;
import com.monte.montrix.model.ObservableModel;
/** Observable presentation state for Temperature; receives prepared values from a coordinator. */
public final class TemperatureModel extends ObservableModel {
 /** Original dashboard snapshot. */ public final String temperatureText;
 public TemperatureModel(){this.temperatureText="";}
 public TemperatureModel(String text){this.temperatureText=text==null?"":text;this.primary=this.temperatureText;}
 /** Display-ready primary text. */ public String primary="--";
 /** Display-ready secondary text. */ public String secondary="";
 /** Updates the state without calculations. @param main primary text @param detail secondary text */
 public void update(String main,String detail){primary=main;secondary=detail;changed();}
}
