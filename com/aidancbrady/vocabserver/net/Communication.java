package com.aidancbrady.vocabserver.net;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.aidancbrady.vocabserver.Account;
import com.aidancbrady.vocabserver.AccountParser;
import com.aidancbrady.vocabserver.VocabServer;

public class Communication extends Thread
{
	public Socket socket;
	
	public BufferedReader reader;
	
	public PrintWriter writer;
	
	public boolean disconnected;
	
	public Communication(Socket s)
	{
		socket = s;
	}
	
	@Override
	public void run()
	{
		try {
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			writer = new PrintWriter(socket.getOutputStream(), true);
			
			String reading = "";
			
			while((reading = reader.readLine()) != null && !disconnected)
			{
				String[] msg = reading.trim().split(":");
				
				if(msg[0].equals("LOGIN"))
				{
					String[] creds = msg[1].split(",");
					
					System.out.println(socket.getInetAddress() + " attempted to log in with creds " + creds[0] + ", " + creds[1]);
					
					Account acct = null;
					
					if((acct = VocabServer.instance().findAccount(creds)) != null)
					{
						writer.println("ACCEPT:" + acct.gamesWon + "," + acct.gamesLost);
					}
					else {
						writer.println("REJECT:Bad credentials");
					}
				}
				else if(msg[0].equals("REGISTER"))
				{
					String[] creds = msg[1].split(",");
					
					System.out.println(socket.getInetAddress() + " attempted to register with creds " + creds[0] + ", " + creds[1]);
					
					if(VocabServer.instance().findAccount(creds[0]) != null)
					{
						writer.println("REJECT:Username already in use");
					}
					else if(creds[0].trim().length() > 16)
					{
						writer.println("REJECT:Username must be at or below 16 characters");
					}
					else if(creds[1].trim().length() > 16)
					{
						writer.println("REJECT:Password must be at or below 16 characters");
					}
					else if(creds[1].trim().length() < 6)
					{
						writer.println("REJECT:Password must be at least 6 characters");
					}
					else if(creds[0].trim().contains(" ") || creds[1].trim().contains(" "))
					{
						writer.println("REJECT:Username and password cannot contain spaces");
					}
					else if(!AccountParser.isValidCredential(creds[0]) || !AccountParser.isValidCredential(creds[1]))
					{
						writer.println("REJECT:Special characters are not allowed");
					}
					else {
						writer.println("ACCEPT");
						VocabServer.instance().addAccount(creds);
						
						System.out.println(socket.getInetAddress() + " has created account " + creds[0].trim());
					}
				}
				else if(msg[0].equals("LFRIENDS"))
				{
					Account acct = null;
					
					if((acct = VocabServer.instance().findAccount(msg[1].trim())) != null)
					{
						StringBuilder str = new StringBuilder();
						
						for(String s : acct.friends)
						{
							str.append(s);
							str.append(":");
						}
						
						writer.println("ACCEPT:" + str);
						
						StringBuilder str1 = new StringBuilder();
						
						for(String s : acct.requested)
						{
							str1.append(s);
							str1.append(":");
						}
						
						writer.println("CONT:" + str1);
					}
					else {
						writer.println("REJECT:Unable to authenticate");
					}
				}
				else if(msg[0].equals("DELFRIEND"))
				{
					Account acct = null;
					
					if((acct = VocabServer.instance().findAccount(msg[1].trim())) != null)
					{
						Account delAcct = null;
						
						if((delAcct = VocabServer.instance().findAccount(msg[2].trim())) != null)
						{
							int type = Integer.parseInt(msg[3].trim());
							
							if(type == 0 /*Friend*/)
							{
								acct.friends.remove(delAcct.username);
								delAcct.friends.remove(acct.username);
								
								writer.println("ACCEPT");
							}
							else if(type == 1 /*Request*/)
							{
								acct.requests.remove(delAcct.username);
								delAcct.requested.remove(acct.username);
								
								writer.println("ACCEPT");
							}
							else if(type == 2 /*Requested*/)
							{
								acct.requested.remove(delAcct.username);
								delAcct.requests.remove(acct.username);
								
								writer.println("ACCEPT");
							}
							else {
								writer.println("REJECT:Couldn't parse request");
							}
						}
						else {
							writer.println("REJECT:Account doesn't exist");
						}
					}
					else {
						writer.println("REJECT:Unable to authenticate");
					}
				}
				else if(msg[0].equals("LUSERS"))
				{
					Account acct = null;
					
					if((acct = VocabServer.instance().findAccount(msg[1].trim())) != null)
					{
						String query = msg[2].trim();
						
						List<String> accts = new ArrayList<String>();
						
						int i = 0;
						
						for(Account iterAcct : VocabServer.instance().accounts)
						{
							if(iterAcct.username.toLowerCase().contains(query.toLowerCase()))
							{
								if(!iterAcct.username.equals(acct.username))
								{
									accts.add(iterAcct.username);
									i++;
								}
							}
							
							if(i == 20)
							{
								break;
							}
						}
						
						StringBuilder str = new StringBuilder();
						
						for(String s : accts)
						{
							str.append(s);
							str.append(",");
						}
						
						writer.println("ACCEPT:" + str);
					}
					else {
						writer.println("REJECT:Unable to authenticate");
					}
				}
				else if(msg[0].equals("LREQUESTS"))
				{
					Account acct = null;
					
					if((acct = VocabServer.instance().findAccount(msg[1].trim())) != null)
					{
						StringBuilder str = new StringBuilder();
						
						for(String s : acct.requests)
						{
							str.append(s);
							str.append(":");
						}
						
						writer.println("ACCEPT:" + str);
					}
					else {
						writer.println("REJECT:Unable to authenticate");
					}
				}
				else if(msg[0].equals("FRIENDREQ"))
				{
					Account acct = null;
					
					if((acct = VocabServer.instance().findAccount(msg[1].trim())) != null)
					{
						Account reqAcct = null;
						
						if((reqAcct = VocabServer.instance().findAccount(msg[2].trim())) != null)
						{
							if(acct.friends.contains(reqAcct.username))
							{
								writer.println("REJECT:User is already in your friends list");
								System.out.println(msg[1].trim() + " tried to send a FRIENDREQ request to " + msg[2].trim() + ", who was already friends");
							}
							else {
								if(!reqAcct.requests.contains(acct.username))
								{
									reqAcct.requests.add(acct.username);
								}
								
								if(!acct.requested.contains(reqAcct.username))
								{
									acct.requested.add(reqAcct.username);
								}
								
								writer.println("ACCEPT");
							}
						}
						else {
							writer.println("REJECT:Account doesn't exist");
							System.out.println(msg[1].trim() + " tried to send a FRIENDREQ request to " + msg[2].trim() + ", which doesn't exist");
						}
					}
					else {
						writer.println("REJECT:Unable to authenticate");
						System.out.println("Unable to authenticate " + msg[1].trim() + " in FRIENDREQ request");
					}
				}
				else if(msg[0].equals("REQCONF"))
				{
					Account acct = null;
					
					if((acct = VocabServer.instance().findAccount(msg[1].trim())) != null)
					{
						Account reqAcct = null;
						
						if((reqAcct = VocabServer.instance().findAccount(msg[2].trim())) != null)
						{
							acct.friends.add(reqAcct.username);
							reqAcct.friends.add(acct.username);
							acct.requests.remove(reqAcct.username);
							reqAcct.requests.remove(acct.username);
							acct.requested.remove(acct.username);
							reqAcct.requested.remove(acct.username);
							
							writer.println("ACCEPT");
						}
						else {
							writer.println("REJECT:Unable to authenticate");
						}
					}
					else {
						writer.println("REJECT:Account doesn't exist");
					}
				}
				else if(msg[0].equals("GETINFO"))
				{
					
				}
			}
			
			writer.flush();
			
			System.out.println("Closing connection with " + socket.getInetAddress());
			close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void close()
	{
		disconnected = true;
		
		try {
			socket.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
