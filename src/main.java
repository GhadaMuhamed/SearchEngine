import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Indexes;
import java.io.*;
import java.util.*;

public class main {
    public static Map<String, Integer> stopWords;
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
    public static void main(String[] args) throws IOException, InterruptedException {
        // MongoClientURI connectionString = new MongoClientURI("mongodb://ghada:ghada@ds163796.mlab.com:63796/search_engine");
        Map<String,Boolean> oldFiles=new HashMap<>();
        File oldFolder = new File("C:\\Users\\ghada\\Documents\\old");
        for (final File fileEntry : oldFolder.listFiles())
            oldFiles.put(fileEntry.getName(),true);
        Date t1=new Date();
        MongoClient mongoClient = new MongoClient("localhost",27017);  //create mongo client
        MongoDatabase db = mongoClient.getDatabase("search___Engine");
        MongoCollection col;
        String colName = "invertedDocument";
        col = db.getCollection(colName);
        col.createIndex(Indexes.descending("word"));
        System.out.println("Connected to the database successfully");
        readStopWords();
        /*String paths[] =new String[2005];
        for (int i=0;i<2005;++i)
            paths[i]=Integer.toString(i)+".html";*/
        indexer ind = new indexer(db,stopWords,col,oldFiles);
        ArrayList<Thread>threads=new ArrayList<Thread>();
        for(Integer i=0;i<3;++i){
            threads.add(new Thread( new Master(ind),"T"+i.toString()));
            threads.get(i).start();
        }
        /*for(Integer i=0;i<2005;++i) {
            new indexer(db,stopWords,col,oldFiles).start();
        }*/
        /*int n=0;

        for(Integer i=n+0;i<n+10;++i){
            threads.get(i).join();
        }

        for(Integer i=n+10;i<n+20;++i){
            threads.add(new Thread( new indexer(db,stopWords,paths[i],i,col),"T"+i.toString()));
            threads.get(i).start();
        }
        for(Integer i=n+10;i<n+20;++i){
            threads.get(i).join();
        }

        for(Integer i=n+20;i<n+30;++i){
            threads.add(new Thread( new indexer(db,stopWords,paths[i],i,col),"T"+i.toString()));
            threads.get(i).start();
        }
        for(Integer i=n+20;i<n+30;++i){
            threads.get(i).join();
        }


        for(Integer i=n+30;i<n+40;++i){
            threads.add(new Thread( new indexer(db,stopWords,paths[i],i,col),"T"+i.toString()));
            threads.get(i).start();
        }
        for(Integer i=n+30;i<n+40;++i){
            threads.get(i).join();
        }


        for(Integer i=n+40;i<n+50;++i){
            threads.add(new Thread( new indexer(db,stopWords,paths[i],i,col),"T"+i.toString()));
            threads.get(i).start();
        }
        for(Integer i=n+40;i<n+50;++i){
            threads.get(i).join();
        }


        for(Integer i=n+50;i<n+60;++i){
            threads.add(new Thread( new indexer(db,stopWords,paths[i],i,col),"T"+i.toString()));
            threads.get(i).start();
        }
        for(Integer i=n+50;i<n+60;++i){
            threads.get(i).join();
        }


        for(Integer i=n+60;i<n+70;++i){
            threads.add(new Thread( new indexer(db,stopWords,paths[i],i,col),"T"+i.toString()));
            threads.get(i).start();
        }
        for(Integer i=n+60;i<n+70;++i){
            threads.get(i).join();
        }



        for(Integer i=n+70;i<n+80;++i){
            threads.add(new Thread( new indexer(db,stopWords,paths[i],i,col),"T"+i.toString()));
            threads.get(i).start();
        }
        for(Integer i=n+70;i<n+80;++i){
            threads.get(i).join();
        }


        for(Integer i=n+80;i<n+90;++i){
            threads.add(new Thread( new indexer(db,stopWords,paths[i],i,col),"T"+i.toString()));
            threads.get(i).start();
        }
        for(Integer i=n+80;i<n+90;++i){
            threads.get(i).join();
        }


        for(Integer i=n+90;i<n+100;++i){
            threads.add(new Thread( new indexer(db,stopWords,paths[i],i,col),"T"+i.toString()));
            threads.get(i).start();
        }
        for(Integer i=n+90;i<n+100;++i){
            threads.get(i).join();
        }


        for(Integer i=n+100;i<n+110;++i){
            threads.add(new Thread( new indexer(db,stopWords,paths[i],i,col),"T"+i.toString()));
            threads.get(i).start();
        }
        for(Integer i=n+100;i<n+110;++i){
            threads.get(i).join();
        }

        for(Integer i=n+110;i<n+120;++i){
            threads.add(new Thread( new indexer(db,stopWords,paths[i],i,col),"T"+i.toString()));
            threads.get(i).start();
        }
        for(Integer i=n+110;i<n+120;++i){
            threads.get(i).join();
        }


        for(Integer i=n+120;i<n+130;++i){
            threads.add(new Thread( new indexer(db,stopWords,paths[i],i,col),"T"+i.toString()));
            threads.get(i).start();
        }
        for(Integer i=n+120;i<n+130;++i){
            threads.get(i).join();
        }


        for(Integer i=n+130;i<n+137;++i){
            threads.add(new Thread( new indexer(db,stopWords,paths[i],i,col),"T"+i.toString()));
            threads.get(i).start();
        }
        for(Integer i=n+130;i<n+137;++i){
            threads.get(i).join();
        }

        */
        Date t2=new Date();
        System.out.println(t2.getTime()-t1.getTime());
    }
}

