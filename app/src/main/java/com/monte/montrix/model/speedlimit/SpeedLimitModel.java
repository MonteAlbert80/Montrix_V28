package com.monte.montrix.model.speedlimit;
import com.monte.montrix.model.ObservableModel;
/** Observable presentation state for SpeedLimit; receives prepared values from a coordinator. */
public final class SpeedLimitModel extends ObservableModel {
 /** Original dashboard snapshot. */ public final String limitText;
 public SpeedLimitModel(){this.limitText="";}
 public SpeedLimitModel(String text){this.limitText=text==null?"":text;this.primary=this.limitText;}
 /** Display-ready primary text. */ public String primary="--";
 /** Display-ready secondary text. */ public String secondary="";
 /** Updates the state without calculations. @param main primary text @param detail secondary text */
 public void update(String main,String detail){primary=main;secondary=detail;changed();}
}
