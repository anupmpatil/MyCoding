/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author Anup_Dell
 */
public class XMLParser {

    public HashMap queryMap;
    public String path;
    public XMLParser(String path)
    {
        this.path = path;
        queryMap = new HashMap();
    }
    public HashMap getQueries()
    {
        try {
            Scanner inputReader = null;
            inputReader = new Scanner(new File(path));
            List numberList = new ArrayList();
            List queryList = new ArrayList();
            String temp;
            String Query = null;
            boolean startReading = false;
            while (inputReader.hasNextLine()) {
                String line = inputReader.nextLine();
                int number = 0;
                
                if (startReading)
                {
                    String text = "</text>";
                    
                    if(line.contains("</text>"))
                        {                            
                            if (line.length() == text.length())
                            {
                                queryList.add(Query);
                                startReading = false;
                                Query = null;
                            }
                            else
                            {
                                int endIndex = line.indexOf("</text>");
                                temp = line.substring(0, endIndex);
                                Query = Query + " " + temp;
                                queryList.add(Query);
                                startReading = false;
                                Query = null;               
                            }
                        }
                    else
                    {
                        Query = Query + " " + line;
                    }
                }
                if (line.contains("<number>"))
                {
                    int startIndex = line.indexOf(">");
                    int endIndex = line.indexOf("</number>");
                    temp = line.substring(startIndex+1, endIndex);
                    //System.out.println(temp);
                    number = Integer.parseInt(temp);
                    numberList.add(number);
                    
                }
                String text = "<text>";
                if (line.contains("<text>"))
                {
                    startReading = true;
                    if (line.length() > text.length())
                    {
                        if(line.contains("</text>"))
                        {
                            int startIndex = line.indexOf(">");
                            int endIndex = line.indexOf("</text>");
                            temp = line.substring(startIndex+1, endIndex);
                            //System.out.println(temp);
                            queryList.add(temp);
                            startReading = false;
                        }
                        else{
                            int startIndex = line.indexOf(">");
                            Query = line.substring(startIndex+1);
                        }
                    }
                }
            }
            
            
            for (int i = 0; i < queryList.size(); i++)
            {
                queryMap.put((int)numberList.get(i), queryList.get(i).toString());
                //System.out.println((i+1) + queryList.get(i).toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return queryMap;
    }
}
