package application.model;

import java.io.File;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class FileView {
	
	private StringProperty fileName;
	private StringProperty fileSize;
	private Boolean isFolder;

	public FileView(String fileName, String fileSize, Boolean isFolder) {
		this.fileName = new SimpleStringProperty(fileName);
		this.fileSize = new SimpleStringProperty(fileSize);
		this.isFolder = isFolder;
	}
	
	public FileView(StringProperty fileName, StringProperty fileSize, Boolean isFolder) {
		this.fileName = fileName;
		this.fileSize = fileSize;
		this.isFolder = isFolder;
	}
	
	public FileView(File file){
		this.fileName = new SimpleStringProperty(file.getName());
		this.fileSize = new SimpleStringProperty(Long.toString(file.length()));
		this.isFolder = file.isDirectory();
	}

	public StringProperty getFileName(){
		return this.fileName;
	}
	
	public StringProperty getFileSize(){
		return this.fileSize;
	}
	
	public Boolean isFolder(){
		return this.isFolder;
	}
}
