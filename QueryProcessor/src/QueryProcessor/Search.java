package QueryProcessor;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import java.util.*;
import javafx.util.Pair;
import org.bson.Document;
import static QueryProcessor.Search.SearchWords;

public class Search {
    public static HashMap<String, Double> idfs;
    public static HashMap<String, Double> idfsquery ;
    static LinkedHashMap<String, Integer> SearchWords;
    static LinkedHashSet<String> SearchStem ;
    static List<BasicDBObject> ObjsQuery;
    static LinkedHashMap<Integer, Doc> MyMapDoc;
    public LinkedHashMap<String,Integer >  TfWordsinQuery;
    //public String search;
    public static MongoCollection CollectionInvDoc;
    public static List<Double> pagerank = new ArrayList<>();
    public static PorterAlgo pa = new PorterAlgo();
    public static boolean phrase;
    public int cntwords;
    public Search(List<Integer> ArrayIDS,List<Double> pagerank, MongoCollection CollectionInvDoc, LinkedHashMap<String, Integer> SearchWords,
                  LinkedHashSet<String> SearchStem, int cntwords, boolean phrase) {
       // this.search=search;
        this.phrase=phrase;
        this.SearchWords=SearchWords;
        this.pagerank=pagerank;
        this.SearchStem=SearchStem;
        this.cntwords=cntwords;
        ObjsQuery = new ArrayList<>();
        MyMapDoc = new LinkedHashMap<>();
        TfWordsinQuery=new LinkedHashMap<String,Integer >();
        this.CollectionInvDoc=CollectionInvDoc;
        SearchStem = new LinkedHashSet<>();
        SearchWords = new LinkedHashMap<>();
        idfsquery = new HashMap<String, Double>();
        idfs = new HashMap<String, Double>();
        run(ArrayIDS);
    }
    public  void run(List<Integer> ls){
        MyMapDoc.clear();

        CreateQuery(pa);
        if (!ObjsQuery.isEmpty())
            GetMyMap();

        //print 3ady ll map bt3te y3ne

        //System.out.println(MyMapDoc.size());

        if (phrase)
            GetMyMap_Phrase();


        rank(ls,idfs, pagerank,  MyMapDoc, SearchWords, idfsquery , TfWordsinQuery,cntwords);
        // System.out.println(MyMapDoc.size());

    }

    public  void countidfsquery(HashMap<String, Double> idfsquery,
                                LinkedHashMap<String, Integer>  SearchWords,HashMap<String, Double> idfs
            ,FindIterable<org.bson.Document> doc) {
        Iterator<Map.Entry<String, Integer>> iterator = SearchWords.entrySet().iterator();
        while (iterator.hasNext()) {
            double idf_query=0.0;
            int num=0;
            String wordnow = iterator.next().getKey();
            String stemmed_wordnow=pa.stripAffixes(wordnow);
            for (Document myDoc : doc) {
                String word = myDoc.get("word").toString();

                if(word.equals(wordnow)) {
                    if(TfWordsinQuery.containsKey(word)){
                        int val=TfWordsinQuery.get(word);
                        val+=SearchWords.get(wordnow);
                        TfWordsinQuery.put(word,val);
                    }
                    else
                        TfWordsinQuery.put(word,SearchWords.get(wordnow));
                    continue;
                }
                String stemmed_word=pa.stripAffixes(word);
                if(stemmed_word.equals(stemmed_wordnow)) {
                    if(TfWordsinQuery.containsKey(word)){
                        int val=TfWordsinQuery.get(word);
                        val+=SearchWords.get(wordnow);
                        TfWordsinQuery.put(word,val);
                    }
                    else
                        TfWordsinQuery.put(word,SearchWords.get(wordnow));
                    idf_query += idfs.get(word);
                    num+=1;
                }
            }
            if(num!=0)
                idfsquery.put(wordnow,idf_query/num);
            else
                idfsquery.put(wordnow,0.0);
        }
    }

