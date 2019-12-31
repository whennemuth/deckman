package deckman.images.unzip;

import java.util.*;
import java.util.zip.*;
import java.util.regex.*;
import deckman.utils.codeLib;

public class result implements java.io.Serializable {
    
        //This will be the intialized value of all numeric result properties of intrinsic type.
    public static final int VALUE_NOT_SET = 0;
    
        //postponement of unzipping a file can come from the following reasons:
    public static final int UNZIP_CANCELLED_NOT_ZIP = 10;
    public static final int UNZIP_CANCELLED_NO_UNZIP_DIR = 1;
    //public static final int UNZIP_CANCELLED_DUPLICATE_NAMES_FOUND = 2;
    public static final int UNZIP_FILE_NOT_FOUND = 5;
    public static final int UNZIP_CANCELLED_FAILED_TO_CLEAR_UNZIP_DIR = 13;
    public static final int UNZIP_CANCELLED_UNSUPPORTED_COMPRESSION_METHOD = 6;
    
        //for all items in a zip file, their collective unzipping success can be described with the following constants
        //That is, assuming cancelling everything on the first error has not been specified.
    public static final int UNZIP_CANCELLED_ON_FIRST_ERROR = 8;
    public static final int UNZIP_FAILED = 3;
    public static final int UNZIP_SUCCESS_PARTIAL = 12;
    public static final int UNZIP_SUCCESS = 4;
    
    
    public static final int UNZIP_TYPE_ZIP = 7;
    public static final int UNZIP_TYPE_GZIP = 9;
    
    private static final HashMap desc = new HashMap();
    public static final HashMap RESULT_DESCRIPTIONS(){
        if(desc.isEmpty()){
            desc.put(Integer.valueOf(UNZIP_CANCELLED_NOT_ZIP), "Invalid attempt to unzip a file that is not in the zip format.");
            desc.put(Integer.valueOf(UNZIP_CANCELLED_UNSUPPORTED_COMPRESSION_METHOD), "Zip file has an unsupported compression method.");
            desc.put(Integer.valueOf(UNZIP_CANCELLED_NO_UNZIP_DIR), "The directory specified as the unzip target location does not exist.");
            desc.put(Integer.valueOf(UNZIP_CANCELLED_FAILED_TO_CLEAR_UNZIP_DIR), "The directory specified as the unzip location could not be cleared out in preparation for this unzip attempt");
            //desc.put(Integer.valueOf(UNZIP_CANCELLED_DUPLICATE_NAMES_FOUND), "File extraction cancelled because more than one contained file was found with the same name.");
            desc.put(Integer.valueOf(UNZIP_FILE_NOT_FOUND), "The specified zip file could not be found.");
            desc.put(Integer.valueOf(UNZIP_FAILED), "An error occurred while unzipping the uploaded file. No files extracted.");
            desc.put(Integer.valueOf(UNZIP_SUCCESS), "The uploaded file unzipped successfully");
            desc.put(Integer.valueOf(UNZIP_SUCCESS_PARTIAL), "File(s) unzipping partially successful. Partial extraction");
            desc.put(Integer.valueOf(UNZIP_CANCELLED_ON_FIRST_ERROR), "An unspecified error occurred during unzipping. File extraction cancelled.");
        }
        return desc;
    }
    
    private static final HashMap types = new HashMap();
    public static final HashMap ZIP_NAMES(){
        if(types.isEmpty()){
            types.put(Integer.valueOf(UNZIP_TYPE_ZIP), "ZIP");
            types.put(Integer.valueOf(UNZIP_TYPE_GZIP), "GZIP");
        }
        return types;
    }
    
    private int iResultType = this.VALUE_NOT_SET;
    private String sZipFileInSpec = null;
    private String sZipFileOutDir = null;
    private int iZipType = 0;
    private long lZipSize = 0;
    private long lZipLength = 0;
    private ArrayList aZipExtractionResults = null;
    
