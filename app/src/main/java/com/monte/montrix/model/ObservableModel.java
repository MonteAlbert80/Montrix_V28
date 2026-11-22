package com.monte.montrix.model;
import java.util.ArrayList;
import java.util.List;
/** Base class for UI-independent observable state. */
public abstract class ObservableModel {
 /** Registered change listeners. */ private final List<Runnable> listeners=new ArrayList<>();
 /** Registers a listener. @param listener state observer */ public final void addListener(Runnable listener){if(listener!=null&&!listeners.contains(listener))listeners.add(listener);}
 /** Removes a listener. @param listener state observer */ public final void removeListener(Runnable listener){listeners.remove(listener);}
 /** Notifies listeners after a state update. */ protected final void changed(){for(Runnable r:new ArrayList<>(listeners))r.run();}
}
