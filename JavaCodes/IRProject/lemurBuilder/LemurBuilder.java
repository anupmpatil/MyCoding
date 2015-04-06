/*  
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Anup_Dell
 */
class Document {

    String docPath;
    public int docId;
    public String docName;
    public String regex[] = new String[10];
    public HashMap documentTermMap;
    public int docLen;

    public Document(String docPath, int docId, String docName) {
        this.docPath = docPath;
        this.docId = docId;
        this.docName = docName;
        documentTermMap = new HashMap();
        docLen = 0;
        regex[0] = "^([\\d]+[\\s]+[\\d]+[\\s]+[\\d]+)";
        regex[4] = "^([<][h][t][m][l][>])";
        regex[5] = "^([<][/][h][t][m][l][>])";
        regex[6] = "^([<][p][r][e][>])";
        regex[7] = "^([<][/][p][r][e][>])";

        processDocument();
    }

    /*
     * ^([\d]+[\s]+[\d]+[\s]+[\d]+)


     CA791202 DB February 25, 1980  11:03 AM

     ^([C][A][\d]+)
     or
     ([\d]+[:]+[\d]+[\s]+[\w]+)$

     2.11 3.52 3.53 3.80
     ([\d]+[.][\d]+)*


     CACM December, 1979
     ^([C][A][C][M][\s][\w]+[,][\s]+[\d]+)
     */
    /*
     * Read every line in the document.
     * Process every line.
     */
    public void processDocument() {
        Path filePath = Paths.get(docPath);
        try 
		{
            Scanner scanner = new Scanner(filePath);
            while (scanner.hasNextLine()) 
			{
                processLine(scanner.nextLine());
            }
        } 
		catch (Exception e) 
		{
            e.printStackTrace();
            System.exit(docId);
        }
    }

    /*
     * Match pattern to decide whether 
     * to consider the line or not
     */
    protected void processLine(String line) 
	{
        //use a second Scanner to parse the content of each line 
        Scanner scanner = new Scanner(line);
        //scanner.useDelimiter("=");
        if (scanner.hasNext()) 
		{
            Pattern pattern1 = Pattern.compile(regex[0]);
            Matcher matcher1 = pattern1.matcher(line);
            Pattern pattern5 = Pattern.compile(regex[4]);
            Matcher matcher5 = pattern5.matcher(line);
            Pattern pattern6 = Pattern.compile(regex[5]);
            Matcher matcher6 = pattern6.matcher(line);
            Pattern pattern7 = Pattern.compile(regex[6]);
            Matcher matcher7 = pattern7.matcher(line);
            Pattern pattern8 = Pattern.compile(regex[7]);
            Matcher matcher8 = pattern8.matcher(line);


            if (!((matcher1.matches())
                    || (matcher5.matches())
                    || (matcher6.matches())
                    || (matcher7.matches())
                    || (matcher8.matches()))) 
			{
                /*
                 * If line is passed through filter,
                 * Process it further to split
                 * line in words.
                 */
                parseLine(line);
            }
        } 
		else 
		{
            System.out.println("Empty or invalid line. Unable to process.");
        }
    }

    /*
     * split line in words
     */
    public void parseLine(String line) {
        Scanner scanner = new Scanner(line);

        String regx = "([\\w]+[,][\\s]*[\\w][.][\\s]*)";
        
        Pattern pattern1 = Pattern.compile(regx);
        Matcher matcher1 = pattern1.matcher(line);


        String word;
        if (matcher1.lookingAt()) {
            /*
             * Case Author names pattern matching
             */
            word = line.replaceAll("[^a-zA-Z ]", "").toLowerCase();
            //System.out.println(word);
            processTerm(word);
            docLen++;
        } else {
            /*
             * Replace all non a-z, A-Z, 0-9 words by ""
             * and convert each word to lower case.
             */
            String[] words = line.split("[\\s,-]+");

            for (int i = 0; i < words.length; i++) 
            {
                word = words[i].replaceAll("[\\W]", "").toLowerCase();
                if (word != null) 
                {
                    if (!(LemurBuilder.stopWordSet.contains(word))) 
                    {
                        word = LemurBuilder.applyStemmer(word);
                        processTerm(word);
                        docLen++;
                    }
                }
            }
        }
    }

