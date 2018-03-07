import java.io.IOException;
import java.util.*;
import java.io.*;
public class PorterCheck {
    public String runStem (String s) throws IOException {
        //stemming the words
        ArrayList<String> tokList = new ArrayList<String>();
        tokenizer tok=new tokenizer();
        /*File file = new File("rev.txt");
        FileInputStream fis = new FileInputStream(file);
        byte[] data = new byte[(int) file.length()];
        fis.read(data);
        fis.close();
        String s = new String(data, "UTF-8");*/
        tokList=tok.tokenizing(s);
        ArrayList<String> res = completeStem(tokList);
        if (res.size()>0)
         return res.get(0);
        else return "";
    }
    //method to completely stem the words in an array list
    public static ArrayList<String> completeStem(List<String> tokens1) {
        PorterAlgo pa = new PorterAlgo();
        ArrayList<String> arrstr = new ArrayList<String>();
        for (String i : tokens1) {
            String s1 = pa.stripAffixes(i);
            arrstr.add(s1);
        }
        return arrstr;
    }

}