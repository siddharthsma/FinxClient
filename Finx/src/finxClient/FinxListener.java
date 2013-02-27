package finxClient;

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
		try {
			serverFacingThread.sendFile(rootPath + "/" + name);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Notifier.generateNotification(notification_type, rootPath, name, name);
	}

	public void fileDeleted(int wd, String rootPath, String name) {
		notification_type = "DELETED";
		Notifier.generateNotification(notification_type, rootPath, name, name);
	}

	public void fileCreated(int wd, String rootPath, String name) {
		notification_type = "CREATED";
		try {
			serverFacingThread.sendFile(rootPath + "/" + name);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Notifier.generateNotification(notification_type, rootPath, name, name);
	}

	void print(String msg) {
		System.err.println(msg);
	}
	
}
