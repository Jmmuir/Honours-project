package application.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Optional;

import com.dropbox.core.DbxException;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import application.DropboxConnection;
import application.DropboxInitialiser;
import application.Main;
import application.PrivateConnection;
import application.PrivateInitialiser;
import application.model.FileView;
import application.model.FileViewMovable;

public class FileViewController {

	private static Main main;
	private static DropboxConnection dbCon = null;
	private static PrivateConnection pCon = null;
	private static File workingDirectory;
	private static File transferDir;
	private static String fileSeparator;
	private static FileView selecteditem;
	private static FileViewMovable itemToMove;
	private static ArrayList<FileViewMovable> pvtTrashFiles = new ArrayList<FileViewMovable>();
	private static ArrayList<FileViewMovable> dbTrashFiles = new ArrayList<FileViewMovable>();

	@FXML
	private TableView<FileView> pvtFileTable;
	@FXML
	private TableColumn<FileView, String> pvtNameColumn;
	@FXML
	private TableColumn<FileView, String> pvtSizeColumn;

	@FXML
	private TableView<FileView> dbFileTable;
	@FXML
	private TableColumn<FileView, String> dbNameColumn;
	@FXML
	private TableColumn<FileView, String> dbSizeColumn;

	private ObservableList<FileView> dbFiles = FXCollections.observableArrayList();
	private ObservableList<FileView> pvtFiles = FXCollections.observableArrayList();

	@FXML
	private TitledPane dbCurrentFolder;

	@FXML
	private TitledPane pvtCurrentFolder;

	@FXML
	private TabPane serverSelect;
	@FXML
	private Tab dropboxTab;
	@FXML
	private Tab privateTab;

	@FXML
	private Button dbDownloadButton;
	@FXML
	private Button pvtDownloadButton;
	@FXML
	private Button dbUploadButton;
	@FXML
	private Button pvtUploadButton;
	@FXML
	private Button dbCreateButton;
	@FXML
	private Button pvtCreateButton;
	@FXML
	private Button dbDeleteButton;
	@FXML
	private Button pvtDeleteButton;
	@FXML
	private Button dbMoveButton;
	@FXML
	private Button pvtMoveButton;
	@FXML
	private Button dbRenameButton;
	@FXML
	private Button pvtRenameButton;	
	@FXML
	private Button dbTrashButton;
	@FXML
	private Button pvtTrashButton;
	@FXML
	private Button pvtConnectButton;

	private ArrayList<Button> buttonlist = new ArrayList<Button>();

	public FileViewController() {
	}

	@FXML
	private void initialize() {

		getCurrentDir();

		dbNameColumn.setCellValueFactory(cellData -> cellData.getValue().getFileName());
		dbSizeColumn.setCellValueFactory(cellData -> cellData.getValue().getFileSize());

		dbFileTable.setItems(dbFiles);

		pvtNameColumn.setCellValueFactory(cellData -> cellData.getValue().getFileName());
		pvtSizeColumn.setCellValueFactory(cellData -> cellData.getValue().getFileSize());

		pvtFileTable.setItems(pvtFiles);

		dbFiles.add(new FileView("Contacting", "Dropbox...", false));

		pvtFiles.add(new FileView("Waiting to connect to", "Private Server...", false));

		Platform.runLater(new DropboxInitialiser(this, workingDirectory));

		collectButtons();

		dropboxTab.setText(FileViewMovable.DROPBOXSOURCE);
		dropboxTab.setStyle(dbFileTable.getStyle());
		privateTab.setText(FileViewMovable.PRIVATESOURCE);
		privateTab.setStyle(pvtFileTable.getStyle());
		
		pvtDownloadButton.setDisable(true);
		pvtUploadButton.setDisable(true);
		pvtCreateButton.setDisable(true);
		pvtDeleteButton.setDisable(true);
		pvtRenameButton.setDisable(true);
		pvtMoveButton.setDisable(true);
		pvtTrashButton.setDisable(true);
	}

