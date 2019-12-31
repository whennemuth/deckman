package deckman.settings;

import java.io.*;
import java.util.*;
import javax.servlet.http.*;

//The reinitialization class does everything the initialization (accessor) class does but extends it to act as a setter of all fields as well (mutator)
public class reinitialization extends initialization implements java.io.Serializable {

    private static final int UPDATE = 0;
    private static final int REMOVE = 1;

    // static variable used only from the main method to record incoming parameters
    private static String propfileSpec = null;
    private static String context = null;



    protected reinitialization(String sFile) {
        super(sFile, null);
    }



    protected reinitialization(String sFile, HttpSession session) {
        super(sFile, session);
    }



    public void setProperty(String sName, Object oVal) {
        super.map.put(sName, oVal);
    }



    public void removeProperty(String sName) {
        super.map.remove(sName);
    }



    public synchronized void commitToDisk() throws PropertyToDiskException {
        String sThisMethod = this.getClass().getName() + ".commitToDisk";
        String sFileSpec = super.getInitializationFileSpec();
        File file = new File(sFileSpec);
        String sErrDetail = sFileSpec + "\r\n[" + sThisMethod + "]";

        if (file.exists()) {
            Properties props = new Properties();
            InputStream input;
            OutputStream output;
            try {
                props.load(input = new FileInputStream(file));
                input.close();

                props.clear();
                HashMap map = super.getStringMap();   //make all map values strings.
                props.putAll(map);
                props.store(output = new FileOutputStream(file), "THESE ARE THE COMMENTS");
                output.close();
            }
            catch (FileNotFoundException e) {
                throw new PropertyToDiskException("Property file not found:\r\n" + sErrDetail, e);
            }
            catch (IOException e) {
                throw new PropertyToDiskException("Unable load, write to, or save property file:\r\n" + sErrDetail, e);
            }
            catch (Exception e) {
                throw new PropertyToDiskException("Unexpected exception while processing property file:\r\n" + sErrDetail, e);
            }
        }
        else {
            try {
                file.createNewFile();   //in case the file somehow got deleted since the super class was instantianted and initialized
                commitToDisk();
                this.resetInitSingleton(this);
            }
            catch (IOException e) {
                throw new PropertyToDiskException("Unable to create property file:\r\n" + sErrDetail, e);
            }
        }
    }


    public class PropertyToDiskException extends Exception {

        private String sMsg = "";
        private Exception e = null;



        public PropertyToDiskException(String sMsg, Exception e) {
            this.sMsg = sMsg;
        }



        public String getMessage() {
            return this.sMsg;
        }



        public Exception wrappedException() {
            return e;
        }
    }


    public static String usage = "USAGE:\r\n" +
            "    file    [name of properties file with/without full path],\r\n" +
            "    context [refer to one of 3 potential contexts the properties would apply to:\r\n" +
            "        - local [the development environment, my own PC],\r\n" +
            "        - deckman [the deckman website (thedeckmancapecod.com)],\r\n" +
            "        - deckmantest [the subdomain deckman website, (test.thedeckmancapecod.com)]\r\n\r\n" +
            "EXAMPLE:\r\n" +
            "java reinitialziation file=\"c:\\somedir\\..\\" + initialization.DEFAULT_PROP_FILE_NAME + "\", context=deckmantest";



    public static boolean parseArgs(String[] args) {
        if (args.length == 0) {
            return false;
        }
        else {
            for (int i = 0; i < args.length; i++) {
                if (args[i].matches("^[^=]+=.*$")) {
                    int idx = args[i].indexOf("=");
                    String sName = args[i].substring(0, idx).trim();
                    String sVal = args[i].substring(idx+1).trim();
                    if (sName.equalsIgnoreCase("file")) {
                        propfileSpec = sVal;
                    }
                    else if (sName.equalsIgnoreCase("context")) {
                        if(     !sVal.equalsIgnoreCase("local") &&
                                !sVal.equalsIgnoreCase("deckman") &&
                                !sVal.equalsIgnoreCase("deckmantest")){
                            return false; // the context parameter is not one of the 3 choices
                        }
                        context = sVal;
                    }
                }
                else {
                    return false;
                }
            }

            if (propfileSpec == null || context == null) {
                return false;
            }
            else {
                return true;
            }
        }
    }



