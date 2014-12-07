package com.aidancbrady.vocabserver;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.aidancbrady.vocabserver.file.AccountHandler;
import com.aidancbrady.vocabserver.game.Game;
import com.aidancbrady.vocabserver.net.ConnectionHandler;

public class VocabServer 
{
	private static VocabServer instance = new VocabServer();
	
	public List<Account> accounts = new ArrayList<Account>();
	
	public boolean serverRunning;
	
	public static final int SERVER_PORT = 26830;
	
	public ServerSocket serverSocket;
	
	public static void main(String[] args)
	{
		instance().init();
	}
	
	public void init()
	{
		AccountHandler.load();
		
		try {
			serverSocket = new ServerSocket(SERVER_PORT);
			serverRunning = true;
			
			new ServerTimer().start();
			new ConnectionHandler().start();
			
			System.out.println("Initiated server");
			
			Scanner scan = new Scanner(System.in);
			
			while(scan.hasNext())
			{
				String s = scan.nextLine();
				
				if(s.equals("stop") || s.equals("quit"))
				{
					System.out.println("Shutting down");
					quit();
				}
				else if(s.equals("accounts"))
				{
					System.out.println("Printing entire account list...");
					
					for(Account account : accounts)
					{
						System.out.println(account.username + " " + account.email + " " + account.password);
					}
				}
				else if(s.startsWith("delete") || s.startsWith("remove"))
				{
					if(s.split(" ").length > 1)
					{
						String account = s.split(" ")[1].trim();
						
						if(findAccount(account) != null)
						{
							accounts.remove(findAccount(account));
							AccountHandler.assertValidity();
							System.out.println("Removed account '" + account + "'");
						}
						else {
							System.out.println("Account '" + account + "' does not exist");
						}
					}
					else {
						System.out.println("Invalid parameters");
					}
				}
				else if(s.equals("save"))
				{
					AccountHandler.save();
					System.out.println("Successfully saved account data");
				}
				else if(s.equals("load"))
				{
					AccountHandler.load();
					System.out.println("Successfully loaded account data");
				}
				else if(s.equals("validate") || s.equals("validify"))
				{
					AccountHandler.assertValidity();
					System.out.println("Successfully asserted validity of user list");
				}
				else if(s.startsWith("info"))
				{
					if(s.split(" ").length > 1)
					{
						String account = s.split(" ")[1].trim();
						Account acct = null;
						
						if((acct = findAccount(account)) != null)
						{
							StringBuilder friends = new StringBuilder();
							
							for(String s1 : acct.friends)
							{
								friends.append(s1);
								friends.append(", ");
							}
							
							StringBuilder requests = new StringBuilder();
							
							for(String s1 : acct.requests)
							{
								requests.append(s1);
								requests.append(", ");
							}
							
							StringBuilder requested = new StringBuilder();
							
							for(String s1 : acct.requested)
							{
								requested.append(s1);
								requested.append(", ");
							}
							
							System.out.println("Account details:");
							System.out.println("Username: " + acct.username);
							System.out.println("Email: " + acct.email);
							System.out.println("Password: " + acct.password);
							System.out.println("Score: " + acct.gamesWon + "-" + acct.gamesLost);
							System.out.println("Friends: " + friends);
							System.out.println("Requests: " + requests);
							System.out.println("Requested: " + requested);
							System.out.println("Active games: " + acct.activeGames.size());
							System.out.println("Request games: " + acct.requestGames.size());
							System.out.println("Past games: " + acct.pastGames.size());
						}
						else {
							System.out.println("Account '" + account + "' does not exist");
						}
					}
					else {
						System.out.println("Invalid parameters");
					}
				}
				else if(s.startsWith("create"))
				{
					String[] split = s.split(" ");
					
					if(split.length == 4)
					{
						if(AccountParser.isValidCredential(split[1].trim(), false) && AccountParser.isValidCredential(split[2].trim(), true) && AccountParser.isValidCredential(split[3].trim(), false))
						{
							accounts.add(new Account(split[1].trim(), split[2].trim(), split[3].trim()));
							System.out.println("Created account '" + split[1].trim() + "' '" + split[2].trim() + "' '" + split[3].trim() + "'");
						}
						else {
							System.out.println("Invalid amount of chars");
						}
					}
					else {
						System.out.println("Invalid parameters");
					}
				}
				else {
					System.out.println("Unknown command");
				}
			}
			
			scan.close();
		} catch(Exception e) {
			System.out.println("Unable to start server");
			e.printStackTrace();
		}
		
		serverRunning = false;
	}
	
	public void quit()
	{
		serverRunning = false;
		AccountHandler.save();
		
		try {
			serverSocket.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		System.exit(0);
	}
	
	public Account findAccount(String username, String password)
	{
		for(Account account : accounts)
		{
			if(account.username.equals(username) && account.password.equals(password))
			{
				return account;
			}
		}
		
		return null;
	}
	
	public Account findAccount(String username)
	{
		for(Account account : accounts)
		{
			if(account.username.equals(username.trim()))
			{
				return account;
			}
		}
		
		return null;
	}
	
	public Game findActiveGame(Account user, Account opponent)
	{
		for(Game g : user.activeGames)
		{
			if(g.hasUser(opponent.username))
			{
				return g;
			}
		}
		
		return null;
	}
	
	public Game findActiveGamePair(Game g)
	{
		Account opponent = findAccount(g.opponent);
		
		for(Game pair : opponent.activeGames)
		{
			if(pair.hasUser(g.user))
			{
				return pair;
			}
		}
		
		return null;
	}
	
	public Game findPastGame(Account user, Account opponent)
	{
		for(Game g : user.pastGames)
		{
			if(g.hasUser(opponent.username))
			{
				return g;
			}
		}
		
		return null;
	}
	
	public Game findPastGamePair(Game g)
	{
		Account opponent = findAccount(g.opponent);
		Game pair = g.getNewPair();
		
		for(Game iterG : opponent.pastGames)
		{
			if(iterG.equals(pair))
			{
				return iterG;
			}
		}
		
		return null;
	}
	
	/**
	 * Finds a game that "opponent" requested against "user," from user's perspective.
	 * In other words, the user being requested's perspective.
	 */
	public Game findRequestGame(Account user, Account opponent)
	{
		for(Game g : user.requestGames)
		{
			if(!g.activeRequested && g.hasUser(opponent.username))
			{
				return g;
			}
		}
		
		return null;
	}
	
	/** 
	 * Finds a game that "opponent" requested against "user," from opponent's perspective.
	 * In other words, the requesting user's perspective.
	 */
	public Game findRequestGamePair(Account user, Account opponent)
	{
		for(Game g : opponent.requestGames)
		{
			if(g.activeRequested && g.hasUser(user.username))
			{
				return g;
			}
		}
		
		return null;
	}
	
	public void addAccount(String username, String email, String password)
	{
		accounts.add(new Account(username.trim(), email.trim(), password.trim()));
	}
	
	public static VocabServer instance()
	{
		return instance;
	}
}
