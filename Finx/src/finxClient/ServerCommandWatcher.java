package finxClient;

import java.io.IOException;

public class ServerCommandWatcher extends Thread{

	private ServerFacingThread clientThread;
	
	public ServerCommandWatcher(ServerFacingThread clientThread) {
		this.clientThread = clientThread;
		start();
	}
	
	public void run() {
		while(true) {
			waitForCommands();
		}
	}
	
	public void waitForCommands() {
		/* The idea is that the Server is Passive, which means that it will wait
		 * for commands to be sent by the Client via protocol messages and then react
		 * accordingly.
		 */
		String command = null;
		try {
			command = clientThread.protocolInput.readLine();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		System.out.println("The command issued is: " + command);

		if (command.startsWith("fetchrequest")) {
			String[] info = command.split("#");
			try {
				clientThread.dealWithFetchRequest(info[1]);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {

		}
	}
}
