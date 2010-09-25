/**
 *  SysInfo Widget 
 * 
 * Copyright (C) 2010  userdelroot r00t316@gmail.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package fac.userdelroot.sysinfo;

import java.util.HashMap;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Spinner;
import android.widget.Toast;

public class InfoConfigure extends Activity {
	
	private static final String TAG = "InfoConfigure ";
	private static final String PREFS_NAME = "fac.userdelroot.sysinfo.InfoProvider";
	private static Spinner mRefresh;
	private static final String REFRESH_PREFIX = "refresh_rate_";
	private static final String ID_PREFIX = "widgetid_";
	private int mWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	private static final String CUSTOM_INTENT = "fac.userdelroot.sysinfo.action.WIDGET_UPDATE";
	 
	
	/**
	 * Constructor
	 */
	public InfoConfigure() {
		
	}
	 
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		// Cancel out on Back button
		setResult(RESULT_CANCELED);
		
		setContentView(R.layout.info_configure);
		mRefresh = (Spinner) findViewById(R.id.opt_refresh_rates);
		
		if (mRefresh == null) 
			return;
		
		Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        if (Log.DEBUG)
        	Log.v(TAG + "mWidgetId " + mWidgetId);
        // If they gave us an intent without the widget id, just bail.
        if (mWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

	}

	
	public void applyHandler(View v) {
		
		if (!savePreferences()) {
			Toast.makeText(this, R.string.create_widget_err, Toast.LENGTH_LONG).show();
			if (Log.DEBUG)
				Log.e(TAG + "Error saving widgetId " + mWidgetId);
			
			finish();
			return;
		}
		
		setRefreshAlarm(this, mWidgetId);
		
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
		InfoProvider.updateWidget(this, appWidgetManager, mWidgetId);
		Intent resultValue = new Intent();
		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetId);
        setResult(RESULT_OK, resultValue);
        
		finish();
		
	}
	
	private boolean savePreferences() {
		
		if (Log.DEBUG)
			Log.v(TAG + "savePreferences");
		
		try {
			int refresh = (int) mRefresh.getSelectedItemId();
			SharedPreferences.Editor prefs = this.getSharedPreferences(PREFS_NAME, 0).edit();
			prefs.putInt(REFRESH_PREFIX + mWidgetId, refresh);
			prefs.putInt(ID_PREFIX + mWidgetId, mWidgetId);
			prefs.commit();
			
		}
		catch (RuntimeException e) {
			if (Log.DEBUG)
				Log.e(TAG + "savePreferences", e);
			return false;
		}
		
		
		return true;
	}
	
	static void deleteWidget(Context c, int widgetId) {
		if (Log.DEBUG)
			Log.v(TAG + "widgetId " + widgetId);
		try {
			SharedPreferences.Editor prefs = c.getSharedPreferences(PREFS_NAME, 0).edit();
			prefs.remove(REFRESH_PREFIX + widgetId);
			prefs.remove(ID_PREFIX + widgetId);
			prefs.commit();
			
			Intent widgetUpdate = new Intent();
			widgetUpdate.setAction(CUSTOM_INTENT);
			widgetUpdate.putExtra(ID_PREFIX, widgetId);
			PendingIntent pendingintent = PendingIntent.getBroadcast(c.getApplicationContext(), 0, widgetUpdate, PendingIntent.FLAG_UPDATE_CURRENT);
			AlarmManager alarm = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
		
			// if there is one cancel it
			alarm.cancel(pendingintent);
		}
		catch (RuntimeException e) {
			if (Log.DEBUG)
				Log.e(TAG + "deletePreferences", e);
		}
	}
	
	static HashMap<String, Integer> getPreferences(Context c, int widgetId) {
		
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		
		try {
			SharedPreferences prefs = c.getSharedPreferences(PREFS_NAME, 0);
			int refresh = prefs.getInt(REFRESH_PREFIX + widgetId, -1);
			int id = prefs.getInt(ID_PREFIX + widgetId, -1);
			
			// if either of these -1 we are invalid bail
			if ( id <= 0)
				return null;
			
			map.put(REFRESH_PREFIX, refresh);
			map.put(ID_PREFIX, id);
		} 
		catch (RuntimeException e) {
			if (Log.DEBUG)
				Log.e(TAG + "getPreferences",e);
			return null;
		}
		
		return map;
	}
	
	static void setRefreshAlarm(Context c, int widgetId) {
		
		int refresh = 0;
		// if widgetId is invalid bail out
		if (widgetId <= 0) 
			return;
		
		try {
			SharedPreferences prefs = c.getSharedPreferences(PREFS_NAME, 0);
			refresh = prefs.getInt(REFRESH_PREFIX + widgetId, -1);
			
			// bail if we don't have anything
			if (refresh < 0)
				return;
			
			if (refresh == 0) 
				refresh = 3;
			
			Intent widgetUpdate = new Intent();
			widgetUpdate.setAction(CUSTOM_INTENT);
			widgetUpdate.putExtra(ID_PREFIX, widgetId);
			PendingIntent pendingintent = PendingIntent.getBroadcast(c.getApplicationContext(), 0, widgetUpdate, PendingIntent.FLAG_UPDATE_CURRENT);
			AlarmManager alarm = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
		
			// if there is one cancel it
			alarm.cancel(pendingintent);
			
			alarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + (refresh * 5 * 1000), refresh * 1000 * 5, pendingintent);
			
			
		}
		catch (RuntimeException e) {
			
		}
	}
	
	
}
