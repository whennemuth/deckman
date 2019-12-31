<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="java.util.*, java.io.*, deckman.settings.*, deckman.utils.*, com.warren.logging.*, com.warren.logging.logs.* " %>
<%@ include file="/admin/session_potential_router.jsp" %>

<%!
	public void printPropertyFileContents(JspWriter out, HttpServletRequest request, HttpSession session){
		
		HashMap map = null;
		Iterator iter = null;
		try{
			out.println("<p><b>contents of property file</b></p><p>");
			String sPropFileName = request.getParameter("propertyfilename");
			if(sPropFileName == null){
				return;
			}
			sPropFileName = (String)sPropFileName;
            initialization init = initialization.getInstance(sPropFileName, session);
			map = init.getStringMap();
			iter = map.keySet().iterator();
		}
		catch(Exception e){
			try{
				out.print("<br><pre>");
				out.print(codeLib.getStackTraceAsString(e));
				out.print("</pre>");
			}
			catch(IOException IOEx1){ }
			return;
		}
			
		int i = 0;

		try{
			out.println("<table cellpadding=2 cellspacing=0>");
			while(iter.hasNext()){
				i++;
				Object o = iter.next();
				Object oVal = map.get(o);

				out.print("<tr>");
				out.print("<tr><td class='td'>&nbsp;&nbsp;&nbsp;&nbsp;</td>");
				out.print("<td" + (iter.hasNext()?"":" class='bottomTD'") + ">");
				out.print((String.valueOf(i) + ")&nbsp;&nbsp;") + o);
				out.print("</td>");
				out.print("<td" + (iter.hasNext()?"":" class='bottomTD'") + ">");
				out.print(oVal);
				out.print("</td>");
				out.println("</tr>");
			}
			out.println("</table></p>");
		}
		catch(IOException IOEx2){
			try{
				out.print("<br><pre>");
				out.print(codeLib.getStackTraceAsString(IOEx2));
				out.print("</pre>");
			}
			catch(IOException IOEx3){ }
		}
	}
	
	public void printEnvironment(JspWriter out){
		try{
			out.println("<b>System.getenv():</b><p>");
			Map map = System.getenv();
			Set keyset = map.keySet();
			Iterator iter = keyset.iterator();
			out.println("<table cellpadding=2 cellspacing=0>");
			while(iter.hasNext()){
				String sKey = (String)iter.next();
				out.println("<tr><td class='td'>&nbsp;&nbsp;&nbsp;&nbsp;</td>");
				out.println("<td" + (iter.hasNext()?"":" class='bottomTD'") + ">" + sKey + "</td>");
				out.println("<td" + (iter.hasNext()?"":" class='bottomTD'") + ">" + (map.get(sKey)) + "</td>");
				out.println("</tr>");
			}
			out.println("</table></p>");
			
		}
		catch(Exception e){
			try{
				out.print("<br><pre>");
				out.print(codeLib.getStackTraceAsString(e));
				out.print("</pre>");
			}
			catch(IOException IOEx){ }
		}
	}
	
	public void printApplicationVars(JspWriter out, ServletContext application){
		Iterator iter = null;
		try{
			out.println("<p><b>application variables:</b><br>");
			out.println("application.getMajorVersion() = " + String.valueOf(application.getMajorVersion()) + "<br>");
			out.println("application.getMinorVersion() = " + String.valueOf(application.getMinorVersion()) + "<br>");
			out.println("application.getRealPath(\"\") = " + application.getRealPath("") + "<br>");
			out.println("application.getServerInfo() = " + application.getServerInfo() + "<br>");
			out.println("application.getResourcePaths():<br><ol>");
			Set set = application.getResourcePaths("/");
			iter = set.iterator();
			while(iter.hasNext()){
				out.println("<li>" + ((String)iter.next()) + "</li>");
			}
			out.println("</ol></p>");
			//out.println("images dir up one folder from app path = " + sAppPath.substring(0, sAppPath.lastIndexOf(File.separator)+1) + "images<br>");				
		}
		catch(Exception e){
			try{
				out.print("<br><pre>");
				out.print(codeLib.getStackTraceAsString(e));
				out.print("</pre>");
			}
			catch(IOException IOEx){ }
		}
	}

    public void writeToLogFile(HttpServletRequest request, JspWriter out) throws IOException {
        String sLogPathName = request.getParameter("LogPathName");
        String sLogEntry = request.getParameter("LogEntry");

        if(StaticLog.isSet()){
            System.out.println("static log was set");
            StaticLog.log(sLogEntry, LogEntry.EntryType.INFO);
        }
        else{
            System.out.println("static log was not set");
            out.println("static log was not set<br>");
            PeriodLog plog = null;
            try{
                plog = new PeriodLog(sLogPathName, PeriodLog.period.MONTHLY);
            }
            catch(FileNotFoundException e){
                out.println(deckman.utils.codeLib.getStackTraceAsString(e));
            }
            StaticLog.setLog(plog);
            StaticLog.log(sLogEntry, LogEntry.EntryType.INFO);
        }
    }
