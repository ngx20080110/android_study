package com.example.gpstest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

public class ThisApplication extends Application {
	public String TAG = "ThisApplication";
	
	/**
	 * Get the external files path of the application
	 * 
	 * @return String
	 */
	public String getExternalFilesPath() {
		return externalFilesPath;
	}
	
	public String getPhotoPath() {
		File photoDir = new File(photoPath);
		if (!photoDir.exists()) {
			photoDir.mkdir();
		}
		return photoPath;
	}
	
	/**
	 * Get the DBManager instance
	 * 
	 * @return DBManager
	 */
	public DBManager getDBManager() {
		return dbMgr;
	}
	
	public void exportDB() {
		Log.d(TAG, "exportDB called");
		
		PackageManager pm = getPackageManager();
		
		try {
			PackageInfo pkgInfo = pm.getPackageInfo(getPackageName(), 0);
			
			if (pkgInfo != null) {
				String pkgPath = pkgInfo.applicationInfo.dataDir;
				
				File source = new File(pkgPath + File.separator + "databases" + File.separator + dbName);
				File dbDir = new File(externalDBPath);
				if (!dbDir.exists()) {
					dbDir.mkdir();
				}
				
				String destName = externalDBPath + dbName;
				File dest = new File(destName);
				
				
				if (source.exists()) {
					if (dest.exists()) {
						dest.delete();
					}
					
					try {
						copyFile(source, dest);
						Log.d(TAG, source.getAbsolutePath() + " exported to " + dest.getAbsolutePath());
					}
					catch (IOException ex) {
						Log.e(TAG, ex.getMessage());
					}
				}
			}
		} catch (NameNotFoundException e) {
			Log.e(TAG, e.getMessage());
		}
	}
	
	public void releaseResources() {
		Log.d(TAG, "releaseResources called");
		
		// Release resource of SQLite database
		if (dbMgr != null) {
			dbMgr.closeDB();
		}
		if (dbHelper != null) {
			dbHelper.close();
		}
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		// Define the variables
		externalFilesPath = this.getExternalFilesDir("").getAbsolutePath();
		photoPath = externalFilesPath + File.separator + "photo" + File.separator;
		dbName = "gpstest.db";
		dbVersion = 100;
		externalDBPath = externalFilesPath + File.separator + "db" + File.separator;
		
		Log.d(TAG, "onCreate called");
		
		// Create DBHelper
		dbHelper = new DBHelper(this, dbName, null, dbVersion);
		
		// Create DBManger
		dbMgr = new DBManager(this, dbHelper, dbName, dbVersion, externalDBPath + dbName);
	}
	
	private void copyFile(File source, File destination) throws IOException {
		InputStream input = null;
		OutputStream output = null;

		try {
			input = new FileInputStream(source);
			output = new FileOutputStream(destination);

			byte[] buf = new byte[1024];
			int bytesRead;
			while ((bytesRead = input.read(buf)) > 0) {
				output.write(buf, 0, bytesRead);
			}
		}
		catch (Exception ex) {
			Log.e(TAG, ex.getMessage());
		}
		finally {
			input.close();
			output.close();
		}
	}
	
	private DBHelper dbHelper;
	private DBManager dbMgr;
	private String externalFilesPath;
	private String photoPath;
	private String dbName;
	private int dbVersion;
	private String externalDBPath;
}
