<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
<%
		// Test at: http://warrenh.myip.org:8080/deckman/cookieTest.jsp
		// will not work if tested on localhost because the privacy policy is ignored since the browser is on the same machine as the web server.
    boolean bCookieSaved = false;
    String sQueryString = request.getQueryString();
    if(sQueryString == null){
        if(session.getAttribute("cookie_test") == null){
            session.setAttribute("cookie_test", "oreo");
            response.sendRedirect("cookieTest.jsp?CheckCookie");
        }
        else{
            bCookieSaved = true;
        }
    }
    else{
        if(sQueryString.equals("CheckCookie")){
            if(session.getAttribute("cookie_test") != null){
                bCookieSaved = true;
            }
        }
    }
	
%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Cookie Test</title>
    </head>
    <body leftmargin=70 topmargin=80>
        <div style="position:absolute; margin-top:100; margin-bottom:100; margin-left:20; margin-right:20; display:block; left:100; top:100; background-color:#B7B791; background-style:solid; height:200px; color:white; font-family:verdana; font-size:12;"> 
            <center>Your browser <%=(bCookieSaved ? "<font color='green'><u>DOES</u></font>" : "<font color='red'><u>DOES NOT</u></font>")%> support session cookies</center>
			<br>
			<table cellpadding=2 cellspacing=0>
				<tr>
					<td>session creation time:</td>
					<td><%=(new java.util.Date(session.getCreationTime())).toString()%></td>
				</tr>
				<tr>
					<td>session id:</td>
					<td><%=String.valueOf(session.getId())%></td>
				</tr>
				<tr>
					<td>session last accessed:</td>
					<td><%=(new java.util.Date(session.getLastAccessedTime())).toString()%></td>
				</tr>
				<tr>
					<td>session is new:</td>
					<td><%=String.valueOf(session.isNew())%></td>
				</tr>
			</table>
        </div>
    </body>
</html>
