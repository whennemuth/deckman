package deckman.images.upload;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.servlet.*;
import org.apache.commons.fileupload.disk.*;
import deckman.utils.codeLib;
import com.warren.logging.*;


public class uploader {
    public static final int PARSE_FAILURE = 1;
    public static final int PARSE_SUCCESS = 2;
    private String sOutDirPath = null;
    private String sTempDirPath = null;
    private HttpSession session = null;
    private ServletResponse response = null;
    private ServletRequest request = null;
    private List items = null;  //will remain null until request is parsed
    
    /**
     * Use this constructor to specify: the buffer directory for uploading files and the permanent directory
     * @param sTempDirPath the directory that file(s) for uploading will be temporarily uploaded until the upload is complete
     * @param sOutDirPath the directory that files(s) in the sTempDirPath directory will be transferred to once upload is complete. This is their permanent destination.
     */
    public uploader(HttpSession ssn, ServletRequest request, ServletResponse response, String sTempDirPath, String sOutDirPath){
        this.sOutDirPath = sOutDirPath;
        this.sTempDirPath = sTempDirPath;
        this.session = ssn;
        this.response = response;
        this.request = request;
    }
    
    public String getOutDir(){
        return sOutDirPath;
    }
    
    public parseResult parseRequest(){
        HttpServletRequest request = (HttpServletRequest)this.request;
        parseResult Result = new parseResult();
        if(ServletFileUpload.isMultipartContent(request)){
            File tempDir = new File(sTempDirPath);
            if(tempDir.exists()){
                    /* should be clear already because while ServletFileUpload will put a file in this temp directory (set by 
                     * you with the setRepository method) as the result of parsing a request object, it will automatically clear
                     * it out again once each DiskFileItem has been written out from the temp parsed file using the write method
                     * of the DiskFileItem object and the temp parsed file is garbage collected. Clear directory here in case an
                     * error had occurred somewhere during the last file upload and the temp file not garbage collected.
                     */
                codeLib.clearDirectory(tempDir);  
                DiskFileItemFactory factory = new DiskFileItemFactory();

                factory.setSizeThreshold(1000);
                factory.setRepository(tempDir);
                ServletFileUpload upload = new ServletFileUpload(factory);

                upload.setFileSizeMax(1000000000);  // 1GB
                upload.setSizeMax(1000000000); // 1GB
                ProgressReporter listener = new ProgressReporter();
                this.session.setAttribute("LISTENER", listener);    //put the file upload progress listener in a session variable
                upload.setProgressListener(listener);                

                try{
                    StaticLog.log("Start of multipart request parsing... ", LogEntry.EntryType.INFO);
                    items = upload.parseRequest(request);
                    StaticLog.log("End of multipart request parsing.", LogEntry.EntryType.INFO);
                    Result.setResultType(parseResult.PARSE_SUCCESS);
                }
                catch(FileUploadException e){
                    StaticLog.log(e, LogEntry.EntryType.ERROR);
                    Result.setResultType(parseResult.UPLOAD_CANCELLED_ON_FIRST_ERROR);
                }
                catch(Exception e){
                    StaticLog.log(e, LogEntry.EntryType.ERROR);
                    Result.setResultType(parseResult.UPLOAD_CANCELLED_ON_FIRST_ERROR);
                }
            }
            else{
                Result.setResultType(parseResult.PARSE_CANCELLED_NO_TEMP_DIR);
            }
        }
        else{
            Result.setResultType(parseResult.PARSE_CANCELLED_NOT_MULTIPART);
        }

        if(Result.getResultType() != parseResult.PARSE_SUCCESS){
            Result.setFileOutTempDir(this.sTempDirPath);                            
            Result.setFileOutSpec(this.sOutDirPath);
        }
        
        return Result;
    }
    
    public class uploadResult extends result implements Serializable { }
    public class parseResult extends result implements Serializable { }
    
    public uploadResult upload(){
        return upload(false, "");
    }
    
