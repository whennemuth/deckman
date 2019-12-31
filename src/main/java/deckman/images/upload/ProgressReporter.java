/*
 * ProgressReporter.java
 *
 * Created on October 25, 2007, 11:00 PM
 *
 */

package deckman.images.upload;

import java.util.*;
import com.warren.logging.StaticLog;
import com.warren.logging.LogEntry;

    /*An instance of this class is stored in a session variable (LISTENER). The update method of this instance is called
      repetitively by the ServletFileUpload instance that is uploading a file to report the lastest information on how much of
      the file is uploaded, size of file, etc. ProgressReporter is a subclass of ProgressListener, extended to also accumulate
      these updates in an ArrayList and the lastest update in an HashMap. Typically, a servlet is trying to access this instance 
      to get this data to service repeating http requests via AJAX to power a progress meter that needs constant adustment to reflect 
      the progress of the upload during the interval between posting the form and receiving a response back reporting the upload 
      result (The web page along with the javascript that powers the AJAX lingers after the submit util the upload is complete and 
      response can be sent). The JSP page that uploads the file might also access this ArrayList in the same way to provide
      the history of the upload increments to supplement its report of upload completion/failure.*/
public class ProgressReporter implements org.apache.commons.fileupload.ProgressListener {
    private HashMap mLatestUpdate = null;
    private long lLastBytes = -1;
    
    public ProgressReporter() { }
    
    public void update(long lBytesRead, long lContentLength, int lItem) {
        try{
            if(lItem > 0){  // "0" means no items yet. (see javadoc for ProgressListener)
                if(lLastBytes == lBytesRead){
                    return;
                }
                else{
                    this.lLastBytes = lBytesRead;

                        //grow the history array
                    HashMap map = new HashMap();
                    map.put(new String("BytesRead"),        new Long(lBytesRead));
                    map.put(new String("ContentLength"),    new Long(lContentLength));
                    map.put(new String("Item"),             new Long(lItem));

                    mLatestUpdate = map;
                }
            }
        }
        catch(Exception e){
            StaticLog.log(e, LogEntry.EntryType.ERROR);
        }
    }
    
    public HashMap getLastestUpdate(){
        if(mLatestUpdate == null){
            mLatestUpdate = new HashMap();
            mLatestUpdate.put(uploader.NO_UPDATE_DATA, uploader.NO_UPDATE_DATA);
        }
        return mLatestUpdate;
    }
    
}
