package deckman.images.reimage;

import java.util.*;
import java.util.zip.*;
import java.util.regex.*;
import deckman.utils.codeLib;

public class result implements java.io.Serializable {
    
        //This will be the intialized value of all numeric result properties of intrinsic type.
    public static final int VALUE_NOT_SET = 0;
    
        //postponement of reimaging a file can come from the following reasons:
    public static final int REIMAGE_CANCELLED_NOT_IMAGE = 10;
    public static final int REIMAGE_CANCELLED_NO_TARGET_DIR = 1;
    public static final int REIMAGE_FILE_NOT_FOUND = 5;
    public static final int REIMAGE_CANCELLED_FAILED_TO_CLEAR_TARGET_DIR = 13;
    
        //when reimaging a file starts, success or failure can be described with the following constants
        //That is, assuming cancelling everything on the first error has not been specified.
    public static final int REIMAGE_CANCELLED_ON_FIRST_ERROR = 8;
    public static final int REIMAGE_FAILED = 3;
    public static final int REIMAGE_SUCCESS = 4;
    
    private static final HashMap desc = new HashMap();
    public static final HashMap RESULT_DESCRIPTIONS(){
        if(desc.isEmpty()){
            desc.put(Integer.valueOf(REIMAGE_CANCELLED_NOT_IMAGE), "Invalid attempt to reimage a file that is not itself an image.");
            desc.put(Integer.valueOf(REIMAGE_CANCELLED_NO_TARGET_DIR), "The directory specified as the thumb (reimaged file) target location does not exist.");
            desc.put(Integer.valueOf(REIMAGE_CANCELLED_FAILED_TO_CLEAR_TARGET_DIR), "The directory specified as the target location of reimaged (thumb) files could not be cleared out in preparation for this reimage attempt");
            desc.put(Integer.valueOf(REIMAGE_FILE_NOT_FOUND), "The specified image file could not be found.");
            desc.put(Integer.valueOf(REIMAGE_FAILED), "An error occurred while reimaging an image file.");
            desc.put(Integer.valueOf(REIMAGE_SUCCESS), "The image file was reimaged successfully");
            desc.put(Integer.valueOf(REIMAGE_CANCELLED_ON_FIRST_ERROR), "An unspecified error occurred during reimaging.");
        }
        return desc;
    }
    
    
    private int iResultType = this.VALUE_NOT_SET;
    private String sImageFileInSpec = null;
    private String sThumbFileOutDir = null;
    private String sThumbShortName = null;
    private String sImageNewFullname = null;
    private String sImageType = null;
    private String sErrorDetail = null;
    private long lImageSize = 0;
    private long lThumbSize = 0;
    private ArrayList aZipExtractionResults = null;
    
    public result(){ }
        
    public result(String sImageFileInSpec, String sThumbFileOutDir){
        this.sImageFileInSpec = sImageFileInSpec;
        this.sThumbFileOutDir = sThumbFileOutDir;
    }
        
    public int getType(){ return iResultType; }    
    public void setType(int iType){ iResultType = iType; }
    
    public String getDescription(){
        if(iResultType == 0){
            return null;
        }
        else{
            return (String)result.RESULT_DESCRIPTIONS().get(new Integer(iResultType));
        }
    }
    
    public String getImageType(){ return sImageType; }    
    public void setImageType(String sType){ sImageType = sType; }
        
    public String getImageFileInSpec(){ return sImageFileInSpec; }    
    public void setImageFileInSpec(String sSpec){ sImageFileInSpec = sSpec; }
    
    public String getThumbFileOutDir(){ return sThumbFileOutDir; }    
    public void setThumbFileOutDir(String sName){ sThumbFileOutDir = sName; }
    
    public String getThumbShortName(){ return sThumbShortName; }    
    public void setThumbShortName(String sShortName){ sThumbShortName = sShortName; }
    
    public String getImageNewFullName(){ return sImageNewFullname; }    
    public void setImageNewFullName(String sFullname){ sImageNewFullname = sFullname; }
    
    public long getImageSize(){ return lImageSize; }
    public void setImageSize(long lSize){ lImageSize = lSize; }
    
    public long getThumbSize(){ return lThumbSize; }
    public void setThumbSize(long lSize){ lThumbSize = lSize; }
    
    public String getError(){ return sErrorDetail; }
    public void setError(String sDetail){ sErrorDetail = sDetail; }

    public static void main(String[] args){
        
        
    }
}
