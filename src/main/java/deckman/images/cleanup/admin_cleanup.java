package deckman.images.cleanup;

import java.sql.*;
import java.util.*;
import java.io.*;
import javax.servlet.http.*;
import deckman.settings.*;
import deckman.utils.*;

/**
 * Created on May 26, 2008, 11:15 PM
 *
 * <p> There should be a one to one correspondence between the following:
 * <br>  1) each item of content in the directory in which fullsize images for the website is stored
 * <br>  2) each entry in the database for these items of content
 * <br>  3) smaller thumbnail files for each item of content
 * <br> For example, if a fullsize image or images are found for which no entry exists in the database, an option is available
 * <br> here to either add the entry to the database or delete the file so that the required database entry will no longer be necessary.
 * <br> In Either case, the result will be a database field that has an entry for every item of content in the images directory with no
 * <br> extras and no omissions.
 *   
 * <p> Testing for this will require frequent updates to a copy of the production database.
 * <br> So, to restore it back to its exact copy state after updates to the main table (album), the following can be used:
 * 
 * <p>   DROP TABLE IF EXISTS `deckman`.`album`;
 * <br>    CREATE TABLE  `deckman`.`album` (
 * <br>      `ID` int(11) NOT NULL AUTO_INCREMENT,
 * <br>      `name` varchar(100) DEFAULT NULL,
 * <br>      `Deck, pressure treated` tinyint(1) DEFAULT 0,
 * <br>      `Deck, mahogany` tinyint(1) DEFAULT 0,
 * <br>      `Deck, white cedar` tinyint(1) DEFAULT 0,
 * <br>      `Deck, composite` tinyint(1) DEFAULT 0,
 * <br>      `Deck, other` tinyint(1) DEFAULT 0,
 * <br>      `Rail, pressure treated` tinyint(1) DEFAULT 0,
 * <br>      `rail, mahogany` tinyint(1) DEFAULT 0,
 * <br>      `rail, white cedar` tinyint(1) DEFAULT 0,
 * <br>      `rail, synthetic` tinyint(1) DEFAULT 0,
 * <br>      `rail, other` tinyint(1) DEFAULT 0,
 * <br>      `Benches` tinyint(1) DEFAULT 0,
 * <br>      `Walkways` tinyint(1) DEFAULT 0,
 * <br>      `Facia options` tinyint(1) DEFAULT 0,
 * <br>      `Porches` tinyint(1) DEFAULT 0,
 * <br>      `Gazebos, arbors, trellis` tinyint(1) DEFAULT 0,
 * <br>      `Under deck enclosure` tinyint(1) DEFAULT 0,
 * <br>      `Roof decks` tinyint(1) DEFAULT 0,
 * <br>      `Privacy Fences/Screens` tinyint(1) DEFAULT 0,
 * <br>      `Showers` tinyint(1) DEFAULT 0,
 * <br>      `DO NOT USE` tinyint(1) DEFAULT 0,
 * <br>	     `MARK FOR DELETION` tinyint(1) DEFAULT 0, 
 * <br>      PRIMARY KEY (`ID`)
 * <br>    ) ENGINE=MyISAM AUTO_INCREMENT=438 DEFAULT CHARSET=latin1;
 * 
 * 
 * <p> To refresh the data of the album table in the test database, use the following:
 * 
 * <p>	DELETE FROM `test`.`album`;
 * <br>	INSERT INTO `test`.`album` 
 * <br>	SELECT * FROM `deckman`.`album`;	 
 * <br>
 * @author Warren
 *
 */
public class admin_cleanup {
    
    private Connection DbConn = null;
    private String sFullsizeFilePath = null; 
    private String sThumbsDir = null;
    private String sReimageOutDir = null;
    private String sDB_Driver = null;
    private String sDB_ConnectionString = null;
    private String sDB_User = null;
    private String sDB_Password = null;
    private boolean bMySQL = false;
	/** see {@link #DatabaseErrorStackTrace DatabaseErrorStackTrace} */
    private String sDBExceptionStackTrace = null;
    private int iRecordsAffectedByDbUpdate = 0;
    
    private ArrayList listOfImageFiles = null;
    private ArrayList listOfThumbFiles = null;
    private ArrayList listOfDbEntries = null;
    
