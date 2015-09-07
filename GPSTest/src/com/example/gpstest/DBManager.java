package com.example.gpstest;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DBManager {
	public String TAG = "DBManager";

	public List<String> getGPSList() {
		List<String> gpsList = new ArrayList<String>();
		
		Cursor c = db.rawQuery("SELECT dt, lng, lat FROM tgps ORDER BY dt DESC", null);
		
		if (c.moveToFirst()) {
			do {
				String dt = getString(c, "dt");
				String lng = getString(c, "lng");
				String lat = getString(c, "lat");
				gpsList.add(dt + ":\n" + lng + ", " + lat + "\n");
			}
			while (c.moveToNext());
		}
		c.close();
		
		return gpsList;
	}
	
	public void insertGPS(String dt, String lng, String lat) {
		String params[] = new String[] { dt, lng, lat };
		db.execSQL("INSERT INTO tgps (dt, lng, lat) VALUES (?, ?, ?)", params);
	}
	
	public DBManager(Context ctx, DBHelper helper, String dbName, int dbVersion, String dbExternalPath) {
		Log.d(TAG, "DBManager called");
		
		if (ctx == null) {
			Log.w(TAG, "context is null");
		}
		else {
			db = helper.getWritableDatabase();
		}
	}
	
	public void closeDB() {
		Log.d(TAG, "DBManager.closeDB called");
		if (db != null && db.isOpen()) {
			db.close();
		}
	}
	
	private String getString(Cursor c, String column) {
		return c.getString(c.getColumnIndex(column));
	}
	
	private SQLiteDatabase db;
}
