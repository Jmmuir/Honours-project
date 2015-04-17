package application;

import java.io.IOException;

import application.controller.FileViewController;
import application.password.PasswordController;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;


public class Main extends Application {

	private Stage primaryStage;
	private Stage passwordStage;
	private BorderPane rootLayout;
	private AnchorPane passLayout;
	private FileViewController controller;

	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Honours Project User App");
		loadRoot();
		showTabbedDirectories();
	}

	public static void main(String[] args) {
		launch(args);
	}

	private void loadRoot(){
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("RootLayout.fxml"));
			rootLayout = (BorderPane) loader.load();
			
			Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
           
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
	private void showTabbedDirectories(){
		try{
			FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("controller/Honours User App.fxml"));
            AnchorPane filesView = (AnchorPane) loader.load();
            
            rootLayout.setCenter(filesView);
            
            controller = loader.getController();
            FileViewController.setMain(this);
            
		} catch (IOException e) {
			System.out.print(e.getMessage());
		}
	}
	
	public void showClickPassword(){
		passwordStage = new Stage();
		passwordStage.setResizable(false);
		passwordStage.setTitle("Enter Password");
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("password/PasswordLayout.fxml"));
			passLayout = (AnchorPane)loader.load();
			
			Scene scene = new Scene(passLayout);
            passwordStage.setScene(scene);
            passwordStage.show();
            
            PasswordController passwordController = loader.getController();
            PasswordController.setMain(this);
           
		} catch (IOException e) {
			System.out.print(e.getMessage());
		}
	}
	
	public void connectToPrivate(){
		passwordStage.close();
		controller.connectToPrivate();
	}
	
}