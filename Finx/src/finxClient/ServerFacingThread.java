package finxClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServerFacingThread extends Thread{

	private Socket myClient;
	private String serverAddress = "127.0.0.1";
	private PrintStream protocolOutput;
	private BufferedReader protocolInput;
	private InputStreamReader protocolInputReader;
	private InetAddress ip;
	private StringBuilder MACaddr; 
	private SimpleDateFormat dateFormat;
	private Date dateTime;
	
	
	public ServerFacingThread() {
		connect_to_Server();
		setOutputStream();
		setInputStream();
		start();
	}
	
	public void run() {
		authenticate();
		receiveLastPushTime();
		
	}
	
	public void receiveLastPushTime() {
		try {
			String dateTimeString = protocolInput.readLine();
			dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			dateTime = dateFormat.parse(dateTimeString);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}	
	}
	
	public void authenticate() {
		/* Client sends his/her MAC address,
		 * and receives an authentication message from the Server */
		protocolOutput.println(getMACAddress());
		try {
			if(!protocolInput.readLine().equals("Authenticated")) {
				// dont need to worry about this yet
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void connect_to_Server() {
		try {
	           myClient = new Socket(serverAddress, 9396);
	    }
	    catch (IOException e) {
	        System.out.println(e);
	    }
	}
	
	public void setOutputStream() {
		try {
			protocolOutput = new PrintStream(myClient.getOutputStream());
	    }
	    catch (IOException e) {
	        System.out.println(e);
	    }
	}
	
	public void setInputStream() {
		try {
			protocolInputReader = new InputStreamReader(myClient.getInputStream());
			protocolInput = new BufferedReader(protocolInputReader);
	    }
	    catch (IOException e) {
	        System.out.println(e);
	    }
	}
	
	
	public String getMACAddress() {
		try {
			ip = InetAddress.getLocalHost();
			NetworkInterface network = NetworkInterface.getByInetAddress(ip);
			byte[] mac = network.getHardwareAddress();
	 
			MACaddr = new StringBuilder();
			for (int i = 0; i < mac.length; i++) {
				MACaddr.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "" : ""));		
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SocketException e){
			e.printStackTrace();
		}
		return MACaddr.toString();
	}
	
	
	
}
	

