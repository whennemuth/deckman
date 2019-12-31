package deckman.utils;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import javax.servlet.http.*;


/**
 * This class contains mostly static functions that are generic enough not to place in 
 * any of the other more specialized classes of this project.
 */
public class codeLib {

    /**
     * Creates a new instance of codeLib
     */
    public codeLib() {
    }



    /**
     * This function provides a quick way to establish if the file represented by a String path exists or not.
     * @param sName The absolute path name of the file whose existence is being verified
     * @return boolean 
     */
    public static boolean fileExists(String sName) {
        boolean bRetval = false;
        File f = new File(sName);
        if (f.exists()) {
            bRetval = true;
        }
        return bRetval;
    }



    /**
     * This function provides a quick way to establish if the directory represented by a String path exists and is a directory.
     * @param sPath The absolute path name of the directory whose existence is being verified
     * @return boolean 
     */
    public static boolean directoryExists(String sPath) {
        boolean bRetval = false;
        File f = new File(sPath);
        if (f.exists()) {
            if (f.isDirectory()) {
                bRetval = true;
            }
        }
        return bRetval;
    }



    /**
     * This function will return a String representing the stacktrace of an Exception
     * @param e Exception - An instance of java.lang.Exception
     * @return String - The String representation of the Exception
     */
    public static String getStackTraceAsString(Throwable e) {
        String sRetval = "";
        OutputStream out = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(out);
        e.printStackTrace(pw);
        pw.flush();
        sRetval = out.toString();
        pw.close();
        return sRetval;
    }



    /**
     * Deletes all files and subdirectories under and including dir. Returns true if all deletions were successful.
     * If a deletion fails, the method stops attempting to delete and returns false.
     * @param dir java.io.File - the directory to delete
     * @return true/false - indicates if the directory was deleted or not
     */
    public static boolean deleteDirectory(File dir) {
        try {
            if (dir.isDirectory()) {
                String[] children = dir.list();
                for (int i = 0; i < children.length; i++) {
                    boolean success = deleteDirectory(new File(dir, children[i]));
                    if (!success) {
                        return false;
                    }
                }
            }
            return dir.delete();
        }
        catch (Exception e) {
            return false;
        }
    }



    /**
     * Deletes all files and subdirectories under and including dir. Returns true if all deletions were successful.
     * If a deletion fails, the method stops attempting to delete and returns false.
     * @param sDir java.lang.String - the path of the directory to delete
     * @return true/false - indicates if the directory was deleted or not
     */
    public static boolean deleteDirectory(String sDir) {
        File dir = new File(sDir);
        if (dir.exists()) {
            return deleteDirectory(dir);
        }
        else {
            return false;
        }
    }



    /**
     * Deletes all files and subdirectories under but not including dir. Returns true if all deletions were successful.
     * If a deletion fails, the method continues attempting to delete remaining files/directories, but returns false overall.
     * @param dir java.io.File - the directory whose contents are to be deleted.
     * @param limitFiles java.io.File[] - if contents of dir are not in this they will not be deleted. If null, all contents are deleted.
     * @return true/false - indicates if all contents of dir were deleted or not.
     */
    public static boolean clearDirectory(File dir, File[] limitFiles) {
        boolean bRetval = true;
        try {
            if (dir.exists()) {
                if (dir.isDirectory()) {
                    File[] children = limitFiles == null ? dir.listFiles() : limitFiles;
                    for (int i = 0; i < children.length; i++) {
                        File child = children[i];
                        if (child.isDirectory()) {
                            if (!deleteDirectory(child)) {
                                bRetval = false;
                            }
                        }
                        else {
                            if (!child.delete()) {
                                bRetval = false;
                            }
                        }
                    }
                }
                else {
                    bRetval = false;
                }
            }
            else {
                bRetval = false;
            }
        }
        catch (Exception e) {
            bRetval = false;
        }
        return bRetval;
    }



    /**
     * Deletes all files and subdirectories under but not including dir. Returns true if all deletions were successful.
     * If a deletion fails, the method continues attempting to delete remaining files/directories, but returns false overall.
     * @param dir java.io.File - the directory whose contents are to be deleted.
     * @return true/false - indicates if all contents of dir were deleted or not.
     */
    public static boolean clearDirectory(File dir) {
        return clearDirectory(dir, null);
    }



