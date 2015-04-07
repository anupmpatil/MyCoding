import java.util.*;
import java.lang.*;

/* Reverse sentence in given input string
 * 
 */

class reverseString
{
	public static void main(String [] args)
	{
		StringBuffer input = new StringBuffer("Do or do not, there is no try.");
		reverseWords(input);
		System.out.println("After function call = " + input);
		reverseWord(input, 0, input.length()-1);
		System.out.println("After function call = " + input);
		/*
		Integer num = new Integer(20);
		add(num);
		System.out.println("After function call = " + num);
		String str = "new String";
		change(str);
		System.out.println("After function call = " + str);*/
	}
	
	
	public static void reverseWords(StringBuffer input)
	{
		int startPos = 0;
		int endPos = 0;
		
		//input.setCharAt(0, 'z');
		int length = input.length();
		while(endPos < length)
		{
			while((endPos < length) && input.charAt(endPos) != ' ')
			{
				endPos++;
			}
			reverseWord(input, startPos, endPos-1);
		
			endPos++;
			startPos = endPos;
		}		
	}
	
	public static void reverseWord(StringBuffer input, int startPos, int endPos)
	{
		int wordLen = (endPos - startPos) + 1;
		//even word length
		if (wordLen%2 == 0)
		{
			while (startPos < endPos)
			{
				char temp = input.charAt(endPos);
				input.setCharAt(endPos, input.charAt(startPos));
				input.setCharAt(startPos, temp);
				startPos++;
				endPos--;
			}
		}
		else
		{
			while (startPos != endPos)
			{
				char temp = input.charAt(endPos);
				input.setCharAt(endPos, input.charAt(startPos));
				input.setCharAt(startPos, temp);
				startPos++;
				endPos--;
			}
		}
	}
}