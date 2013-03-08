package finxClient;

import java.io.File;
import java.io.IOException;

import net.contentobjects.jnotify.JNotifyListener;

public class FinxListener implements JNotifyListener{
	
	private String notification_type;
	private ServerFacingThread serverFacingThread;

	public FinxListener(TrayApplication trayApp, ServerFacingThread serverFacingThread) {
		super();
		this.serverFacingThread = serverFacingThread;
	}
	
	public void fileRenamed(int wd, String rootPath, String oldName,
			String newName) {
		notification_type = "RENAME";
		Notifier.generateNotification(notification_type, rootPath, oldName, newName);
	}

	public void fileModified(int wd, String rootPath, String name) {
		notification_type = "MODIFIED";
		String filePath = rootPath + name;
		
		if (!serverFacingThread.fetch_map.contains(filePath)) {
			try {
				serverFacingThread.sendFile(filePath);
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}
		Notifier.generateNotification(notification_type, rootPath, name, name);
	}

	public void fileDeleted(int wd, String rootPath, String name) {
		notification_type = "DELETED";
		Notifier.generateNotification(notification_type, rootPath, name, name);
	}

	public void fileCreated(int wd, String rootPath, String name) {
		notification_type = "CREATED";
		String filePath = rootPath + name;
		System.out.println("The listener path is: " + filePath);
		File theFile = new File(rootPath + name);
		if (theFile.isFile()) {
			if (!serverFacingThread.fetch_map.contains(filePath)) {
				try {
					serverFacingThread.sendFile(filePath);
				} catch (IOException e) {
					e.printStackTrace();
				}	
			}
		}
		
		
		Notifier.generateNotification(notification_type, rootPath, name, name);
	}

	void print(String msg) {
		System.err.println(msg);
	}
	
}
