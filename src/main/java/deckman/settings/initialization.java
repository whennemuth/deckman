package deckman.settings;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import deckman.utils.*;


public class initialization implements java.io.Serializable {

    public static final String DEFAULT_PROP_FILE_NAME = "initialize.prop";
    
    protected String sInstantiationProblem = null;
    protected String sInitializationFileName = null;
    protected String sInitializationFilePath;
    protected HashMap map = new HashMap();
    private static reinitialization initSingleton = null;


    public static enum cacheType {

        SESSION, SINGLETON, DISK
    };



    protected initialization(String sFile) {
        this(sFile, null);
    }



    protected initialization(String sFileSpec, HttpSession session) {

            // 1) GET THE INITIALIZATION INSTANCE STORED IN SESSION IF IT EXISTS
        Object oInit = null;
        if (session != null) {
            if (!session.isNew()) {
                oInit = session.getAttribute("initializer");
            }
            else {
                /* Either the second post/get during the session has not been made yet,
                or the browser of the visitor does not support session cookies. */
            }
        }

            // 2)  IF THE INITIALIZATION SHOULD BE OBTAINED FROM THE INITIALIATION INSTANCE
            // STORED IN SESSION OR IF IT SHOULD BE OBTAINED FROM THE PROPERTIES FILE ON DISK.
        boolean bReloadFromDisk = false;
        if (oInit == null) {
            bReloadFromDisk = true; // session was null, or there was no "initializer" attribute in session
        }
        else {
            reinitialization reinit = (reinitialization) oInit;
            if(sFileSpec != null){
                if(!reinit.matchesFileSpec(sFileSpec)){
                    bReloadFromDisk = true;
                }
            }
        }

            // 3) INITIALIZE THIS INITIALIZATION INSTANCE.
        if(bReloadFromDisk){
            parseFileSpec(sFileSpec, this);
            initializeFromDisk();
            if (session != null) {
                session.setAttribute("initializer", this);
            }
        }
        else{
            initializeFromInstance((reinitialization) oInit);
        }
    }


        /**
         * The user may specify either a filename with full path info., or just the filename.
         * This function will determine which is the case and will set the file name and directory
         * fields of the init object accordingly. If the sFileSpec parameter indicates just a file
         * name, then the property file directory field of the init object will be assigned the
         * path of the deckman.settings package where the initialization.class file is.
         * @param sFileSpec - The name with or without path info of the properties file
         * @param init - The initialization instance whose properties are being set from parsing
         * results against the sFileSpec parameter.
         */
    private void parseFileSpec(String sFileSpec, initialization init){
        if(sFileSpec == null) sFileSpec = initialization.DEFAULT_PROP_FILE_NAME;

        if (sFileSpec.endsWith(File.separator)) {
            sFileSpec = sFileSpec.substring(0, sFileSpec.length() - 1);
        }
        int i = sFileSpec.lastIndexOf(File.separator);
        if (i == -1) {
            init.setInitializationFileName(sFileSpec);
            init.setInitializationFilePathToDefault();
        }
        else {
            init.setInitializationFileName(sFileSpec.substring(i + 1));
            init.setInitializationFilePath(sFileSpec.substring(0, i));
        }
    }


        /**
         * This function determines if the provided file specification, if passed to the constructor
         * of this class would result in an instance whose fields are backed by the same properties
         * file on disk as those of the current instance. This function can be used in the constructor
         * that is passed the session object to determine if it can reuse the initialization instance
         * that is in one of that sessions attributes or if it has to go to disk to load the properties
         * file because sFileSpec indicates a different file.
         * @param sFileSpec - The file specification compared against the property file specs of this instance.
         * @return
         */
    protected boolean matchesFileSpec(String sFileSpec){
        String sFilePath = null;
        String sFileName = null;
        String sPathName = null;

        if (sFileSpec.endsWith(File.separator)) {
                /* there is no obvious reason why the user would have entered a file
                   specification ending in a file separator character. Just being safe. */
            sFileSpec = sFileSpec.substring(0, sFileSpec.length() - 1);
        }

        int i = sFileSpec.lastIndexOf(File.separator);
        if (i == -1) {
            sFileName = sFileSpec;
            sFilePath = initialization.getDefaultInitializationFilePath();
            sPathName = sFilePath + File.separator + sFileName;
        }
        else {
            sPathName = sFileSpec;
        }

        return this.getInitializationFileSpec().equalsIgnoreCase(sPathName);
    }


