
package deckman.images.unzip;

import java.util.*;
import java.util.zip.*;
import java.io.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import deckman.utils.codeLib;

public class unzipper {
    private String sZipFileName = null;
    private File zipFile = null;
    private String sOutDir = null;
    //final int iBufSize = 4096;    
    final int iBufSize = 1024;    
    
    /**
     * Creates a new instance of unzipper.
     * Of the 3 constructors, use of this one requires that subsequent use of the unzip method
     * be of either of the two overloaded versions that require the zip file spec parameter
     * @param sOutDir String - the path indicating where zip files are initially to be unzipped to.
     */
    public unzipper(String sOutDir){
        this.sOutDir = sOutDir;
    }
    
    /** Creates a new instance of unzipper 
     * @param sZipFileName A String indicating the file name of the zip file to be decompressed
     * @param sOutDir String - the path indicating where zip files are initially to be unzipped to.
     */
    public unzipper(String sZipFileName, String sOutDir) {
        this.sZipFileName = sZipFileName;
        this.sOutDir = sOutDir;
    }
    
    /** Creates a new instance of unzipper 
     * @param zipFile A file. Should be a zip file, though if not, the discrepancy will be caught as one of the
     * first steps of extraction.
     * @param sOutDir String - the path indicating where zip files are initially to be unzipped to.
     */
    public unzipper(File zipFile, String sOutDir){
        this.zipFile = zipFile;
        this.sOutDir = sOutDir;
    }
    
    /**
     * When a user uploads a zip file containing image files to the website, the first thing that happens is that 
     * each file entry is extracted from the zip file. An instance of the deckman.unzip.result class will be created
     * to store and return the results of extraction of each file using the aZipExtractionResults ArrayList of the 
     * result class. One HashMap is created and stored in the ArrayList for each file decompressed from the zip file.
     * When the results of a zip file extraction are displayed in HTML, the ArrayList in the result class instance
     * will be iterated through to for the details.
     * @return deckman.images.result
     */
    public result unzip(boolean bClearTargetDir){
        if(this.zipFile != null){
            return unzip(this.zipFile, bClearTargetDir);
        }
        else if(this.sZipFileName != null){
            return unzip(this.sZipFileName, bClearTargetDir);
        }
        else{
            return null;
        }
    }
    
    /**
     * When a user uploads a zip file containing image files to the website, the first thing that happens is that 
     * each file entry is extracted from the zip file. An instance of the deckman.unzip.result class will be created
     * to store and return the results of extraction of each file (not the files themselves) using the aZipExtractionResults ArrayList of the 
     * result class. One HashMap is created and stored in the ArrayList for each file decompressed from the zip file.
     * When the results of a zip file extraction are displayed in HTML, the ArrayList in the result class instance
     * will be iterated through to for the details.
     * @param sFileName String - The file name of the zip file whose contents are to be extracted
     * @return Instance of deckman.images.unzip.result
     */
    public result unzip(String sFileName, boolean bClearTargetDir){
        File f = new File(sFileName);
        return unzip(f, bClearTargetDir);
    }
    
