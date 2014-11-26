package com.aidancbrady.vocabserver.game;

import java.util.ArrayList;
import java.util.List;

public class Game 
{
	public String user;
	public String opponent;
	
	public int gameType;
	
	public boolean userTurn;
	
	/** If this is a request, this number will be the score of the requester. */
	public List<Integer> userPoints = new ArrayList<Integer>();
	public List<Integer> opponentPoints = new ArrayList<Integer>();
	
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
		g.userTurn = Boolean.parseBoolean(split[2]);
		
		int index = g.readScoreList(split, 3, true);
		g.readScoreList(split, index, false);
		
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
		g.userTurn = Boolean.parseBoolean(split[3]);
		
		g.readScoreList(split, 4, true);
		
		return g;
	}
	
	public void writeDefault(StringBuilder str)
	{
		str.append(opponent);
		str.append(":");
		str.append(gameType);
		str.append(":");
		str.append(userTurn);
		str.append(":");
		
		writeScoreList(userPoints, str);
		writeScoreList(opponentPoints, str);
	}
	
	public void writeRequest(StringBuilder str)
	{
		str.append(userRequested);
		str.append(":");
		str.append(getRequestOpponent());
		str.append(":");
		str.append(gameType);
		str.append(":");
		str.append(userTurn);
		str.append(":");
		
		writeScoreList(userPoints, str);
	}
	
	public void writeScoreList(List<Integer> score, StringBuilder str)
	{
		str.append(score.size());
		str.append(":");
		
		for(int i : score)
		{
			str.append(i);
			str.append(":");
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
	
	public String getRequesterName()
	{
		return userRequested ? user : opponent;
	}
	
	public String getRequestOpponent()
	{
		return userRequested ? opponent : user;
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
	
	public static enum GameType
	{
		SINGLE,
		BEST_OF_3,
		BEST_OF_5;
	}
}
