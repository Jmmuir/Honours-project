package honours.pserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

	public Main() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ConnectionHandler connectionHandler = new ConnectionHandler();
		Thread connectionHolderThread = new Thread(connectionHandler);
		connectionHolderThread.start();
		setUpFolder();
		try {
			startConsole();
		} catch (IOException e) {
			System.out.println("IOException caught:" + e.getMessage());
		}
	}
	
	private static void startConsole()throws IOException{
		String inputString = "";
		String command;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		do{
			inputString = br.readLine();
			String[] input = inputString.split(" ", 2);
			command = input[0].toLowerCase();
			if(command.equals("threads")){
				System.out.println("open threads: " + Thread.activeCount());
			}
			else{
				System.out.println(command + "!!!");
			}
		}while(!command.equals("quit"));
		System.out.println("Stopping main thread.");
	}
	
	private static void setUpFolder(){
		String fileSeparator = System.getProperty("file.separator");
		File workingDirectory = new File(System.getProperty("user.dir") + fileSeparator + "HonoursPrivateServer");
		System.out.println("files are being stored in: " + workingDirectory.getAbsolutePath());
		if(workingDirectory.mkdir()){
			System.out.println("Created new directory.");
		}
		workingDirectory = new File(workingDirectory.getAbsolutePath() + fileSeparator + "Trash");
		workingDirectory.mkdir();
	}

}
