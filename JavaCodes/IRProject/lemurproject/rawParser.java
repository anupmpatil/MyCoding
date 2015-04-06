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
public class rawParser {
    
    public HashMap queryMap;
    public String path;
    public rawParser(String path)
    {
        this.path = path;
        queryMap = new HashMap();
    }
    public HashMap getQueries()
    {
        try {
            Scanner inputReader = null;
            inputReader = new Scanner(new File("cacm.query"));
            List numberList = new ArrayList();
            List queryList = new ArrayList();
            String temp;
            String Query = "";
            boolean startReading = false;
            while (inputReader.hasNextLine()) {
                String line = inputReader.nextLine();
                int number = 0;
                
                if (startReading)
                {
                    
                    if (line.contains("</DOC>"))
                    {
                        queryList.add(Query);
                        startReading = false;
                        Query = "";
                    }
                    else
                    {                    
                        Query = Query + " " + line;
                    }
                }
                if (line.contains("<DOCNO>"))
                {
                    int startIndex = line.indexOf(" ");
                    int endIndex = line.indexOf(" </DOCNO>");
                    temp = line.substring(startIndex+1, endIndex);
                    System.out.println(temp);
                    number = Integer.parseInt(temp);
                    numberList.add(number);
                    startReading = true;
                    
                }                
            }
            
            
            for (int i = 0; i < queryList.size(); i++)
            {
                String line = (String)queryList.get(i);
                //String[] words = line.split("[\\s,-]+");
                String word = line.replaceAll("[-]", " ");
                word = word.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase();
                queryMap.put((int)numberList.get(i), word);
                System.out.println((i+1) + word);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return queryMap;
    }
    
}
