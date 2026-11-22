package com.monte.montrix.service.apps;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import com.monte.montrix.model.apps.AppListModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Discovers applications and delegates launches to Android's task manager.
 *
 * <p>One tile is shown per package, even if the package advertises multiple
 * launcher activities. Launches use the package's canonical launcher intent
 * and do not request a new document or a separate task instance.</p>
 *
 * <p>Android and the destination application ultimately control activity
 * reuse and Recents visibility; no launcher can guarantee one process or
 * activity instance for arbitrary third-party applications.</p>
 */
public final class AppListService {
    /** Application context used for package discovery. */
    private final Context context;

    /**
     * Creates the app list service.
     * @param context Android context
     */
    public AppListService(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Returns a display-ready list with at most one tile per application.
     * @return immutable snapshot of launchable apps
     */
    public AppListModel load() {
        PackageManager pm = context.getPackageManager();
        Intent query = new Intent(Intent.ACTION_MAIN);
        query.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> matches = pm.queryIntentActivities(query, 0);
        Collections.sort(matches, new ResolveInfo.DisplayNameComparator(pm));
        Set<String> seenPackages = new HashSet<>();
        List<AppListModel.Entry> entries = new ArrayList<>();
        for (ResolveInfo match : matches) {
            if (match.activityInfo == null) continue;
            String packageName = match.activityInfo.packageName;
            if (context.getPackageName().equals(packageName)) continue;
            if (!seenPackages.add(packageName)) continue;
            Intent launch = pm.getLaunchIntentForPackage(packageName);
            if (launch == null) {
                launch = new Intent(Intent.ACTION_MAIN);
                launch.addCategory(Intent.CATEGORY_LAUNCHER);
                launch.setClassName(packageName, match.activityInfo.name);
            }
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            entries.add(new AppListModel.Entry(
                    match.loadLabel(pm).toString(), match.loadIcon(pm), launch));
        }
        return new AppListModel(entries);
    }

    /**
     * Launches an app using its canonical task-aware launcher intent.
     * @param activity foreground host activity
     * @param entry selected application
     */
    public void launch(Activity activity, AppListModel.Entry entry) {
        activity.startActivity(new Intent(entry.launchIntent));
    }
}