	/**
	 * Creates a new instance of admin_cleanup
	 */
    public admin_cleanup(initialization init) {
	this.sFullsizeFilePath = init.getPropertyAsString("FullsizeFilePath");
	this.sThumbsDir = init.getPropertyAsString("ThumbsFilePath");
	this.sReimageOutDir = init.getPropertyAsString("ReimageOutFilePath");
	this.sDB_Driver = init.getPropertyAsString("DB_Driver");
	this.sDB_ConnectionString = init.getPropertyAsString("DB_ConnectionString");
	this.sDB_User = init.getPropertyAsString("DB_User");
	this.sDB_Password = init.getPropertyAsString("DB_Password");
	this.bMySQL = this.sDB_Driver.matches("(?i).*mysql.*");
    }
    
    
	/**
	 * This function always returns a this.DbConn (unless exception). The connection string is already a property of the class -
	 * so is the use of MySQL vs. MSAccess databases. If any errors are encountered while getting the connection,
	 * the stack trace is set on the appropriate property of the class and a null is returned.
	 * Prior to contacting the database itself for the connection, the DbConn property of this class is checked.
	 * If it is not null, it is returned immediately, making contact with the database to set this.DbConn unnecessary.
	 * @return java.sql.Connection returns this.DbConn
	 */
    private Connection getDB_Connection(){
	if(this.DbConn == null){
	    try{
		Class.forName(this.sDB_Driver);
		if(this.bMySQL){
		    //ctn = DriverManager.getConnection("jdbc:mysql://localhost/deckman?user=root&password=mypassword");
		    this.DbConn = DriverManager.getConnection(this.sDB_ConnectionString, this.sDB_User, this.sDB_Password);
		}
		else{
		    this.DbConn = DriverManager.getConnection(this.sDB_ConnectionString);
		}	    
	    }
	    catch(java.lang.ClassNotFoundException e){
		this.sDBExceptionStackTrace = codeLib.getStackTraceAsString(e);
		return null;
	    }
	    catch(java.sql.SQLException e){
		this.sDBExceptionStackTrace = codeLib.getStackTraceAsString(e);
		return null;
	    }
	    return this.DbConn;
	}
	else{
	    return this.DbConn;
	}
    }

    
	/**
	 * This function closes the this.DbConn connection if it is not already and sets it to null.
	 */
    public void breakDB_Connection() throws SQLException {
	if(this.DbConn != null){
	    try{
		if(this.DbConn.isValid(5)){
		    this.DbConn.close();
		}
		else{
		    if(!this.DbConn.isClosed()){
			this.DbConn.close();
		    }
		}
	    }
	    catch(AbstractMethodError e){
		    // probably means the version of the JDBC driver does not implement the isValid method
		try{
		    if(!this.DbConn.isClosed()){
			this.DbConn.close();
		    }
		}
		catch(Exception ex){ }
	    }
	    this.DbConn = null;
	}	
    }

    
	/**
	 * Returns the stack trace of any Exception encountered while performing a database operation such as getting a 
	 * connection or any other database action where the first exception should stop execution. This does not include
	 * Exceptions associated with those database tasks involving multiple database updates that are each executed in 
	 * separate iterations of looping functionality that is allowed to continue in the event of such exceptions in 
	 * order to give each database update a chance (exceptions being noted along the way by growing an ArrayList). 
	 */
    public String DatabaseErrorStackTrace(){
	return this.sDBExceptionStackTrace;
    }
    
    
	/**
	 * Gets an ArrayList that contains the shortname of each image file in the directory specified by sDir.
	 * Image shortnames are the names of all java.io.File objects in the directory specified by sDir whose file
	 * extensions suggest an image file. No other file names will make the ArrayList.
	 * @param sDir String, the path of the directory whose image file names will be listed
	 * @param savedList java.util.ArrayList, the first time this function is run, savedList will be null and the
	 * sDir directory will have to be accessed to assign its list values. Otherwise savedList can be returned by
	 * the function immediately, making the list building unnecessary.
	 * @return java.util.ArrayList, The list of image file names from directory sDir
	 */
    private ArrayList getDirectoryImageContents(String sDir, ArrayList savedList){
	if(savedList == null){
	    File imgDir = new File(sDir);

	    ArrayList files = new ArrayList();
	    if(imgDir.exists()){
		if(imgDir.isDirectory()){
		    File[] aFiles = imgDir.listFiles(new deckman.images.imageFileFilter());
		    
		    if(aFiles.length > 0){
			for(int i=0; i<aFiles.length; i++) files.add(new EquatableImageFileName(aFiles[i]));		    
		    }
		}
	    }
	    savedList = files;
	    return new ArrayList(files);
	}
	else{
	    return new ArrayList(savedList);
	}	
    }
    
    
	/**
	 * Get an ArrayList that contains the value or each row in the album table for the given field sDbFieldName
	 * The first time this function is run, this.listOfDbEntries will be null and the database will have to be 
	 * accessed to assign it a list of values. Otherwise this.listOfDbEntries can be returned by
	 * the function immediately, making the database access unnecessary.
	 * @param sDbIdFieldName String, The name of a database table field that serves as an id, like an index, or 
	 * auto-increment field. Can be null, indicating that there is no ID field for the database table.
	 * @param sDbFieldName String, The name of the database table field that the id field corresponds to.
	 * @return java.util.ArrayList, The list representing the entire table field specified by sDbFieldName
	 */
    private ArrayList getDbFieldAsList(String sDbIdFieldName, String sDbFieldName, String sTableName){
	if(this.listOfDbEntries == null){
	    Connection db = getDB_Connection();
	    if(db == null){
		return null;
	    }
	    else{
		try {
		    Statement stmt = db.createStatement(ResultSet.TYPE_FORWARD_ONLY, com.mysql.jdbc.ResultSet.CONCUR_READ_ONLY);
		    ResultSet rsFiles = null;
		    if(sDbIdFieldName == null){
			rsFiles = stmt.executeQuery("SELECT " + sDbFieldName + " FROM " + sTableName + ";");
			this.listOfDbEntries = new ArrayList();
			while(rsFiles.next()){
			    String dbFile = rsFiles.getString(sDbFieldName);
			    this.listOfDbEntries.add(new EquatableImageFileName(dbFile));			    
			}
		    }
		    else{
			rsFiles = stmt.executeQuery("SELECT " + sDbIdFieldName + ", " + sDbFieldName + " FROM " + sTableName + ";");
			this.listOfDbEntries = new ArrayList();
			while(rsFiles.next()){
			    String dbFile = rsFiles.getString(sDbFieldName);
			    int dbFileID = rsFiles.getInt(sDbIdFieldName);
			    this.listOfDbEntries.add(new EquatableImageFileName(dbFileID, dbFile));			    
			}
		    }
		    
		    return new ArrayList(this.listOfDbEntries);
		}
		catch (SQLException ex){
		    this.sDBExceptionStackTrace = codeLib.getStackTraceAsString(ex);
		    ArrayList ExList = new ArrayList();
		    ExList.add("ERROR");
		    return ExList;
		}		    
	    }
	}
	else{
	    return new ArrayList(this.listOfDbEntries);
	}
    }
    
    
	/**
	 * Two ArrayLists are compared here. Both must contain strings (maybe use of generics for String should be
	 * worked in to the method signature). If the first contains entries that the second one does not, these
	 * entries are returned as a new ArrayList. The function also prevents null pointer exceptions in the event
	 * either of the two ArrayList parameters were passed in as null due to an error in building them.
	 * @param list1 ArrayList, the first ArrayList.
	 * @param list2 ArrayList, the second ArrayList.
	 * @param sErrMsg1 String, the message to subsititute in list1 as its only item in case list1 is null
	 * @param sErrMsg2 String, the message to subsititute in list2 as its only item in case list2 is null
	 * @return ArrayList, the items contained in list1 not found in list2.
	 */
    private ArrayList getOrphans(ArrayList list1, ArrayList list2, String sErrMsg1, String sErrMsg2){
	ArrayList error = new ArrayList();
	
	if(list1 == null){
	    error.add(sErrMsg1);
	}
	else {
	    if(!list1.isEmpty()){
		if(list1.get(0).toString().equalsIgnoreCase("ERROR")){
		    error.add(sErrMsg1);
		}
	    }
	}
	
	if(list2 == null){
	    error.add(sErrMsg2);
	}
	else {
	    if(!list2.isEmpty()){
		if(list2.get(0).toString().equalsIgnoreCase("ERROR")){
		    error.add(sErrMsg2);
		}
	    }
	}
	
	if(error.isEmpty()){
	    FasterArrayList fList1 = new FasterArrayList(list1);
	    Object[] oList2 = list2.toArray();
	    fList1.removeAll(oList2);
	    return fList1;
	}
	else{
	    return error;
	}
    }
    
    
	/**
	 * Gets a list of any image files whose names are not represented in the database.
	 * @param sFullsizeDir String, the path of the directory whose image file contents are to be checked against
	 * the database for any failed matches.
	 * @param sDbFieldName String, the name of the database field that is to be checked for failure to have a 
	 * matching record for each image file in sFullSizeDir.
	 * @return ArrayList, the list of any image files whose names are not represented in the database. These
	 * would be the orphans.
	 */
    public ArrayList getFileOrphans(String sFullsizeDir, String sDbIdFieldName, String sDbFieldName, String sTableName){
	ArrayList files = getDirectoryImageContents(sFullsizeDir, this.listOfImageFiles);
	ArrayList dbFiles = getDbFieldAsList(sDbIdFieldName, sDbFieldName, sTableName);	
	String sFileErrMsg = "ERROR: failure obtaining image folder listing";
	String sDBErrMsg = "ERROR: failure obtaining database image listing";
	return getOrphans(files, dbFiles, sFileErrMsg, sDBErrMsg);
    }
    
    
    public ArrayList getFileOrphans(){
	return getFileOrphans(this.sFullsizeFilePath, "ID", "name", "album");
    }
    
    
	/**
	 * Gets a list of any database records whose field represented by sDbFieldName contains a value that cannot be
	 * matched with the name of any image file in the directory represented by sFullsizeDir.
	 * @param sFullsizeDir String, the path of the directory whose image file contents are to be checked against
	 * the database for any failed matches.
	 * @param sDbFieldName String, the name of the database field that is to be checked for failure to have a 
	 * matching record for each image file in sFullSizeDir.
	 * @return ArrayList, the list of any image files whose names are not represented in the database. These
	 * would be the orphans.
	 */
    public ArrayList getDatabaseOrphans(String sFullsizeDir, String sDbIdFieldName, String sDbFieldName, String sTableName){
	ArrayList files = getDirectoryImageContents(sFullsizeDir, this.listOfImageFiles);
	ArrayList dbFiles = getDbFieldAsList(sDbIdFieldName, sDbFieldName, sTableName);
	String sFileErrMsg = "ERROR: failure obtaining image folder listing";
	String sDBErrMsg = "ERROR: failure obtaining database image listing";
	return getOrphans(dbFiles, files, sDBErrMsg, sFileErrMsg);
    }
    
    
    public ArrayList getDatabaseOrphans(){
	return getDatabaseOrphans(this.sFullsizeFilePath, "ID", "name", "album");
    }
    