    /**
     * When a user uploads a zip file containing image files to the website, the first thing that happens is that 
     * each file entry is extracted from the zip file. An instance of the deckman.unzip.result class will be created
     * to store and return the results of extraction of each file (not the files themselves) using the aZipExtractionResults ArrayList of the 
     * result class. One HashMap is created and stored in the ArrayList for each file decompressed from the zip file.
     * When the results of a zip file extraction are displayed in HTML, the ArrayList in the result class instance
     * will be iterated through to for the details.
     * @return Instance of deckman.images.unzip.result
     * @param f File - The zip file whose contents are to be extracted
     */
    public result unzip(File f, boolean bClearTargetDir){
        result ZipResult = new result();
        try{ ZipResult.setZipFileInSpec(f.getCanonicalPath()); } catch(IOException e){}
        ZipResult.setZipFileOutDir(this.sOutDir);
        ZipResult.setZipSize(f.length());
        ZipResult.setZipTypeID(result.UNZIP_TYPE_ZIP);
        boolean bPartialSuccess = false;
        int iFileCounter = 0;
        if(!f.exists()){
            ZipResult.setType(result.UNZIP_FILE_NOT_FOUND);
        }
        else if(!codeLib.fileExists(this.sOutDir)){            
            ZipResult.setType(result.UNZIP_CANCELLED_NO_UNZIP_DIR);
        }
        else if(!isZipFile(f)){
            ZipResult.setType(result.UNZIP_CANCELLED_NOT_ZIP);
        }
	else if(!isCompressionMethodSupported(f.getPath())){
	    ZipResult.setType(result.UNZIP_CANCELLED_UNSUPPORTED_COMPRESSION_METHOD);
	}
        else{
            boolean bTargetDirIsReady = bClearTargetDir ? codeLib.clearDirectory(this.sOutDir) : true;
            if(!bTargetDirIsReady){
                ZipResult.setType(result.UNZIP_CANCELLED_FAILED_TO_CLEAR_UNZIP_DIR);
            }
            else{
                try{
                    BufferedOutputStream boStream = null;
                    FileInputStream fiStream = new FileInputStream(f);
                    BufferedInputStream biStream = new BufferedInputStream(fiStream);
                    ZipInputStream ziStream = new ZipInputStream(biStream);
                    ZipEntry zEntry = null;
                    int iBytesRead;
                    while((zEntry = ziStream.getNextEntry()) != null){
                        if(!zEntry.isDirectory()){
                            byte[] aBuffer = new byte[iBufSize];
                            try{
                                String sOutFile = getZipToDirFileName(zEntry, this.sOutDir, ZipResult); //will alter name if needed to make it unique for a "flattened" (no subdirectories) version of the zip file. 
                                FileOutputStream foStream = new FileOutputStream(sOutFile);
				// System.out.println("extracting " + zEntry.getName() + "...");
                                boStream = new BufferedOutputStream(foStream, iBufSize);
                                while((iBytesRead = ziStream.read(aBuffer, 0, iBufSize)) != -1){
                                    boStream.write(aBuffer, 0, iBytesRead);
                                }
                                boStream.flush();
                                boStream.close();
                                ZipResult.addZipExtractionResult(zEntry, this.sOutDir, null);
                                iFileCounter++;
                            }
                            catch(ZipException e){
                                ZipResult.addZipExtractionResult(zEntry, this.sOutDir, e);
                                bPartialSuccess = true; 
                            }
                            catch(IOException e){ 
                                ZipResult.addZipExtractionResult(zEntry, this.sOutDir, e);
                                bPartialSuccess = true; 
                            }
                        }
                    }
                    ziStream.close();
                    ZipResult.setZipLength(iFileCounter);
                    if(bPartialSuccess) ZipResult.setType(result.UNZIP_SUCCESS_PARTIAL);
                }
                catch(ZipException e){
                    ZipResult.setType(result.UNZIP_CANCELLED_ON_FIRST_ERROR);
                    ZipResult.addZipExtractionResult(null, this.sOutDir, e);
                }
                catch(IOException e){
                    if(e instanceof FileNotFoundException){
                        ZipResult.setType(result.UNZIP_FILE_NOT_FOUND); //probably unreachable code because this check is done of f above
                    }
                    else{
                        ZipResult.setType(result.UNZIP_CANCELLED_ON_FIRST_ERROR);
                    }
                    ZipResult.addZipExtractionResult(null, this.sOutDir, e);
                }
            }
        } 
        
            /* A result object is instantiated with a type property of VALUE_NOT_SET. If this remains unchanged to this point
            * then no errors or problems have occurred yet and been indicated by setting a different type. This would mean 
            * the entire extraction was successful. Set the type accordingly.*/
        if(ZipResult.getType() == result.VALUE_NOT_SET){
            ZipResult.setType(result.UNZIP_SUCCESS);
        }
        else if(bPartialSuccess){
            ZipResult.setType(iFileCounter==0 ? result.UNZIP_FAILED : result.UNZIP_SUCCESS_PARTIAL);
        }
        
        return ZipResult;
    }
    
