package com.aidancbrady.vocabserver;

public class AccountParser 
{
	public static Character[] badChars = new Character[] {',', ':', '&', ' '};
	
	public static boolean isValidCredential(String str, boolean email)
	{
		if(!email && !isValidStr(str))
		{
			return false;
		}
		else {
			if(!isValidStr(str))
			{
				return false;
			}
			
			str = str.replace(".", "&|~|&");
			
			if(str.split("@").length != 2 || str.split("&|~|&").length < 2)
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
			for(Character c : badChars)
			{
				if(s.trim().contains(c.toString()))
				{
					return false;
				}
			}
		}
		
		return true;
	}
}
