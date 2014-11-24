package com.aidancbrady.vocabserver.net;

import java.net.Socket;

import com.aidancbrady.vocabserver.VocabServer;

public class ConnectionHandler extends Thread
{
	@Override
	public void run()
	{
		try {
			while(VocabServer.instance().serverRunning)
			{
				Socket connection = VocabServer.instance().serverSocket.accept();
				
				if(VocabServer.instance().serverRunning)
				{
					new Communication(connection).start();
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
