package com.aidancbrady.vocabserver.game;

import java.util.ArrayList;
import java.util.List;

public class Game 
{
	public static final Game DEFAULT = new Game("Guest1", "Guest2");
	
	/** If this is an active or past game, this represents the active user. 
	 * If this is a request, this represents the user that requested the game. 
	 * */
	public String user;
	
	/** If this is an active or past game, this represents the other user. 
	 * If this is a request, this represents the user that received the request. 
	 * */
	public String opponent;
	
	/** The game type this game is following. */
	public int gameType;
	
	public boolean userTurn = true;
	
	/** If this is a request, this will be the score of the requester. 
	 * If this is an active game, this represents the active user's score.
	 * */
	public List<Integer> userPoints = new ArrayList<Integer>();
	
	/** If this is a request, this will be empty.
	 * If this is an active game, this represents the other user's score.
	 * */
	public List<Integer> opponentPoints = new ArrayList<Integer>();
	
	/** True if the active user requested this game, if this is the case then 
	 * "user" represents the active user, and "opponent" represents the other
	 * player. Otherwise, "user" will represent the other player, and "opponent"
	 * will represent the active player.
	 * */
	public boolean activeRequested;
	
	/** List of 10 words that were fabricated by the game host and are still in use. */
	public List<String> activeWords = new ArrayList<String>();
	
	public String listIdentifier;
	
	/** Only used client-side */
	public boolean isRequest;
	
	/** Only used client-side */
	public String opponentEmail;
	
	public Game(String name1, String name2)
	{
		user = name1;
		opponent = name2;
	}
	
	public Game(String userName, String opponentName, boolean userReq)
	{
		activeRequested = userReq;
		
		user = activeRequested ? userName : opponentName;
		opponent = activeRequested ? opponentName : userName;
		
		isRequest = true;
	}
	
	public Game getNewRequestPair()
	{
		Game g = new Game(user, opponent, !activeRequested);
		g.gameType = gameType;
		g.listIdentifier = listIdentifier;
		g.activeWords = activeWords;
		g.userTurn = !g.userTurn;
		g.userPoints = userPoints;
		
		return g;
	}
	
	public Game getNewPair()
	{
		Game g = new Game(opponent, user);
		
		String temp = opponent;
		g.opponent = user;
		g.user = temp;
		
		List<Integer> temp1 = opponentPoints;
		g.opponentPoints = userPoints;
		g.userPoints = temp1;
		
		g.userTurn = !userTurn;
		g.activeWords = activeWords;
		g.listIdentifier = listIdentifier;
		
		return g;
	}
	
	public Game convertToActive()
	{
		if(!activeRequested) //If the active account received the request and is represented by "opponent"
		{
			String temp = opponent;
			opponent = user;
			user = temp;
			
			opponentPoints = userPoints;
			userPoints = new ArrayList<Integer>();
		}
		
		return this;
	}
	
	public Game convertToPast()
	{
		userPoints.clear();
		opponentPoints.clear();
		activeWords.clear();
		
		return this;
	}
	
	public static Game readDefault(String s, Character splitter)
	{
		String[] split = s.split(splitter.toString());
		
		Game g = new Game(split[0], split[1].trim());
		g.gameType = Integer.parseInt(split[2]);
		g.userTurn = Boolean.parseBoolean(split[3]);
		g.listIdentifier = split[4].trim();
		
		int index = g.readScoreList(split, 5, true);
		index = g.readScoreList(split, index, false);
		
		g.readWordList(split[index]);
		
		return g;
	}
	
	public static Game readRequest(String s, Character splitter)
	{
		String[] split = s.split(splitter.toString());
		
		Game g = new Game(split[1], split[2].trim(), Boolean.parseBoolean(split[0]));
		g.gameType = Integer.parseInt(split[3]);
		g.userTurn = Boolean.parseBoolean(split[4]);
		g.listIdentifier = split[5].trim();
		
		int index = g.readScoreList(split, 6, true);
		
		g.readWordList(split[index]);
		
		return g;
	}
	
	public void writeDefault(StringBuilder str, Character splitter)
	{
		str.append(user);
		str.append(splitter);
		str.append(opponent);
		str.append(splitter);
		str.append(gameType);
		str.append(splitter);
		str.append(userTurn);
		str.append(splitter);
		str.append(listIdentifier);
		str.append(splitter);
		
		writeScoreList(userPoints, str, splitter);
		writeScoreList(opponentPoints, str, splitter);
		
		writeWordList(str);
		str.append(splitter);
	}
	