%>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Environment</title>
		<style>
			td{
				border-top-style:solid;
				border-top-color:gray;
				border-top-width:1px;
				border-left-style:solid;
				border-left-color:gray;
				border-left-width:1px;
			}
			
			.td{
				border-top-style:none;
				border-left-style:none;
				border-bottom-style:none;
			}
			
			.bottomTD{
				border-bottom-style:solid;
				border-bottom-color:gray;
				border-bottom-width:1px;			
			}
			
			table{
				border-right-style:solid;
				border-right-color:gray;
				border-right-width:1px;
				font-family:verdana;
				font-size:10px;
			}
		</style
		<link href="styles.css" rel="stylesheet" type="text/css">
    </head>
    <h1>Environment</h1>
    <body style='font-family:verdana; font-size:10px;'>	

	<jsp:include page="banner.jsp">
		<jsp:param name="banner_selection" value="none" />
	</jsp:include>
	
	<br>
	<%
        printEnvironment(out);
        boolean bIsPOST = request.getMethod().equalsIgnoreCase("POST");
        String sSubmitter = null;

        String sUserAgent = request.getHeader("User-Agent");
        out.println("<br>Server-based browser detection: " + sUserAgent + "<br>");
    %>
	
    <script language="javascript">
        document.write("<br>Client-based browser detection: " + navigator.userAgent + "<br>");
    </script>

	<form method=post action="environment.jsp">
		Name of property file (with or without full path information)<br>
		<input type="text" name="propertyfilename" value="<%=initialization.DEFAULT_PROP_FILE_NAME%>" style="width:600px; font-family:monospace; font-size:12px;">
		<br><br>
		<input type="submit" value="Get Properties" name="GetPropFile">
		<%
			if(bIsPOST){
                sSubmitter = request.getParameter("GetPropFile");
                if(sSubmitter != null){
                    if(sSubmitter.equals("Get Properties")){
                        printPropertyFileContents(out, request, session);
                    }
                }
			}
		%>

        <br><br>
        <br><br>
        <br><br>



		Specify a log file path name:<br>
		<input type="text" name="LogPathName" value="<%=(init.getPropertyAsString("AdminLogPathName"))%>" style="width:600px; font-family:monospace; font-size:12px;">
		<br><br>
        Specify an entry to make to the log file:<br>
        <input type="text" name="LogEntry" style="width:600px; font-family:monospace; font-size:12px;" value="default entry">
		<br><br>
		<input type="submit" value="Make Log Entry" name="WriteLogEntry">
        <%
            if(bIsPOST){
                sSubmitter = request.getParameter("WriteLogEntry");
                 if(sSubmitter != null){
                    if(sSubmitter.equals("Make Log Entry")){
                        writeToLogFile(request, out);
                    }
                }
            }
        %>
	</form>
    
    </body>
</html>
