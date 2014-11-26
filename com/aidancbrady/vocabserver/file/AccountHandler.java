package com.aidancbrady.vocabserver.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
				String[] split = readingLine.split(",");
				
				if(split.length >= 5)
				{
					int won = Integer.parseInt(split[3]);
					int lost = Integer.parseInt(split[4]);
					
					List<String> friends = new ArrayList<String>();
					List<String> requests = new ArrayList<String>();
					List<String> requested = new ArrayList<String>();
					
					List<Game> activeGames = new ArrayList<Game>();
					List<Game> requestGames = new ArrayList<Game>();
					List<Game> pastGames = new ArrayList<Game>();
					
					String[] orig = split;
					
					if(split.length > 5)
					{
						for(String s : split[5].split(":"))
						{
							friends.add(s.trim());
						}
					}
					
					if(split.length > 6)
					{
						for(String s : split[6].split(":"))
						{
							requests.add(s.trim());
						}
					}
					
					if(split.length > 7)
					{
						for(String s : split[7].split(":"))
						{
							requested.add(s.trim());
						}
					}
					
					split = reader.readLine().split(",");
					
					for(String active : split)
					{
						Game g = Game.readDefault(split[0], active);
						
						if(g != null)
						{
							activeGames.add(g);
						}
					}
					
					split = reader.readLine().split(",");
					
					for(String request : split)
					{
						Game g = Game.readRequest(split[0], request);
						
						if(g != null)
						{
							requestGames.add(g);
						}
					}
					
					split = reader.readLine().split(",");
					
					for(String past : split)
					{
						Game g = Game.readDefault(split[0], past);
						
						if(g != null)
						{
							pastGames.add(g);
						}
					}
					
					split = orig;
					
					VocabServer.instance().accounts.add(new Account(split[0], split[1], split[2])
					.setGamesWon(won).setGamesLost(lost)
					.setFriends(friends).setRequests(requests).setRequested(requested)
					.setGameData(activeGames, requestGames, pastGames));
				}
			}
			
			for(Account acct : VocabServer.instance().accounts)
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
			}
			
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
				
				for(String s : acct.friends)
				{
					friends.append(s);
					friends.append(":");
				}
				
				StringBuilder requests = new StringBuilder();
				
				for(String s : acct.requests)
				{
					requests.append(s);
					requests.append(":");
				}
				
				StringBuilder requested = new StringBuilder();
				
				for(String s : acct.requested)
				{
					requested.append(s);
					requested.append(":");
				}
				
				StringBuilder activeGames = new StringBuilder();
				
				for(Game g : acct.activeGames)
				{
					g.writeDefault(activeGames, ':');
					activeGames.append(",");
				}
				
				StringBuilder requestGames = new StringBuilder();
				
				for(Game g : acct.requestGames)
				{
					g.writeRequest(requestGames, ':');
					activeGames.append(",");
				}
				
				StringBuilder pastGames = new StringBuilder();
				
				for(Game g : acct.pastGames)
				{
					g.writeDefault(pastGames, ':');
					pastGames.append(",");
				}
				
				writer.append(acct.username + "," + acct.email + "," + acct.password + "," + acct.gamesWon + "," + acct.gamesLost + "," + friends + "," + requests + "," + requested);
				writer.newLine();
				
				writer.append(activeGames);
				writer.newLine();
				
				writer.append(requestGames);
				writer.newLine();
				
				writer.append(pastGames);
				writer.newLine();
			}
			
			writer.flush();
			writer.close();
		} catch(Exception e) {
			System.err.println("An error occured while saving to data file:");
			e.printStackTrace();
		}
	}
	
	public static String getHomeDirectory()
	{
		return System.getProperty("user.home");
	}
}