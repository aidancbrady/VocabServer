package com.aidancbrady.vocabserver.game;

import java.util.ArrayList;
import java.util.List;

public class Game 
{
	/** If this is an active or past game, this represents the active user. 
	 * If this is a request, this represents the user that requested the game. 
	 * */
	public String user;
	
	/** If this is an active or past game, this represents the other user. 
	 * If this is a request, this represents the user that received the request. 
	 * */
	public String opponent;
	
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
	}
	
	public Game getRequestPair()
	{
		Game g = new Game(opponent, user, !activeRequested);
		g.gameType = gameType;
		g.userTurn = !g.userTurn;
		
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
	
	public void writeDefault(StringBuilder str, Character split)
	{
		str.append(opponent);
		str.append(split);
		str.append(gameType);
		str.append(split);
		str.append(userTurn);
		str.append(split);
		
		writeScoreList(userPoints, str, split);
		writeScoreList(opponentPoints, str, split);
	}
	
	public void writeRequest(StringBuilder str, Character split)
	{
		str.append(activeRequested);
		str.append(split);
		str.append(getRequestOpponent());
		str.append(split);
		str.append(gameType);
		str.append(split);
		str.append(userTurn);
		str.append(split);
		
		writeScoreList(userPoints, str, split);
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
	
	public String getRequesterName()
	{
		return activeRequested ? user : opponent;
	}
	
	public String getRequestOpponent()
	{
		return activeRequested ? opponent : user;
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
