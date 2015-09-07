package com.example.gpstest;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
	public String TAG = "DBHelper";
	
	public DBHelper(Context ctx, String name, CursorFactory factory,
			int version) {
		super(ctx, name, factory, version);
		
		Log.d(TAG, ("name=" + name + ", version=" + version));
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d(TAG, ("onCreate called"));
		
		db.execSQL("CREATE TABLE tgps (dt text primary key, lat text, lng text)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d(TAG, ("onUpgrade called"));
	}
}
