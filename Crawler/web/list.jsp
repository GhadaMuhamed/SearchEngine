<%@page import="java.util.Iterator"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@page import ="org.bson.Document"%>
<%@page import=" org.bson.conversions.Bson"%>

<%@page import ="com.mongodb.BasicDBObject"%>
<%@page import ="com.mongodb.MongoClient"%>
<%@page import ="com.mongodb.client.FindIterable"%>
<%@page import ="com.mongodb.client.MongoCollection"%>
<%@page import ="com.mongodb.client.MongoDatabase"%>
<%
	 MongoCollection collection=null;
	 MongoClient mongoClient = new MongoClient( "localhost" , 27017 );  //create mongo client   
	    MongoDatabase db = mongoClient.getDatabase("searchEngine"); //accessing database
	     collection = db.getCollection("Search");
	     String query = (String)request.getParameter("q");
	     response.setHeader("Content-Type", "text/html");
	     
	   
	 FindIterable<Document> doc_sort = collection.find();
		doc_sort.sort(new BasicDBObject("num", -1));
		
	      // BasicDBObject qu= new BasicDBObject("num",new BasicDBObject("$eq", 1));
			//    FindIterable<org.bson.Document> doc= collection.find(qu);			   
	        int count=0;
	        for (Document mydoc : doc_sort) {			   
			   String word = (String) mydoc.get("word");
			   if(word.toUpperCase().startsWith(query.toUpperCase()))
	             	{
			          out.print(word+"\n");			
	             	}			   
			   }
	
%>