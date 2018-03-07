import java.io.IOException;
import java.util.*;
import java.io.*;
public class tokenizer{
    public ArrayList <String >tokenizing (String s) {
        s=s.toLowerCase();
        Map<String, Integer> stopWords = new HashMap<String, Integer>();
        File file=new File("stopWords.txt");
        FileReader fileReader;
        try{ fileReader=new FileReader(file);
            BufferedReader br = new BufferedReader(fileReader);
            String stopWord;
            try {
                stopWord = br.readLine();
                while (stopWord != null) {
                    stopWords.put(stopWord,1);
                    stopWord = br.readLine();
                }
            }
            catch(IOException e){}
            try{ fileReader.close();}
            catch (IOException e){
            }
        }
        catch (FileNotFoundException x){
            System.out.println("file not found");
        }
        ArrayList<String> tokenlist = new ArrayList<String>();
        StringTokenizer strtoken = new StringTokenizer(s);
        String hh = "";
        while (strtoken.hasMoreElements()) {
            hh="";
            String ss = strtoken.nextToken();
            for (int i = 0; i < ss.length(); ++i) {
                if (ss.charAt(i) == ',' || ss.charAt(i) == '"' || ss.charAt(i) == '+' || ss.charAt(i) == '-' || ss.charAt(i) == '}' ||
                        ss.charAt(i) == '/' || ss.charAt(i) == '*' || ss.charAt(i) == '(' || ss.charAt(i) == ')' || ss.charAt(i) == '{' ||
                        ss.charAt(i) == '!' || ss.charAt(i) == ':' || ss.charAt(i) == '?' || ss.charAt(i) == '[' || ss.charAt(i) == ']' ||
                        ss.charAt(i) == '&' || ss.charAt(i) == '@') {
                    if (hh.compareTo("") != 0&&!stopWords.containsKey(hh))
                        tokenlist.add(hh);
                    hh = "";
                } else
                    hh += ss.charAt(i);
            }
            if (hh.compareTo("") != 0&&!stopWords.containsKey(hh))
                tokenlist.add(hh);
        }
        return tokenlist;
    }
}