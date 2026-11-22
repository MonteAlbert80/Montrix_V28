package com.monte.montrix.service.apps;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import com.monte.montrix.model.apps.AppListModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/** Queries Android's launcher registry and supplies resolved display state. */
public final class AppListService {
 /** Android context used for package discovery. */ private final Context context;
 /** Creates the app-list service. @param context application context */
 public AppListService(Context context){this.context=context.getApplicationContext();}
 /** Resolves launcher activities in display order. @return immutable presentation model */
 public AppListModel load(){PackageManager pm=context.getPackageManager();Intent query=new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);List<ResolveInfo> matches=pm.queryIntentActivities(query,0);Collections.sort(matches,new ResolveInfo.DisplayNameComparator(pm));List<AppListModel.Entry> entries=new ArrayList<>();for(ResolveInfo match:matches){if(match.activityInfo==null)continue;Intent launch=new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setClassName(match.activityInfo.packageName,match.activityInfo.name).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);entries.add(new AppListModel.Entry(match.loadLabel(pm).toString(),match.loadIcon(pm),launch));}return new AppListModel(entries);}
}
