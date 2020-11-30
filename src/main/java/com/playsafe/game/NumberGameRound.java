package com.playsafe.game;

public class NumberGameRound extends GameRound{

	private Integer no;
	
	public NumberGameRound(GameType type, Integer no, Double betAmount) {
		super(type,betAmount);
		this.setNo(no);
	}

	public Integer getNo() {
		return no;
	}

	public void setNo(Integer no) {
		this.no = no;
	}
}
