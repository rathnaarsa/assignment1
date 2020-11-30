package com.playsafe.exceptions;

public class GameException extends Exception {

	private static final long serialVersionUID = 1020101L;

	public GameException(String errorMessage) {
		 super(errorMessage);
	 }
}
