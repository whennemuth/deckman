<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="deckman.settings.*, deckman.images.unzip.*, java.io.*" %>
<%@ include file="/admin/session_potential_router.jsp" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<%
    boolean bIsPost = request.getMethod().equalsIgnoreCase("POST");
    boolean bIsPostBack = false;
	result[] results = null;
	admin_unzip codeBehind = null;
	
    if(bIsPost){
        String sTask = (String)request.getParameter("task");
        if(sTask != null) bIsPostBack = true;
        if(bIsPostBack){
			codeBehind = new admin_unzip();
            results = codeBehind.doAction(request, sTask, init);
        }
   }
   
%>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>unzip</title>
		<link href="styles.css" rel="stylesheet" type="text/css">
    </head>
    <body>
        <center>
            <jsp:include page="banner.jsp">
                <jsp:param name="banner_selection" value="unzip" />
            </jsp:include>
            <form action="unzip.jsp" method="post">
                <table cellpadding=6 cellspacing=0 width="800px">
                    <tr>
                        <td align="left" colspan="2" class="td1 td5 td2">FILES TO UNZIP</td>
                    </tr>
                    <%
                    String sZipDir = init.getPropertyAsString("UnzipInFilePath");
                    File zipDir = new File(sZipDir);
                    File[] files = zipDir.listFiles(unzipper.getZipFileFilter());
                    if(files.length == 0){
                        out.println("<tr><td class='td1 td5'>No zip files</td></tr>");
                    }
                    else{
                        for(int i=0; i<files.length; i++){
                            %>
                            <tr>
                                <td align="left" class="td1 td5" valign="bottom">
                                    <input type="hidden" id="txtZipname<%=i%>" name="zipname<%=i%>" value="<%=files[i].getName()%>">
                                    <table cellpadding=3 class="td1">
                                        <tr>
                                            <td><input type="checkbox" id="chk<%=i%>" name="chk<%=i%>" style=""></td>
                                            <td align="right"><%=files[i].getName()%></td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
                            <%
                        }
						%>
						<tr>
							<td align="left" class="td1 td5" valign="bottom">
                                <input type="hidden" id="txtTask" name="task">
								<input type="button" id="btnUnzip" style="width:170px; cursor: hand;" value="unzip checked item(s)" onclick="javascript: doSubmit('unzip');">
								<br><br>
								<input type="button" id="btnDelete" style="width:170px; cursor: hand;" value="delete checked item(s)" onclick="javascript: doSubmit('delete');">
								
                                <script language="javascript">
									function doSubmit(sTask){
										var chk;
										var f = document.forms[0];
										var bChecked = false;
										for(var i=0; (chk = f['chk'+i]) != null; i++){
											bChecked |= chk.checked;
										}
										
										if(bChecked){
											f.txtTask.value = sTask;
											if(sTask == 'delete'){
												if(confirm('Are you sure you want to delete the checked item(s)?\r\n\r\nDeletions cannot be undone')){
													f.submit();
												}
											}
											else{
												f.submit();
											}
										}
										else{
											alert('No items have been checked');
										}
									}
								</script>
							</td>
						</tr>
						<%
                    }
                    %>
                </table>
            </form>
        </center>
<%
	if(results != null){
		boolean bDebug = init.getPropertyAsBoolean("DebugMode").booleanValue();
		codeBehind.displayUnzipResults(results, out, bDebug);
	}		
%>
    </body>
</html>
