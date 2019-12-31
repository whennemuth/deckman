<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@ page import="java.sql.*, java.util.*, java.lang.*, deckman.images.*, java.net.*, deckman.settings.*, deckman.images.organize.*" %>
<%@ include file="/admin/session_potential_router.jsp" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
<script language='javascript' src='scriptlib.js'></script>
<script language="javascript">
	
	String.prototype.suffix = function suffix(){
		if(/(0?[4-9]$)|(1\d$)|(0$)/.test(this)) return 'th';
		if(/1$/.test(this)) return 'st';
		if(/2$/.test(this)) return 'nd';
		if(/3$/.test(this)) return 'rd';
	}
    
	Number.prototype.suffix = function suffix2(){
		return this.toString().suffix()
	}
    
    function doSubmit(sTask, sValue){
        txtTask = document.forms[0].txtTask;
        switch(sTask){
            case 'last':
                txtTask.value = 'last';
                break;
            case 'next':
                txtTask.value = 'next';
                break;
            case 'jump':
                txtTask.value = 'jump';
                if(sValue != undefined){
                    document.forms[0].cbxJumpTo.value = sValue;
                }
                break;
			case 'category':
				txtTask.value = 'category';
				break;
            case 'list':
                txtTask.value = 'list';
                break;
            case 'edit':
                txtTask.value = 'edit';
                break;
        }
        document.forms[0].submit();
    }
</script>

<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>organize</title>
	<link href="styles.css" rel="stylesheet" type="text/css">
</head>
	
<%
	boolean bKeepResizing = false;
	admin_organize codeBehind = new admin_organize(request, session, init);
	if(codeBehind.getExceptionStackTrace() == null){
		if(codeBehind.getViewStyle().equalsIgnoreCase("fit")){
			bKeepResizing = true;
		}
	}
%>

