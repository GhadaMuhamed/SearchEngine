<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" 
"http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>	
</head>
<body>
 <form id="myform" action="back" method="GET">

 <%

 String[] show =(String [])request.getAttribute("show");
 String[] ArraybodyShow =(String [])request.getAttribute("ArraybodyShow");
 String[] ArraytitleShow =(String [])request.getAttribute("ArraytitleShow");
 String print;
 int num=show.length;
 for(int j=0;j<num;j++)
 {
   %>
   <div style=" width: 650px; margin:30px;" >
    <a href="<%=show[j]%>" style="font-size:25px; text-decoration: none;">  <%=ArraytitleShow[j]%></a>
    <p style="color:green; font-size:20px;margin: 0; padding: 0;" >  <%=show[j]%></p>
    <p style="color:black;  font-size:15px;margin: 0; padding: 0;">  <%=ArraybodyShow[j]%></p>
     </div>

     <br>     
  <% }%>

 
 
 
 <%
 int pages=(Integer)request.getAttribute("pages");

 for(int i=1;i<=pages;i++)
 { String s = Integer.toString(i);
  System.out.println(pages);
   %>
   <INPUT  type="submit" name="page" value="<%=s%>">
 
  <% }%>

 </form>
</body>

</html>