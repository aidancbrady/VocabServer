package com.aidancbrady.vocabserver;

import java.util.ArrayList;
import java.util.List;

public class Account 
{
	public String username;
	
	public String password;
	
	public int gamesWon;
	
	public int gamesLost;
	
	public List<String> friends = new ArrayList<String>();
	
	public Account(String user, String pass)
	{
		username = user;
		password = pass;
	}
	
	public Account setUsername(String user)
	{
		username = user.trim();
		
		return this;
	}
	
	public Account setPassword(String pass)
	{
		password = pass.trim();
		
		return this;
	}
	
	public Account setGamesWon(int won)
	{
		gamesWon = won;
		
		return this;
	}
	
	public Account setGamesLost(int lost)
	{
		gamesLost = lost;
		
		return this;
	}
	
	public Account addFriend(String username)
	{
		friends.add(username.trim());
		
		return this;
	}
	
	public Account setFriends(List<String> list)
	{
		friends = list;
		
		return this;
	}
	
	public double getWinRatio()
	{
		return (double)gamesWon/(double)gamesLost;
	}
}
