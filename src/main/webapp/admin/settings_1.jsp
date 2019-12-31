<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="deckman.settings.*, java.util.*, java.util.regex.*, com.warren.logging.*, com.warren.logging.logs.*"%>
<%@ include file="/admin/session_potential_router.jsp" %>

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
        <form action="settings_2.jsp" method="post">
            <table border="0" cellpadding="6" width="800px">
                <tr> 
                    <td colspan="2" bgcolor="#AE9E8F" class="td1 td5 td2" style="background-color:#AE9E8F">
                        The deckman website uses a range of properties and settings that include things like:
						<ul>
							<li>The physical path of the directory that is the target of generated thumbnails</li>
							<li>The connection string for the site database (choices will be amoung the production DB or test copies)</li>
							<li>The physical path of the directory targeted for fileuploads over HTTP</li>
							<li>etc...</li>
						</ul>
						What these properties and settings all have in common is that they define different "configurations" and therefore
						should be modifiable. Hence they are not hardcoded into jsp or java code, but are instead itemized as name/property
						entries in a properties file. The default behavior on any jsp page of the deckman website is to obtain the data in 
						a property file called "<%=initialization.DEFAULT_PROP_FILE_NAME%>" found in the deckman.settings package (same directory where initialize.class will be found)
						to get the configuration data that it needs - that is, if the data in the properties file hasn't already been loaded into a
						session variable ("initializer") - in which case, the jsp page will obtain its data from there.
						<br><br>
						This jsp page:
						<ol>
							<li>
								Offers the option of specifying where and under what name the properties file is to be found.
								This will be helpful if a separate set of configurations is needed to see how the site will run under - for example -
								a different database. One need only have already loaded a different properties file up to the website and then use this
								interface to switch from using the default properties file to using this one instead.
							</li>
							<li>
								Offers a feature to edit existing property names and/or values in a specified properties file.
							</li>	
							<li>
								Offers a feature to add or remove properties in a specified properties file.
							</li>	
						</ol>
                    </td>
                </tr>
                <tr> 
                    <td colspan="2" bgcolor="#AE9E8F" class="td1 td5 td2" style="background-color:#AE9E8F">
                        A properties file is accessed from the physical directory of the deckman.settings package. Enter the name of the property 
						file only, unless you would like to override the default directory with a manual entry. If the file cannot be found at the
						specified/default location with the name entered, it will be created there as a blank file.
                    </td>
                </tr>
                <tr>
                    <td class="td1 td5">
                        <b>PROPERTIES FILE NAME:</b> <i>(if you leave out the path, the default directory for locating the properties file will be:<br>
						<%=(initialization.getDefaultInitializationFilePath())%>)<br>
                        <input type="text" class="textbox1" id="txtFileName" name="propertyfilename" onkeypress="if(event.keyCode == 13){ document.forms[0].txtSubmit.click() };"><br>
                        <b>NOTE:</b> After submitting this page, all pages in the website will operate against properties stored in the property file named above</i>
                    </td>
                </tr>
                <tr> 
                    <td colspan="2" align="right" bgcolor="#AE9E8F" class="td1 td5 td2" style="background-color:#AE9E8F">
                        <input type="button" class="button1" style="cursor: hand;" id="txtSubmit" value="submit" accesskey="s" onclick="javascript: document.forms[0]['txtFileName'].value ? document.forms[0].submit() : alert('please enter a file name');">
                    </td>
                </tr>
            </table>
        </form>
		<script language="javascript">
			try{
				document.forms[0].txtFileName.focus();
			}
			catch(e){ /* do nothing */ }
		</script>

    </center>
   </body>
</html>