    public uploadResult upload(boolean bSimulate, String sSimulateFileName){
        uploadResult Result = new uploadResult();
        
        if(items == null){
            Result.setResultType(uploadResult.UPLOAD_CANCELLED_UNPARSED_REQUEST);
        }
        else{
            //if(!codeLib.clearDirectory(this.sOutDirPath)){
                /**
                 * Do nothing because getUniqueName will be applied to any DiskFileItem being uploaded, eliminating
                 * the risk of name conflicts when each is uploaded to the directory at sOutDirPath. 
                 * @todo It may be worth putting something here that causes a message to be displayed notifying about the 
                 * delete failure(s) because if the failure is repetitive, the sOutDirPath directory will grow unnessesarily.
                 */
            //}
            Iterator iter = items.iterator();
            while(iter.hasNext()){
                try{
                    FileItem item = (FileItem) iter.next();
                    if(item.isFormField()){
                        /**
                         * String sFldName = item.getFieldName();
                         * String sFldVal = item.getString();
                         */
                    }
                    else{
                        DiskFileItem ditem = (DiskFileItem)item;
                        long lFileSize = ditem.getSize();
                        Result.setFileSize(lFileSize);
                        
                        String sFileInSpec = ditem.getName();   // this will be the path the user selected in the file upload dialog box
                        Result.setFileInSpec(sFileInSpec);      // make the above a property of this instance.                           
                        String sName = bSimulate ? sSimulateFileName : codeLib.removePathlikePortionFromFileSpec(sFileInSpec); //get the shortname of the above
                        
                            /* The ServletFileUpload will put a file in this temp directory (set using the setRepository method) 
                             * with a name like: "upload__175e19f7_1183502f051__8000_00000063.tmp" as the result of parsing a 
                             * request object. It will automatically clear it out again once each DiskFileItem has been written 
                             * out to sOutDirPath from this temp parsed file using the write method of the DiskFileItem object 
                             * and the temp parsed file is garbage collected.
                             */
                        Result.setFileOutTempDir(this.sTempDirPath);                            
                        
                            // determine the target directory and file of the upload
                        String sFileOutName = codeLib.getUniqueFileName(sName, this.sOutDirPath);
                        String sFileOutSpec = this.sOutDirPath + File.separator + sFileOutName;
                        Result.setFileOutSpec(sFileOutSpec);
                        
                            // if not simulating an upload, write the file out as sFileOutSpec, completing the upload
                        File uploadedFile = new File(sFileOutSpec);
                        if(bSimulate){
                            if(simulateUpload()){
                                Result.setResultType(uploadResult.UPLOAD_SUCCESS);
                            }
                            else{
                                Result.setResultType(uploadResult.UPLOAD_CANCELLED_ON_FIRST_ERROR);
                            }
                        }
                        else{
                            ditem.write(uploadedFile);
                            Result.setResultType(uploadResult.UPLOAD_SUCCESS);
                        }
                    }
                }
                catch(FileUploadException e){
                    StaticLog.log(e, LogEntry.EntryType.ERROR);
                    e.printStackTrace();
                    Result.setResultType(uploadResult.UPLOAD_CANCELLED_ON_FIRST_ERROR);
                }
                catch(Exception e){
                    StaticLog.log(e, LogEntry.EntryType.ERROR);
                    e.printStackTrace();
                    Result.setResultType(uploadResult.UPLOAD_CANCELLED_ON_FIRST_ERROR);                    
                }
            }
        }
        
        return Result;
    }
    
    private boolean simulateUpload(){
        ProgressReporter reporter = (ProgressReporter)session.getAttribute("LISTENER");
        if(reporter == null){
            return false;
        }
        else{
            try{
                long lSize = Long.parseLong(this.getMultiPartFormFieldParameterValue("simulate_size"));                
                long lInterval = Long.parseLong(this.getMultiPartFormFieldParameterValue("simulate_interval"));
                long lChunk = Long.parseLong(this.getMultiPartFormFieldParameterValue("simulate_chunk"));
                for(long lBytesRead=0; lBytesRead<lSize; lBytesRead+=lChunk){
                    reporter.update(lBytesRead, lSize, 1);
                    Thread.sleep(lInterval);
                }
                return true;
            }
            catch(Exception e){
                return false;
            }
        }
    }
    
        /*Once a multi-part request has been parsed, its non-file parameters can be accessed through the resulting items List.
          This function iterates through each item in the list, skips file parameters, and returns a parameter value for any 
          parameter found whose name matches sParamName*/
    public String getMultiPartFormFieldParameterValue(String sParamName){
        String sParamVal = null;
        
        if(this.items == null){
            return null;
        }
        else{
            Iterator iter = null;
            iter = this.items.iterator();

            while(iter.hasNext()){
                FileItem item = (FileItem) iter.next();
                if(item.isFormField()){
                    if(item.getFieldName().equals(sParamName)){
                        sParamVal = item.getString();
                    }
                }
            }
        }
        
        return sParamVal;
    }
    
    public static final String NO_SESSION = "no session";
    public static final String NO_LISTENER = "no listener in session";
    public static final String NO_UPDATE_DATA = "no upload data yet, wait...";
    
        /*An instance of ProgressReporter is stored in a session variable (LISTENER). The update method of this instance is called
          repetitively by the ServletFileUpload instance that is uploading a file to report the lastest information on how much of
          the file is uploaded, size of file, etc. ProgressReporter is a subclass of ProgressListener, extended to also store the
          latest update in an HashMap. This function gets the pointer to the instance of ProgressReporter from the session variable
          and returns this HashMap. This function might be called by a servlet that is trying to return the data in response to an
          AJAX call made while the file is uploading (AJAX can still be used after a form POST to upload a file because the page
          remains until a response is received).*/
    public static HashMap getLatestUploadProgressUpdate(HttpSession session){

        if(session == null){
            HashMap map = new HashMap(1);
            map.put(NO_SESSION, NO_SESSION);
            return map;
        }
        else{
                /*get the ProgressReporter attributes set by the latest of repeating callbacks to its update method by the 
                  ServletFileUpload object (reference to ProgressReporter instance is stored in session variable LISTENER)*/
            ProgressReporter reporter = (ProgressReporter)session.getAttribute("LISTENER");
            if(reporter == null){
                HashMap map = new HashMap(1);
                map.put(NO_LISTENER, NO_LISTENER);
                return map;
            }
            else{
                return reporter.getLastestUpdate();
            }
        }
    }

    
    public static void clearUploadProgress(HttpSession session){
        session.setAttribute("LISTENER", null);
    }
        
    
    public static String getMultiPartRequestAsString(ServletRequest request){
            /*This function is for debugging purposes only. It will print out all form parameters and files found in a
             multipart request as a string. Sometimes it helps to see just what is in a request to debug any problems uploading files from it.*/
        StringBuffer s = new StringBuffer();
        try{
            BufferedReader reader = request.getReader();
            int i;
            while((i = reader.read()) >= 0){
                s.append((char)i);
            }
            reader.close();
            return s.toString();
        }
        catch(IOException e){
            return codeLib.getStackTraceAsString(e);
        }
    }
}
