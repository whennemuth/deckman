package deckman.images;

import java.util.*;
import java.lang.*;

/**
 * Provides a more sophisticated way of getting the index of a particular image name in an ArrayList that
 * contains them as described in indexof(String, String).
 * <p>
 * The an instance of imagesArrayList is serializable so that it can be stored by javax.servlet.http.HttpSession object.
 */
public class imagesArrayList extends ArrayList implements java.io.Serializable {
    
    public imagesArrayList() {
        
    }
    
        /**
         * This method of java.util.ArrayList is overloaded here, otherwise the existing method would not return
         * true if an Array , A, existed such that A = new String[] {sID, sIndex}.
         * This is because the indexOf method would be evaluating based on A.equals(B) where B is any given item
         * of the ArrayList. Similar to a deepEquals method, this could be thought of as a "deepIndexOf" method.
         * @param sID, String - corresponds to the ID of an image
         * @param sIndex, String - Represents the index of an image, which describes it's relative display order 
         * with other images.
         * @return sIdx, int - the index of the image.  
         */
    public int indexOf(String sID, String sIndex){
        int iIdx = -1;
        for(int i=0; i<this.size(); i++){
            String[] aPair = (String[])(this.get(i));
            if(sID.equals(aPair[0]) && sIndex.equals(aPair[1])){
                iIdx = i;
                break;
            }
        }
        return iIdx;
    }
    
}