    /**
     * <b>NOT IN USE.</b>
     * This function overrides the Object.equals function and will determine equality between two initialization
     * instances if each protected variable in it is equal. One of these protected variables is a java.util.Map
     * instance, so a "deepequals" is performed between the two maps where equality is obtained if keysets
     * between the two maps are identical and each value mapped to each keyset equals the corresponding value
     * in the other map.
     * @param obj Object, the wrapped initialization instance being compared to this instance
     * @return boolean, result of the test described above.
     */
    public boolean equals(Object obj) {
        boolean bRetval = true;
        if (obj == null) {
            bRetval = false;
        }
        else if (obj instanceof initialization) {
            initialization init = (initialization) obj;
            if (init.sInitializationFileName.equals(this.sInitializationFileName)) {
                if (init.sInitializationFilePath.equals(this.sInitializationFilePath)) {
                    Set TheseKeys = this.map.keySet();
                    Set ThoseKeys = init.map.keySet();
                    if (TheseKeys.size() == ThoseKeys.size()) {
                        if (TheseKeys.containsAll(ThoseKeys)) {
                            Iterator iter = TheseKeys.iterator();
                            while (iter.hasNext()) {
                                String sThisKey = (String) iter.next();
                                String sThisVal = (String) this.map.get(sThisKey);
                                String sThatVal = (String) init.map.get(sThisKey);
                                if (!sThisVal.equals(sThatVal)) {
                                    bRetval = false;
                                    break;
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
                else {
                    bRetval = false;
                }
            }
            else {
                bRetval = false;
            }
        }
        else {
            bRetval = false;
        }

        return bRetval;
    }



    /**
     * Returns an initialization instance by deriving it from what's in the property file every time. No reference to any cached
     * data. If sFile matches path/[shortname], a file matching [shortname] will be used as the property file. If shortname does
     * not exist, a blank intialization instance will be returned, worthless if it's reinitialization subtype is not being used
     * to create a new property file - if it were, a new property file called [shortname] would be created by
     * reinitialzation.commitToDisk. It is necessary to return the subclass reinitialization because if a reinitialization
     * object were being instantiated, this function would return the type initialization that could be downcast to
     * reinitialization. This is because the initialization instance now references an instance of the reinitialization subclass.
     * Otherwise a ClassCastException would result. You cannot downcast a superclass to one of its subclasses if the superclass
     * does not actually reference an instance of that subclass.
     * @param sFile String, the property file name, with or without path (if no path, a default will be assigned)
     * @return initialization - the initialization instance.
     */
    public static synchronized initialization getInstance(String sFile) {
        return getInstance(sFile, null, initialization.cacheType.DISK);
    }


    /**
     * This instance will be initialized using the properties file with the name
     * {@link #DEFAULT_PROP_FILE_NAME DEFAULT_PROP_FILE_NAME} in the deckman.settings
     * package directory, where initialization.class is.
     * @return
     */
    public static synchronized initialization getInstance(){
        return getInstance(initialization.DEFAULT_PROP_FILE_NAME);
    }


    /**
     * This instance will be initialized using the properties file indicated by the sFile parameter.
     * @param sFile The properties file on which this instance is to be based.
     * @param chtyp Either {@link initialization.cacheType#DISK DISK} or {@link initialization.cacheType#SINGLETON SINGLETON}
     * @return
     */
    public static synchronized initialization getInstance(String sFile, cacheType chtyp) throws java.lang.IllegalArgumentException {
        if(chtyp.equals(cacheType.SESSION)){
            throw new java.lang.IllegalArgumentException("SESSION is not a legal enum type for this method. Use DISK or SINGLETON");
        }
        return getInstance(sFile, null, chtyp);
    }


    /**
     * This instance will be initialized using an existing instance of the initialization
     * class stored in an HttpSession. Basically, this instance will become a "clone" of the
     * session instance.
     * @param session This session should have an attribute called "initializer" which
     * should contain an instance of the initialization class.
     * @return
     */
    public static synchronized initialization getInstance(HttpSession session){
        return getInstance(null, session, cacheType.SESSION);
    }


    /**
     * This instance will be initialized using the properties file indicated by the sFile parameter.
     * @param sFile The properties file on which this instance is to be based.
     * @param session The session "initializer" attribute will be set with this instance.
     * Any existing instance in the "initializer" attribute will be overwritten.
     * @return
     */
    public static synchronized initialization getInstance(String sFile, HttpSession session){
        return getInstance(sFile, session, cacheType.SESSION);
    }



    private static synchronized initialization getInstance(String sFile, HttpSession session, cacheType chtyp) {
        initialization init = null;
        switch (chtyp) {
            case SINGLETON:
                init = getInstanceFromSingleton(sFile);
                break;
            case SESSION:
                init = getInstanceFromSession(sFile, session);
                break;
            case DISK:
                init = new reinitialization(sFile);
                break;
        }
        return init;
    }

    
    /**
     *
     * <b>APPLICATION SCOPE INITIALIZATION CACHING <u><i>(NOT USED).</i></u></b>
     * This function returns an instance of initialization, but ensures that it's never an additional instance if one already
     * exists somewhere. To do this, it will return a singleton (initSingleton) if it is not null. This makes it unnessesary to
     * open the property file (sFile) that the initialization object represents in order to get its details. It is necessary
     * to return the subclass reinitialization because if a reinitialization object were being instantiated, this function would
     * return a type initialization that could be downcast to reinitialization. This is because the initialization instance
     * now references an instance of the reinitialization subclass. Otherwise a ClassCastException would result. You cannot
     * downcast a superclass to one of its subclasses if the superclass does not actually reference an instance of that subclass.
     * @param sFile String, the property file name, with or without path (if no path, a default will be assigned)
     * @return initialization - the initialization instance.
     */
    private static synchronized initialization getInstanceFromSingleton(String sFile) {
        if (initSingleton == null) {
            reinitialization init = new reinitialization(sFile);
            initSingleton = init;
            return initSingleton;
        }
        else {
            if (sFile != null) {
                reinitialization init = new reinitialization(sFile);
                initSingleton = init;
            }
            return initSingleton;
        }
    }



    /**
     * <b>SESSION SCOPE INITIALIZATION CACHING</b>.
     * This function does not use the singleton approach to caching the initialization state. Instead, the first instance that
     * is created of an initialization object or its subclass is stored in a session variable. Then each instance that is handed
     * after that is obtained from the session variable if it is found to exist. It is necessary
     * to return the subclass reinitialization because if a reinitialization object were being instantiated, this function would
     * return the type initialization that could be downcast to reinitialization. This is because the initialization instance
     * now references an instance of the reinitialization subclass. Otherwise a ClassCastException would result. You cannot
     * downcast a superclass to one of its subclasses if the superclass does not actually reference an instance of that subclass.
     * @param sFile String, the name of the properties file. This could include the full path, or it could simply be the
     * shortname of the file - in which case, the location of the initialization class file would be determined at runtime and
     * used as a default directory.
     * @param session javax.servlet.http.HttpSession, the session from which an initialization object could be cached, and into
     * which an intialization object could be deposited.
     * @return initialization - the initialization instance.
     */
    private static synchronized initialization getInstanceFromSession(String sFile, HttpSession session) {
        return new reinitialization(sFile, session);
    }



    private synchronized void initializeFromDisk() {
        String sThisMethod = this.getClass().getName() + ".initialize";
        String sFileSpec = getInitializationFileSpec();
        File file = new File(sFileSpec);
        if (file.exists()) {
            /* Alternate method of loading a properties file using ResourceBundle:
            http://java.sun.com/docs/books/tutorial/i18n/resbundle/propfile.html */
            Properties props = new Properties();
            InputStream input;
            try {
                props.load(input = new FileInputStream(file));
                Enumeration Enum = props.propertyNames();
                while (Enum.hasMoreElements()) {
                    String sKey = (String) Enum.nextElement();
                    String sVal = props.getProperty(sKey);
                    this.map.put(sKey, getObjectFromString(sVal));
                }

                input.close();
            }
            catch (FileNotFoundException e) {
                this.sInstantiationProblem = "Property File:\r\n" + sFileSpec + "\r\nexists, but still generated a FileNotFoundException.\r\n[" + sThisMethod + "]";
                this.sInstantiationProblem += "\r\n\r\nStacktrace as follows:\r\n\r\n";
                this.sInstantiationProblem += codeLib.getStackTraceAsString(e);
            }
            catch (IOException e) {
                this.sInstantiationProblem = "Unable to open property file:\r\n" + sFileSpec + "\r\n[" + sThisMethod + "]";
                this.sInstantiationProblem += "\r\n\r\nStacktrace as follows:\r\n\r\n";
                this.sInstantiationProblem += codeLib.getStackTraceAsString(e);
            }
            catch (Exception e) {
                this.sInstantiationProblem = "Unexpected Exception while loading, iterating, or closing the property file:\r\n" + sFileSpec + "\r\n[" + sThisMethod + "]";
                this.sInstantiationProblem += "\r\n\r\nStacktrace as follows:\r\n\r\n";
                this.sInstantiationProblem += codeLib.getStackTraceAsString(e);
            }
        }
        else {
            this.sInstantiationProblem = "Property file does not exist:\r\n" + sFileSpec + "\r\n[" + sThisMethod + "]\r\n\r\n";
            try {
                file.createNewFile();
                this.initializeFromDisk();
            }
            catch (IOException e) {
                this.sInstantiationProblem += "Cannot create file:\r\n" + sFileSpec + "\r\n[" + sThisMethod + "]\r\n";
                this.sInstantiationProblem += "Loading a blank initialization object to session state.\r\n";
                this.sInstantiationProblem += "You can add and modify new properties during the session, but nothing will be persisted to disk.";
                this.sInstantiationProblem += "\r\n\r\nStacktrace as follows:\r\n\r\n";
                this.sInstantiationProblem += codeLib.getStackTraceAsString(e);
            }
            catch (Exception e) {
                this.sInstantiationProblem += "Unexpected Exception trying to create:\r\n" + sFileSpec + "\r\n[" + sThisMethod + "]";
                this.sInstantiationProblem += "\r\n\r\nStacktrace as follows:\r\n\r\n";
                this.sInstantiationProblem += codeLib.getStackTraceAsString(e);
            }
        }
    }



    /**
     * If in the constructor an initialization object is successfully retrieved from the session attribute "initializer", then
     * This function is called with object passed in to be used to populate this instances attributes.
     * @param init deckman.settings.reinitialization, the initialization object obtained from the session attribute.
     */
    private void initializeFromInstance(reinitialization init) {
        this.map = init.getMap();
        this.sInitializationFilePath = init.getInitializationFilePath();
        this.sInitializationFileName = init.getInitializationFileName();
        resetInitSingleton(init);
    }



    /**
     * Indicates if there was a problem during intialization.
     * @return boolean, Indicates if - during the execution of {@link #initialize initialize} there was
     * either an exception during the attempt to open, load, iterate, and close the properties file, or
     * the file itself could not be found with the provided or default directory and/or file name parameters.
     * Failure because data expected in a certain file was not loaded into the initialization object created
     * by the {@link #initialization(String, HttpSession) constructor}.
     */
    public boolean failedInitialization() {
        return (this.sInstantiationProblem != null);
    }



    /**
     * If there was a problem during initialization (see {@link #failedInitialization failedInitialization}),
     * This function returns a string detailing that problem. However, although the {@link #initialize constructor}
     * may have technically failed, a blank initialization object is created nonetheless and stored in session.
     * All subsequent calls to {@link #getInstanceFromSession(String, HttpSession) getInstanceFromSession}
     * will return that same instance and no real instantiation has taken place. Therefore this function will
     * return the problem and then clear it out so that those subsequent calls will not continue to
     * report the same problem.
     * @return String, Contains a brief message about what the problem is, usually with a stacktrace included.
     */
    public String getInitializationProblem() {
        String sRetval = ("<pre>" + this.sInstantiationProblem + "</pre>");
        this.sInstantiationProblem = null;
        return sRetval;
    }



    /**
     * This is the only way a reinitialization instance can access the private initSingleton variable
     * @param init deckman.settings.reinitialization, the initialization instance to set initSingleton to
     */
    protected void resetInitSingleton(reinitialization init) {
        initialization.initSingleton = init;
    }



    /**
     * Since a property file can only store properties as text, this function is used to interpret certain text as
     * intending other datatypes where indicated. For example if a property consists of the text "ON", this indicates
     * a Boolean.TRUE, the text "5" indicates an Integer, and so on.
     * @param sVal String, the text value obtained from a property file name/value pair
     * @return an Object version of sVal
     */
    protected Object getObjectFromString(String sVal) {
        Object o = new Object();
        boolean isTrue =
                sVal.trim().equalsIgnoreCase("TRUE") ||
                sVal.trim().equalsIgnoreCase("YES") ||
                sVal.trim().equalsIgnoreCase("ON");
        boolean isFalse =
                sVal.trim().equalsIgnoreCase("FALSE") ||
                sVal.trim().equalsIgnoreCase("NO") ||
                sVal.trim().equalsIgnoreCase("OFF");

        if (isTrue || isFalse) {
            return Boolean.valueOf(isTrue);
        }
        else if (sVal.matches("^\\d+$")) {
            try {
                return new Integer(sVal);
            }
            catch (NumberFormatException e) {
                try {
                    return new Long(sVal);
                }
                catch (NumberFormatException ex) {
                    return sVal;    //anything larger is returned as a string
                }
            }
        }
        else if (sVal.matches("^\\d*\\.\\d+$")) {
            try {
                return new Float(sVal);
            }
            catch (NumberFormatException e) {
                return sVal;
            }
        }
        else {
            return sVal;
        }

    }



    /**
     * getter for this.map
     */
    public HashMap getMap() {
        return this.map;
    }



    public HashMap getStringMap() {
        HashMap newmap = new HashMap(this.map);   //make a copy of super.map
        Iterator iter = newmap.keySet().iterator();
        while (iter.hasNext()) {
            Object o = iter.next();
            Object oVal = newmap.get(o);
            if (!(oVal instanceof String)) {
                newmap.put(o, String.valueOf(oVal));
            }
        }
        return newmap;
    }



    /**
     * Upon initialization, text stored in the properties file is loaded into this classes HashMap as the datatype the text
     * indicates. This function performs the reverse, where the datatype corresponding to the key sName that is stored in
     * map is returned by this function as a string.
     * @param sName String, the map key whose paired value is to be returned as a String
     * @return String, represents the map object value corresponding to the map key sName
     */
    public String getPropertyAsString(String sName) {
        if (this.map.containsKey(sName)) {
            Object o = map.get(sName);
            if (o instanceof String) {
                return (String) o;
            }
            else if (o instanceof Number) {
                return String.valueOf(o);
            }
            else {
                return o.toString();
            }
        }
        else {
            return null;
        }
    }



    /**
     * Upon initialization, text stored in the properties file is loaded into this classes HashMap as the datatype the text
     * indicates. This function performs the reverse, where the datatype corresponding to the key sName that is stored in
     * map is returned by this function as a boolean. This function would only return a false if the text in the prop file,
     * was interpreted as a boolean false and hence the HashMap value is false, or they key is present in the HashMap, but
     * no value (they key was present in the property file, but with nothing after the "=" sign.
     * @param sName String, the map key whose paired value is to be returned as a Boolean
     * @return Boolean, represents the map object value corresponding to the map key sName
     */
    public Boolean getPropertyAsBoolean(String sName) {
        if (this.map.containsKey(sName)) {
            Object o = map.get(sName);
            if (o instanceof Boolean) {
                return (Boolean) o;
            }
            else if (o instanceof String) {
                if (o.equals("")) {
                    return Boolean.FALSE;
                }
                else if (((String) o).trim().equals("")) {
                    return Boolean.FALSE;
                }
                else {
                    return Boolean.TRUE;
                }
            }
            else {
                return Boolean.TRUE;
            }
        }
        else {
            return null;
        }
    }



    public void setInitializationFilePathToDefault() {
        this.sInitializationFilePath = getDefaultInitializationFilePath();
    }

    public void setInitializationFilePath(String sFilepath){
        this.sInitializationFilePath = sFilepath;
    }

    public void setInitializationFileName(String sFilename){
        this.sInitializationFileName = sFilename;
    }

    /**
     * The properties files are to go in the same directory that contains the package of the class specified below (initialization.class).
     * The leading "/" and the class filename are stripped off to obtain the path if windows seems to be the OS.
     * This is the one property you cannot set (only get).
     * @return String, the file path
     */
    public static String getDefaultInitializationFilePath() {
        return codeLib.getResourceFilePath(initialization.class, "/deckman/settings/initialization.class");
    }



    /**
     * This function will return the physical path of the servlet context
     * @param request HttpServletRequest, From this object the servlet context can be obtained
     */
    public static String getDefaultInitializationFilePath(HttpServletRequest request) {
        return initialization.getDefaultInitializationFilePath(request.getSession().getServletContext());
    }



    /**
     * This function will return the physical path of the servlet context
     * @param session HttpSession, From this object the servlet context can be obtained
     */
    public static String getDefaultInitializationFilePath(HttpSession session) {
        return initialization.getDefaultInitializationFilePath(session.getServletContext());
    }



    /**
     * This function will return the physical path of the servlet context
     * @param application ServletContext, The servlet context whose path is to be returned
     */
    public static String getDefaultInitializationFilePath(ServletContext application) {
        String sAppPath = application.getRealPath("");
        if (!sAppPath.endsWith(File.separator)) {
            sAppPath += File.separator;
        }
        return sAppPath;
    }



    public String getInitializationFileSpec() {
        if (sInitializationFilePath == null) {
            setInitializationFilePathToDefault();
        }
        return (this.sInitializationFilePath + File.separator + this.sInitializationFileName);
    }



    public String getInitializationFileName() {
        return this.sInitializationFileName;
    }



    public String getInitializationFilePath() {
        return this.sInitializationFilePath;
    }



    /**
     * Using the Object clone method is not good enough because this will create a "shallow" clone of the initialization instance.
     * The object fields of the clone would have references to the corresponding fields of the original initialization being cloned.
     * Changes in these fields belonging to the clone would be reflected in the initialization that was cloned.
     * A "deep" clone is required. One way of getting a deep clone is to serialize the object out to a byte array stream using
     * the writeUnshared method, and deserialize it back in to a new object using the readUnshared method.
     */
    public Object deepClone() throws Exception {
        ByteArrayOutputStream bytestreamOut = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        initialization clone = null;

        try {
            out = new ObjectOutputStream(bytestreamOut);
            out.writeUnshared(this);
            byte[] bytes = bytestreamOut.toByteArray();
            ByteArrayInputStream bytestreamIn = new ByteArrayInputStream(bytes);
            ObjectInputStream in = new ObjectInputStream(bytestreamIn);

            //try{
            clone = (initialization) in.readUnshared();
            //}
            //catch(ClassNotFoundException e1){ /* Class of a initialization object to deserialize cannot be found */ }
            //catch(StreamCorruptedException e2){ /* Control information in the stream is inconsistent while deserializing initialization object */ }

            //catch(ObjectStreamException e3){ /* Object to deserialize has already appeared in stream */ }

            out.close();

        }
        //catch(OptionalDataException e4){ /* Primitive data is next in stream */ }
        //catch(IOException e5){ }
        finally {
            if (out != null) {
                out.close();
            }
        }

        return clone;
    }



    public static void main(String[] args) {

    }
}
