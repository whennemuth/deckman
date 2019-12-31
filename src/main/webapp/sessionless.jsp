<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<title>Untitled Document</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
</head>

<body>
<div style="height:30px; background-color:#003399; width:100%"></div>
<br><br><br>
<center>
	<div>
		<font face="Verdana, Arial, Helvetica, sans-serif">
		<b>
			Your browser doesn't seem to support session cookies.<br>
			Session cookies allow this site to retain information across its various web pages within a single visit.<br>
			These cookies do not remain after you leave the website.<br>
			Please enable session cookies in your browser, then click the button below. 
		</b>
		</font>
	</div>
	<br><br>
	
	<input type="button" value="Return to Website" onclick="javascript: retrySession();">
	<br><br><br><br>
	<div style="height:30px; background-color:#003399; width:100%"></div>
</center>


<script language="javascript">
	function retrySession(){
		document.location.href = "<%=request.getContextPath()%>/admin/fileUpload.jsp";
	}
</script>
</body>
</html>

