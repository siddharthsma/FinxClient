package finxClient;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;

public class FileTreeWalker extends SimpleFileVisitor<Path>{

	private Date modDateofFile;
	private Date lastPushDate;
	private ServerFacingThread myServerThread;
	
	public FileTreeWalker(Date lastPushDate, ServerFacingThread myServerThread) {
		this.myServerThread = myServerThread;
		this.lastPushDate = lastPushDate;
		
	}
	
	public FileVisitResult visitFile(Path filePath, BasicFileAttributes attr) {
		
		modDateofFile = new Date(attr.lastModifiedTime().toMillis()); 
		if (modDateofFile.after(lastPushDate)) {
			// need to push that file to the server.
			try {
				File aFile = new File(filePath.toString());
				if (aFile.length() > 0) {
					myServerThread.sendFile(filePath.toString());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Past sendFile");
		}
		return FileVisitResult.CONTINUE;
	}
	
}
