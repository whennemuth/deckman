/*
 * filterableArrayList.java
 *
 * Created on June 8, 2008, 3:30 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package deckman.utils;

/**
 *
 * @author Warren
 *
 * This class extends ArrayList to provide two additional methods for removing items in the ArrayList based on critera
 * passed in as an instance of listFilter. The items of the ArrayList are assumed to be Strings and if any of these 
 * items are matched by the criteria, they are either removed or kept (removing all non-matches).
 */

import java.util.*;

public class filterableArrayList extends ArrayList {
    
    /**
	 * Creates a new instance of filterableArrayList
	 */
    public filterableArrayList(List alist){
	this.addAll(alist);
    }

    public void filterFor(listFilter filter){
	doFilter(filter, false);
    }

    public void filterOff(listFilter filter){
	doFilter(filter, true);
    }

    private void doFilter(listFilter filter, boolean bRemove){
	    // build an arraylist of items that are to be filtered off
	ArrayList aUnwanted = new ArrayList();
	Iterator iter = this.iterator();

	while(iter.hasNext()){
	    String sValue = (String)iter.next();
	    if(bRemove){
		if(filter.isMatched(sValue)){
		    aUnwanted.add(sValue);
		}
	    }
	    else{
		if(!filter.isMatched(sValue)){
		    aUnwanted.add(sValue);
		}
	    }
	}

	// remove the unwanted items from the filterableArrayList
	if(!aUnwanted.isEmpty()){
	    this.removeAll(aUnwanted);
	}
    }
    
    public interface listFilter{
        public boolean isMatched(String listItem);
    }
    
}
