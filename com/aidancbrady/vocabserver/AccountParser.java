package com.aidancbrady.vocabserver;

public class AccountParser 
{
	public static String[] badChars = new String[] {VocabServer.SPLITTER_1, VocabServer.SPLITTER_2, "&", " ", "|", VocabServer.LIST_SPLITTER};
	
	public static boolean isValidCredential(String str, boolean email)
	{
		if(!email)
		{
			if(!isValidStr(str))
			{
				return false;
			}
		}
		else {
			if(!isValidStr(str))
			{
				return false;
			}
			
			str = str.replace(".", "&~&");
			
			if(str.split("@").length != 2 || str.split("&~&").length < 2)
			{
				return false;
			}
		}
		
		return true;
	}
	
	public static boolean isValidStr(String... creds)
	{
		for(String s : creds)
		{
			for(String s1 : badChars)
			{
				if(s.contains(s1.toString()))
				{
					return false;
				}
			}
		}
		
		return true;
	}
}
