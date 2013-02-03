package finxClient;


import java.awt.AWTException;
import java.awt.SystemTray;
import java.io.File;
import java.io.IOException;

import net.contentobjects.jnotify.*;

public class FinxController {
	
	private static TrayApplication trayApp;
	private static SetupFinxEnv setup;
	private static SystemTray sysTray;

	public static void main(String[] args) {
		
		/* Set up the application environment here - may want to move this to a setup
		program eventually */
		
		setup = new SetupFinxEnv();
		trayApp = new TrayApplication(setup.get_folder_path());
		sysTray = SystemTray.getSystemTray();
		add_to_tray();
		ServerFacingThread serverFacingThread = new ServerFacingThread();
		keep_watching_FinxFolder();
		
		/* Do not place any code in this function beyond this point as it is unreachable */
		
	}
	
	public static void keep_watching_FinxFolder() {
		try {
			watch_folder();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void add_to_tray() {
		try {
			sysTray.add(trayApp.getTrayIcon());
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}
	
	//JNotify Stuff
	
	public static void watch_folder() throws Exception{
	    // watch mask, specify events you care about,
	    // or JNotify.FILE_ANY for all events.
	    int mask = JNotify.FILE_CREATED  | 
	               JNotify.FILE_DELETED  | 
	               JNotify.FILE_MODIFIED | 
	               JNotify.FILE_RENAMED;

	    // watch subtree?
	    boolean watchSubtree = true;

	    // add actual watch
	    int watchID = JNotify.addWatch(setup.get_folder_path(), mask, watchSubtree, new FinxListener(trayApp));

	    // sleep a little, the application will exit if you
	    // don't (watching is asynchronous), depending on your
	    // application, this may not be required
	    Thread.sleep(1000000);
	    // to remove watch the watch
	    boolean res = JNotify.removeWatch(watchID);
	    if (!res) {
	       //invalid watch ID specified.
	    }
	  }
	

}
