package com.aidancbrady.vocabserver;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.aidancbrady.vocabserver.file.AccountHandler;
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
			
			new ServerTimer().start();
			new ConnectionHandler().start();
			
			serverRunning = true;
			
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
						System.out.println(account.username + " " + account.password + " " + account.gamesWon + " " + account.gamesLost);
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
							System.out.println("Removed account '" + account + "'");
						}
						else {
							System.out.println("Account '" + account + "' does not exist");
						}
					}
				}
				else if(s.equals("save"))
				{
					AccountHandler.save();
					System.out.println("Successfully saved account data");
				}
				else if(s.startsWith("info"))
				{
					if(s.split(" ").length > 1)
					{
						String account = s.split(" ")[1].trim();
						Account acct = null;
						
						if((acct = findAccount(account)) != null)
						{
							System.out.println("Account details:");
							System.out.println("Username: " + acct.username);
							System.out.println("Password: " + acct.password);
							System.out.println("Games won: " + acct.gamesWon);
							System.out.println("Games lost: " + acct.gamesLost);
						}
						else {
							System.out.println("Account '" + account + "' does not exist");
						}
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
	
	public Account findAccount(String[] creds)
	{
		for(Account account : accounts)
		{
			if(account.username.equals(creds[0].trim()) && account.password.equals(creds[1].trim()))
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
	
	public void addAccount(String[] creds)
	{
		accounts.add(new Account(creds[0].trim(), creds[1].trim()));
	}
	
	public static VocabServer instance()
	{
		return instance;
	}
}
