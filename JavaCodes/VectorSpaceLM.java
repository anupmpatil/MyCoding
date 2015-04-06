/*
 * Information Retrieval using vector space model,
 * language model, BM25.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;

/**
 *
 * @author Anup_Dell
 */

class Document {
    // document Id
    int docid;
    // document length
    int doclen;
    /*
     * For each document this attribute
     * stores the query term weight set for 
     * current query.
     */
    double[] qtWeightSet;
    /*
     * This attribute records the similarity of document
     * with current query. This value depends on model 
     * being used currently.
     * Using this similarity values, documents will be 
     * ranked.
     */
    double similarity;
    /*
     * Store query terms of current Query
     */
    String[] qTermSet;

    public Document(int id, int len, int qtsetSize) {
        docid = id;
        doclen = len;
        qtWeightSet = new double[qtsetSize];
        for (int i = 0; i < qtsetSize; i++) {
            qtWeightSet[i] = 0.0;
        }
        similarity = 0.0;
    }

    public void setWeight(int index, double value) {
        qtWeightSet[index] = value;
    }
    
    public void setQueryTerm(int index, String value) {
        qTermSet[index] = value;
    }
}

class Query {
    // actual query string
    String queryName;
    // actual length of query, including repeated words
    int queryLength;
    // list of individual query words (this list may contains words after stopping stemming)
    List queryTerms = new ArrayList<String>();
    // weight of query terms in query
    double qtWeights[];
    // this records how many times the word appears in a query
    HashMap queryTermsTf = new HashMap();
    // size of query after stemming stopping de-duplication
    int queryTermsSize;
    // query index number from input file between 50-100
    int queryNumber;
    // this records how many times the query term appears in collection
    HashMap queryTermCollectionTF = new HashMap();

    public Query(String qText) {
        queryName = qText;
        queryLength = 0;
        queryTermsSize = 0;
        queryNumber = 0;
        processQuery(qText);
    }

    public void setQueryNumber(int num) {
        queryNumber = num;
    }

    /*
     * Does parsing of query.
     * Separates the query in individual terms.
     * Eliminates the stop words.
     * Stemming on query is not done as only database 
     * 3 is used which does both stopping and stemming
     * 
     */
    public void processQuery(String qText) {
        String delim = " ,\"-()";
        StringTokenizer st = new StringTokenizer(qText, delim);

        while (st.hasMoreTokens()) {
            String str = st.nextToken();
            String tempStr = str;
            if (!(VectorSpaceLM.stopWordSet.contains(str))) {
                if (str.equals("U.S.")) {
                    str = "us";
                    if (queryTermsTf.containsKey(str)) {
                        int freq = (Integer) queryTermsTf.get(str);
                        freq++;
                        queryTermsTf.remove(str);
                        queryTermsTf.put(str, freq);
                    } else {
                        int freq = 1;
                        queryTermsTf.put(str, freq);
                        queryTerms.add(str);
                    }
                } else if (str.contains(".")) {

                    tempStr = str.substring(0, (str.length() - 1));
                    tempStr = tempStr.toLowerCase();
                    if (!(VectorSpaceLM.stopWordSet.contains(tempStr))) {
                        if (queryTermsTf.containsKey(tempStr)) {
                            int freq = (Integer) queryTermsTf.get(tempStr);
                            freq++;
                            queryTermsTf.remove(tempStr);
                            queryTermsTf.put(tempStr, freq);
                        } else {
                            int freq = 1;
                            queryTermsTf.put(tempStr, freq);
                            queryTerms.add(tempStr);
                        }
                    } else {
                        queryLength--;
                    }
                } else if (str.contains("\'")) {
                    int pos = str.indexOf("\'");
                    String part1 = str.substring(0, pos);
                    String part2 = str.substring(pos + 1, str.length());
                    str = part1 + part2;
                    //System.out.println("removed app: " + str);
                    tempStr = str;
                    if (queryTermsTf.containsKey(tempStr)) {
                        int freq = (Integer) queryTermsTf.get(tempStr);
                        freq++;
                        queryTermsTf.remove(tempStr);
                        queryTermsTf.put(tempStr, freq);
                    } else {
                        int freq = 1;
                        queryTermsTf.put(tempStr, freq);
                        queryTerms.add(tempStr);
                    }
                } else {
                    tempStr = tempStr.toLowerCase();
                    if (queryTermsTf.containsKey(tempStr)) {
                        int freq = (Integer) queryTermsTf.get(tempStr);
                        freq++;
                        queryTermsTf.remove(tempStr);
                        queryTermsTf.put(tempStr, freq);
                    } else {
                        int freq = 1;
                        queryTermsTf.put(tempStr, freq);
                        queryTerms.add(tempStr);
                    }
                }
                queryLength++;
            }
        }// query terms ready
        queryTermsSize = queryTerms.size();
    }

