<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="deckman.settings.*, deckman.utils.*, java.util.*, java.util.regex.*"%>
<%@ include file="/admin/session_potential_router.jsp" %>

<%!
    
    private String doUpdate(ServletRequest request, reinitialization reinit, boolean bCommitToDisk) throws java.io.IOException {
        boolean bUpdatable = false;        
        Enumeration Enum = request.getParameterNames();
        StringBuffer s = new StringBuffer();
        while(Enum.hasMoreElements()){
            String sKey = (String)Enum.nextElement();
            String sVal = request.getParameter(sKey);
            String sPropVal = "";
            String sPropName = "";
            Matcher m = Pattern.compile("^((edit)|(remove))(\\d+)$").matcher(sKey);
            if(m.find()){
                if(sVal.equalsIgnoreCase("ON")){
                    String sTask = m.group(1);
                    String sIdx = m.group(4);
                    sPropVal = request.getParameter("prop"+sIdx);
                    sPropName = request.getParameter("propname"+sIdx);
                    if(sPropVal == null) sPropVal = "[--- BLANK ---]";
                    if(sPropName == null) sPropName = "[--- UNKNOWN NAME ---]";
                    if(sTask.equalsIgnoreCase("EDIT")){
                        reinit.setProperty(sPropName, sPropVal);
                        s.append("<B>Edit:</B> " + sPropName + " = " + sPropVal + "<br>");
                    }
                    else if(sTask.equalsIgnoreCase("REMOVE")){
                        reinit.removeProperty(sPropName);
                        s.append("<B>Removal:</B> " + sPropName + "<br>");
                    }
                }
            }
        }
		if(bCommitToDisk){
			doCommit(reinit, s);
		}
		else{
			s.insert(0, "<div class='td1 td5' align='left' style='padding:6; width:800px;'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<B><U>CHANGES APPLIED:</U></B><br><br>");
			s.append("</div>");
		}
		return s.toString();
    }
    
    private String doAdd(ServletRequest request, reinitialization reinit, boolean bCommitToDisk) throws java.io.IOException {
        StringBuffer s = new StringBuffer();
        String sPropVal = request.getParameter("newpropvalue");
        String sPropName = request.getParameter("newpropname");
        if(sPropVal == null) sPropVal = "[--- BLANK ---]";
        if(sPropName == null) sPropName = "[--- UNKNOWN NAME ---]";
        reinit.setProperty(sPropName, sPropVal);
        s.append("<B>Addition:</B> " + sPropName + " = " + sPropVal + "<br>");
		if(bCommitToDisk){
			doCommit(reinit, s);
		}
		else{
			s.insert(0, "<div class='td1 td5' align='left' style='padding:6; width:800px;'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<B><U>CHANGES APPLIED:</U></B><br><br>");
			s.append("</div>");
		}
		return s.toString();
    }
    
    private void doCommit(reinitialization reinit, StringBuffer s) throws java.io.IOException{
        try{
            reinit.commitToDisk();
			s.insert(0, "<div class='td1 td5' align='left' style='padding:6; width:800px;'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<B><U>CHANGES APPLIED:</U></B><br><br>");
			s.append("</div>");
        }
        catch(reinitialization.PropertyToDiskException e) {
			s.delete(0, s.length());
            s.append("<div class='td1 td5' align='left' style='padding:6; width:800px;'><B><U>ERROR COMMITING PROPERTY CHANGES TO DISK:</U></B><br><br>");
			s.append("<pre style='width:800px; font-size:11px;'>");
            //s.append(e.getMessage() + "<br><br>");
            s.append(codeLib.getStackTraceAsString(e));
            s.append("</pre>");
            s.append("</div>");
        }
    }
%>




