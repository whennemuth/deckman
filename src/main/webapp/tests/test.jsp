<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>

<%@page import="java.util.*, java.io.*, deckman.utils.*, javax.servlet.http.*, javax.servlet.jsp.*" %> 

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
<%!
	public void writeFile(HttpServletRequest request, JspWriter out) throws java.io.IOException {
		String sDir = request.getParameter("dir");
		String sFilename = request.getParameter("filename");
		String sContent = request.getParameter("content");

		if(!sDir.endsWith(File.separator)) sDir += File.separator;
		if(sFilename.startsWith(File.separator)) sFilename = sFilename.substring(1);
		String sPath = sDir + sFilename;

		File f = new File(sPath);
		boolean bDoWrite = false;
		if(f.exists()){
			if(f.isDirectory()){
				out.write("The specified file\r\n\r\n\t[" + sPath + "]\r\n\r\nAlready exists as a directory.");
			}
			else{
				out.write("The specified file\r\n\r\n\t[" + sPath + "]\r\n\r\nAlready exists.\r\n\r\nOverwriting...\r\n\r\n");
				bDoWrite = true;
			}
		}
		else{
			bDoWrite = true;
		}
		
		if(bDoWrite){
			try {
				BufferedWriter bwr = new BufferedWriter(new FileWriter(sPath));
				bwr.write(sContent);
				bwr.close();
				out.write("File successfully created:\r\n\r\n[" + sPath + "]");			
			}
			catch (IOException e){
				out.print(codeLib.getStackTraceAsString(e));
				return;
			}			
		}
	}
%>

<%
	String sDir = File.separator;
	String sFilename = "myfile.txt";
	String sContent = "Write some content here";
	
	if(request.getMethod().equalsIgnoreCase("POST")){
		sDir = request.getParameter("dir");
		sFilename = request.getParameter("filename");
		sContent = request.getParameter("content");		
	}
%>
<html>
<head>
<title>Write file test</title>
</head>

<body leftmargin="50" topmargin="50">
<center>
<form action="test.jsp" method="post">
<div style="border-style:solid; border-width:1px; border-color: #003399; width:600px; background-color:beige;">
 <table cellpadding=10 cellspacing=0 style="background-color:beige; color:#003399; font-family:verdana; font-size:12px;">
 <tr>
 	<td colspan=2 align="center">WRITE A FILE TO ANY LOCATION RELATIVE TO THE ROOT<br>
	<span style="font-size:10px;">(application root is: <%=application.getRealPath("")%>)</span><br></td>
 </tr>	
 <tr>
 <td>Directory</td>
 <td>
 	<input type="text" id="txtDir" name="dir" style="width:300px; font-family:monospace;" value="<%=sDir%>">
 </td>
 </tr>	
 <tr>
 <td>File Name</td>
 <td>
 	<input type="text" id="txtFilename" name="filename" style="width:300px; font-family:monospace;" value="<%=sFilename%>">
 </td>
 </tr>	
 <tr>
 <td>Some content</td>
 <td>
 	<textarea cols=50 rows=3 id="txtContent" name="content" style="width:300px; font-family:monospace;"><%=sContent%></textarea>
 </td>
 </tr>	
 <tr>
 <td colspan=2 align="center">
 	<input type="button" value="     Create File     " onclick="javascript:
		document.forms[0].submit();
	">
 </td>
 </tr>	
 </table>
</div>
<br>
<br>
<div align="left" style="font-size:11px; border-style:solid; border-width:1px; border-color: #003399; width:600px; background-color:beige; padding-left:10px; padding-top:10px;">
	<pre><%
		if(request.getMethod().equalsIgnoreCase("POST")){
			writeFile(request, out);
		}
		else{
			out.write("results here");
		}
	%></pre>
</div>
</form>
</center>
</body>
</html>

