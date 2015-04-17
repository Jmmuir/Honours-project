package application;

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
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.ImageView;
import application.model.FileView;

public class PrivateConnection {

	private static String hostname = "82.8.36.234";
	private static int port = 6636;
	private static Socket pvtSocket;

	public PrivateConnection() throws IOException {
		pvtSocket = new Socket(hostname, port);
		pvtSocket.setSoTimeout(2000);
	}

	public void sendMessage(String message) throws IOException{
		PrintWriter out = new PrintWriter(pvtSocket.getOutputStream(), true);
		BufferedReader in = new BufferedReader(
				new InputStreamReader(pvtSocket.getInputStream()));

		out.println("+ " + message);
		if(in.readLine().equals("recieved")){
			System.out.println("Message recieved");
		}
	}

	public void downloadFile(String fileName, String seperator, String destination)throws IOException{
		int filesize;
		File outputFile = new File(destination + seperator +  fileName);
		FileOutputStream outputStream = new FileOutputStream(outputFile);
		BufferedReader stringIn = new BufferedReader(new InputStreamReader(pvtSocket.getInputStream()));
		InputStream bytesIn = pvtSocket.getInputStream();
		BufferedOutputStream bos = new BufferedOutputStream(outputStream);
		PrintWriter out = new PrintWriter(pvtSocket.getOutputStream(), true);
		try {
			out.println("download " + fileName);
			filesize = Integer.parseInt(stringIn.readLine());
			if(filesize == 0){
				System.out.println("Server could not find the file specified, or it is too large, or the file is blank.");
			}
			byte[] fileBytes = new byte[filesize];
			int divisions = (int)Math.ceil((double)filesize/65535);
			if (divisions == 1){
				bytesIn.read(fileBytes);
			}
			else{
				int j = 0;
				for(int c = 0; c < divisions; c++){
					j += bytesIn.read(fileBytes, j, fileBytes.length - j);
				}
			}
			if(!outputFile.exists()){
				outputFile.createNewFile();
			}
			bos.write(fileBytes);
			bos.flush();
		} finally {
			outputStream.close();
		}
	}

	public void downloadFileForTransfer(String filePath, String fileName, String seperator, String destination)throws IOException{
		int filesize;
		File outputFile = new File(destination + seperator +  fileName);
		FileOutputStream outputStream = new FileOutputStream(outputFile);
		BufferedReader stringIn = new BufferedReader(new InputStreamReader(pvtSocket.getInputStream()));
		InputStream bytesIn = pvtSocket.getInputStream();
		BufferedOutputStream bos = new BufferedOutputStream(outputStream);
		PrintWriter out = new PrintWriter(pvtSocket.getOutputStream(), true);
		try {
			out.println("downloadft " + filePath + seperator + fileName);
			filesize = Integer.parseInt(stringIn.readLine());
			if(filesize == 0){
				System.out.println("Server could not find the file specified, or it is too large, or the file is blank.");
			}
			byte[] fileBytes = new byte[filesize];
			bytesIn.read(fileBytes);
			if(!outputFile.exists()){
				outputFile.createNewFile();
			}
			bos.write(fileBytes);
			bos.flush();
		} finally {
			outputStream.close();
		}
	}

	public void uploadFile(String filePath, String seperator) throws IOException{
		File inputFile = new File(filePath);
		if(inputFile.exists() && inputFile.length() != 0 && inputFile.length() <= Integer.MAX_VALUE){
			FileInputStream inputStream = new FileInputStream(inputFile);
			String[] fileNameParts = filePath.split(seperator.replaceAll("\\\\", "\\\\\\\\"));
			String uploadedName = fileNameParts[fileNameParts.length-1];
			PrintWriter out = new PrintWriter(pvtSocket.getOutputStream(), true);
			BufferedReader stringIn = new BufferedReader(new InputStreamReader(pvtSocket.getInputStream()));
			try(FileInputStream fis = new FileInputStream(inputFile);
					BufferedInputStream bis = new BufferedInputStream(fis);){
				out.println("upload " + uploadedName);
				String response = stringIn.readLine();
				if(response.equals("sendfile")){
					Long fileSize = inputFile.length();
					int divisions = (int)Math.ceil((double)fileSize/65535);
					out.println(Long.toString(fileSize));
					response = stringIn.readLine();
					byte[] fileBytes = new byte[(int)inputFile.length()];
					if(response.equals("fileSizeOkay")){
						if(divisions == 1){
							bis.read(fileBytes);
							OutputStream bytesOut = pvtSocket.getOutputStream();
							bytesOut.write(fileBytes);
							bytesOut.flush();
							System.out.println(stringIn.readLine());
						} else {
							bis.read(fileBytes);
							int j = 0;
							for(int c = 0; c < divisions; c++){
								byte[] chunk;
								if (j + 65535 < fileSize){
									chunk = Arrays.copyOfRange(fileBytes, j, j+65535);
									j += 65535;
								}else{
									chunk = Arrays.copyOfRange(fileBytes, j, fileBytes.length);
								}
								OutputStream bytesOut = pvtSocket.getOutputStream();
								bytesOut.write(chunk);
								bytesOut.flush();
								if(!stringIn.readLine().equals("continue")){
									return;
								}
							}
						}
					}
					else{
						System.out.println(stringIn.readLine());
					}
				}
				else{
					System.out.println(response);
				}
			}finally{
				inputStream.close();
			}
		}
		else{
			System.out.println("File either does not exist, contains no data, or is too large.");
		}
	}

