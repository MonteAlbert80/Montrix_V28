package com.monte.montrix.model;
import com.monte.montrix.model.music.MusicModel;
/** Dashboard aggregate state. Contains no calculations, database queries, or services. */
public final class DashboardModel {
 /** Legacy clock and date snapshot used by the original dashboard. */
 public final String clockText;
 public final String dateText;
 /** Creates state for the refactored dashboard. */
 public DashboardModel(){this("", "");}
 /** Creates a clock/date snapshot for the original dashboard. */
 public DashboardModel(String clockText,String dateText){this.clockText=clockText;this.dateText=dateText;}
 /** Selected media source and selector intent. */ public final MediaSelectionModel media=new MediaSelectionModel();
 /** Music state. */public final MusicModel music=new MusicModel();
 /** Radio state. */ public final com.monte.montrix.model.radio.RadioModel radio=new com.monte.montrix.model.radio.RadioModel();
 /** Bluetooth state. */ public final com.monte.montrix.model.bluetooth.BluetoothModel bluetooth=new com.monte.montrix.model.bluetooth.BluetoothModel();
 /** CurrentSpeed state. */ public final com.monte.montrix.model.speed.CurrentSpeedModel speed=new com.monte.montrix.model.speed.CurrentSpeedModel();
 /** SpeedLimit state. */ public final com.monte.montrix.model.speedlimit.SpeedLimitModel speedlimit=new com.monte.montrix.model.speedlimit.SpeedLimitModel();
 /** Temperature state. */ public final com.monte.montrix.model.temperature.TemperatureModel temperature=new com.monte.montrix.model.temperature.TemperatureModel();
 /** Map state. */ public final com.monte.montrix.model.map.MapModel map=new com.monte.montrix.model.map.MapModel();
 /** Shift state. */ public final com.monte.montrix.model.shift.ShiftModel shift=new com.monte.montrix.model.shift.ShiftModel();
 /** VehicleStatus state. */ public final com.monte.montrix.model.vehicle.VehicleStatusModel vehicle=new com.monte.montrix.model.vehicle.VehicleStatusModel();
 /** Clock state. */ public final com.monte.montrix.model.clock.ClockModel clock=new com.monte.montrix.model.clock.ClockModel();
 /** PictureAlbum state. */ public final com.monte.montrix.model.album.PictureAlbumModel album=new com.monte.montrix.model.album.PictureAlbumModel();
}