    /**
     * Deletes all files and subdirectories under but not including dir. Returns true if all deletions were successful.
     * If a deletion fails, the method continues attempting to delete remaining files/directories, but returns false overall.
     * @param sDir java.lang.String - the path of the directory whose contents are to be deleted.
     * @return true/false - indicates if all contents of dir were deleted or not.
     */
    public static boolean clearDirectory(String sDir) {
        File dir = new File(sDir);
        return clearDirectory(dir);
    }



    /**
     * Deletes all files and subdirectories under but not including dir. Returns true if all deletions were successful.
     * If a deletion fails, the method continues attempting to delete remaining files/directories, but returns false overall.
     * @param dir java.io.File - the directory whose contents are to be deleted.
     * @param filter java.io.FileFilter - if contents of dir are not defined by this filter they will not be deleted. 
     * If null, all contents are deleted.
     * @return true/false - indicates if all contents of dir were deleted or not.
     */
    public static boolean clearDirectory(String sDir, FileFilter filter) {
        File dir = new File(sDir);
        if (filter == null) {
            File[] files = dir.listFiles(filter);
            return clearDirectory(dir, files);
        }
        else {
            return clearDirectory(dir);
        }
    }



    public static String[] moveFile(String sFile, String sDirTo, boolean bAvoidOverwrite) {
        return moveFile(new File(sFile), new File(sDirTo), bAvoidOverwrite);
    }



    public static String[] moveFile(String sFile, File dirTo, boolean bAvoidOverwrite) {
        return moveFile(new File(sFile), dirTo, bAvoidOverwrite);
    }



    public static String[] moveFile(File f, String sDirTo, boolean bAvoidOverwrite) {
        return moveFile(f, new File(sDirTo), bAvoidOverwrite);
    }



    /**
     * This function moves a file from one directory to another.
     * @param f java.io.File, the file that is to to be moved.
     * @param dirTo java.io.File, the directory that the file is being moved to.
     * @param bAvoidOverwrite boolean, if true, a file with the same name will not be overwritten because a unique name for the 
     * file being moved is generated and used in the renameTo method.
     * @return String[] - This is a two cell Array that indicates how moving the file went:
     *    a) file does not exist: 
     *		cell 1 = null
     *		cell 2 = null
     *    b) file exists, destination does not exist or renameTo returns false:
     *		cell 1 = short name of file before moving it
     *		cell 2 = null
     *	  c) file exists and renameTo returns true:
     *		cell 1 = short name of file before moving it
     *		cell 2 = short name of file in its new location
     */
    public static String[] moveFile(File f, File dirTo, boolean bAvoidOverwrite) {
        String[] sRetval = {null, null};
        if (f.exists()) {
            String sShortName = f.getName();
            sRetval[0] = sShortName;
            if (dirTo.exists() && dirTo.isDirectory()) {
                if (bAvoidOverwrite) {
                    sShortName = getUniqueFileName(sShortName, dirTo);
                }
                String sFileTo = dirTo.getPath() + File.separator + sShortName;
                if (f.renameTo(new File(sFileTo))) {
                    sRetval[1] = sShortName;
                }
            }
        }
        return sRetval;
    }



    /**
     * This function moves the contents of one directory to another. This should result in an empty directory from where the files
     * are being moved, unless a filter is passed in and the filter doesn't match at least one file.
     * @param dirFrom java.io.File, the directory whose contents are to be moved.
     * @param dirTo java.io.File, the directory that files are being moved to.
     * @param filter java.io.FileFilter, only those files that are matched by the filter will be moved.
     * @param bAvoidOverwrite boolean, if true, a file with the same name will not be overwritten because a unique name for the 
     * file being moved is generated and used in the renameTo method.
     * @return java.util.ArrayList - The ArrayList will have the same number of items as files qualifying for moving were found
     * (all files if no FileFilter). The success/failure of each move is indicated by the String Array found in each ArrayList
     * item as described in function moveFile().
     */
    public static ArrayList moveDirectoryContents(File dirFrom, File dirTo, FileFilter filter, boolean bAvoidOverwrite) {
        ArrayList alist = new ArrayList();

        if (dirFrom.exists() && dirTo.exists()) {
            if (dirFrom.isDirectory() && dirTo.isDirectory()) {
                File[] files = null;
                if (filter == null) {
                    files = dirFrom.listFiles();
                }
                else {
                    files = dirFrom.listFiles(filter);
                }
                for (int i = 0; i < files.length; i++) {
                    String[] shortNames = moveFile(files[i], dirTo, bAvoidOverwrite);
                    if (shortNames[0] != null && shortNames[1] != null) {
                        alist.add(shortNames);
                    }
                }
            }
        }

        return alist;
    }