	public ArrayList<String> getFiles() throws IOException{
		ArrayList<String> fileStrings = new ArrayList<String>();
		BufferedReader stringIn = new BufferedReader(new InputStreamReader(pvtSocket.getInputStream()));
		PrintWriter out = new PrintWriter(pvtSocket.getOutputStream(), true);
		out.println("getfiles");
		String rawFileString = stringIn.readLine();
		String[] splitStrings = rawFileString.split(",");
		for(String s : splitStrings){
			fileStrings.add(s);
		}
		return fileStrings;
	}

	public void createFolder(String folderName) throws IOException{
		if(folderName.contains("Trash")){
			System.out.println("'Trash' is a reserved name, please choose another.");
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Reserved Folder Name");
			alert.setHeaderText(null);
			alert.setContentText("'Trash' is a reserved name, please choose another.");
			alert.showAndWait();
			return;
		}
		BufferedReader stringIn = new BufferedReader(new InputStreamReader(pvtSocket.getInputStream()));
		PrintWriter out = new PrintWriter(pvtSocket.getOutputStream(), true);
		out.println("makefolder " + folderName);
		System.out.println(stringIn.readLine());
	}

	public void changeDirectory(String dirName) throws IOException{
		BufferedReader stringIn = new BufferedReader(new InputStreamReader(pvtSocket.getInputStream()));
		PrintWriter out = new PrintWriter(pvtSocket.getOutputStream(), true);
		out.println("cd " + dirName);
		System.out.println(stringIn.readLine());
	}

	public boolean deleteFile(String fileName) throws IOException{
		String dir = getDirectory();
		if(dir.contains("Trash")){
			Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
			alert.setTitle("Confirm Full Deletion");
			alert.setHeaderText("Deleting File Permanently");
			alert.setContentText("Files in the trash folder are recoverable. If you delete this file, it can no longer be recovered, continue?");
			alert.setGraphic(new ImageView(this.getClass().getResource("images/HonoursFileTrueDelete.png").toString()));
			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == ButtonType.OK){
				BufferedReader stringIn = new BufferedReader(new InputStreamReader(pvtSocket.getInputStream()));
				PrintWriter out = new PrintWriter(pvtSocket.getOutputStream(), true);
				out.println("delete " + fileName);
				System.out.println(stringIn.readLine());
			}
			return false;
		}else{
			moveFile(dir + fileName, "/Trash/" + fileName);
			return true;
		}
	}

	public void closeSocket(){
		try {
			pvtSocket.close();
		} catch (IOException e) {
			System.out.println("Error when closing socket, " + e.getMessage()); 
		}
	}

	public ArrayList<FileView> getFilesAsFileView() throws IOException{
		ArrayList<FileView> files = new ArrayList<FileView>();
		BufferedReader stringIn = new BufferedReader(new InputStreamReader(pvtSocket.getInputStream()));
		PrintWriter out = new PrintWriter(pvtSocket.getOutputStream(), true);
		out.println("getfilesafv");
		String rawFileString = stringIn.readLine();
		String[] splitStrings = rawFileString.split(",");
		if(!splitStrings[0].equals("")){
			for(String s : splitStrings){
				String[] a = s.split(":");
				if(!a[0].equals("Trash")){
					files.add(new FileView(a[0], a[1], Boolean.valueOf(a[2])));
				}
			}
		}
		return files;
	}

	public String getDirectory(){
		try{
			BufferedReader stringIn = new BufferedReader(new InputStreamReader(pvtSocket.getInputStream()));
			PrintWriter out = new PrintWriter(pvtSocket.getOutputStream(), true);
			out.println("getdirectory");
			String directory = stringIn.readLine();
			return directory;
		} catch(IOException e){
			System.out.println(e.getMessage());
		}
		return null;
	}

	public void rename(String oldName, String newName)throws IOException{
		BufferedReader stringIn = new BufferedReader(new InputStreamReader(pvtSocket.getInputStream()));
		PrintWriter out = new PrintWriter(pvtSocket.getOutputStream(), true);
		out.println("rename " + oldName + ":" + newName);
		System.out.println(stringIn.readLine());
	}

	public void moveFile(String oldPath, String newPath) throws IOException{
		BufferedReader stringIn = new BufferedReader(new InputStreamReader(pvtSocket.getInputStream()));
		PrintWriter out = new PrintWriter(pvtSocket.getOutputStream(), true);
		out.println("move " + oldPath + ":" + newPath);
		System.out.println(stringIn.readLine());
	}

	public Boolean goToDir(String directory) throws IOException{
		BufferedReader stringIn = new BufferedReader(new InputStreamReader(pvtSocket.getInputStream()));
		PrintWriter out = new PrintWriter(pvtSocket.getOutputStream(), true);
		out.println("goto " + directory);
		if(stringIn.readLine().equals("success")){
			return true;
		}
		return false;
	}
}