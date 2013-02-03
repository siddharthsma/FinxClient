package finxClient;

import net.contentobjects.jnotify.JNotifyListener;

public class FinxListener implements JNotifyListener{
	
	private TrayApplication trayApp;
	private String notification_type;

	public FinxListener(TrayApplication trayApp) {
		super();
		this.trayApp = trayApp;
	}
	
	public void fileRenamed(int wd, String rootPath, String oldName,
			String newName) {
		notification_type = "RENAME";
		Notifier.generateNotification(notification_type, rootPath, oldName, newName);
	}

	public void fileModified(int wd, String rootPath, String name) {
		notification_type = "MODIFIED";
		Notifier.generateNotification(notification_type, rootPath, name, name);
	}

	public void fileDeleted(int wd, String rootPath, String name) {
		notification_type = "DELETED";
		Notifier.generateNotification(notification_type, rootPath, name, name);
	}

	public void fileCreated(int wd, String rootPath, String name) {
		notification_type = "CREATED";
		Notifier.generateNotification(notification_type, rootPath, name, name);
	}

	void print(String msg) {
		System.err.println(msg);
	}
	
}