    public static ArrayList moveDirectoryContents(File dirFrom, File dirTo) {
        return moveDirectoryContents(dirFrom, dirTo, null, false);
    }



    public static ArrayList moveDirectoryContents(String sDirFrom, String sDirTo, boolean bAvoidOverwrite) {
        File dirFrom = new File(sDirFrom);
        File dirTo = new File(sDirTo);
        return moveDirectoryContents(dirFrom, dirTo, null, bAvoidOverwrite);
    }



    public static ArrayList moveDirectoryContents(String sDirFrom, String sDirTo) {
        File dirFrom = new File(sDirFrom);
        File dirTo = new File(sDirTo);
        return moveDirectoryContents(dirFrom, dirTo, null, false);
    }



    public static ArrayList moveDirectoryContents(String sDirFrom, String sDirTo, FileFilter filter) {
        File dirFrom = new File(sDirFrom);
        File dirTo = new File(sDirTo);
        return moveDirectoryContents(dirFrom, dirTo, filter, false);
    }



    public static ArrayList moveDirectoryContents(String sDirFrom, String sDirTo, FileFilter filter, boolean bAvoidOverwrite) {
        File dirFrom = new File(sDirFrom);
        File dirTo = new File(sDirTo);
        return moveDirectoryContents(dirFrom, dirTo, filter, bAvoidOverwrite);
    }



    /**
     * Returns a filename filter that filters off all directories.
     */
    public static FilenameFilter getFilenameFilter() {
        FilenameFilter filter = new FilenameFilter() {

            public boolean accept(File dir, String sShortName) {
                boolean bRetval = false;
                String sFullName = dir.getPath() + File.separator + sShortName;
                File f = new File(sFullName);
                if (f.exists()) {
                    bRetval = f.isFile();
                }
                return bRetval;
            }
        };
        return filter;
    }



    /**
     * This function is used to adjust the shortname of a file (sName) so that it can be saved in targetDir without a name conflict.
     * @param sName String, the name of the file for which a file with a duplicate name is being searched for in targetDir.
     * It is assumed that this is a shorname, the path portion having been stripped off before passing in this parameter.
     * @param targetDir java.io.File, the directory against whose contents the need for a unique name for sName is determined.
     * @param iIdx Integer, If a file whose shortname matches sName is found in targetDir, this function recurses, passing in
     * in an integer (iIdx) increased by one on every recursion to be used in assigning a new name for sName.
     * @return String - a unique name for sName, making it possible to save the corresonding file in targetDir without a
     * name conflict. If an error is encountered, the function returns null.
     */
    private static String getUniqueName(String sShortName, File targetDir) {
        String sNewShortName = null;

        String[] sNames = targetDir.list(getFilenameFilter());  // get an array of all file names in targetDir, excluding directories.
        if (containsIgnoreCase(sNames, sShortName)) {         // a file with name sShortName already exists in targetDir
            String sExt = getFileExtension(sShortName);     // save the file extension on the side. "" if no file extension
            sShortName = removeFileExtension(sShortName);   // remove the file extension for now. Remains the same if no extension

            Pattern p = Pattern.compile("__(\\d+)$");
            Matcher m = p.matcher(sShortName);
            if (m.find()) {   // change a name like "filename__x.ext" to "filename__[x+1].ext"
                int i = Integer.valueOf(m.group(1));
                String sInt = String.valueOf(++i);
                sShortName = m.replaceAll("");
                sShortName += ("__" + sInt + "." + sExt);
            }
            else {
                sShortName += ("__1." + sExt);
            }
            sNewShortName = getUniqueName(sShortName, targetDir);   // recursion here
        }
        else {   // sShortName does not conflict with any filename in targetDir
            sNewShortName = sShortName;
        }

        return sNewShortName;
    }



    public static String getUniqueFileName(String sName, File targetDir) {
        String sRetval = null;

        String sShortName = removePathFromFileSpec(sName);

        if (targetDir.exists()) {
            if (targetDir.isDirectory()) {
                sRetval = getUniqueName(sShortName, targetDir);    //call the recursive function
            }
        }

        return sRetval;
    }



