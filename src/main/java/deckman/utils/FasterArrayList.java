/*
 * FasterArrayList.java
 *
 * Created on June 8, 2008, 4:25 PM
 */

package deckman.utils;

import deckman.images.cleanup.EquatableImageFileName;
import java.util.*;

/**
 * The standard ArrayList implementation of removeAll(Collection c) is slow.
 * This is because for each element in Collection a such that a.removeAll(c) = d, c must be iterated through for each item
 * in a until a match is found or the end of c is reached. This can lead to MANY iterations and significant processing time,
 * especially if equals methods are overidden with extra code and the Collections are large.
 * These iterations would remain few if both Collections a and c were first sorted and the removal of the matching items 
 * took place in c as well as a once they were found. Iteration through c would be occurring over a constantly shrinking 
 * Collection and finding a match would generally not involve iterating to the end of c because both Collections a and c 
 * have been sorted, keeping potentially equal items at the top of each.
 *
 * @author Warren
 */
public class FasterArrayList extends ArrayList {
    
    public FasterArrayList(){
	super();
    }
    
    
    public FasterArrayList(Collection c){
	super(c);
    }
    
        
	/**
	 * All matching items in objs will be removed from "this". Amoung other things, this function differs from the standard
	 * removeAll method provided with the collection interface in that the argument here is an array, not another collection.
	 * Short of using generics in the method signature, this is the only way to ensure that a "collection" of only one type
	 * is passed in. Without uniformity in the type, it is not clear that like items between the "remover" and "removee"
	 * "collections" would group in equivalent positions and reduce iterations as described in {@link FasterArrayList the introduction}
	 * Also, this method is customized in that when comparing objects with one another for matching and removal, the objects 
	 * must be either type String or EquatableImageFileName. This is primarily because this class is adapting the use of the
	 * removeAll function for the deckman.images.cleanup package where the data structures involved in the removeAll operation
	 * represent mostly directory contents and matches between two items of content follow specific rules that go beyond just
	 * determing if their shortnames are equal (using the String.equals() method).
	 * @param objs Object[], the file names that are to be removed from the list this class represents. Should be either an
	 * array of Strings or EquatableImageFileNames to avoid resorting back to the standard removeAll method of the Collection
	 * interface.
	 */
    public boolean removeAll(Object[] objs){
	if(objs.length == 0){
	    return true;
	}
	
	    /* Note that objs instanceof String[] is not being attempted here.
	       This is because only objs instance of Object[] will return true*/
	if(objs[0] instanceof String){
	    Arrays.sort((String[])objs, String.CASE_INSENSITIVE_ORDER);
	}
	    /* Note that objs instanceof EquatableImageFileName[] is not being attempted here.
	       This is because only objs instance of Object[] will return true*/
	else if(objs[0] instanceof EquatableImageFileName){
	    Arrays.sort(objs);	    
	}	
	else{
	    return super.removeAll(Arrays.asList(objs));
	}
	
	LinkedList list = new LinkedList(Arrays.asList(objs));	// no need to sort as backing array was already sorted.
	FasterArrayList leftover = new FasterArrayList();
	Iterator iter1 = this.iterator();
	while(iter1.hasNext()){
	    Object a = iter1.next();
	    Iterator iter2 = list.iterator();
	    while(iter2.hasNext()){
		Object b = iter2.next();
		if(a instanceof EquatableImageFileName){
		    if(a.equals(b)){
			iter1.remove();
			iter2.remove();
			break;
		    }
		}
		else if(b instanceof EquatableImageFileName){
		    if(b.equals(a)){
			iter1.remove();
			iter2.remove();
			break;
		    }
		}
		else{	// both are strings
		    if((((String)a)).equalsIgnoreCase((String)b)){
			iter1.remove();
			iter2.remove();
			break;
		    }
		}
	    }
	}
	return true;
	
    }
    
    
    public static void main(String[] args){
	List list = new LinkedList(Arrays.asList(new String[]{"a", "b", "c", "A", "B", "C"}));
	
	System.out.println("");
	Iterator iter = list.iterator();
	while(iter.hasNext()){
	    System.out.println(iter.next());
	}
	
	String[] aList = {"a", "b", "c", "A", "B", "C"};
	Arrays.sort(aList, String.CASE_INSENSITIVE_ORDER);
	list = Arrays.asList(aList);
	System.out.println("");
	iter = list.iterator();
	while(iter.hasNext()){
	    System.out.println(iter.next());
	}
    }
}
