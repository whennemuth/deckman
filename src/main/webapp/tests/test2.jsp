<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="java.util.*, deckman.settings.*"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body leftmargin=50 topmargin=50>
		This jsp shows that you can set a create a session variable, but never need to set it from that point on
		for modifications to show.<br>You need only use session.getAttribute to get the reference, and change a field
		of the resulting object.<br>You do not need to use session.setAttribute to have the modification saved.
	<form action="test2.jsp" method="post">
    <%	
		Object attrib1 = session.getAttribute("testattrib1");
		reinitialization reinit1 = null;
		String sLastvalue1 = null;
		
		Object attrib2 = session.getAttribute("testattrib2");
		reinitialization reinit2 = null;
		String sLastvalue2 = null;
		
		if(request.getMethod().equalsIgnoreCase("POST")){
				// get the reference to the session variable
			reinit1 = (reinitialization)attrib1;
			sLastvalue1 = (String)(reinit1.getPropertyAsString("DB_User"));
			
				// get a clone of what the session variable reference points to.
			reinit2 = (reinitialization)(((initialization)attrib2).deepClone());
			sLastvalue2 = (String)(reinit2.getPropertyAsString("DB_User"));
		}
		else{
				// first visit to page (not a POST)
			sLastvalue1 = "null";
			reinit1 = (reinitialization)initialization.getInstance();
			session.setAttribute("testattrib1", reinit1);	// attribute is set only once
			
			sLastvalue2 = "null";
			reinit2 = (reinitialization)initialization.getInstance();
			session.setAttribute("testattrib2", reinit2);	// attribute is set only once
		}
	%>
	<table cellpadding=10 cellspacing=0 border=1>
		<tr>
			<td colspan=2 align="center"><b>uncloned</b></td>
			<td colspan=2 align="center"><b>cloned</b></td>
		</tr>
		<tr>
			<td>two values ago</td>
			<td><%=sLastvalue1%></td>
			<td>two values ago</td>
			<td><%=sLastvalue2%></td>
		</tr>
		<%
		String sInput = request.getParameter("input");
		if(sInput == null){
				// get the default value
			sInput = reinit1.getPropertyAsString("DB_User"); // should be the same as reinit2.getPropertyAsString("DB_User");
		}
		reinit1.setProperty("DB_User", sInput);
		reinit2.setProperty("DB_User", sInput);
		%>
		<tr>
			<td>one value ago</td>
			<td><%=sInput%></td>
			<td>one value ago</td>
			<td><%=sInput%></td>
		</tr>		 
	</table>
	
	<br><br>
	input next value: <input type=text id=txtInput name=input>	
	<input type=submit value="submit">	
	
	</form>
    </body>
</html>
