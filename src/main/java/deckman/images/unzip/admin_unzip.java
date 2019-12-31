/*
 * admin_unzip.java
 *
 * Created on September 28, 2008, 4:00 AM
 *
 */

package deckman.images.unzip;

import deckman.settings.*;
import deckman.utils.*;
import java.io.*;
import java.util.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;

public class admin_unzip {
    
    /** Creates a new instance of admin_unzip */
    public admin_unzip() {
    }
    
    public result[] doAction(HttpServletRequest request, String sTask, initialization init){
	ArrayList alist = Collections.list(request.getParameterNames());

	    // remove items that do not correspond to form parameter names indicating checkboxes
		    filterableArrayList flist = new filterableArrayList(alist);
		    filterableArrayList.listFilter filter = new filterableArrayList.listFilter(){
	    public boolean isMatched(String listItem){
		return listItem.matches("chk\\d+");
	    }
	};
	flist.filterFor(filter);

	    /**
	     * sort the resulting list so that the checkbox names are in numerical order of their suffixes. chk[i] where i = suffix.
	     * This will correspond to the order they appear in the HTML page (the request.getParameterNames() function 
	     * above returns the field names in a random order that does not reflect their order in the HTML page).
	     */
	Comparator comp = new Comparator(){
	    public int compare(Object s1, Object s2){
		int i1 = Integer.valueOf(((String)s1).substring(3));
		int i2 = Integer.valueOf(((String)s2).substring(3));
		return (i1 - i2);
	    }
	    public boolean equals(Object o){ return super.equals(o); }
	};                
	Collections.sort(flist, comp);

	    // unzip or delete all files whose shortnames are found in flist.
	String sZipPath = init.getPropertyAsString("UnzipInFilePath");
	String sOutDir = init.getPropertyAsString("UnzipOutFilePath");
		    String sFullImgDir = init.getPropertyAsString("FullsizeFilePath");
	result[] results = null;
	Iterator iter = flist.iterator();
	if(!flist.isEmpty()){
	    results = new result[flist.size()];
	    int iResult = 0;
	    boolean bClearTargetDir = true;
	    while(iter.hasNext()){
		String sIdx = ((String)(iter.next())).substring(3);
		String sParamName = "zipname" + sIdx;
		String sZipShortName = request.getParameter(sParamName);
		String sZipFile = sZipPath + File.separator + sZipShortName;
		if(sTask.equalsIgnoreCase("UNZIP")){
		    result r = new unzipper(sZipFile, sOutDir).unzip(bClearTargetDir);
		    results[iResult] = r;
		    iResult++;
		    if(bClearTargetDir) bClearTargetDir = false;    // otherwise extracted files from the last iteration will be deleted
		}
		else if(sTask.equalsIgnoreCase("DELETE")){
		    File f = new File(sZipFile);
		    f.delete();
		    results = null;
		}
	     }
	}

	if(sTask.equalsIgnoreCase("UNZIP")){
		// move the unzipped files that can be identified as images from the "UnzipOutFilePath" directory to the websites fullsize image directory (final destination)
	    FileFilter imgFilter = new deckman.images.imageFileFilter();
	    ArrayList aMoves = codeLib.moveDirectoryContents(sOutDir, sFullImgDir, imgFilter, true);
	}

	return results;
    }


    public void displayUnzipResults(result[] results, JspWriter out, boolean bDebug) throws IOException {
	boolean bAllSuccessful = true;
	String sErrorImgList = "";
	
	for(int i=0; i<results.length; i++){
	    result r = results[i];
	    if(bDebug){
		System.out.println("");
		System.out.println("RESULT: " + r.getDescription());
		System.out.println("Zip file: " + r.getZipFileInSpec());
		System.out.println("Zip file type: " + r.getZipTypeDescription());
		System.out.println("unzip to: " + r.getZipFileOutDir());
		System.out.println("Zip file size: " + String.valueOf(r.getZipSize()));
		System.out.println("Files extracted: " + String.valueOf(r.getZipLength()));
	    }
	    
	    
	    if(r.getType() != result.UNZIP_SUCCESS){
		
		out.print("<pre>");
		out.println("RESULT: " + r.getDescription());
		out.println("Zip file: " + r.getZipFileInSpec());
		out.println("Zip file type: " + r.getZipTypeDescription());
		out.println("unzip to: " + r.getZipFileOutDir());
		out.println("Zip file size: " + String.valueOf(r.getZipSize()));
		out.println("Files extracted: " + String.valueOf(r.getZipLength()));  
		out.print("</pre>");
		
		out.println("<pre>");
		System.out.println("");
		bAllSuccessful = false;
		ArrayList a = r.getZipExtractionResults();
		if(a != null){
		    Iterator iter = a.iterator();
		    while(iter.hasNext()){
			Map m = (Map)iter.next();
			if(m.containsKey("ErrorDetail")){
			    if(m.containsKey("ShortName")){
				if(bDebug) System.out.println("Exception extracting " + m.get("ShortName"));
				out.println("[ E X C E P T I O N ]  ---  \"" + m.get("ShortName") + "\"\r\n");
				sErrorImgList += ("   " + m.get("ShortName") + "\\r\\n");
			    }                       
			    if(m.containsKey("ErrorStackTrace")){
				if(bDebug) System.out.print(m.get("ErrorStackTrace"));
				out.print(m.get("ErrorStackTrace"));
				out.println("");
				out.println("");
			    }
			    else{
				if(bDebug) System.out.println(m.get("ErrorDetail"));
				out.println(m.get("ErrorDetail"));
				out.println("");
				out.println("");
			    }
			}
		    }
		}
		out.println("</pre>");
	    }
	}

	if(bAllSuccessful){
	    out.println("<script language='javascript'>alert('Files Unzipped Successfully');</script>");
	}
	else{
	    out.print("<script language='javascript'>");
	    if(sErrorImgList.equals("")){
		out.print("alert('Encountered an error unzipping the file(s)");
	    }
	    else{
		out.print("alert('Encountered errors while unzipping the following:\\r\\n\\r\\n");
		out.print(sErrorImgList);		
	    }
	    out.print("');</script>");
	}
    }
	
    
    public static void main(String[] args){
	
    }
}
