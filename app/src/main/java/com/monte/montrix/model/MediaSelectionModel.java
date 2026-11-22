package com.monte.montrix.model;
/** Selected dashboard audio source. Selection is user intent, not a hardware claim. */
public final class MediaSelectionModel extends ObservableModel {
 public enum Source { RADIO, MUSIC, BLUETOOTH }
 public interface Actions { void select(Source source); }
 private Source selected=Source.MUSIC;
 private Actions actions;
 public Source selected(){return selected;}
 public void setActions(Actions value){actions=value;}
 public void requestSelect(Source source){if(source!=null && actions!=null)actions.select(source);}
 public void update(Source source){if(source!=null && selected!=source){selected=source;changed();}}
}
