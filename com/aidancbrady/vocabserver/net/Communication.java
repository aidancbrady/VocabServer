package com.aidancbrady.vocabserver.net;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.aidancbrady.vocabserver.Account;
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
					
					System.out.println(socket.getInetAddress() + " attempted to log in with creds " + creds);
					
					Account acct = null;
					
					if((acct = VocabServer.instance().findAccount(msg[1].split(","))) != null)
					{
						writer.println("ACCEPT:" + acct.gamesWon + "," + acct.gamesLost);
					}
					else {
						writer.println("REJECT:Bad credentials");
					}
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
