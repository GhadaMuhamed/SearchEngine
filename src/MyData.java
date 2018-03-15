package crawler;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bson.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
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

class MyData 
{
	//String[] Seeds={"https://yts.am/"};
	
	 static MongoClientURI connectionString = null;
	    static MongoClient mongoClient = null;
	    static MongoDatabase db = null;

	    final LinkedHashSet<String> NotVisit= new LinkedHashSet();
		 final static LinkedHashSet<String>Visited=new LinkedHashSet();
		private static final int MAX_IT = 0;
		 final LinkedHashSet<String> Content=new LinkedHashSet<>();
		 final LinkedHashSet<String> Processing=new LinkedHashSet<>();
		 final List<Integer> InCounter=new ArrayList<>();	
				 

       
        //fetch url from nonvisited and check if it is in visited list or proccessing list
  public static void main(String[] args) throws IOException {
		
		        	
        	
        	
        	  connectionString = new MongoClientURI("mongodb://ghada:ghada@ds247347.mlab.com:47347/search_engine");
              mongoClient = new MongoClient(connectionString);
              db = mongoClient.getDatabase(connectionString.getDatabase());
              MongoCollection con = db.getCollection("Crawler");
              int id=0;
              FindIterable <Document> doc = con.find(gte("id", id));
             // while (doc.first() != null) {
                  for (Document myDoc : doc) {
                	  
                	  String url = myDoc.get("url").toString();
                	  Visited.add(url);
                	  System.out.println(".......... "+url);
                	  
                  }
                  doc = con.find(gte("id", id+1));
            //  }
        	
      
        //ReadFile(NotVisit,Visited,Content,InCounter);
        }
        
        
        
        public void ReadFile(LinkedHashSet NotVisit,LinkedHashSet Visited,LinkedHashSet Content,List InCounter) throws IOException
        {
            //read visited list
            System.out.println("ReadFile");
             String st;
            File visited = new File("C:\\Users\\menna\\OneDrive\\Documents\\NetBeansProjects\\Crawler\\src\\crawler\\visited.txt");
            
            try 
            {
                    BufferedReader  bv = new BufferedReader(new FileReader(visited));

                   while ((st = bv.readLine()) != null&&!st.equals(""))
                   {
                      System.out.println("ana areeet visited ****** "+st);
                      Visited.add(st);
                   }
            } 
            catch (FileNotFoundException ex) 
            {
                  visited.createNewFile();
            } 
           
            
            
            //read notvisited lis 
            File Notvisit = new File("C:\\Users\\menna\\OneDrive\\Documents\\NetBeansProjects\\Crawler\\src\\crawler\\notvisit.txt");
            try
            {
                BufferedReader bn = new BufferedReader(new FileReader(Notvisit));
                while ((st = bn.readLine()) != null&&!st.equals(""))
                {
                    System.out.println("ana areeet notvisit ****** "+st);
                   NotVisit.add(st);
                }
            }
            catch (FileNotFoundException ex) 
            {
               Notvisit.createNewFile();

            } 
           
            
            //read content list
            File content = new File("C:\\Users\\menna\\OneDrive\\Documents\\NetBeansProjects\\Crawler\\src\\crawler\\content.txt");
            try
            {    
                BufferedReader bc = new BufferedReader(new FileReader(content));
                while ((st = bc.readLine()) != null&&!st.equals(" "))
                {
                    System.out.println("ana areeet content ****** "+st);
                   Content.add(st);
                }
            }
            catch(FileNotFoundException ex)
            {
                content.createNewFile();
            } 
          
            //read Incounter list
            File Incounter = new File("C:\\Users\\menna\\OneDrive\\Documents\\NetBeansProjects\\Crawler\\src\\crawler\\incounter.txt");
           try
           {
                BufferedReader bi = new BufferedReader(new FileReader(Incounter));
                while ((st = bi.readLine()) != null&&!st.equals(""))
                {
                   InCounter.add(Integer.parseInt(st));
                }
           }
           catch(FileNotFoundException ex)
           {
                Incounter.createNewFile();

           } 
         
        
        }
        
  
        
        
        public  synchronized String fetch()
            {
                if(!GetNotVisit().isEmpty())
                {
                String URL;
                URL=this.NotVisit.iterator().next();
                this.NotVisit.remove(this.NotVisit.iterator().next());
                URL=processURL(URL);
                System.out.println("no visited "+GetNotVisit().size());
                if(!this.Visited.contains(URL)&&!this.Processing.contains(URL))
                {   this.Processing.add(URL);
                    return URL;
                }
                else if(this.Visited.contains(URL))
                {
                   int i = new ArrayList<>(this.Visited).indexOf(URL);
                   IncInCounter(i);
                }
                }
                return null;
            }
        
        
        public synchronized void InsertNotVisit(Elements l)
            {   int count =0;
            if(Visited.size()+NotVisit.size()<MAX_IT)
            { //System.out.println("insert notvisited from thread "+Thread.currentThread().getId());
                //System.out.println("****inseeeeeeeeeeeeeeeeeeeert*******************************");  
                for(Element link : l)
                    {
                        if(link.absUrl("href")!=""&&!this.Visited.contains(link.absUrl("href"))&&!this.NotVisit.contains(link.absUrl("href")))

                        //if(link.absUrl("href")!=""&&!this.Visited.contains(link.absUrl("href"))&&!this.NotVisit.contains(link.absUrl("href"))&&!this.HashCodes.contains(content.hashCode()))
                        { this.NotVisit.add(link.absUrl("href"));
                            count++;
                        }
                            
                    }
                System.out.println("the size of inserted pages "+ count);
               // System.out.println("size of notvisited "+this.NotVisit.size());
              //  System.out.println("size of visited "+this.Visited.size());
            }
            }
         
        
        public String processURL(String theURL) 
        {
            int endPos;
            if (theURL.indexOf("?") > 0) 
            {
                endPos = theURL.indexOf("?");
            } 
            else if (theURL.indexOf("#") > 0) 
            {
                 endPos = theURL.indexOf("#");
            } 
            else 
            {
                endPos = theURL.length();
            }
            return theURL.substring(0, endPos);
        }
        
        public synchronized boolean InsertVisit(String url,String content,String title)
            {//!this.ContentVisited.contains(content)&&
             //!this.Visited.contains(url)&&
                if(this.Content.add(content))
                {
                 System.out.println("the visited url "+url);
                 this.Visited.add(url);
                 this.InCounter.add(0);
                 System.out.println("size of content "+this.Content.size());
                 System.out.println("size of visited "+this.Visited.size());
                 System.out.println("size of counter "+this.InCounter.size());
                 return true;       
                }
                //else if(this.Visited.contains(url))
                //{
                //    System.out.println("*////////////////incereemeeeeeeeent*******************");
                    //int i = new ArrayList<>(this.Visited).indexOf(url);
                  //  IncInCounter(i);}

                   
                return false;
            }
        
        public synchronized LinkedHashSet GetContent()
        {
            return Content;
        }
         
        public synchronized LinkedHashSet GetVisited()
        {
            return Visited;
        }
       
        public synchronized LinkedHashSet GetNotVisit()
        {
            return NotVisit;
        }
         public synchronized void IncInCounter(int index)
        { 
           InCounter.set(index,(InCounter.get(index)+1));
        }
         public List<Integer> GetInCounter()
        { 
           return InCounter;
        }
         
         public int MAX_IT()
         {
             return MAX_IT;
         }
         
         
         
        
     
	
}