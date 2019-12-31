<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@ page import="java.util.*, deckman.images.cleanup.*, deckman.settings.*, deckman.utils.codeLib" %>
<%@ include file="/admin/session_potential_router.jsp" %>

<%--
When images are uploaded to the website, their names are entered into its database. Later, these database entries are tagged with specific categories chosen by the Deckman. There should remain a one-to-one matching of physical image file and corresponding database entry. However, discrepancies can arise between the physical image store and database content. Such discrepancies can arise from:
Manually deleting image files
Manually renaming image files
Manually editing or deleting database entries
Runtime errors that occur during normal administrative use of the "Create Thumbnails" or "Organize Images" web pages


Orphaned database entries.
Entries that exist in the website database, but no matching image files can be found

Orphaned images files.
Image files found that have no matching entry in the website database.

 
--%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
   
<%
	admin_cleanup cleaner = new admin_cleanup(init);
	Iterator iter = null;
	StringBuffer buff = new StringBuffer();
	boolean bDebug = init.getPropertyAsBoolean("DebugMode").booleanValue();
	boolean bDisplayMeter = false;
	String sSimulation = request.getParameter("simulation");
	boolean bSimulation = sSimulation==null ? false: (sSimulation.equalsIgnoreCase("ON"));
	
	String sTask = request.getParameter("task");	// indicates what the user chose to do out of the choices provided. Also indicates a postback.
	
	List AllParams = Collections.list(request.getParameterNames());
	List CheckboxNames = new ArrayList();
	List CheckboxValues = null;
	List Errors = null;
	
	if(sTask != null){
		String sNameBase = null;
		
		if(sTask.equalsIgnoreCase("DELETE_MARKED_IMAGES")){
			Errors = cleaner.purgeMarkedImagesFromDB();
			if(Errors.isEmpty()){
				out.println("<script language='javascript'>alert('" + String.valueOf(cleaner.getRecordsAffected()) + " records deleted');</script>");
			}
		}	
		else if(sTask.equalsIgnoreCase("PURGE_ENTRIES")){
			CheckboxNames = codeLib.getSortedIndexedSubset(AllParams, "fixEntry");
			CheckboxValues = codeLib.getFormParameterValues(CheckboxNames, request);
			Errors = cleaner.purgeDatabaseOrphans(CheckboxValues, "ID");
		}
		else if(sTask.equalsIgnoreCase("PURGE_FILES")){
			CheckboxNames = codeLib.getSortedIndexedSubset(AllParams, "fixImage");
			CheckboxValues = codeLib.getFormParameterValues(CheckboxNames, request);
			Errors = cleaner.purgeFullsizeImages(CheckboxValues);
		}
		else if(sTask.equalsIgnoreCase("ADD_TO_DATABASE")){
			CheckboxNames = codeLib.getSortedIndexedSubset(AllParams, "fixImage");
			CheckboxValues = codeLib.getFormParameterValues(CheckboxNames, request);
			Errors = cleaner.addOrphanedImagesToDatabase(CheckboxValues);
		}
		else if(sTask.equalsIgnoreCase("PURGE_THUMBNAILS")){
			CheckboxNames = codeLib.getSortedIndexedSubset(AllParams, "fixThumbOrphans");
			CheckboxValues = codeLib.getFormParameterValues(CheckboxNames, request);
			Errors = cleaner.purgeOrphanedThumbnails(CheckboxValues);
		}
		else if(sTask.equalsIgnoreCase("CREATE_THUMBNAILS")){
			CheckboxNames = codeLib.getSortedIndexedSubset(AllParams, "fixMissingThumb");
			CheckboxValues = codeLib.getFormParameterValues(CheckboxNames, request);
			if(bSimulation){
				Errors = cleaner.simulateMissingThumbnailCreation(CheckboxValues, session);
			}
			else{
				Errors = cleaner.createMissingThumbnails(CheckboxValues, session);
			}
			bDisplayMeter = true;
		}
		
	}
