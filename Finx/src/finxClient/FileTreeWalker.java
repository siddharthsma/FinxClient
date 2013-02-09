package finxClient;

import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileTreeWalker extends SimpleFileVisitor<Path>{

	private SimpleDateFormat dateFormat;
	private Date modDateofFile;
	private Date lastPushDate;
	private ServerFacingThread myServerThread;
	
	public FileTreeWalker(Date lastPushDate, ServerFacingThread myServerThread) {
		this.myServerThread = myServerThread;
		this.lastPushDate = lastPushDate;
		dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
	}
	
	public FileVisitResult visitFile(Path filePath, BasicFileAttributes attr) {
		
		modDateofFile = new Date(attr.lastModifiedTime().toMillis()); 
		if (modDateofFile.after(lastPushDate)) {
			// need to push that file to the server.
			myServerThread.sendFile(filePath.toFile());

		}
		return FileVisitResult.CONTINUE;
	}
	
}
