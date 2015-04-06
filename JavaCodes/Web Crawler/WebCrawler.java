/*
 * Program to crawl a set 100 URLs
 * 
 */


import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.*;

/**
 *
 * @author Anup_Dell
 */
public class WebCrawler 
{  
    public static int crawlCount = 0;
    public static int crawlIndex = 0;
    public static String robotDisallow = "Disallow:";    
    public static int arrayIndex = 0;
    public static Hashtable <String,String> robotTable;
	private static final int MAXQSIZE = 500000;
	private static final int CRAWL_COUNT = 100;
	public static String [] crawlingLinks = new String[MAXQSIZE];
    

    /**
      * Access the url
      */

    public void accessURL(String inputUrl) throws Exception 
	{
        try 
		{
            URL url = new URL(inputUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.2; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0");
            connection.connect();
			//Check if url user wants to crawl is allowed by robot.
            if (checkForRobot(connection, url)) 
			{
                String contentType = connection.getContentType();
                if ((contentType.contains("text/html")) || (contentType.contains("html")) || (contentType.contains("text")) || (url.toString().contains(".pdf"))) {
                    int httpResponseCode = connection.getResponseCode();
					// Check if connection to this url was successful
                    if (httpResponseCode == 200)
                    {
                        processURL(connection);
                    }
                }
            }
            crawlCount++;
        } 
		catch (MalformedURLException e) 
		{
			System.out.println("bad url");
        }
        catch(UnknownHostException e)
        {
            System.out.println("Unknown Host");
        }
        catch(Exception e)
        {
			e.printStackTrace();
		}

    }
	
    /**
      * Parse page contents to find 
	  * and store more links on it.
      */
    public void processURL(HttpURLConnection con) throws Exception 
	{
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        String hlink = "<a href=\"";
        while ((inputLine = in.readLine()) != null) 
		{
            if (inputLine.contains(hlink)) 
			{
                int startIndex = inputLine.indexOf(hlink) + hlink.length();
                int endPos = inputLine.indexOf("\"", startIndex);
                CharSequence flag_received = inputLine.subSequence(startIndex, endPos);
                String newurl = flag_received.toString();
                String temp = newurl + "\n";
                insertLink(newurl);
            }
        }
        in.close();
    }

    /**
      * Check if robot for given host is already present. 
      * If yes, do not fetch it again, otherwise fetch it.
      * Check if url user wants to crawl is allowed by robot.
      */
    public boolean checkForRobot(HttpURLConnection con, URL url) 
    {

        String strCommands = null;
        String strHost = url.getHost();
        
        if (robotTable.containsKey(strHost))
        {
            strCommands = robotTable.get(strHost);
        }
        
        else
        {
            // form URL of the robots.txt file
            String strRobot = "http://" + strHost + "/robots.txt";
            URL urlRobot;
            try 
            {
				// Try to fetch robot.txt for this host
                urlRobot = new URL(strRobot);
            }  
            catch (MalformedURLException e) 
            {
                // something weird is happening, so don't trust it
                return false;
            }

            System.out.println("Checking robot protocol " + urlRobot.toString());
        
           try 
           {
			   // Fetch the contents of robot
                HttpURLConnection connection = (HttpURLConnection) urlRobot.openConnection();
                connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.2; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0");
                connection.connect();

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) 
                {
                    strCommands += inputLine;
                }
				// Store all robot commands to robots table
				// So that next time if you visit same host
			    // you can jest fetch commands stored
                robotTable.put(strHost, strCommands);
            }
           catch (IOException e) 
           {
				// if there is no robots.txt file, it is OK to search
				//System.out.println("\n No robot found");
				//e.printStackTrace();
                return true;
            }
        }

        
        // assume that this robots.txt refers to us and 
        // search for "Disallow:" commands.
        String strURL = url.getFile();
        int index = 0;
        while ((index = strCommands.indexOf(robotDisallow, index)) != -1) {
            index += robotDisallow.length();
            String strPath = strCommands.substring(index);
            //System.out.println("\n strPath" + strPath);
            StringTokenizer st = new StringTokenizer(strPath);

            if (!st.hasMoreTokens()) {
                break;
            }

            String badPathValue = st.nextToken();

            // if the URL starts with a disallowed path, it is not safe
            if (strURL.indexOf(badPathValue) == 0) {
                return false;
            }
        }

        return true;
    }
	
	/**
      * Insert link in queue, if element already exists, do not insert.
      */
    public static void insertLink(String url) throws Exception
    {
        int index = 0;
        Boolean insert = true;
		try
		{
 	       while (index < arrayIndex)
 	       {
 	           if(crawlingLinks[index].equals(url))
 	           {
 	               insert = false;
 	               break;
 	           }            
 	           index++;
 	       }
	        if (insert)
	        {
				if (arrayIndex < MAXQSIZE)
				{
					crawlingLinks[arrayIndex] = url;
					arrayIndex++;
				}
        	}
		}
        catch(Exception e)
        {
			e.printStackTrace();
		}
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
		 WebCrawler wc = new WebCrawler();
         robotTable = new Hashtable<String, String>();
        
		 // User should pass initial URL to start crawling with
         if(args.length == 0)
         {
             System.out.println("Incorrect number of arguments, need initial crawl link");
             System.exit(0);
         }
		 
		 // Initial URL to start crawling with
         String initialUrl = args[0];       
        
		// insert initial url
		crawlingLinks[arrayIndex] = initialUrl;
        arrayIndex ++;
        
        

        //insertLink("http://www.ccs.neu.edu/");
        while (crawlCount < CRAWL_COUNT)
        {
            String url = crawlingLinks[crawlIndex];
            System.out.println(crawlIndex + " " + url);
            crawlIndex++;            
            wc.accessURL(url);
	        // wait for 5 seconds between two requests
            Thread.sleep(5000);
        }

        // TODO code application logic here
    }
}




