package com.monte.montrix;
import android.content.*;
import android.os.*;
import android.util.Log;
import com.monte.montrix.model.temperature.TemperatureModel;
import com.monte.montrix.service.temperature.TemperatureService;
/** Read-only SYU ambient callback transport. Owns only its own service binding. */
public final class VehicleTemperatureProvider {
    public interface Listener { void onTemperature(TemperatureModel state); }
    private static final String TK="com.syu.ipc.IRemoteToolkit", MOD="com.syu.ipc.IRemoteModule", CB="com.syu.ipc.IModuleCallback";
    private static final String TAG="MONTRIX_TEMP";
    private final Context context;
    private final Listener listener;
    private final TemperatureService service=new TemperatureService();
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final boolean realFyt;
    private boolean started,bound;
    private int generation;
    private Integer raw,availability;
    private BroadcastReceiver receiver;
    private ServiceConnection connection;
    private Binder mainCallback,canCallback;
    public VehicleTemperatureProvider(Context c, Listener listener, boolean realFyt) { this.context=c; this.listener=listener; this.realFyt=realFyt; }
    public void start() {
        if(started)return; started=true; publish(service.snapshot(null));
        if(realFyt) bind();
        else {
            receiver=new BroadcastReceiver(){@Override public void onReceive(Context c,Intent i){
                double f=read(i,"fahrenheit"), cel=read(i,"celsius");
                publish(service.snapshot(Double.isNaN(f)?cel*1.8+32:f));
            }};
            IntentFilter filter=new IntentFilter("com.monte.cwgradio.VEHICLE_TEMPERATURE");
            if(Build.VERSION.SDK_INT>=33)context.registerReceiver(receiver,filter,Context.RECEIVER_EXPORTED);else context.registerReceiver(receiver,filter);
        }
    }
    private static double read(Intent i,String key){try{Object v=i.getExtras()==null?null:i.getExtras().get(key);return v instanceof Number?((Number)v).doubleValue():v instanceof String?Double.parseDouble((String)v):Double.NaN;}catch(Exception e){return Double.NaN;}}
    private void publish(TemperatureModel state){if(started)listener.onTemperature(state);}
    private void refresh(){publish(raw!=null && Integer.valueOf(1).equals(availability)?service.decodeSyu(raw):service.snapshot(null));}
    private final Runnable retry=this::retryBinding;
    private void retryBinding(){if(started&&realFyt&&!bound)bind();}
    private void bind(){
        if(!started||bound)return;
        final int token=++generation;
        connection=new ServiceConnection(){
            @Override public void onServiceConnected(ComponentName n,IBinder toolkit){
                if(!started||token!=generation)return;
                raw=null;availability=null;refresh();
                mainCallback=callback(0,40,token);canCallback=callback(7,1012,token);
                try{register(module(toolkit,0),40,mainCallback);register(module(toolkit,7),1012,canCallback);}catch(Exception e){Log.w(TAG,"Subscription failed",e);resetAndRetry();}
            }
            @Override public void onServiceDisconnected(ComponentName n){if(token==generation)resetAndRetry();}
            @Override public void onBindingDied(ComponentName n){if(token==generation)resetAndRetry();}
            @Override public void onNullBinding(ComponentName n){if(token==generation)resetAndRetry();}
        };
        try{bound=context.bindService(new Intent("com.syu.ms.toolkit").setComponent(new ComponentName("com.syu.ms","app.ToolkitService")),connection,Context.BIND_AUTO_CREATE);}catch(Exception e){Log.w(TAG,"Bind failed",e);bound=false;}
        if(!bound)handler.postDelayed(retry,5000);
    }
    private static IBinder module(IBinder toolkit,int id)throws RemoteException{
        Parcel d=Parcel.obtain(),r=Parcel.obtain();try{d.writeInterfaceToken(TK);d.writeInt(id);if(!toolkit.transact(1,d,r,0))throw new RemoteException("Module rejected: "+id);r.readException();IBinder b=r.readStrongBinder();if(b==null)throw new RemoteException("Module absent: "+id);return b;}finally{d.recycle();r.recycle();}
    }
    private static void register(IBinder module,int id,Binder callback)throws RemoteException{
        Parcel d=Parcel.obtain(),r=Parcel.obtain();try{d.writeInterfaceToken(MOD);d.writeStrongBinder(callback);d.writeInt(id);d.writeInt(1);if(!module.transact(3,d,r,0))throw new RemoteException("Registration rejected: "+id);r.readException();}finally{d.recycle();r.recycle();}
    }
    private Binder callback(final int module,final int expected,final int token){return new Binder(){
        {attachInterface(null,CB);}
        @Override protected boolean onTransact(int code,Parcel d,Parcel r,int flags)throws RemoteException{
            if(code==INTERFACE_TRANSACTION){if(r!=null)r.writeString(CB);return true;}
            if(code!=1)return super.onTransact(code,d,r,flags);
            d.enforceInterface(CB);int id=d.readInt();int[] a=d.createIntArray();d.createFloatArray();d.createStringArray();
            if(id==expected){final Integer value=a==null||a.length==0?null:a[0];handler.post(()->{if(!started||token!=generation)return;if(module==0)raw=value;else availability=value;Log.i(TAG,"module="+module+" id="+id+" raw="+value);refresh();});}
            if(r!=null)r.writeNoException();return true;
        }
    };}
    private void disconnect(){++generation;if(bound&&connection!=null)try{context.unbindService(connection);}catch(Exception e){Log.w(TAG,"Unbind",e);}bound=false;connection=null;mainCallback=null;canCallback=null;raw=null;availability=null;}
    private void resetAndRetry(){disconnect();refresh();handler.removeCallbacks(retry);if(started)handler.postDelayed(retry,5000);}
    public void stop(){started=false;handler.removeCallbacksAndMessages(null);disconnect();if(receiver!=null){try{context.unregisterReceiver(receiver);}catch(Exception ignored){}receiver=null;}}
}