%>


<html>

	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>cleanup</title>
		<link href="styles.css" rel="stylesheet" type="text/css">
	</head>
	
	<script language="javascript">
		
		
		function doSubmit(sTask, sCheckboxID){
			var bSelectionMade = false;
			if(sCheckboxID == null){
				bSelectionMade = true;
			}
			else if(sCheckboxID == undefined){
				bSelectionMade = true;
			}
			else{
				bSelectionMade = isSelectionMade(sCheckboxID);
			}
			
			if(bSelectionMade){
				/* Possible values for sTask:
						'PURGE_ENTRIES'
						'PURGE_FILES'
						'ADD_TO_DATABASE'
						'PURGE_THUMBNAILS'
						'CREATE_THUMBNAILS'
						'DELETE_MARKED_IMAGES'
				*/
				var f = document.forms[0];
				f['task'].value = sTask;
				f.submit();
				if(sTask == 'CREATE_THUMBNAILS'){
					document.getElementById('divMeter').style.display = '';
					f.submit();
					useAJAXforThumbProgress();
				}
			}
			else{
				alert('No items have been checked');
			}
		}
		
		
		
			
						
		var request;
        var requestTimer;
        var MAX_WAITING_TIME = 60000;   // wait for a minute

        function useAJAXforThumbProgress(){
            /*
                    REFERENCE: http://ajaxpatterns.org/XMLHttpRequest_Call
                    Why am I Using async in the open method of the XMLHttpRequest object? Most scenarios would use
                    the async mode (the third parameter of open() set to true). The async parameter specifies whether
                    the request should be handled asynchronously or not. True means that script continues to run after
                    the send() method, without waiting for a response from the server. false means that the script
                    waits for a response before continuing script processing. By setting this parameter to false,
                    you run the risk of having your script hang if there is a network or server problem, or if the
                    request is long (the UI locks while the request is being made) a user may even see the
                    "Not Responding" message. It is safer to send asynchronously and design your code around the
                    onreadystatechange event.
            */

            /**
             * Safari will not process any callback functionality after this page has been submitted and waiting
             * for a response. However, it will handle ajax responses if they are done synchronously.
             */
            var sBrowser = navigator.userAgent.toUpperCase();
            var bSafari = sBrowser.indexOf("SAFARI") != -1;
            var bAsync = !bSafari;

            if(window.XMLHttpRequest){  // ie 7 and above, non-ie browsers
                try{
                    request = new XMLHttpRequest();
                        // timeout argument only added to make URL unique so as to prevent caching
                    request.open("get", "reimageProgress?type=DETAILED&timestamp=" + new Date().getTime(), bAsync);

                    if(bAsync){
                        request.onreadystatechange = animateProgressMeter;
                    }
                }
                catch(e){
                    alert(e);
                }

                requestTimer = window.setTimeout(function(){
                    request.abort();
                    alert("upload aborted due to AJAX timeout with server.");
                }, MAX_WAITING_TIME);

                request.send(null);

                if(!bAsync){
                    animateProgressMeter();
                }
            }             
            else if(window.ActiveXObject){  //IE browsers
                try{
                    // ie 6 (will work with ie 7 and above if allowed)
                    request = new ActiveXObject("Msxml2.XMLHTTP");
                }
                catch(e){
                    // ie 5.5 and below
                    request = new ActiveXObject("Microsoft.XMLHTTP");
                }

                if(request){
                    if(bAsync){
                        request.onreadystatechange = animateProgressMeter;
                    }

                        // timeout argument only added to make URL unique so as to prevent caching
                    request.open("GET", "reimageProgress?type=DETAILED&timestamp=" + new Date().getTime(), bAsync);  //See notes

                    requestTimer = window.setTimeout(function(){
                        request.abort();
                        alert("upload aborted due to AJAX timeout with server.");
                    }, MAX_WAITING_TIME);

                    request.send(null);
                    if(!bAsync){
                        animateProgressMeter();
                    }
                }
                else{
                    alert("no ActiveXObject");
                }
            }
            else{
                window.status = "you browser does not support the necessary components to drive a file upload progress meter";
            }
        }	
		
		
			
						
		function animateProgressMeter(){
			var bRepeatAJAX = true;
			if(request.readyState == 4){    //loaded state
				var sRetval = request.responseText;
				var iStatus = request.status;
				if(request.status == 200){  //ok status
					var oDivErr = document.getElementById('divAJAX_error');
					if(/^\d+,\d+,\d+$/.test(sRetval)){
						oDivErr.style.display = 'none';
						aRetval = sRetval.split(',');
						sThumbsCreated = aRetval[0];
						sTotalImages = aRetval[1];
						sPercentDone = aRetval[2];
						if(sPercentDone == '100'){
							bRepeatAJAX = false;
						}
						else{
							growBar(sThumbsCreated, sTotalImages, sPercentDone);
						}
					}
					else{
						var oPreErr = document.getElementById('preAJAX_error');
						var oTotalImages = document.getElementById('tdTotalImages');
						var oMiddle = document.getElementById('tdMiddle');
						var oThumbsCreated = document.getElementById('tdThumbsCreated');

						oPreErr.innerHTML = sRetval;
						oDivErr.style.display = '';
						oTotalImages.innerHTML = '';
						oThumbsCreated.innerHTML = '';
						oMiddle.innerHTML = "<font color='red'>ERROR</font>";
					}
				}

				if(bRepeatAJAX) window.setTimeout("useAJAXforThumbProgress()", 300);
			}
		}
		
		
		
			
						
		function growBar(sThumbsCreated, sTotalImages, sPercentDone){			
			var oTotalImages = document.getElementById('tdTotalImages');
			var oMiddle = document.getElementById('tdMiddle');
			var oThumbsCreated = document.getElementById('tdThumbsCreated');
			var bar = document.getElementById('divBar');

			if(oThumbsCreated != null){
				if(sPercentDone == '100'){
					oMiddle.innerHTML = 'done';
				}
				else{
					oMiddle.innerHTML = 'of';
					oTotalImages.innerHTML = sTotalImages;
					oThumbsCreated.innerHTML = sThumbsCreated + " images";
					bar.style.width = sPercentDone + "%";
				}
			}
		}
		
		
		
			
						
		function isSelectionMade(sCheckboxID){
			/* 
			   This function checks to make sure that at least one checkbox in a group of checkboxes is checked.
			   The group of checkboxes includes any checkbox with an id attribute of the form: sCheckboxID+i, where i is an integer.
			   The assumption is made that the group will not have "gaps" because id of the next member of the group will always be sCheckboxID+[i+1]
			 */
			 
			var chk;
			var f = document.forms[0];
			var bChecked = false;
			for(var i=1; (chk = f[sCheckboxID+i]) != null; i++){
				bChecked |= chk.checked;
			}
			return bChecked;
		}
		
		
		
			
						
		function toggleAllCheckboxes(sCheckboxID, bChecked){
			/* 
			   This function checks/unchecks each checkbox in a group of checkboxes.
			   The group of checkboxes includes any checkbox with an id attribute of the form: sCheckboxID+i, where i is an integer.
			   The assumption is made that the group will not have "gaps" because id of the next member of the group will always be sCheckboxID+[i+1]
			 */
			 
			var chk;
			var f = document.forms[0];
			for(var i=1; (chk = f[sCheckboxID+i]) != null; i++){
				chk.checked = bChecked;
			}
		}
	</script>
	
	<body>

		<center>
			<jsp:include page="banner.jsp">
				<jsp:param name="banner_selection" value="cleanup" />
			</jsp:include>

			<br>
			<table style="width:800px;" cellspacing=0 cellpadding=6>
			<tr>
				<td class="td1 td5 td2" colspan=4>S I T E&nbsp;&nbsp;&nbsp;&nbsp;C L E A N U P</td>
			</tr>
			<tr>
				<td colspan=4 class="td1 td5">
					When images are uploaded to the website, their names are entered into its database. 
					Later, these database entries are tagged with specific categories chosen by the Deckman. There should remain a one-to-one matching of physical image file and corresponding database entry. However, discrepancies can arise between the physical image store and database content. Such discrepancies could arise from:
					<div align="left">
					<ol>
					<li>Runtime errors that occur during normal administrative use of the "Create Thumbnails" or "Organize Images" web pages</li>
					<li>Manually deleting image files</li>
					<li>Manually renaming image files</li>
					<li>Manually editing or deleting database entries</li>
					</ol></div>
				</td>
			</tr>
			<tr>
				<td colspan=2 class="td1 td5 td2" style="width:50%">
					<u>FULLSIZE IMAGES</u><br>
					to remove images marked for deletion click 
					<input type="button" style="font-size:10px;" value="here " onclick="javascript: doSubmit('DELETE_MARKED_IMAGES');">
				</td>
				<td colspan=2 class="td1 td5 td2" style="width:50%"><u>THUMBNAILS</u></td>
			</tr>
			<tr>
				<td class="td1 td5 td2" style="width:25%">
					<div>ORPHANED DATABASE ENTRIES</div>
					<div style="padding-top:5px">Entries in the website database, but no matching image files found</div>
				</td>
				<td class="td1 td5 td2" style="width:25%">
					ORPHANED IMAGE FILES
					<br>
					<div style="padding-top:5px">Image files found with no matching entry in the website database</div>
				</td>
				<td class="td1 td5 td2" style="width:25%">
					ORPHANED THUMBNAILS
					<br>
					<div style="padding-top:5px">Thumbnails found with no corresponding fullsize image</div>
				</td>
				<td class="td1 td5 td2" style="width:25%">
					MISSING THUMBNAILS
					<br>
					<div style="padding-top:5px">Fullsize images for which no thumbnails have been created</div>
				</td>
			</tr>
			</table>





			<!-- START THUMBNAIL UPLOAD PROGRESS METER -->
			<div id="divMeter" style="display:<%=(bDisplayMeter?"":"none")%>;">
				<br>
				<table cellpadding="6" cellspacing="0" width="800px">
					<tr>
						<td colspan=3 class="td1 td5 td2">
							THUMB PROGRESS:
						</td>
					</tr>
					<tr>
						<td class="td1 td5">
							<table width="400px">
								<tr>
									<td id="tdThumbsCreated" align="right" class="td1 td4" width="175"></td>
									<td id="tdMiddle" align="center" class="td1 td4" width="30px">done</td>
									<td id="tdTotalImages" align="left" class="td1 td4" width="185"></td>
								</tr>
								<tr>
									<td colspan=3>
										<div align="left" style="width:400px; border-style:solid; border-width:1px; border-color:#0099CC; padding:2px; background-color:white;">
											<div id="divBar" style="height:100%; width:<%=(bDisplayMeter?"100":"0")%>%; background-color:blue"></div>
										</div>
									</td>
								</tr>
							</table>
						</td>
					</tr>
				</table>

				<div id="divAJAX_error" style="position:absolute; display:none;">

                     <div style="
						position:relative;
						left:-203px;
						width:400px;
						padding:3px;
						border-style:solid;
						border-width:1px;
						background-color:red;
						font-family:monospace;
						font-size:8pt;
						overflow:auto;
					">
                        <pre id="preAJAX_error"></pre>
                    </div>
				</div>

			</div>
			<!-- END THUMBNAIL UPLOAD PROGRESS METER -->
			
			
			
			
			
			
			<form action="cleanup.jsp" method="POST">
				<input type="hidden" id="task" name="task">
				
				<table style="width:800px;" cellspacing=0 cellpadding=0>
				<tr>



					
					<!-- START ORPHANED DATABASE ENTRIES -->
					<td valign="top" class="td7" style="width:25%; border-right: 1pt solid #FFFFFF;" align="center">
						<table height="100%" style="width:100%; border-width:0px; height:100%;" cellspacing=0 cellpadding=3>
							<%
							ArrayList list = cleaner.getDatabaseOrphans();
							if(list.isEmpty()){
							%>
								<tr><td class="td1 td6" align="center"><br>NONE<br></td></tr>
							<%
							}
							else{
								iter = list.iterator();
								int iCounter = 1;
								boolean bError = false;
								buff = new StringBuffer();
								while(iter.hasNext()){
									Object o = iter.next();
									if(o instanceof EquatableImageFileName){
										EquatableImageFileName eifn = (EquatableImageFileName)o;
										String sID = eifn.getID().toString();
										String sImgName = eifn.getStringValue();
										
										buff.append("<tr>\r\n");
											buff.append("<td class='td1 td6' align='left'>\r\n");
												
												// start checkbox
												buff.append("<input ");
												buff.append(	"type='checkbox' ");
												buff.append(	"id='chkFixEntry" + String.valueOf(iCounter)+ "' ");
												buff.append(	"name='fixEntry" + (iCounter++) + "' ");
												buff.append(	"value=\"" + sID + "\"");
												buff.append(">");
												// end checkbox
												
												buff.append(sImgName);
											buff.append("</td>\r\n");
										buff.append("</tr>\r\n");
									}
									else if(o instanceof String){
											// indicates an error
										bError = true;
							%>
										<tr><td class="td1 td6" align="center"><%=o.toString()%></td></tr>								
							<%
									} // end if
								} // end while
								
								if(!bError){
							%>
									<tr>
										<td class="td1 td6" align="center">
											<input type="button" class="textbox2" style="width:66px; font-size:10px;" value="check all" onclick="javascript: toggleAllCheckboxes('chkFixEntry', true);">
											<input type="button" class="textbox2" style="width:66px; font-size:10px;" value="uncheck all " onclick="javascript: toggleAllCheckboxes('chkFixEntry', false);">
										</td>
									</tr>
									<tr valign="middle">
										<td class="td1 td6" align="center" height="75px">
											<input type="button" class="textbox2" style="width:136px; height:100%;" value="Purge Entries" onclick="javascript: doSubmit('PURGE_ENTRIES', 'chkFixEntry');">
											<br>
										</td>
									</tr>
							<%
									out.println(buff.toString());
								}
							}
							%>
						</table>
						<br>
					</td>
					<!-- END ORPHANED DATABASE ENTRIES -->


					
					
					
					<!-- START ORPHANED IMAGE FILES -->
					<td valign="top" class="td7" style="width:25%; border-right: 1pt solid #FFFFFF;" align="center">
						<table height="100%" style="width:100%; border-width:0px; height:100%;" cellspacing=0 cellpadding=3>
							<%
							list = cleaner.getFileOrphans();
							if(list.isEmpty()){
							%>
								<tr><td class="td1 td6" align="center"><br>NONE<br></td></tr>
							<%
							}
							else{
								iter = list.iterator();
								int iCounter = 1;
								boolean bError = false;
								buff = new StringBuffer();
								while(iter.hasNext()){
									Object o = iter.next();
									if(o instanceof EquatableImageFileName){
										EquatableImageFileName eifn = (EquatableImageFileName)o;
										String sImgName = eifn.getStringValue();
										buff.append("<tr>\r\n");
											buff.append("<td class='td1 td6' align='left'>\r\n");
												
												// start checkbox
												buff.append("<input ");
												buff.append(	"type='checkbox' ");
												buff.append(	"id='chkFixImages" + String.valueOf(iCounter) + "' ");
												buff.append(	"name='fixImage" + (iCounter++) + "' ");
												buff.append(	"value=\"" + sImgName + "\"");
												buff.append(">");
												// end checkbox
												
												buff.append(sImgName);
											buff.append("</td>\r\n");
										buff.append("</tr>\r\n");
									}
									else if(o instanceof String){
										bError = true;
											// indicates an error
							%>
										<tr><td class="td1 td6" align="center"><%=o.toString()%></td></tr>								
							<%
									} // end if
								} // end while
								
								if(!bError){
							%>
									<tr>
										<td class="td1 td6" align="center">
											<input type="button" class="textbox2" style="width:66px; font-size:10px;" value="check all" onclick="javascript: toggleAllCheckboxes('chkFixImages', true);">
											<input type="button" class="textbox2" style="width:66px; font-size:10px;" value="uncheck all " onclick="javascript: toggleAllCheckboxes('chkFixImages', false);">
										</td>
									</tr>
									<tr valign="middle">
										<td class="td1 td6" align="center" height="75px">
											<input name="button" type="button" class="textbox2" style="width:136px;" value="Purge Files" onclick="javascript: doSubmit('PURGE_FILES', 'chkFixImages');">
											<br><br> 
											<input type="button" class="textbox2" style="width:136px;" value="Add to Database" onclick="javascript: doSubmit('ADD_TO_DATABASE', 'chkFixImages');">
											<br>
										</td>
									</tr>
								
							<%
									out.println(buff.toString());
								} // end if
							} // end if
							%>
						</table>
						<br>
					</td>
					<!-- END ORPHANED IMAGE FILES -->

					
					

					
					<!-- START ORPHANED THUMBNAILS -->
					<td valign="top" class="td7" style="width:25%; border-right: 1pt solid #FFFFFF;" align="center">
						<table height="100%" style="width:100%; border-width:0px; height:100%;" cellspacing=0 cellpadding=3>
							<%
							list = cleaner.getThumbnailOrphans();
							if(list.isEmpty()){
							%>
								<tr><td class="td1 td6" align="center"><br>NONE<br></td></tr>
							<%
							}
							else{
								iter = list.iterator();
								int iCounter = 1;
								boolean bError = false;
								buff = new StringBuffer();
								while(iter.hasNext()){
									Object o = iter.next();
									if(o instanceof EquatableImageFileName){
										EquatableImageFileName eifn = (EquatableImageFileName)o;
										String sImgName = eifn.getStringValue();
										
										buff.append("<tr>\r\n");
											buff.append("<td class='td1 td6' align='left'>\r\n");

												// start checkbox
												buff.append("<input ");
												buff.append(	"type='checkbox' ");
												buff.append(	"id='chkFixThumbOrphans" + String.valueOf(iCounter) + "' ");
												buff.append(	"name='fixThumbOrphans" + (iCounter++) + "' ");
												buff.append(	"value=\"" + sImgName + "\"");
												buff.append(">");
												// end checkbox

												buff.append(sImgName);													
											buff.append("</td>\r\n");
										buff.append("</tr>\r\n");
										
									}
									else if(o instanceof String){
											// indicates an error
										bError = true;
							%>
										<tr><td class="td1 td6" align="center"><%=o.toString()%></td></tr>								
							<%
									} // end if
								} // end while
								
								if(!bError){
							%>
									<tr>
										<td class="td1 td6" align="center">
											<input type="button" class="textbox2" style="width:66px; font-size:10px;" value="check all" onclick="javascript: toggleAllCheckboxes('chkFixThumbOrphans', true);">
											<input type="button" class="textbox2" style="width:66px; font-size:10px;" value="uncheck all " onclick="javascript: toggleAllCheckboxes('chkFixThumbOrphans', false);">
										</td>
									</tr>
									<tr valign="middle">
										<td class="td1 td6" align="center" height="75px">
											<input type="button" class="textbox2" style="width:136px;" value="Purge Thumbnails" onclick="javascript: doSubmit('PURGE_THUMBNAILS', 'chkFixThumbOrphans');">
											<br>
										</td>
									</tr>
							<%
									out.println(buff.toString());
								} // end if
							} // end if
							%>
						</table>
						<br>
					</td>
					<!-- END ORPHANED THUMBNAILS -->

					
					

					
					<!-- START MISSING THUMBNAILS -->
					<td valign="top" class="td7" style="width:25%; border-right: 1pt solid #FFFFFF;" align="center">
						<table height="100%" style="width:100%; border-width:0px; height:100%;" cellspacing=0 cellpadding=3>
							<%
							list = cleaner.getMissingThumbnails();
							if(list.isEmpty()){
							%>
								<tr><td class="td1 td6" align="center"><br>NONE<br></td></tr>
							<%
							}
							else{
								iter = list.iterator();
								int iCounter = 1;
								boolean bError = false;
								buff = new StringBuffer();
								while(iter.hasNext()){
									Object o = iter.next();
									if(o instanceof EquatableImageFileName){
										EquatableImageFileName eifn = (EquatableImageFileName)o;
										String sImgName = eifn.getStringValue();
										buff.append("<tr>\r\n");
											buff.append("<td height='100%' class='td1 td6' align='left'>\r\n");
												
												// start checkbox
												buff.append("<input ");
												buff.append(	"type='checkbox' ");
												buff.append(	"id='chkFixMissingThumb" + String.valueOf(iCounter) + "' ");
												buff.append(	"name='fixMissingThumb" + (iCounter++) + "' ");
												buff.append(	"value=\"" + sImgName + "\"");
												buff.append(">\r\n");
												// end checkbox
												
												buff.append(sImgName);
											buff.append("</td>\r\n");
										buff.append("</tr>\r\n");
									}
									else if(o instanceof String){
											// indicates an error
										bError = true;
							%>
										<tr><td class="td1 td6" align="center"><%=o.toString()%></td></tr>								
							<%
									}
								}
								
								if(!bError){
							%>
									<tr>
										<td class="td1 td6" align="center">
											<input type="button" class="textbox2" style="width:66px; font-size:10px;" value="check all" onclick="javascript: toggleAllCheckboxes('chkFixMissingThumb', true);">
											<input type="button" class="textbox2" style="width:66px; font-size:10px;" value="uncheck all " onclick="javascript: toggleAllCheckboxes('chkFixMissingThumb', false);">
										</td>
									</tr>
									<tr valign="middle">
										<td class="td1 td6" align="center" height="75px">
											<span style="display: <%=bDebug?"":"none"%>;">simulate thumb creation</span>
											<input type="checkbox" name="simulation" id="chkSimulation" style="display: <%=bDebug?"":"none"%>;">
											<br style="display: <%=bDebug?"":"none"%>;">
											
											<input type="button" class="textbox2" style="width:136px;" value="Create Thumbnails" onclick="javascript: doSubmit('CREATE_THUMBNAILS', 'chkFixMissingThumb');">
										</td>
									</tr>
							<%
									out.println(buff.toString());
								}
							}
							%>
						</table>
						<br>
					</td>
					<!-- END MISSING THUMBNAILS -->
					
					
					
					
				</tr>
				</table>
			</form>

		</center>
			
			
			<!--<div style="width:800px; overflow:auto;" align="left">-->
			<%
					// if any of the database updates or image purging or creation operations produce errors, those errors are displayed here.
				String sDBErr = cleaner.DatabaseErrorStackTrace();
				if(sDBErr != null){
					List DBErrList = new ArrayList();
					DBErrList.add(sDBErr);
					String sDBErrHTML = cleaner.GetErrorListAsHTML(DBErrList);
					out.println(sDBErrHTML);
				}
				
				if(Errors != null){
					if(!Errors.isEmpty()){
						out.println(cleaner.GetErrorListAsHTML(Errors));
					}
				}
				
				cleaner.breakDB_Connection();
			%>
			<!--</div>	-->
    </body>
</html>
