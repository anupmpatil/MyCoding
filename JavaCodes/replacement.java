import java.util.*;
import java.lang.*;

/*
 * Replace letters mentioned in filter
 * with blank
 */
class replacement
{
	public static void main(String [] args)
	{
		String input = "Battle of the vowels: Hawaii vs. Grozny";
		String filter = "aeiou";
		String regex = "[" + filter + "]";
		String output = input.replaceAll(regex,"");
		System.out.println("Output = " + output);
		
		StringBuffer br = new StringBuffer("Battle of the vowels: Hawaii vs. Grozny");
		/*
		for(int i = 0; i < input.length(); i++)
		{
			Char ch = input.charAt(i);
			if (filter.contains(ch))
			{}
		}*/
		int j = 0;
		int length = br.length();
		int k = 0;
		System.out.println("Input length = " + length);
		
		HashSet hs = new HashSet();
		
		for (int i = 0; i < filter.length(); i++)
		{
			char val = filter.charAt(i);
			Character ch = new Character(val);
			hs.add(ch);
		}
		
		while ( k < length )
		{
			char ch = br.charAt(k);
			
			Character val = new Character(ch);
			if (hs.contains(val))
			{
				k++;
			}
			else
			{
				br.setCharAt(j, ch);
				j++;
				k++;
			}
		}
		while ( j < length )
		{
			br.setCharAt(j, '\0');
			j++;
		}
		System.out.println("After Modification = " + br.toString());
		
		
		ArrayList al = new ArrayList();
		k = 10;
		for (int i = 0; i < 10; i++)
		{
			al.add(k);
			k--;
		}
		int i = 2;
		System.out.println(al.indexOf(i));
		System.out.println(al.indexOf((Integer)i));
		
	}
}