<%
    boolean bIsPost = request.getMethod().equalsIgnoreCase("POST");
    boolean bIsValidSource = false;
	String sReferer = request.getHeader("referer");

	if(bIsPost){
		if(sReferer != null){
			if(sReferer.matches(".*/settings_[1-3].jsp$") || sReferer.endsWith("/login.jsp")){
				/* 
				   a) "/settings_1.jsp" indicates that /settings_1.jsp has posted to 
				   settings_2.jsp, who - provided initialization is successful - would have forwarded to this 
				   page (equivalent to an asp server.transfer).
				   
				   b) "/login.jsp" indicates the same thing as a) and does not mean that settings_1.jsp was
				   not the immediately prior page - it just means that the initial http request came from
				   login.jsp but was transferred to settings_1.jsp through a <jsp:forward> action.
				   
				   c) "/settings_2.jsp" indicates a postback. For a postback, "/settings_3.jsp" 
				   would be the expected referer, but this page seems to retain the original "/settings_2.jsp" 
				   referer between postbacks. This seems to indicate that the "referer" header attribute does not change 
				   until a different page is navigated to (no postbacks).
				   
				   d) "/settings_3.jsp" is not a referer that fits any scenario, but is matched here just in
				   case. The referer header attribute will never be assigned a value if the page "refers" to itself.
				*/
				bIsValidSource = true;			
			}
		}
	}

	if(!bIsValidSource){
		response.sendRedirect("settings_1.jsp");
	}
	else{
		init = null;
		reinitialization reinit = null;
		
		String sPropFileName = (String)request.getParameter("propertyfilename");
		String sTask = (String)request.getParameter("task");
		String sUpdateTo = request.getParameter("updateto");
		String sUpdateResults = "";
		boolean bCommitToDisk = true;
		
		String sBothChecker = "";
		String sSessionChecker = "";
		String sDiskChecker = "";
		
		if(sUpdateTo == null){
			init = initialization.getInstance(sPropFileName, session);
			sBothChecker = "CHECKED";
		}
		else{
			if(sUpdateTo.equalsIgnoreCase("BOTH")){
				init = initialization.getInstance(sPropFileName, session);
				sBothChecker = "CHECKED";
			}
			else if(sUpdateTo.equalsIgnoreCase("SESSION")){
				sSessionChecker = "CHECKED";
				init = initialization.getInstance(sPropFileName, session);
				bCommitToDisk = false;
			}
			else if(sUpdateTo.equalsIgnoreCase("DISK")){
				init = initialization.getInstance(sPropFileName);			
				sDiskChecker = "CHECKED";
			}
		}
		
		reinit = (reinitialization)init;
		
		if(sTask != null){
			if(sTask.equalsIgnoreCase("EDIT")){
				sUpdateResults = doUpdate(request, reinit, bCommitToDisk);
			}
			else if(sTask.equalsIgnoreCase("ADD")){
				sUpdateResults = doAdd(request, reinit, bCommitToDisk);
			}
		}
		
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

				<form action="settings_3.jsp" method="post">
					<%=sUpdateResults%>
					<table border="0" cellpadding="6" width="800px">
						<tr> 
							<td colspan="2" bgcolor="#AE9E8F" class="td1 td5 td2" style="background-color:#AE9E8F">
								<center>
									Various properties used to set things like database location and authentication 
									details, debugging on/off status, etc. are saved on disk in the following file:
									<br><br>
									<font color="black">
										[--<i>&nbsp;&nbsp;&nbsp;<%=reinit.getInitializationFileSpec()%>&nbsp;&nbsp;&nbsp;</i>--]
									</font>
									<br><br>
									Change, add and remove those properties here. Alternately you could open the file 
									directly and edit the contents there.
									
									<br><br>
									<table cellpadding=0 cellspacing=0 style="border-style:solid; border-width:1px; border-color:#2F221A;">
									<tr>
										<td class="td1">
											<input type="radio" name="updateto" id="rdoChangeBoth" value="BOTH" <%=sBothChecker%> >&nbsp;
										</td>
										<td class="td1">Updates affect properties stored on disk and the correponding properties stored in session state&nbsp;&nbsp;</td>
									</tr>
									<tr>
										<td class="td1">
											<input type="radio" name="updateto" id="rdoChangeSession" value="SESSION" <%=sSessionChecker%> >&nbsp;
										</td>
										<td class="td1">Updates affect session state only (properties file is unaltered)</td>
									</tr>
									<tr>
										<td class="td1">
											<input type="radio" name="updateto" id="rdoChangeDisk" value="DISK" <%=sDiskChecker%> >&nbsp;
										</td>
										<td class="td1">Updates affect properties stored on disk only (session state remains the same).</td>
									</tr>
									</table>
									<br>
								</center>
							</td>
						</tr>

						<%
							HashMap map = reinit.getStringMap();
							Iterator iter = map.keySet().iterator();
							int i = 0;
							while(iter.hasNext()){
								i++;
								Object o = iter.next();
								Object oVal = map.get(o);
						%>
								<tr> 
									<td class="td1 td5">
										<table cellpadding=0 cellspacing=0 border=0>
											<tr>
												<td class="td1">edit</td>
												<td>&nbsp;&nbsp;</td>
												<td class="td1">remove</td>
												<td>&nbsp;&nbsp;</td>
												<td class="td1"><input name="propname<%=i%>" id="txtPropName<%=i%>" type="text" class="label1" readonly value="<%=o%>" style="width:300px;"></td>
											</tr>
											<tr>
												<td><input name="edit<%=i%>" id="chkEdit<%=i%>" type="checkbox" onClick="javascript: if(this.checked) document.forms[0]['chkRemove<%=i%>'].checked = false;"></td>
												<td>&nbsp;&nbsp;</td>
												<td><input name="remove<%=i%>" id="chkRemove<%=i%>" type="checkbox" onClick="javascript: if(this.checked) document.forms[0]['chkEdit<%=i%>'].checked = false;"></td>
												<td>&nbsp;&nbsp;</td>
												<td width="100%">
													<input width="100%" name="prop<%=i%>" type="text" class="textbox1" id="txtProp<%=i%>" value="<%=oVal%>">
												</td>
											</tr>
										</table>
									</td>
								</tr>
						<%  }%>
						<tr> 
							<td colspan="2" align="right" bgcolor="#AE9E8F" class="td1 td5" style="background-color:#AE9E8F">
								update checked items&nbsp;&nbsp;
								<input type="button" class="button1" style="cursor: hand;" id="txtSubmitEdit" value="submit" onclick="javascript:doSubmit('edit');">
							</td>
						</tr>
						<tr><td height="30px">&nbsp;</td></tr>
						<tr> 
							<td colspan="2" bgcolor="#AE9E8F" class="td1 td5" style="background-color:#AE9E8F">
								<center>Enter a new name-value pair to create a new property</center>
							</td>
						</tr>
						<tr> 
							<td class="td1 td5">
								New property name<br>
								<input width="100%" type="text" class="textbox1" id="txtPropName" name="newpropname" onkeypress="javascript: if(/\W/.test(String.fromCharCode(event.keyCode))) event.returnValue = false;"> 
							</td>
						</tr>
						<tr> 
							<td class="td1 td5">
								New property value<br>
								<input width="100%" type="text" class="textbox1" id="txtPropValue" name="newpropvalue"> 
							</td>
						</tr>
						<tr> 
							<td colspan="2" align="right" bgcolor="#AE9E8F" class="td1 td5" style="background-color:#AE9E8F">
								<input type="button" class="button1" style="cursor: hand;" id="txtSubmitAdd" value="submit" onclick="javascript:doSubmit('add');">
							</td>
						</tr>
					</table>

					<input type="hidden" id="txtTask" name="task">
					<input type="hidden" name="propertyfilename" value="<%=sPropFileName%>">

					<script language="javascript">
						function doSubmit(sTask){
							var f = document.forms[0];
							switch(sTask){
								case 'edit':
									var bUpdatable = false;
									var chk;
									for(var i=1; (chk = f['chkEdit'+i]) != null; i++){
										if(chk.checked){
											bUpdatable = true;
											break;
										}
									}
									if(!bUpdatable){
										for(var i=1; (chk = f['chkRemove'+i]) != null; i++){
											if(chk.checked){
												bUpdatable = true;
												break;
											}
										}
									}
									if(bUpdatable){
										f['txtTask'].value = 'edit';
										f.submit();
									}
									else{
										alert('Please check one or more items to edit or remove');
									}
									break;
								case 'add':
									var sName = f['txtPropName'].value;
									var sVal = f['txtPropValue'].value;
									if(!sName){
										alert('Please enter a property name');
									}
									else if(/^\s+$/.test(sName)){
										alert('Please enter a property name');
									}
									else if(!/^\w+$/.test(sName)){
										alert('Allowed characters: A-Za-z0-9_');
									}
									else if(!sVal){
										alert('Please enter a property value');
									}
									else if(/^\s+$/.test(sVal)){
										alert('Please enter a property value');
									}
									else{
										f['txtTask'].value = 'add';
										f.submit();
									}
									break;
							}
						}
					</script>
				</form>
			</center>
			</body>
		</html>
<% 
	}
%>