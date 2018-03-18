import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import java.io.*;
import java.util.*;
import static com.mongodb.client.model.Filters.*;

public class Indexer {
    static MongoClientURI connectionString = null;
    static MongoClient mongoClient = null;
    static MongoDatabase db = null;
    public static Map<String, Integer> stopWords;
    static PorterAlgo pa = new PorterAlgo();
    static Map<String, Integer> importance = new HashMap<>();

    public static void addHeader(int val, String s) {
        String tmp = "";
        s=s.toLowerCase();
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            if (c == ',' || c == '"' || c == '+' || c == '-' || c == '}' ||
                    c == '/' || c == '*' || c == '(' || c == ')' || c == '{' ||
                    c == '!' || c == ':' || c == '?' || c == '[' || c == ']' ||
                    c == '&' || c == '@' || c == ' ') {
                if (tmp.equals("") || isStopWord(tmp)) {
                    tmp = "";
                    continue;
                }

                // addWordsToMap(mp, tmp, i);
                if (!importance.containsKey(tmp))
                    importance.put(tmp, val);
                tmp = "";
            } else tmp += c;
        }
        if (!tmp.equals("")) {
            //addWordsToMap(mp, tmp, title.length());
            if (!importance.containsKey(tmp))
                importance.put(tmp, val);
        }
    }
    public static void readStopWords(){
        stopWords = new HashMap<String, Integer>();
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
    }
    public static Map<String,Double> Index( File input,Map<String, List<Integer>> mp)throws IOException{
        org.jsoup.nodes.Document htmlDocument = Jsoup.parse(input, "UTF-8", "");
        String body = htmlDocument.body().text();
        String title = htmlDocument.title();
        String tmp = "";
        title = title.toLowerCase();
        for (int i = 0; i < title.length(); ++i) {
            char c = title.charAt(i);
            if (c == ',' || c == '"' || c == '+' || c == '-' || c == '}' ||
                    c == '/' || c == '*' || c == '(' || c == ')' || c == '{' ||
                    c == '!' || c == ':' || c == '?' || c == '[' || c == ']' ||
                    c == '&' || c == '@' || c == ' ' || c=='.') {
                if (tmp.equals("") || isStopWord(tmp)) {
                    tmp = "";
                    continue;
                }
                //tmp = tmp.toLowerCase();
                addWordsToMap(mp, tmp, i);
                if (!importance.containsKey(tmp))
                    importance.put(tmp, 8);
                tmp = "";
            } else tmp += c;

        }
        if (!tmp.equals("")) {
            addWordsToMap(mp, tmp, title.length());
            if (!importance.containsKey(tmp))
                importance.put(tmp, 8);
        }
        Elements h1Tags = htmlDocument.select("h1");
        String sH1 = h1Tags.text();
        addHeader(2, sH1);
        // Group of all h2-Tags
        Elements h2Tags = htmlDocument.select("h2");
        String sH2 = h2Tags.text();
        addHeader(3, sH2);
        Elements h3Tags = htmlDocument.select("h3");
        String sH3 = h3Tags.text();
        addHeader(4, sH3);
        // Group of all h2-Tags
        Elements h4Tags = htmlDocument.select("h4");
        String sH4 = h4Tags.text();
        addHeader(5, sH4);
        Elements h5Tags = htmlDocument.select("h5");
        String sH5 = h5Tags.text();
        addHeader(6, sH5);
        // Group of all h2-Tags
        Elements h6Tags = htmlDocument.select("h6");
        String sH6 = h1Tags.text();
        addHeader(7, sH6);
        String cont = body;
        cont=cont.toLowerCase();
        tmp = "";
        for (int i = 0; i < cont.length(); ++i) {
            char c = cont.charAt(i);
            if (c == ',' || c == '"' || c == '+' || c == '-' || c == '}' ||
                    c == '/' || c == '*' || c == '(' || c == ')' || c == '{' ||
                    c == '!' || c == ':' || c == '?' || c == '[' || c == ']' ||
                    c == '&' || c == '@' || c == ' ' || c == '.') {
                if (tmp.equals("") || isStopWord(tmp)) {tmp="";continue;}
                //tmp = tmp.toLowerCase();
                
                addWordsToMap(mp, tmp, i + title.length() + 1);
                if (!importance.containsKey(tmp))
                    importance.put(tmp, 1);
                tmp = "";
            } else tmp += c;
        }
        if (!tmp.equals(""))
            addWordsToMap(mp, tmp, cont.length() + title.length() + 1);
        // add words indices to DB
         return calculateTf(mp);
    }
    public static void main(String[] args) throws IOException {
        readStopWords();
       // connectionString = new MongoClientURI("mongodb://ghada:ghada@ds247347.mlab.com:47347/search_engine");
        mongoClient = new MongoClient("localhost",27017);
        db = mongoClient.getDatabase("searchEngine");
        File input = new File("ghadaold.html");
        Integer curId = 0; //(Integer) myDoc.get("id");
        Map<String, List<Integer>> mp =new HashMap<>();
        Map<String, Double> tf =  Index(input,mp);
        addWordsToDB(mp, curId, tf);
    }

    public static Boolean isStopWord(String s){
        return stopWords.containsKey(s);
    }

    public static void addWordsToMap(Map<String, List<Integer>> mp,String str,Integer idx) throws IOException {
        str = str.replaceAll("[^A-Za-z0-9]", "");
        if (!mp.containsKey(str))
            mp.put(str,new ArrayList<>());
        mp.get(str).add(idx - str.length());
    }
    public static Map<String,Double> calculateTf(Map<String, List<Integer>> mp){
        Double dom = 0.0;
        Map <String,Double> ret = new HashMap<>();
        for (Map.Entry<String, List<Integer>> entry : mp.entrySet())
            dom +=entry.getValue().size()*entry.getValue().size();
        dom=Math.sqrt(dom);
        for (Map.Entry<String, List<Integer>> entry : mp.entrySet()) {
            Integer num=1;
            if (importance.get(entry.getKey()) !=null)
                num=importance.get(entry.getKey());
            ret.put(entry.getKey(), (entry.getValue().size() / dom) * num);
        }
        return ret;
    }
    public static void addWordsToDB(Map<String, List<Integer>> mp, Integer docID,Map<String, Double> tf){
        MongoCollection col;
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            if (pair.getKey().toString().length()<1){it.remove();  continue;}
            String str=pair.getKey().toString();
            String colName = str.substring(0,Math.min(str.length(),2));
            col = db.getCollection(colName);
            List<Integer> ls= (List<Integer>) pair.getValue();
            BasicDBObject mtch1 = new BasicDBObject("word",pair.getKey().toString());
            BasicDBObject mtch2 = new BasicDBObject("stemmed",pa.stripAffixes(pair.getKey().toString()));
            Document doc = new Document("docNum", docID).append("idx",ls).append("tf",tf.get(pair.getKey()));
            BasicDBObject pushID = new BasicDBObject("$push",new BasicDBObject("doc",doc));
            col.updateOne(and(mtch1,mtch2), pushID,new UpdateOptions().upsert(true));
            it.remove(); // avoids a ConcurrentModificationException
        }

    }


}