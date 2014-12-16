package com.aidancbrady.vocabserver.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.aidancbrady.vocabserver.Account;
import com.aidancbrady.vocabserver.VocabServer;
import com.aidancbrady.vocabserver.game.Game;

public final class AccountHandler 
{
	public static File dataDir = new File(getHomeDirectory() + File.separator + "Documents" + File.separator + "VocabServer" + File.separator + "Data");
	public static File dataFile = new File(dataDir, "Accounts.txt");
	
	public static void load()
	{
		System.out.println("Loading accounts list...");
		
		try {
			dataDir.mkdirs();
			
			if(!dataFile.exists())
			{
				return;
			}
			
			BufferedReader reader = new BufferedReader(new FileReader(dataFile));
			
			String readingLine;
			
			while((readingLine = reader.readLine()) != null)
			{
				String[] split = readingLine.split(VocabServer.SPLITTER_1);
				
				if(split.length >= 5)
				{
					int won = Integer.parseInt(split[3]);
					int lost = Integer.parseInt(split[4]);
					long lastLogin = Long.parseLong(split[5]);
					boolean premium = Boolean.parseBoolean(split[6]);
					
					List<String> friends = new ArrayList<String>();
					List<String> requests = new ArrayList<String>();
					List<String> requested = new ArrayList<String>();
					
					List<Game> activeGames = new ArrayList<Game>();
					List<Game> requestGames = new ArrayList<Game>();
					List<Game> pastGames = new ArrayList<Game>();
					
					Map<String, String> ownedLists = new HashMap<String, String>();
					
					String[] orig = split;
					
					for(String s : split[7].split(VocabServer.SPLITTER_2))
					{
						if(s.equals("|NULL|"))
						{
							break;
						}
						
						friends.add(s.trim());
					}
					
					for(String s : split[8].split(VocabServer.SPLITTER_2))
					{
						if(s.equals("|NULL|"))
						{
							break;
						}
						
						requests.add(s.trim());
					}
					
					for(String s : split[9].split(VocabServer.SPLITTER_2))
					{
						if(s.equals("|NULL|"))
						{
							break;
						}
						
						requested.add(s.trim());
					}
					
					split = reader.readLine().split(VocabServer.SPLITTER_1);
					
					for(String active : split)
					{
						Game g = Game.readDefault(active, VocabServer.SPLITTER_2);
						
						if(g != null)
						{
							activeGames.add(g);
						}
					}
					
					split = reader.readLine().split(VocabServer.SPLITTER_1);
					
					for(String request : split)
					{
						Game g = Game.readRequest(request, VocabServer.SPLITTER_2);
						
						if(g != null)
						{
							requestGames.add(g);
						}
					}
					
					split = reader.readLine().split(VocabServer.SPLITTER_1);
					
					for(String past : split)
					{
						Game g = Game.readDefault(past, VocabServer.SPLITTER_2);
						
						if(g != null)
						{
							pastGames.add(g);
						}
					}
					
					split = reader.readLine().split(VocabServer.SPLITTER_1);
					
					for(String entry : split)
					{
						String[] entrySplit = entry.split(VocabServer.SPLITTER_2);
						
						if(entrySplit.length == 2)
						{
							ownedLists.put(entrySplit[0], entrySplit[1]);
						}
					}
					
					split = orig;
					
					VocabServer.instance().accounts.add(new Account(split[0], split[1], split[2])
					.setGamesWon(won).setGamesLost(lost).setLastLogin(lastLogin).setPremium(premium)
					.setFriends(friends).setRequests(requests).setRequested(requested)
					.setGameData(activeGames, requestGames, pastGames).setOwnedLists(ownedLists));
				}
			}
			
			assertValidity();
			
			reader.close();
		} catch(Exception e) {
			System.err.println("An error occured while loading from data file:");
			e.printStackTrace();
		}
	}
	