    public result(){

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
    
    public int getZipTypeID(){ return iZipType; }    
    public void setZipTypeID(int iType){ iZipType = iType; }
    
    public String getZipTypeDescription(){
        if(iZipType == 0){
            return null;
        }
        else{
            return (String)result.ZIP_NAMES().get(new Integer(iZipType));
        }
    }    
    
    public boolean containsDuplicateShortName(String sShortName){
        if(aZipExtractionResults == null){
            return false;
        }
        else if(aZipExtractionResults.isEmpty()){
            return false;
        }
        else{
            boolean bFound = false;
            Iterator iter = aZipExtractionResults.iterator();
            while(iter.hasNext()){
                Map map = (Map)(iter.next());
                if(sShortName.equalsIgnoreCase((String)map.get("ShortName"))){
                    return (bFound = true);
                }
            }
            return bFound;
        }
    }
    
    public boolean containsDuplicateShortName(ZipEntry zEntry){
        return containsDuplicateShortName(zEntry.getName());
    }
    
    public String getUniqueShortName(String sShortName){
        return getUniqueShortName(sShortName, 1);
    }
    
    private String getUniqueShortName(String sShortName, int iIdx){
        String sNewShortName;
        if(sShortName.matches("^[^\\.]+$")){    // "__[iIdx]" appended to the end if the short name
            sNewShortName = sShortName + "__" + String.valueOf(iIdx);
        }
        else{    // "__[sIdx]" inserted just before the ".[extension]" of the file extension. 
            sNewShortName = sShortName.replaceAll("\\.([^\\.]+)$", "__" + String.valueOf(iIdx) + ".$1");
        }
        
        if(containsDuplicateShortName(sNewShortName)){
            return getUniqueShortName(sShortName, ++iIdx);
        }
        else{
            return sNewShortName;
        }
    }
    
    public String getZipFileInSpec(){ return sZipFileInSpec; }    
    public void setZipFileInSpec(String sSpec){ sZipFileInSpec = sSpec; }
    
    public String getZipFileOutDir(){ return sZipFileOutDir; }    
    public void setZipFileOutDir(String sName){ sZipFileOutDir = sName; }
    
    public long getZipSize(){ return lZipSize; }
    public void setZipSize(long lSize){ lZipSize = lSize; }
    
    public long getZipLength(){ return lZipLength; }
    public void setZipLength(long lLength){ lZipLength = lLength; }
    
    public ArrayList getZipExtractionResults(){ return aZipExtractionResults; }

    /**
     * When a user uploads a zip file containing image files to the website, the first thing that happens is that 
     * each file entry is extracted from the zip file. An instance of the deckman.unzip.result class will be created
     * to store the results of extraction of each file using the aZipExtractionResults ArrayList of the result class.
     * One HashMap is created and stored in the ArrayList for each file decompressed from the zip file, this function
     * being called each time to do so. When the results of an entire zip file extraction are displayed in HTML, the 
     * ArrayList in the result class instance will be iterated through to for the details.
     * @param sOutDir The directory into which decompression of each zipped file is targeted.
     * @param zEntry java.util.zip.ZipEntry - The file entry for which the current HashMap (see general details) is storing extraction results
     * @param e If not null, this will be a java.lang.Exception instance that will be used to get details as to why extraction
     * of the particular zip file entry failed. This will appear as additional data in the HashMap.
     */
    public void addZipExtractionResult(ZipEntry zEntry, String sOutDir, Exception e){
        if(aZipExtractionResults == null){
            aZipExtractionResults = new ArrayList();
        }

        Map map = new HashMap();
        if(zEntry != null){
            map.put("ZipName", zEntry.getName());
            map.put("ShortName", unzipper.getZipShortFileName(zEntry, sOutDir));
            map.put("OutName", unzipper.getZipToDirFileName(zEntry, sOutDir, this));
            map.put("Size", String.valueOf(zEntry.getSize()));
        }
        
        if(e != null){
            map.put("ErrorDetail", e.toString());
            map.put("ErrorStackTrace", codeLib.getStackTraceAsString(e));
        }
        
        aZipExtractionResults.add(map);
    }

    public static void main(String[] args){
        //String sShortName = "hello";
        //String sShortName = "hello.zip";
        String sShortName = "hello.goodbye.zip";
        int iIdx = 1;
        String sNewShortName = null;
        if(sShortName.matches("^[^\\.]+$")){
            sNewShortName = sShortName + "__" + String.valueOf(iIdx);
        }
        else{
            sNewShortName = sShortName.replaceAll("\\.([^\\.]+)$", "__" + String.valueOf(iIdx) + ".$1");
        }
        System.out.println(sNewShortName);
    }
}
