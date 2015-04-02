package com.aidancbrady.vocabserver.net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.aidancbrady.vocabserver.Account;
import com.aidancbrady.vocabserver.AccountParser;
import com.aidancbrady.vocabserver.NotificationManager;
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
		Account acct = null;
		
		try {
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			writer = new PrintWriter(socket.getOutputStream(), true);
			
			String reading = "";
			
			while((reading = reader.readLine()) != null && !disconnected)
			{
				String[] msg = reading.trim().split(VocabServer.SPLITTER_1);
				
				if(msg[0].equals("LOGIN"))
				{
					if(VocabServer.logCommands)
					{
						System.out.println(socket.getInetAddress() + " attempted to log in with creds " + msg[1] + ", " + msg[2]);
					}
					
					if((acct = VocabServer.instance().findAccount(msg[1], msg[2])) != null)
					{
						acct.login();
						writer.println(compileMsg("ACCEPT", acct.email, acct.gamesWon, acct.gamesLost));
					}
					else {
						writer.println(compileMsg("REJECT", "Bad credentials"));
					}
				}
				else if(msg[0].equals("REGISTER"))
				{
					if(VocabServer.logCommands)
					{
						System.out.println(socket.getInetAddress() + " attempted to register with creds " + msg[1] + ", " + msg[2] + ", " + msg[3]);
					}
					
					if(VocabServer.instance().findAccount(msg[1]) != null)
					{
						writer.println(compileMsg("REJECT", "Username already in use"));
					}
					else if(msg[1].trim().length() > 16)
					{
						writer.println(compileMsg("REJECT", "Username must be at or below 16 characters"));
					}
					else if(msg[3].trim().length() > 16)
					{
						writer.println(compileMsg("REJECT", "Password must be at or below 16 characters"));
					}
					else if(msg[3].trim().length() < 6)
					{
						writer.println(compileMsg("REJECT", "Password must be at least 6 characters"));
					}
					else if(!AccountParser.isValidCredential(msg[1], false) || !AccountParser.isValidCredential(msg[2], true) || !AccountParser.isValidCredential(msg[3], false))
					{
						writer.println(compileMsg("REJECT", "Special characters are not allowed"));
					}
					else {
						writer.println("ACCEPT");
						VocabServer.instance().addAccount(msg[1], msg[2], msg[3]);
						
						System.out.println(socket.getInetAddress() + " has created account " + msg[1].trim());
					}
				}
				else if(msg[0].equals("LFRIENDS"))
				{
					if(VocabServer.logCommands)
					{
						System.out.println(socket.getInetAddress() + " requested a list of friends as " + msg[1]);
					}
					
					if((acct = VocabServer.instance().findAccount(msg[1].trim())) != null)
					{
						StringBuilder str = new StringBuilder();
						
						for(String s : acct.friends)
						{
							Account iterAcct = VocabServer.instance().findAccount(s);
							
							str.append(s);
							str.append(VocabServer.SPLITTER_2);
							str.append(iterAcct.email);
							str.append(VocabServer.SPLITTER_2);
							str.append(iterAcct.lastLogin);
							str.append(VocabServer.SPLITTER_1);
						}
						
						writer.println(compileMsg("ACCEPT", str));
						
						StringBuilder str1 = new StringBuilder();
						
						for(String s : acct.requested)
						{
							str1.append(s);
							str1.append(VocabServer.SPLITTER_1);
						}
						
						writer.println(compileMsg("CONT", str1));
					}
					else {
						writer.println(compileMsg("REJECT", "Unable to authenticate"));
					}
				}
				else if(msg[0].equals("DELFRIEND"))
				{
					if(VocabServer.logCommands)
					{
						System.out.println(socket.getInetAddress() + " requested to delete friend " + msg[2] + " as " + msg[1]);
					}
					
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
							writer.println(compileMsg("REJECT", "Account doesn't exist"));
						}
					}
					else {
						writer.println(compileMsg("REJECT", "Unable to authenticate"));
					}
				}
				else if(msg[0].equals("LUSERS"))
				{
					if(VocabServer.logCommands)
					{
						System.out.println(socket.getInetAddress() + " requested a list of users with query '" + (msg.length == 3 ? msg[2] : "") + "' as " + msg[1]);
					}
					
					if((acct = VocabServer.instance().findAccount(msg[1].trim())) != null)
					{
						String query = null;
						
						if(msg.length == 3)
						{
							query = msg[2].trim();
						}
						
						List<String> accts = new ArrayList<String>();
						
						int i = 0;
						
						for(Account iterAcct : VocabServer.instance().accounts)
						{
							if(query == null || iterAcct.username.toLowerCase().contains(query.toLowerCase()))
							{
								if(!iterAcct.username.equals(acct.username) && !acct.friends.contains(iterAcct) && !acct.requests.contains(iterAcct) && !acct.requested.contains(iterAcct))
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
							str.append(VocabServer.SPLITTER_2);
						}
						
						writer.println(compileMsg("ACCEPT", str));
					}
					else {
						writer.println(compileMsg("REJECT", "Unable to authenticate"));
					}
				}
				else if(msg[0].equals("LREQUESTS"))
				{
					if(VocabServer.logCommands)
					{
						System.out.println(socket.getInetAddress() + " requested a list of friend requests as " + msg[1]);
					}
					
					if((acct = VocabServer.instance().findAccount(msg[1].trim())) != null)
					{
						StringBuilder str = new StringBuilder();
						
						for(String s : acct.requests)
						{
							str.append(s);
							str.append(VocabServer.SPLITTER_2);
							str.append(VocabServer.instance().findAccount(s).email);
							str.append(VocabServer.SPLITTER_1);
						}
						
						writer.println(compileMsg("ACCEPT", str));
					}
					else {
						writer.println(compileMsg("REJECT", "Unable to authenticate"));
					}
				}
				else if(msg[0].equals("FRIENDREQ"))
				{
					if(VocabServer.logCommands)
					{
						System.out.println(socket.getInetAddress() + " sent a friend request to " + msg[2] + " as " + msg[1]);
					}
					
					if((acct = VocabServer.instance().findAccount(msg[1].trim())) != null)
					{
						Account reqAcct = null;
						
						if((reqAcct = VocabServer.instance().findAccount(msg[2].trim())) != null)
						{
							if(acct.friends.contains(reqAcct.username))
							{
								writer.println(compileMsg("REJECT", "User is already in your friends list"));
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
								
								NotificationManager.onFriendRequest(acct, reqAcct);
								
								writer.println("ACCEPT");
							}
						}
						else {
							writer.println(compileMsg("REJECT", "Account doesn't exist"));
							System.out.println(msg[1].trim() + " tried to send a FRIENDREQ request to " + msg[2].trim() + ", which doesn't exist");
						}
					}
					else {
						writer.println(compileMsg("REJECT", "Unable to authenticate"));
						System.out.println("Unable to authenticate " + msg[1].trim() + " in FRIENDREQ request");
					}
				}
				else if(msg[0].equals("REQCONF"))
				{
					if(VocabServer.logCommands)
					{
						System.out.println(socket.getInetAddress() + " confirmed " + msg[2] + "'s friend request as " + msg[1]);
					}
					
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
							
							NotificationManager.onFriendAccept(reqAcct, acct);
							
							writer.println("ACCEPT");
						}
						else {
							writer.println(compileMsg("REJECT", "Account doesn't exist"));
						}
					}
					else {
						writer.println(compileMsg("REJECT", "Unable to authenticate"));
					}
				}
				else if(msg[0].equals("GETINFO"))
				{
					if(VocabServer.logCommands)
					{
						System.out.println(socket.getInetAddress() + " requested info of user " + msg[2] + " as " + msg[1]);
					}
					
					if(VocabServer.instance().findAccount(msg[1].trim()) != null)
					{
						Account reqAcct = null;
						
						if((reqAcct = VocabServer.instance().findAccount(msg[2].trim())) != null)
						{
							writer.println(compileMsg("ACCEPT", reqAcct.email, reqAcct.gamesWon, reqAcct.gamesLost, reqAcct.lastLogin));
						}
						else {
							writer.println(compileMsg("REJECT", "Account doesn't exist"));
						}
					}
					else {
						writer.println(compileMsg("REJECT", "Unable to authenticate"));
					}
				}
				else if(msg[0].equals("GETGAME"))
				{
					if(VocabServer.logCommands)
					{
						System.out.println(socket.getInetAddress() + " requested info of a game with user " + msg[2] + " as " + msg[1]);
						
						if((acct = VocabServer.instance().findAccount(msg[1].trim())) != null)
						{
							Account reqAcct = null;
							
							if((reqAcct = VocabServer.instance().findAccount(msg[2].trim())) != null)
							{
								Game g = VocabServer.instance().findActiveGame(acct, reqAcct);
								
								if(g != null)
								{
									StringBuilder str = new StringBuilder();
									g.writeDefault(str, VocabServer.SPLITTER_2);
									
									writer.println(compileMsg("ACCEPT", str));
								}
								else {
									writer.println(compileMsg("REJECT", "Game doesn't exist"));
								}
							}
							else {
								writer.println(compileMsg("REJECT", "Account doesn't exist"));
							}
						}
						else {
							writer.println(compileMsg("REJECT", "Unable to authenticate"));
						}
					}
				}
				else if(msg[0].equals("LGAMES_S"))
				{
					if(VocabServer.logCommands)
					{
						System.out.println(socket.getInetAddress() + " requested a short list of active games as " + msg[1]);
					}
					
					if((acct = VocabServer.instance().findAccount(msg[1].trim())) != null)
					{
						StringBuilder str = new StringBuilder();
						
						for(Game g : acct.activeGames)
						{
							str.append(g.opponent);
							str.append(VocabServer.SPLITTER_2);
							str.append(g.userTurn);
							str.append(VocabServer.SPLITTER_2);
							str.append(g.getUserScore());
							str.append(VocabServer.SPLITTER_2);
							str.append(g.getOpponentScore());
							str.append(VocabServer.SPLITTER_2);
							str.append(VocabServer.instance().findAccount(g.opponent).email);
							str.append(VocabServer.SPLITTER_1);
						}
						
						writer.println(compileMsg("ACCEPT", str));
						
						StringBuilder str1 = new StringBuilder();
						
						for(Game g : acct.requestGames)
						{
							str1.append(g.getOtherUser(acct.username));
							str1.append(VocabServer.SPLITTER_2);
							str1.append(g.userTurn);
							str1.append(VocabServer.SPLITTER_2);
							str1.append(g.getUserScore());
							str1.append(VocabServer.SPLITTER_2);
							str1.append(VocabServer.instance().findAccount(g.getOtherUser(acct.username)).email);
							str1.append(VocabServer.SPLITTER_1);
						}
						
						writer.println(compileMsg("CONT", str1));
					}
					else {
						writer.println(compileMsg("REJECT", "Unable to authenticate"));
					}
				}
				else if(msg[0].equals("LGAMES"))
				{
					if(VocabServer.logCommands)
					{
						System.out.println(socket.getInetAddress() + " requested a list of active games as " + msg[1]);
					}
					
					if((acct = VocabServer.instance().findAccount(msg[1].trim())) != null)
					{
						StringBuilder str = new StringBuilder();
						
						for(Game g : acct.activeGames)
						{
							g.writeDefault(str, VocabServer.SPLITTER_2);
							str.append(VocabServer.SPLITTER_1);
							str.append(VocabServer.instance().findAccount(g.opponent).email);
							str.append(VocabServer.SPLITTER_1);
						}
						
						writer.println(compileMsg("ACCEPT", str));
						
						StringBuilder str1 = new StringBuilder();
						
						for(Game g : acct.requestGames)
						{
							g.writeRequest(str1, VocabServer.SPLITTER_2);
							str1.append(VocabServer.SPLITTER_1);
							str1.append(VocabServer.instance().findAccount(g.getOtherUser(acct.username)).email);
							str1.append(VocabServer.SPLITTER_1);
						}
						
						writer.println(compileMsg("CONT", str1));
					}
					else {
						writer.println(compileMsg("REJECT", "Unable to authenticate"));
					}
				}
				else if(msg[0].equals("LPAST"))
				{
					if(VocabServer.logCommands)
					{
						System.out.println(socket.getInetAddress() + " requested a list of past games as " + msg[1]);
					}
					
					if((acct = VocabServer.instance().findAccount(msg[1].trim())) != null)
					{
						StringBuilder str = new StringBuilder();
						
						for(Game g : acct.pastGames)
						{
							g.writeDefault(str, VocabServer.SPLITTER_2);
							str.append(VocabServer.SPLITTER_1);
							str.append(VocabServer.instance().findAccount(g.getOtherUser(acct.username)).email);
							str.append(VocabServer.SPLITTER_1);
						}
						
						writer.println(compileMsg("ACCEPT", str));
					}
					else {
						writer.println(compileMsg("REJECT", "Unable to authenticate"));
					}
				}
				else if(msg[0].equals("CONFGAME"))
				{
					if(VocabServer.logCommands)
					{
						System.out.println(socket.getInetAddress() + " requested a game confirmation with user " + msg[2] + " as " + msg[1]);
					}
					
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
								writer.println(compileMsg("REJECT", "You already have a game in progress with " + reqAcct.username + "."));
							}
							else if(status == 2)
							{
								writer.println(compileMsg("REJECT", "A game request already exists between you and " + reqAcct.username + "."));
							}
						}
						else {
							writer.println(compileMsg("REJECT", "Account doesn't exist"));
						}
					}
					else {
						writer.println(compileMsg("REJECT", "Unable to authenticate"));
					}
				}
				else if(msg[0].equals("NEWGAME"))
				{
					if(VocabServer.logCommands)
					{
						System.out.println(socket.getInetAddress() + " requested to start a new game with user " + msg[2] + " as " + msg[1]);
					}
					
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
								game.setList(msg[5].trim(), msg[6].trim());
								game.readWordList(msg[7].trim());
								game.userTurn = false;
								
								acct.requestGames.add(game);
								reqAcct.requestGames.add(game.getNewRequestPair());
								
								NotificationManager.onGameRequest(acct, reqAcct);
								
								writer.println("ACCEPT");
							}
							else if(status == 1)
							{
								writer.println(compileMsg("REJECT", "You already have a game in progress with " + reqAcct.username + "."));
							}
							else if(status == 2)
							{
								writer.println(compileMsg("REJECT", "A game request already exists between you and " + reqAcct.username + "."));
							}
						}
						else {
							writer.println(compileMsg("REJECT", "Account doesn't exist"));
						}
					}
					else {
						writer.println(compileMsg("REJECT", "Unable to authenticate"));
					}
				}
				else if(msg[0].equals("COMPGAME"))
				{
					if(VocabServer.logCommands)
					{
						System.out.println(socket.getInetAddress() + " completed an individual game with " + msg[2] + " as " + msg[1]);
					}
					
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
									
									NotificationManager.onGameTurn(acct, reqAcct);
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
									
									NotificationManager.onGameComplete(acct, reqAcct);
								}
								
								writer.println("ACCEPT");
							}
							else {
								writer.println(compileMsg("REJECT", "Game doesn't exist"));
							}
						}
						else {
							writer.println(compileMsg("REJECT", "Account doesn't exist"));
						}
					}
					else {
						writer.println(compileMsg("REJECT", "Unable to authenticate"));
					}
				}
				else if(msg[0].equals("GAMEREQCONF"))
				{
					if(VocabServer.logCommands)
					{
						System.out.println(socket.getInetAddress() + " accepted " + msg[2] + "'s game request as " + msg[1]);
					}
					
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
								
								NotificationManager.onGameAccept(reqAcct, acct);
								
								writer.println("ACCEPT");
							}
							else {
								writer.println(compileMsg("REJECT", "Game doesn't exist"));
							}
						}
						else {
							writer.println(compileMsg("REJECT", "Account doesn't exist"));
						}
					}
					else {
						writer.println(compileMsg("REJECT", "Unable to authenticate"));
					}
				}
				else if(msg[0].equals("DELGAME"))
				{
					if(VocabServer.logCommands)
					{
						System.out.println(socket.getInetAddress() + " requested to delete a game with " + msg[2] + " as " + msg[1]);
					}
					
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
								
								NotificationManager.onGameResign(acct, delAcct);
								
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
							writer.println(compileMsg("REJECT", "Account doesn't exist"));
						}
					}
					else {
						writer.println(compileMsg("REJECT", "Unable to authenticate"));
					}
				}
				else if(msg[0].equals("CHANGEPASS"))
				{
					if(VocabServer.logCommands)
					{
						System.out.println(socket.getInetAddress() + " requested a password change of " + msg[3] + " as " + msg[1]);
					}
					
					if((acct = VocabServer.instance().findAccount(msg[1].trim())) != null)
					{
						String current = msg[2].trim();
						
						if(acct.password.equals(current))
						{
							String newPass = msg[3].trim();
							
							if(newPass.length() > 16)
							{
								writer.println(compileMsg("REJECT", "Password must be at or below 16 characters"));
							}
							else if(newPass.length() < 6)
							{
								writer.println(compileMsg("REJECT", "Password must be at least 6 characters"));
							}
							else if(!AccountParser.isValidCredential(newPass, false))
							{
								writer.println(compileMsg("REJECT", "Special characters are not allowed"));
							}
							else {
								acct.setPassword(newPass);
								
								writer.println("ACCEPT");
							}
						}
						else {
							writer.println(compileMsg("REJECT", "Current password does not match."));
						}
					}
					else {
						writer.println(compileMsg("REJECT", "Unable to authenticate"));
					}
				}
				else if(msg[0].equals("JOINGAME"))
				{
					if((acct = VocabServer.instance().findAccount(msg[1].trim())) != null)
					{
						boolean found = false;
						
						if(VocabServer.instance().searching.size() > 0)
						{
							for(Map.Entry<String, String> entry : VocabServer.instance().searching.entrySet())
							{
								if(entry.getValue() != Account.DEFAULT.username)
								{
									VocabServer.instance().searching.put(entry.getKey(), acct.username);
									writer.println(compileMsg("ACCEPT", entry.getKey()));
									found = true;
									break;
								}
							}
						}
						
						if(!found)
						{
							VocabServer.instance().searching.put(acct.username, Account.DEFAULT.username);
							int count = 60;
							
							while(count > 0)
							{
								String user = VocabServer.instance().searching.get(acct.username);
								
								if(user != null && user != Account.DEFAULT.username)
								{
									writer.println(compileMsg("ACCEPT", VocabServer.instance().searching.get(acct.username)));
									found = true;
									break;
								}
								
								count--;
								Thread.sleep(1000);
							}
							
							if(!found)
							{
								writer.println("REJECT:Timeout");
							}
						}
					}
				}
				else if(msg[0].equals("CONFLIST"))
				{
					if(VocabServer.logCommands)
					{
						System.out.println(socket.getInetAddress() + " requested a new list confirmation of id " + msg[2] + " as " + msg[1]);
					}
					
					if((acct = VocabServer.instance().findAccount(msg[1].trim())) != null)
					{
						String listID = msg[2].trim();
						
						if(acct.ownedLists.size() >= 5 && !acct.premium)
						{
							writer.println(compileMsg("REJECT", "You've reached the maximum amount of custom lists."));
						}
						else if(!listID.equals(VocabServer.NULL) && acct.ownedLists.get(listID) != null)
						{
							writer.println(compileMsg("REJECT", "You already have a custom list with that identifier!"));
						}
						else {
							writer.println("ACCEPT");
						}
					}
					else {
						writer.println(compileMsg("REJECT", "Unable to authenticate"));
					}
				}
				else if(msg[0].equals("LLISTS"))
				{
					if(VocabServer.logCommands)
					{
						System.out.println(socket.getInetAddress() + " requested a list of uploaded lists as " + msg[1]);
					}
					
					if((acct = VocabServer.instance().findAccount(msg[1].trim())) != null)
					{
						StringBuilder str = new StringBuilder();
						
						for(Map.Entry<String, String> entry : acct.ownedLists.entrySet())
						{
							str.append(entry.getKey() + VocabServer.SPLITTER_2 + entry.getValue());
							str.append(VocabServer.SPLITTER_1);
						}
						
						writer.println(compileMsg("ACCEPT", str));
					}
					else {
						writer.println(compileMsg("REJECT", "Unable to authenticate"));
					}
				}
				else if(msg[0].equals("UPLOAD"))
				{
					if(VocabServer.logCommands)
					{
						System.out.println(socket.getInetAddress() + " requested to upload list of id " + msg[2] + " as " + msg[1]);
					}
					
					if((acct = VocabServer.instance().findAccount(msg[1].trim())) != null)
					{
						String listID = msg[2].trim();
						String[] listData = Arrays.copyOfRange(msg, 3, msg.length);
						
						String url = "http://104.236.13.142/Lists/" + acct.username + "/" + listID + ".txt";
						
						if(createList(acct, listID, listData))
						{
							acct.ownedLists.put(listID, url);
						}
						
						writer.println(compileMsg("ACCEPT", acct.ownedLists.size(), url));
					}
					else {
						writer.println(compileMsg("REJECT", "Unable to authenticate"));
					}
				}
				else if(msg[0].equals("DELLIST"))
				{
					if(VocabServer.logCommands)
					{
						System.out.println(socket.getInetAddress() + " requested to delete a list of id " + msg[2] + " as " + msg[1]);
					}
					
					if((acct = VocabServer.instance().findAccount(msg[1].trim())) != null)
					{
						String listID = msg[2].trim();
						
						deleteList(acct, listID);
						acct.ownedLists.remove(listID);
						
						writer.println("ACCEPT");
					}
					else {
						writer.println(compileMsg("REJECT", "Unable to authenticate"));
					}
				}
				else if(msg[0].equals("EDITLIST"))
				{
					if(VocabServer.logCommands)
					{
						System.out.println(socket.getInetAddress() + " requested to edit list of id " + msg[2] + " as " + msg[1]);
					}
					
					if((acct = VocabServer.instance().findAccount(msg[1].trim())) != null)
					{
						String listID = msg[2].trim();
						String[] listData = Arrays.copyOfRange(msg, 3, msg.length);
						
						deleteList(acct, listID);
						createList(acct, listID, listData);
						
						writer.println("ACCEPT");
					}
					else {
						writer.println(compileMsg("REJECT", "Unable to authenticate"));
					}
				}
				else if(msg[0].equals("PUSHID"))
				{
					if(VocabServer.logCommands)
					{
						System.out.println(socket.getInetAddress() + " sent device ID of " + msg[2] + " as " + msg[1]);
					}
					
					if((acct = VocabServer.instance().findAccount(msg[1].trim())) != null)
					{
						String deviceID = msg[2].trim();
						
						for(Account iterAcct : VocabServer.instance().accounts)
						{
							iterAcct.deviceIDs.remove(deviceID);
						}
						
						acct.deviceIDs.add(deviceID);
						
						writer.println("ACCEPT");
					}
					else {
						writer.println(compileMsg("REJECT", "Unable to authenticate"));
					}
				}
			}
			
			writer.flush();
			
			if(VocabServer.logConnections)
			{
				System.out.println("Closing connection with " + socket.getInetAddress());
			}
			
			close();
		} catch(Throwable t) {
			t.printStackTrace();
		}
		
		if(acct != null)
		{
			VocabServer.instance().searching.remove(acct.username);
		}
	}
	
	public static void deleteList(Account acct, String listID)
	{
		File userDir = new File(VocabServer.LISTS_DIR, acct.username);
		File listFile = new File(userDir, listID + ".txt");
		
		if(listFile.exists())
		{
			listFile.delete();
		}
	}
	
	public static boolean createList(Account acct, String listID, String[] listData)
	{
		try {
			File userDir = new File(VocabServer.LISTS_DIR, acct.username);
			
			try {
				Files.createDirectories(userDir.toPath());
			} catch(Throwable t) {
				t.printStackTrace();
			}
			
			File listFile = new File(userDir, listID + ".txt");
			
			if(listFile.exists())
			{
				listFile.delete();
			}
			
			listFile.createNewFile();
			
			BufferedWriter writer = new BufferedWriter(new FileWriter(listFile));
			
			for(String entry : listData)
			{
				String[] split = entry.split(VocabServer.SPLITTER_2);
				
				if(split.length == 2)
				{
					writer.write(split[0] + ">" + split[1]);
					writer.newLine();
				}
			}
			
			writer.flush();
			writer.close();
			
			return true;
		} catch(Exception e) {
			System.err.println("Couldn't create new word list:");
			e.printStackTrace();
		}
		
		return false;
	}
	
	public static String compileMsg(Object... strings)
	{
		StringBuilder str = new StringBuilder();
		
		for(int i = 0; i < strings.length; i++)
		{
			str.append(strings[i]);
			
			if(i < strings.length-1)
			{
				str.append(VocabServer.SPLITTER_1);
			}
		}
		
		return str.toString();
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
