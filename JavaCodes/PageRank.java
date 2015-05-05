/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.File;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 *
 * @author Anup_Dell
 */

class pageInfo
{
    public String pagename;
    public List inlinks;
    public double pageRank;
    
    public pageInfo()
    {
        pagename = "";
        pageRank = 0.0;
    }
}

public class PageRank {
    
     public static double RoundToNDecimals(double val) {
            DecimalFormat df2 = new DecimalFormat("###.######");
        return Double.valueOf(df2.format(val));
}

    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Scanner inputReader = null;
        
        List pages = new ArrayList();
        List sinkPages = new ArrayList();
        HashSet<String> allPageSet = new HashSet<String>();
        HashSet<String> inlinkSet = new HashSet<String>();
        List perplexityList = new ArrayList();
        
        
        long allPagesSize = 0;
        
    //read file
    try {
         
        inputReader = new Scanner(new File("wt2g_inlinks.txt"));
    } catch (FileNotFoundException e) {
        e.printStackTrace();  
        System.out.println("file not found");
    }
    // store page name to number of inlink mapping
    HashMap pageMap = new HashMap(); // set M(p)
    // store page name to page rank
    HashMap pageRank = new HashMap();
    // store page name to new page rank
    HashMap newPageRank = new HashMap();
    // store page name to number of outlinks
    HashMap outlinkSet = new HashMap();
    // store page name to number of inlinks
    HashMap inlinkCount = new HashMap();
    
    //read line by line
    try{
    while (inputReader.hasNextLine()) {
            Scanner line = new Scanner(inputReader.nextLine());
            pageInfo page = new pageInfo();
            boolean isName = true;
            
        // while line has words
        while (line.hasNext()) {
            String word = line.next();
            //first word is page name
            if (isName)
            {
                page.pagename = word;
                page.inlinks = new ArrayList();
                allPageSet.add(word);
                isName = false;
            }
            else
            {
                // other words are inliks to page
                 page.inlinks.add(word);
                 inlinkSet.add(word);
            }
        }
        //System.out.println("page.inlink.size = " + page.inlinks.size());
            for (int i = 0; i < page.inlinks.size(); i++) 
            {
                String currentLink = page.inlinks.get(i).toString();
                //System.out.println("page" + page.pagename);
                //System.out.println("currentlink" + currentLink);
                if (outlinkSet.containsKey(currentLink))
                {                    
                    int outLinkCount = (Integer) outlinkSet.get(currentLink);
                    outLinkCount += 1;
                    outlinkSet.remove(currentLink);
                    outlinkSet.put(currentLink, outLinkCount);
                }
                else
                {
                    outlinkSet.put(currentLink, 1);
                }
            }
        pages.add(page);
        pageMap.put(page.pagename, page.inlinks);
        inlinkCount.put(page.pagename, page.inlinks.size());
       }
    }
    catch(Exception e)
    {
        e.printStackTrace();
    }
    //System.out.println("Number of all pages : "+ allPageSet.size());
    //System.out.println("Number of inlink pages : "+ inlinkSet.size());
    
    
     for (String e : allPageSet) {
                if (! (inlinkSet.contains(e))) {
                    sinkPages.add(e);
                }
                if (! (outlinkSet.containsKey(e))) {
                    outlinkSet.put(e, 0);
                }
            }
    
    
     
    allPagesSize = pages.size();
    double initialPageRank = (double)1/allPagesSize;
    System.out.println("Number of pages : "+ pages.size());
    double px = 0.0;
    double power = 0.0;
    for (int i = 0; i < pages.size(); i++)
    {
        pageInfo p = (pageInfo)pages.get(i);
        //System.out.println("Initial page rank = "+ initialPageRank);
        p.pageRank = initialPageRank;
        pageRank.put(p.pagename, p.pageRank);
        //System.out.println("log2px = " + );
        px = 0.0d - (double)((double)initialPageRank*((double)Math.log(initialPageRank)/(double)Math.log(2.0)));
        //System.out.println("px = "+px);
        power = power + px;
        
        /*System.out.println(p.pagename+ "              ");
        for(int k = 0; k< p.inlinks.size();k++)
        {
            System.out.println("    "+ p.inlinks.get(k).toString()+";");
        }*/
    }
    //System.out.println("power = "+power);
    double perplexity = Math.pow(2, power);
    System.out.println("Initial perplexity = "+perplexity);
    perplexityList.add(perplexity);
    allPagesSize = allPageSet.size();
    
    //System.out.println("Sink pages are: size =  "+ sinkPages.size());
    /*for (int i = 0; i < sinkPages.size(); i++)
    {
        System.out.println(sinkPages.get(i).toString());
    }*/
    
    
    
    //System.out.println("Outlink count L(q): ");
    //System.out.println(outlinkSet);
    /*for (int i = 0; i < sinkPages.size(); i++)
    {
        System.out.println(outlinkSet.values());
    }*/
    
    //damping factor
    double d = 0.85;
    int count = 0;
    
