package honours.pserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ConnectionHandler implements Runnable{

	public ConnectionHandler() {
	}

	@Override
	public void run() {
		ServerSocket serverSocket;
		try {
			serverSocket = new ServerSocket(6636);
			while(true){
				Socket clientSocket = serverSocket.accept();
				ActiveConnection connection = new ActiveConnection(clientSocket);
				Thread connectionThread = new Thread(connection);
				connectionThread.start();
			}
		} catch (IOException e) {
			System.out.println("IO error when trying to accept connections: " + e.getMessage());
		}
		
	}

}
