package finxClient;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

public class SetupFinxEnv {
	
	private String finx_folder_path;
	
	public SetupFinxEnv() {
		finx_folder_path = set_folder_path_depending_on_OS();
		copy_base_FinxFolder();
	}

	public String set_folder_path_depending_on_OS() {
		String folder_path = "/";
		if (isWindows()) {
			//some path - gotta find out
		} 
		else if (isMac()) {
			folder_path = "/Users/sameerambegaonkar/Desktop/FinxFolder/";
		}
		return folder_path;
	}
	
	
	public void copy_base_FinxFolder() {
		File finx_folder = new File(finx_folder_path);
		// if the FinxFolder does not already exist where at the correct path
		if (!finx_folder.exists()) {
			if (isWindows()) {
				//some batch file thing
			}
			else if (isMac()) {
				// run applescript
					try {
						Desktop.getDesktop().open(new File("finxcopybase.app"));
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		}
	}
	
	public void create_log_file() {
		
	}
	
	
	public boolean isWindows() {
		return (System.getProperty("os.name").indexOf("Win") >= 0);
	}
 
	public boolean isMac() {
		return (System.getProperty("os.name").indexOf("Mac") >= 0);
	}
	
	public String get_folder_path() {
		return finx_folder_path;
	}
	
}