    public static void CreateQuery(PorterAlgo pa) {
        if (!phrase) {
            for (int i = 0; i < SearchStem.size(); i++) {
                //System.out.println("hro "+new ArrayList<>(SearchStem).get(i));
                BasicDBObject query = new BasicDBObject("stemmed", new BasicDBObject("$eq", new ArrayList<>(SearchStem).get(i)));
                ObjsQuery.add(query);
            }
        }

        if (phrase) {
            for (int i = 0; i < SearchWords.size(); i++) {

                BasicDBObject query = new BasicDBObject("word", new BasicDBObject("$eq", new ArrayList<>(SearchWords.keySet()).get(i)));
                ObjsQuery.add(query);
            }
        }
    }


    public void GetMyMap() {

        BasicDBObject orQuery = new BasicDBObject();
       /* System.out.println("objy");
        for (int i=0;i<ObjsQuery.size();++i)
            System.out.println(ObjsQuery.get(i));*/
        orQuery.put("$or", ObjsQuery);
        BasicDBObject projection = new BasicDBObject();
        projection.put("_id",0);
        if(!phrase)
            projection.put("doc.idx",0);
        FindIterable<org.bson.Document> doc = CollectionInvDoc.find(orQuery).projection(projection);
        if(doc==null)
            return;

        for (Document myDoc : doc) {
            String word = myDoc.get("word").toString();
            List<Document> obj = (List<Document>) myDoc.get("doc");
            for (Document obj1 : obj) {
                int docnum = Integer.parseInt(obj1.get("docNum").toString());
                double tf = Double.parseDouble(obj1.get("tf").toString());
                List<Integer> idx=new ArrayList<>();
                if(phrase)
                    idx = (List<Integer>) obj1.get("idx");
                idfs.put(word, Math.log((double) pagerank.size() / obj.size()));
                if (!MyMapDoc.containsKey(docnum)) {
                    Doc docobj = new Doc(word, tf, idx);
                    MyMapDoc.put(docnum, docobj);
                } else {
                    Doc docobj = MyMapDoc.get(docnum);
                    docobj.addword(word, tf, idx);
                    MyMapDoc.put(docnum, docobj);
                }

            }

        }
        countidfsquery(idfsquery,SearchWords,idfs,doc);
        //System.out.println(pagerank.size());
    }


    public static void GetMyMap_Phrase() {
       // System.out.println("phrase");
        Iterator<Map.Entry<Integer, Doc>> i = MyMapDoc.entrySet().iterator();
        int doc_num = 0;
        while (i.hasNext()) {
            doc_num = doc_num + 1;
            int count = 0;
            Map.Entry<Integer, Doc> pair = i.next();
            int size = pair.getValue().Words.size();  //size of words fel element el wa7d
            //System.out.println("the doc num "+doc_num);
            if (size == SearchWords.size()) {
                for (int j = 0; j < size - 1; j++) //size da 3dd l arrays (y3ne 3dd l klmat bm3na asa7)
                {
                    if (j == count) {
                        int size1 = pair.getValue().idx.get(j).size();
                        int size2 = pair.getValue().idx.get(j + 1).size();
                        int n1 = 0, n2 = 0;
                        int element1 = 0;
                        int element2 = 0;

                        while (n1 < size1 && n2 < size2) {
                            element1 = pair.getValue().idx.get(j).get(n1);
                            element2 = pair.getValue().idx.get(j + 1).get(n2);
                            if (element1  + 1 == element2) {
                                count = count + 1;
                                break;
                            } else if (element1 +  1 > element2)
                                n2++;
                            else if (element1 + 1 < element2)
                                n1++;

                        }

                    } else
                        break;
                }

                if (count + 1 != SearchWords.size())
                    i.remove();

            } else
                i.remove();


        }
    }