    /**
     * Zip files may contain compressed files within subdirectories. In this case, java.util.zip methods that
     * return the names of contained files do so under the following format: dirname/dirname/.../filename. This
     * function strips off the filename portion on the end and returns it, provided the name has not already
     * been used in the overall extraction - in which case, a unique name is generated and returned.
     * @param zEntry java.util.zip.ZipEntry - represents one entry (compressed file/folder) in the zip file. From this 
     * using the .getName() method can be obtained the full path including file name of the file from 
     * the root of a zip file. This parameter will be returned unaltered if the corresponding file is 
     * at the root level.
     * @param sOutDir String, the file into which files extracted from the zip file will go. Used here to determine
     * if a file has already been extracted from the current zip file with the same name as the one being extracted now.
     * @return String - the file name minus any subdirectory path data of a file in a zip file.
     */   
    public static String getZipShortFileName(ZipEntry zEntry, String sOutDir){
        String sFullName = zEntry.getName();
            //this is what the separator character is in windows for a zip path returned by ZipEntry.getName() 
        int idx = sFullName.lastIndexOf("/");
            //just in case that if on a non-windows system ZipEntry.getName returns a path built with "\\" as the separator
            //NOTE: the above two lines assume that neither "/" or "\\" are valid characters in a directory or file shortname
        if(idx == -1) idx = sFullName.lastIndexOf("\\");    //happens to be File.speparator on windows, but this is irrelevant.
        if(idx == -1) idx = sFullName.lastIndexOf(File.separator);  //if on a os where File.separator is neither "/" or "\\"
        String sShortName = idx==-1 ? sFullName : sFullName.substring(++idx);
        sShortName = codeLib.getUniqueFileName(sShortName, sOutDir);
        return sShortName;
    }

    /**
     * Zip files may contain compressed files within subdirectories. In this case, java.util.zip methods that
     * return the names of contained files do so under the following format: dirname/dirname/.../filename. This
     * function strips off the filename portion on the end and returns it appended to the a physical 
     * destination directory intended as the location the file is to be extracted to.
     * @param zEntry java.util.zip.ZipEntry - represents one entry (compressed file/folder) in the zip file. From this 
     * using the .getName() method can be obtained the full path including file name of the file from 
     * the root of a zip file. This parameter will be returned unaltered if the corresponding file is 
     * at the root level.
     * @param sMoveToDir String - This is the path of the directory intended as the location a file in a zip file is 
     * to be extracted to.
     * @param ZipResult deckman.images.unzip.result - An instance of the deckman.unzip.result class, created
     * to store and return the results of extraction of each file (not the files themselves). Used here to determine
     * if a file has already been extracted from the current zip file with the same name as the one being extracted now.
     * @return String - This is the full name on disk a file extracted from the zip file will have such that
     * if the file were represented as a java.io.File f, retval = f.getAbsolutePath()
     */
    public static String getZipToDirFileName(ZipEntry zEntry, String sMoveToDir, result ZipResult){
        String sShortName = getZipShortFileName(zEntry, sMoveToDir);
        String sNewFullName = sMoveToDir + File.separator + sShortName;
        return sNewFullName;
    }
    