	/**
	 * Gets a list of the names of thumbnail image files that do not have matches in the directory
	 * where fullsize images are kept. An image file of the same name would be a match.
	 * @param sThumbDir String, the path of the directory where thumbnails are kept.
	 * @param sFullsizeDir String, the path of the directory where fullsize images are kept.
	 * @return ArrayList, a list of the thumbnails having no fullsize counterparts.
	 */
    public ArrayList getThumbnailOrphans(String sThumbDir, String sFullsizeDir){
	ArrayList files = getDirectoryImageContents(sFullsizeDir, this.listOfImageFiles);
	ArrayList thumbs = getDirectoryImageContents(sThumbDir, this.listOfThumbFiles);
	String sThumbErrMsg = "ERROR: failure obtaining thumbnail folder listing";
	String sFileErrMsg = "ERROR: failure obtaining image folder listing";
	return getOrphans(thumbs, files, sThumbErrMsg, sFileErrMsg);
    }
    
    
    public ArrayList getThumbnailOrphans(){
	return getThumbnailOrphans(this.sThumbsDir, this.sFullsizeFilePath);
    }
    
    
	/**
	 * Gets a list of the names of fullsize image files that do not have matches in the directory
	 * where thumbnail images are kept. An image file of the same name would be a match.
	 * @param sThumbDir String, the path of the directory where thumbnails are kept.
	 * @param sFullsizeDir String, the path of the directory where fullsize images are kept.
	 * @return ArrayList, a list of the fullsize images having no thumbnail counterparts.
	 */
    public ArrayList getMissingThumbnails(String sThumbDir, String sFullsizeDir){
	ArrayList files = getDirectoryImageContents(sFullsizeDir, this.listOfImageFiles);
	ArrayList thumbs = getDirectoryImageContents(sThumbDir, this.listOfThumbFiles);
	String sThumbErrMsg = "ERROR: failure obtaining thumbnail folder listing";
	String sFileErrMsg = "ERROR: failure obtaining image folder listing";
	return getOrphans(files, thumbs, sFileErrMsg, sThumbErrMsg);
    }
    
    
    public ArrayList getMissingThumbnails(){
	return getMissingThumbnails(this.sThumbsDir, this.sFullsizeFilePath);
    }

    
    public List purgeMarkedImagesFromDB(){
	Connection ctn = getDB_Connection();	
	PreparedStatement pstmt = null;
	String sSQL = null;
	List Errors = new ArrayList();
	
	if(this.bMySQL){
	    sSQL = "DELETE FROM `album` WHERE `MARK FOR DELETION` = true;";
	}
	else{
	    sSQL = "DELETE FROM album WHERE [MARK FOR DELETION] = true;";
	}
	
	try{
	    pstmt = ctn.prepareStatement(sSQL);
	}
	catch(SQLException e){
	    Errors.add(codeLib.getStackTraceAsString(e));
	    return Errors;
	}

	String sName = null;
	try{
	    this.iRecordsAffectedByDbUpdate = pstmt.executeUpdate();
	}
	catch(Exception e){
	    Map map = new HashMap();
	    map.put(sName, codeLib.getStackTraceAsString(e));
	    Errors.add(map);
	}

	
	try{
	    if(!pstmt.isClosed()) pstmt.close();
	}
	catch(SQLException e){
	    Errors.add("ERROR CLOSING PREPARED STATEMENT: " + codeLib.getStackTraceAsString(e));
	    return Errors;
	}
	catch(java.lang.AbstractMethodError e){
	    // Do nothing. I think this means that the mysql jdbc driver being used does not support the isClosed property or the close method
	}
	pstmt = null;

	return Errors;	
    }
    
    
    public int getRecordsAffected(){
	return this.iRecordsAffectedByDbUpdate;
    }
    
    
	/**
	 * A condition exists where there are entries in the website database, but no matching image files found.
	 * The user has elected to eliminate these entries from the database.
	 * @param aNames java.util.List, A list of the values in the "name" field of the database table the deletions are
	 * to be performed against - one row for each list item.
	 * @param sWhereFldName String, The database field that the values in aNames are to be compared in the WHERE clause 
	 * of the delete sql
	 * @return List, An indication of one of 3 things:
	 *	1) A subset of aNames indicating a "name" field value for each row of the database table against which a
	 *	   deletion failed and generated an error. This will be an ArrayList of maps where the map key is the name
	 *	   value and the map value is the stacktrace of the error thrown.
	 *	2) A SQLEexception ocurred before an deletion attempts take place. This will simply be a one item ArrayList
	 *	   containing a string value for the stacktrace.
	 *	3) An empty ArrayList. This will indicate success of this function for all deletions. No exceptions thrown.
	 */
    public List purgeDatabaseOrphans(List aNames, String sWhereFldName){
	if(aNames == null){
	    return new ArrayList(); // an empty array list will indicate success albeit without having had to do anything
	}
	else if(aNames.isEmpty()){
	    return new ArrayList(); // an empty array list will indicate success albeit without having had to do anything
	}
	else{
	    String sSQL = null;
	    if(this.bMySQL){
		sSQL = "DELETE FROM `album` WHERE `" + sWhereFldName + "` = ?;";
	    }
	    else{
		sSQL = "DELETE FROM album WHERE [" + sWhereFldName + "] = ?;";
	    }

	    return performDatabaseUpdate(aNames, sSQL);
	}
    }
    
    
    public List addOrphanedImagesToDatabase(List aNames){
	if(aNames == null){
	    return new ArrayList(); // an empty array list will indicate success albeit without having had to do anything
	}
	else if(aNames.isEmpty()){
	    return new ArrayList(); // an empty array list will indicate success albeit without having had to do anything
	}
	else{
	    String sSQL = null;
	    if(this.bMySQL){
		    /* NOTE: use of "IGNORE" ensures that the shorthand style of providing a column and values list in the insert 
		       statement will be accepted and not produce an error because the lists don't comprise all columns. */
		sSQL = "INSERT IGNORE INTO `album` (`name`) VALUES (?);";	    
	    }
	    else{
		sSQL = "INSERT INTO [album] ([name]) VALUES (?);";	    
	    }

	    return performDatabaseUpdate(aNames, sSQL);
	}
    }
    
	
    private List performDatabaseUpdate(List aNames, String sSQL){
	Connection ctn = getDB_Connection();	
	PreparedStatement pstmt = null;
	
	try{
	    pstmt = ctn.prepareStatement(sSQL);
	}
	catch(SQLException e){
	    List Err = new ArrayList();
	    Err.add(codeLib.getStackTraceAsString(e));
	    return Err;
	}

	List Errors = new ArrayList();
	Iterator iter = aNames.iterator();
	int i = 1;
	while(iter.hasNext()){
	    String sName = null;
	    int iRecordsAffected = 0;
	    try{
		Object oName = iter.next();
		if(oName instanceof EquatableImageFileName){
		    sName = ((EquatableImageFileName)oName).getStringValue();
		}
		else{	// oName should be a String			
		    sName = (String)oName;
		}
		pstmt.setString(1, sName);
		iRecordsAffected = pstmt.executeUpdate();
		if(iRecordsAffected == 0){
		    Map map = new HashMap();
		    String sErr = sSQL.startsWith("DELETE") ? "DELETION " : "APPEND ";
		    sErr += "executed without error, but no records affected";
		    map.put(sName, sErr);
		}
		else{
		    this.iRecordsAffectedByDbUpdate = iRecordsAffected;
		}
	    }
	    catch(SQLException e){
		Map map = new HashMap();
		map.put(sName, codeLib.getStackTraceAsString(e));
		Errors.add(map);
	    }
	    catch(Exception e){
		Map map = new HashMap();
		map.put(("Iterator error getting value for key # " + String.valueOf(i)), codeLib.getStackTraceAsString(e));
		Errors.add(map);
	    }
	    i++;
	}
	
	try{
	    if(!pstmt.isClosed()) pstmt.close();
	}
	catch(SQLException e){
	    List Err = new ArrayList();
	    Err.add("ERROR CLOSING PREPARED STATEMENT: " + codeLib.getStackTraceAsString(e));
	    return Err;
	}
	catch(java.lang.AbstractMethodError e){
	    // Do nothing. I think this means that the mysql jdbc driver being used does not support the isClosed property or the close method
	}
	pstmt = null;

	return Errors;	
    }
    
    
	/**
	 * Removes all image files from the fullsize image directory of the website that have no corresponding
	 * entry in the website database.
	 * @param aNames java.util.List - A list of the names of image files to delete. These are image files in
	 * the fullsize image directory of the website that have no corresponding entry in the website database.
	 * @return java.util.List - A list comprising the name of each image file that was successfully deleted.
	 */
    public List purgeFullsizeImages(List aNames){
	return purgeFiles(aNames, this.sFullsizeFilePath);
    }
    
    
	/**
	 * Removes all thumbnail files from the thumbnail directory of the website that have no fullsize 
	 * counterpart in the fullsize image directory of the website.
	 * @param aNames java.util.List - A list of the names of thumbnail files to delete. These are thumbnail files in
	 * the thumbnail image directory of the website that have no fullsize counterpart in the fullsize image 
	 * directory of the website.
	 * @return java.util.List - A list comprising the name of each thumbnail file that was not successfully deleted.
	 */
    public List purgeOrphanedThumbnails(List aNames){
	return purgeFiles(aNames, this.sThumbsDir);
    }

    
	/**
	 * Removes all files from a specified directory that match any of the file names contained in a specifed list of names
	 * @param aNames List, Files found in the specified directory (sFileDirPath) whose names match any member of this list
	 * will be deleted.
	 * @param sFileDirPath String, The directory from which files matching any of the members in aNames will be deleted.
	 * @return java.util.List - A list comprising the name of each file that was identified for deletion that failed to delete.
	 */
    private List purgeFiles(List aNames, String sFileDirPath){
	List aErrors = new ArrayList();
	if(codeLib.directoryExists(sFileDirPath)){
	    Iterator iter = aNames.iterator();
	    while(iter.hasNext()){
		Object oFileName = iter.next();
		String sFileName = null;
		if(oFileName instanceof EquatableImageFileName){
		    sFileName = ((EquatableImageFileName)oFileName).getStringValue();
		}
		else{
		    sFileName = (String)oFileName;
		}
		if(!sFileDirPath.endsWith(File.separator)) sFileDirPath += File.separator;
		String sFilePath = sFileDirPath + sFileName;
		File f = new File(sFilePath);
		if(!f.delete()){
		    aErrors.add("Failed to delete: " + sFilePath);
		}
	    }
	}
	else{
	    aErrors.add("Directory [" + sFileDirPath + "] does not exist.");
	}
	return aErrors;    
    }

    
	/**
	 * Creates a thumbnail file in the temporary directory specified (by one of the properties of the initialization object
	 * passed into an instance of this object through the constructor) for thumbnail creation for each fullsize image file
	 * that has no matching thumbnail in the permanent thumbnail directory. Once the thumbnails are created in the temporary
	 * directory, they are moved to the permanent directory. The code behind java class for the reimaging administrative
	 * jsp page and the reimaging results class are borrowed in this function (deckman.images.reimage package).
	 * @param aNames java.util.List, The list of fullsize image names for which matching thumbnails need to be generated
	 * @param session HttpSession, From the session, a thumbnail progress meter powered by ajax can determine the latest percent completion data.
	 * @return java.util.List - A list comprising the failure details of each thumbnail file that was not successfully generated.
	 */
    public List createMissingThumbnails(List aNames, HttpSession session){
	List aErrors = new ArrayList();
	deckman.images.reimage.result[] results = null;
	if(session != null){
	    session.setAttribute("THUMB_PROGRESS", null);	// reset the counter
	}

	if(!aNames.isEmpty()){
	    results = new deckman.images.reimage.result[aNames.size()];
	    int iResult = 0;
	    boolean bClearTargetDir = true;

		// make thumbs from all files whose shortnames are found in aNames.
	    ArrayList imgShortNames = new ArrayList();
	    Iterator iter = aNames.iterator();
	    while(iter.hasNext()){
		Object oImgShortName = iter.next();
		String sImgShortName = null;
		if(oImgShortName instanceof EquatableImageFileName){
		    sImgShortName = ((EquatableImageFileName)oImgShortName).getStringValue();
		}
		else{
		    sImgShortName = (String)oImgShortName;
		}
		imgShortNames.add(sImgShortName);
		String sImgFile = null;
		if(this.sFullsizeFilePath.endsWith(File.separator)){
		    sImgFile = this.sFullsizeFilePath + sImgShortName;
		}
		else{
		    sImgFile = this.sFullsizeFilePath + File.separator + sImgShortName;
		}
		deckman.images.reimage.rescaler rsclr = new deckman.images.reimage.rescaler(sImgFile, this.sReimageOutDir); 
		deckman.images.reimage.result r = rsclr.generateThumb(bClearTargetDir);  
		if(r.getThumbShortName() == null) r.setThumbShortName(sImgShortName);
		results[iResult] = r;
		iResult++;
		if(bClearTargetDir) bClearTargetDir = false;    // otherwise extracted files from the last iteration will be deleted
		if(session != null){
		    incrementSessionCounter(session, aNames.size());
		}
	    }

		// move all the newly created thumbnails to the thumbnail images dir (final destination)
	    ArrayList aMoves = codeLib.moveDirectoryContents(this.sReimageOutDir, this.sThumbsDir, true);
	    deckman.images.reimage.admin_thumbnail admin_thumbnail = new deckman.images.reimage.admin_thumbnail();
	    admin_thumbnail.putNewNamesIntoResults(aMoves, results, "THUMB");

		/* only failures are reported for cleanup results, so the result failures are extracted from the 
		   results array to an ArrayList */
	    for(int i=0; i<results.length; i++){
		deckman.images.reimage.result result = results[i];
		if(result.getType() != deckman.images.reimage.result.REIMAGE_SUCCESS){
		    Map map = new HashMap();
		    String sErr = result.getError();
		    if(sErr == null) sErr = ("Error generating thumbnail: \"" + result.getThumbShortName() + "\"");
		    map.put(sErr, result.getDescription());
		    aErrors.add(map);
		}
	    }
	}

	return aErrors;
    }
    
    
    public List createMissingThumbnails(List aNames){
	return createMissingThumbnails(aNames, null);
    }