    public void calculateWeights(double avgQLen) {
        int qtsize = queryTerms.size();
        qtWeights = new double[qtsize];
        for (int j = 0; j < qtsize; j++) {
            String tempStr = (String) queryTerms.get(j);
            int freq = (Integer) queryTermsTf.get(tempStr);
            double oktf = 0.0;
            oktf = (double) (freq / (freq + 0.5 + (1.5 * (queryLength / avgQLen))));
            qtWeights[j] = oktf;
        }
    }

    public int getQueryLength() {
        return queryLength;
    }
    
    public void setQueryTermCTF(String queryTerm, int ctf){
        queryTermCollectionTF.put(queryTerm, ctf);
    }
    
    public int getQueryTermCTF(int index){
        String queryTerm = (String)queryTerms.get(index);
        return (Integer)queryTermCollectionTF.get(queryTerm);
    }
}


public class VectorSpaceLM {

    // 
    static String inputUrl = "http://fiji5.ccs.neu.edu/~zerg/lemurcgi/lemur.cgi?g=p&d=3";
    //static String [] querySet;
    static List querySet;
    static List stopWordSet;
    static int avgdoclen = 1;
    static int databaseNum = 3;
    static boolean applyStopping = true;
    static int numDocs = 84678;
    static int qnum = 4;
    static HashMap externalDocIdMap;
    static int uniq_corp_size = 0;
    static double lambda = 0.8;
    static long totalTerms = 0;
    static int r = 0, R = 0;
    static double k1 = 1.2;
    static int k2 = 100;
    static double b = 0.75;
    static double k = 0.0;

