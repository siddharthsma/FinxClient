package finxClient;

import java.awt.Desktop;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class TrayApplication {

	private TrayIcon finx_tray_icon;
	private String finx_folder_path;
	
	public TrayApplication(String finx_folder_path) {
		this.finx_folder_path = finx_folder_path;
		set_up_TrayIcon();
		populate_TrayIcon_menu();
	}
	
	public void set_up_TrayIcon() {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		String finx_icon_url = "Assets/finxicon.png";
		Image finx_icon_image = toolkit.getImage(finx_icon_url);
		finx_tray_icon = new TrayIcon(finx_icon_image);
	}
	
	public void populate_TrayIcon_menu() {
		PopupMenu popup = new PopupMenu();
		//set up menu items and action listeners
		MenuItem item_finx_folder = new MenuItem("Open Finx Folder");
		item_finx_folder.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Desktop desktop = Desktop.getDesktop();
				try {
					desktop.open(new File(finx_folder_path));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		MenuItem item_about = new MenuItem("About");
		MenuItem item_exit = new MenuItem("Quit Finx");
		item_exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});
		popup.add(item_finx_folder);
		popup.add(item_exit);
		finx_tray_icon.setPopupMenu(popup);
	}

	public TrayIcon getTrayIcon() {
		// should return the TrayIcon - now fully configured
		return finx_tray_icon;
	}
}
