package finxClient;

import javax.swing.ImageIcon;

import ch.swingfx.twinkle.NotificationBuilder;
import ch.swingfx.twinkle.event.NotificationEvent;
import ch.swingfx.twinkle.event.NotificationEventAdapter;
import ch.swingfx.twinkle.style.INotificationStyle;
import ch.swingfx.twinkle.style.theme.DarkDefaultNotification;
import ch.swingfx.twinkle.window.Positions;

public class Notifier {
	
	private static String message;
	
	public static void generateNotification(String notification_type, String rootPath, String oldName, String newName) {
		// AA the text
		System.setProperty("swing.aatext", "true");

		// First we define the style/theme of the window.
		// Note how we override the default values
		INotificationStyle style = new DarkDefaultNotification()
				.withWidth(400) // Optional
				.withAlpha(0.9f) // Optional
		;

		// Now set the Message to display
		setMessage(notification_type, rootPath, oldName, newName);
		
		// Now lets build the notification
		new NotificationBuilder()
				.withStyle(style) // Required. here we set the previously set style
				.withTitle(notification_type) // Required.
				.withMessage(message) // Optional
				.withIcon(new ImageIcon("Assets/finxiconnotify.png")) // Optional. You could also use a String path
				.withDisplayTime(5000) // Optional
				.withPosition(Positions.NORTH_EAST) // Optional. Show it at the center of the screen
				.withListener(new NotificationEventAdapter() { // Optional
					public void closed(NotificationEvent event) {
						System.out.println("closed notification with UUID " + event.getId());
					}

					public void clicked(NotificationEvent event) {
						System.out.println("clicked notification with UUID " + event.getId());
					}
				})
				.showNotification(); // this returns a UUID that you can use to identify events on the listener

	}
	
	public static void setMessage(String type, String rootPath, String oldName, String newName) {
		if (type.equals("RENAME")) {
			message = "renamed" + ": " + oldName + " -> " + newName;
		}
		else if (type.equals("MODIFIED")) {
			message = "modified" + ": " + newName;
		}
		else if (type.equals("DELETED")) {
			message = "deleted" + ": " + newName;
		}
		else if (type.equals("CREATED")) {
			message = "created" + ": " + newName;
		}
	}

}
