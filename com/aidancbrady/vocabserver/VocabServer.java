package com.aidancbrady.vocabserver;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.aidancbrady.vocabserver.file.AccountHandler;

public class VocabServer 
{
	private static VocabServer instance = new VocabServer();
	
	public List<Account> accounts = new ArrayList<Account>();
	
	public boolean serverRunning;
	
	public static void main(String[] args)
	{
		instance().init();
	}
	
	public void init()
	{
		AccountHandler.load();
		new ServerTimer().start();
		
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
		}
	}
	
	public void quit()
	{
		serverRunning = false;
		AccountHandler.save();
		System.exit(0);
	}
	
	public static VocabServer instance()
	{
		return instance;
	}
}