	public static void setMain(Main mainToSet){
		main = mainToSet;
	}

	@FXML
	private void onDbFolderPaneClicked(){
		try {
			dbCon.changeDirectory("..");
		} catch (DbxException e) {
			System.out.println(e.getMessage());
		}
		dbCurrentFolder.setExpanded(true);
		updateDbFiles();

	}

	@FXML
	private void onPvtFolderPaneClicked(){
		try {
			pCon.changeDirectory("..");
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		pvtCurrentFolder.setExpanded(true);
		updatePvtFiles();

	}

	private static void getCurrentDir(){
		fileSeparator = System.getProperty("file.separator");
		workingDirectory = new File(System.getProperty("user.dir") + fileSeparator + "HonoursDropBox");
		System.out.println("working in: " + workingDirectory.getAbsolutePath());
		if(workingDirectory.mkdir()){
			System.out.println("Created new directory.");
		}
		transferDir = new File(workingDirectory.getAbsolutePath() + fileSeparator + "TransferFiles");
		transferDir.mkdir();
		try{
			Path path = FileSystems.getDefault().getPath(workingDirectory.getAbsolutePath(), "");
			if(!workingDirectory.isHidden()){
				Files.setAttribute(path, "dos:hidden", true);
				System.out.println("Working directory has been hidden.");
			}
		}
		catch(IOException e){
			System.out.println("An error occurred while trying to hide the working directory, this should not affect the functioning of the application.");
			System.out.println(e.getMessage());
		}
	}

	private void updateDbFiles(){
		if(dbCon != null){
			try {
				dbFiles.clear();
				dbFiles.addAll(dbCon.getFilesAsFileView());
				dbCurrentFolder.setText(dbCon.getCurrentDirectory());
			} catch (DbxException e) {
				System.out.println(e.getMessage());
			}
			selecteditem = null;
			for(Button b : buttonlist){
				b.setDisable(false);
			}
			dbTrashButton.setText("View Deleted");
		}
	}

	private void updatePvtFiles(){
		if(pCon != null){
			pvtFiles.clear();
			try {
				pvtFiles.addAll(pCon.getFilesAsFileView());
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
			pvtCurrentFolder.setText(pCon.getDirectory());
			selecteditem = null;
			for(Button b : buttonlist){
				b.setDisable(false);
			}
			pvtTrashButton.setText("View Deleted");
		}
	}

	@FXML	
	private void onClickDbFileTable(){
		FileView selected = dbFileTable.getSelectionModel().getSelectedItem();
		if (selected != null){
			if(selected.isFolder() && selected.equals(selecteditem)){
				try {
					if(dbCon.changeDirectory(selected.getFileName().getValue())){
						updateDbFiles();
					}
				} catch (DbxException e) {
					System.out.println(e.getMessage());
				}
			}
			else{
				selecteditem = selected;
			}
		}
	}

	@FXML	
	private void onClickPvtFileTable(){
		FileView selected = pvtFileTable.getSelectionModel().getSelectedItem();
		if (selected != null){
			if(selected.isFolder() && selected.equals(selecteditem)){
				try {
					pCon.changeDirectory(selected.getFileName().getValue());
				} catch (IOException e) {
					System.out.println(e.getMessage());
				}
				updatePvtFiles();
			}
			else{
				selecteditem = selected;
			}
		}
	}

	public void setDbCon(DropboxConnection con){
		dbCon = con;
		updateDbFiles();
	}

	public void setPvtCon(PrivateConnection con){
		pCon = con;
		updatePvtFiles();
	}

	@FXML
	private void dbCreateFolder(){
		TextInputDialog dialog = new TextInputDialog("");
		dialog.setTitle("Create Folder");
		dialog.setHeaderText(null);
		dialog.setContentText("Call Folder what?:");
		Optional<String> result = dialog.showAndWait();
		if (result.isPresent() && result.get() != ""){
			try {
				dbCon.createFolder(result.get());
			} catch (DbxException e) {
				System.out.println(e.getMessage());
			}
		}
		updateDbFiles();
	}

	@FXML
	private void pvtCreateFolder(){
		if(pCon != null){
			TextInputDialog dialog = new TextInputDialog("");
			dialog.setTitle("Create Folder");
			dialog.setHeaderText(null);
			dialog.setContentText("Call Folder what?:");
			Optional<String> result = dialog.showAndWait();
			if (result.isPresent() && result.get() != ""){
				try {
					pCon.createFolder(result.get());
				} catch (IOException e) {
					System.out.println(e.getMessage());
				}
			}
			updatePvtFiles();
		}
	}

	@FXML
	private void dbDownload(){
		if(dbFileTable.getSelectionModel().getSelectedItem() != null){
			TextInputDialog dialog = new TextInputDialog(workingDirectory.getAbsolutePath().substring(0, workingDirectory.getAbsolutePath().length()-14));
			dialog.setTitle("DropBox Download");
			dialog.setHeaderText(null);
			dialog.setContentText("Download to where?:");
			Optional<String> result = dialog.showAndWait();
			if (result.isPresent() && result.get() != ""){
				try {
					dbCon.downloadFile(dbFileTable.getSelectionModel().getSelectedItem().getFileName().get(), fileSeparator, result.get());
				} catch (DbxException e) {
					System.out.println(e.getMessage());
				} catch (IOException e) {
					System.out.println(e.getMessage());
				}
			}
			updateDbFiles();
		}
	}

	@FXML
	private void pvtDownload(){
		if(pvtFileTable.getSelectionModel().getSelectedItem() != null){
			TextInputDialog dialog = new TextInputDialog(workingDirectory.getAbsolutePath().substring(0, workingDirectory.getAbsolutePath().length()-14));
			dialog.setTitle("Private Server Download");
			dialog.setHeaderText(null);
			dialog.setContentText("Download to where?:");
			Optional<String> result = dialog.showAndWait();
			if (result.isPresent() && result.get() != ""){
				try {
					pCon.downloadFile(pvtFileTable.getSelectionModel().getSelectedItem().getFileName().get(), fileSeparator, result.get());
				} catch (IOException e) {
					System.out.println(e.getMessage());
				}
			}
			updatePvtFiles();
		}
	}

	@FXML
	private void dbUpload(){
		TextInputDialog dialog = new TextInputDialog("");
		dialog.setTitle("DropBox Upload");
		dialog.setHeaderText(null);
		dialog.setContentText("Upload from where?:");
		Optional<String> result = dialog.showAndWait();
		if (result.isPresent() && result.get() != ""){
			try {
				dbCon.uploadFile(result.get(), fileSeparator);
			} catch (DbxException e) {
				System.out.println(e.getMessage());
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}
		updateDbFiles();
	}

	@FXML
	private void pvtUpload(){
		TextInputDialog dialog = new TextInputDialog("");
		dialog.setTitle("Private Upload");
		dialog.setHeaderText(null);
		dialog.setContentText("Upload from where?:");
		Optional<String> result = dialog.showAndWait();
		if (result.isPresent() && result.get() != ""){
			try {
				pCon.uploadFile(result.get(), fileSeparator);
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}
		updatePvtFiles();
	}

	@FXML
	private void dbDelete(){
		try{
			if(dbFileTable.getSelectionModel().getSelectedItem() != null){
				if(dbCon.deleteFile(dbFileTable.getSelectionModel().getSelectedItem().getFileName().get())){
					FileView file = dbFileTable.getSelectionModel().getSelectedItem();
					addDbDeletedFile(file.getFileName().get(), file.getFileSize().get(), file.isFolder(), dbCon.getCurrentDirectory());
				}
				if(dbCon.getCurrentDirectory().equals("//Trash")){
					updateDbTrashFiles();
					return;
				}
				updateDbFiles();
			}
		} catch (DbxException e){
			System.out.println(e.getMessage());
		}
	}

	@FXML
	private void pvtDelete(){
		try{
			if(pvtFileTable.getSelectionModel().getSelectedItem() != null){
				if(pCon.deleteFile(pvtFileTable.getSelectionModel().getSelectedItem().getFileName().get())){
					FileView file = pvtFileTable.getSelectionModel().getSelectedItem();
					addPvtDeletedFile(file.getFileName().get(), file.getFileSize().get(), file.isFolder(), pCon.getDirectory());
				}
				if(pCon.getDirectory().contains("Trash")){
					updatePvtTrashFiles();
					return;
				}
				updatePvtFiles();
			}
		} catch (IOException e){
			System.out.println(e.getMessage());
		}
	}

	@FXML
	private void dbRename(){
		if(dbFileTable.getSelectionModel().getSelectedItem() != null){
			TextInputDialog dialog = new TextInputDialog("");
			dialog.setTitle("Rename Selection");
			dialog.setHeaderText(null);
			dialog.setContentText("Rename to what?:");
			Optional<String> result = dialog.showAndWait();
			if (result.isPresent() && result.get() != ""){
				try {
					dbCon.rename(dbFileTable.getSelectionModel().getSelectedItem().getFileName().get(), result.get());
				} catch (DbxException e) {
					System.out.println(e.getMessage());
				}
			}
			updateDbFiles();
		}
	}

	@FXML
	private void pvtRename(){
		if(pvtFileTable.getSelectionModel().getSelectedItem() != null){
			TextInputDialog dialog = new TextInputDialog("");
			dialog.setTitle("Rename Selection");
			dialog.setHeaderText(null);
			dialog.setContentText("Rename to what?:");
			Optional<String> result = dialog.showAndWait();
			if (result.isPresent() && result.get() != ""){
				try {
					pCon.rename(pvtFileTable.getSelectionModel().getSelectedItem().getFileName().get(), result.get());
				} catch (IOException e) {
					System.out.println(e.getMessage());
				}
			}
			updatePvtFiles();
		}
	}

	@FXML
	private void dbMove(){
		if(itemToMove == null){
			FileView selected = dbFileTable.getSelectionModel().getSelectedItem();
			if(selected != null){
				itemToMove = new FileViewMovable(selected, FileViewMovable.DROPBOXSOURCE, dbCon.getCurrentDirectory());
				dbMoveButton.setText("Finish Move");
				pvtMoveButton.setText("Finish Move");
			}
		} else if (itemToMove.getSource().equals(FileViewMovable.DROPBOXSOURCE)){
			try {
				dbCon.moveFile(itemToMove.getSourceDir() + "/" + itemToMove.getFileName().get(), dbCon.getCurrentDirectory() + "/" + itemToMove.getFileName().get());
				updateDbFiles();
			} catch (DbxException e) {
				System.out.println(e.getMessage());
			}finally{
				itemToMove = null;
				dbMoveButton.setText("Move");
				pvtMoveButton.setText("Move");
			}
		} else if (itemToMove.getSource().equals(FileViewMovable.PRIVATESOURCE)){
			Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
			alert.setTitle("Confirm File Move");
			alert.setHeaderText("Copying file from Private Server to Dropbox");
			alert.setContentText("Are you sure you wish to copy this file to the dropbox? Dropbox is far less secure than the private server, and should never be used to store files which contain sensitive information. This may have irreversible side effects.");
			alert.setGraphic(new ImageView(this.getClass().getResource("/application/images/HonoursInsecure.png").toString()));
			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == ButtonType.OK){
				File transferFile = new File(transferDir + fileSeparator + itemToMove.getFileName().get());
				try{
					pCon.downloadFileForTransfer(itemToMove.getSourceDir(), itemToMove.getFileName().get(), fileSeparator, transferDir.getAbsolutePath());
					dbCon.uploadFile(transferFile.getAbsolutePath(), fileSeparator);
					updateDbFiles();
				} catch (DbxException e){
					System.out.println(e.getMessage());
				} catch (IOException e){
					System.out.println(e.getMessage());
				}finally{
					itemToMove = null;
					dbMoveButton.setText("Move");
					pvtMoveButton.setText("Move");
					if(transferFile.exists()){
						transferFile.delete();
					}
				}
			}
		}
	}

	@FXML
	private void pvtMove(){
		if(itemToMove == null){
			FileView selected = pvtFileTable.getSelectionModel().getSelectedItem();
			if(selected != null){
				itemToMove = new FileViewMovable(selected, FileViewMovable.PRIVATESOURCE, pCon.getDirectory());
				dbMoveButton.setText("Finish Move");
				pvtMoveButton.setText("Finish Move");
			}
		} else if (itemToMove.getSource().equals(FileViewMovable.PRIVATESOURCE)){
			try {
				pCon.moveFile(itemToMove.getSourceDir() + "/" + itemToMove.getFileName().get(), pCon.getDirectory() + "/" + itemToMove.getFileName().get());
				updatePvtFiles();
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}finally{
				itemToMove = null;
				dbMoveButton.setText("Move");
				pvtMoveButton.setText("Move");
			}
		} else if (itemToMove.getSource().equals(FileViewMovable.DROPBOXSOURCE)){
			Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
			alert.setTitle("Confirm File Move");
			alert.setHeaderText("Copying file from Dropbox to Private Server");
			alert.setContentText("Are you sure you wish to copy this file to the private server? The private server may not be visible to all users, and should not be used to share files widely.");
			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == ButtonType.OK){
				File transferFile = new File(transferDir + fileSeparator + itemToMove.getFileName().get());
				try{
					dbCon.downloadFileForTransfer(itemToMove.getSourceDir(), itemToMove.getFileName().get(), fileSeparator, transferDir);
					pCon.uploadFile(transferFile.getAbsolutePath(), fileSeparator);
					updatePvtFiles();
				} catch (DbxException e){
					System.out.println(e.getMessage());
				} catch (IOException e){
					System.out.println(e.getMessage());
				}finally{
					itemToMove = null;
					dbMoveButton.setText("Move");
					pvtMoveButton.setText("Move");
					if(transferFile.exists()){
						transferFile.delete();
					}
				}
			}
		}
	}

	@FXML
	private void dbTrashButton(){
		if(dbCon != null){
			if(!dbCon.getCurrentDirectory().equals("//Trash")){
				if(!dbCon.goToDir("//Trash")){
					System.out.println("Panic Stations, the trash folder is gone!!");
					return;
				}
				else{
					updateDbTrashFiles();
				}
			} else {
				if(dbFileTable.getSelectionModel().getSelectedItem() != null){
					FileView fileToRecover = dbFileTable.getSelectionModel().getSelectedItem();
					for(FileViewMovable f : dbTrashFiles){
						if(f.getFileName().get().equals(fileToRecover.getFileName().get())){
							try {
								dbCon.moveFile("//Trash/" + fileToRecover.getFileName().get(), f.getSourceDir() + "/" + f.getFileName().get());
								updateDbTrashFiles();
								return;
							} catch (DbxException e) {
								System.out.println(e.getMessage());
							}
						}
					}
					System.out.println("Cannot recover files not deleted in this session. If you need this file to be recovered, please contact a server admin.");
				}
			}
		}
	}

	@FXML
	private void pvtTrashButton(){
		if(pCon != null){
			if(!pCon.getDirectory().contains("Trash")){
				try {
					if(!pCon.goToDir("Trash")){
						System.out.println("Panic Stations, the trash folder is gone!!");
						return;
					}
					else{
						updatePvtTrashFiles();
					}
				} catch (IOException e) {
					System.out.println(e.getMessage());
				}
			} else {
				if(pvtFileTable.getSelectionModel().getSelectedItem() != null){
					FileView fileToRecover = pvtFileTable.getSelectionModel().getSelectedItem();
					for(FileViewMovable f : pvtTrashFiles){
						if(f.getFileName().get().equals(fileToRecover.getFileName().get())){
							try {
								pCon.moveFile("/Trash/" + fileToRecover.getFileName().get(), f.getSourceDir() + "/" + f.getFileName().get());
								updatePvtTrashFiles();
								return;
							} catch (IOException e) {
								System.out.println(e.getMessage());
							}
						}
					}
					System.out.println("Cannot recover files not deleted in this session. If you need this file to be recovered, please contact a server admin.");
				}
			}
		}
	}

	private void updateDbTrashFiles(){
		updateDbFiles();
		for(Button b : buttonlist){
			b.setDisable(true);
		}
		dbTrashButton.setText("Recover");
		dbTrashButton.setDisable(false);
		dbDeleteButton.setDisable(false);
	}

	private void updatePvtTrashFiles(){
		updatePvtFiles();
		for(Button b : buttonlist){
			b.setDisable(true);
		}
		pvtTrashButton.setText("Recover");
		pvtTrashButton.setDisable(false);
		pvtDeleteButton.setDisable(false);
	}

	private void collectButtons(){
		buttonlist.add(dbDownloadButton);
		buttonlist.add(dbUploadButton);
		buttonlist.add(dbCreateButton);
		buttonlist.add(dbDeleteButton);
		buttonlist.add(dbRenameButton);
		buttonlist.add(dbMoveButton);
		buttonlist.add(dbTrashButton);

		buttonlist.add(pvtDownloadButton);
		buttonlist.add(pvtUploadButton);
		buttonlist.add(pvtCreateButton);
		buttonlist.add(pvtDeleteButton);
		buttonlist.add(pvtRenameButton);
		buttonlist.add(pvtMoveButton);
		buttonlist.add(pvtTrashButton);
	}

	public void addPvtDeletedFile(String fileName, String fileSize, Boolean isFolder, String sourceDir){
		pvtTrashFiles.add(new FileViewMovable(fileName, fileSize, isFolder, FileViewMovable.PRIVATESOURCE, sourceDir));
	}

	public void removePvtDeletedFile(String fileName, String sourceDir){
		for(FileViewMovable f : pvtTrashFiles){
			if(f.getFileName().get().equals(fileName) && f.getSourceDir().equals(sourceDir)){
				pvtTrashFiles.remove(f);
				return;
			}
		}
	}

	public void addDbDeletedFile(String fileName, String fileSize, Boolean isFolder, String sourceDir){
		dbTrashFiles.add(new FileViewMovable(fileName, fileSize, isFolder, FileViewMovable.DROPBOXSOURCE, sourceDir));
	}

	public void removeDbDeletedFile(String fileName, String sourceDir){
		for(FileViewMovable f : dbTrashFiles){
			if(f.getFileName().get().equals(fileName) && f.getSourceDir().equals(sourceDir)){
				dbTrashFiles.remove(f);
				return;
			}
		}
	}

	@FXML
	private void pvtConnect(){
		pvtFiles.add(new FileView("Contacting", "Private Server...", false));
		main.showClickPassword();
	}
	
	public void connectToPrivate(){
		Platform.runLater(new PrivateInitialiser(this));
		pvtDownloadButton.setDisable(false);
		pvtUploadButton.setDisable(false);
		pvtCreateButton.setDisable(false);
		pvtDeleteButton.setDisable(false);
		pvtRenameButton.setDisable(false);
		pvtMoveButton.setDisable(false);
		pvtTrashButton.setDisable(false);
		pvtConnectButton.setDisable(true);
	}
}