package com.monte.montrix.trips;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.SystemClock;

/**
 * GPS persistence module. Does NOT infer a shift from a button.
 * This first implementation records fixes while the dashboard is active.
 * A foreground collection service is required before claiming all-day coverage.
 */
public final class TripRecorder {
    private final TripDatabase helper;
    private Location previousAccepted;
    private long activeTripId = -1;
    private long lastAcceptedElapsedMs = -1;
    private static final long GAP_MS = 12 * 60 * 1000L;
    private static final float MAX_ACCURACY_M = 75f;
    private static final float MAX_SPEED_MPS = 65f;

    public TripRecorder(Context context) {
        helper = new TripDatabase(context.getApplicationContext());
    }

    public synchronized void record(Location fix) {
        if (fix == null) return;
        boolean accepted = Double.isFinite(fix.getLatitude()) && Double.isFinite(fix.getLongitude())
                && Math.abs(fix.getLatitude()) <= 90 && Math.abs(fix.getLongitude()) <= 180
                && (!fix.hasAccuracy() || fix.getAccuracy() <= MAX_ACCURACY_M)
                && (!fix.hasSpeed() || fix.getSpeed() <= MAX_SPEED_MPS);
        long now = fix.getTime() > 0 ? fix.getTime() : System.currentTimeMillis();
        long elapsed = SystemClock.elapsedRealtime();
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues point = new ContentValues();
            point.put("time_ms", now);
            point.put("elapsed_ns", fix.getElapsedRealtimeNanos());
            point.put("latitude", fix.getLatitude());
            point.put("longitude", fix.getLongitude());
            if (fix.hasAccuracy()) point.put("accuracy_m", fix.getAccuracy());
            if (fix.hasSpeed()) point.put("speed_mps", fix.getSpeed());
            if (fix.hasBearing()) point.put("bearing_deg", fix.getBearing());
            point.put("provider", fix.getProvider());
            point.put("accepted", accepted ? 1 : 0);
            if (!accepted) point.put("rejection_reason", "invalid_or_low_quality_fix");
            db.insertOrThrow("gps_points", null, point);
            if (accepted) {
                boolean newTrip = previousAccepted == null || lastAcceptedElapsedMs < 0
                        || elapsed - lastAcceptedElapsedMs > GAP_MS;
                if (newTrip) {
                    if (activeTripId > 0) {
                        ContentValues end = new ContentValues();
                        end.put("end_ms", now);
                        db.update("trips", end, "id=?", new String[]{Long.toString(activeTripId)});
                    }
                    ContentValues trip = new ContentValues();
                    trip.put("start_ms", now);
                    activeTripId = db.insertOrThrow("trips", null, trip);
                } else {
                    float distance = previousAccepted.distanceTo(fix);
                    long deltaMs = elapsed - lastAcceptedElapsedMs;
                    if (distance >= 3 && deltaMs > 0 &&
                            distance / (deltaMs / 1000.0) <= MAX_SPEED_MPS) {
                        db.execSQL("UPDATE trips SET distance_m=distance_m+?, end_ms=? WHERE id=?",
                                new Object[]{(double)distance, now, activeTripId});
                    }
                }
                previousAccepted = new Location(fix);
                lastAcceptedElapsedMs = elapsed;
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public synchronized void close() {
        helper.close();
    }
}