    public void processTerm(String name) {
        
        Term t = LemurBuilder.searchTerm(name);
        if (LemurBuilder.termNames.contains(name))
        {
            
            for (Object t1 : LemurBuilder.collectionTermList)
            {
                Term temp = (Term) t1;
                if (temp.term.equals(name)) 
                {
                    temp.addNewDoc(docId);
                    temp.updateTermFrequency();
                    break;
                }
            }
        }
        
        else 
        {
            t = new Term(name, 1, docId);
            LemurBuilder.collectionTermList.add(t);
            LemurBuilder.termNames.add(name);
        }

        if (documentTermMap.containsKey(name)) {
            int value = (int) documentTermMap.get(name);
            value++;
            documentTermMap.remove(name);
            documentTermMap.put(name, value);
        } else {
            documentTermMap.put(name, 1);
        }
    }
}

class Term {

    String term;
    /*
     * Number of times the term occurs in overall collection
     */
    int termFrequency;
    Set documentSet;

    public String getName() {
        return term;
    }

    Term(String term, int termFrequemcy, int docId) {
        this.term = term;
        this.termFrequency = termFrequemcy;
        documentSet = new HashSet();
        this.documentSet.add(docId);
    }

    public void updateTermFrequency() {
        termFrequency++;
    }

    public void addNewDoc(int docId) {
        documentSet.add(docId);
    }
}

public class LemurBuilder {

    static List documentSet;
    static List stopWordSet;
    static List termNames;
    /*
     * Lit of all unique terms in collection
     */
    static List collectionTermList;
    static int totalDocs = 0;
    static int totalCollectionTerms = 0;
    /*
     * Porter stemmer
     */
    static Stemmer s = new Stemmer();

    
    public LemurBuilder()
    {
        collectionTermList = new ArrayList<Term>();
        documentSet = new ArrayList<Document>();
        termNames = new ArrayList<String>();
        readStopWords();
        readDirectory();
        createFileThree();
        createFileTwo();
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        collectionTermList = new ArrayList<Term>();
        
        documentSet = new ArrayList<Document>();
        termNames = new ArrayList<String>();
        readStopWords();
        readDirectory();
        createFileThree();
        createFileTwo();
        createStatFile();
        System.out.println("Unique terms in collection: "+collectionTermList.size());
        System.out.println("Unique terms in collection: "+termNames.size());
    }