    public static String getUniqueFileName(File f, File targetDir) {
        return getUniqueFileName(f.getName(), targetDir);
    }



    public static String getUniqueFileName(String sName, String sTargetDir) {
        String sRetval = null;
        sTargetDir = sTargetDir.trim();
        if (sTargetDir.endsWith(File.separator)) {
            sTargetDir = sTargetDir.substring(0, sTargetDir.length() - 1);
        }
        sRetval = getUniqueFileName(sName, new File(sTargetDir));
        return sRetval;
    }



    public static String getUniqueFileName(File f, String sTargetDir) {
        return getUniqueFileName(f.getName(), sTargetDir);
    }



    public static String removePathFromFileSpec(String sFileSpec) {
        sFileSpec = sFileSpec.trim();
        int idx = sFileSpec.lastIndexOf(File.separator);
        String sShortName = idx == -1 ? sFileSpec : sFileSpec.substring(++idx);
        return sShortName;
    }



    /**
     * This function removes any pathlike portion of a string. The assumption is that the File.separator character
     * alone is not what is used to create a path.<p>
     * EXAMPLES:<p>
     * <code>
     *    removePathlikePortionFromFileSpec("mypath\\myfile.txt") yields... "myfile.txt"<br>
     *    removePathlikePortionFromFileSpec("/mypath/myfile.txt") yields... "myfile.txt"<br>
     *    removePathlikePortionFromFileSpec("/mypath\\dir/dir\\dir/myfile.txt") yields... "myfile.txt"<br>
     *    removePathlikePortionFromFileSpec("mypath|myfile.txt") yields... "myfile.txt" (if File.separator is "|")
     * </code>
     * @param sFileSpec String, The Full path + filename from which filename only is to be returned.
     * @return String, The filename absent the path information
     */
    public static String removePathlikePortionFromFileSpec(String sFileSpec) {
        String sPattern1 = "\\\\([^\\\\]+$)";	// windows
        String sPattern2 = "/([^/]+$)";		// linux/unix
        String sPattern3 = "\\" + File.separator + "([^\\" + File.separator + "]+$)";	// any platform

        String[] patterns = new String[]{
            /*      windows: */"\\\\([^\\\\]+$)",
            /*        linux: */ "/([^/]+$)",
            /* any platform: */ "\\" + File.separator + "([^\\" + File.separator + "]+$)"
        };

        Pattern p = null;
        Matcher m = null;

        for (int i = 0; i < patterns.length; i++) {
            p = Pattern.compile(patterns[i]);
            m = p.matcher(sFileSpec);
            if (m.find()) {
                sFileSpec = m.group(1);
            }
        }

        return sFileSpec;
    }



    public static String getFileExtension(String sFileName) {
        String sRetval = "";
        String sPattern = "\\.(\\w+)$";
        Pattern p = Pattern.compile(sPattern);
        Matcher m = p.matcher(sFileName);
        if (m.find()) {
            sRetval = m.group(1);
        }
        return sRetval;
    }



    public static String switchFileExtension(String sFileName, String sNewExt) {
        if (!sNewExt.startsWith(".")) {
            sNewExt = "." + sNewExt;
        }
        int iDotAt = sFileName.lastIndexOf(".");

        if (!endsWithIgnoreCase(sFileName, sNewExt)) {
            if (iDotAt == -1) {	// sFileName has no dot character in it.
                sFileName += sNewExt;
            }
            else {
                sFileName = sFileName.substring(0, iDotAt) + sNewExt;
            }
        }

        return sFileName;
    }



    public static String removeFileExtension(String sFileName) {
        String sPattern = "\\.\\w+$";
        Pattern p = Pattern.compile(sPattern);
        Matcher m = p.matcher(sFileName);
        if (m.find()) {
            sFileName = m.replaceAll("");
        }
        return sFileName;
    }



    public static String getPathFromFileSpec(String sFileSpec) {
        if (sFileSpec.indexOf(File.separator) == -1) {
            return "";
        // alternately, figure out a way to determine the "current" directory and return that instead
        }
        else {
            return sFileSpec.substring(0, sFileSpec.lastIndexOf(File.separator));
        }
    }



