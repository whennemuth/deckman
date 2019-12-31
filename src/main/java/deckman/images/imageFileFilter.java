/*
 * imageFileFilter.java
 *
 * Created on August 1, 2008, 11:44 PM
 *
 */
package deckman.images;

import java.io.*;
import deckman.utils.codeLib;


/**
 * Filters off non image files formats not supported by javax.imageio.ImageIO.write
 * When used as the parameter for java.io.File.listFiles(java.io.FileFilter), the listFiles function returns a String array 
 * of a directories contents as a File array, excluding anything those files determined to be images that meet the expected format(s).
 *
 * @author Warren
 */
public class imageFileFilter implements java.io.FileFilter {



    /** Creates a new instance of imageFileFilter */
    public imageFileFilter() {
    }



    public boolean accept(File pathname) {
        if (pathname.exists()) {
            if (pathname.isDirectory()) {
                return false;
            }
            else {
                String sName = pathname.getName();
                String sExt = sName.substring(sName.lastIndexOf(".") + 1);
                return imageFileFilter.isValidFileExtension(sExt);
            }
        }
        else {
            return false;
        }
    }


    public static enum ImageType {

        GIF, JPG, JPEG, PNG, BMP, WBMP
    };



    public static String[] getValidFileExtensions() {
        return new String[]{"jpg", "jpeg", "gif", "png", "bmp", "wbmp"};
    }



    public static boolean isValidFileExtension(String sExt) {
        String[] aExt = getValidFileExtensions();
        for (int i = 0; i < aExt.length; i++) {
            if (sExt.startsWith(".")) {
                sExt = sExt.substring(1);	// turn ".ext" into "ext"
            }
            if (aExt[i].equalsIgnoreCase(sExt.trim())) {
                return true;
            }
        }
        return false;
    }



    public static boolean hasValidFileExtension(String sFileName) {
        String sExt = codeLib.getFileExtension(sFileName);
        if (sExt.length() == 0) {
            return false;
        }
        else {
            return isValidFileExtension(sExt);
        }
    }
}