    public static void generateDocIdMap() {
        externalDocIdMap = new HashMap<Integer, String>();
        try {
            Scanner inputReader = null;
            inputReader = new Scanner(new File("docidmap.txt"));
            while (inputReader.hasNextLine()) {
                String line = inputReader.nextLine();
                String[] splitString = line.split("   ", 2);
                int docid = Integer.parseInt(splitString[0]);
                String externalDocId = splitString[1];
                externalDocIdMap.put(docid, externalDocId);
                //System.out.println(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("file not found");
        } catch (Exception e) {
        }
    }

    /*
     * Parse other lines in HTML
     * Extract docid, doclen, termFrequency
     * Calculate OKTF weight
     */
    public static double parseLine(String[] splitString, int[] parsedContents) {
        int tokenCount = 0;
        int tf = 0;
        int doclen = 0;
        for (int k = 0; k < splitString.length; k++) {
            if ((!splitString[k].equals(" ")) && (!splitString[k].equals(""))) {
                if (tokenCount == 0) {
                    // first non null token is docid
                    parsedContents[0] = Integer.parseInt(splitString[k]);
                    tokenCount++;
                } else if (tokenCount == 1) {
                    parsedContents[1] = Integer.parseInt(splitString[k]);
                    doclen = parsedContents[1];
                    //System.out.println("doclen = " + doclen);
                    tokenCount++;
                } else if (tokenCount == 2) {
                    parsedContents[2] = Integer.parseInt(splitString[k]);
                    tf = parsedContents[2];
                    //System.out.println("tf = " + tf);
                }
            }
        }// for loop for tokens
        if (parsedContents[0] == 12028) {
            //System.out.println("doclen = " + parsedContents[1]);
            //System.out.println("tf =" + parsedContents[2]);
            //System.out.println("avgdoclen =" + avgdoclen);
        }
        double OKTF = (double) (tf / (tf + 0.5 + (1.5 * (double) ((double) (doclen * 1.0) / (double) avgdoclen))));
        if (parsedContents[0] == 12028) {
            //System.out.println("OKTF = "+OKTF);
        }
        return OKTF;
    }

    /*
     * Parse first line in HTML
     * Extract Collection term frequency and Document term frequency
     * from first line on page.
     * Also calculate IDF
     */
    public static double parseFirstLine(String[] splitString, int[] parsedContents) {
        int tokenCount = 0;
        int termCtf = 0;
        int termDf = 0;
        double idf = 0.0;
        for (int k = 0; k < splitString.length; k++) {
            if ((!splitString[k].equals(" ")) && (!splitString[k].equals(""))) {
                if (tokenCount == 0) {
                    String num = splitString[k];
                    termCtf = (int) Integer.parseInt(num);
                    parsedContents[0] = termCtf;
                    //System.out.println("termCtf = " + termCtf);
                    tokenCount++;
                } else if (tokenCount == 1) {
                    termDf = Integer.parseInt(splitString[k]);
                    parsedContents[0] = termDf;
                    //System.out.println("termDf = " + termDf);
                }
            }
        }// for loop for tokens in firstline
        //double fraction = (double)(numDocs * 1.0)/(double)(1+termDf);
        double fraction = (double) (numDocs * 1.0) / (double) (1.0 * (termDf + 1));
        //idf = Math.log(fraction);
        idf = Math.log10(fraction);
        return idf;
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
            querySet = new ArrayList<String>();
        if (databaseNum == 0) {
            avgdoclen = 493;
            uniq_corp_size = 207615;
            totalTerms = 41802513;
        } else if (databaseNum == 1) {
            avgdoclen = 493;
            uniq_corp_size = 166242;
            totalTerms = 41802513;
        } else if (databaseNum == 2) {
            avgdoclen = 288;
            uniq_corp_size = 207224;
            totalTerms = 24401877;
        } else {
            avgdoclen = 288;
            uniq_corp_size = 166054;
            totalTerms = 24401877;
        }
        if (args.length == 0)
        {
            qnum = 1;
        }
        else
        {
            qnum = Integer.parseInt(args[0]);
        }
        try {
            //qnum = 4;
            generateDocIdMap();
            if (applyStopping) {
                readStopWords();
            } else {
                stopWordSet = new ArrayList<String>();
            }
            createQuerySet();
            for (int qloop = 0; qloop < querySet.size(); qloop++) {
                Query q = (Query) querySet.get(qloop);
                //}
            /*
                 OKTF=tf/(tf + 0.5 + 1.5*doclen/avgdoclen).
                 On queries, Okapi tf can be computed in the same way, 
                 but use length of the query instead of doclen.
             
                 */
                List documentSet = new ArrayList<Document>();
                List docIdSet = new ArrayList<Integer>();
                // docid <-> qt similarity set
                HashMap similarityMap = new HashMap();
                double[] qtWeights = q.qtWeights;
                //int i = 0;
                for (int i = 0; i < q.queryTermsSize; i++) {
                    //System.out.println(q.queryTerms.get(i).toString());
                    List queryTerms = q.queryTerms;
                    //System.out.println(queryTerms);
                    int qtsize = q.queryTermsSize;

                    /**
                     *****************************************************************************************************************
                     * For every query term, find doc id, when new doc is
                     * encountered, update set of weights of query terms.
                     * ****************************************************************************************************************
                     */
                    String currentQueryTerm = queryTerms.get(i).toString();

                    String currentUrl = inputUrl;
                    String extv = "&v=" + currentQueryTerm;
                    currentUrl = currentUrl + extv;
                    //System.out.println(currentUrl);
                    URL url = new URL(currentUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.2; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0");
                    connection.connect();


                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    String sopTag = "<BODY>";
                    String eopTag = "<HR>";
                    boolean startParsing = false;
                    boolean firstLine = true;
                    int ignoreLine = 0;
                    int termCtf = 0;
                    int termDf = 0;
                    int docid = 0;
                    int doclen = 0;
                    int tf = 0;
                    double idf = 0.0;
                    double OKTF = 0.0;
                    Document d = null;
                    double prob = 0.0;
                    int queryTermFreq = (Integer) q.queryTermsTf.get(currentQueryTerm);
                    //inputLine = in.readLine();
                    while ((inputLine = in.readLine()) != null) {
                        if (inputLine.contains(sopTag)) {
                            startParsing = true;
                            continue;
                        } else if (inputLine.contains(eopTag)) {
                            startParsing = false;
                            break;
                        }
                        if (startParsing) {
                            if (ignoreLine == 0) {
                                ignoreLine = 1;
                            } else {
                                //System.out.println(inputLine);
                                String[] splitString = inputLine.split(" ");
                                //System.out.println(splitString.length);

                                // extract term CTF and term DF from first line read
                                if (firstLine) {

                                    int[] parsedContents = new int[2];
                                    idf = parseFirstLine(splitString, parsedContents);
                                    termCtf = parsedContents[0];
                                    termDf = parsedContents[1];
                                    q.setQueryTermCTF(currentQueryTerm, termCtf);
                                    firstLine = false;
                                } // if it is a firstline
                                // For rest of lines get docid  doclen  term frequence in document
                                else {
                                    int[] parsedContents = new int[3];

                                    OKTF = parseLine(splitString, parsedContents);
                                    docid = parsedContents[0];
                                    doclen = parsedContents[1];
                                    tf = parsedContents[2];

                                    if (!(docIdSet.contains(docid))) {
                                        docIdSet.add(docid);
                                        d = new Document(docid, doclen, qtsize);
                                        documentSet.add(d);
                                        //d.setIndex(i, OKTF);
                                    } else {
                                        for (int p = 0; p < documentSet.size(); p++) {
                                            d = (Document) documentSet.get(p);
                                            if (d.docid == docid) {
                                                //System.out.println("fetched = " + docid);
                                                break;
                                            }
                                        }
                                        //d.setIndex(i, OKTF);
                                    }
                                    firstLine = false;
                                }
                                /*
                                 * For question 1 set weight in document's
                                 * query term vector as calculated OKTF.
                                 */
                                if (qnum == 1) {
                                    if (d != null) {
                                        d.setWeight(i, OKTF);
                                    }
                                }
                                /*
                                 * For question 2 set weight in document's
                                 * query term vector as OKTF * IDF.
                                 */
                                if (qnum == 2) {
                                    if (d != null) {
                                        d.setWeight(i, OKTF * idf);
                                    }
                                }
                                /*
                                 * For question 3 set weight in document's
                                 * query term vector as calculated probabilty
                                 * and Laplace smoothing.
                                 */
                                if (qnum == 3) {
                                    if (d != null) {
                                        prob = (double) ((double) ((1 + tf) * 1.0) / (double) ((doclen + uniq_corp_size) * 1.0));
                                        //System.out.println(prob);
                                        d.setWeight(i, prob);
                                    }
                                }
                                /*
                                 * For question 4 set weight in document's
                                 * query term vector as calculated probabilty
                                 * and Jelinek-Mercer smoothing.
                                 */
                                if (qnum == 4) {
                                    if (d != null) {
                                        prob = (double) (lambda * (double) (tf * 1.0 / doclen * 1.0)) + (double) ((double) (1 - lambda) * ((double) (termCtf * 1.0) / (double) (totalTerms * 1.0)));
                                        d.setWeight(i, prob);
                                    }
                                }
                                /*
                                 * For question 5 set weight in document's
                                 * query term vector as calculated BM25
                                 * weighting function.
                                 */
                                if (qnum == 5) {
                                    if (d != null) {
                                        double numarator = (double) ((double) (r + 0.5) / (double) (R - r + 0.5));
                                        double denominator = (double) ((double) (termDf - r + 0.5) / (double) (numDocs - termDf - R + r + 0.5));
                                        k = k1 * ((double)(1 - b) + b * (double) ((double) (doclen * 1.0) / (double) (avgdoclen * 1.0)));
                                        double temp1 = (double) ((k1 + 1) * (tf * 1.0) / (double) (k + tf));
                                        double temp2 = (double) (((k2 + 1) * (double)queryTermFreq * 1.0) / (k2 + (queryTermFreq* 1.0)));
                                        double weight = Math.log10((double)((double)numarator / (double)denominator)) * temp1 * temp2;
                                        //double weight = Math.log10(temp3);
                                        d.setWeight(i, weight);
                                    }
                                }
                            }//if -else for ignore line
                        }
                    }

                    //System.out.println("Document set size: " + documentSet.size());
                    in.close();
                }// When final queryDocMap is ready or when every query term is processed

                /*
                 * For question 3, apply Laplace smoothing
                 * for all documents, when query term vector 
                 * value is 0 (i.e. document does not contain
                 * the current query term)
                 */
                if (qnum == 3)
                {
                    for (int p = 0; p < documentSet.size(); p++) {
                        Document d = (Document) documentSet.get(p);
                        double[] twArray = d.qtWeightSet;
                        for (int j = 0; j < twArray.length; j++) {
                            if (twArray[j] == 0)
                            {
                                double smoothenedWeight = (double) ((double) (1 * 1.0) / (double) ((d.doclen + uniq_corp_size) * 1.0));
                                d.setWeight(j, smoothenedWeight);
                            }
                        }
                    }
                }
                /*
                 * For question 4, apply Jelinek-Mercer smoothing
                 * for all documents, when query term vector 
                 * value is 0 (i.e. document does not contain
                 * the current query term)
                 */
                if (qnum == 4)
                {
                    for (int p = 0; p < documentSet.size(); p++) {
                        Document d = (Document) documentSet.get(p);
                        double[] twArray = d.qtWeightSet;
                        for (int j = 0; j < twArray.length; j++) {
                            if (twArray[j] == 0)
                            {
                                int qrCtf = q.getQueryTermCTF(j);
                                double smoothenedWeight = (double) ((double) (1 - lambda) * ((double) (qrCtf * 1.0) / (double) (totalTerms * 1.0)));
                                d.setWeight(j, smoothenedWeight);
                            }
                        }
                    }
                }
                
                for (int p = 0; p < documentSet.size(); p++) {
                    //System.out.println("In loop");
                    Document d = (Document) documentSet.get(p);
                    int currentDocId = d.docid;
                    double[] twArray = d.qtWeightSet;

                    double sigmaDijQj = 0.0;
                    double sigmaDijSq = 0.0;
                    double sigmaQjSq = 0.0;
                    double denominator = 1.0;
                    double similarity = 0.0;
                    
                    /*
                     * For question 1 and 2 it is the dot product of
                     * document vector and query vector
                     */
                    if ((qnum == 1) || (qnum == 2)) {
                        for (int j = 0; j < twArray.length; j++) {
                            sigmaDijQj = sigmaDijQj + twArray[j] * qtWeights[j];
                            sigmaDijSq = sigmaDijSq + (twArray[j] * twArray[j]);
                            sigmaQjSq = sigmaQjSq + (qtWeights[j] * qtWeights[j]);
                        }
                        denominator = Math.sqrt(sigmaQjSq * sigmaDijSq);

                        similarity = sigmaDijQj / denominator;

                        similarityMap.put(currentDocId, sigmaDijQj);
                        d.similarity = sigmaDijQj;
                    }
                    /*
                     * For question 3 and 4 it is product of
                     * probability values calculated and stored 
                     * in document vector.
                     */                            
                    double tempProb = 1.0;
                    if ((qnum == 3) || (qnum == 4)) {
                        for (int j = 0; j < twArray.length; j++) {
                            tempProb = tempProb * twArray[j];
                            //System.out.println(tempProb);
                        }

                        similarity = tempProb;
                        similarityMap.put(currentDocId, similarity);
                        d.similarity = similarity;
                    }
                    double tempSimVal = 0.0;
                    /*
                     * For question 5, we add the calculated weights
                     */
                    if (qnum == 5) {
                        for (int j = 0; j < twArray.length; j++) {
                            tempSimVal = tempSimVal + twArray[j];
                        }

                        similarity = tempSimVal;
                        similarityMap.put(currentDocId, similarity);
                        d.similarity = similarity;
                    }

                    //System.out.println(d.docid + " " + d.similarity);
                    for (int i = 0; i < d.qtWeightSet.length; i++) {
                        //System.out.println(d.qtWeightSet[i]);
                    }
                }

                //Transfer as List and sort it
                ArrayList<Map.Entry<Integer, Double>> l = new ArrayList(similarityMap.entrySet());
                Collections.sort(l, new Comparator<Map.Entry<?, Double>>() {
                    public int compare(Map.Entry<?, Double> o1, Map.Entry<?, Double> o2) {
                        return o2.getValue().compareTo(o1.getValue());
                    }
                });

                //System.out.println(similarityMap.size());
                /*
                 * Print first 1000 entries in array
                 */
                int cnt = 1;
                for (Map.Entry<Integer, Double> item : l) {
                    if (cnt == 1001) {
                        break;
                    }
                    System.out.println(q.queryNumber + " Q0 " + externalDocIdMap.get(item.getKey()).toString() + " " + cnt + " " + item.getValue() + " Exp");

                    cnt++;
                }

                //System.out.println(l);
            }

        } catch (MalformedURLException e) {
            System.out.println("bad url");
            //e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("file not found");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /*
     * Read the list of stop words from file
     */
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

    /*
     * Prepare the set of queries
     */
    public static void createQuerySet() {
        try {
            Scanner inputReader = null;
            inputReader = new Scanner(new File("queries.txt"));
            int totalQueries = 0;

            /*
             * 1. Read queries from queries.txt file.
             * 2. Form the object of query.
             * 3. Add it in query set.
             */

            while (inputReader.hasNextLine()) {
                String line = inputReader.nextLine();
                String[] splitString = line.split("   ", 2);
                //System.out.println(" \n tokens in string cnt " + totalQueries + " " + splitString.length + " " + splitString[1]);
                String tempStr = "0";
                if (splitString[0].contains(".")) {
                    tempStr = splitString[0].substring(0, (splitString[0].length() - 1));
                    //System.out.println(tempStr);
                }

                Query q = new Query(splitString[1]);
                int querynumber = Integer.parseInt(tempStr);
                q.setQueryNumber(querynumber);
                querySet.add(q);
                totalQueries++;
            }

            /*
             * Calculate average query length.
             */
            int totalQueryLength = 0;
            for (int i = 0; i < querySet.size(); i++) {
                Query q = (Query) querySet.get(i);
                totalQueryLength = totalQueryLength + q.getQueryLength();
            }// form a set of query terms for each query
            
            double avgQLen = (double) (totalQueryLength * 1.0 / totalQueries);
            

            /*
             * Calculate query term weights.
             */

            for (int i = 0; i < querySet.size(); i++) {
                Query q = (Query) querySet.get(i);
                q.calculateWeights(avgQLen);
                //System.out.println("query number "+q.queryNumber);
                for (int k = 0; k < q.qtWeights.length; k++) {
                    //System.out.println(q.qtWeights[k]);
                }
            }

            /*
             * Sort the query term set
             */

            Collections.sort(querySet, new Comparator<Query>() {
                public int compare(Query o1, Query o2) {
                    if (o1.queryNumber == o2.queryNumber) {
                        return 0;
                    }
                    return o1.queryNumber < o2.queryNumber ? -1 : 1;
                }
            });

            for (int cnt = 0; cnt < querySet.size(); cnt++) {
                Query q = (Query) querySet.get(cnt);
                //System.out.println(q.queryNumber);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("file not found");
        }
        //System.exit(0);
    }
}