    /**
     * The java class loader can determine the {@link <a href="http://java.sun.com/javase/6/docs/api/java/net/URL.html">URL</a>} of a class it loaded using the
     * {@link <a href="http://java.sun.com/javase/6/docs/api/java/lang/Class.html#getResource(java.lang.String)">getResource</a>} method. This function takes advantage of
     * that fact and builds a platform specific file path to the class from the {@link <a href="http://java.sun.com/javase/6/docs/api/java/net/URL.html">URL</a>}.
     * @param cls Class, The {@link <a href="http://java.sun.com/javase/6/docs/api/java/lang/Class.html">Class</a>} object representing a class whose physical
     * location is being determined.
     * @param sResource String, A String from which the {@link <a href="http://java.sun.com/javase/6/docs/api/java/lang/Class.html">Class</a>} object can get a
     * {@link <a href="http://java.sun.com/javase/6/docs/api/java/net/URL.html">URL</a>} using the {@link <a href="http://java.sun.com/javase/6/docs/api/java/lang/Class.html#getResource(java.lang.String)">getResource</a>} method.
     * For example: "/java/lang/String.class"
     */
    public static String getResourceFilePath(Class cls, String sResource) {
        // http://www.javaworld.com/javaworld/javaqa/2003-08/01-qa-0808-property.html?page=1
        java.net.URL url = cls.getResource(sResource);
        String sFileSpec = url.getPath();
        String[] aSplit = sFileSpec.split("/");
        String sFragment = "";
        sFileSpec = "";
        for (int i = 0; i < (aSplit.length - 1); i++) {
            sFragment = aSplit[i];
            if (!sFragment.equals("")) {
                sFileSpec += sFragment;
                if (i != (aSplit.length - 2)) {
                    sFileSpec += File.separator;
                }
            }
        }

        // If on a Linux system...
        if (File.separator.equals("/")) {
            if (!sFileSpec.startsWith("/")) {
                sFileSpec = "/" + sFileSpec;
            }
        }

        return sFileSpec;
    }



    public static boolean containsIgnoreCase(String[] a, String b) {
        boolean bFound = false;
        for (int i = 0; i < a.length; i++) {
            if (b.equalsIgnoreCase(a[i])) {
                bFound = true;
            }
        }
        return bFound;
    }



    public static boolean endsWithIgnoreCase(String s, String sEnd) {
        s = s.toUpperCase();
        sEnd = sEnd.toUpperCase();
        return s.endsWith(sEnd);
    }



    /**
     * This function will take an ArrayList of Strings, remove from it any items that do not match a pattern formed
     * by adding a numeric value at the end of a specified base (sBase). Next it will sort the ArrayList and return it.
     * Typically used to isolate and sort certain members of request.getParameterNames where request is an
     * HttpServletRequest instance. Despite their ordered layout in an HTML page the getParameterNames() function
     * returns the field names in a random order that does not reflect their order in the HTML page).
     * Sample usage:
     *          // an HTML form has - amoung others - checkbox elements named "chk1", "chk2", "chk3". An ArrayList is
     *          // needed that contains just the names of these elements sorted by their numeric ending.
     *      ArrayList alist = Collections.list(request.getParameterNames());
     *      ArrayList aFilteredSorted = getSortedIndexedSubset(alist, "chk");
     * @param alist java.util.ArrayList, The ArrayList that is to be filtered and sorted.
     * @param sBase String, After filtering the ArrayList, only items where alist.startsWith(sBase) == true will remain.
     * @return ArrayList, The filtered and sorted ArrayList.
     */
    public static ArrayList getSortedIndexedSubset(List alist, final String sBase) {
        // remove items that do not match sRegex
        filterableArrayList flist = new filterableArrayList(alist);
        filterableArrayList.listFilter filter = new filterableArrayList.listFilter() {

            public boolean isMatched(String listItem) {
                boolean bMatched = false;
                if (listItem.startsWith(sBase)) {
                    if (listItem.length() > sBase.length()) {
                        String sIdx = listItem.substring(sBase.length());
                        if (sIdx.matches("^\\d+$")) {
                            bMatched = true;
                        }
                    }
                }
                return bMatched;
            }
        };
        flist.filterFor(filter);

        // sort the resulting list so that the members are in numerical order of their suffixes.
        Comparator comp = new Comparator() {

            public int compare(Object s1, Object s2) {
                int i1 = Integer.valueOf(((String) s1).substring(sBase.length()));
                int i2 = Integer.valueOf(((String) s2).substring(sBase.length()));
                return (i1 - i2);
            }



            public boolean equals(Object o) {
                return super.equals(o);
            }
        };
        Collections.sort(flist, comp);

        return flist;
    }



