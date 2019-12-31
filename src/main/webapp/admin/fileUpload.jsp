<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@ page import="deckman.images.upload.*, deckman.settings.*, java.util.*, org.apache.commons.fileupload.servlet.*, com.warren.logging.*, com.warren.logging.logs.*" %>
<%@ include file="/admin/session_potential_router.jsp" %>

<%
        /**
         * NOTE: Safari will not process any callback functionality after this page has been
         * submitted and waiting for a response. However, it will handle ajax responses if
         * they are done synchronously.
         */
        boolean bSafari = request.getHeader("User-Agent").toUpperCase().contains("SAFARI");
        boolean bAjaxAsync = !bSafari; // setting default
        String sAjaxMethod = "get"; // setting default
        String sAjaxURL = "fileUploadProgress";

        boolean bSimulate = false;
        boolean bDisplayMeter = false;
        String sSimulateFileName = "";
        String sSimulateFileSize = "";
        String sSimulateInterval = "";
        String sSimulateChunk = "";
        admin_fileupload codeBehind = new admin_fileupload();
        ArrayList aList = codeBehind.getUploadHistory(session, request);
        String sTextArea = "<textarea id='taMessages' name='messages' cols=70 rows=10 style='height:170px;' class='textbox1'></textarea>";
        if (ServletFileUpload.isMultipartContent((HttpServletRequest) request)) {

            uploader loader = codeBehind.getUploader(session, request, response, aList);
            uploader.parseResult parseResult = codeBehind.parseRequest(loader, aList, session);
            if (parseResult.getResultType() == uploader.parseResult.PARSE_SUCCESS) {
                bDisplayMeter = true;

                String sType = loader.getMultiPartFormFieldParameterValue("uploadType");
                if (sType != null) {
                    bSimulate = sType.equalsIgnoreCase("SIMULATE");
                }

                String sAjaxAsync = loader.getMultiPartFormFieldParameterValue("AjaxAsync");
                if(sAjaxAsync == null){
                    bAjaxAsync = !bSafari;
                }

                sAjaxMethod = loader.getMultiPartFormFieldParameterValue("AjaxMethod");
                if(sAjaxMethod == null){
                    sAjaxMethod = "get";
                }
                
                if (bSimulate) {
                    sSimulateFileName = loader.getMultiPartFormFieldParameterValue("simulate_name");
                    if (sSimulateFileName == null) {
                        sSimulateFileName = "";
                    }

                    sSimulateFileSize = loader.getMultiPartFormFieldParameterValue("simulate_size");
                    if (sSimulateFileSize == null) {
                        sSimulateFileSize = "";
                    }

                    sSimulateInterval = loader.getMultiPartFormFieldParameterValue("simulate_interval");
                    if (sSimulateInterval == null) {
                        sSimulateInterval = "";
                    }

                    sSimulateChunk = loader.getMultiPartFormFieldParameterValue("simulate_chunk");
                    if (sSimulateChunk == null) {
                        sSimulateChunk = "";
                    }
                }

                uploader.uploadResult uploadResult = codeBehind.upload(loader, aList, session, bSimulate, sSimulateFileName);
                uploader.clearUploadProgress(session);
            }
        }

