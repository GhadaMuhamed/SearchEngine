import org.jsoup.Jsoup;
import java.io.*;
import java.util.*;

public class indexing {

    Map<String, Integer> stopWords;
    public indexing( Map<String, Integer> mapStopWords){
        stopWords=mapStopWords;
    }
    static PorterAlgo pa = new PorterAlgo();

    public  Boolean isStopWord(String s){
        return stopWords.containsKey(s);
    }
    public  Map<String,Double> calculateTf(Map<String, List<Integer>> mp){
        Double dom = 0.0;
        Map <String,Double> ret = new HashMap<>();
        for (Map.Entry<String, List<Integer>> entry : mp.entrySet())
            dom +=entry.getValue().size()*entry.getValue().size();
        dom=Math.sqrt(dom);
        for (Map.Entry<String, List<Integer>> entry : mp.entrySet()) {
            ret.put(entry.getKey(), (entry.getValue().size() / dom));
        }
        return ret;
    }

    public  Map<String,Double> Index( File input,Map<String, List<Integer>> mp) {
        org.jsoup.nodes.Document htmlDocument = null;
        try {
            htmlDocument = Jsoup.parse(input, "UTF-8", "");
        } catch (IOException e) {
            System.out.println("can't parse in index document : " + input.getName());
        }
        if (htmlDocument== null)
            return new HashMap<>();
        String body = htmlDocument.body().text();
        //System.out.println(body);
        String title = htmlDocument.title();
        String tmp = "";
        title = title.toLowerCase();
        int cnt=0;
        for (int i = 0; i < title.length(); ++i) {
            char c = title.charAt(i);
            if (!(Character.isDigit(c) || Character.isLetter(c) || c=='.' || c=='\'')) {
                if (tmp.equals("") || isStopWord(tmp)) {
                    tmp = "";
                    continue;
                }
                addWordsToMap(mp, tmp, cnt++);

                tmp = "";
            } else if (c!='.') tmp += c;

        }
        if (!tmp.equals("") && !isStopWord(tmp) && !tmp.equals(".")) {
            addWordsToMap(mp, tmp, cnt++);

        }

        String cont = body;
        cont=cont.toLowerCase();

        tmp = "";
        for (int i = 0; i < cont.length(); ++i) {
            char c = cont.charAt(i);
            if (!(Character.isDigit(c) || Character.isLetter(c) || c=='.' || c=='\'')) {
                if (tmp.equals("") || isStopWord(tmp)) {tmp="";continue;}
                //tmp = tmp.toLowerCase();

                addWordsToMap(mp, tmp, cnt++);

                tmp = "";
            } else if (c!='.') tmp += c;
        }
        if (!tmp.equals("") && !isStopWord(tmp) && !tmp.equals("."))
            addWordsToMap(mp, tmp, cnt++);
        // add words indices to DB
        return calculateTf(mp);
    }
    public static void addWordsToMap(Map<String, List<Integer>> mp,String str,Integer idx)  {
        if (!mp.containsKey(str))
            mp.put(str,new ArrayList<>());
        mp.get(str).add(idx);
    }


}