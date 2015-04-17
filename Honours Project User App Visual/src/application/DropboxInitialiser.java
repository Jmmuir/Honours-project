package application;

import java.io.File;
import java.io.IOException;

import org.tmatesoft.sqljet.core.SqlJetException;

import com.dropbox.core.DbxException;

import application.controller.FileViewController;

public class DropboxInitialiser implements Runnable {
	
	private static SettingsDAO settings;
	private static FileViewController controller;
	private static File workingDirectory;
	private static DropboxConnection dbCon;

	public DropboxInitialiser(FileViewController controller, File currentDir) {
		this.controller = controller;
		this.workingDirectory = currentDir;
	}

	@Override
	public void run() {
		obtainSettings();
		controller.setDbCon(dbCon);
	}
	
	private static void obtainSettings(){
		settings = new SettingsDAO(workingDirectory);
		String accessToken;
		try {
			accessToken = settings.readAccessToken();
		} catch (SqlJetException e) {
			accessToken = null;
			System.out.println("An error occurred trying to read the access token... attempting to obtain fresh one. " + e.getMessage());
		}
		if(accessToken != null){
			dbCon = new DropboxConnection(accessToken);
		}
		else{
			try {
				dbCon = new DropboxConnection();
				settings.setAccessToken(dbCon.getAccessToken());
			} catch (IOException e) {
				System.out.println("Error trying to create a new Dropbox connection " + e.getMessage());
			} catch (DbxException e) {
				System.out.println("Error trying to create a new Dropbox connection " + e.getMessage());
			} catch (SqlJetException e) {
				System.out.println("Error trying to save access token, application will have to ask for a new one next time." + e.getMessage());
			}
		}

	}

}
