/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;


class termInformation {
    /*
     * Collection term frequency
     */

    int ctf;
    /*
     * Document term frequency
     * Number of documetns in 
     * collection containing term
     */
    int dtf;
    /*
     * Offset in file
     */
    int offset;
    /*
     * Length - number of lines
     * for this term which should 
     * be read from file.
     */
    int length;

    public termInformation(int ctf, int dtf, int offset, int length) {
        this.ctf = ctf;
        this.dtf = dtf;
        this.offset = offset;
        this.length = length;
    }
}

/*
 * Use this class to store information
 * about all documents containing a 
 * particular term.
 */
class docLenTFreq {

    int docId;
    int docLen;
    int termFrequency;

    public docLenTFreq(int docId, int docLen, int termFrequency) {
        this.docId = docId;
        this.docLen = docLen;
        this.termFrequency = termFrequency;
    }
}
/*
 * term information like ctf, dtf, offset length,length and
 * List of documents containing term
 */

class searchReturn {

    termInformation t;
    List termFrequencyMap;
    
    public searchReturn() {
    }

    public searchReturn(termInformation t, List termFrequencyMap) {
        this.t = t;
        this.termFrequencyMap = new ArrayList();
        this.termFrequencyMap = termFrequencyMap;
    }
}

/*
 *
 * @author Anup_Dell
 */
/*
 * LemurInterface class
 * Class is for accepting requests for 
 * query search. This class will use 
 * preprocessed data stored in files created by
 * LemurBuilder. The this class will perform 
 * search operation and return search results
 */
public class LemurInterface {

    String searchQuery;
    /*
     * Read file1.txt in this map
     * when LemurInterface object is created
     */
    HashMap termInfoMap;
    static int startOffset = 4;
    /*
     * apply stemming on search term
     */
    static Stemmer s = new Stemmer();

    public LemurInterface() {
        termInfoMap = new HashMap();
        int cnt  =0;
        try {
            
            Scanner inputReader = null;
            inputReader = new Scanner(new File("C:\\Users\\Anup_Dell\\Desktop\\IRP3\\lemurBuilder\\file1.txt"));
            while (inputReader.hasNextLine()) {
                String words[] = new String[5];
                String line = inputReader.nextLine();
                ParseLine(line, words);
                int ctf = Integer.parseInt(words[1]);
                int dtf = Integer.parseInt(words[2]);
                int offset = Integer.parseInt(words[3]);
                int length = Integer.parseInt(words[4]);
                termInformation tinfo = new termInformation(ctf, dtf, offset, length);
                termInfoMap.put(words[0], tinfo);
                cnt++;
               // System.out.println(line);
                
            }
            inputReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("file not found");
        } catch (Exception e) {
        }
        
        //System.out.println("cnt = "+cnt);
        //System.exit(0);
    }

    public void ParseLine(String line, String[] words) {
        //String splitWords[] = line.split("[\\s,-]+");
        String splitWords[] = line.split("[\t][\t]");
        int j = 0;
        for (int i = 0; i < splitWords.length; i++) {
            if (splitWords[i] != null) {
                words[j] = splitWords[i];
                //System.out.println(words[i]);
                j++;
            }
        }
    }

    public searchReturn searchTermInfo(String queryTerm)
    {
        if (termInfoMap == null) 
        {
            return null;
        }

        String stemmedTerm = applyStemmer(queryTerm);
        searchReturn sr = new searchReturn();
        List termFrequencyMap = new ArrayList();
        termInformation t = null;
        int offset = 0;
        int len = 0;
        int startline = 0;
        int currentLine = 0;
        if (termInfoMap.containsKey(stemmedTerm)) 
        {
            t = (termInformation) termInfoMap.get(stemmedTerm);
            offset = t.offset;
            len = t.length;
            startline = offset + startOffset;
            try 
            {
                Scanner inputReader = null;
                inputReader = new Scanner(new File("file2.txt"));
                while (inputReader.hasNextLine()) {
                    String line = inputReader.nextLine();
                    currentLine++;

                    if (currentLine > offset + len) {
                        break;
                    }

                    if ((startline <= currentLine)) {
                        String words[] = new String[3];
                        ParseLine(line, words);
                        int docId = Integer.parseInt(words[0]);
                        int docLen = Integer.parseInt(words[1]);
                        int termFreq = Integer.parseInt(words[2]);
                        docLenTFreq var = new docLenTFreq(docId, docLen, termFreq);
                        termFrequencyMap.add(var);

                    }
                }
            }
            catch (FileNotFoundException e) 
            {
                e.printStackTrace();
                System.out.println("file not found");
            } 
            catch (Exception e) 
            {
                e.printStackTrace();
            }
        } 
        else 
        {
            t = new termInformation(0, 0, 0, 0);
            sr.t = t;
            sr.termFrequencyMap = termFrequencyMap;
            return sr;
        }
        sr = new searchReturn(t, termFrequencyMap);
        return sr;
    }

    /**
     * @param args the command line arguments
     *
     * public static void main(String[] args) { // TODO code application logic
     * here LemurInterface l = new LemurInterface(); searchReturn sr =
     * l.searchTermInfo("store");
     *
     * if (sr == null) { System.out.println("Something wrong"); } else {
     * termInformation t = sr.t; System.out.println("term CTF = " + t.ctf);
     * System.out.println("term DTF = " + t.dtf); HashMap freqMap =
     * sr.termFrequencyMap;
     *
     * // Get a set of the entries Set set = freqMap.entrySet(); // Get an
     * iterator Iterator i = set.iterator(); // Display elements
     * while(i.hasNext()) { Map.Entry me = (Map.Entry)i.next();
     * System.out.print(me.getKey() + ": "); System.out.println(me.getValue());
     * } }
    }
     */
    public static String applyStemmer(String word) 
    {
        //System.out.println("Input word: "+word);
        int len = word.length();
        char[] w = new char[len];
        String inputWord = word;
        int j = 0;
        for (int i = 0; i < word.length(); i++) 
        {
            int ch = word.charAt(i);
            {
                j = 0;
                //ch = Character.toLowerCase((char) ch);
                w[j] = (char) ch;
                if (j < len) 
                {
                    j++;
                }
            }
        }

        for (int c = 0; c < len; c++) 
        {
            s.add(word.charAt(c));
        }
        /*for (int c = 0; c < j; c++) 
         {
         s.add(w[c]);
         }*/

        s.stem();
        {
            String u;

            /* and now, to test toString() : */
            u = s.toString();

            /* to test getResultBuffer(), getResultLength() : */
            /* u = new String(s.getResultBuffer(), 0, s.getResultLength()); */

            //System.out.println("After stemming: "+ u);
            inputWord = u;
        }
        return inputWord;
    }
}
