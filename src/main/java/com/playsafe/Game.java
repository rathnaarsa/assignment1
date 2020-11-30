package com.playsafe;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;
import java.util.Random;

import com.playsafe.exceptions.GameException;
import com.playsafe.game.GameRound;
import com.playsafe.game.GameType;
import com.playsafe.game.NumberGameRound;

public class Game {

	private final static Logger LOGGER = Logger.getLogger(Game.class.getName());
	private final static List<String> players = new ArrayList<String>();

	public static void main(String[] args) {
		
		try {
			BufferedReader br = new BufferedReader(new FileReader("src/main/resources/players.txt"));
			String line = br.readLine();
			while(line != null) {
				players.add(line);
				line = br.readLine();
			}
		} catch (FileNotFoundException e1) {
			LOGGER.info("Error reading from the file");
			System.exit(-1);
		} catch (IOException e) {
			LOGGER.info("Error reading from the file");
			System.exit(-1);
		}
			  

		Random rand = new Random();
		Game game = new Game();

		ExecutorService myWorkers = Executors.newFixedThreadPool(10);

		try {
			while (true) {
				final Map<String, List<GameRound>> task = CompletableFuture.supplyAsync(() -> {
					try {
						return game.readInput();
					} catch (GameException e) {
						LOGGER.info("There is an error with the game");
						LOGGER.info(e.toString());
						myWorkers.shutdownNow();
						return null;
					}
				}).get(30, TimeUnit.SECONDS);

				if (task != null)
					game.processGameResult(1 + rand.nextInt(36), task);
				else
					throw new GameException("Error playing the Game");
			}

		} catch (InterruptedException | ExecutionException e) {
			LOGGER.info("The game got timed out");
			myWorkers.shutdownNow();
		} catch (TimeoutException e) {
			LOGGER.info("The game got timed out");
			myWorkers.shutdownNow();
		} catch (GameException e) {
			LOGGER.info("There is an error with the game");
			LOGGER.info(e.toString());
		}

	}

	public Map<String, List<GameRound>> readInput() throws GameException {

		LOGGER.info("Type case insensitive 'END' string to mark the end of input");

		Scanner scanner = new Scanner(System.in);
		Map<String, List<GameRound>> game = new HashMap<String, List<GameRound>>();

		String input = scanner.nextLine();
		String[] values = null;

		try {
			while (!input.equalsIgnoreCase("END")) {
				values = input.split("\\s+");
				parseInput(values, game);
				input = scanner.nextLine();
			}
		} catch (GameException ge) {
			throw ge;
		} finally {
			scanner.close();
		}

		return game;
	}

	public void parseInput(String input[], Map<String, List<GameRound>> game) throws GameException {

		if (input.length < 3) {
			LOGGER.info("Invalid no of input arguments");
			throw new GameException("Invalid no of input arguments");
		}

		Double betAmount = null;
		try {
			betAmount = Double.parseDouble(input[2]);
		} catch (NumberFormatException nfe) {
			LOGGER.info("Invalid bet amount value");
			throw new GameException("Invalid bet amount value");
		}

		if (input[0].isEmpty()) {
			LOGGER.info("Player name must be provided");
			throw new GameException("Player name must be provided");
		}

		GameRound round = null;
		if (input[1].equalsIgnoreCase("EVEN")) {
			round = new GameRound(GameType.EVEN, betAmount);
		} else if (input[1].equalsIgnoreCase("ODD")) {
			round = new GameRound(GameType.ODD, betAmount);
		} else {
			int no;
			try {
				no = Integer.parseInt(input[1]);
				if (no < 1 && no > 36) {
					LOGGER.info("Must be a number between 1 to 36");
					throw new GameException("Must be a number between 1 to 36");
				}
			} catch (NumberFormatException e) {
				LOGGER.info("Not a number");
				throw new GameException("Not a number");
			}
			round = new NumberGameRound(GameType.NUMBER, no, betAmount);
		}

		String playerName = input[0].toLowerCase();
		List<GameRound> list = null;
		if (game.containsKey(playerName)) {
			list = game.get(playerName);
			if (list == null) {
				list = new ArrayList<GameRound>();
			}
			list.add(round);
			game.put(playerName, list);
		} else {
			list = new ArrayList<GameRound>();
			list.add(round);
			game.put(playerName, list);
		}
	}

	private void processGameResult(Integer gameNumber, Map<String, List<GameRound>> game) throws GameException {

		LOGGER.info("The Game No is " + gameNumber);
		
		if (game == null) {
			LOGGER.info("There must be atleast two players to play this game");
			throw new GameException("There must be atleast two players to play this game");
		}

		Iterator<Map.Entry<String, List<GameRound>>> itr = game.entrySet().iterator();
		double betAmt = 0;
		boolean wonRound = false;
		String betType = null;
		String result = null;

		System.out.println("Player" + "   " + "Bet" + "   " + "Outcome" + "   " + "Winnings");
		System.out.println("-----------------------------------------------------------------");

		while (itr.hasNext()) {
			Map.Entry<String, List<GameRound>> entry = itr.next();
			List<GameRound> rounds = entry.getValue();

			if (rounds == null || rounds.isEmpty()) {
				LOGGER.info("One of the player has not placed the bet");
				throw new GameException("One of the player has not placed the bet");
			}
			
			if(!players.contains(entry.getKey().toLowerCase())) {
				LOGGER.info("The Player's name is not in the list");
				throw new GameException("The Player's name is not in the list");
			}

			for (GameRound round : rounds) {

				wonRound = false;
				betAmt = 0.0;

				if (round instanceof NumberGameRound) {
					NumberGameRound noRound = (NumberGameRound) round;
					betType = noRound.getNo().toString();
					if (noRound.getNo() == gameNumber) {
						wonRound = true;
						betAmt = noRound.getBetAmount() * 36;
					}
				} else if (round.getType().equals(GameType.EVEN)) {
					betType = round.getType().toString();
					if (gameNumber % 2 == 0) {
						wonRound = true;
						betAmt = round.getBetAmount() * 2;
					}
				} else {
					betType = round.getType().toString();
					if (gameNumber % 2 == 1) {
						wonRound = true;
						betAmt = round.getBetAmount() * 2;
					}
				}

				if (wonRound) {
					result = "WON";
				} else {
					result = "LOSE";
				}

				System.out.println(entry.getKey() + "   " + betType + "   " + result + "   " + betAmt);
			}
		}
	}

}