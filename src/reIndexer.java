import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.jsoup.Jsoup;
import java.io.*;
import java.util.*;
import static com.mongodb.client.model.Filters.*;

public class reIndexer {
    static MongoClientURI connectionString = null;
    static MongoClient mongoClient = null;
    static MongoDatabase db = null;
    static PorterAlgo pa = new PorterAlgo();
    static Map<String, Integer> importance = new HashMap<>();
    static Indexer ind = new Indexer();
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
    public static void  main(String[] args) throws IOException {
        Map<String,Boolean> old=new HashMap<>();
        ind.readStopWords();
      //  connectionString = new MongoClientURI("mongodb://ghada:ghada@ds247347.mlab.com:47347/search_engine");
       // mongoClient = new MongoClient(connectionString);
        //db = mongoClient.getDatabase(connectionString.getDatabase());
        mongoClient = new MongoClient("localhost",27017);
        db = mongoClient.getDatabase("searchEngine");
        File input = new File("ghadaold.html");
        Map<String, List<Integer>> mp = new HashMap<>();
        readOld(old,input);
        File newFile= new File("ghada.html");
        MakeIndexing(newFile,old);

    }
    public static void readOld(Map<String,Boolean> old,File input) throws IOException {
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
                addWordsToMap(old, tmp);
                tmp = "";
            } else tmp += c;
        }
        if (!tmp.equals("")) {
            addWordsToMap(old, tmp);
        }
        String cont = body;
        cont=cont.toLowerCase();
        tmp = "";
        for (int i = 0; i < cont.length(); ++i) {
            char c = cont.charAt(i);
            if (c == ',' || c == '"' || c == '+' || c == '-' || c == '}' ||
                    c == '/' || c == '*' || c == '(' || c == ')' || c == '{' ||
                    c == '!' || c == ':' || c == '?' || c == '[' || c == ']' ||
                    c == '&' || c == '@' || c == ' ' || c == '.') {
                if (tmp.equals("") || isStopWord(tmp)) {
                    tmp="";continue;
                }
                //tmp = tmp.toLowerCase();
                addWordsToMap(old, tmp);
                tmp = "";
            } else tmp += c;
        }
        if (!tmp.equals(""))
            addWordsToMap(old, tmp);
        // add words indices to DB
    }
    public static void MakeIndexing(File f,Map <String,Boolean> old) throws IOException {
        Map <String,List<Integer>> nw=new HashMap<>();
        Map <String,Double> tf = ind.Index(f,nw);;
        Integer curId = 0; //(Integer) myDoc.get("id");
        addWordsToDB(nw,old,curId,tf);
    }
    public static Boolean isStopWord(String s){
        return ind.stopWords.containsKey(s);
    }

    public static void addWordsToMap(Map<String, Boolean> mp,String str) throws IOException {
        str = str.replaceAll("[^A-Za-z0-9]", "");
        mp.put(str,true);
    }

    public static void addWordsToDB(Map<String, List<Integer>> mp,Map<String, Boolean> old, Integer docID,Map<String, Double> tf){
        MongoCollection col;
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            if (pair.getKey().toString().length()<1){
                it.remove();  continue;
            }
            String str=pair.getKey().toString();
            String colName = str.substring(0,Math.min(str.length(),2));
            col = db.getCollection(colName);
            List<Integer> ls= (List<Integer>) pair.getValue();
            if (str.equals("brightest"))
                System.out.println(ls);
            if (old.containsKey(str)){
                BasicDBObject mtch1=new BasicDBObject("doc.docNum",docID);
                BasicDBObject mtch2=new BasicDBObject("word",str);
                BasicDBObject updateIdx= new BasicDBObject("$set",new BasicDBObject("doc.$.idx",ls));
                BasicDBObject updateTf= new BasicDBObject("$set",new BasicDBObject("doc.$.tf",tf.get(str)));
                col.updateOne(and(mtch1,mtch2), updateIdx);
                col.updateOne(and(mtch1,mtch2), updateTf);
                old.remove(str);
            }else {
                BasicDBObject mtch1 = new BasicDBObject("word",pair.getKey().toString());
                BasicDBObject mtch2 = new BasicDBObject("stemmed",pa.stripAffixes(pair.getKey().toString()));
                Document doc = new Document("docNum", docID).append("idx",ls).append("tf",tf.get(pair.getKey()));
                BasicDBObject pushID = new BasicDBObject("$push",new BasicDBObject("doc",doc));
                col.updateOne(and(mtch1,mtch2), pushID,new UpdateOptions().upsert(true));
            }
            it.remove(); // avoids a ConcurrentModificationException
        }
        it = old.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry pair = (Map.Entry)it.next();
            if (pair.getKey().toString().length()<1){
                it.remove();  continue;
            }
            String str=pair.getKey().toString();
            String colName = str.substring(0,Math.min(str.length(),2));
            col = db.getCollection(colName);
            BasicDBObject ob0 = new BasicDBObject("docNum",docID);
            BasicDBObject ob1 = new BasicDBObject("doc",ob0);
            BasicDBObject rem = new BasicDBObject("$pull",ob1);
            BasicDBObject matchObject = new BasicDBObject("word",str);
            col.updateOne(matchObject,rem);
        }

    }

}