package application;

import application.model.FileView;

import com.dropbox.core.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.ImageView;

public class DropboxConnection {

	private static final String APP_KEY = "ig2qn5v8bhhdpjp";
	private static final String APP_SECRET = "fs8izpvr29m5jui";
	private String accessToken;
	private String currentDirectory = "/";
	private DbxRequestConfig config;
	private DbxClient client;

	public DropboxConnection() throws IOException, DbxException{

		DbxAppInfo appInfo = new DbxAppInfo(APP_KEY, APP_SECRET);

		config = new DbxRequestConfig("HonoursProject/1.0", Locale.getDefault().toString());
		DbxWebAuthNoRedirect webAuth = new DbxWebAuthNoRedirect(config, appInfo);

		String authorizeUrl = webAuth.start();
		System.out.println("1. Go to: " + authorizeUrl);
		System.out.println("2. Click \"Allow\" (you might have to log in first)");
		System.out.println("3. Copy the authorization code.");
		String code = new BufferedReader(new InputStreamReader(System.in)).readLine().trim();

		DbxAuthFinish authFinish = webAuth.finish(code);
		accessToken = authFinish.accessToken;

		client = new DbxClient(config, accessToken);
		createTrashFolder();
	}

	public DropboxConnection(String token){
		accessToken = token;
		config = new DbxRequestConfig("HonoursProject/1.0", Locale.getDefault().toString());
		client = new DbxClient(config, accessToken);
		createTrashFolder();
	}

	public String getAccessToken(){
		return this.accessToken;
	}

	public void uploadFile(String fileName, String seperator) throws DbxException, IOException{
		File inputFile = new File(fileName);
		FileInputStream inputStream = new FileInputStream(inputFile);
		String[] fileNameParts = fileName.split(seperator.replaceAll("\\\\", "\\\\\\\\"));
		String uploadedName = fileNameParts[fileNameParts.length-1];
		try {
			DbxEntry.File uploadedFile = client.uploadFile(currentDirectory + "/" + uploadedName,
					DbxWriteMode.add(), inputFile.length(), inputStream);
			System.out.println("Uploaded: " + uploadedFile.toString());
		} finally {
			inputStream.close();
		}
	}

	public ArrayList<String> getFilesAsStrings() throws DbxException{
		DbxEntry.WithChildren listing = client.getMetadataWithChildren(currentDirectory);
		ArrayList<String> contents = new ArrayList<String>();
		for (DbxEntry child : listing.children) {
			if(child.isFile()){
				contents.add(child.name);
			}
			else if(child.isFolder()){
				contents.add("/" + child.name);
			}
		}
		return contents;
	}

	public boolean changeDirectory(String directoryName) throws DbxException{
		if(directoryName.equals("..")){
			if(currentDirectory.equals("/")){
				System.out.println("Already in root directory.");
				return false;
			}
			int i = currentDirectory.lastIndexOf("/");
			currentDirectory = currentDirectory.substring(0, i);
			System.out.println("Now in Dropbox folder: " + currentDirectory);
			return true;
		}
		DbxEntry.WithChildren listing = client.getMetadataWithChildren(currentDirectory);
		for (DbxEntry child : listing.children) {
			if(child.isFolder() && child.name.equals(directoryName)){
				currentDirectory += "/" + directoryName;
				System.out.println("Now in Dropbox folder: " + currentDirectory);
				return true;
			}
		}
		return false;
	}

	public boolean createFolder(String folderName) throws DbxException{
		if(folderName.equals("Trash")){
			System.out.println("'Trash' is a reserved name, please choose another.");
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Reserved Folder Name");
			alert.setHeaderText(null);
			alert.setContentText("'Trash' is a reserved name, please choose another.");
			alert.showAndWait();
			return false;
		}
		if(client.createFolder(currentDirectory + "/" + folderName) != null){return true;}
		return false;
	}

	public String getCurrentDirectory(){
		return this.currentDirectory;
	}

	public void downloadFile(String fileName, String seperator, String destination)throws DbxException, IOException{
		File outputFile = new File(destination + seperator +  fileName);
		if(!outputFile.exists()){
			outputFile.createNewFile();
		}
		FileOutputStream outputStream = new FileOutputStream(outputFile);
		try {
			client.getFile(currentDirectory + "/" + fileName, null, outputStream);
		} finally {
			outputStream.close();
		}
	}

	public void downloadFileForTransfer(String filePath, String fileName, String fileSeperator, File targetDir)throws DbxException, IOException{
		File outputFile = new File(targetDir.getAbsolutePath() + fileSeperator +  fileName);
		if(!outputFile.exists()){
			outputFile.createNewFile();
		}
		FileOutputStream outputStream = new FileOutputStream(outputFile);
		try {
			client.getFile(filePath + "/" + fileName, null, outputStream);
		} finally {
			outputStream.close();
		}
	}

	public boolean deleteFile(String fileName) throws DbxException{
		if(currentDirectory.equals("//Trash")){
			Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
			alert.setTitle("Confirm Full Deletion");
			alert.setHeaderText("Deleting File Permanently");
			alert.setContentText("Files in the trash folder are recoverable. If you delete this file, it can no longer be recovered, continue?");
			alert.setGraphic(new ImageView(this.getClass().getResource("images/HonoursFileTrueDelete.png").toString()));
			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == ButtonType.OK){
				client.delete(currentDirectory + "/" + fileName);
			}
			return false;
		}else{
			moveFile(currentDirectory + "/" + fileName, "//Trash/" + fileName);
			return true;
		}
	}

	public ArrayList<FileView> getFilesAsFileView() throws DbxException{
		ArrayList<FileView> files = new ArrayList<FileView>();
		FileView currentItem = null;
		DbxEntry.WithChildren listing = client.getMetadataWithChildren(currentDirectory);
		for(DbxEntry child : listing.children){
			if(child.isFile()){
				DbxEntry.File currentFile = child.asFile();
				currentItem = new FileView(currentFile.name, currentFile.humanSize, false);
			}
			else if(child.isFolder()){
				DbxEntry.Folder currentFolder = child.asFolder();
				currentItem = new FileView(currentFolder.name, "folder", true);
			}
			if(currentItem != null){
				if(!currentItem.getFileName().get().equals("Trash")){
					files.add(currentItem);
				}
			}
			currentItem=null;
		}
		return files;
	}

	public void rename(String oldName, String newName) throws DbxException{
		client.move(currentDirectory + "/" + oldName, currentDirectory + "/" + newName);
	}

	public void moveFile(String oldLocation, String newLocation)throws DbxException{
		client.move(oldLocation, newLocation);
	}

	private void createTrashFolder(){
		DbxEntry.WithChildren listing;
		try {
			listing = client.getMetadataWithChildren(currentDirectory);
			for(DbxEntry child : listing.children){
				if(child.isFolder() && child.asFolder().name.equals("Trash")){
					return;
				}
				client.createFolder("/Trash");
			}
		}
		catch (DbxException e) {
			System.out.println(e.getMessage());
		}
	}
	
	public Boolean goToDir(String directory){
		try {
			if(client.getMetadata(directory)!=null){
				currentDirectory = directory;
				return true;
			}
		} catch (DbxException e) {
			System.out.println(e.getMessage());
		}
		return false;
	}
}