    public static void main(String[] args) {

        //args = new String[]{"file="+initialization.DEFAULT_PROP_FILE_NAME, "context=deckmantest"};
        args = new String[]{"file=C:\\Documents and Settings\\Warren\\Desktop\\" +
                initialization.DEFAULT_PROP_FILE_NAME, "context=deckmantest"};

        if(parseArgs(args)){

            reinitialization r = (reinitialization) initialization.getInstance(propfileSpec);

            if (context.equalsIgnoreCase("deckman")) {
                    // this will produce the default properties file for the actual live deckman website
                if (!File.separator.equals("/")) {
                        // taking this fact as an indication that the class IS NOT running on a Linux machine
                    System.out.println("WARNING: This code assumes that the \"deckman\" context " +
                            "refers to a website that is hosted on a Linux platform.\r\n" +
                            "This code is currently running on a non-linux platform and the " +
                            "properties of the file produced match\r\nthe ones needed for the " +
                            "linux server hosting the application at lunarpages.com.");
                }
                
                r.setProperty("DebugMode", Boolean.TRUE);

                // DATABASE
                r.setProperty("DB_Driver", "com.mysql.jdbc.Driver");                    //MYSQL
                //r.setProperty("DB_Driver", "sun.jdbc.odbc.JdbcOdbcDriver");           //MSACCESS
                r.setProperty("DB_ConnectionString", "jdbc:mysql://thedeckmancapecod.com/thede41_deckman");    //MYSQL
                //r.setProperty("DB_ConnectionString", "jdbc:odbc:thede41_deckman");            //MSACCESS
                r.setProperty("DB_UserPwdRequired", r.getObjectFromString("YES"));
                r.setProperty("DB_User", "thede41_warhen");
                r.setProperty("DB_Password", "coffeepayday99");

                // TEMP FOLDERS
                r.setProperty("UnzipInFilePath", "/home/thede41/public_html/images/temp/unzip/in");
                r.setProperty("UnzipOutFilePath", "/home/thede41/public_html/images/temp/unzip/out");
                //r.setProperty("ReimageInFilePath", "/home/thede41/public_html/images/temp/reimage/in");
                // set the reimage in path to be the same as the FullsizeFilePath so that images with no matching thumbs will show up on cleanup.jsp
                r.setProperty("ReimageInFilePath", "/home/thede41/public_html/images/fullsize");
                r.setProperty("ReimageOutFilePath", "/home/thede41/public_html/images/temp/reimage/out");
                r.setProperty("UploadInFilePath", "/home/thede41/public_html/images/temp/upload");
                r.setProperty("UploadOutImageFilePath", "/home/thede41/public_html/images/fullsize");
                r.setProperty("UploadOutZipFilePath", "/home/thede41/public_html/images/temp/unzip/in");

                // PERMANENT FOLDERS
                r.setProperty("FullsizeFilePath", "/home/thede41/public_html/images/fullsize");
                r.setProperty("ThumbsFilePath", "/home/thede41/public_html/images/thumbnails");
                r.setProperty("AdminLogPathName", "/home/thede41/logs/adminLog.log");
                r.setProperty("SiteLogPathName", "/home/thede41/logs/siteLog.log");
                
                // CONTEXT PATHS
                r.setProperty("FullsizeContextPath", "/images/fullsize");
                r.setProperty("ThumbnailContextPath", "/images/thumbnails");
                r.setProperty("ImagesContextPath", "/images");

                // SECURITY
                r.setProperty("RequireAdminPassword", Boolean.FALSE);
                r.setProperty("AdminPassword", "coffeepayday");
                r.setProperty("RequireSessionCookieCheck", Boolean.TRUE);
            }
            else if(context.equalsIgnoreCase("deckmantest")){
                    // this will produce the default properties file for the live deckman test website (test.thedeckmancapecod.com)
                if (!File.separator.equals("/")) {
                        // taking this fact as an indication that the class IS NOT running on a Linux machine
                    System.out.println("WARNING: This code assumes that the \"deckmantest\" context " +
                            "refers to a website that is hosted on a Linux platform.\r\n" +
                            "This code is currently running on a non-linux platform and the " +
                            "properties of the file produced match\r\nthe ones needed for the " +
                            "linux server hosting the application at lunarpages.com.");
                }
                
                r.setProperty("DebugMode", Boolean.TRUE);

                // DATABASE
                r.setProperty("DB_Driver", "com.mysql.jdbc.Driver");                    //MYSQL
                //r.setProperty("DB_Driver", "sun.jdbc.odbc.JdbcOdbcDriver");           //MSACCESS
                r.setProperty("DB_ConnectionString", "jdbc:mysql://thedeckmancapecod.com/thede41_deckman2");    //MYSQL
                //r.setProperty("DB_ConnectionString", "jdbc:odbc:thede41_deckman2");            //MSACCESS
                r.setProperty("DB_UserPwdRequired", r.getObjectFromString("YES"));
                r.setProperty("DB_User", "thede41_warhen");
                r.setProperty("DB_Password", "coffeepayday99");

                // TEMP FOLDERS
                r.setProperty("UnzipInFilePath", "/home/thede41/public_html/test/images/temp/unzip/in");
                r.setProperty("UnzipOutFilePath", "/home/thede41/public_html/test/images/temp/unzip/out");
                //r.setProperty("ReimageInFilePath", "/home/thede41/public_html/test/images/temp/reimage/in");
                // set the reimage in path to be the same as the FullsizeFilePath so that images with no matching thumbs will show up on cleanup.jsp
                r.setProperty("ReimageInFilePath", "/home/thede41/public_html/test/images/fullsize");
                r.setProperty("ReimageOutFilePath", "/home/thede41/public_html/test/images/temp/reimage/out");
                r.setProperty("UploadInFilePath", "/home/thede41/public_html/test/images/temp/upload");
                r.setProperty("UploadOutImageFilePath", "/home/thede41/public_html/test/images/fullsize");
                r.setProperty("UploadOutZipFilePath", "/home/thede41/public_html/test/images/temp/unzip/in");

                // PERMANENT FOLDERS
                r.setProperty("FullsizeFilePath", "/home/thede41/public_html/test/images/fullsize");
                r.setProperty("ThumbsFilePath", "/home/thede41/public_html/test/images/thumbnails");
                r.setProperty("AdminLogPathName", "/home/thede41/logs/testAdminLog.log");
                r.setProperty("SiteLogPathName", "/home/thede41/logs/testSiteLog.log");

                // CONTEXT PATHS
                r.setProperty("FullsizeContextPath", "/images/fullsize");
                r.setProperty("ThumbnailContextPath", "/images/thumbnails");
                r.setProperty("ImagesContextPath", "/images");
                
                // SECURITY
                r.setProperty("RequireAdminPassword", Boolean.FALSE);
                r.setProperty("AdminPassword", "coffeepayday");
                r.setProperty("RequireSessionCookieCheck", Boolean.TRUE);
                
            }
            else if(context.equalsIgnoreCase("local")){
                    // this will produce the default properties file for the deckman development machine (windows-based)
                if (File.separator.equals("/")) {
                        // taking this fact as an indication that the class IS running on a Linux machine
                    System.out.println("WARNING: This code assumes that the \"local\" context " +
                            "refers to a website that is hosted on the windows development platform.\r\n" +
                            "This code is currently running on a linux platform and the " +
                            "properties of the file produced match\r\nthe ones needed for the " +
                            "windows server for testing and development at home.");
                }

                //DEBUG
                r.setProperty("DebugMode", Boolean.TRUE);

                //DATABASE
                r.setProperty("DB_Driver", "com.mysql.jdbc.Driver");                    //MYSQL
                //r.setProperty("DB_Driver", "sun.jdbc.odbc.JdbcOdbcDriver");           //MSACCESS
                r.setProperty("DB_ConnectionString", "jdbc:mysql://localhost/deckman"); //MYSQL
                //r.setProperty("DB_ConnectionString", "jdbc:odbc:deckman");            //MSACCESS
                r.setProperty("DB_UserPwdRequired", r.getObjectFromString("YES"));
                r.setProperty("DB_User", "root");
                r.setProperty("DB_Password", "mypassword");

                //TEMP FOLDERS
                r.setProperty("UnzipInFilePath", "C:\\myTechnicalStuff\\projects\\java\\projects\\netbeans\\web\\deckman\\build\\web\\images\\temp\\unzip\\in");
                r.setProperty("UnzipOutFilePath", "C:\\myTechnicalStuff\\projects\\java\\projects\\netbeans\\web\\deckman\\build\\web\\images\\temp\\unzip\\out");
                //r.setProperty("ReimageInFilePath", "C:\\myTechnicalStuff\\projects\\java\\projects\\netbeans\\web\\deckman\\build\\web\\images\\temp\\reimage\\in");
                // set the reimage in path to be the same as the FullsizeFilePath so that images with no matching thumbs will show up on cleanup.jsp
                r.setProperty("ReimageInFilePath", "C:\\myTechnicalStuff\\projects\\java\\projects\\netbeans\\web\\deckman\\build\\web\\images\\fullsize");
                r.setProperty("ReimageOutFilePath", "C:\\myTechnicalStuff\\projects\\java\\projects\\netbeans\\web\\deckman\\build\\web\\images\\temp\\reimage\\out");
                r.setProperty("UploadInFilePath", "C:\\myTechnicalStuff\\projects\\java\\projects\\netbeans\\web\\deckman\\build\\web\\images\\temp\\upload");
                r.setProperty("UploadOutImageFilePath", "C:\\myTechnicalStuff\\projects\\java\\projects\\netbeans\\web\\deckman\\build\\web\\images\\fullsize");
                r.setProperty("UploadOutZipFilePath", "C:\\myTechnicalStuff\\projects\\java\\projects\\netbeans\\web\\deckman\\build\\web\\images\\temp\\unzip\\in");

                //PERMANENT FOLDERS
                r.setProperty("FullsizeFilePath", "C:\\myTechnicalStuff\\projects\\java\\projects\\netbeans\\web\\deckman\\build\\web\\images\\fullsize");
                r.setProperty("ThumbsFilePath", "C:\\myTechnicalStuff\\projects\\java\\projects\\netbeans\\web\\deckman\\build\\web\\images\\thumbnails");
                r.setProperty("AdminLogPathName", "C:\\myTechnicalStuff\\projects\\java\\projects\\netbeans\\web\\deckman\\build\\web\\logs\\adminLog.log");
                r.setProperty("SiteLogPathName", "C:\\myTechnicalStuff\\projects\\java\\projects\\netbeans\\web\\deckman\\build\\web\\logs\\siteLog.log");

                // CONTEXT PATHS
                r.setProperty("FullsizeContextPath", "/deckman/images/fullsize");
                r.setProperty("ThumbnailContextPath", "/deckman/images/thumbnails");
                r.setProperty("ImagesContextPath", "/deckman/images");
                
                // SECURITY
                r.setProperty("RequireAdminPassword", Boolean.FALSE);
                r.setProperty("AdminPassword", "coffeepayday");
                r.setProperty("RequireSessionCookieCheck", Boolean.TRUE);
            }
            else{
                System.out.println(usage);
                return;
            }

            try {
                r.commitToDisk();
            }
            catch (reinitialization.PropertyToDiskException e) {
                e.printStackTrace();
            }
        }
        else{
            System.out.println(usage);
        }
    }
}
