/*
 * admin_thumbnail.java
 *
 * Created on April 20, 2008, 10:35 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package deckman.images.reimage;

import deckman.settings.*;
import deckman.utils.codeLib;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import java.util.*;
import java.io.*;
/**
 *
 * @author Warren
 */
public class admin_thumbnail {
    private String sThumbInDir = null;
    private String sThumbOutDir = null;
    private String sFullImgDir = null;
    private String sThumbsDir = null;
    private boolean bDebug = false;

    
	/**
	 * Creates a new instance of admin_thumbnail, but does not ready it for producing thumbnails.
	 */
    public admin_thumbnail(){
	
    }
	/**
	 * Creates a new instance of admin_thumbnail
	 */
    public admin_thumbnail(initialization init) {
	sThumbInDir = init.getPropertyAsString("ReimageInFilePath");
	sThumbOutDir = init.getPropertyAsString("ReimageOutFilePath");
	sFullImgDir = init.getPropertyAsString("FullsizeFilePath");
	sThumbsDir = init.getPropertyAsString("ThumbsFilePath");
	bDebug = init.getPropertyAsBoolean("DebugMode").booleanValue();
    }


    public result[] doDelete(HttpServletRequest request){
	result[] results = null;
	ArrayList alist = Collections.list(request.getParameterNames());
	alist = codeLib.getSortedIndexedSubset(alist, "chk");
	if(!alist.isEmpty()){
	    Iterator iter = alist.iterator();
	    while(iter.hasNext()){
		String sIdx = ((String)(iter.next())).substring(3);
		String sParamName = "imagename" + sIdx;
		String sImgShortName = request.getParameter(sParamName);
		String sImgFile = sThumbInDir + File.separator + sImgShortName;
		File f = new File(sImgFile);
		f.delete();
	    }
	}
	
	return results;
    }
    
    
    public result[] doThumbs(HttpServletRequest request){
	result[] results = null;
	ArrayList alist = Collections.list(request.getParameterNames());
	alist = codeLib.getSortedIndexedSubset(alist, "chk");
	HttpSession session = request.getSession(true);
	session.setAttribute("THUMB_PROGRESS", null);	// reset the counter 
	
	if(!alist.isEmpty()){
	    results = new result[alist.size()];
	    int iResult = 0;
	    boolean bClearTargetDir = true;

		// make thumbs from all files whose shortnames are found in alist.
	    ArrayList imgShortNames = new ArrayList();
	    Iterator iter = alist.iterator();
	    while(iter.hasNext()){
		String sIdx = ((String)(iter.next())).substring(3);
		String sParamName = "imagename" + sIdx;
		String sImgShortName = request.getParameter(sParamName);
		imgShortNames.add(sImgShortName);
		String sImgFile = sThumbInDir + File.separator + sImgShortName;
		result r = new rescaler(sImgFile, sThumbOutDir).generateThumb(bClearTargetDir);                       
		results[iResult] = r;
		iResult++;
		if(bClearTargetDir) bClearTargetDir = false;    // otherwise extracted files from the last iteration will be deleted
		incrementSessionCounter(session, alist.size());
	    }

		// move all the newly created thumbnails to the thumbnail images dir (final destination)
	    ArrayList aMoves = codeLib.moveDirectoryContents(sThumbOutDir, sThumbsDir, true);
	    putNewNamesIntoResults(aMoves, results, "THUMB");

		// move the fullsize images from which thumbnails were created to the images dir (final destination)
	    FileFilter filter = getMoveableItemsFileFilter(imgShortNames, results);
	    aMoves = codeLib.moveDirectoryContents(sThumbInDir, sFullImgDir, filter, true);
	    putNewNamesIntoResults(aMoves, results, "FULLSIZE");
	}
	return results;
    }

    
    public result[] simulateThumbs(HttpServletRequest request){
	ArrayList alist = Collections.list(request.getParameterNames());
	alist = codeLib.getSortedIndexedSubset(alist, "chk");
	
	int i = 0;
	
	/*
	alist = new ArrayList();
	alist.add("1");
	alist.add("2");
	alist.add("3");
	alist.add("4");
	alist.add("5");
	alist.add("6");
	alist.add("7");
	alist.add("8");
	alist.add("9");
	alist.add("10");
	alist.add("11");
	alist.add("12");
	alist.add("13");
	alist.add("14");
	alist.add("15");
	*/
	
	result[] results = new result[alist.size()];
	HttpSession session = request.getSession(true);
	session.setAttribute("THUMB_PROGRESS", null);	// reset the counter 
	
	if(!alist.isEmpty()){
	    Iterator iter = alist.iterator();
	    while(iter.hasNext()){
		Object o = iter.next();
		result r = new result();
		boolean bError = false;
		try {
		    Thread.sleep(1000);
		} catch (InterruptedException ex) {
		    r.setError(codeLib.getStackTraceAsString(ex));
		    r.setType(result.REIMAGE_CANCELLED_ON_FIRST_ERROR);
		    bError = true;
		}

		if(!bError){
		    r.setImageFileInSpec("[simulated thumb value] " + String.valueOf(i+1));
		    r.setImageNewFullName("[simulated thumb value] " + String.valueOf(i+1));
		    r.setImageSize(0);
		    r.setImageType("[simulated thumb value] " + String.valueOf(i+1));
		    r.setThumbFileOutDir("[simulated thumb value] " + String.valueOf(i+1));
		    r.setThumbShortName("[simulated thumb value] " + String.valueOf(i+1));
		    r.setThumbSize(0);
		    r.setType(result.REIMAGE_SUCCESS);
		}
		incrementSessionCounter(session, alist.size());
		results[i++] = r;
	    }
	}
	
	return results;
    }
    