    public static void readDirectory() {
        documentSet = new ArrayList<Document>();
        int i = 1;
        Path dir = Paths.get("cacm");
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path file : stream) {
                String docName = file.getFileName().toString();
                String fullName = "cacm\\" + docName;
                int endIndex = docName.indexOf(".html");
                String modifiedName = docName.substring(0, endIndex);
                Document d = new Document(fullName, i, modifiedName);
                i++;
                totalDocs++;
                documentSet.add(d);
            }
        } catch (IOException | DirectoryIteratorException x) {
            // IOException can never be thrown by the iteration.
            // In this snippet, it can only be thrown by newDirectoryStream.
            System.err.println(x);
        }
    }

    public static void readStopWords() {
        stopWordSet = new ArrayList<String>();
        try {
            Scanner inputReader = null;
            inputReader = new Scanner(new File("stopwordlist.txt"));
            while (inputReader.hasNextLine()) {
                String line = inputReader.nextLine();
                stopWordSet.add(line);
                //System.out.println(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("file not found");
        } catch (Exception e) {
        }
    }

    public static Term searchTerm(String name) {
        Term t = null;
        if (collectionTermList == null) {
            return t;
        }
        for (Object t1 : collectionTermList) {
            t = (Term) t1;
            if (t.term.equals(name)) {
                //System.out.println("match found for : " + name + " = "+ t.term);
                break;
            }
        }
        return t;
    }

    public static void updateTermList(Term t) {
        for (Object t1 : collectionTermList) {
            Term temp = (Term) t1;
            if (temp.term.equals(t.term)) {
                collectionTermList.remove(temp);
                collectionTermList.add(t);
                break;
            }
        }
    }

    public static Document searchDocument(int docId) {
        Document d = null;
        if (documentSet == null) {
            return d;
        }
        for (Object d1 : documentSet) {
            Document temp = (Document) d1;
            if (temp.docId == docId) {
                return temp;
            }
        }
        return d;
    }
    
    
   /* public static void writeList2()
    {
        try 
        {
            PrintWriter writer = new PrintWriter("arrlist.txt", "UTF-8");
            for(int i = 0; i < termNames.size(); i++)
            {
               writer.println(termNames.get(i).toString());
            }
            writer.close();
         } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }*/
    
    /* public static void writeList1()
    {
        try 
        {
            PrintWriter writer = new PrintWriter("list.txt", "UTF-8");
            for (int i = 0; i < collectionTermList.size(); i++)
            {
                Term temp = (Term) collectionTermList.get(i);
                writer.println(temp.getName());
            }
            
          
            writer.close();
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }*/

    public static void createStatFile()
    {
        try 
        {
            PrintWriter writer = new PrintWriter("statfile.txt", "UTF-8");
            writer.println("Total Documents in collection = "+totalDocs);
            writer.println("Unique terms in collection = " + collectionTermList.size());
            writer.println("Total terms in collection = "+ totalCollectionTerms);
            writer.println("Average doc length = "+ (totalCollectionTerms/totalDocs));
            writer.close();
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }
    public static void createFileThree() {
        //Charset charset = Charset.forName("US-ASCII");
        //String s = "...";
        try {
            PrintWriter writer = new PrintWriter("file3.txt", "UTF-8");
            PrintWriter writer1 = new PrintWriter("docid.txt", "UTF-8");
            writer.println("Total Documents is collection = " + totalDocs);
            for (int i = 0; i < totalDocs; i++) {
                Document D = (Document) documentSet.get(i);
                writer.println(D.docId + "\t" + D.docName + "\t" + D.docLen + "\t" + D.documentTermMap);
                writer1.println(D.docId + "\t" + D.docName);
                totalCollectionTerms = totalCollectionTerms + D.docLen;
            }

            //writer.println("The second line");
            writer.close();
            writer1.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void createFileTwo() {
        try {
            int offsetCounter = 0;
            int offset = 0;
            PrintWriter writer = new PrintWriter("file2.txt", "UTF-8");
            PrintWriter writer1 = new PrintWriter("file1.txt", "UTF-8");
            writer.println("Total Documents is collection = " + totalDocs);
            offsetCounter++;
            for (int i = 0; i < collectionTermList.size(); i++) {
                Term t = (Term) collectionTermList.get(i);
                writer.println("Term Name: " + t.getName());
                offsetCounter++;
                offset = offsetCounter;
                writer.println("Collection Term Frequency:" + t.termFrequency);
                offsetCounter++;

                Set ds = t.documentSet;
                Iterator itr = ds.iterator();

                writer.println("Document Term Frequency:" + ds.size());
                offsetCounter++;
                writer.println("docId\t\tterm frequency in Doc\t\t");
                offsetCounter++;
                while (itr.hasNext()) 
                {
                    Integer docId = (Integer) itr.next();
                    Document D = searchDocument(docId);
                    HashMap documentTermMap = D.documentTermMap;
                    int freq = 0;
                    if (documentTermMap != null) {
                        freq = (int) documentTermMap.get(t.getName());

                    }
                    writer.println(docId + "\t\t"+ D.docLen+"\t\t" + freq);
                    offsetCounter++;
                }
                createFileOne(writer1, t.getName(), t.termFrequency, ds.size(), offset, offsetCounter-offset);
            }

            //writer.println("The second line");
            writer.close();
            writer1.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * term name        ctf     dtf     offset
     */
    public static void createFileOne(PrintWriter writer, String termName, int ctf, int dtf, int offset, int length) {
        writer.println(termName + "\t\t" + ctf + "\t\t" + dtf + "\t\t" + offset + "\t\t"+length);
    }

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
            //if (Character.isLetter((char) ch)) 
            {
                j = 0;
                 
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
