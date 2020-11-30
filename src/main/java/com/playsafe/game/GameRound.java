package com.playsafe.game;

public class GameRound {

	protected GameType type;
	protected Double betAmount;
	
	public GameRound(GameType type, Double betAmount) {
		this.setType(type);
		this.setBetAmount(betAmount);
	}

	public GameType getType() {
		return type;
	}

	public void setType(GameType type) {
		this.type = type;
	}

	public Double getBetAmount() {
		return betAmount;
	}

	public void setBetAmount(Double betAmount) {
		this.betAmount = betAmount;
	}
}
