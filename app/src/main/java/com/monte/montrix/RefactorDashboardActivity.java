package com.monte.montrix;
import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Build;
import com.monte.montrix.coordinator.DashboardCoordinator;
import com.monte.montrix.model.DashboardModel;
import com.monte.montrix.view.DashboardView;
/** Android lifecycle host for Montrix V28. No widget construction or business calculations. */
public final class RefactorDashboardActivity extends Activity {
 /** Permission request identifier. */ private static final int MEDIA_PERMISSION=2801;
 /** Owns service lifecycle and model bindings. */ private DashboardCoordinator coordinator;
 /** Initializes the dashboard and requests media access when necessary. @param state saved state */
 @Override public void onCreate(Bundle state){super.onCreate(state);DashboardModel model=new DashboardModel();coordinator=new DashboardCoordinator(this,model);setContentView(new DashboardView(this,model));if(Build.VERSION.SDK_INT>=23&&checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},MEDIA_PERMISSION);}else coordinator.start();}
 /** Receives the media permission result. @param code request code @param permissions requested permissions @param grants grant results */
 @Override public void onRequestPermissionsResult(int code,String[] permissions,int[] grants){super.onRequestPermissionsResult(code,permissions,grants);if(code==MEDIA_PERMISSION&&coordinator!=null)coordinator.start();}
 /** Resumes state refresh. */ @Override protected void onResume(){super.onResume();if(coordinator!=null)coordinator.resume();}
 /** Pauses model updates. */ @Override protected void onPause(){if(coordinator!=null)coordinator.pause();super.onPause();}
 /** Releases all service resources. */ @Override protected void onDestroy(){if(coordinator!=null)coordinator.close();super.onDestroy();}
}
