package com.monte.montrix.trips;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/** Work-by-default classification; personal exclusions never delete raw GPS evidence. */
public final class TripReviewRepository {
    private final TripDatabase database;
    public TripReviewRepository(Context context) { database = new TripDatabase(context); }

    public void setPersonal(long tripId, boolean personal) {
        ContentValues values = new ContentValues();
        values.put("classification", personal ? "PERSONAL" : "WORK_ASSUMED");
        values.put("excluded", personal ? 1 : 0);
        database.getWritableDatabase().update("trips", values, "id=?",
                new String[]{Long.toString(tripId)});
    }

    public Cursor allTrips() {
        return database.getReadableDatabase().rawQuery(
                "SELECT id,start_ms,end_ms,distance_m,classification,excluded,note " +
                "FROM trips ORDER BY start_ms DESC", null);
    }
}
