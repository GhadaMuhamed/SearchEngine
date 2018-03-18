import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;

import java.io.IOException;
import java.util.Arrays;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;


public class test {
    static MongoClientURI connectionString = null;
    static MongoClient mongoClient = null;
    static MongoDatabase db = null;
    public static void main(String[] args) throws IOException {
        MongoCollection col;
        connectionString = new MongoClientURI("mongodb://ghada:ghada@ds247347.mlab.com:47347/search_engine");
        mongoClient = new MongoClient(connectionString);
        db = mongoClient.getDatabase(connectionString.getDatabase());

        col = db.getCollection("ghada");
       // BasicDBObject mtch1 = new BasicDBObject("word","references");
        //BasicDBObject mtch2 = new BasicDBObject("stemmed","refer");
        Document doc = new Document("docNum", 2).append("idx",Arrays.asList(1,2,3)).append("tf",0.5);

        BasicDBObject mtch=new BasicDBObject("doc.docNum",0);
        BasicDBObject mtch_=new BasicDBObject("word","springer");
       /* BasicDBObject ob0 = new BasicDBObject("idx",);
        BasicDBObject ob1= new BasicDBObject("doc",ob0);
        BasicDBObject ob2 = new BasicDBObject("$set",ob1);
        BasicDBObject ob3= new BasicDBObject("doc.tf",0.5);*/
        BasicDBObject updateIdx= new BasicDBObject("$set",new BasicDBObject("doc.$.idx",Arrays.asList(1,5,9)));
        BasicDBObject updateTf= new BasicDBObject("$set",new BasicDBObject("doc.$.tf",0.9));

        col.updateOne(and(mtch,mtch_), updateIdx);
        col.updateOne(and(mtch,mtch_), updateTf);

        /* BasicDBObject mtch0 = new BasicDBObject("docNum",0);
        BasicDBObject mtch = new BasicDBObject("doc",mtch0);
        BasicDBObject rem = new BasicDBObject("$pull",mtch);
        BasicDBObject matchQuery = new BasicDBObject("word","references");
        col.updateOne(matchQuery,rem);*/
    }
}
