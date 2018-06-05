import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import org.bson.Document;
import org.jsoup.Jsoup;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class indexer extends indexing {
    MongoDatabase db;
    MongoCollection col;
    Map<String,Boolean> oldFiles;
    public static String newFolderName="C:\\Users\\ghada\\Documents\\crawl";
    public static String oldFolderName="C:\\Users\\ghada\\Documents\\old";
    private Map<String,Boolean> processing;
    public indexer( MongoDatabase database, Map<String, Integer> stopWords,MongoCollection c,Map<String,Boolean> of){
        super(stopWords);
        db=database;
        col = c;
        oldFiles=of;
        processing = new HashMap<>();
    }

    public synchronized String getName(Map<String,Boolean> old)  {
        String name="";
        File newFolder = new File(newFolderName);
        for (File fileEntry : newFolder.listFiles()){
            name=fileEntry.getName();
            //System.out.println(Thread.currentThread().getId()+" " +name);
            //System.out.println(newFolder.length());
            Boolean flag=oldFiles.containsKey(name);
            org.jsoup.nodes.Document htmlDocument = null;
            if (flag) {
                String f = oldFolderName + "\\" + name;
                File input=new File(f);
                try {
                    htmlDocument = Jsoup.parse(input, "UTF-8", "");
                } catch (IOException e) {
                    name="";
                    continue;
                }
            }
            try {
                Path temp = Files.move
                        (Paths.get(newFolderName+"\\"+name),
                                Paths.get(oldFolderName+"\\"+name),REPLACE_EXISTING);
                //System.out.println("replace "+Thread.currentThread().getId()+" "+name);
            }
            catch (IOException e){
                //System.out.println("Error in move files");
                //System.out.println("Thrad :" +Thread.currentThread().getId());

                //System.out.println("name :" +name);
                old.clear();
                name = "";
                continue;
            }

            if (!name.equals("")) {
                if (flag) readOld(htmlDocument,old);
                else oldFiles.put(name,true);
                //System.out.println(old.size());
            }
            break;
        }
       // System.out.println("ret "+Thread.currentThread().getId()+" "+name);
        return name;
    }

    public void start() {
        File input=null;
        while (true){
            Map<String,Boolean> old=new HashMap<>();
            String name = "";
            name = getName(old);
            if (name.equals("")) continue;
            if (processing.containsKey(name)) continue;
            processing.put(name,true);
            input = new File(oldFolderName+"\\"+name);
            if (!name.equals("")){
                Map<String, List<Integer>> mp =new HashMap<>();
                Map<String, Double> tf =  super.Index(input,mp);
                String id=name.substring(0,name.length()-5);
                addWordsToDB(mp,old, Integer.parseInt(id), tf,name);
            }
        }
    }
    public void readOld(org.jsoup.nodes.Document htmlDocument , Map<String,Boolean> old) {
        String body = htmlDocument.body().text();
        String title = htmlDocument.title();
        String tmp = "";
        title = title.toLowerCase();
        for (int i = 0; i < title.length(); ++i) {
            char c = title.charAt(i);
            if (!(Character.isDigit(c) || Character.isLetter(c) || c=='.' || c=='\'')) {
                if (tmp.equals("") || super.isStopWord(tmp)) {
                    tmp = "";
                    continue;
                }
                addOldWords(old, tmp);
                tmp = "";
            } else if (c!='.') tmp += c;
        }
        if (!tmp.equals("") && !super.isStopWord(tmp) && !tmp.equals("."))
            addOldWords(old, tmp);

        String cont = body;
        cont=cont.toLowerCase();
        tmp = "";
        for (int i = 0; i < cont.length(); ++i) {
            char c = cont.charAt(i);
            if (!(Character.isDigit(c) || Character.isLetter(c) || c=='.' || c=='\'')) {
                if (tmp.equals("") || super.isStopWord(tmp)) {
                    tmp="";continue;
                }
                addOldWords(old, tmp);
                tmp = "";
            } else if (c!='.') tmp += c;
        }
        if (!tmp.equals("") && !super.isStopWord(tmp) && !tmp.equals("."))
            addOldWords(old, tmp);
        // add words indices to DB
    }
    public static void addOldWords(Map<String, Boolean> mp,String str) {
        str = str.replaceAll("[^A-Za-z0-9]", "");
        mp.put(str,true);
    }


    public void addWordsToDB(Map<String, List<Integer>> mp,Map<String, Boolean> old, Integer docID,Map<String, Double> tf,String name)  {
        Iterator it = mp.entrySet().iterator();
        List<WriteModel<Document>> writes = new ArrayList<>();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            if (pair.getKey().toString().length()<1){
                it.remove();  continue;
            }
            String str = pair.getKey().toString();
            List<Integer> ls= (List<Integer>) pair.getValue();
            String stemmed = super.pa.stripAffixes(pair.getKey().toString());
            if (super.isStopWord(stemmed)) {
                it.remove();  continue;
            }
            if (old.containsKey(str)){
                Document mtch1=new Document("doc.docNum",docID);
                mtch1.append("word",str);
                Document updateIdx= new Document("$set",new Document("doc.$.idx",ls));
                Document updateTf= new Document("$set",new Document("doc.$.tf",tf.get(str)));
                writes.add(new UpdateOneModel<>(mtch1,updateIdx,new UpdateOptions()));
                writes.add(new UpdateOneModel<>(mtch1,updateTf,new UpdateOptions()));
                old.remove(str);
            }else {
                Document mtch1 = new Document("word", pair.getKey().toString());
                mtch1.append("stemmed",stemmed);
                Document doc = new Document("docNum", docID).append("idx", ls).append("tf", tf.get(pair.getKey()));
                Document pushID = new Document("$push", new Document("doc", doc));
                writes.add(new UpdateOneModel<>(mtch1,pushID,new UpdateOptions().upsert(true)));
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
            String stemmed = super.pa.stripAffixes(str);
            if (super.isStopWord(stemmed)) {
                it.remove();  continue;
            }
            Document ob0 = new Document("docNum",docID);
            BasicDBObject ob1 = new BasicDBObject("doc",ob0);
            BasicDBObject rem = new BasicDBObject("$pull",ob1);
            BasicDBObject matchObject = new BasicDBObject("word",str);
            writes.add(new UpdateOneModel<>(matchObject,rem));
        }
        if (writes.size()<=0) return;
        synchronized (col){
            col.bulkWrite(writes,new BulkWriteOptions().ordered(false));
        }
        processing.remove(name);
    }
}