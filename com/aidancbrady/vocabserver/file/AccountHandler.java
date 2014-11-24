package com.aidancbrady.vocabserver.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

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
					
					VocabServer.instance().accounts.add(new Account(split[0], split[1]).setGamesWon(won).setGamesLost(lost));
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
				writer.append(acct.username + "," + acct.password + "," + acct.gamesWon + "," + acct.gamesLost);
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