package application.password;

import application.Main;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;

public class PasswordController {

	private static Main main;
	@FXML
	private BorderPane passImagePane;

	private double xcoord;
	private double ycoord;
	private int sequence = 0;
	private PassCoord[] passwordCoords;

	public PasswordController() {
	}

	@FXML
	private void initialize() {
		passImagePane.setCenter(new ImageView(this.getClass().getResource("/application/images/Image000.png").toString()));
		passImagePane.setOnMouseMoved(new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent event) {
				xcoord = event.getX();
				ycoord = event.getY();
			}
		});
		setDefaultPass();
	}

	public static void setMain(Main mainToSet){
		main = mainToSet;
	}

	@FXML
	private void onPassClick(){
		if(sequence == -1){
			sequence = 0;
			passImagePane.setCenter(new ImageView(this.getClass().getResource("/application/images/Image000.png").toString()));
		}else if(sequence < passwordCoords.length){
			if(passwordCoords[sequence].compareWithTolerance(xcoord, ycoord)){
				if(sequence == passwordCoords.length-1){
					main.connectToPrivate();
				}else{
					sequence++;
					passImagePane.setCenter(new ImageView(this.getClass().getResource("/application/images/Image00" + sequence + ".png").toString()));
				}
			}else{
				passImagePane.setCenter(new ImageView(this.getClass().getResource("/application/images/WrongPoint.png").toString()));
				sequence = -1;
			}
		}


	}

	private void setDefaultPass(){
		PassCoord one = new PassCoord(403,154);
		PassCoord two = new PassCoord(64,70);
		PassCoord three = new PassCoord(476,189);
		PassCoord four = new PassCoord(40,236);
		passwordCoords = new PassCoord[4];
		passwordCoords[0] = one;
		passwordCoords[1] = two;
		passwordCoords[2] = three;
		passwordCoords[3] = four;
	}
}