    /**
     * Returns List containing a servlet request parameter value for each servlet request parameter name
     * found in the provided List (FormParameterNames)
     * @param FormParameterNames java.util.List, the List of servlet request parameter names
     * @return List, the List of servlet request parameter values.
     */
    public static List getFormParameterValues(List FormParameterNames, HttpServletRequest request) {
        List Values = new ArrayList();
        if (!FormParameterNames.isEmpty()) {
            Iterator iter = FormParameterNames.iterator();
            while (iter.hasNext()) {
                String sParamName = (String) (iter.next());
                String sParamVal = (String) (request.getParameter(sParamName));
                Values.add(sParamVal);
            }
        }
        return Values;
    }



    /**
     * This function is just a placeholder. Will be complete when some way of determining if a file is an image
     * by doing something more sophisticated than checking the file extension is found
     */
    public static boolean isImageFile(File f) {
        return true;
    }



    /**
     * "yes", "on", and "true" should all be interpreted as Boolean.TRUE.booleanValue() (comparisons are case-insensitive).
     * Everything else should be interpreted as Boolean.FALSE.booleanValue().
     * @param s String, The string to be interpreted as a boolean value.
     * @return boolean, the interpreted boolean value
     */
    public static boolean interpretStringAsBooleanValue(String s) {
        if (s.trim().equalsIgnoreCase("yes")) {
            return true;
        }
        else if (s.trim().equalsIgnoreCase("on")) {
            return true;
        }
        else if (s.trim().equalsIgnoreCase("true")) {
            return true;
        }
        else {
            return false;
        }
    }



    /**
     * This function compares the URL of the webpage that requested the current page to the URL of the current
     * webpage to determine if they are from the same website. The comparison concentrates on the portion of
     * each URL before the first non-repeating "/" character. If these portions of each URL are equal (case ignored),
     * then the two URLs are considered to be from the same website. If they are not, then the current request is
     * the first one to be made of this website for the current session.
     * @param request javax.servlet.http.HttpServletRequest, The request from which the prior and current webpages
     * will be identified.
     * @return boolean, Indicates if the current request is the first of the session.
     */
    public static boolean requestIsFirstOfSession(javax.servlet.http.HttpServletRequest request) {
        String sReferer = request.getHeader("referer");
        if (sReferer == null) {
            return true;
        }
        else {
            String sURL = request.getRequestURL().toString();
            /* the following regex pattern is composed of a single "/" character sandwiched between one
            negative lookbehind and one negative lookahead for the same character. This ensures the
            "/" matched will does not immediately follow or precede another "/" */
            String sPattern = "(?<!/)/(?!/)";
            String sWebsite1 = sReferer.split(sPattern)[0];
            String sWebsite2 = sURL.split(sPattern)[0];
            return (!sWebsite1.equalsIgnoreCase(sWebsite2));
        }
    }



    public static void test(javax.servlet.http.HttpServletRequest request, javax.servlet.ServletContext app, javax.servlet.jsp.JspWriter out) {
        try {
            java.net.URL url = null;

            /*
            url = ClassLoader.getSystemClassLoader().getResource("/deckman/settings/initialization.class");
            out.println("ClassLoader.getSystemClassLoader().getResource(\"/deckman/settings/initialization.class\")");
            out.println("<br>");
            out.println(url.getPath());
            out.println("<p>");
             */

            url = deckman.settings.initialization.class.getResource("/deckman/settings/initialization.class");
            out.println("deckman.settings.initialization.class.getResource(\"/deckman/settings/initialization.class\")");
            out.println("<br>");
            out.println(url.getPath());
            out.println("<p>");

            url = Thread.currentThread().getContextClassLoader().getResource("deckman/settings/initialization.class");
            out.println("Thread.currentThread().getContextClassLoader().getResource(\"deckman/settings/initialization.class\")");
            out.println("<br>");
            out.println(url.getPath());
            out.println("<p>");

            String sPath = app.getRealPath("");
            if (!sPath.endsWith(File.separator)) {
                sPath += File.separator + "images";
            }
            File imgDir = new File(sPath);
            boolean bDirOK = imgDir.exists() && imgDir.isDirectory();
            out.println("image directory path:");
            out.println("<br>");
            out.println(sPath);
            out.println("<br>");
            out.println(bDirOK ? "SUCCESS: exists as a directory" : "ERROR: does not exist or is not a directory");
            out.println("<p>");



        }
        catch (IOException e) {
        }
    }



