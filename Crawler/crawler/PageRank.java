package crawler;
import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import java.io.*;
import java.util.*;
public class PageRank {


        public static int cnt;
        public static double alpha = .85;
        private MongoDatabase database;
        public PageRank(MongoDatabase db){
            database = db;
        }
        public void rank() throws IOException {
            // Creating a Mongo client
            // Retieving a collection
            MongoCollection<Document> collection = database.getCollection("Visit");
            BasicDBObject projection = new BasicDBObject();
            projection.put("Parent", 1);
            projection.put("Id", 1);
            long len = collection.count();
            cnt = (int) len;
            System.out.println(len);
            FindIterable<Document> iterDoc = collection.find().projection(projection);
            Iterator<Document> iterator = iterDoc.iterator();
            List<Map<String, Object>> list = new ArrayList<>();
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream("G:\\mat.txt"), "utf-8"))) {
                writer.write(Integer.toString(cnt) + "\n");
                while (iterator.hasNext()) {
                    Document document = iterator.next();
                    document.remove("_id");
                    Map<String, Object> map = new HashMap<>(document);
                    Object p = map.get("Parent");
                    Object idO = map.get("Id");
                    String id = idO.toString();
                    String par = p.toString();
                    String[] parents = par.split(" ");
                    for (int i = 0; i < parents.length; ++i) {
                        String tmp = parents[i];
                        if (tmp.equals(""))
                            continue;
                        writer.write(parents[i] + " " + id + "\n");

                     }
            }
        }

            try {
                Runtime.getRuntime().exec("cmd /c start cmd.exe /K \"cd C:\\ProgramData\\NVIDIA Corporation\\CUDA Samples\\v8.0\\bin\\win64\\Release &&  simpleCUBLAS.exe\"");
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

}

