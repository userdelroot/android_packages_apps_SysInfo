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
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class InfoProvider extends AppWidgetProvider {

	private static final String TAG = "InfoProvider ";
	private static Context mContext;
	
	
	private static final String ID_PREFIX = "widgetid_";
	private static final String CUSTOM_INTENT = "fac.userdelroot.sysinfo.action.WIDGET_UPDATE";
	private static int iProcCount;
	private static int iSwapUsage;
	private static int iMemUsage;
	private static int iCpuUsage;	
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {

		for (int id : appWidgetIds) {
			InfoConfigure.deleteWidget(context, id);
		}
		
		super.onDeleted(context, appWidgetIds);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		
		for (int id : appWidgetIds) {
			updateWidget(context, appWidgetManager, id);
			
		}
	}
	
	@Override
	public void onEnabled(Context context) {
		
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		ComponentName appWidget = new ComponentName(context, InfoProvider.class);
		int[] widgetIds = appWidgetManager.getAppWidgetIds(appWidget);
		
		for (int id : widgetIds) {
			HashMap<String, Integer> map = InfoConfigure.getPreferences(context, id);
			if (map == null)
				continue;
			
			if (map.isEmpty())
				continue;
			
			InfoConfigure.setRefreshAlarm(context, id);
		}
	}
	
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		String action = intent.getAction();
		if (action.equals(CUSTOM_INTENT)) {
			int widgetId = intent.getIntExtra(ID_PREFIX, -1);
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			updateWidget(context, appWidgetManager, widgetId);
			if (Log.DEBUG)
				Log.v(TAG + "UpdateWidget Alarm Intent widgetId " + widgetId);
		}
			
		
		super.onReceive(context, intent);
	}

	static void updateWidget(Context c, AppWidgetManager appWidgetManager,
			int widgetId) {
		
		if (Log.DEBUG) 
			Log.v(TAG + "updateWidget");
		

		// no need to go further if invalid widgetId
		if (widgetId <= 0)
			return;
		
		
		HashMap<String, Integer> prefsMap = InfoConfigure.getPreferences(c,
				widgetId);
		
		
		if (prefsMap == null) {
			if (Log.DEBUG)
				Log.e(TAG + "prefsMap null");
			return;
		}
		
		if (prefsMap.isEmpty()) {
		
			if (Log.DEBUG)
				Log.e(TAG + "prefsMap empty");
			return;
		}
		
		
		mContext = c;
		StatInfoHelper.processStats();
	}
	
	protected static void doUpdateUI(int cpuusage , int memusage , int proccount, int swapusage) {

		iCpuUsage = cpuusage;
		iMemUsage = memusage;
		iProcCount = proccount;
		iSwapUsage = swapusage;		

		try {
			
			AppWidgetManager appWidgetManager = AppWidgetManager
					.getInstance(mContext);
			ComponentName appWidget = new ComponentName(mContext,
					InfoProvider.class);
			int[] widgetIds = appWidgetManager.getAppWidgetIds(appWidget);

			for (int widgetId : widgetIds) {
				updateUI(mContext, appWidgetManager, widgetId);

			}
		} catch (RuntimeException e) {

		}
	}

	private static void updateUI(Context c, AppWidgetManager appWidgetManager,
			int widgetId) {

		try {
			HashMap<String, Integer> prefsMap = InfoConfigure.getPreferences(c,
					widgetId);
			// this should not happen as the HashMap is created immediately in
			// the method
			if (prefsMap == null)
				return;

			int errVal = -1;
			// empty prefs or invalid prefs
			if (prefsMap.isEmpty() || prefsMap.containsValue(errVal))
				return;

			if (Log.DEBUG)
				Log.i(TAG + "prefsMap " + prefsMap.toString());

			String format = mContext.getString(R.string.format_string);

			if (format == null)
				return;

			
			RemoteViews views = new RemoteViews(mContext.getPackageName(),
					R.layout.info_widget);

			views.setTextViewText(R.id.cpu_usage, String.format(format, iCpuUsage + "%"));
			views.setTextViewText(R.id.mem_usage, String.format(format, iMemUsage + "%"));
			views.setTextViewText(R.id.process_usage, String.format(format, iProcCount));
			views.setTextViewText(R.id.swap_usage, String.format(format, (iSwapUsage > 0 ) ? iSwapUsage + "%" : "n/a" ));

			
			appWidgetManager.updateAppWidget(widgetId, views);
		} catch (RuntimeException e) {

			// NOTE: should we delete ourselfs?
			if (Log.DEBUG)
				Log.e(TAG + "RemoteViews err", e);
		}

	}
	
}
