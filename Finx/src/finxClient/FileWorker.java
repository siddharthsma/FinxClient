package finxClient;

public class FileWorker extends Thread{
	
	String filePath;

	public FileWorker(String filePath) {
		System.out.println("In FileWorker with: " + filePath);
		start();
	}
	
	public void run() {
		/*System.out.println("Path is: " + myFilePath);
		File myFile = new File (myFilePath);
		
		sendTell("push " + myFile.getName());
		
		byte [] mybytearray  = new byte [(int)myFile.length()];
		FileInputStream fis = new FileInputStream(myFile);
		BufferedInputStream bis = new BufferedInputStream(fis);
		bis.read(mybytearray,0,mybytearray.length);
		OutputStream os = myClient.getOutputStream();
		System.out.println("Sending...");
		os.write(mybytearray,0,mybytearray.length);
		os.flush();
		//myClient.close();
		System.out.println("Sent ! Or so it should be ...");*/
	}
}
