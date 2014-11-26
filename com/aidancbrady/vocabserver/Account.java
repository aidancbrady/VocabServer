package com.aidancbrady.vocabserver;

import java.util.ArrayList;
import java.util.List;

public class Account 
{
	public String username;
	
	public String email;
	
	public String password;
	
	public int gamesWon;
	
	public int gamesLost;
	
	public List<String> friends = new ArrayList<String>();
	public List<String> requests = new ArrayList<String>();
	public List<String> requested = new ArrayList<String>();
	
	public Account(String user, String em, String pass)
	{
		username = user;
		email = em;
		password = pass;
	}
	
	public Account setUsername(String user)
	{
		username = user.trim();
		
		return this;
	}
	
	public Account setEmail(String em)
	{
		email = em;
		
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
	
	public Account setRequests(List<String> list)
	{
		requests = list;
		
		return this;
	}
	
	public Account setRequested(List<String> list)
	{
		requested = list;
		
		return this;
	}
	
	public double getWinRatio()
	{
		return (double)gamesWon/(double)gamesLost;
	}
}