    public static void main(String[] args) {

        int iTest = 0;

        switch (iTest) {
            // delete from a directory files whose name match a certain pattern.
            case 1:
                final ArrayList aFiles = new ArrayList();
                aFiles.add("!!!b-4");
                aFiles.add("!!agony");
                aFiles.add("!victory!");
                aFiles.add("1 beam gone");
                aFiles.add("1 clean");
                File dir = new File("C:\\myTechnicalStuff\\projects\\java\\projects\\netbeans\\web\\deckman\\web\\images\\thumbnails");
                FileFilter filter = new FileFilter() {

                    public boolean accept(File f) {
                        return aFiles.contains(f.getName().replaceAll("\\.[a-zA-Z]{3,4}$", ""));
                    }
                };
                File[] files = dir.listFiles(filter);
                for (int i = 0; i < files.length; i++) {
                    ((File) files[i]).delete();
                }

                dir = new File("C:\\myTechnicalStuff\\projects\\java\\projects\\netbeans\\web\\deckman\\web\\images\\fullsize");
                files = dir.listFiles(filter);
                for (int i = 0; i < files.length; i++) {
                    File f = ((File) files[i]);
                    f.renameTo(new File(f.getParentFile().getParentFile().getPath() + File.separator + "temp\\reimage\\in\\" + f.getName()));
                }

                break;
            case 2:
                /* test filtering for and then sorting by "chk\d" from an ArrayList of strings, where \d is
                 * an integer to sort by. This is functionality used redisplay the values of only checkbox
                 * elements with name attribute of "chk\d" posted by an HTML form in order of their numeric
                 * suffix "\d" */
                ArrayList alist = new ArrayList();
                alist.add("chk2");
                alist.add("oranges");
                alist.add("pears");
                alist.add("chk3");
                alist.add("bannanas");
                alist.add("grapes");
                alist.add("chk1");
                alist.add("3chk4");
                alist.add("apr5icots7");
                codeLib u = new codeLib();

                ArrayList aFilteredSorted = getSortedIndexedSubset(alist, "chk");
                Iterator iter2 = aFilteredSorted.iterator();
                while (iter2.hasNext()) {
                    System.out.println(iter2.next());
                }
                break;
            case 3:
                /* This code will move the contents of one directory to another directory assigning unique names
                 * to them where the target directory already contains contents with the same name */
                File from1 = new File("C:\\Documents and Settings\\Warren\\Desktop\\from1");
                File from2 = new File("C:\\Documents and Settings\\Warren\\Desktop\\from2");
                File to = new File("C:\\Documents and Settings\\Warren\\Desktop\\to");

                File[] files1 = from1.listFiles();
                File[] files2 = from2.listFiles();

                for (int i = 0; i < files1.length; i++) {
                    String sNewName = getUniqueFileName(files1[i].getName(), to);
                    sNewName = to.getPath() + File.separator + sNewName;
                    System.out.println(files1[i].getName() + "... to ... " + sNewName);
                    files1[i].renameTo(new File(sNewName));
                }

                for (int i = 0; i < files2.length; i++) {
                    String sNewName = getUniqueFileName(files2[i].getName(), to);
                    sNewName = to.getPath() + File.separator + sNewName;
                    System.out.println(files2[i].getName() + "... to ... " + sNewName);
                    files2[i].renameTo(new File(sNewName));
                }

                String sDirFrom = "C:\\Documents and Settings\\Warren\\Desktop\\zip1";
                String sDirTo = "C:\\Documents and Settings\\Warren\\Desktop\\zip1a";
                System.out.println(moveDirectoryContents(sDirFrom, sDirTo));
                //System.out.println(deleteDirectory(new File("C:\\Documents and Settings\\Warren\\Desktop\\zip5")));
                //System.out.println(clearDirectory(new File("C:\\Documents and Settings\\Warren\\Desktop\\zip4")));
                break;
        }
    }
}
