package honours.pserver;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ActiveConnection implements Runnable{

	private Socket socket;
	private String fileSeperator = System.getProperty("file.separator");
	private String baseDirectory = System.getProperty("user.dir") + fileSeperator + "HonoursPrivateServer";
	private String currentDirectory = baseDirectory.toString();


	public ActiveConnection(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		try(
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				)
				{
			System.out.println("created thread with ID " + Thread.currentThread().getId() + " for new connection.");
			String inputLine;
			String command;
			while((inputLine = in.readLine()) != null){
				DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
				Date date = new Date();
				String defaultMessage = "[" + dateFormat.format(date) + "]" + "Thread: " + Thread.currentThread().getId() + " ";
				String[] input = inputLine.split(" ", 2);
				command = input[0].toLowerCase();
				switch(command){
				case("+"):
					System.out.println(defaultMessage + "sent message: " + input[1]);
				out.println("recieved");
				break;
				case("download"):
					System.out.println(defaultMessage + "executed command: download");
				sendFile(input[1], out);
				break;
				case("downloadft"):
					System.out.println(defaultMessage + "executed command: download");
				sendFileFromFull(input[1], out);
				break;
				case("upload"):
					System.out.println(defaultMessage + "executed command: upload");
				receiveFile(out, input[1]);
				break;
				case("getfiles"):
					System.out.println(defaultMessage + "executed command: getfiles");
				getFiles(out);
				break;
				case("getfilesafv"):
					System.out.println(defaultMessage + "executed command: getfiles (for graphical interface)");
				getFilesAsFileView(out);
				break;
				case("makefolder"):
					System.out.println(defaultMessage + "executed command: makefolder " + input[1]);
				makeFolder(out, input[1]);
				break;
				case("cd"):
					System.out.println(defaultMessage + "executed command: changedirectory " + input[1]);
				changeDirectory(out, input[1]);
				break;
				case("delete"):
					System.out.println(defaultMessage + "executed command: delete ");
				deleteFile(out, input[1]);
				break;
				case("getdirectory"):
					System.out.println(defaultMessage + "executed command: get directory ");
				getCurrentDirectory(out);
				break;
				case("rename"):
					System.out.println(defaultMessage + "executed command: rename " + input[1]);
				renameFile(out, input[1]);
				break;
				case("move"):
					System.out.println(defaultMessage + "executed command: move " + input[1]);
				moveFile(out, input[1]);
				break;
				case("goto"):
					System.out.println(defaultMessage + "executed command: go to " + input[1]);
				goToDir(out, input[1]);
				break;
				default:
					System.out.println(inputLine);
					break;
				}
				out.flush();
			}
				}
		catch(IOException e){
			System.out.println("Exception in thread: " + Thread.currentThread().getId() + ". " + e.getMessage());
		}
		finally{
			System.out.println("Connection closed on thread: " + Thread.currentThread().getId() + ". ");
		}

	}

	private void sendFile(String fileName, PrintWriter stringOut) throws IOException{
		OutputStream bytesOut = socket.getOutputStream();
		File fileToSend = new File(currentDirectory + fileSeperator + fileName);
		try(
				FileInputStream fis = new FileInputStream(fileToSend);
				BufferedInputStream bis = new BufferedInputStream(fis);
				){
			if(!fileToSend.exists()){
				stringOut.println("0");
				return;
			}
			if(fileToSend.length() > Integer.MAX_VALUE){
				stringOut.println("0");
				return;
			}
			String testString = Long.toString(fileToSend.length());
			System.out.println(testString);
			stringOut.println(testString);
			byte[] fileBytes = new byte[(int)fileToSend.length()];
			bis.read(fileBytes);
			bytesOut.write(fileBytes);
			System.out.println("Sent file: " + fileName);
		}
	}

	private void sendFileFromFull(String filePath, PrintWriter stringOut) throws IOException{
		OutputStream bytesOut = socket.getOutputStream();
		File fileToSend = new File(baseDirectory + fileSeperator + filePath);
		try(
				FileInputStream fis = new FileInputStream(fileToSend);
				BufferedInputStream bis = new BufferedInputStream(fis);
				){
			if(!fileToSend.exists()){
				stringOut.println("0");
				return;
			}
			if(fileToSend.length() > Integer.MAX_VALUE){
				stringOut.println("0");
				return;
			}
			String testString = Long.toString(fileToSend.length());
			System.out.println(testString);
			stringOut.println(testString);
			byte[] fileBytes = new byte[(int)fileToSend.length()];
			bis.read(fileBytes);
			bytesOut.write(fileBytes);
			System.out.println("Sent file at: " + filePath);
		}
	}

	private void getFiles(PrintWriter stringOut) throws IOException{
		String filesString = "";
		File directoryToCheck = new File(currentDirectory);
		File[] heldFiles = directoryToCheck.listFiles();
		for(File f : heldFiles){
			if(f.isDirectory()){
				filesString += fileSeperator + f.getName() + ",";
			}
			else if(f.isFile()){
				filesString += f.getName() + ",";
			}
		}
		stringOut.println(filesString);
	}

	private void makeFolder(PrintWriter stringOut, String name){
		File folderToCreate = new File(currentDirectory + fileSeperator + name);
		if(folderToCreate.mkdir()){
			stringOut.println("Created new folder.");
		}
		else{
			stringOut.println("Folder already exists.");
		}
	}

	private void changeDirectory(PrintWriter stringOut, String name){
		if(name.equals("..")){
			if(currentDirectory.equals(baseDirectory)){
				stringOut.println("Already in root directory.");
				return;
			}
			int i = currentDirectory.lastIndexOf(fileSeperator);
			currentDirectory = currentDirectory.substring(0, i);
			stringOut.println("Now in Private folder: " + currentDirectory.substring(baseDirectory.length()) + fileSeperator);
			return;
		}
		File [] files = new File(currentDirectory).listFiles();
		for (File f : files) {
			if(f.isDirectory() && f.getName().equals(name)){
				currentDirectory += fileSeperator + name;
				stringOut.println("Now in Private folder: " + currentDirectory.substring(baseDirectory.length()) + fileSeperator);
				return;
			}
		}
		stringOut.println("Couldn't find folder.");
	}

	private void receiveFile(PrintWriter stringOut, String name) throws IOException{
		File directory = new File(currentDirectory);
		File[] existingFiles = directory.listFiles();
		for(File f : existingFiles){
			if(f.getName().equals(name)){
				stringOut.println("File already exists with this name.");
				return;
			}
		}
		stringOut.println("sendfile");
		BufferedReader stringIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		InputStream bytesIn = socket.getInputStream();
		int fileSize = Integer.parseInt(stringIn.readLine());
		byte[] fileBytes = new byte[fileSize];
		stringOut.println("fileSizeOkay");
		int divisions = (int)Math.ceil((double)fileSize/65535);
		if (divisions == 1){
			bytesIn.read(fileBytes);
		}
		else{
			int j = 0;
			byte[] chunk;
			for(int c = 0; c <= divisions; c++){
				if(j + 65535 <= fileSize){
					int l = Integer.valueOf(j);
					chunk = new byte[65535];
					j += bytesIn.read(chunk, 0, 65535);
					stringOut.println("continue");
					System.arraycopy(chunk, 0, fileBytes, l, chunk.length);
				} else {
					int l = Integer.valueOf(j);
					chunk = new byte[fileSize - j];
					j += bytesIn.read(chunk, 0, chunk.length - 1);
					stringOut.println("continue");
					System.arraycopy(chunk, 0, fileBytes, l, chunk.length);
				}
			}
		}
		stringOut.println("file saved");
		File newFile = new File(currentDirectory + fileSeperator + name);
		newFile.createNewFile();
		try(
				FileOutputStream fileStream = new FileOutputStream(newFile);
				BufferedOutputStream bos = new BufferedOutputStream(fileStream);
				)
				{
			bos.write(fileBytes);
			bos.flush();
				}
	}

	private void deleteFile(PrintWriter out, String fileName){
		File fileToDelete = new File(currentDirectory + fileSeperator + fileName);
		if(fileToDelete.exists()){
			fileToDelete.delete();
			out.println("File deleted.");
		}
		else{
			out.println("File not found.");
		}
	}

	private void getFilesAsFileView(PrintWriter stringOut) throws IOException{
		String filesString = "";
		File directoryToCheck = new File(currentDirectory);
		File[] heldFiles = directoryToCheck.listFiles();
		for(File f : heldFiles){
			if(f.isDirectory()){
				filesString += f.getName() + ":" + "folder" + ":" + "true" + ",";
			}
			else{
				filesString += f.getName() + ":" + Long.toString(f.length()) + ":" + "false" + ",";
			}
		}
		stringOut.println(filesString);
	}

	private void getCurrentDirectory(PrintWriter stringOut) throws IOException{
		String cutDir = currentDirectory.substring(baseDirectory.length());
		stringOut.println(cutDir + fileSeperator);
	}

	private void renameFile(PrintWriter stringOut, String renameData) throws IOException{
		String[] names = renameData.split(":");
		File fileToRename = new File(currentDirectory + fileSeperator + names[0]);
		File newName = new File(currentDirectory + fileSeperator + names[1]);
		try{
			Files.move(fileToRename.toPath(), newName.toPath(), StandardCopyOption.ATOMIC_MOVE);
		} catch (IOException e){
			System.out.println("rename failed.");
			stringOut.println("rename failed.");
			return;
		}
		stringOut.println("success");
	}

	private void moveFile(PrintWriter stringOut, String moveData) throws IOException{
		String[] paths = moveData.split(":");
		File fileToRename = new File(baseDirectory + paths[0]);
		File newName = new File(baseDirectory + paths[1]);
		try{
			Files.move(fileToRename.toPath(), newName.toPath(), StandardCopyOption.ATOMIC_MOVE);
		} catch (IOException e){
			System.out.println("move failed.");
			stringOut.println("move failed.");
			return;
		}
		stringOut.println("success");
	}

	private void goToDir(PrintWriter stringOut, String directory) throws IOException{
		File directoryFile = new File(baseDirectory + fileSeperator + directory);
		if (directoryFile.exists() && directoryFile.isDirectory()){
			currentDirectory = directoryFile.getAbsolutePath();
			stringOut.println("success");
		}
		else{
			stringOut.println("Invalid directory");
		}
	}
}