	public static void save()
	{
		System.out.println("Saving accounts list...");
		
		try {
			if(dataFile.exists())
			{
				dataFile.delete();
			}
			
			dataFile.createNewFile();
			
			BufferedWriter writer = new BufferedWriter(new FileWriter(dataFile));
			
			for(Account acct : VocabServer.instance().accounts)
			{
				StringBuilder friends = new StringBuilder();
				
				if(!acct.friends.isEmpty())
				{
					for(String s : acct.friends)
					{
						friends.append(s);
						friends.append(VocabServer.SPLITTER_2);
					}
				}
				else {
					friends.append("|NULL|");
				}
				
				StringBuilder requests = new StringBuilder();
				
				if(!acct.requests.isEmpty())
				{
					for(String s : acct.requests)
					{
						requests.append(s);
						requests.append(VocabServer.SPLITTER_2);
					}
				}
				else {
					requests.append("|NULL|");
				}
				
				StringBuilder requested = new StringBuilder();
				
				if(!acct.requested.isEmpty())
				{
					for(String s : acct.requested)
					{
						requested.append(s);
						requested.append(VocabServer.SPLITTER_2);
					}
				}
				else {
					requested.append("|NULL|");
				}
				
				StringBuilder activeGames = new StringBuilder();
				
				for(Game g : acct.activeGames)
				{
					g.writeDefault(activeGames, VocabServer.SPLITTER_2);
					activeGames.append(VocabServer.SPLITTER_1);
				}
				
				StringBuilder requestGames = new StringBuilder();
				
				for(Game g : acct.requestGames)
				{
					g.writeRequest(requestGames, VocabServer.SPLITTER_2);
					activeGames.append(VocabServer.SPLITTER_1);
				}
				
				StringBuilder pastGames = new StringBuilder();
				
				for(Game g : acct.pastGames)
				{
					g.writeDefault(pastGames, VocabServer.SPLITTER_2);
					pastGames.append(VocabServer.SPLITTER_1);
				}
				
				StringBuilder ownedLists = new StringBuilder();
				
				for(Map.Entry<String, String> entry : acct.ownedLists.entrySet())
				{
					ownedLists.append(entry.getKey() + VocabServer.SPLITTER_2 + entry.getValue());
					ownedLists.append(VocabServer.SPLITTER_1);
				}
				
				writer.append(acct.username + VocabServer.SPLITTER_1 + acct.email + VocabServer.SPLITTER_1 + acct.password + 
						VocabServer.SPLITTER_1 + acct.gamesWon + VocabServer.SPLITTER_1 + acct.gamesLost + 
						VocabServer.SPLITTER_1 + acct.lastLogin + VocabServer.SPLITTER_1 + acct.premium + 
						VocabServer.SPLITTER_1 + friends + VocabServer.SPLITTER_1 + requests + VocabServer.SPLITTER_1 + requested);
				writer.newLine();
				
				writer.append(activeGames);
				writer.newLine();
				
				writer.append(requestGames);
				writer.newLine();
				
				writer.append(pastGames);
				writer.newLine();
				
				writer.append(ownedLists);
				writer.newLine();
			}
			
			writer.flush();
			writer.close();
		} catch(Exception e) {
			System.err.println("An error occured while saving to data file:");
			e.printStackTrace();
		}
	}
	
	public static void assertValidity()
	{
		for(Account acct : VocabServer.instance().accounts)
		{
			checkValidity(acct);
		}
	}
	
	public static void checkValidity(Account acct)
	{
		for(Iterator<String> iter = acct.friends.iterator(); iter.hasNext();)
		{
			if(VocabServer.instance().findAccount(iter.next()) == null)
			{
				iter.remove();
			}
		}
		
		for(Iterator<String> iter = acct.requests.iterator(); iter.hasNext();)
		{
			if(VocabServer.instance().findAccount(iter.next()) == null)
			{
				iter.remove();
			}
		}
		
		for(Iterator<String> iter = acct.requested.iterator(); iter.hasNext();)
		{
			if(VocabServer.instance().findAccount(iter.next()) == null)
			{
				iter.remove();
			}
		}
		
		for(Iterator<Game> iter = acct.activeGames.iterator(); iter.hasNext();)
		{
			Game g = iter.next();
			
			if(VocabServer.instance().findAccount(g.opponent) == null)
			{
				iter.remove();
			}
		}
		
		for(Iterator<Game> iter = acct.requestGames.iterator(); iter.hasNext();)
		{
			Game g = iter.next();
			
			if(VocabServer.instance().findAccount(g.getOtherUser(acct.username)) == null)
			{
				iter.remove();
			}
		}
		
		for(Iterator<Game> iter = acct.pastGames.iterator(); iter.hasNext();)
		{
			Game g = iter.next();
			
			if(VocabServer.instance().findAccount(g.opponent) == null)
			{
				iter.remove();
			}
		}
	}
	
	public static String getHomeDirectory()
	{
		return System.getProperty("user.home");
	}
}