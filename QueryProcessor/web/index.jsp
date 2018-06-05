<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" 
"http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<link rel="stylesheet" type="text/css" href="css/style.css" />
	<script type="text/javascript" src="JS/jquery-1.4.2.min.js"></script>
	<script src="JS/jquery.autocomplete.js"></script>	
</head>
<body background="background.jpg" >
 <form id="myform" action="back" method="GET">
	<div style="width: 600px; margin: 50px auto;">
		 <input type="text" id="search" name="search" class="input_text" placeholder="click to search here"  required/>
		<button  type="submit" id="btn" class="bu"  >Go</button>
	</div>
	 </form>
</body>
<script>
	jQuery(function(){
		$("#search").autocomplete("list.jsp");
	});
</script>
</html>