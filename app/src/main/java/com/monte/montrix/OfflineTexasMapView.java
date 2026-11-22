package com.monte.montrix;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Environment;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.view.Gravity;
import java.io.File;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.datastore.MapDataStore;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.cache.InMemoryTileCache;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.internal.MapsforgeThemes;
import org.mapsforge.map.view.InputListener;

/** V24: stable offline map + geographic GPS marker + manual-pan/follow separation. */
public class OfflineTexasMapView extends FrameLayout {
    private static final String TAG="ASURE_OFFLINE_MAP";
    // Simulator-only home reference, corrected from the V20 eastward offset.
    public static final double SIM_HOME_LAT=30.49603, SIM_HOME_LON=-97.57347;
    private MapView map;
    private TileRendererLayer layer;
    private MapDataStore store;
    private android.view.View positionDot; private android.widget.TextView headingMarker;
    private LatLong livePosition;
    private boolean ready;
    private Runnable manualMoveListener;

    public OfflineTexasMapView(Context c) {
        super(c);
        setBackgroundColor(Color.rgb(10,16,22));
        try { AndroidGraphicFactory.createInstance(c.getApplicationContext()); } catch (Throwable ignored) {}
        File f=new File(Environment.getExternalStorageDirectory(),"ASURE/maps/texas.map");
        if (!f.isFile() || f.length()<1024*1024) { message("OFFLINE TEXAS MAP NOT INSTALLED"); return; }
        try {
            map=new MapView(c);
            android.graphics.drawable.GradientDrawable mapMask=new android.graphics.drawable.GradientDrawable();
            mapMask.setColor(0xff101820);
            mapMask.setCornerRadius(15f);
            map.setBackground(mapMask);
            map.setClipToOutline(true);
            map.setClickable(true);
            map.setBuiltInZoomControls(false);
            map.getMapScaleBar().setVisible(false);
            map.getModel().frameBufferModel.setOverdrawFactor(1.15);
            livePosition=new LatLong(SIM_HOME_LAT,SIM_HOME_LON);
            map.getModel().mapViewPosition.setCenter(livePosition);
            map.getModel().mapViewPosition.setZoomLevel((byte)16);

            TileCache cache=new InMemoryTileCache(64);
            store=new MapFile(f);
            layer=new TileRendererLayer(cache,store,map.getModel().mapViewPosition,AndroidGraphicFactory.INSTANCE);
            layer.setXmlRenderTheme(MapsforgeThemes.MOTORIDER);
            map.getLayerManager().getLayers().add(layer);

            // This listener fires only for HUMAN map movement/zoom, not software recentering.
            // Dashboard uses it to suspend follow mode when the driver pans away.
            map.addInputListener(new InputListener(){
                @Override public void onMoveEvent(){ if(manualMoveListener!=null) manualMoveListener.run(); }
                @Override public void onZoomEvent(){ /* preserve follow state for pinch zoom */ }
            });

            addView(map,new FrameLayout.LayoutParams(-1,-1));

            // IMPORTANT: marker is a CHILD OF MAPVIEW anchored to LatLong.
            // Mapsforge therefore projects it automatically during every pan/zoom.
            positionDot=new android.view.View(c);
            GradientDrawable dot=new GradientDrawable();
            dot.setShape(GradientDrawable.OVAL);
            dot.setColor(0xff168cff);
            dot.setStroke(4,0xffffffff);
            positionDot.setBackground(dot);
            positionDot.setElevation(12f);
            MapView.LayoutParams gpsLp=new MapView.LayoutParams(
                22,22,livePosition,MapView.LayoutParams.Alignment.CENTER);
            map.addView(positionDot,gpsLp);
            headingMarker=new android.widget.TextView(c);headingMarker.setText("➤");headingMarker.setTextSize(30);headingMarker.setTextColor(0xff006dff);headingMarker.setGravity(Gravity.CENTER);headingMarker.setVisibility(GONE);headingMarker.setElevation(13f);
            map.addView(headingMarker,new MapView.LayoutParams(48,48,livePosition,MapView.LayoutParams.Alignment.CENTER));

            ready=true;
            Log.i(TAG,"V24 map ready zoom=16 cache=64 geographic-marker theme=MOTORIDER bytes="+f.length());
        } catch(Throwable e) { Log.e(TAG,"offline map init failed",e); message("OFFLINE MAP ERROR"); }
    }

    public void setOnManualMove(Runnable r){manualMoveListener=r;}
    private void message(String s){TextView t=new TextView(getContext());t.setText(s+"\nRun install_v21.ps1 to install the Texas map");t.setTextColor(0xffd9e7f2);t.setTextSize(16);t.setGravity(Gravity.CENTER);addView(t,new FrameLayout.LayoutParams(-1,-1));}
    public boolean isReady(){return ready;}

    public void zoomBy(int d){
        if(map!=null){
            byte z=map.getModel().mapViewPosition.getZoomLevel();
            map.getModel().mapViewPosition.setZoomLevel((byte)Math.max(3,Math.min(20,z+d)));
        }
    }

    public void showPosition(double lat,double lon,boolean recenter){
        if(map==null||!validTexas(lat,lon))return;
        livePosition=new LatLong(lat,lon);
        if(positionDot!=null){
            android.view.ViewGroup.LayoutParams raw=positionDot.getLayoutParams();
            if(raw instanceof MapView.LayoutParams){
                MapView.LayoutParams lp=(MapView.LayoutParams)raw;
                lp.latLong=livePosition;
                positionDot.setLayoutParams(lp);
                positionDot.requestLayout();
                if(headingMarker!=null){MapView.LayoutParams hp=(MapView.LayoutParams)headingMarker.getLayoutParams();hp.latLong=livePosition;headingMarker.setLayoutParams(hp);}
            }
        }
        // GPS changes move the marker independently. Only follow/Locate moves the map.
        if(recenter){
            map.getModel().mapViewPosition.setCenter(livePosition);
            if(map.getModel().mapViewPosition.getZoomLevel()<15)
                map.getModel().mapViewPosition.setZoomLevel((byte)16);
        }
    }

    public void setHeading(float heading,boolean moving){
      if(headingMarker==null)return;
      headingMarker.setVisibility(moving?VISIBLE:GONE);positionDot.setVisibility(moving?GONE:VISIBLE);
      headingMarker.setRotation(heading-90f); // right-facing arrow at zero rotation; north = upward
    }
    public void centerOnLive(){
        if(map!=null&&livePosition!=null)map.getModel().mapViewPosition.setCenter(livePosition);
    }
    public void centerHome(){showPosition(SIM_HOME_LAT,SIM_HOME_LON,true);}
    public static boolean validTexas(double lat,double lon){return lat>=25.7&&lat<=36.7&&lon>=-106.8&&lon<=-93.4;}
    public void destroy(){try{if(map!=null)map.destroyAll();}catch(Throwable ignored){}try{if(store!=null)store.close();}catch(Throwable ignored){}}
}
