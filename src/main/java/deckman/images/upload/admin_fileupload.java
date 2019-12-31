/*
 * admin_fileupload.java
 *
 * Created on September 28, 2008, 4:19 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package deckman.images.upload;

import deckman.images.unzip.unzipper;
import deckman.settings.*;
import java.util.*;
import deckman.utils.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;


public class admin_fileupload {

    /** Creates a new instance of admin_fileupload */
    public admin_fileupload() {
    }



    public ArrayList getUploadHistory(HttpSession session, HttpServletRequest request) {
        /*This function retrieves a list of files that have been uploaded in this session from a session variable*/
        ArrayList aList = null;
        if (request.getMethod().equalsIgnoreCase("POST")) {
            Object oList = session.getAttribute("uploadedfiles");
            if (oList == null) {
                aList = new ArrayList();
            }
            else {
                aList = (ArrayList) oList;
            }
        }
        return aList;
    }



    public uploader getUploader(HttpSession session, ServletRequest request, ServletResponse response, ArrayList aList) {
        //get the parameters for managing the file upload stored on disk via the initialization object
        initialization init = initialization.getInstance(session);
        String sUploadInPath = init.getPropertyAsString("UploadInFilePath");
        String sUploadOutZipFilePath = init.getPropertyAsString("UploadOutZipFilePath");	// assume the uploaded file is a zip file.

        //upload the file
        uploader loader = new uploader(session, request, response, sUploadInPath, sUploadOutZipFilePath);

        //if the file is not a zipfile, transfer it to
        return loader;
    }



    public uploader.uploadResult upload(uploader loader, ArrayList aList, HttpSession session, boolean bSimulate, String sSimulateFileName) {
        //get the parameters for managing the file upload stored on disk via the initialization object
        initialization init = initialization.getInstance(session);
        String sUploadOutImageFilePath = init.getPropertyAsString("UploadOutImageFilePath");
        String sFullsizeDir = init.getPropertyAsString("FullsizeFilePath");

        if (sUploadOutImageFilePath.endsWith(java.io.File.separator)) {
            sUploadOutImageFilePath = sUploadOutImageFilePath.substring(0, sUploadOutImageFilePath.length() - 1);
        }
        if (sFullsizeDir.endsWith(java.io.File.separator)) {
            sFullsizeDir = sFullsizeDir.substring(0, sFullsizeDir.length() - 1);
        }
        boolean bEnsureUniqueNames = sUploadOutImageFilePath.equalsIgnoreCase(sFullsizeDir);

        //get the parameters for managing the file upload stored on disk via the initialization object
        uploader.uploadResult uploadResult = loader.upload(bSimulate, sSimulateFileName);

        //add the results of the upload to the session in the aList ArrayList
        aList.add(uploadResult);

        int iResultType = uploadResult.getResultType();
        if (iResultType == result.UPLOAD_SUCCESS || iResultType == result.UPLOAD_SUCCESS_PARTIAL) {
            /**
             * if any of the uploaded files are not zip files, remove them from the unzipping directory and place them in
             * the reimaging in directory. This is where files will end up anyway, once they are unzipped. The reimaging
             * directory could be the same directory as the final storage location for the websites fullsize images if the
             * files within can be searched through for only those that do not have a thumbnail (this is done in cleanup.jsp)
             */
            ArrayList movedFiles = codeLib.moveDirectoryContents(loader.getOutDir(), sUploadOutImageFilePath, unzipper.getNonZipFileFilter(), bEnsureUniqueNames);
            boolean bOnlyZipFilesRemain = true; // assume all qualifying files were moved successfully
            Iterator iter = movedFiles.iterator();
            while (iter.hasNext()) {
                String[] details = (String[]) iter.next();
                if (details[1] == null) {
                    bOnlyZipFilesRemain = false;
                }
            }
            if (!bOnlyZipFilesRemain) {
                /**
                 * Do nothing yet. Have another go at transferring these files once all files have been extracted from the zip files.
                 * If any files fail to be moved over to the reimaging in directory then, the unzipper result instance will announce it.
                 */
            }
        }

        session.setAttribute("uploadedfiles", aList);
        return uploadResult;
    }



    public uploader.parseResult parseRequest(uploader loader, ArrayList aList, HttpSession session) {
        uploader.parseResult parseResult = loader.parseRequest();

        if (parseResult.getResultType() != parseResult.PARSE_SUCCESS) {
            aList.add(parseResult);
            session.setAttribute("uploadedfiles", aList);
        }

        return parseResult;
    }



    public void printUploadHistory(HttpSession session, HttpServletRequest request, JspWriter out, ArrayList aList) throws java.io.IOException {
        if (aList != null) {
            ListIterator iter = aList.listIterator();
            int i = 1;
            while (iter.hasNext()) {
                result theResult = (result) (iter.next());
                String sResult1 = theResult.getClass().getName();

                String sResult2 = "";
                if (theResult instanceof uploader.parseResult) {
                    sResult2 = "uploader.parseResult";
                }
                if (theResult instanceof uploader.uploadResult) {
                    sResult2 = "uploader.uploadResult";
                }

                int iResultType = theResult.getResultType();

                if (theResult instanceof uploader.uploadResult) {
                    String sColor = iResultType == result.UPLOAD_SUCCESS ? "" : " fontRed";

                    //print the result description (failure/success code will be used to obtain the desciption)
                    out.println("<tr><td class='td1 td5" + sColor + "' colspan=2>&nbsp;</td></tr>");
                    out.println("<tr><td class='td1" + sColor + "' colspan=2>&nbsp;&nbsp;&nbsp;<b>" + (i++) + "</b></td></tr>");
                    out.println("<tr><td class='td1" + sColor + "' align='right'>");
                    out.print("<b>upload result:</b>&nbsp;&nbsp;&nbsp;</td><td class='td1" + sColor + "'>");
                    HashMap mymap = result.RESULT_DESCRIPTIONS();
                    Integer myint = Integer.valueOf(iResultType);
                    String sResultDesc = (String) (mymap.get(myint));
                    out.print(sResultDesc);
                    out.println("</td></tr>");

                    //print the uploaded file origin path
                    String sFileInSpec = theResult.getFileInSpec();
                    out.println("<tr><td class='td1" + sColor + "' align='right'><b>file being uploaded:</b>&nbsp;&nbsp;&nbsp;</td><td class='td1" + sColor + "'>");
                    out.println(sFileInSpec);
                    out.println("</td></tr>");

                    //print the file temporary upload point
                    String sFileOutTempSpec = theResult.getFileOutTempDir();
                    out.println("<tr><td class='td1" + sColor + "' align='right'><b>file temporary upload location:</b>&nbsp;&nbsp;&nbsp;</td><td class='td1" + sColor + "'>");
                    out.println(sFileOutTempSpec);
                    out.println("</td></tr>");

                    //print the uploaded file destination
                    String sFileOutSpec = theResult.getFileOutSpec();
                    out.println("<tr><td class='td1" + sColor + "' align='right'><b>file being uploaded to:</b>&nbsp;&nbsp;&nbsp;</td><td class='td1" + sColor + "'>");
                    out.println(sFileOutSpec);
                    out.println("</td></tr>");

                    //print the uploaded file size
                    String sFileSize = String.valueOf(theResult.getFileSize());
                    out.println("<tr><td class='td1" + sColor + "' align='right'><b>file size:</b>&nbsp;&nbsp;&nbsp;</td><td class='td1" + sColor + "'>");
                    out.println(sFileSize + " bytes");
                    out.println("</td></tr>");
                    out.println("<tr><td class='td1" + sColor + "' colspan=2>&nbsp;</td></tr>");
                }
                else if (theResult instanceof uploader.parseResult) {
                    String sColor = iResultType == result.PARSE_SUCCESS ? "" : " fontRed";

                    //print the result description (failure/success code will be used to obtain the description)
                    out.println("<tr><td class='td1 td5" + sColor + "' colspan=2>&nbsp;</td></tr>");
                    out.println("<tr><td class='td1" + sColor + "' colspan=2>&nbsp;&nbsp;&nbsp;<b>" + (i++) + "</b></td></tr>");
                    out.println("<tr><td class='td1" + sColor + "' align='right'>");
                    out.print("upload result:</td><td class='td1" + sColor + "'>");
                    HashMap mymap = result.RESULT_DESCRIPTIONS();
                    Integer myint = Integer.valueOf(iResultType);
                    String sResultDesc = (String) (mymap.get(myint));
                    out.print(sResultDesc);
                    out.println("</td></tr>");

                    //print the file temporary upload point
                    String sFileOutTempSpec = theResult.getFileOutTempDir();
                    out.println("<tr><td class='td1" + sColor + "' align='right'><b>file temporary upload location:</b>&nbsp;&nbsp;&nbsp;</td><td class='td1" + sColor + "'>");
                    out.println(sFileOutTempSpec);
                    out.println("</td></tr>");

                    //print the uploaded file destination
                    String sFileOutSpec = theResult.getFileOutSpec();
                    out.println("<tr><td class='td1" + sColor + "' align='right'><b>file being uploaded to:</b>&nbsp;&nbsp;&nbsp;</td><td class='td1" + sColor + "'>");
                    out.println(sFileOutSpec);
                    out.println("</td></tr>");
                    out.println("<tr><td class='td1" + sColor + "' colspan=2>&nbsp;</td></tr>");

                    out.println("</tbody>");
                }
            }
        }
    }



    public static void main(String[] args) {
    }
}