    public static void rank(List<Integer> onlyIDs,HashMap<String,Double> idfs, List <Double> pagerank, LinkedHashMap<Integer,Doc> MyMapDoc ,
                                      LinkedHashMap<String,Integer> SearchWords,
                                      HashMap<String,Double> idfsquery, LinkedHashMap<String,Integer >  TfWordsinQuery,
                                      int cntOfwords){
        List<Pair<Integer,Double>>docs=new ArrayList<Pair<Integer,Double>>();
        Iterator<Map.Entry<String,Integer>> iterator =SearchWords.entrySet().iterator();
        double query=0;
        while (iterator.hasNext()) {
            String wordnow=iterator.next().getKey();
            double idfnow=0.0;
            if(idfs.containsKey(wordnow))
                idfnow=idfs.get(wordnow);
            idfnow+=idfsquery.get(wordnow);
            query+=Math.pow(idfnow*SearchWords.get(wordnow)/cntOfwords,2 );//lsa hadrb fi addad
        }
        Iterator<Map.Entry<Integer, Doc>> it = MyMapDoc.entrySet().iterator();
        query=Math.sqrt(query);
        int idofdoc;
        Iterator<Map.Entry<String, Integer>> it2 = TfWordsinQuery.entrySet().iterator();
   /* while (it2.hasNext()){
        System.out.println("map "+it2.next().getKey());
    }*/
        while(it.hasNext()) {
            double doc=0;
            Map.Entry<Integer, Doc> pair = it.next();
            idofdoc=pair.getKey();
            double dot_product=0;
            List<String>words=pair.getValue().Words;
            List<Double>tf=pair.getValue().tf;
            for(int i=0;i<words.size();++i){
                String wordnow=words.get(i);
                double w=1;
                if(SearchWords.containsKey(wordnow))
                    w=1.5;
                double idfnow=idfs.get(wordnow);
                //System.out.println("wordnow "+wordnow);

                double tf_idf_query=idfnow*TfWordsinQuery.get(wordnow)/cntOfwords;
                double tf_idf_fordoc=idfnow*tf.get(i)*w;
                dot_product+=tf_idf_query*tf_idf_fordoc;
                doc+=Math.pow(tf_idf_fordoc,2);
            }
            doc=Math.sqrt(doc);
            if(query==0||doc==0)
                docs.add(new Pair <Integer,Double>(idofdoc,0.0));
            docs.add(new Pair <Integer,Double>(idofdoc,dot_product/(query*doc)*.8+pagerank.get(idofdoc)*.2));
        }
        docs.sort(new Comparator<Pair<Integer,Double>>() {
            @Override
            public int compare(Pair< Integer,Double> o1, Pair< Integer,Double> o2) {
                if (o1.getValue() > o2.getValue()) {
                    return -1;
                } else if (o1.getValue().equals(o2.getValue())) {
                    return 0; // You can change this to make it then look at the
                    //words alphabetical order
                } else {
                    return 1;
                }
            }
        });
        for(int i=0;i<docs.size();++i)
            onlyIDs.add(docs.get(i).getKey());
       // System.out.println(onlyIDs.size());
        return;
    }





}

class Doc {
    public List<String> Words = new ArrayList<>();
    public List<Double> tf = new ArrayList<>();
    public List<List<Integer>> idx = new ArrayList<>();

    public Doc(String word, double tf, List<Integer> idx) {
        this.Words.add(word);
        this.tf.add(tf);
        this.idx.add(idx);

    }

    void addword(String word, double t, List<Integer> id) {
        this.Words.add(word);
        this.tf.add(t);
        this.idx.add(id);
        int self = new ArrayList<>(SearchWords.keySet()).indexOf(word);
        int cur_self = Words.size() - 1;
        for (int i = Words.size() - 2; i >= 0; i--) {
            int other = new ArrayList<>(SearchWords.keySet()).indexOf(Words.get(i));
            if (other > self) {
                Collections.swap(Words, cur_self, i);
                Collections.swap(tf, cur_self, i);
                Collections.swap(idx, cur_self, i);
                cur_self = i;
            }
        }
    }

}




