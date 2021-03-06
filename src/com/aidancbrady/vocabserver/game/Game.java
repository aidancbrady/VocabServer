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
	
	private String listName;
	private String listURL;
	
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
		this(userName, opponentName);
		
		activeRequested = userReq;
		isRequest = true;
	}
	
	public Game getNewRequestPair()
	{
		Game g = new Game(user, opponent, !activeRequested);
		g.gameType = gameType;
		g.listName = listName;
		g.listURL = listURL;
		g.activeWords = activeWords;
		g.userTurn = !userTurn;
		g.userPoints = new ArrayList<Integer>(userPoints);
		
		return g;
	}
	
	public Game getNewPair()
	{
		Game g = new Game(opponent, user);
		
		String temp = opponent;
		g.opponent = user;
		g.user = temp;
		
		g.opponentPoints = new ArrayList<Integer>(userPoints);
		g.userPoints = new ArrayList<Integer>(opponentPoints);
		
		g.userTurn = !userTurn;
		g.activeWords = activeWords;
		g.listName = listName;
		g.listURL = listURL;
		
		return g;
	}
	
	public Game convertToActive(String userPerspective)
	{
		if(!user.equals(userPerspective)) //If requesting user equals perspective user
		{
			String temp = opponent;
			opponent = user;
			user = temp;
			
			opponentPoints = userPoints;
			userPoints = new ArrayList<Integer>();
		}
		
		isRequest = false;
		
		return this;
	}
	
	public Game convertToPast()
	{
		activeWords.clear();
		
		return this;
	}
	
	public static Game readDefault(String s, String splitter)
	{
		String[] split = s.split(splitter);
		
		if(split.length < 4)
		{
			return null;
		}
		
		Game g = new Game(split[0], split[1].trim());
		g.gameType = Integer.parseInt(split[2]);
		g.userTurn = Boolean.parseBoolean(split[3]);
		g.listName = split[4].trim();
		g.listURL = split[5].trim();
		
		int index = g.readScoreList(split, 6, true);
		index = g.readScoreList(split, index, false);
		
		g.readWordList(split[index]);
		
		return g;
	}
	
	public static Game readRequest(String s, String splitter)
	{
		String[] split = s.split(splitter);
		
		if(split.length < 4)
		{
			return null;
		}
		
		Game g = new Game(split[1], split[2].trim(), Boolean.parseBoolean(split[0]));
		g.gameType = Integer.parseInt(split[3]);
		g.userTurn = Boolean.parseBoolean(split[4]);
		g.listName = split[5].trim();
		g.listURL = split[6].trim();
		
		int index = g.readScoreList(split, 7, true);
		
		g.readWordList(split[index]);
		
		return g;
	}
	
	public void writeDefault(StringBuilder str, String splitter)
	{
		str.append(user);
		str.append(splitter);
		str.append(opponent);
		str.append(splitter);
		str.append(gameType);
		str.append(splitter);
		str.append(userTurn);
		str.append(splitter);
		str.append(listName);
		str.append(splitter);
		str.append(listURL);
		str.append(splitter);
		
		writeScoreList(userPoints, str, splitter);
		writeScoreList(opponentPoints, str, splitter);
		
		writeWordList(str);
		str.append(splitter);
	}
	
	public void writeRequest(StringBuilder str, String splitter)
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
		str.append(listName);
		str.append(splitter);
		str.append(listURL);
		str.append(splitter);
		
		writeScoreList(userPoints, str, splitter);
		
		writeWordList(str);
		str.append(splitter);
	}
	
	public void writeScoreList(List<Integer> score, StringBuilder str, String splitter)
	{
		str.append(score.size());
		str.append(splitter);
		
		for(int i : score)
		{
			str.append(i);
			str.append(splitter);
		}
	}
	
	public int readScoreList(String[] array, int start, boolean user)
	{
		List<Integer> list = new ArrayList<Integer>();
		
		int size = Integer.parseInt(array[start]);
		int maxIndex = size+start;
		
		for(int i = 0; i < size; i++)
		{
			list.add(Integer.parseInt(array[start+1+i]));
			maxIndex = start+1+i;
		}
		
		if(user)
		{
			userPoints = list;
		}
		else {
			opponentPoints = list;
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
		
		if(activeWords.isEmpty())
		{
			str.append("null");
		}
	}
	
	public void readWordList(String s)
	{		
		String[] split = s.split("&");
		
		if(split.length == 1 && split[0].equals("null"))
		{
			return;
		}
		
		activeWords.clear();
		
		for(String word : split)
		{
			activeWords.add(word.trim());
		}
	}
	
	public String getWinner()
	{
		int max = GameType.values()[gameType].getWinningScore();
		
		if(getUserScore() == max && getOpponentScore() == max)
		{
			return null;
		}
		
		return getUserScore() == max ? user : (getOpponentScore() == max ? opponent : null);
	}
	
	public boolean hasWinner()
	{
		int max = GameType.values()[gameType].getWinningScore();
		
		return getUserScore() == max || getOpponentScore() == max;
	}
	
	public String getListName()
	{
		return listName;
	}
	
	public String getListURL()
	{
		return listURL;
	}
	
	public void setList(String name, String url)
	{
		listName = name;
		listURL = url;
	}
	
	public String getRequester()
	{
		return user;
	}
	
	public String getRequestReceiver()
	{
		return opponent;
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
