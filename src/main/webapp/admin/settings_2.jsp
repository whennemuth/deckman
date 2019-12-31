<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="deckman.settings.*, java.util.*, java.util.regex.*"%>
<%@ include file="/admin/session_potential_router.jsp" %>

<%
	String sReferer = request.getHeader("referer");
	
	if(!sReferer.endsWith("settings_1.jsp") && !sReferer.endsWith("login.jsp")){
			 /* only settings_1.jsp can direct to this page, so "settings_1.jsp is an exceptable referer.
			    However, if "login.jsp", shows up as the referer, this does not mean that "settings_1.jsp"
				wasn't the immediately prior page - it just means that processing of the initial http 
				request was started at login.jsp and was transferred to settings_1.jsp through a
				<jsp:forward> action */
		response.sendRedirect("settings_1.jsp");
	}
	else{
		boolean bIsPost = request.getMethod().equalsIgnoreCase("POST");
		if(!bIsPost){
				 // only settings_1.jsp can direct to this page AND via a post (not a get).
			response.sendRedirect("settings_1.jsp");
		}
		else{
				// open the property file specified by the refering page (javascript validation will prevent a null value from coming through)
			String sPropFileName = (String)request.getParameter("propertyfilename");
			reinitialization reinit = (reinitialization)init;
			
			if(!init.failedInitialization()){
					// initialization is ok. Proceed to the 3rd settings page.
				%>
				<jsp:forward page="settings_3.jsp"/>
				<%
			}
			else{
					// there was a problem. Remain here and display the error details
%>
				<html>
					<head>
						<META HTTP-EQUIV="PRAGMA" CONTENT="NO-CACHE">
						<title>Deckman site settings</title>
						<link href="styles.css" rel="stylesheet" type="text/css">
					</head>

					<body>
					<center>

						<jsp:include page="banner.jsp">
							<jsp:param name="banner_selection" value="settings_1" />
						</jsp:include>

						<table border="0" cellpadding="6" width="800px">
							<tr>
								<td>
									<div class='td1 td5' style='padding:6px; font-size:12px;'>
										<% out.print(init.getInitializationProblem()); %>
									</div>
								</td>
							</tr>
							<tr> 
								<td colspan="2" align="center" bgcolor="#AE9E8F" class="td1 td5" style="background-color:#AE9E8F">
									<input type="button" class="button1" style="width:200px; cursor: hand;" id="txtSubmitEdit" value="Return to prior page" onclick="javascript: document.location.href = 'settings_1.jsp';">
								</td>
							</tr>
							<tr><td height="30px">&nbsp;</td></tr>
						</table>

					</center>
				   </body>
				</html>

<%
			}		
		}		
	}
%>