    public List simulateMissingThumbnailCreation(HttpSession session){
	List aNames = new ArrayList();
	for(int i=1; i<=15; i++) aNames.add(String.valueOf(i));
	return simulateMissingThumbnailCreation(aNames, session);
    }
    
       
    public List simulateMissingThumbnailCreation(List aNames, HttpSession session){
	List aErrors = new ArrayList();	
	int i = 0;	
	deckman.images.reimage.result[] results = new deckman.images.reimage.result[aNames.size()];
	session.setAttribute("THUMB_PROGRESS", null);	// reset the counter 
	
	if(!aNames.isEmpty()){
	    Iterator iter = aNames.iterator();
	    while(iter.hasNext()){
		Object o = iter.next();
		deckman.images.reimage.result r = new deckman.images.reimage.result();
		boolean bError = false;
		try {
		    Thread.sleep(1000);
		} catch (InterruptedException ex) {
		    r.setError(codeLib.getStackTraceAsString(ex));
		    r.setType(deckman.images.reimage.result.REIMAGE_CANCELLED_ON_FIRST_ERROR);
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
		    r.setType(deckman.images.reimage.result.REIMAGE_SUCCESS);
		}
		incrementSessionCounter(session, aNames.size());
		results[i++] = r;
	    }
	}

	    /* only failures are reported for cleanup results, so the result failures are extracted from the 
	       results array to an ArrayList */
	for(int x=0; x<results.length; x++){
	    deckman.images.reimage.result result = results[x];
	    if(result.getType() != deckman.images.reimage.result.REIMAGE_SUCCESS){
		Map map = new HashMap();
		String sErr = result.getError();
		if(sErr == null) sErr = ("Error generating thumbnail: \"" + result.getThumbShortName() + "\"");
		map.put(sErr, result.getDescription());
		aErrors.add(map);
	    }
	}
	
	return aErrors;
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
	    iDone = new Integer(iDone.intValue()+1);
	    map.put("done", iDone);
	    map.put("total", new Integer(iTotal));
	}
	session.setAttribute("THUMB_PROGRESS", map);	
    }
    
    
    
    
	/**
	 * Used for testing. Will print out to the console every item in the provided list (list). Additional functionality is 
	 * provided to test an item of list to determine its type and use the appropriate method to get a string value. 
	 * {@link deckman.images.cleanup.EquatableImageFileName EquatableImageFileName} is included as one of the types to
	 * check, making this function somewhat specific to this package only.
	 * @param list List, The list to be printed out to the console.
	 */
    private static void PrintList(List list, String sListTitle){
	System.out.println(" ");
	if(sListTitle != null){
	    System.out.println(sListTitle);
	    for(int i=1; i<= sListTitle.length(); i++) System.out.print("-");   // prints an underline for the list title
	    System.out.println(" ");
	}
	if(list.isEmpty()){
	    System.out.println("List is empty.");
	}
	else{
	    Iterator iter = list.iterator();
	    while(iter.hasNext()){
		Object o = iter.next();
		if(o instanceof deckman.images.cleanup.EquatableImageFileName){
		    System.out.println(((deckman.images.cleanup.EquatableImageFileName)o).getStringValue());
		}
		else if(o instanceof String){
		    System.out.println((String)o);
		}
		else{
		    System.out.println(o.toString());
		}
	    }
	}
    }
    

	/**
	 * All cleanup operations return errors encountered as a list. This is because the first error that is encountered
	 * is caught and added to the list in order to continue execution. Errors may be added to the list as a simple string
	 * or as an HashMap in which the key is a short title or description and the value is more detailed content like a
	 * StackTrace. This function cycles through the list and produces something that can be rendered in HTML that reflects
	 * the List content and maintains the look of a "list". This is not an HTML table, but simply the list items formatted
	 * and separated by "<br>" tags.
	 */
    public String GetErrorListAsHTML(List Errors){
	String sHTML = "";
	if(!Errors.isEmpty()){
	    StringBuffer buffer = new StringBuffer();
	    Iterator iter = Errors.iterator();
	    while(iter.hasNext()){
		Object o = iter.next();
		if(o instanceof Map){
		    Map map = (Map)o;
		    Set keys = map.keySet();
		    Iterator keyiter = keys.iterator();
		    while(keyiter.hasNext()){
			Object key = keyiter.next();
			buffer.append("<pre>");
			buffer.append(key.toString());
			buffer.append(map.get(key));
			buffer.append("</pre>");
			buffer.append("<br>");
		    }
		}
		else{
		    buffer.append("<pre>");
		    buffer.append(o.toString());
		    buffer.append("</pre>");
		}
	    }
	    sHTML = buffer.toString();
	}
	
	return sHTML;
    }
    
    
	/**
	 * For testing only (from the netbeans IDE). Will set system parameters to that the main function 
	 * can be shorter. System parameters are used in the main method, and the args[] array is ignored.
	 */
    public static void setSystemParameters(){
	//System.setProperty("printDbOrphans", "true");		    // TESTED
	System.setProperty("purgeDbOrphans", "true");		    // TESTED

	//System.setProperty("printThumbnailOrphans", "true");	    // TESTED
	//System.setProperty("purgeThumbnailOrphans", "true");	    // TESTED
	
	//System.setProperty("printMissingThumbnails", "true");	    // TESTED
	//System.setProperty("createMissingThumbnails", "true");    // TESTED
	
	//System.setProperty("printImageOrphans", "true");	    // TESTED
	//System.setProperty("addOrphansToDb", "true");		    // TESTED
	    // or...
	//System.setProperty("purgeImageOrphans", "true");	    // TESTED
	    
	System.setProperty("FullsizeFilePath", "C:\\Documents and Settings\\Warren\\Desktop\\deckman images\\fullsize");
	System.setProperty("ThumbsFilePath", "C:\\Documents and Settings\\Warren\\Desktop\\deckman images\\thumbnails");
	System.setProperty("DB_ConnectionString", "jdbc:mysql://localhost/test");
    }
    
    
	/**
	 * <b>NAMED PARAMETERS</b> (case insensitive): <br>
	 * 
	 * @param printDbOrphans	    boolean (default=false)<br>
	 * @param purgeDbOrphans	    boolean (default=false)<br>
	 *  <p>
	 * @param printThumbnailOrphans    boolean (default=false)<br>
	 * @param purgeThumbnailOrphans    boolean (default=false)
	 *  <p>
	 * @param printMissingThumbnails   boolean (default=false)<br>
	 * @param createMissingThumbnails  boolean (default=false)
	 *  <p>
	 * @param printImageOrphans	    boolean (default=false)
	 * @param purgeImageOrphans	    boolean (default=false)<br>
	 * @param addOrphansToDb	    boolean (default=false)<br>
	 *  <p>
	 *  <br>
	 * @param FullsizeFilePath	    String (default=[value obtainable from the projects property file in property "FullsizeFilePath"])<br>
	 * @param ThumbsFilePath	    String (default=[value obtainable from the projects property file in property "ThumbsFilePath"])<br>
	 * @param ReimageOutFilePath	    String (default=[value obtainable from the projects property file in property "ReimageOutFilePath"])<br>
	 * @param DB_ConnectionString	    String (default=[value obtainable from the projects property file in property "DB_ConnectionString"])<br>
	 *  <br>
	 *  <b>EXAMPLE:</b><br>
	 * 	To print out orphaned thumbnails for the website and to delete them as well, use:<br><br>
	 * 	<CODE>
	 * 	    cd "C:\myTechnicalStuff\projects\java\projects\netbeans\web\deckman\build\web\WEB-INF\classes"<br>
	 * 		    java -DprintThumbnailOrphans="true" 
	 * 		 -DpurgeThumbnailOrphans="true" 
	 * 		 -DThumbsFilePath="C:\Documents and Settings\Warren\Desktop\deckman images\thumbnails" 
	 * 		 -DFullsizeFilePath="C:\Documents and Settings\Warren\Desktop\deckman images\fullsize" 
	 * 		 deckman.images.cleanup.admin_cleanup
	 * 	</CODE>
	 */
    public static void main(String[] args){
	
	boolean bRunningFromNetBeans = true;
	if(bRunningFromNetBeans){
	    	// simulates what would happen if this class was executed from the command line with system parameters specified in the java method
	    setSystemParameters();
	}
	
	reinitialization reinit = (reinitialization)initialization.getInstance(initialization.DEFAULT_PROP_FILE_NAME);
	String tempProp = null;
	
	    // overwrite the reinitialization object properties with the corresponding system parameters if they exist.
	String sDBConnStr = reinit.getPropertyAsString("DB_ConnectionString");
	tempProp = System.getProperty("DB_ConnectionString", sDBConnStr);
	if(!sDBConnStr.equalsIgnoreCase(tempProp)) reinit.setProperty("DB_ConnectionString", tempProp);
	
	String sFullsizeDir = reinit.getPropertyAsString("FullsizeFilePath");
	tempProp = System.getProperty("FullsizeFilePath", sFullsizeDir);
	if(!sFullsizeDir.equalsIgnoreCase(tempProp)) reinit.setProperty("FullsizeFilePath", tempProp);
	
	String sThumbDir = reinit.getPropertyAsString("ThumbsFilePath");
	tempProp = System.getProperty("ThumbsFilePath", sThumbDir);
	if(!sThumbDir.equalsIgnoreCase(tempProp)) reinit.setProperty("ThumbsFilePath", tempProp);
	
	String sReimageOutDir = reinit.getPropertyAsString("ReimageOutFilePath");
	tempProp = System.getProperty("ReimageOutFilePath", sReimageOutDir);
	if(!sReimageOutDir.equalsIgnoreCase(tempProp)) reinit.setProperty("ReimageOutFilePath", tempProp);
	
	    // instantiate the admin_cleanup object
	admin_cleanup cu = new admin_cleanup(reinit);
	
	    /* additional system parameters will have been set if running from the command line to specify each operation to run
	     * and to print out the results */
	if(Boolean.getBoolean("printDbOrphans")) PrintList(cu.getDatabaseOrphans(), "DATABASE ORPHANS:");
	if(Boolean.getBoolean("printThumbnailOrphans")) PrintList(cu.getThumbnailOrphans(), "THUMBNAIL ORPHANS:");
	if(Boolean.getBoolean("printMissingThumbnails")) PrintList(cu.getMissingThumbnails(), "MISSING THUMBNAILS:");
	if(Boolean.getBoolean("printImageOrphans")) PrintList(cu.getFileOrphans(), "IMAGE FILE ORPHANS:");
	if(Boolean.getBoolean("purgeDbOrphans")) PrintList(cu.purgeDatabaseOrphans(cu.getDatabaseOrphans(), "name"), "DATABASE ORPHANS DELETED:");
	if(Boolean.getBoolean("purgeImageOrphans")) PrintList(cu.purgeFullsizeImages(cu.getFileOrphans()), "IMAGE FILE DELETION FAILURES:");
	if(Boolean.getBoolean("purgeThumbnailOrphans")) PrintList(cu.purgeOrphanedThumbnails(cu.getThumbnailOrphans()), "THUMBNAILS DELETED:");
	if(Boolean.getBoolean("addOrphansToDb")) PrintList(cu.addOrphanedImagesToDatabase(cu.getFileOrphans()), "IMAGE FILE ORPHANS ADDED TO DATABASE:");
	if(Boolean.getBoolean("createMissingThumbnails")) PrintList(cu.createMissingThumbnails(cu.getMissingThumbnails()), "MISSING THUMBNAIL RESTORATION FAILURES:");
	
	try{			
	    cu.breakDB_Connection();
	}
	catch (SQLException ex) {
	    ex.printStackTrace();
	}	

    }
}
