package com.monte.montrix.trips;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Local-first trip journal. GPS points are immutable observations; derived
 * trips and work classifications can be corrected without deleting history.
 *
 * Future schema changes MUST use migrations, not destructive recreation.
 */
public final class TripDatabase extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "montrix_trips.sqlite";
    public static final int VERSION = 1;

    public TripDatabase(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE gps_points (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "time_ms INTEGER NOT NULL, elapsed_ns INTEGER," +
                "latitude REAL NOT NULL, longitude REAL NOT NULL," +
                "accuracy_m REAL, speed_mps REAL, bearing_deg REAL," +
                "provider TEXT, accepted INTEGER NOT NULL DEFAULT 1," +
                "rejection_reason TEXT)");
        db.execSQL("CREATE INDEX idx_gps_time ON gps_points(time_ms)");
        db.execSQL("CREATE TABLE trips (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "start_ms INTEGER NOT NULL, end_ms INTEGER," +
                "distance_m REAL NOT NULL DEFAULT 0," +
                "classification TEXT NOT NULL DEFAULT 'WORK_ASSUMED'," +
                "excluded INTEGER NOT NULL DEFAULT 0," +
                "note TEXT)");
        db.execSQL("CREATE TABLE stops (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, trip_id INTEGER," +
                "arrival_ms INTEGER NOT NULL, departure_ms INTEGER," +
                "latitude REAL, longitude REAL, duration_ms INTEGER," +
                "FOREIGN KEY(trip_id) REFERENCES trips(id))");
        db.execSQL("CREATE TABLE turns (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, trip_id INTEGER," +
                "time_ms INTEGER NOT NULL, latitude REAL, longitude REAL," +
                "heading_before REAL, heading_after REAL, angle_deg REAL," +
                "FOREIGN KEY(trip_id) REFERENCES trips(id))");
        db.execSQL("CREATE TABLE shifts (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, start_ms INTEGER NOT NULL," +
                "end_ms INTEGER, source TEXT NOT NULL DEFAULT 'INFERRED'," +
                "note TEXT)");
        db.execSQL("CREATE TABLE trip_shift (" +
                "trip_id INTEGER NOT NULL, shift_id INTEGER NOT NULL," +
                "PRIMARY KEY(trip_id,shift_id))");
        db.execSQL("CREATE TABLE events (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, time_ms INTEGER NOT NULL," +
                "type TEXT NOT NULL, details TEXT)");
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        throw new IllegalStateException("TripDatabase migration required: " + oldVersion + " -> " + newVersion);
    }
}
