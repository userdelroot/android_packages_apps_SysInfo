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

/**
 * 
 * Helper class for display Log messages for debugging
 * @author userdelroot
 * 
 */
public class Log {

	// Name of AppWidget(s)
	public final static String LOGTAG = "SysInfo";

	// Used throughout the code to log info true 
	// unset on release version
	static final boolean DEBUG = false;

	/**
	 * Log Verbose
	 * @param logMe
	 */
	static final void v(String logMe) {
		android.util.Log.v(LOGTAG, logMe);
	}

	/**
	 * Log Error
	 * @param logMe
	 */
	static final void e(String logMe) {
		android.util.Log.e(LOGTAG, logMe);
	}

	/**
	 * Log Error with exception
	 * @param logMe
	 * @param ex
	 */
	static final void e(String logMe, Exception ex) {
		android.util.Log.e(LOGTAG, logMe, ex);
	}

	/**
	 * Log Warning
	 * @param logMe
	 */
	static final void w(String logMe) {
		android.util.Log.w(LOGTAG, logMe);
	}
	
	
	/**
	 * Log info
	 * @param logMe
	 */
	static final void i(String logMe) {
		android.util.Log.i(LOGTAG, logMe);
	}
}
