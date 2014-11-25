package com.aidancbrady.vocabserver;

public class AccountParser 
{
	public static boolean isValidCredential(String str)
	{
		for(Character c : str.toCharArray())
		{
			if(!Character.isDigit(c) && !Character.isLetter(c))
			{
				return false;
			}
		}
		
		return true;
	}
}
