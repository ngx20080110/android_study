package com.example.gpstest;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	public String TAG = "MainActivity";
	private BluetoothAdapter mBluetoothAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate called");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		txt = (TextView)findViewById(R.id.textView1);
		list = (ListView)findViewById(R.id.listView1);
		
		locMgr = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		
		if (locMgr != null) {
			Log.d(TAG, "locMgr is not null");		
			Location location = locMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (location != null) {
				setGPSInfo(location, true);
			}
			else {
				Log.d(TAG, "getLastKnownLocation is null");
			}
		}
		else {
			Log.d(TAG, "locMgr is null");
		}
		
		listener = new MyLocationListener();
		
		app = (ThisApplication)getApplication();

		dbm = app.getDBManager();
		
		  //reference to the bluetooth adapter
		  mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		  //check if adatpter is available, please note if you running 
		  //this application in emulator currently there is no support for bluetooth
		  if(mBluetoothAdapter == null){
		   Log.d(TAG, "BlueTooth adapter not found");
		  }
		  //check the status and set the button text accordingly
		  else {
		   if (mBluetoothAdapter.isEnabled()) {
			   Log.d(TAG, "BlueTooth is currently switched ON");
		   }else{ 
			   Log.d(TAG, "BlueTooth is currently switched OFF");
		   } 
		  }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			
			app.exportDB();
			
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		
		Log.i(TAG, "Activity.onStop");
				
		stopLocationListener();
		
		  WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE); 
		  if (wifiManager.isWifiEnabled()) {
			  wifiManager.setWifiEnabled(false);
			  setMobileDataEnabled(this, false);
		  }
		  else {
			  wifiManager.setWifiEnabled(true);
			  setMobileDataEnabled(this, true);
			  
		  }
		  
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		Log.i(TAG, "Activity.onPause");
		
		stopLocationListener();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		Log.i(TAG, "Activity.onResume");
		
		startLocationListener();
	}
	
	public void getCurrentGPS(View view) {
//		setGPSInfo(locMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER), true);
		if (locMgr != null) {
			locMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTimePeriod, minDistance, listener);
//			longTimeNoGPSHandler = new Handler();
//			longTimeNoGPSHandler.postDelayed(new Timer4StopGPS(), minTimePeriod);
		}
	}
	
	public void showGPSHistory(View view) {
		List<String> gpsList = dbm.getGPSList();
		if (gpsList != null && gpsList.size() > 0) {
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, gpsList);
			list.setAdapter(adapter);
		}
		
		  if (mBluetoothAdapter.isEnabled()) {
			    mBluetoothAdapter.disable(); 
			    Log.d(TAG, "BlueTooth is currently switched OFF");
			   }
			   //enable the bluetooth adapter
			   else{ 
			    mBluetoothAdapter.enable();
			    Log.d(TAG, "BlueTooth is currently switched ON");
			   }
		  
		  WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE); 
		  if (wifiManager.isWifiEnabled()) {
			  wifiManager.setWifiEnabled(false);
			  setMobileDataEnabled(this, false);
		  }
		  else {
			  wifiManager.setWifiEnabled(true);
			  setMobileDataEnabled(this, true);
			  
		  }
		  
	}
	
	private void setMobileDataEnabled(Context context, boolean enabled) {
		try {
	    final ConnectivityManager conman = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    final Class conmanClass = Class.forName(conman.getClass().getName());
	    final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
	    iConnectivityManagerField.setAccessible(true);
	    final Object iConnectivityManager = iConnectivityManagerField.get(conman);
	    final Class iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
	    final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
	    setMobileDataEnabledMethod.setAccessible(true);

	    setMobileDataEnabledMethod.invoke(iConnectivityManager, enabled);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void setGPSInfo(Location location, boolean isLastLocation) {
		Log.i(TAG, "setGPSInfo called!");
		
		try {
			if (location != null) {
				Date date = new Date(location.getTime());
				Calendar cal = Calendar.getInstance();
				cal.setTime(date);
				cal.add(Calendar.HOUR_OF_DAY, 8);
				date = cal.getTime();
				String lng = String.valueOf(location.getLongitude());
				String lat = String.valueOf(location.getLatitude());
				String dt = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault()).format(date);
				
				lng = lng.substring(0, Math.min(lng.indexOf(".") + 5, lng.length()));
				lat = lat.substring(0, Math.min(lat.indexOf(".") + 5, lat.length()));
				txt.setText(dt + ":" + lng + ", " + lat);
				
				if (!isLastLocation) {
					dbm.insertGPS(dt, lng, lat);
				}
			}
			else {
				Log.d(TAG, "location is null");
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
			Log.e(TAG, "Cannot get GPS information -> " + ex.getMessage());
		}
	}

	public void stopLocationListener() {
		Log.d(TAG, "stopLocationListener called");
		if (locMgr != null) {
			locMgr.removeUpdates(listener);
//			new Handler().postDelayed(new Timer4StartGPS(), maxTimePeriod);
		}
	}
	
	public void startLocationListener() {
		Log.d(TAG, "startLocationListener called");
		if (locMgr != null) {
			locMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTimePeriod, minDistance, listener);
//			locMgr.addGpsStatusListener(listener)
//			longTimeNoGPSHandler = new Handler();
//			longTimeNoGPSHandler.postDelayed(new Timer4StopGPS(), minTimePeriod);
		}
	}
	
	private final class MyLocationListener implements LocationListener {

		@Override
		public void onLocationChanged(Location location) {
			Log.d(TAG, "onLocationChanged called");
			if (location != null) {
				Log.d(TAG, "location is not null");
//				longTimeNoGPS = false;
				setGPSInfo(location, false);
			}
			else {
//				longTimeNoGPS = true;
				Log.d(TAG, "location is null");
			}
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			 switch (status) {
			    case LocationProvider.OUT_OF_SERVICE:
			        Log.v(TAG, "Status Changed: Out of Service");
			        Toast.makeText(MainActivity.this, "Status Changed: Out of Service",
			                Toast.LENGTH_SHORT).show();
			        break;
			    case LocationProvider.TEMPORARILY_UNAVAILABLE:
			        Log.v(TAG, "Status Changed: Temporarily Unavailable");
			        Toast.makeText(MainActivity.this, "Status Changed: Temporarily Unavailable",
			                Toast.LENGTH_SHORT).show();
			        break;
			    case LocationProvider.AVAILABLE:
			        Log.v(TAG, "Status Changed: Available");
			        Toast.makeText(MainActivity.this, "Status Changed: Available",
			                Toast.LENGTH_SHORT).show();
			        break;
			    }
		}

		@Override
		public void onProviderEnabled(String provider) { }

		@Override
		public void onProviderDisabled(String provider) { }
		
	}
	
//	private class Timer4StopGPS implements Runnable {
//
//		@Override
//		public void run() {
//			if (longTimeNoGPS) {
//				stopLocationListener();
//			}
//		}
//		
//	}
//	
//	private class Timer4StartGPS implements Runnable {
//
//		@Override
//		public void run() {
//			startLocationListener();
//		}
//		
//	}
	
//	protected void setLocation(Location location) {
//		
//		if (location != null) {
//			locationString = location.getLongitude() + "|" + location.getLatitude();
//		}
//		else {
//			locationString = "|";
//		}
//		setGPSInfo();
//	}
	
	protected String getLocation() {
		return locationString;
	}
	
	private String getCurrentDateTime() {
		Date current = new Date();
		return new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault()).format(current);
	}
	
	LocationManager locMgr;
	MyLocationListener listener;
	private String locationString = null;
	private int minDistance = 0;
	private int minTimePeriod = 1000 * 60;
	private int maxTimePeriod = 1000 * 60 * 3;
	
//	private boolean longTimeNoGPS = true;
//	private Handler longTimeNoGPSHandler;
	
	private ThisApplication app;
	private DBManager dbm;
	
	private TextView txt;
	private ListView list;
}
