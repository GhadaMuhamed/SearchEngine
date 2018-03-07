import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import java.io.IOException;
import java.util.*;
import static com.mongodb.client.model.Filters.*;

public class Main {
    static PorterCheck pC=new PorterCheck();
    static MongoClientURI connectionString = null;
    static MongoClient mongoClient = null;
    static MongoDatabase db = null;

    public static void main(String[] args) throws IOException {
          connectionString = new MongoClientURI("mongodb://ghada:ghada@ds247347.mlab.com:47347/search_engine");
          mongoClient = new MongoClient(connectionString);
          db = mongoClient.getDatabase(connectionString.getDatabase());
          MongoCollection words = db.getCollection("tmp");
       // MongoCollection col = db.getCollection("Words");

        /*List<Integer> ls=new ArrayList<>();
        ls.add(2);
        ls.add(8);
        ls.add(1);

        System.out.println(col.updateOne(
                eq("word", "ghada"),new BasicDBObject("$push",new BasicDBObject("occur",ls)),new UpdateOptions().upsert(true)));*/
       /* Document doc =new Document("name","commuication");
       Vector<Pair<Integer,Vector<Integer>>> vec=new Vector<>();
        Vector<Integer> tmp=new Vector<>();
        tmp.add(5);tmp.add(6);tmp.add(7);
        vec=new Pair(0,tmp);
        vec.add(new Pair(0,tmp));
        vec.add(new Pair(1,tmp));
        Document  person = new Document ("occur",tmp);
        words.insertOne(person);
        FindIterable<Document> docs = words.find();
        for (Document myDoc : docs) {
            //log.info(doc.getString("name"));
            ArrayList<ArrayList<Integer>> vecy =( ArrayList<ArrayList<Integer>>) myDoc.get("occur");
            System.out.println(vecy);

        }
        //System.out.println(findIterable.first());*/
        int id=0;
        FindIterable <Document> doc = words.find(gte("id", id));
        while (doc.first() != null) {
            for (Document myDoc : doc) {
                Map<String, List<Integer>> withStem = new HashMap<>();
                Map<String, List<Integer>> withoutStem = new HashMap<>();
                String res;
                Integer curId = (Integer) myDoc.get("id");
                id=Math.max(id,curId);
                String cont = myDoc.get("Content").toString();
                String tmp= "";
                for (int i=0;i<cont.length();++i)
                    if (cont.charAt(i)== ' '){
                        if (tmp.equals("")) continue;
                        addWordsToMap(withStem,withoutStem,tmp,i);
                        tmp="";
                    }
                    else tmp += cont.charAt(i);

                if (!tmp.equals(""))
                    addWordsToMap(withStem,withoutStem,tmp,cont.length());
                // add words indices to DB
                addWordsToDB(withStem,curId);
                addWordsToDB(withoutStem,curId);
            }
            doc = words.find(gte("id", id+1));
        }
    }

    public static void addWordsToMap(Map<String, List<Integer>> withStem,Map<String, List<Integer>> withoutStem,String str,Integer idx) throws IOException {
        String res =  pC.runStem(str);
        str = str.replaceAll("[^A-Za-z0-9]", "");
        if (res.equals("")) return;
        else {
            if (res.equals(str)){
                str = str.toUpperCase();
                if (!withoutStem.containsKey(str))
                    withoutStem.put(str,new ArrayList<>());
                 withoutStem.get(str).add(idx - res.length());
            }
            else {
                res = res.toLowerCase();
                if (!withStem.containsKey(res))
                    withStem.put(res,new ArrayList<>());
                    withStem.get(res).add(idx - str.length());
                str = str.toUpperCase();
                if (!withoutStem.containsKey(str))
                    withoutStem.put(str,new ArrayList<>());
                withoutStem.get(str.toUpperCase()).add(idx - str.length());
            }
        }
    }
    public static void addWordsToDB(Map<String, List<Integer>> mp, Integer docID){
        MongoCollection col;
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            String colName = pair.getKey().toString().substring(0,2);
            col = db.getCollection(colName);
            List<Integer> ls= (List<Integer>) pair.getValue();
            col.updateOne(
                    eq("word", pair.getKey().toString()),new BasicDBObject("$push",new BasicDBObject("occur",ls)),new UpdateOptions().upsert(true));
            col.updateOne(
                    eq("word", pair.getKey().toString()),new BasicDBObject("$push",new BasicDBObject("docNum",docID)),new UpdateOptions().upsert(true));
            it.remove(); // avoids a ConcurrentModificationException
        }

    }
}
