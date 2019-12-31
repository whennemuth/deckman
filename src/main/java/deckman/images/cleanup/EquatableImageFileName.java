/*
 * EquatableImageFileName.java
 *
 * Created on June 8, 2008, 4:08 PM
 *
 * This class represents the name of an image file. Its main function is to provide an equals function that allows
 * it to be compared with another String or imageFileName instance and base equality on one of the following criteria:
 *  1) Both objects compare as equal (caseless) as Strings
 *  2) Both objects have a have a name portion that compares as above, and the file extensions, while not required to
 *     be equal, must both fall within the list of supported image types.
 * EXAMPLES: 
 *  a) "myImage.gif".equals("MYIMAGE.JPG") returns true
 *  b) "myImage.gif".equals("myImage.txt") returns false
 *  c) "myImage.gif".equals("yoImage.gif") returns false
 */

package deckman.images.cleanup;

import java.util.*;
import java.io.*;
import deckman.utils.*;

/**
 *
 * @author Warren
 */
public class EquatableImageFileName implements Comparable, Cloneable {
    
    /** Creates a new instance of EquatableImageFileName */
    private String sStringVal = null;
    private Integer iID = null;
    String[] sExts = deckman.images.imageFileFilter.getValidFileExtensions();
    List exts = Arrays.asList(sExts);

    public EquatableImageFileName(int iID, String sStringVal){
	this.iID = new Integer(iID);
	this.sStringVal = sStringVal;
    }
    
    public EquatableImageFileName(String sStringVal){
	this.sStringVal = sStringVal;
    }

    public EquatableImageFileName(int iID, File f){
	this(iID, f.getName());
    }
    
    public EquatableImageFileName(File f){
	this(f.getName());
    }
    
    public String getStringValue(){
	return this.sStringVal;
    }
    
    public Integer getID(){
	return iID;
    }

    public boolean equals(Object obj){
	if(this.sStringVal==null || obj==null){
	    return false;
	}
	else if(this.hashCode() == obj.hashCode()){
	    return true;
	}
	else{
	    String s = null;
	    if(obj instanceof EquatableImageFileName){
		s = ((EquatableImageFileName)obj).getStringValue();
	    }
	    else if(obj instanceof String){
		s = (String)obj;
	    }

	    if(s == null){
		return false;
	    }
	    else{
		if(this.sStringVal.equalsIgnoreCase(s)){
		    return true;
		}
		else{
		    if(codeLib.removeFileExtension(this.sStringVal).equalsIgnoreCase(codeLib.removeFileExtension(s))){
			String sExt1 = codeLib.getFileExtension(this.sStringVal).toLowerCase();
			String sExt2 = codeLib.getFileExtension(s).toLowerCase();
			if(sExt1.equals(sExt2)){
			    return true;
			}
			else{				
			    return (exts.contains(sExt1) && exts.contains(sExt2));
			}			    
		    }
		    else{
			return false;
		    }
		}
	    }
	}
    }
    
    
	/**
	 * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
	 */
    public int compareTo(Object obj){
	if(this.sStringVal==null && obj==null){
	    return 0;
	}
	else if(this.sStringVal == null){
	    return 1;
	}
	else if(obj == null){
	    return -1;
	}
	else if(this.equals(obj)){
	    return 0;
	}
	else{
	    String s = null;
	    if(obj instanceof EquatableImageFileName){
		s = ((EquatableImageFileName)obj).getStringValue();
	    }
	    else if(obj instanceof String){
		s = (String)obj;
	    }
	    else{
		s = obj.toString();
	    }
	    
	    if(s == null){
		return -1;
	    }
	    else{
		return this.sStringVal.compareToIgnoreCase(s);		
	    }
	}
	
    }
    
    
    public Object clone(){
	try{
	    return super.clone();
	}
	catch (CloneNotSupportedException ex){
	    if(this.sStringVal == null){
		return null;
	    }
	    else{
		return new EquatableImageFileName(this.sStringVal);
	    }
	}
    }
    
    
    public static void main(String[] args){
	/*
	 List list = new LinkedList(Arrays.asList(new String[]{"a", "b", "c", "A", "B", "C"}));
	Arrays.sort(a, String.CASE_INSENSITIVE_ORDER);
	for(int i=0; i<a.length; i++){
	    System.out.println(a[i]);
	}
	 */
	
	ArrayList list = new ArrayList();
	list.add(new EquatableImageFileName("a"));
	list.add(new EquatableImageFileName("b"));
	list.add(new EquatableImageFileName("c"));
	list.add(new EquatableImageFileName("A"));
	list.add(new EquatableImageFileName("B"));
	list.add(new EquatableImageFileName("C"));
	
	System.out.println("");
	Iterator iter = list.iterator();
	while(iter.hasNext()){
	    System.out.println(((EquatableImageFileName)iter.next()).getStringValue());
	}
	
	Collections.sort(list);
	
	System.out.println("");
	iter = list.iterator();
	while(iter.hasNext()){
	    System.out.println(((EquatableImageFileName)iter.next()).getStringValue());
	}
    }
}
