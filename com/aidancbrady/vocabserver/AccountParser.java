package com.aidancbrady.vocabserver;

public class AccountParser 
{
	public static boolean isValidCredential(String str, boolean email)
	{
		if(!email)
		{
			for(Character c : str.toCharArray())
			{
				if(!Character.isDigit(c) && !Character.isLetter(c))
				{
					return false;
				}
			}
		}
		else {
			str = str.replace(".", "&|~|&");
			
			if(str.contains(",") || str.contains(":"))
			{
				return false;
			}
			else if(str.split("@").length != 2 || str.split("&|~|&").length < 2)
			{
				return false;
			}
		}
		
		return true;
	}
}