<body leftmargin="30" <%=(bKeepResizing ? "onresize=\"javascript: onResize();\"" : "")%>>
	
	<center>
		<jsp:include page="banner.jsp">
			<jsp:param name="banner_selection" value="organize" />
		</jsp:include>
	</center>
	
	<%
		if(codeBehind.getExceptionStackTrace() != null){
			out.println("<pre>");
			out.print(codeBehind.getExceptionStackTrace());
			out.println("</pre>");
			out.println("</body></html>");
			out.close(); 
		}
	%>
	<form action="organize.jsp" method="get">


	<div style="font-family:courier; display:none;">
		Index:<input type="text" id="txtIndex" name="index" value="<%=String.valueOf(codeBehind.getIndex())%>"><br>
		&nbsp;Task:<input type="text" id="txtTask" name="task">
	</div>

	
	<table border="0" cellpadding=3 cellspacing=0 width="100%">
		
	<%
	boolean bDisplayCategorySelectionRow = codeBehind.getDeckCategories() != null;
	boolean bImageViewingPossible = true;
	
	if(codeBehind.getImagesList().isEmpty()){
		bImageViewingPossible = false;
		if(codeBehind.getCategory().equals("ALL")){
			bDisplayCategorySelectionRow = false;
			out.print("<tr><td class='td1 td3 td5' align='middle' colspan=2>THE DATABASE IS EMPTY OF IMAGE DATA</td></tr>");
		}
		else{
			out.print("<tr><td class='td1 td3 td5' align='middle' colspan=2>THERE ARE NO IMAGES ASSIGNED TO THE SELECTED CATEGORY</td></tr>");
		}
	}
	
	
	if(bDisplayCategorySelectionRow){ %>
		<tr>
			<td class="td1 td3 td5" align='right' style="width:175px;">CATEGORY</td>
			<td class="td1 td6">
				<select id="cbxCategory" name="category" onchange="javascript: doSubmit('category');" style="width:200px;">
					<%
						Iterator iter1 = codeBehind.getDeckCategories().iterator();
						String sCatSelected = "";
						while(iter1.hasNext()){
							String sCat = (String)iter1.next();
							if(codeBehind.getCategory().equals(sCat)){
								sCatSelected = "SELECTED";
							}
							out.println("<option value='" + sCat + "' " + sCatSelected + ">" + sCat + "</option>\r\n");
							sCatSelected = "";
						}
					%>
				</select>
			</td>
		</tr>
	<% } %>


	<%
	if(bImageViewingPossible){
		boolean bDisplayJumpTo = true;
		bDisplayJumpTo &= codeBehind.getImage() != null;
		bDisplayJumpTo &= (!codeBehind.getViewStyle().equalsIgnoreCase("listing"));
		
	%>
		<tr style="display:<%=(bDisplayJumpTo?"":"none")%>">
			<td class="td1 td3 td5" align='right'>IMAGE</td>
			<td class="td1 td6">
				<select id="cbxJumpTo" name="JumpTo" onchange="javascript: doSubmit('jump');" style="width:200px;">
					<%
						Iterator iter2 = codeBehind.getImagesList().iterator();
						int iCounter = 0;
						try{
							while(iter2.hasNext()){
								String[] aImg = (String[])iter2.next();
								String sSelected = iCounter==codeBehind.getIndex() ? " SELECTED" : "";
								out.println("<option value=\"" + String.valueOf(iCounter++) + "\" ID=\"" + aImg[0] + "\"" + sSelected + ">" + aImg[1] + "</option>\r\n");
							}
						}
						catch(Exception e){
							out.println(e.toString());
						}
					%>
				</select>
				<script language="javascript">
					var iRecord = <%=String.valueOf((codeBehind.getIndex()+1))%>;
					var iRecords = <%=String.valueOf(codeBehind.getImagesList().size())%>;
					document.write(iRecord + iRecord.suffix() + " of " + iRecords);
				</script>
			</td>
		</tr>



		<tr>
			<td class="td1 td3 td5" align="right">VIEW CHOICES</td>
			<td class="td1 td6" valign="middle">
				<%
					String sFull = codeBehind.getViewStyle().equals("full") ? "CHECKED" : "";
					String sFit = codeBehind.getViewStyle().equals("fit") ? "CHECKED" : "";
					String sThumbnail = codeBehind.getViewStyle().equals("thumbnail") ? "CHECKED" : "";
					String sListing = codeBehind.getViewStyle().equals("listing") ? "CHECKED" : "";
				%>
				<table cellpadding=0 cellspacing=0 style="border-style:solid; border-width:4px; border-color:#AE9E8F;">
					<tr>
					<td class="td1 td3">&nbsp;Image&nbsp;&nbsp;</td>
					<td valign="middle" align="right">&nbsp;Actual Size</td>
					<td valign="middle"><input type="radio" name="viewstyle" <%=sFull%> value="full"  onclick="doSubmit('jump');">&nbsp;&nbsp;&nbsp;&nbsp;</td>
					<td valign="middle" align="right">Fit to Screen Width</td>
					<td valign="middle"><input type="radio" name="viewstyle" <%=sFit%> value="fit" onclick="doSubmit('jump');">&nbsp;&nbsp;&nbsp;&nbsp;</td>
					<td valign="middle" align="right">Thumbnail</td>
					<td valign="middle"><input type="radio" name="viewstyle" <%=sThumbnail%> value="thumbnail"  onclick="doSubmit('jump');">&nbsp;&nbsp;&nbsp;&nbsp;</td>
					<td class="td1 td3">&nbsp;&nbsp;Listing&nbsp;&nbsp;</td>
					<td valign="middle"><input type="radio" name="viewstyle" <%=sListing%> value="listing"  onclick="doSubmit('list');"></td>
					</tr>
				</table>
			</td>
		</tr>
		</table>


		<table class="td1 td6">
			<tr>
				<td valign="top">
				<% if(codeBehind.getImage() != null){ %>
					<table cellspacing="0" cellpadding="1" style="font-weight:bold">
					<% if(!codeBehind.getViewStyle().equalsIgnoreCase("listing")){ %>
						<tr> 
							<td class="td1" align="center" colspan=2>
								<input type='button' value='<< Last ' style='width:76px;' onclick="javascript: doSubmit('last');"> 
								<input type='button' value=' Next >>' style='width:76px;' onclick="javascript: doSubmit('next');">
							</td>
						</tr>

						<tr> 
							<td class="td1" align="center" colspan=2>
								<input type="button" value="COMMIT CHANGES" style="width:155px; font-weight:bold;" onclick="javascript: doSubmit('edit');"> 
							</td>
						</tr>

						<tr> 
							<td colspan=2 style="padding:0px"><hr color="white" width="175px" size="1px"></td>
						</tr>
					<%
						}
							boolean bChecked = false;

							Iterator iter3 = codeBehind.getDeckCategories().iterator();
							int i = 0;
							while(iter3.hasNext()){
								String sFldID = (String)iter3.next();

								try{
									if(codeBehind.getImage().containsKey(sFldID)){
										bChecked = ((Boolean)codeBehind.getImage().get(sFldID)).booleanValue();
										out.println("<tr><td align='right' class='td1'>");
										out.println(sFldID);
										out.println("</td>");
										out.println("<td class='td1'>");
										out.print("<input type='checkbox' id='chk" + String.valueOf(i) + "'  name='" + sFldID + "' " + (bChecked?"CHECKED":"") + ">");
										out.println("</td></tr>");
									}
								}
								catch(Exception e){
									out.println("<tr><td class='td1'>");
									out.print("<pre style='font-size:10pt; align:left;'>" + deckman.utils.codeLib.getStackTraceAsString(e) + "</pre>");
									out.println("</td>");
									out.println("<td class='td1'>");
									out.print("<input type='checkbox' id='chk" + String.valueOf(i) + "'  name='" + sFldID + "' " + (bChecked?"CHECKED":"") + ">");
									out.println("</td></tr>");
								}

								i++;
							}
						}

						%>
					</table>
						<%
						if(codeBehind.getTask().equals("edit")){
							boolean bEditSuccess = codeBehind.getExceptionStackTrace()==null;
							if(bEditSuccess){
								out.println("<div id=\"divEditMsg\" align=\"right\" class=\"EditSuccess\"><br>Edit Successful</div>");
							}
							else{
								out.println("<div id=\"divEditMsg\" align=\"right\" class=\"EditFailure\">Edit Failed<br>");
								out.println("<div style='font-family: monospace; font-size:8pt; overflow:auto; width:500px;'><pre>");
								out.print(codeBehind.getExceptionStackTrace());
								out.println("</pre></div></div>");
							}
							out.println("<script language=\"javascript\">window.setTimeout(\"document.all.divEditMsg.style.display='none';\", 2000);</script>");
						}
						%>
				</td>

				<td width="100%" id="tdImage" rowspan="18" align="left" valign="top" style='padding-left:10'>
					<%
						String sSrc = "";
						String sDisplay = "";
						if(codeBehind.getViewStyle().equalsIgnoreCase("listing")){
							if(codeBehind.getCategoryThumbnails() != null){
								Iterator iter4 = codeBehind.getCategoryThumbnails().iterator();
								while(iter4.hasNext()){
									sDisplay += (String)iter4.next();
								}
							}
							else{
								sDisplay = "ArrayList CatThumbs is null";
							}
						}
						else{
							sSrc = (String)codeBehind.getImage().get("name");
							if(codeBehind.getViewStyle().equalsIgnoreCase("full") || codeBehind.getViewStyle().equalsIgnoreCase("fit")){
								sSrc = codeBehind.getFullsizeContextPath() + "/" + sSrc;
								sDisplay = "<img id='imgLarge' src=\"" + sSrc + "\">";
							}
							else if(codeBehind.getViewStyle().equalsIgnoreCase("thumbnail")){
								sSrc = codeBehind.getThumbnailContextPath() + "/" + sSrc;
								sSrc = codeLib.switchFileExtension(sSrc, "gif");
								sDisplay = "<img id='imgLarge' src=\"" + sSrc + "\" >";
							}
						}
					%>
					<%=sDisplay%>
				</td>
			</tr>
		<% } %>
		</table>
	</form>

	<script language="javascript">
		<%=(bKeepResizing ? "doResize();" : "")%>

		function doResize(){
			var img = document.getElementById('imgLarge');
			if(img != null){
				img.width = 100;
				onResize();
			}
		}

		function onResize(){
			var img = document.getElementById('imgLarge');
			if(img != null){
				if(/fullsize/i.test(img.src)) img.width = document.body.clientWidth - 200
			}
		}
	</script>
</body>
</html>
