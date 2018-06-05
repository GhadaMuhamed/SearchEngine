package front;
import QueryProcessor.PorterAlgo;
import QueryProcessor.Search;
import QueryProcessor.rankread;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jsoup.Jsoup;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

@WebServlet("/back")
public class back extends javax.servlet.http.HttpServlet {
    public MongoCollection collection=null;
    public MongoCollection collection2=null;
    public MongoCollection collection3=null;
    public MongoCollection CollectionInvDoc;
    public static final long serialVersionUID = 1L;
    public static ArrayList<BasicDBObject> ObjsQuery = new ArrayList<>();
    public static PorterAlgo pa = new PorterAlgo();
    public static HashSet<String> StopWords;
    public static List<Double> pagerank ;
    public static LinkedHashMap<String, Integer> SearchWords;
    public static LinkedHashSet<String> SearchStem;
    public static boolean phrase;
    public static int cntwords;
    String search;
    org.jsoup.nodes.Document htmlDocument = null;
    public back() {
        super();
        cntwords=0;
        StopWords = new HashSet<>();
        SearchWords = new LinkedHashMap<>();
        SearchStem = new LinkedHashSet<>();
        MongoClient mongoClient = new MongoClient( "localhost" , 27017 );  //create mongo client
        MongoDatabase db = mongoClient.getDatabase("searchEngine"); //accessing database
        collection = db.getCollection("Search");
        collection2 = db.getCollection("Find");
        collection3 = db.getCollection("Visit");
        CollectionInvDoc = db.getCollection("invertedDocument");
        pagerank = new ArrayList<>();
        ReadStopWords();
        PorterAlgo pa=new PorterAlgo();
        Timer timer = new Timer();
        // scheduling the task at interval
        rankread pagesrank = new rankread(pagerank);
        timer.schedule(pagesrank, 0, 8400000);
        System.out.println("init is heree ");
    }
    public void clearAll(){

        SearchWords.clear();
        SearchStem.clear();
        phrase=false;
        cntwords=0;
        ObjsQuery.clear();
        System.out.println("clear");
    }
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        SearchStem.clear();
        System.out.println("awl el get");
        ArrayList<String> urls = new ArrayList<>();
        phrase=false;
        String tmp;
        tmp= request.getParameter("search");
        if(tmp!=null && !tmp.equals(search)){
            search=tmp;
            clearAll();
        }
        //System.out.println(search);
        cntwords= addHeader(search,pa);
        Iterator<Map.Entry<String, Integer>> iterate = SearchWords.entrySet().iterator();
        while (iterate.hasNext())
            SearchStem.add(pa.stripAffixes(iterate.next().getKey()));
        List<Integer> ArrayIDS = new ArrayList<>();
        Search search_obj = new Search(ArrayIDS,pagerank,CollectionInvDoc,SearchWords,SearchStem,cntwords,phrase);
        //System.out.println("array Ids "+ArrayIDS.size());
        if (search != null && !search.equals("")){
            Bson filter1 = new Document("number", 1);
            Bson newValue1 = new Document("snippts",search);
            Bson updateOperationDocument1 = new Document("$set", newValue1);
            collection2.updateOne(filter1, updateOperationDocument1);
        }

        int numberPer_Page=10;

        //ArrayIDS.add(3);
        if (ArrayIDS.size()==0)
        {

            request.getRequestDispatcher("nofound.jsp").forward(request, response);
            return;
        }
        for(int i=0;i<ArrayIDS.size();i++){
            BasicDBObject query = new BasicDBObject("Id", new BasicDBObject("$eq",ArrayIDS.get(i)));
            //System.out.println(query.toString());
            ObjsQuery.add(query);
        }
        BasicDBObject orQuery = new BasicDBObject();
        orQuery.put("$or", ObjsQuery);
        FindIterable<Document> doc3= collection3.find(orQuery);
        System.out.println(doc3.toString());
        if (doc3!=null)
            for (Document mydoc3 : doc3)
                urls.add((String) mydoc3.get("Url"));


        String[] Arraybody = new String[ArrayIDS.size()];
        String[] Arraytitle = new String[ArrayIDS.size()];
        String[] ArraybodyShow = new String[numberPer_Page];
        String[] ArraytitleShow = new String[numberPer_Page];
        //get tilte and body to evrey doc
        System.out.println("abl snippets");

