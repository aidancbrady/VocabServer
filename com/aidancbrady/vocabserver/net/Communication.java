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
import com.aidancbrady.vocabserver.game.Game;

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
					System.out.println(socket.getInetAddress() + " attempted to log in with creds " + msg[1] + ", " + msg[2]);
					
					Account acct = null;
					
					if((acct = VocabServer.instance().findAccount(msg[1], msg[2])) != null)
					{
						writer.println("ACCEPT:" + acct.email + ":" + acct.gamesWon + ":" + acct.gamesLost);
					}
					else {
						writer.println("REJECT:Bad credentials");
					}
				}
				else if(msg[0].equals("REGISTER"))
				{
					System.out.println(socket.getInetAddress() + " attempted to register with creds " + msg[1] + ", " + msg[2] + ", " + msg[3]);
					
					if(VocabServer.instance().findAccount(msg[1]) != null)
					{
						writer.println("REJECT:Username already in use");
					}
					else if(msg[1].trim().length() > 16)
					{
						writer.println("REJECT:Username must be at or below 16 characters");
					}
					else if(msg[3].trim().length() > 16)
					{
						writer.println("REJECT:Password must be at or below 16 characters");
					}
					else if(msg[3].trim().length() < 6)
					{
						writer.println("REJECT:Password must be at least 6 characters");
					}
					else if(!AccountParser.isValidCredential(msg[1], false) || !AccountParser.isValidCredential(msg[2], true) || !AccountParser.isValidCredential(msg[3], false))
					{
						writer.println("REJECT:Special characters are not allowed");
					}
					else {
						writer.println("ACCEPT");
						VocabServer.instance().addAccount(msg[1], msg[2], msg[3]);
						
						System.out.println(socket.getInetAddress() + " has created account " + msg[1].trim());
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
							str.append(",");
							str.append(VocabServer.instance().findAccount(s).email);
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
							str.append(",");
							str.append(VocabServer.instance().findAccount(s).email);
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
							writer.println("REJECT:Account doesn't exist");
						}
					}
					else {
						writer.println("REJECT:Unable to authenticate");
					}
				}
				else if(msg[0].equals("GETINFO"))
				{
					Account acct = null;
					
					if((acct = VocabServer.instance().findAccount(msg[1].trim())) != null)
					{
						Account reqAcct = null;
						
						if((reqAcct = VocabServer.instance().findAccount(msg[2].trim())) != null)
						{
							writer.println("ACCEPT:" + reqAcct.email + ":" + reqAcct.gamesWon + ":" + reqAcct.gamesLost);
						}
						else {
							writer.println("REJECT:Account doesn't exist");
						}
					}
					else {
						writer.println("REJECT:Unable to authenticate");
					}
				}
				else if(msg[0].equals("LGAMES"))
				{
					Account acct = null;
					
					if((acct = VocabServer.instance().findAccount(msg[1].trim())) != null)
					{
						StringBuilder str = new StringBuilder();
						
						for(Game g : acct.activeGames)
						{
							g.writeDefault(str, ',');
							str.append(":");
							str.append(VocabServer.instance().findAccount(g.opponent).email);
							str.append(":");
						}
						
						writer.println("ACCEPT:" + str);
						
						StringBuilder str1 = new StringBuilder();
						
						for(Game g : acct.requestGames)
						{
							g.writeRequest(str1, ',');
							str1.append(":");
							str1.append(VocabServer.instance().findAccount(g.getOtherUser(acct.username)).email);
							str1.append(":");
						}
						
						writer.println("CONT:" + str1);
					}
					else {
						writer.println("REJECT:Unable to authenticate");
					}
				}
				else if(msg[0].equals("LPAST"))
				{
					Account acct = null;
					
					if((acct = VocabServer.instance().findAccount(msg[1].trim())) != null)
					{
						StringBuilder str = new StringBuilder();
						
						for(Game g : acct.pastGames)
						{
							g.writeDefault(str, ',');
							str.append(":");
							str.append(VocabServer.instance().findAccount(g.getOtherUser(acct.username)).email);
							str.append(":");
						}
						
						writer.println("ACCEPT:" + str);
					}
					else {
						writer.println("REJECT:Unable to authenticate");
					}
				}
				else if(msg[0].equals("CONFGAME"))
				{
					Account acct = null;
					
					if((acct = VocabServer.instance().findAccount(msg[1].trim())) != null)
					{
						Account reqAcct = null;
						
						if((reqAcct = VocabServer.instance().findAccount(msg[2].trim())) != null)
						{
							int status = canStartGame(acct, reqAcct);
							
							if(status == 0)
							{
								writer.println("ACCEPT");
							}
							else if(status == 1)
							{
								writer.println("REJECT:You already have a game in progress with " + reqAcct.username + ".");
							}
							else if(status == 2)
							{
								writer.println("REJECT:A game request already exists between you and " + reqAcct.username + ".");
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
				else if(msg[0].equals("NEWGAME"))
				{
					Account acct = null;
					
					if((acct = VocabServer.instance().findAccount(msg[1].trim())) != null)
					{
						Account reqAcct = null;
						
						if((reqAcct = VocabServer.instance().findAccount(msg[2].trim())) != null)
						{
							int status = canStartGame(acct, reqAcct);
							
							if(status == 0)
							{
								Game game = new Game(acct.username, reqAcct.username, true);
								game.gameType = Integer.parseInt(msg[3]);
								game.userPoints.add(Integer.parseInt(msg[4]));
								game.listIdentifier = msg[5].trim();
								game.readWordList(msg[6].trim());
								game.userTurn = false;
								
								acct.requestGames.add(game);
								reqAcct.requestGames.add(game.getNewRequestPair());
								
								writer.println("ACCEPT");
							}
							else if(status == 1)
							{
								writer.println("REJECT:You already have a game in progress with " + reqAcct.username + ".");
							}
							else if(status == 2)
							{
								writer.println("REJECT:A game request already exists between you and " + reqAcct.username + ".");
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
				else if(msg[0].equals("COMPGAME"))
				{
					Account acct = null;
					
					if((acct = VocabServer.instance().findAccount(msg[1].trim())) != null)
					{
						Account reqAcct = null;
						
						if((reqAcct = VocabServer.instance().findAccount(msg[2].trim())) != null)
						{
							Game g = VocabServer.instance().findActiveGame(acct, reqAcct);
							
							if(g != null)
							{
								Game pair = VocabServer.instance().findActiveGamePair(g);
								int score = Integer.parseInt(msg[3]);
								
								g.userTurn = false;
								g.userPoints.add(score);
								
								pair.userTurn = true;
								pair.opponentPoints.add(score);
								
								if(!g.hasWinner())
								{									
									if(g.userPoints.size() != g.opponentPoints.size())
									{
										g.readWordList(msg[4]);
										pair.readWordList(msg[4]);
									}
								}
								else {
									String winner = g.getWinner();
									
									acct.activeGames.remove(g);
									reqAcct.activeGames.remove(pair);
									
									acct.pastGames.add(g.convertToPast());
									reqAcct.pastGames.add(pair.convertToPast());
									
									if(winner == null)
									{
										acct.gamesWon++;
										reqAcct.gamesWon++;
									}
									else if(winner.equals(acct.username))
									{
										acct.gamesWon++;
										reqAcct.gamesLost++;
									}
									else {
										reqAcct.gamesWon++;
										acct.gamesLost++;
									}
								}
								
								writer.println("ACCEPT");
							}
							else {
								writer.println("REJECT:Game doesn't exist");
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
				else if(msg[0].equals("GAMEREQCONF"))
				{
					Account acct = null;
					
					if((acct = VocabServer.instance().findAccount(msg[1].trim())) != null)
					{
						Account reqAcct = null;
						
						if((reqAcct = VocabServer.instance().findAccount(msg[2].trim())) != null)
						{
							Game g = VocabServer.instance().findRequestGame(acct, reqAcct);
							Game pair = VocabServer.instance().findRequestGamePair(acct, reqAcct);
							
							if(g != null && pair != null)
							{
								acct.requestGames.remove(g);
								reqAcct.requestGames.remove(pair);
								
								acct.activeGames.add(g.convertToActive(acct.username));
								reqAcct.activeGames.add(pair.convertToActive(reqAcct.username));
								
								writer.println("ACCEPT");
							}
							else {
								writer.println("REJECT:Game doesn't exist");
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
				else if(msg[0].equals("DELGAME"))
				{
					Account acct = null;
					
					if((acct = VocabServer.instance().findAccount(msg[1].trim())) != null)
					{
						Account delAcct = null;
						
						if((delAcct = VocabServer.instance().findAccount(msg[2].trim())) != null)
						{
							int type = Integer.parseInt(msg[3].trim());
							
							if(type == 0 /*Active*/)
							{
								acct.activeGames.remove(VocabServer.instance().findActiveGame(acct, delAcct));
								acct.gamesLost++;
								
								delAcct.activeGames.remove(VocabServer.instance().findActiveGame(delAcct, acct));
								delAcct.gamesWon++;
								
								writer.println("ACCEPT");
							}
							else if(type == 1 /*Past*/)
							{
								int removeIndex = Integer.parseInt(msg[4].trim());
								
								Game remove = acct.pastGames.get(removeIndex);
								
								acct.pastGames.remove(remove);
								delAcct.pastGames.remove(VocabServer.instance().findPastGamePair(remove));
								
								writer.println("ACCEPT");
							}
							else if(type == 2 /*Request*/)
							{
								acct.requestGames.remove(VocabServer.instance().findRequestGamePair(delAcct, acct));
								delAcct.requestGames.remove(VocabServer.instance().findRequestGame(delAcct, acct));
								
								writer.println("ACCEPT");
							}
							else if(type == 3 /*Requested*/)
							{
								acct.requestGames.remove(VocabServer.instance().findRequestGamePair(acct, delAcct));
								delAcct.requestGames.remove(VocabServer.instance().findRequestGame(acct, delAcct));
								
								writer.println("ACCEPT");
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
				else if(msg[0].equals("CHANGEPASS"))
				{
					Account acct = null;
					
					if((acct = VocabServer.instance().findAccount(msg[1].trim())) != null)
					{
						String current = msg[2].trim();
						
						if(acct.password.equals(current))
						{
							String newPass = msg[3].trim();
							
							if(newPass.length() > 16)
							{
								writer.println("REJECT:Password must be at or below 16 characters");
							}
							else if(newPass.length() < 6)
							{
								writer.println("REJECT:Password must be at least 6 characters");
							}
							else if(!AccountParser.isValidCredential(newPass, false))
							{
								writer.println("REJECT:Special characters are not allowed");
							}
							else {
								acct.setPassword(newPass);
								
								writer.println("ACCEPT");
							}
						}
						else {
							writer.println("REJECT:Current password does not match.");
						}
					}
					else {
						writer.println("REJECT:Unable to authenticate");
					}
				}
			}
			
			writer.flush();
			
			System.out.println("Closing connection with " + socket.getInetAddress());
			close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public int canStartGame(Account acct, Account reqAcct)
	{
		int status = 0;
		
		for(Game g : reqAcct.activeGames)
		{
			if(g.hasUser(acct.username))
			{
				status = 1;
				break;
			}
		}
		
		if(status == 0)
		{
			for(Game g : reqAcct.requestGames)
			{
				if(g.hasUser(acct.username))
				{
					status = 2;
					break;
				}
			}
		}
		
		return status;
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