    /**
     * This method determines if the supplied file is a valid zip file by testing the first 4 bytes with readLeInt against ZipFile.LOCSIG
     * @param f the file being tested as a zip file
     * @return boolean true/false
     */
    public static boolean isZipFile(File f){
        byte[] buf = new byte[4];
        RandomAccessFile raf = null;
        boolean bRetval = true;
        
        try{
            raf = new RandomAccessFile(f, "r");
            raf.read(buf);
            if(readLeInt(buf, 0) != ZipFile.LOCSIG) bRetval = false;
        }
        catch(FileNotFoundException e){
            bRetval = false;
        }
        catch(IOException e){
            bRetval = false;
        }
        finally{
            try{ raf.close(); } catch(IOException e){}
            return bRetval;
        }
    }

    
	/**
	 * This function will determine if the compression method of a zip file is supported.
	 * If the first ZipEntry in the ZipFile has a compression method that matches either 
	 * ZipEntry.STORED or ZipEntry.DEFLATED, then return true, otherwise false. Obviously
	 * STORED and DEFLATED are the only two recognized formats, 0 and 8 respectively.
	 * However, WinZip may return 9, which is unknown.
	 * @Param sFileSpec String, The path and filename of the zip file whose compression 
	 * method is to be tested.
	 * @return boolean, compression supported true/false.
	 */
    public static boolean isCompressionMethodSupported(String sFileSpec){
	boolean bSupported = false;
	try{
	    ZipFile zf = new ZipFile(sFileSpec);
	    Enumeration entries = zf.entries();
	    //for (Enumeration entries = zf.entries(); entries.hasMoreElements();) {
	    if(entries.hasMoreElements()){
		ZipEntry zEntry = (ZipEntry)entries.nextElement();
		if(zEntry.getMethod() == zEntry.DEFLATED || zEntry.getMethod() == zEntry.STORED){
		    bSupported = true;
		}
	    }

	}
	catch(IOException e){
	    /* do nothing. function will return false */
	}
	
	return bSupported;	
    }
    
    
   /**
    * [-- This method taken from the ZipFile java source code, java.util.zip.ZipFile --]
    * Read an int in little endian byte order from the given
    * byte buffer at the given offset.
    *
    * @param b the byte array to read from.
    * @param off the offset to read from.
    * @return The value read.
    */
   private static int readLeInt(byte[] b, int off){
     return ((b[off] & 0xff) | (b[off+1] & 0xff) << 8) | ((b[off+2] & 0xff) | (b[off+3] & 0xff) << 8) << 16;
   }
   
    /**
     * Filters off non-zip file names
     * @return File[] - returns a listing of a directories contents as a File array, excluding anything except zip files.
     */
   public static FileFilter getZipFileFilter(){
       FileFilter filter = new FileFilter(){
           public boolean accept(File f){
               return unzipper.isZipFile(f);
           }
       };
       return filter;
   }
   
     /**
     * Filters off zip file names
     * @return File[] - returns listing of a directories contents as a File array, excluding all zip files.
     */
  public static FileFilter getNonZipFileFilter(){
       FileFilter filter = new FileFilter(){
           public boolean accept(File f){
               return !unzipper.isZipFile(f);
           }
       };
       return filter;
   }
   

