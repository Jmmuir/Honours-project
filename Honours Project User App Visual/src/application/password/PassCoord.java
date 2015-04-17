package application.password;

public class PassCoord {
	
	private double Xcoord;
	private double Ycoord;

	public PassCoord(double X, double Y) {
		this.Xcoord = X;
		this.Ycoord = Y;
	}
	
	public boolean compareWithTolerance(double X, double Y){
		double xDiff = this.Xcoord - X;
		double yDiff = this.Ycoord - Y;
		if(xDiff <=10 && xDiff >= -10 && yDiff <=10 && yDiff >= -10){
			return true;
		}else{return false;}
	}
	
	public boolean compareWithoutTolerance(double X, double Y){
		if(this.Xcoord == X && this.Ycoord == Y){
			return true;
		}else{return false;}
	}

}
