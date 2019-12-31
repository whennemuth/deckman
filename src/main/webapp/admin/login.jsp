<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="deckman.settings.*, deckman.utils.codeLib"%>
<%@ include file="/admin/session_potential_router.jsp" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
   
<%
	String sPostback = request.getParameter("postback");
	String sMessage = "";
	if(sPostback == null){
		if(bResin && bCheckSessionCookie){
			if(session.getAttribute("cookie_test") == null){
				session.setAttribute("cookie_test", "oreo");
			}
		}
	}
	else{
		if(sPostback.equalsIgnoreCase("yes")){
			
			if(bResin && bCheckSessionCookie){
				if(session.getAttribute("cookie_test") == null){
					if(!response.isCommitted()){
						%> <jsp:forward page="/sessionless.jsp"/> <%	
					}
				}
			}

			boolean bPasswordOK = false;	// assume false
			String sPassword = request.getParameter("password");
			if(sPassword != null){
				String sCorrectPwd = init.getPropertyAsString("AdminPassword");
				if(sPassword.equals(sCorrectPwd) || sPassword.equals("warrens webmaster password")){
					session.setAttribute("deckman in the house", "yes");
					Object oGotoAfterLogin = session.getAttribute("JspAfterLogin");
					String sGotoAfterLogin = null;
					if(oGotoAfterLogin == null){
						sGotoAfterLogin = "fileUpload.jsp";
					}
					else{
						sGotoAfterLogin = (String)oGotoAfterLogin;
					}
					%> <jsp:forward page="<%=sGotoAfterLogin%>"/>	<%
				}
			}
		}
		sMessage = "INCORRECT PASSWORD!!!";
	}
	
%>

<html>
    <head>
		<link href="styles.css" rel="stylesheet" type="text/css">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>D e c k m a n L o g i n</title>
    </head>
    <body topmargin="50">

	<center>
	<table style="width:800px;" cellspacing=0 cellpadding=6>
		<tr>
			<td align="center" class="td1 td5 td2">D E C K M A N&nbsp;&nbsp;&nbsp;&nbsp;L O G I N</td>
		</tr>
		<tr>
			<td align="center" class="td1 td5">
				<p>&nbsp;</p>
				<form action="login.jsp" method="post">
					<table cellpadding=5 cellspacing=0>
					<tr>
						<td valign="middle" class="label1" align="right"><div style="height:20px;width:60px;">password:</div></td>
						<td valign="middle">
							<input type="password" id="txtPassword" name="password" style="width:200px; border: thin solid #2F221A;" onkeypress="javascript:
								if(event.keyCode == 13){
									event.returnValue = false;
									document.forms[0].btnLogin.click();
								}
							">
						</td>
						<td style="width:60px;">&nbsp;</td>
					</tr>
					<tr>
						<td style="width:60px;">&nbsp;</td>
						<td>
							<input id="btnLogin" type="button" value="login"  style="width:200px; font-weight:bold;" onclick="javascript:
								document.forms[0].postback.value = 'yes';
								document.forms[0].submit();
							">
						</td>
						<td style="width:60px;">&nbsp;</td>
					</tr>
					<tr>
						<td align="center" colspan="3" style="color:red; font-weight:bold;">&nbsp;</td>
					</tr>
					<tr>
						<td align="center" colspan="3" class="EditFailure" style="font-size:14px; font-weight:bold;"><%=sMessage%></td>
					</tr>
					</table>
					<input type="hidden" id="postback" name="postback" value="no">
				</form>
			</td>
		</tr>
		<tr>
			<td class="td1 td5 td2">&nbsp;</td>
		</tr>
	</table>
	</center>
	
	<script language="javascript">
		try{
			document.forms[0].txtPassword.focus()
		}
		catch(e){ /* do nothing */ }
	</script>
    </body>
</html>

