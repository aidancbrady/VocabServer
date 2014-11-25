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
				
				if(split.length == 4)
				{
					int won = Integer.parseInt(split[2]);
					int lost = Integer.parseInt(split[3]);
					
					List<String> friends = new ArrayList<String>();
					
					for(String s : split[4].split(":"))
					{
						friends.add(s.trim());
					}
					
					List<String> requests = new ArrayList<String>();
					
					for(String s : split[5].split(":"))
					{
						requests.add(s.trim());
					}
					
					VocabServer.instance().accounts.add(new Account(split[0], split[1]).setGamesWon(won).setGamesLost(lost).setFriends(friends).setRequests(requests));
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
				
				writer.append(acct.username + "," + acct.password + "," + acct.gamesWon + "," + acct.gamesLost + "," + friends + "," + requests);
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