        for (int i=0;i<ArrayIDS.size();i++) {
            File input = new File("C:\\Users\\ghada\\Documents\\old"+"\\"+ArrayIDS.get(i)+".html");
            try {
                htmlDocument = Jsoup.parse(input, "UTF-8", "");
            }
            catch (IOException e) {
                System.out.println("can't parse in index document : " + input.getName());
            }
            if (htmlDocument== null)
                System.out.println("Doc is null");
            String body = htmlDocument.body().text();
            String title = htmlDocument.title();
            //make snippts from doc body's
            BasicDBObject query1 = new BasicDBObject("number",new BasicDBObject("$eq",1));
            FindIterable<org.bson.Document> doc2= collection2.find(query1);
            String oldSearch="";
            for (Document mydoc2 : doc2) {
                oldSearch = (String) mydoc2.get("snippts");
            }
            //System.out.println(oldSearch);

            String Snippts="";

            String bodySnippts = body.toLowerCase();
            //String searchSnippts= oldSearch.toUpperCase();

            String[] partsBody = bodySnippts.split(" ");
            //System.out.println("hhh "+bodySnippts);
            ArrayList<Integer> indexs = new ArrayList<>();

            for (int it=0;it<partsBody.length;it++) {
                Iterator<String> itr = SearchStem.iterator();
                while (itr.hasNext()){
                    String str= itr.next();
                    //.toUpperCase();
                    // System.out.println(str +" "+ pa.stripAffixes(partsBody[it]));
                    //System.out.println(pa.stripAffixes(partsBody[it]));
                    if(pa.stripAffixes(partsBody[it]).equals(str)) {
                        boolean inrange=false;
                        for(int g=0;g<indexs.size();g++) {
                            if ( indexs.get(g)+6>=it && indexs.get(g)-6<=it ) {
                                inrange=true; break;
                            }
                        }
                        if(inrange) continue;
                        for (int loop=it-6;loop<=it+6;loop++) {
                            if(loop>=0  && loop <partsBody.length){
                                Snippts+=partsBody[loop];
                                Snippts+=" ";
                            }
                        }
                        //   System.out.println(Snippts);
                        Snippts+="..........";
                        Snippts+=" ";
                        if (Snippts.length()>300)
                            break;

                        indexs.add(it);
                    }

                }


                if (Snippts.length()>300)
                    break;
            }
            /// System.out.println(Snippts);
            Arraybody[i]=Snippts;
            Arraytitle[i]=title;

        }


        //check that user write somethig to search on it
        if (search != null && !search.equals("")) {
            int count=0;
            BasicDBObject projection = new BasicDBObject();
            projection.put("word",search);
            FindIterable<Document> iterDoc = collection.find(projection).projection(projection);
            Iterator<Document> iterator = iterDoc.iterator();
            if (iterator.hasNext())
                count=1;


            //check if the word aly be search 3leha mwgoda abl kda wla l2
            // if yes update number of search and if no insert it in DB
            if (count==0) {
                Document doc = new Document("word",search).append("num", 1);
                collection.insertOne(doc);
            }
            else {
                BasicDBObject query = new BasicDBObject("word",new BasicDBObject("$eq",search));
                FindIterable<org.bson.Document> doc= collection.find(query);
                for (Document mydoc : doc) {
                    int num = (int) mydoc.get("num");
                    Bson filter = new Document("word", search);
                    Bson newValue = new Document("num", num+1);
                    Bson updateOperationDocument = new Document("$set", newValue);
                    collection.updateOne(filter, updateOperationDocument);
                    break;
                }
            }
        }
        //get number of pages to show it
        int c=0;
        int pages=0;

