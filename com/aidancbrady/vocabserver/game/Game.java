package com.aidancbrady.vocabserver.game;

public class Game 
{
	public String user;
	public String opponent;
	
	public int gameType;
	
	public boolean userTurn;
	
	/** If this is a request, this number will be the score of the requester. */
	public int userScore;
	public int opponentScore;
	
	/** True if the active user requested this game, if this is the case then 
	 * "user" represents the active user, and "opponent" represents the other
	 * player. Otherwise, "user" will represent the other player, and "opponent"
	 * will represent the active player.
	 * */
	public boolean userRequested;
	
	public Game(String name1, String name2)
	{
		user = name1;
		opponent = name2;
	}
	
	public Game(String userName, String opponentName, boolean userReq)
	{
		userRequested = userReq;
		
		user = userRequested ? userName : opponentName;
		opponent = userRequested ? opponentName : userName;
	}
	
	public static Game readDefault(String user, String s)
	{
		String[] split = s.split(":");
		
		if(split.length != 4)
		{
			return null;
		}
		
		Game g = new Game(user, split[0]);
		g.gameType = Integer.parseInt(split[1]);
		g.setScore(Integer.parseInt(split[2]), Integer.parseInt(split[3]));
		g.userTurn = Boolean.parseBoolean(split[4]);
		
		return g;
	}
	
	public static Game readRequest(String user, String s)
	{
		String[] split = s.split(":");
		
		if(split.length != 4)
		{
			return null;
		}
		
		Game g = new Game(user, split[1], Boolean.parseBoolean(split[0]));
		g.gameType = Integer.parseInt(split[2]);
		g.userScore = Integer.parseInt(split[3]);
		g.userTurn = Boolean.parseBoolean(split[4]);
		
		return g;
	}
	
	public void writeDefault(StringBuilder str)
	{
		str.append(opponent);
		str.append(":");
		str.append(gameType);
		str.append(":");
		str.append(userScore);
		str.append(":");
		str.append(opponentScore);
		str.append(":");
		str.append(userTurn);
	}
	
	public String getRequesterName()
	{
		return userRequested ? user : opponent;
	}
	
	public String getRequestOpponent()
	{
		return userRequested ? opponent : user;
	}
	
	public void setScore(int uScore, int oScore)
	{
		userScore = uScore;
		opponentScore = oScore;
	}
	
	public void writeRequest(StringBuilder str)
	{
		str.append(userRequested);
		str.append(":");
		str.append(getRequestOpponent());
		str.append(":");
		str.append(gameType);
		str.append(":");
		str.append(userScore);
		str.append(":");
		str.append(userTurn);
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
		return user.equals(name) ? userScore : opponentScore;
	}
	
	public boolean isWinning(String name)
	{
		return user.equals(name) ? userScore > opponentScore : opponentScore > userScore;
	}
	
	public String getWinning()
	{
		return isTied() ? null : (isWinning(user) ? user : opponent);
	}
	
	public boolean isTied()
	{
		return userScore == opponentScore;
	}
	
	public static enum GameType
	{
		SINGLE,
		BEST_OF_3,
		BEST_OF_5;
	}
}
