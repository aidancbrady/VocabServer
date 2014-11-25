package com.aidancbrady.vocabserver.net;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

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
					
				}
				else if(msg[0].equals("FRIENDREQ"))
				{
					
				}
			}
			
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