	public void writeRequest(StringBuilder str, Character splitter)
	{
		str.append(activeRequested);
		str.append(splitter);
		str.append(user);
		str.append(splitter);
		str.append(opponent);
		str.append(splitter);
		str.append(gameType);
		str.append(splitter);
		str.append(userTurn);
		str.append(splitter);
		str.append(listIdentifier);
		str.append(splitter);
		
		writeScoreList(userPoints, str, splitter);
		
		writeWordList(str);
		str.append(splitter);
	}
	
	public void writeScoreList(List<Integer> score, StringBuilder str, Character split)
	{
		str.append(score.size());
		str.append(split);
		
		for(int i : score)
		{
			str.append(i);
			str.append(split);
		}
	}
	
	public int readScoreList(String[] array, int start, boolean user)
	{
		List<Integer> list = new ArrayList<Integer>();
		
		int size = Integer.parseInt(array[start]);
		int maxIndex = size;
		
		for(int i = 0; i < size; i++)
		{
			list.add(Integer.parseInt(array[start+1+i]));
			maxIndex = start+1+i;
		}
		
		return maxIndex+1;
	}
	
	public void writeWordList(StringBuilder str)
	{
		for(String s : activeWords)
		{
			str.append(s);
			str.append("&");
		}
	}
	
	public void readWordList(String s)
	{
		String[] split = s.split("&");
		
		for(String word : split)
		{
			activeWords.add(word.trim());
		}
	}
	
	public String getWinner()
	{
		int max = GameType.values()[gameType].getWinningScore();
		
		return getUserScore() == max ? user : (getOpponentScore() == max ? opponent : null);
	}
	
	public boolean hasWinner()
	{
		return getWinner() != null;
	}
	
	public String getRequester()
	{
		return user;
	}
	
	public String getRequestReceiver()
	{
		return activeRequested ? opponent : user;
	}
	
	public String getOtherUser(String s)
	{
		return user.equals(s) ? opponent : user;
	}
	
	public void setGameType(GameType type)
	{
		gameType = type.ordinal();
	}
	
	public GameType getGameType()
	{
		return GameType.values()[gameType];
	}
	
	public boolean hasUser(String name)
	{
		return user.equals(name) || opponent.equals(name);
	}
	
	public int getScore(String name)
	{
		return user.equals(name) ? getUserScore() : getOpponentScore();
	}
	
	public boolean isWinning(String name)
	{
		return user.equals(name) ? getUserScore() > getOpponentScore() : getOpponentScore() > getUserScore();
	}
	
	public String getWinning()
	{
		return isTied() ? null : (isWinning(user) ? user : opponent);
	}
	
	public boolean isTied()
	{
		return getUserScore() == getOpponentScore();
	}
	
	public int getUserScore()
	{
		int won = 0;
		
		for(int i = 0; i < userPoints.size(); i++)
		{
			if(i <= opponentPoints.size()-1)
			{
				if(userPoints.get(i) >= opponentPoints.get(i))
				{
					won++;
				}
			}
		}
		
		return won;
	}
	
	public int getOpponentScore()
	{
		int won = 0;
		
		for(int i = 0; i < opponentPoints.size(); i++)
		{
			if(i <= userPoints.size()-1)
			{
				if(opponentPoints.get(i) >= userPoints.get(i))
				{
					won++;
				}
			}
		}
		
		return won;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(obj == null || !(obj instanceof Game))
		{
			return false;
		}
		
		Game g = (Game)obj;
		
		if(!user.equals(g.user) || !opponent.equals(g.opponent) || gameType != g.gameType)
		{
			return false;
		}
		
		if(!userPoints.equals(g.userPoints) || !opponentPoints.equals(g.opponentPoints))
		{
			return false;
		}
		
		return true;
	}
	
	public static enum GameType
	{
		SINGLE(1, "Single Game"),
		BEST_OF_3(2, "Best of 3"),
		BEST_OF_5(3, "Best of 5");
		
		private String desc;
		private int max;
		
		private GameType(int i, String s)
		{
			max = i;
			desc = s;
		}
		
		public String getDescription()
		{
			return desc;
		}
		
		public int getWinningScore()
		{
			return max;
		}
	}
}
