package fac.userdelroot.sysinfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class StatInfoHelper {

	
	private static final String PROC = "/proc";
	private static final String MEM = PROC + "/meminfo";
	private static final String CPU = PROC + "/stat";

	
	private static final long starttime = System.currentTimeMillis();
	private static long endtime;
	

	private static int iProcCount;
	private static int iSwapUsage;
	private static int iMemUsage;
	private static int iCpuUsage;
	
	private static int iPrevTotal, iPrevIdle;
	
	
	
	public static void processStats() {

		Thread t = new Thread(new StatsThread());
		t.start();
	}
	
	
	public static class StatsThread implements Runnable {

		@Override
		public void run() {
			getProcesses();
			getMemStats();
			getCpuStats();
			doDisplayStats();
		}

		/**
		 * getProcesses get the process list from /proc/number each dir_number is a
		 * process running
		 */
		private static void getProcesses() {


			if (Log.DEBUG)
				Log.v("get processes");
			
			try {
				File dirs = new File(PROC);
				iProcCount = 0;
				for (String dir : dirs.list()) {
					if (!canParseInt(dir))
						continue;

					iProcCount++;
				}
			} catch (NullPointerException e) {
				if (Log.DEBUG)
					Log.v(e.getLocalizedMessage().toString());
			}

		}

		
		/**
		 * Get the memory stats from /proc/meminfo
		 * Parses the meminfo file for the memory information
		 */
		private static void getMemStats() {

			if (Log.DEBUG)
				Log.v("Getting memory stats");
			
			
			HashMap<String,String> map = new HashMap<String,String>();
			File memstats = null;
			FileReader freader = null;
			BufferedReader buf = null;

			try {
				memstats = new File(MEM);
				freader = new FileReader(memstats);
				buf = new BufferedReader(freader, 1024);
				String line = null;
				String[] list = null;
				while ((line = buf.readLine()) != null) {
					list = line.replaceAll(":","").replaceAll("\\s+", " ").split(" ");
					map.put(list[0].toLowerCase(), list[1]);
				}
			} catch (FileNotFoundException e) { // FileReader 
				
				if (Log.DEBUG)
					Log.v("File not found" + e.getLocalizedMessage().toString());
			} catch (IOException e) {  // BufferedReader.readList()
				
				if (Log.DEBUG)
					Log.v(e.getLocalizedMessage().toString());
			} catch (NullPointerException e) {  // File();
				
				if (Log.DEBUG)
					Log.v(e.getLocalizedMessage().toString());
			} finally {

				try {

					if (Log.DEBUG)
						Log.v("closing filehandlers");
					
					freader.close();
					buf.close();

				} catch (IOException e) {
					if (Log.DEBUG)
						Log.v(e.getLocalizedMessage().toString());
				}
			}

			
			
			try { 
				int memtotal = Integer.valueOf(map.get("memtotal"));
				int memfree = Integer.valueOf(map.get("memfree"));
				int swaptotal = Integer.valueOf(map.get("swaptotal"));
				int swapfree = Integer.valueOf(map.get("swapfree"));
				
				if (memtotal > 0)
					iMemUsage =  (1000 * (memtotal - memfree ) / memtotal + 5) / 10;
				else
					iMemUsage = 0;
				
				if (swaptotal > 0)
					iSwapUsage = (1000 * (swaptotal - swapfree ) / swaptotal + 5) / 10;
				else 
					iSwapUsage = 0;
			}
			catch (NumberFormatException e) {
				if (Log.DEBUG)
					Log.v(e.getLocalizedMessage().toString());
			}
		}

		
		/**
		 * Get the cpu info from /proc/stat
		 * This is a bit more trickier.  We have to get the data, then sleep for a second,
		 * then get the data again to get correct results..
		 * TODO: The main widget with store the prev_pass values and will use those to do the calculations
		 * 	instead of double humping the file system here
		 */
		private static void getCpuStats() {
		
			
			File cpustats = null;
			FileReader freader = null;
			BufferedReader buf = null;

			try {
				cpustats = new File(CPU);
				freader = new FileReader(cpustats);
				buf = new BufferedReader(freader,1024);
				String line = null;
				String[] newLine = null; // check 1 
				while ((line = buf.readLine()) != null) {
					// line 1 is only line we need
					newLine = line.replaceAll(":","").replaceAll("\\s+", " ").split(" ");
					break; // we go no further because we don't need anymore.
				}
			
				int idle = stringToInt(newLine[4]); // pass 1 
				
				int sumtotal = 0;
				
				int i = 0;
				for (String s : newLine) {

					if (i > 0)
						sumtotal += stringToInt(s);
				
					i++;
				}
				
				int diffIdle = idle - iPrevIdle;
				int diffTotal = sumtotal - iPrevTotal;
				
				if (diffTotal > 0)
					iCpuUsage = (1000 * (diffTotal - diffIdle ) / diffTotal + 5) / 10;
				else 
					iCpuUsage = 0;
				
				// store off new values to prev values
				iPrevIdle = idle;
				iPrevTotal = sumtotal;
					
			} catch (FileNotFoundException e) { // FileReader 
				
				if (Log.DEBUG)
					Log.v("File not found" + e.getLocalizedMessage().toString());
			} catch (IOException e) {  // BufferedReader.readList()
				
				if (Log.DEBUG)
					Log.v(e.getLocalizedMessage().toString());
			} catch (NullPointerException e) {  // File();
				
				if (Log.DEBUG)
					Log.v(e.getLocalizedMessage().toString());
				
			} catch (NumberFormatException e)  {
				if (Log.DEBUG)
					Log.v(e.getLocalizedMessage().toString());
			}
			finally {

				try {

					if (Log.DEBUG)
						Log.v("closing filehandlers");
					
					freader.close();
					buf.close();

				} catch (IOException e) {
					if (Log.DEBUG)
						Log.v(e.getLocalizedMessage().toString());
				}
			}		
			
			
		}

		
		private static int stringToInt(String in) {
			
			int val = 0;
			
			try {
				val = Integer.valueOf(in);
			} 
			catch (NumberFormatException e) {
				return 0;
			}
			return val;
		}

	}

	/**
	 * Java does not provide a canParseInt(string) so we made our own. 
	 * @param parse
	 * @return
	 */
	private static final boolean canParseInt(String parse) {

		try {
			Integer.valueOf(parse);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private static void doDisplayStats() {
		
		endtime = System.currentTimeMillis() - starttime;

		InfoProvider.doUpdateUI(iCpuUsage, iMemUsage, iProcCount, iSwapUsage);
		if (Log.DEBUG) {
			Log.i("\n\tCpu Usage: " + iCpuUsage + "%\n\tMemory Usage: " + iMemUsage + "%\n\tSwap Usage: " + iSwapUsage + "%\n\tProcesses " + iProcCount + "\n");
			Log.i("\nCompleted in " + endtime + "ms\n");
			Log.i("\nPrevIdle " + iPrevIdle + " PrevTotal " + iPrevTotal + "\n");
		}
	}	
	
}