        String[] show = new String[numberPer_Page];
        if (urls.size()% numberPer_Page!=0)
            pages=(urls.size()/ numberPer_Page)+1;
        else
            pages=(urls.size()/ numberPer_Page);
        //initial first page
        if (numberPer_Page <=urls.size())
        {
            for (int i=0;i<= numberPer_Page-1;i++)
            {
                show[c]=urls.get(i);
                ArraybodyShow[c]=Arraybody[i];
                ArraytitleShow[c]=Arraytitle[i];
                c++;
            }
        }
        else {
            for (int i=0;i<urls.size();i++)
            {
                show[c]=urls.get(i);
                ArraybodyShow[c]=Arraybody[i];
                ArraytitleShow[c]=Arraytitle[i];
                c++;
            }
            for (int i=urls.size();i<numberPer_Page;i++)
            {
                show[c]="";
                ArraybodyShow[c]="";
                ArraytitleShow[c]="";
                c++;
            }


        }
        // when user chane the search page
        String number_page= request.getParameter("page");
        if (number_page != null && !number_page.equals("")) {
            int numPage=Integer.parseInt( number_page);

            int come=numPage;
            int end=come* numberPer_Page ;
            int start=end- numberPer_Page;
            int counter=0;
            if (end <=urls.size())
            {
                for (int i=start;i<end;i++)
                {
                    show[counter]=urls.get(i);
                    ArraybodyShow[counter]=Arraybody[i];
                    ArraytitleShow[counter]=Arraytitle[i];
                    counter++;
                }
            }
            else{
                for (int i=start;i<urls.size();i++)
                {
                    show[counter]=urls.get(i);
                    ArraybodyShow[counter]=Arraybody[i];
                    ArraytitleShow[counter]=Arraytitle[i];
                    counter++;
                }
                for (int i=urls.size();i<end;i++)
                {
                    show[counter]="";
                    ArraybodyShow[counter]="";
                    ArraytitleShow[counter]="";
                    counter++;
                }

            }
        }
        //send thea arrays to show it in page
        request.setAttribute("pages", pages);
        request.setAttribute("show", show);
        request.setAttribute("ArraybodyShow",ArraybodyShow);
        request.setAttribute("ArraytitleShow",ArraytitleShow);
        if (ArrayIDS.size()==0)
        {
            System.out.println("finished");

            request.getRequestDispatcher("nofound.jsp").forward(request, response);
        }
        else
        {
            System.out.println("finished");

            request.getRequestDispatcher("result.jsp").forward(request, response);

        }
    }





    public static void ReadStopWords() {
        File file = new File("stopWords.txt");
        FileReader fileReader;
        try {
            fileReader = new FileReader(file);
            BufferedReader br = new BufferedReader(fileReader);
            String stopWord;
            try {
                stopWord = br.readLine();
                while (stopWord != null) {
                    StopWords.add(stopWord);
                    stopWord = br.readLine();
                }
            } catch (IOException e) {
            }
            try {
                fileReader.close();
            } catch (IOException e) {
            }
        } catch (FileNotFoundException x) {
            System.out.println("file not found");
        }
    }

    public static int addHeader(String s, PorterAlgo pa) {
        if (s==null || s.equals("")) return 0;
        int cntofwords = 0;
        String tmp = "";
        s = s.toLowerCase();
        s = s.trim();
        if (s.charAt(s.length() - 1) == '"' && s.charAt(0) == '"')
            phrase = true;

        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            if (c == ',' || c == '"' || c == '+' || c == '-' || c == '}' ||
                    c == '/' || c == '*' || c == '(' || c == ')' || c == '{' ||
                    c == '!' || c == ':' || c == '?' || c == '[' || c == ']' ||
                    c == '&' || c == '@' || c == ' ') {
                if (tmp.equals("") || isStopWord(tmp) || isStopWord(pa.Clean(tmp))) {
                    tmp = "";
                    continue;
                }
                if (!SearchWords.containsKey(tmp)) {

                    SearchWords.put(tmp, 1);
                } else {
                    int newvalue = SearchWords.get(tmp) + 1;
                    SearchWords.put(tmp, newvalue);
                }
                cntofwords++;
                tmp = "";
            } else tmp += c;
        }
        if (!tmp.equals("") && !isStopWord(tmp) && !isStopWord(pa.Clean(tmp))) {
            if (!SearchWords.containsKey(tmp)) {

                SearchWords.put(tmp, 1);
            } else {
                int newvalue = SearchWords.get(tmp) + 1;
                SearchWords.put(tmp, newvalue);

            }

            cntofwords++;
        }
        return cntofwords;
    }


    public static boolean isStopWord(String s){
        if(StopWords.contains(s))
            return true;
        return false;
    }



}
