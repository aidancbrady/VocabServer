package com.aidancbrady.vocabserver;

public class Account 
{
	public String username;
	
	public String password;
	
	public int gamesWon;
	
	public int gamesLost;
	
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
	
	public double getWinRatio()
	{
		return (double)gamesWon/(double)gamesLost;
	}
}
