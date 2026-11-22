package com.monte.montrix.model.apps;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
/** Immutable presentation-ready snapshot of launchable applications. */
public final class AppListModel {
 /** One launchable application with resolved display metadata. */
 public static final class Entry {
  /** Human-readable application name. */ public final String title;
  /** Icon supplied by Android package manager. */ public final Drawable icon;
  /** Explicit activity launch intent. */ public final Intent launchIntent;
  /** Creates a display entry. @param title resolved label @param icon application icon @param launchIntent explicit activity intent */
  public Entry(String title, Drawable icon, Intent launchIntent){this.title=title;this.icon=icon;this.launchIntent=launchIntent;}
 }
 /** Snapshot of available launchers. */ public final List<Entry> entries;
 /** Creates an immutable list model. @param entries resolved applications */
 public AppListModel(List<Entry> entries){this.entries=Collections.unmodifiableList(new ArrayList<>(entries));}
}
