package com.monte.montrix.view.apps;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.monte.montrix.model.apps.AppListModel;
/** Renders the launcher grid; never queries packages or calculates application state. */
public final class AppListView {
 /** Activity owning the modal window. */ private final Activity activity;
 /** Creates the app list view. @param activity modal host */
 public AppListView(Activity activity){this.activity=activity;}
 /** Displays a supplied snapshot and forwards launches. @param model display data */
 public void show(AppListModel model){final Dialog dialog=new Dialog(activity);GridView grid=new GridView(activity);grid.setBackgroundColor(Color.rgb(12,23,34));grid.setNumColumns(4);grid.setVerticalSpacing(14);grid.setHorizontalSpacing(8);grid.setAdapter(new BaseAdapter(){
  /** Returns the number of visible apps. @return count */ public int getCount(){return model.entries.size();}
  /** Returns the entry at an index. @param position index @return entry */ public Object getItem(int position){return model.entries.get(position);}
  /** Returns the stable row index for this snapshot. @param position index @return row id */ public long getItemId(int position){return position;}
  /** Creates a tile for one app. @param position index @param reusable recycled view @param parent grid @return tile */
  public View getView(int position,View reusable,ViewGroup parent){AppListModel.Entry entry=model.entries.get(position);LinearLayout tile=new LinearLayout(activity);tile.setOrientation(LinearLayout.VERTICAL);tile.setGravity(Gravity.CENTER);tile.setPadding(6,8,6,8);tile.setMinimumHeight(145);tile.setBackgroundColor(Color.rgb(18,34,49));ImageView icon=new ImageView(activity);icon.setImageDrawable(entry.icon);tile.addView(icon,new LinearLayout.LayoutParams(58,58));TextView title=new TextView(activity);title.setText(entry.title);title.setGravity(Gravity.CENTER);title.setTextSize(16);title.setTextColor(Color.WHITE);title.setMaxLines(2);title.setEllipsize(android.text.TextUtils.TruncateAt.END);tile.addView(title,new LinearLayout.LayoutParams(-1,68));return tile;}
 });grid.setOnItemClickListener((parent,view,position,id)->{activity.startActivity(model.entries.get(position).launchIntent);dialog.dismiss();});dialog.setContentView(grid);dialog.show();if(dialog.getWindow()!=null)dialog.getWindow().setLayout((int)(activity.getResources().getDisplayMetrics().widthPixels*.9),(int)(activity.getResources().getDisplayMetrics().heightPixels*.85));}
}