	/**
	 * In order to get accurate information about how many images have been thumbed, a counter stored in session
	 * is incremented after each thumb is produced from an image. Javascript on the client side will query the
	 * reimageProgress servlet which will in turn provide the Integer value of this counter. The progress
	 * meter on client will be updated to reflect any changes in this counter.
	 * @param request HttpServletRequest, will be used to obtain the session that contains the counter
	 * @param iTotal int, the total number of images to make thumbs from. Combined with the counter, a percentage
	 * complete figure is computed by the servlet described above using this number.
	 */
    private void incrementSessionCounter(HttpSession session, int iTotal){
	Map map = (Map)session.getAttribute("THUMB_PROGRESS");
	if(map == null){
	    map = new HashMap();
	    map.put("done", new Integer(1));
	    map.put("total", new Integer(iTotal));
	}
	else{
	    Integer iDone = (Integer)map.get("done");
        if(iDone < iTotal){
            iDone = new Integer(iDone.intValue()+1);
        }
	    map.put("done", iDone);
	    map.put("total", new Integer(iTotal));
	}
	session.setAttribute("THUMB_PROGRESS", map);	
    }
    
    
	/**
	 * This function returns a file filter that specifies only those files that were selected by the user on
	 * the HTML form to create thumbnails from and from them, only those where thumbnail creation was error free
	 * @param imgShortNames java.util.ArrayList, Those images selected by the user through checkboxes.
	 * @param results result[], The results of each attempt to make a thumbnail from a fullsize image
	 * @return FileFilter, The FileFilter that combines the two criteria specified above. 
	 */
    public FileFilter getMoveableItemsFileFilter(final ArrayList imgShortNames, final result[] results){
	FileFilter filter = new FileFilter(){
	    public boolean accept(File f){
		boolean bAccept = false;
		if(imgShortNames.contains(f.getName())){
		    for(int i=0; i<results.length; i++){
			result r = (result)results[i];
			if(codeLib.removePathFromFileSpec(r.getImageFileInSpec()).equals(f.getName())){
			    if(r.getType() == result.REIMAGE_SUCCESS){
				bAccept = true;
			    }
			}
		    }
		}
		return bAccept;
	    }
	};
	
	return filter;
    }

    
	/**
	 * function moveDirectoryContents returns an ArrayList that indicates what name (X) each
	 * file was relocated under. For each moved file listed in aMoves, find the corresponding file
	 * result item in results and set the appropriate property with X. A different name may have been 
	 * assigned as part of the move due to the existing name matching that of a file already present 
	 * in the destination directory. So, when the results are displayed in HTML, it will be clear if 
	 * the uploaded image or thumbnail can still be found under the same name anymore.
	 * @param aMoves java.util.ArrayList, The file by file before and after picture of moving a folders
	 * contents. See util.moveDirectoryContents comments for a description of what an aMoves item is.
	 * @param results deckman.images.reimage.result[], The results ArrayList being updated.
	 */
    public void putNewNamesIntoResults(ArrayList aMoves, result[] results, String sMoved){
	Iterator iter = aMoves.iterator();
	while(iter.hasNext()){
	    String[] sShortNames = (String[])(iter.next());
	    String sOrigShortName = sShortNames[0];	// name of file before moving
	    String sNewShortName = sShortNames[1];	// name of file after moving
	    if(sNewShortName != null){
		for(int i=0; i<results.length; i++){
		    result r = (result)results[i];
		    String sRecordedShortName = codeLib.removePathFromFileSpec(r.getImageFileInSpec());
		    
		    if(sMoved.equalsIgnoreCase("THUMB")){
			if(codeLib.removeFileExtension(sOrigShortName).equals(codeLib.removeFileExtension(sRecordedShortName))){
			    r.setThumbShortName(sNewShortName);
			    results[i] = r; // put the result back in the results array
			}
		    }
		    else if(sMoved.equalsIgnoreCase("FULLSIZE")){
			if(sRecordedShortName.equals(sOrigShortName)){
			    r.setImageNewFullName(sFullImgDir + File.separator + sNewShortName);
			    results[i] = r; // put the result back in the results array
			}
		    }
		}
	    }
	}
	
    }

    
	/**
	 * After making thumbs from fullsize images, the results need to be displayed to the user in the HTML form.
	 * This function outputs these results as HTML.
	 * @param results deckman.images.reimage.result[], Each item of this ArrayList houses the result details for
	 * a particular file.
	 * @param out javax.servlet.jsp.JspWriter, The writer for the JSP page in which the results will be displayed
	 * @param bDebug boolean, indicates if additional information should be output to System.out
	 */
    public void printThumbResultsHTML(result[] results, JspWriter out, boolean bDebug) throws java.io.IOException {
	out.println("<br>");
	out.println("<table cellpadding=1 cellspacing=0 width='800px'>");
	out.println("<tr><td align='left' class='td1 td5 td2' colspan=2>&nbsp;&nbsp;RESULTS:</td></tr>");

	for(int i=0; i<results.length; i++){
	    result r = results[i];
	    String sError = null;
	    if(bDebug){
		System.out.println("RESULT: " + r.getDescription());
		System.out.println("Image file: " + r.getImageFileInSpec());
		System.out.println("Image file type: " + r.getImageType());
		System.out.println("Target thumb dir: " + r.getThumbFileOutDir());
		System.out.println("Thumb size (pixels): " + String.valueOf(r.getImageSize()));
	    }
	    String sColor = "";
	    if(r.getType() != result.REIMAGE_SUCCESS){
		sColor = " fontRed";
		sError = r.getError();
		if(sError != null){
		    out.println("<tr><td class='td1 td5" + sColor + "' colspan=2>&nbsp;</td></tr>");
		    out.println("<tr><td class='td1" + sColor + "' align='right'>");
		    out.print("<b>Error producing thumbnail:</b>&nbsp;&nbsp;&nbsp;</td><td class='td1" + sColor + "' align='left'>");
		    out.println("<div style='font-family: monospace; font-size:8pt; height:100px; overflow:auto; width:650px;'><pre>");
		    out.print(sError);
		    out.println("</pre></div></td></tr>");
		    out.println("<tr><td colspan=2 class='td1' height='5px'></td></tr>");
		}
	    }

	    if(sError == null) out.println("<tr><td colspan=2 class='td1 td5' height='5px'></td></tr>");

	    out.println("<tr><td class='td1" + sColor + "' align='right'><b>file being made a thumb:</b>&nbsp;&nbsp;&nbsp;</td>" +
		"<td align='left' class='td1" + sColor + "'><div style='width:650px;'>");
	    out.println(r.getImageFileInSpec() + " (" + r.getImageSize() + " bytes)");
	    out.println("</div></td></tr>");

	    File f = new File(sThumbsDir + File.separator + r.getThumbShortName());
	    out.println("<tr><td class='td1" + sColor + "' align='right'><b>new thumb file:</b>&nbsp;&nbsp;&nbsp;</td>" +
		"<td align='left' class='td1" + sColor + "'><div style='width:650px;'>");
	    out.println(f.getPath() + " (" + String.valueOf(f.length()) + " bytes)");
	    out.println("</div></td></tr>");

	    out.println("<tr><td class='td1" + sColor + "' align='right'><b>original image saved as:</b>&nbsp;&nbsp;&nbsp;</td>" +
		"<td align='left' class='td1" + sColor + "'><div style='width:650px;'>");
	    out.println(String.valueOf(r.getImageNewFullName()));
	    out.println("</div></td></tr>");

	    out.println("<tr><td class='td1" + sColor + "' align='right'><b>result:</b>&nbsp;&nbsp;&nbsp;</td>" +
		"<td align='left' class='td1" + sColor + "'><div style='width:650px;'>");
	    out.println(r.getDescription());
	    out.println("</div></td></tr>");

	    out.println("<tr><td class='td1' colspan=2 height='5px'></td></tr>");
	}

	out.println("</table>");
    }

    public static void main(String[] args){
	int i=0;
	System.out.println(i++);
    }
}