%>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta http-equiv="pragma" content="nocache">
        <meta http-equiv="expires" content="Mon, 8 Aug 2006 10:00:00 GMT">
        <meta http-equiv="Cache-Control" content="no-cache">

        <title>Deckman file upload</title>
        <link href="styles.css" rel="stylesheet" type="text/css">
        <script language="javascript">

            var request;
            var requestTimer;
            var MAX_WAITING_TIME = 60000;   // wait for a minute

            function useAJAXforFileUploadProgress(){
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

                var bAsync = document.getElementById("rdoAjaxAsync").checked;
                var sMethod = document.getElementById("rdoAjaxPost").checked ? 'post' : 'get';
                var sAjaxUrl = document.getElementById("txtAjaxUrl").value;

                //var bIE7 = sBrowser.indexOf("MSIE 7") != -1;
                //bAsync = bIE7 ? false : bAsync;

                request = getXMLHttpRequestObject();

                // timeout argument only added to make URL unique so as to prevent caching
                var sURL = sAjaxUrl + "?type=LAST&timestamp=" + new Date().getTime();
                var sBody = null;

                request.open(sMethod, sURL, bAsync);
                if(sMethod == 'post'){
                    request.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
                    sBody = "type=LAST";
                }
 
                if(bAsync){
                    request.onreadystatechange = animateProgressMeter;
                }
                    
                requestTimer = window.setTimeout(function(){
                    request.abort();
                    alert("upload aborted due to AJAX timeout with server.");
                }, MAX_WAITING_TIME);

                request.send(sBody);

                if(!bAsync){
                    animateProgressMeter();
                }
            }


            function getXMLHttpRequestObject(){
                var xhr;
                if(window.XMLHttpRequest){  // ie 7 and above, non-ie browsers
                    xhr = new XMLHttpRequest();
                }
                else if(window.ActiveXObject){  //IE browsers
                    try{
                        // ie 6 (will work with ie 7 and above if allowed)
                        xhr = new ActiveXObject("Msxml2.XMLHTTP");
                    }
                    catch(e){
                        // ie 5.5 and below
                        xhr = new ActiveXObject("Microsoft.XMLHTTP");
                    }
 
                    if(!xhr){
                        alert("no ActiveXObject");
                    }
                }
                else{
                    alert("you browser does not support the necessary components to drive a file upload progress meter");                    
                }
                
                return xhr;
                
            }


            var iAJAX_counter = 1;
            function animateProgressMeter(){
                var bRepeatAJAX = true;
                if(request.readyState == 4){    //loaded state
                    window.clearTimeout(requestTimer);
                    var sRetval = request.responseText;
                    var iStatus = request.status;

                    updateLatest10Messages((iAJAX_counter++), iStatus, sRetval);

                    if(request.status == 200){  //ok status

                        if(/^\d+,\d+,\d+$/.test(sRetval)){
                            aRetval = sRetval.split(',');
                            sItem = aRetval[0];
                            sBytesRead = aRetval[1];
                            sContentLength = aRetval[2];
                            if(sBytesRead == sContentLength){
                                bRepeatAJAX = false;
                            }
                            else{
                                growBar(sBytesRead, sContentLength);
                            }
                        }
                        else{
                            //display sRetval as an error message somewhere near the progress meter.
                        }
                    }

                    request.abort();    // resets the XMLHttpRequest/ActiveXObject
                    request = null;
                    if(bRepeatAJAX) window.setTimeout("useAJAXforFileUploadProgress()", 1000);
                }
            }


            function growBar(sBytesRead, sContentLength){

                function relabel(fAmt, oLabel){
                    var iUnit = fAmt>1000000?1000000:1000;
                    sUnit = iUnit==1000 ? ' KB' : ' MB';
                    sLen = (fAmt/iUnit).toString();
                    if(/\./.test(sLen)) sLen = sLen.split('.')[0];
                    oLabel.innerHTML = sLen + sUnit;
                }

                var oContentLength = document.getElementById('tdContentLength');
                var oMiddle = document.getElementById('tdMiddle');
                var oBytesRead = document.getElementById('tdBytesRead');

                if(oBytesRead != null){
                    if(sBytesRead == sContentLength){
                        oMiddle.innerHTML = 'done';
                    }
                    else{
                        oMiddle.innerHTML = 'of';

                        var fLength = parseFloat(sContentLength);
                        relabel(fLength, oContentLength);

                        var fRead = parseFloat(sBytesRead);
                        relabel(fRead, oBytesRead);

                        var bar = document.getElementById('divBar');
                        var sPercent = (parseInt((fRead/fLength)*100)).toString() + '%';
                        bar.style.width = sPercent;
                        window.status = sPercent;
                    }
                }
            }


            /* this function maintains the latest 10 messages returned from the server via AJAX, giving the taMessages
                       textarea the appearance of that it is scrolling the messages upward as the come in */
            function updateLatest10Messages(iCounter, iStatus, sMsg){
                var aSubMsgs = sMsg.split(',');
                if(aSubMsgs.length == 3){
                    sMsg = iCounter ;
                    sMsg += (')  status=' + iStatus);
                    sMsg += ('  item=' + aSubMsgs[0]);
                    sMsg += ('  ' + aSubMsgs[1] + ' of ');
                    sMsg += (aSubMsgs[2] + ' bytes uploaded');
                }

                var taMsgs = document.forms[0]['taMessages'];
                var sMsgs = taMsgs.value;
                var aMsgs = sMsgs.split('\n');
                if(aMsgs.length >= 10){
                    aMsgs.shift();
                }
                aMsgs.push(sMsg);
                sMsgs = aMsgs.join('\n');
                taMsgs.value = sMsgs;
            }
        </script>
    </head>


    <body class="body1">
        <center>
            <jsp:include page="banner.jsp">
                <jsp:param name="banner_selection" value="fileUpload" />
            </jsp:include>
            <form enctype="multipart/form-data" action="fileUpload.jsp" method="post">
                <table cellpadding=6 cellspacing=0 width="800px">
                    <tr>
                        <td colspan="2" class="td1 td5 td2">FILE DETAILS (4)</td>
                    </tr>
                    <tr id="trFileName" style="display:<%=(bSimulate ? "none" : "")%>;">
                        <td colspan="2" class="td1 td5">
                            specify a file for upload<br>
                            <input type="file" id="txtFile" name="file" style="width:600px;" class="textbox1">
                        </td>
                    </tr>
                    <tr id="trSimulateParameters" style="display:<%=(bSimulate ? "" : "none")%>;">
                        <td colspan="2" class="td1 td5">
                            <table cellspacing="0" cellpadding="0" style="width:100%" class="table1">
                                <tr>
                                    <td colspan="4" align="center" class="td3">
                                        <table cellpadding="0" cellspacing="0" style="width:100%" class="table1">
                                            <tr>
                                                <td align="center" class="td1" style="width:100%; padding:2px; border-bottom-style:solid; border-bottom-width:1pt; border-bottom-color:white;">
                                                    <b>Simulated Upload Parameters</b>
                                                </td>
                                                <td style="padding:8px;" rowspan="2" class="td1">
                                                    <input type="button" value="use defaults " class="fontA font2" style="cursor:hand;" onclick="javascript:
                                                            var f = document.forms[0];
                                                        f.txtName.value = 'myuploadfile.txt';
                                                        f.cbxSize.value = '20000000';
                                                        f.cbxInterval.value = '500';
                                                        f.cbxChunk.value = '1000000';
                                                    ">
                                                </td>
                                            </tr>
                                            <tr>
                                                <td class="td1" style="padding:2px;">&nbsp;</td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                                <tr valign="bottom">
                                    <td class="td1">File Name</td>
                                    <td class="td1">Size (mb)</td>
                                    <td class="td1">Interval (ms)</td>
                                    <td class="td1">Chunk per Interval</td>
                                </tr>
                                <tr>
                                    <td class="td1">
                                        <input type="text" id="txtName" name="simulate_name" value="<%=sSimulateFileName%>" style="width:250px;" class="textbox2">
                                    </td>
                                    <td class="td1">
                                        <select id="cbxSize" name="simulate_size">
                                            <option value="" <%if (sSimulateFileSize.equals("")) {
            out.print("SELECTED");
        }%>><- Select One -></option>
                                            <option value="1000000" <%if (sSimulateFileSize.equals("1000000")) {
            out.print("SELECTED");
        }%>>1 MB</option>
                                            <option value="2000000" <%if (sSimulateFileSize.equals("2000000")) {
            out.print("SELECTED");
        }%>>2 MB</option>
                                            <option value="5000000" <%if (sSimulateFileSize.equals("5000000")) {
            out.print("SELECTED");
        }%>>5 MB</option>
                                            <option value="10000000" <%if (sSimulateFileSize.equals("10000000")) {
            out.print("SELECTED");
        }%>>10 MB</option>
                                            <option value="20000000" <%if (sSimulateFileSize.equals("20000000")) {
            out.print("SELECTED");
        }%>>20 MB</option>
                                            <option value="50000000" <%if (sSimulateFileSize.equals("50000000")) {
            out.print("SELECTED");
        }%>>50 MB</option>
                                            <option value="100000000" <%if (sSimulateFileSize.equals("100000000")) {
            out.print("SELECTED");
        }%>>100 MB</option>
                                            <option value="200000000" <%if (sSimulateFileSize.equals("200000000")) {
            out.print("SELECTED");
        }%>>200 MB</option>
                                            <option value="500000000" <%if (sSimulateFileSize.equals("500000000")) {
            out.print("SELECTED");
        }%>>500 MB</option>
                                            <option value="1000000000" <%if (sSimulateFileSize.equals("1000000000")) {
            out.print("SELECTED");
        }%>>1 GB</option>
                                        </select>
                                    </td>
                                    <td class="td1">
                                        <select id="cbxInterval" name="simulate_interval">
                                            <option value="" <%if (sSimulateInterval.equals("")) {
            out.print("SELECTED");
        }%>><- Select One -></option>
                                            <option value="100" <%if (sSimulateInterval.equals("100")) {
            out.print("SELECTED");
        }%>>100 Milsec</option>
                                            <option value="200" <%if (sSimulateInterval.equals("200")) {
            out.print("SELECTED");
        }%>>200 Milsec</option>
                                            <option value="500" <%if (sSimulateInterval.equals("500")) {
            out.print("SELECTED");
        }%>>500 Milsec</option>
                                            <option value="1000" <%if (sSimulateInterval.equals("1000")) {
            out.print("SELECTED");
        }%>>1 Second</option>
                                            <option value="2000" <%if (sSimulateInterval.equals("2000")) {
            out.print("SELECTED");
        }%>>2 Second</option>
                                            <option value="5000" <%if (sSimulateInterval.equals("5000")) {
            out.print("SELECTED");
        }%>>5 Second</option>
                                            <option value="10000" <%if (sSimulateInterval.equals("10000")) {
            out.print("SELECTED");
        }%>>10 Second</option>
                                        </select>
                                    </td>
                                    <td class="td1">
                                        <select id="cbxChunk" name="simulate_chunk">
                                            <option value="" <%if (sSimulateChunk.equals("")) {
            out.print("SELECTED");
        }%>><- Select One -></option>
                                            <option value="1000" <%if (sSimulateChunk.equals("1000")) {
            out.print("SELECTED");
        }%>>1 KB</option>
                                            <option value="2000" <%if (sSimulateChunk.equals("2000")) {
            out.print("SELECTED");
        }%>>2 KB</option>
                                            <option value="5000" <%if (sSimulateChunk.equals("5000")) {
            out.print("SELECTED");
        }%>>5 KB</option>
                                            <option value="10000" <%if (sSimulateChunk.equals("10000")) {
            out.print("SELECTED");
        }%>>10 KB</option>
                                            <option value="20000" <%if (sSimulateChunk.equals("20000")) {
            out.print("SELECTED");
        }%>>20 KB</option>
                                            <option value="50000" <%if (sSimulateChunk.equals("50000")) {
            out.print("SELECTED");
        }%>>50 KB</option>
                                            <option value="100000" <%if (sSimulateChunk.equals("100000")) {
            out.print("SELECTED");
        }%>>100 KB</option>
                                            <option value="333000" <%if (sSimulateChunk.equals("333000")) {
            out.print("SELECTED");
        }%>>333 KB</option>
                                            <option value="500000" <%if (sSimulateChunk.equals("500000")) {
            out.print("SELECTED");
        }%>>500 KB</option>
                                            <option value="1000000" <%if (sSimulateChunk.equals("1000000")) {
            out.print("SELECTED");
        }%>>1 MB</option>
                                            <option value="2000000" <%if (sSimulateChunk.equals("2000000")) {
            out.print("SELECTED");
        }%>>2 MB</option>
                                            <option value="5000000" <%if (sSimulateChunk.equals("5000000")) {
            out.print("SELECTED");
        }%>>5 MB</option>
                                            <option value="10000000" <%if (sSimulateChunk.equals("10000000")) {
            out.print("SELECTED");
        }%>>10 MB</option>
                                            <option value="20000000" <%if (sSimulateChunk.equals("20000000")) {
            out.print("SELECTED");
        }%>>20 MB</option>
                                            <option value="50000000" <%if (sSimulateChunk.equals("50000000")) {
            out.print("SELECTED");
        }%>>50 MB</option>
                                            <option value="100000000" <%if (sSimulateChunk.equals("100000000")) {
            out.print("SELECTED");
        }%>>100 MB</option>
                                            <option value="200000000" <%if (sSimulateChunk.equals("200000000")) {
            out.print("SELECTED");
        }%>>200 MB</option>
                                        </select>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>


                    <tr id="trAjaxParameters" style="display:none;">
                        <td colspan="2" class="td1 td5" style=" padding-top: 20px;">
                            <table cellspacing="0" cellpadding="0" style="width:100%;" class="table1">
                                <tr>
                                    <td colspan="4" align="center" class="td3">
                                        <table cellpadding="0" cellspacing="0" style="width:100%" class="table1">
                                            <tr>
                                                <td align="center" class="td1" style="width:100%; padding:2px; border-bottom-style:solid; border-bottom-width:1pt; border-bottom-color:white;">
                                                    <b>AJAX Parameters</b>
                                                </td>
												<td class="td1" style="padding:2px; border-bottom-style:solid; border-bottom-width:1pt; border-bottom-color:white;"><div style="width:110px; display:block;">&nbsp;</div></td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                                <tr valign="bottom">
                                    <td class="td1" style="vertical-align:bottom;">Ajax URL</td>
                                    <td class="td1" colspan=3 rowspan=2>
                                        <table cellpadding="0" cellspacing="0">
                                            <tr>
                                                <td class="td1" colspan="2" style="text-align:center; padding:4px; border-bottom-style:solid; border-bottom-width:1px; border-bottom-color:white;">Make AJAX requests</td>
                                                <td class="td1" style="width:50px;">&nbsp;</td>
                                                <td class="td1" colspan="2" style="text-align:center; padding:4px; border-bottom-style:solid; border-bottom-width:1px; border-bottom-color:white;">AJAX request methods</td>
                                            </tr>
                                            <tr>
                                                <td class="td1" style="text-align:right;">Asynchronously</td>
                                                <td class="td1">
                                                    <input type="radio" name="AjaxAsync" id="rdoAjaxAsync" value="true"
                                                           <%if(bAjaxAsync) out.print(" checked");%>>
                                                </td>
                                                <td class="td1">&nbsp;</td>
                                                <td class="td1" style="text-align:right;">Get</td>
                                                <td class="td1">
                                                    <input type="radio" name="AjaxMethod" id="rdoAjaxGet" value="get"
                                                        <%if(sAjaxMethod.equalsIgnoreCase("get")) out.print(" checked");%>>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td class="td1" style="text-align:right;">Synchronously</td>
                                                <td class="td1">
                                                    <input type="radio" name="AjaxAsync" id="rdoAjaxSync" value="false"
                                                        <%if(!bAjaxAsync) out.print(" checked");%>>
                                                </td>
                                                <td class="td1">&nbsp;</td>
                                                <td class="td1" style="text-align:right;">Post</td>
                                                <td class="td1">
                                                    <input type="radio" name="AjaxMethod" id="rdoAjaxPost"
                                                        <%if(sAjaxMethod.equalsIgnoreCase("post")) out.print(" checked");%>>
                                                </td>
                                            </tr>
                                        </table>
									</td>
                                </tr>
                                <tr>
                                    <td class="td1" style="vertical-align:top;">
                                        <input type="text" id="txtAjaxUrl" name="ajax_url" value="<%=sAjaxURL%>" style="width:250px;" class="textbox2">
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>

                    <tr>
                        <td class="td1 td5">
                            <table width="100%">
                                <tr>
                                    <td align="right" class="td1" valign="top">
                                        <table cellspacing="0" cellpadding="0" style="display:inline;">
                                            <tr>
                                                <td align="right" class="td1">default AJAX configuration</td>
                                                <td class="td1">
                                                    <input type="radio" id="rdoAjaxDefaults" name="AjaxConfig" value="default" CHECKED
                                                       onclick="javascript:

                                                           var rdo_AjaxAsync = document.getElementById('rdoAjaxAsync');
                                                           var rdo_AjaxSync = document.getElementById('rdoAjaxSync');
                                                           var rdo_AjaxGet = document.getElementById('rdoAjaxGet');
                                                           var rdo_AjaxPost = document.getElementById('rdoAjaxPost');
                                                           var txtAjaxURL = document.getElementById('txtAjaxUrl');
                                                           
                                                           rdo_AjaxAsync.checked = <%out.print(bAjaxAsync?"true":"false");%>;
                                                           rdo_AjaxSync.checked = <%out.print(bAjaxAsync?"false":"true");%>;
                                                           rdo_AjaxGet.checked = true;
                                                           rdo_AjaxPost.checked = false;
                                                           txtAjaxURL.value = '<%=sAjaxURL%>';

                                                           document.getElementById('trAjaxParameters').style.display = 'none';
                                                           this.checked = true;
                                                    ">
                                                </td>
                                            </tr>
                                            <tr>
                                                <td align="right" class="td1">custom AJAX configuration</td>
                                                <td class="td1">
                                                    <input type="radio" id="rdoAjaxCustom" name="AjaxConfig" value="custom"
                                                       onclick="javascript:
                                                           document.getElementById('trAjaxParameters').style.display = '';
                                                           this.checked = true;
                                                    ">
                                                </td>
                                            </tr>
                                        </table>
                                        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                                        <table cellspacing="0" cellpadding="0" style="display:inline;">
                                            <tr>
                                                <td align="right" class="td1">upload</td>
                                                <td class="td1">
                                                    <input type="radio" id="rdoUpload1" name="uploadType" value="upload" <%=(bSimulate ? "" : "CHECKED")%>
                                                       onclick="javascript:
                                                           document.getElementById('txtName').value = '';
                                                           document.getElementById('cbxSize').value = '';
                                                           document.getElementById('cbxInterval').value = '';
                                                           document.getElementById('cbxChunk').value = '';
                                                           document.getElementById('trSimulateParameters').style.display = 'none';
                                                           document.getElementById('trFileName').style.display = '';
                                                           this.checked = true;
                                                    ">
                                                </td>
                                            </tr>
                                            <tr>
                                                <td align="right" class="td1">simulated upload</td>
                                                <td class="td1">
                                                    <input type="radio" id="rdoUpload2" name="uploadType" value="simulate" <%=(bSimulate ? "CHECKED" : "")%>
                                                       onclick="javascript:
                                                           document.getElementById('txtFile').value = '';
                                                           document.getElementById('trSimulateParameters').style.display = '';
                                                           document.getElementById('trFileName').style.display = 'none';
                                                           this.checked = true;
                                                    ">
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                    <td class="td1">
                                        <input type="button" value="UPLOAD" style="height:30px; width:100%; cursor:hand; font-weight:bold;" class="fontB" onclick="javascript:
                                            document.forms[0].taMessages.value = '';
                                            document.getElementById('divMeter').style.display = '';
                                            document.forms[0].submit();
                                            useAJAXforFileUploadProgress();
                                        ">
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>


                <div id="divMeter" style="display:<%=(bDisplayMeter ? "" : "none")%>;">
                    <br><br>
                    <table cellpadding="6" cellspacing="0" width="800px">
                        <tr>
                            <td colspan=3 class="td1 td5 td2">
                                UPLOAD PROGRESS:
                            </td>
                        </tr>
                        <tr>
                            <td class="td1 td5">
                                <table width="400px">
                                    <tr>
                                        <td id="tdBytesRead" align="right" class="td1 td4" width="175"></td>
                                        <td id="tdMiddle" align="center" class="td1 td4" width="30px">done</td>
                                        <td id="tdContentLength" align="left" class="td1 td4" width="185"></td>
                                    </tr>
                                    <tr>
                                        <td colspan=3>
                                            <!--<div style="width:400px; border-style:solid; border-width:1px; border-color:#0099CC; padding:2px; background-color:white;">-->
                                            <div style="width:400px; padding:2px; background-color:white; height:10px;" class="textbox2">
                                                <div id="divBar" style="height:100%; width:<%=(bDisplayMeter ? "100" : "0")%>%; background-color:blue"></div>
                                            </div>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </div>

                <br><br>
                <table cellpadding="6" cellspacing="0" width="800px">
                    <tr>
                        <td class="td1 td5 td2">MESSAGES</td>
                    </tr>
                    <tr>
                        <td class="td1 td5"><% out.println(sTextArea);%></td>
                    </tr>
                </table>


                <br><br>
                <table cellpadding="0" cellspacing="0" width="800px">
                    <tr style="padding:6">
                        <td colspan=2 class="td1 td5 td2">
                            <% out.println("FILE UPLOAD HISTORY:");%>
                        </td>
                    </tr>
                    <% codeBehind.printUploadHistory(session, request, out, aList);%>
                </table>

            </form>
        </center>
    </body>
    <HEAD>
        <META HTTP-EQUIV="PRAGMA" CONTENT="NO-CACHE">
    </HEAD>
</html>