   public void printUnzipHistory(HttpSession session, HttpServletRequest request, JspWriter out, ArrayList aList) throws java.io.IOException {
        if(aList!=null){
            ListIterator iter = aList.listIterator();
            int i = 1;
            while(iter.hasNext()){
                result theResult = (result)(iter.next());
                
                int iResultType = theResult.getType();
                
                String sColor = iResultType==result.UNZIP_SUCCESS?"":" fontRed";

                    //print the result description (failure/success code will be used to obtain the desciption)
                out.println("<tr><td class='td1 td5" + sColor + "' colspan=2>&nbsp;</td></tr>");
                out.println("<tr><td class='td1" + sColor + "' colspan=2>&nbsp;&nbsp;&nbsp;<b>" + (i++) + "</b></td></tr>");
                out.println("<tr><td class='td1" + sColor + "' align='right'>");
                out.print("upload result:</td><td class='td1" + sColor + "'>");
                HashMap mymap = result.RESULT_DESCRIPTIONS();
                Integer myint = Integer.valueOf(iResultType);
                String sResultDesc = (String)(mymap.get(myint));
                out.print(sResultDesc);
                out.println("</td></tr>");

                    //print the uploaded file origin path
                String sFileInSpec = theResult.getZipFileInSpec();
                out.println("<tr><td class='td1" + sColor + "' align='right'>file being unzipped:</td><td class='td1" + sColor + "'>");
                out.println(sFileInSpec);
                out.println("</td></tr>");

                    //print the file unzip point
                String sFileOutDir = theResult.getZipFileOutDir();
                out.println("<tr><td class='td1" + sColor + "' align='right'>unzip target directory:</td><td class='td1" + sColor + "'>");
                out.println(sFileOutDir);
                out.println("</td></tr>");

                    //print the uploaded file size
                String sFileSize = String.valueOf(theResult.getZipSize());
                out.println("<tr><td class='td1" + sColor + "' align='right'>zip file size:</td><td class='td1" + sColor + "'>");
                out.println(sFileSize + " bytes");
                out.println("</td></tr>");
                out.println("<tr><td class='td1" + sColor + "' colspan=2>&nbsp;</td></tr>");
                
                String sFileLen = String.valueOf(theResult.getZipLength());
                out.println("<tr><td class='td1" + sColor + "' align='right'>files in zip file:</td><td class='td1" + sColor + "'>");
                out.println(sFileLen);
                out.println("</td></tr>");
                out.println("<tr><td class='td1" + sColor + "' colspan=2>&nbsp;</td></tr>");
                
                String sFileDesc = String.valueOf(theResult.getZipTypeDescription());
                out.println("<tr><td class='td1" + sColor + "' align='right'>Zip file type:</td><td class='td1" + sColor + "'>");
                out.println(sFileDesc);
                out.println("</td></tr>");
                out.println("<tr><td class='td1" + sColor + "' colspan=2>&nbsp;</td></tr>");
                
                ArrayList aFiles = theResult.getZipExtractionResults();
                long lZipCount = theResult.getZipLength();
                String sZipCount = String.valueOf(lZipCount);
                if(aFiles.isEmpty()){
                    sColor = (lZipCount > 0) ? " fontRed" : "";
                    out.println("<tr><td class='td1" + sColor + "' align='right'>files unzipped:</td>");
                    out.println("<td class='td1" + sColor + "' align='right'>0 of " + sZipCount + " files</td></tr>");
                }
                else{
                    sColor = (lZipCount > aFiles.size()) ? " fontRed" : "";
                    String sUnzipped = String.valueOf(aFiles.size());
                    out.println("<tr><td class='td1" + sColor + "' align='right'>files unzipped:</td>");
                    out.println("<td class='td1" + sColor + "' align='right'>" + sUnzipped + " of " + sZipCount + " files</td></tr>");
                    out.println("<tr><td>&nbsp;</td><td>");
                    Iterator mapIter = aFiles.iterator();
                    while(mapIter.hasNext()){
                        Map map = (Map)mapIter.next();
                        if(map.containsKey("ErrorStackTrace")){
                            out.println("<br>");
                            out.println(map.get("ErrorStackTrace"));
                        }
                        else{
                            out.println("<br>");
                            out.println(map.get("ShortName") + ",  " + map.get("Size") + " bytes");                            
                        }
                    }
                    out.println("</td>");
                }
            }
        }                
    }
   
    public static void main(String[] args){
	String sFile = null;
	String sOutDir = null;
	
	if(args.length == 2){
	    sFile = args[0];
	    sOutDir = args[1];
	}
	else{
	    sFile = "C:\\Documents and Settings\\Warren\\Desktop\\zipTest\\ziptest_with_duplicates.zip";
	    sOutDir = "C:\\Documents and Settings\\Warren\\Desktop\\zipTest\\Out";	    
	}
	
	result r = new unzipper(sFile, sOutDir).unzip(true);

	System.out.println("RESULT: " + r.getDescription());
	System.out.println("Zip file: " + r.getZipFileInSpec());
	System.out.println("Zip file type: " + r.getZipTypeDescription());
	System.out.println("unzip to: " + r.getZipFileOutDir());
	System.out.println("Zip file size: " + String.valueOf(r.getZipSize()));
	System.out.println("Files extracted: " + String.valueOf(r.getZipLength()));

	if(r.getType() != result.UNZIP_SUCCESS){
	     ArrayList a = r.getZipExtractionResults();
	    if(a != null){
		Iterator iter = a.iterator();
		while(iter.hasNext()){
		    Map m = (Map)iter.next();
		    if(m.containsKey("ErrorDetail")){
			if(m.containsKey("ShortName")){
			    System.out.println("Exception extracting " + m.get("ShortName"));
			}                       
			if(m.containsKey("ErrorStackTrace")){
			    System.out.print(m.get("ErrorStackTrace"));
			}
			else{
			    System.out.println(m.get("ErrorDetail"));
			}
		    }
		}
	    }
	}
    }
    
    
}
