package finxClient;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.Socket;

public class FileWorker extends Thread{
	
	String filePath;
	Socket socket;

	public FileWorker(String filePath, Socket socket) {
		this.socket = socket;
		System.out.println("In FileWorker with: " + filePath);
		start();
	}
	
	public void run() {
		try {
			sendFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}
	
	public void sendFile() throws Exception{
		
	}
}
