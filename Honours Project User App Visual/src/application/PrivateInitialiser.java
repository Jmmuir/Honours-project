package application;

import java.io.IOException;

import application.controller.FileViewController;

public class PrivateInitialiser implements Runnable{
	
	private static FileViewController controller;
	private static PrivateConnection pvtCon;

	public PrivateInitialiser(FileViewController InstigatingController) {
		controller = InstigatingController;
	}

	@Override
	public void run() {
		try {
			pvtCon = new PrivateConnection();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		controller.setPvtCon(pvtCon);
	}
	
}
