<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="java.io.*"%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>

    <h1>JSP Page</h1>
<%
    PrintWriter pw = response.getWriter();
    for ( int i = 6; --i > 0; ) {
      out.println( "This is line" + i + "<br>"); 
      out.flush(); 
      response.flushBuffer(); 
      try { Thread.sleep( 2000 ); } catch( Exception e ) {} 
    } 
    //out.close(); 
%>
    </body>
</html>
