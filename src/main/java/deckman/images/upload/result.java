package deckman.images.upload;

import java.util.*;

public class result implements java.io.Serializable {
    
        //a request object is parsed before any uploading takes place. The following problems can occur then:
    public static final int PARSE_CANCELLED_NOT_MULTIPART = 4;
    public static final int PARSE_CANCELLED_NO_TEMP_DIR = 5;
    public static final int PARSE_FAILED = 6;
    public static final int PARSE_SUCCESS = 7;
    
        //for all items in a multi-part form post, the file(s) portions collective success can be described with the following constants
    public static final int UPLOAD_CANCELLED_ON_FIRST_ERROR = 8;
    public static final int UPLOAD_CANCELLED_UNPARSED_REQUEST = 10;
    public static final int UPLOAD_SUCCESS = 11;
    public static final int UPLOAD_SUCCESS_PARTIAL = 12;
    
        //the result of an attempt to upload an individual file of one or more files in a multi-part post can be described with one of the following.
    public static final int ITEMRESULT_TOO_LARGE = 13;
    public static final int ITEMRESULT_FAILED_UPLOAD = 14;
    public static final int ITEMRESULT_FAILED_COPY_TO_IMAGES_DIR = 15;
    public static final int ITEMRESULT_FAILED_COPY_TO_TEMP_DIR = 16;
    public static final int ITEMRESULT_FAILED_TO_UNZIP = 17;
    private static final HashMap desc = new HashMap();

    public static final HashMap RESULT_DESCRIPTIONS(){
        if(desc.isEmpty()){
            desc.put(Integer.valueOf(PARSE_CANCELLED_NOT_MULTIPART), "Invalid attempt to parse a request that is not multi-part.");
            desc.put(Integer.valueOf(PARSE_CANCELLED_NO_TEMP_DIR), "The directory specified as the temporary upload location does not exist.");
            desc.put(Integer.valueOf(PARSE_FAILED), "An error occurred while parsing the multi-part request containing the file specified for upload.");
            desc.put(Integer.valueOf(PARSE_SUCCESS), "The multi-part request parsed successfully");
            desc.put(Integer.valueOf(UPLOAD_CANCELLED_ON_FIRST_ERROR), "An unspecified error occurred during upload. Upload cancelled.");
            desc.put(Integer.valueOf(UPLOAD_CANCELLED_UNPARSED_REQUEST), "The upload has been cancelled because the multi-part request has not been parsed.");
            desc.put(Integer.valueOf(UPLOAD_SUCCESS), "File upload successfully completed.");
            desc.put(Integer.valueOf(UPLOAD_SUCCESS_PARTIAL), "File(s) upload partially successful");
            desc.put(Integer.valueOf(ITEMRESULT_TOO_LARGE), "One or more files was not uploaded due to exceeding size restrictions");
            desc.put(Integer.valueOf(ITEMRESULT_FAILED_UPLOAD), "One or more files has failed to upload");
            desc.put(Integer.valueOf(ITEMRESULT_FAILED_COPY_TO_IMAGES_DIR), "The file has not been successfully transferred from its temporary upload directory to its permanent location");
            desc.put(Integer.valueOf(ITEMRESULT_FAILED_COPY_TO_TEMP_DIR), "ITEMRESULT_FAILED_COPY_TO_TEMP_DIR"); // Is this error valid?
            desc.put(Integer.valueOf(ITEMRESULT_FAILED_TO_UNZIP), "There was an error unzipping the uploaded file");
        }
        return desc;
    }
    
    private int iResultType;
    private String sFileInSpec;
    private String sFileOutTempDir;
    private String sFileOutSpec;
    private long lFileSize;
    private ArrayList imagesAlreadyUploaded;
    
    public result(){

    }
        
    public int getResultType(){ return iResultType; }    
    public void setResultType(int iType){ iResultType = iType; }
    
    public String getFileInSpec(){ return sFileInSpec; }    
    public void setFileInSpec(String sSpec){ sFileInSpec = sSpec; }
    
    public String getFileOutTempDir(){ return sFileOutTempDir; }    
    public void setFileOutTempDir(String sDir){ sFileOutTempDir = sDir; }
    
    public String getFileOutSpec(){ return sFileOutSpec; }    
    public void setFileOutSpec(String sName){ sFileOutSpec = sName; }
    
    public long getFileSize(){ return lFileSize; }
    public void setFileSize(long lSize){ lFileSize = lSize; }
    
    public ArrayList getImagesAlreadyUploaded(){ return imagesAlreadyUploaded; }
    public void setImagesAlreadyUploaded(ArrayList aList){ imagesAlreadyUploaded = aList; }    

    public static void main(String[] args){

    }
}