    double difference = 0.0;
    int whileCount = 0;
    int prevLoopCount = 0;
    // iteratively calculate page rank until it converges
    while(true)
    {
        double sinkPR = 0.0;
        for (int i = 0; i < sinkPages.size(); i++) {
            double pr = (Double) pageRank.get(sinkPages.get(i).toString());
            //System.out.println("pr = " + pr);
            sinkPR = sinkPR + pr;
        }
        //System.out.println("sinkPR = " + sinkPR);
        power = 0;
        for (String e : allPageSet) 
        {
            double newPRp = (double)((1 - d)/allPagesSize);
            newPRp = (double)((double)newPRp + (double)((d * sinkPR)/allPagesSize));
            List pageInlinksP = (List)pageMap.get(e);
            for (int i = 0; i < pageInlinksP.size(); i++)
            {
                String q = pageInlinksP.get(i).toString();
                int L_q = (Integer)outlinkSet.get(q);
                double pr_q = (Double)pageRank.get(q);
                newPRp = newPRp + (double)(d*pr_q/L_q);
            }
            //System.out.println("newPRp = " + newPRp );
            newPageRank.put(e, newPRp);
            px = 0.0d - (double)((double)newPRp*((double)Math.log(newPRp)/(double)Math.log(2.0)));
            //System.out.println("px = "+px);
            power = power + px;
        }
        
        perplexity = Math.pow(2, power);
        //System.out.println("power = " + power);
        //System.out.println("perplexity = " + RoundToNDecimals(perplexity));
        int index = perplexityList.size() - 1;
        if (index >= 0)
        {
            double lastPerplexity = (Double)perplexityList.get(index);
            //if (RoundToNDecimals(lastPerplexity) == RoundToNDecimals(perplexity))
            if (Math.round(lastPerplexity) == Math.round(perplexity))
            {
                if ((whileCount - prevLoopCount == 1) || count == 0)
                {
                    count ++;
                    prevLoopCount = whileCount;
                }
                else
                {
                    count = 0;
                }
                
                if (count == 4)
                {
                    break;
                }
                
                
            }
        }
        
        perplexityList.add(perplexity);
        System.out.println("Perplexity after round " + whileCount + "    =    "+perplexity);
        
        for (String e : allPageSet) 
        {
            double oldPR = (Double) pageRank.get(e);
            double newPR = (Double) newPageRank.get(e);            
            double diff = Math.abs(oldPR - newPR);
            difference = difference + diff;
        }
        
        //System.out.println("difference " + whileCount+ "        "+difference);
       
        /*System.out.println("Old PR: ");
        System.out.println(pageRank);
        System.out.println("New PR: ");
        System.out.println(newPageRank);*/
        
        // this will not work properly without copy constructor
        //pageRank = newPageRank;
        // hence do iterative copy
        
        //newPageRank.putAll(pageMap);
        for (String e : allPageSet) 
        {
            double newPR = (Double) newPageRank.get(e);            
            pageRank.remove(e);
            pageRank.put(e, newPR);
        }
        whileCount++;
    }
    
   
    //System.out.println("Final calculated PR: ");
    //System.out.println(pageRank);
    
    
    // sort page ranks
    ArrayList<Map.Entry<String, Double>> sortedList = new ArrayList(pageRank.entrySet());
       Collections.sort(sortedList, new Comparator<Map.Entry<?, Double>>(){

         public int compare(Map.Entry<?, Double> o1, Map.Entry<?, Double> o2) {
            return o2.getValue().compareTo(o1.getValue());
        }});

       //int cnt = 0;
       System.out.println("\n Sorted pagerank output after convergence: ");
       for(int i = 0; i < sortedList.size(); i++)
       {
               if(i >=50)
               {
                   break;
               }
               String key = sortedList.get(i).getKey();
               List inlinkList = (List)pageMap.get(key);
               int olC = (Integer)outlinkSet.get(key);
               System.out.println("Index = " + i + "\t Pagerank = " + sortedList.get(i).getKey() + " = " +RoundToNDecimals(sortedList.get(i).getValue()) + "\t inlinkcount = " + inlinkList.size() + "\t outlinkcount = " + olC);
               //System.out.println(l.get(i) + "\t inlinkcount = "+ inlinkList.size()+"\t outlinkcount = "+olC);               
       }  
    
       System.out.println("Final perplexity list = "+ perplexityList);    
       
       
       // sort page ranks
    ArrayList<Map.Entry<String, Integer>> sortedListByInlinks = new ArrayList(inlinkCount.entrySet());
       Collections.sort(sortedListByInlinks, new Comparator<Map.Entry<?, Integer>>(){

         public int compare(Map.Entry<?, Integer> o1, Map.Entry<?, Integer> o2) {
            return o2.getValue().compareTo(o1.getValue());
        }});

       //int cnt = 0;
       System.out.println("\n\n TOP 50  PAGES BY INLINKCOUNT: ");
       for(int i = 0; i < sortedListByInlinks.size(); i++)
       {
               if(i >=50)
               {
                   break;
               }
               String key = sortedListByInlinks.get(i).getKey();
               double prval = (Double)pageRank.get(key);
               int olC = (Integer)outlinkSet.get(key);
               System.out.println("Index = " + i + "\t Page = " + sortedListByInlinks.get(i).getKey() + "\t Inlinkcount = " +sortedListByInlinks.get(i).getValue() + "\t pagerank = "+ prval);
               //System.out.println(l.get(i) + "\t inlinkcount = "+ inlinkList.size()+"\t outlinkcount = "+olC);               
       }  
    }
}


