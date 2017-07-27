package com.aidancbrady.vocabserver;

import java.io.File;
import java.util.Iterator;

import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;

public class NotificationManager 
{
	/**
	 * Called when a friend request is sent to another user.
	 * @param friend - friend being requested
	 */
	public static void onFriendRequest(Account requesting, Account requested)
	{
		sendNotification(requested, "REQUEST", requesting.username + " has sent you a friend request!");
	}
	
	/**
	 * Called when a user accepts one of its friend requests.
	 * @param acct - the requesting account
	 */
	public static void onFriendAccept(Account requesting, Account requested)
	{
		sendNotification(requesting, "ACCEPT", requested.username + " accepted your friend request!");
	}
	
	public static void onGameRequest(Account requesting, Account requested)
	{
		sendNotification(requested, "GAMEREQUEST", requesting.username + " has started a game with you!");
	}
	
	public static void onGameAccept(Account requesting, Account requested)
	{
		sendNotification(requesting, "GAMEACCEPT", requested.username + " accepted your game request!");
	}
	
	public static void onGameTurn(Account turnUser, Account opponent)
	{
		sendNotification(opponent, "TURN", turnUser.username + " just played. It's your turn!");
	}
	
	public static void onGameComplete(Account turnUser, Account opponent)
	{
		sendNotification(opponent, "COMPLETE", "Game over with " + turnUser.username + "! Tap for details.");
	}
	
	public static void onGameResign(Account resigned, Account opponent)
	{
		sendNotification(opponent, "RESIGN", resigned.username + " has resigned!");
	}
	
	public static void test(Account acct, String msg)
	{
		test(acct, "TEST", msg);
	}
	
	public static void test(Account acct, String type, String msg)
	{
		sendNotification(acct, type, msg);
	}
	
	private static void sendNotification(Account acct, String type, String msg)
	{
		File certFile = VocabServer.dev ? VocabServer.DEV_CERTIFICATE : VocabServer.PUB_CERTIFICATE;
		ApnsService service = null;
		
		if(VocabServer.dev)
		{
			service = APNS.newService()
					.withCert(certFile.getAbsolutePath(), VocabServer.CERT_PASS)
					.withSandboxDestination()
					.build();
		}
		else {
			service = APNS.newService()
					.withCert(certFile.getAbsolutePath(), VocabServer.CERT_PASS)
					.withProductionDestination()
					.build();
		}
		
		String payload = APNS.newPayload().alertBody(msg).customField("action", type).build();
		
		for(Iterator<String> iter = acct.deviceIDs.iterator(); iter.hasNext();)
		{
		    String id = iter.next();
		    
		    try {
    			service.push(id, payload);
		    } catch(Exception e) {
		        if(e.getMessage().contains("Invalid hex character"))
		        {
		            System.out.println("Removed invalid device ID: " + id);
		            iter.remove();
		        }
		    }
		}
	}
}
