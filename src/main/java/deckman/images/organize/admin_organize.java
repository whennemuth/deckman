/*
 * admin_organize.java
 *
 * Created on May 10, 2008, 5:49 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package deckman.images.organize;

/**
 *
 * @author Warren
 */

import java.sql.*;
import java.util.*;
import javax.servlet.http.*;
import deckman.images.*;
import deckman.settings.*;
import deckman.utils.codeLib;


public class admin_organize {

    private Connection DbConn = null;
    private int iIndex = 0;
    private String sTask = null;
    private String sViewStyle = null;
    private String sCategory = null;
    private String sFullsizeContextPath = null;
    private String sThumbnailContextPath = null;
    private String sDB_Driver = null;
    private String sDB_ConnectionString = null;
    private String sDB_User = null;
    private String sDB_Password = null;
    private boolean bMySQL = false;
    private imagesArrayList ImagesList = null;
    private LinkedHashMap ThisImage = new LinkedHashMap();
    private LinkedHashMap DummyImage = new LinkedHashMap();	// used only to populate the deck categories dropdown if ThisImage is null
    private ArrayList CategoryThumbs = null;
    private Set DeckCategories = null;
    private String sExceptionStackTrace = null;
    private HttpSession session = null;
    private HttpServletRequest request = null;
    private boolean bIsPost = false;



    /** Creates a new instance of admin_organize */
    public admin_organize(HttpServletRequest request, HttpSession session, initialization init) {
        bIsPost = request.getMethod().equalsIgnoreCase("POST");

        String sIndex = request.getParameter("index");
        if (sIndex != null) {
            this.iIndex = Integer.parseInt(sIndex);
        }

        this.sTask = request.getParameter("task");
        if (this.sTask == null) {
            this.sTask = "jump";
        }

        this.sViewStyle = request.getParameter("viewstyle");
        if (this.sViewStyle == null) {
            this.sViewStyle = "full";
        }

        this.sCategory = request.getParameter("category");
        if (this.sCategory == null) {
            this.sCategory = "ALL";
        }

        this.sFullsizeContextPath = init.getPropertyAsString("FullsizeContextPath");
        this.sThumbnailContextPath = init.getPropertyAsString("ThumbnailContextPath");
        this.sDB_Driver = init.getPropertyAsString("DB_Driver");
        this.sDB_ConnectionString = init.getPropertyAsString("DB_ConnectionString");
        this.sDB_User = init.getPropertyAsString("DB_User");
        this.sDB_Password = init.getPropertyAsString("DB_Password");
        this.bMySQL = this.sDB_Driver.matches("(?i).*mysql.*");

        this.session = session;
        this.request = request;

        try {
            processRequest();
        }
        catch (Exception e) {
            if (this.sExceptionStackTrace == null) {
                this.sExceptionStackTrace = codeLib.getStackTraceAsString(e);
            }
        }
    }

    // getters and setters


    public int getIndex() {
        return this.iIndex;
    }



    public void setIndex(int iIndex) {
        this.iIndex = iIndex;
    }



    public String getTask() {
        return this.sTask;
    }



    public void setTask(String sTask) {
        this.sTask = sTask;
    }



    public String getViewStyle() {
        return this.sViewStyle;
    }



    public void setViewStyle(String sViewStyle) {
        this.sViewStyle = sViewStyle;
    }



    public String getCategory() {
        return this.sCategory;
    }



    public void setCategory(String sCategory) {
        this.sCategory = sCategory;
    }



    public String getFullsizeContextPath() {
        return this.sFullsizeContextPath;
    }



    public void setFullsizeContextPath(String sFullsizeContextPath) {
        this.sFullsizeContextPath = sFullsizeContextPath;
    }



    public String getThumbnailContextPath() {
        return this.sThumbnailContextPath;
    }



    public void setThumbnailContextPath(String sThumbnailContextPath) {
        this.sThumbnailContextPath = sThumbnailContextPath;
    }



    public String getDB_Driver() {
        return this.sDB_Driver;
    }



    public void setDB_Driver(String sDB_Driver) {
        this.sDB_Driver = sDB_Driver;
    }



    public String getDB_ConnectionString() {
        return this.sDB_ConnectionString;
    }



    public void setDB_ConnectionString(String sDB_ConnectionString) {
        this.sDB_ConnectionString = sDB_ConnectionString;
    }



    public String getDB_User() {
        return this.sDB_User;
    }



    public void setDB_User(String sDB_User) {
        this.sDB_User = sDB_User;
    }



    public String getDB_Password() {
        return this.sDB_Password;
    }



    public void setDB_Password(String sDB_Password) {
        this.sDB_Password = sDB_Password;
    }



    public String getExceptionStackTrace() {
        return this.sExceptionStackTrace;
    }



    public void setExceptionStackTrace(String sExceptionStackTrace) {
        this.sExceptionStackTrace = sExceptionStackTrace;
    }



    public boolean isMySQL() {
        return this.bMySQL;
    }



    public void setMySQL(boolean bMySQL) {
        this.bMySQL = bMySQL;
    }



    private Connection getDB_Connection() {
        if (this.DbConn == null) {
            try {
                Class.forName(this.sDB_Driver);
                if (this.bMySQL) {
                    //ctn = DriverManager.getConnection("jdbc:mysql://localhost/deckman?user=root&password=mypassword");
                    this.DbConn = DriverManager.getConnection(this.sDB_ConnectionString, this.sDB_User, this.sDB_Password);
                }
                else {
                    this.DbConn = DriverManager.getConnection(this.sDB_ConnectionString);
                }
            }
            catch (java.lang.ClassNotFoundException e) {
                this.sExceptionStackTrace = codeLib.getStackTraceAsString(e);
                return null;
            }
            catch (java.sql.SQLException e) {
                this.sExceptionStackTrace = codeLib.getStackTraceAsString(e);
                return null;
            }
            return this.DbConn;
        }
        else {
            return this.DbConn;
        }
    }



    /**
     * This function closes the this.DbConn connection if it is not already and sets it to null.
     */
    private void breakDB_Connection() throws SQLException {
        if (this.DbConn != null) {
            try {
                if (this.DbConn.isValid(5)) {
                    this.DbConn.close();
                }
                else {
                    if (!this.DbConn.isClosed()) {
                        this.DbConn.close();
                    }
                }
            }
            catch (AbstractMethodError e) {
                // probably means the version of the JDBC driver does not implement the isValid method
                try {
                    if (!this.DbConn.isClosed()) {
                        this.DbConn.close();
                    }
                }
                catch (Exception ex) {
                }
            }
            this.DbConn = null;
        }
    }



    /**
     * This function is called from the constructor. It will selectively call setters based on the form parameters
     * that were picked by the user on the JSP page. These setters will populate data structures used to fill
     * dropdown boxes and set flags used to enable/disable non-applicable controls in HTML.
     */
    public void processRequest() throws SQLException {

        //Get a LinkedHashMap that represents every field name and value pairing for the current image corresponding to iIndex
        boolean bEditSuccess = false;
        Connection ctn = getDB_Connection();

        if (ctn != null) {
            try {
                if (this.sViewStyle.equalsIgnoreCase("listing")) {
                    if (this.sTask.equalsIgnoreCase("jump")) {
                        this.sViewStyle = "fit";
                        processRequest();
                    }
                    else {
                        setImagesList(this.sTask.equalsIgnoreCase("category"));
                        setDeckCategories();
                        setCategoryThumbnails();
                    }
                }
                else {
                    if (this.sTask.equalsIgnoreCase("jump")) {
                        setDeckCategories();
                        setImagesList(!this.bIsPost);	// if bIsPost==false, "jump" was set by default (no real jump ocurred) and the request does not come from organize.jsp
                        String sJumpTo = this.request.getParameter("JumpTo");
                        if (sJumpTo != null) {
                            this.iIndex = Integer.parseInt(sJumpTo);
                        }
                        setImage(this.ThisImage, this.iIndex);
                    }
                    else if (this.sTask.equalsIgnoreCase("last")) {
                        setDeckCategories();
                        setImagesList(false);
                        if (this.iIndex == 0) {
                            this.iIndex = this.ImagesList.size();
                        }
                        setImage(this.ThisImage, --this.iIndex);
                    }
                    else if (this.sTask.equalsIgnoreCase("next")) {
                        setDeckCategories();
                        setImagesList(false);
                        if (this.iIndex == (this.ImagesList.size() - 1)) {
                            this.iIndex = -1;
                        }
                        setImage(this.ThisImage, ++this.iIndex);
                    }
                    else if (this.sTask.equalsIgnoreCase("category")) {
                        setDeckCategories();	// note that this is called BEFORE setImagesList (provides a listing of fields for use in building an SQL statement in setImagesList())
                        setImagesList(true);
                        setImage(this.ThisImage, this.iIndex = 0);
                    }
                    else if (this.sTask.equalsIgnoreCase("edit")) {
                        setDeckCategories();
                        setImagesList(false);
                        setImage(this.ThisImage, this.iIndex);
                        bEditSuccess = doEdit(this.iIndex);
                        setImagesList(true);	// images list may have changed due to edit, so requery
                        if (this.ImagesList.size() == this.iIndex) {
                            this.iIndex--;	// change was made to an image while viewing it as one of a category (not "ALL"). The change removed it from that category, but since it was the last in the list, the record of the last selected index is one greater than the actual size the category is now.
                        }
                        setImage(this.ThisImage, this.iIndex);	// image will have changed due to edit, so requery
                    }
                }

                breakDB_Connection();
            }
            catch (SQLException e) {
                this.sExceptionStackTrace = codeLib.getStackTraceAsString(e);
            }
        }
    }



    /**
     * This function sets an ArrayList of HashMaps containing the ID and text values for each image in the
     * album table of the deckman database. The HashMap is the linked kind to ensure stable iteration order.
     * @param bRequery boolean, get the ArrayList from the database even if a recent copy exists in session state.
     */
    private void setImagesList(boolean bRequery) throws SQLException {
        if (bRequery) {
            setImagesList();
            this.session.setAttribute("IMAGE_LIST", this.ImagesList);
        }
        else {
            if (this.session.getAttribute("IMAGE_LIST") == null) {
                setImagesList();
                this.session.setAttribute("IMAGE_LIST", this.ImagesList);
            }
            else {
                this.ImagesList = (imagesArrayList) this.session.getAttribute("IMAGE_LIST");
            }
        }
    }



    /**
     * This function sets an ArrayList of HashMaps containing the ID and text values for each image in the
     * album table of the deckman database. The HashMap is the linked kind to ensure stable iteration order.
     */
    private void setImagesList() throws SQLException {
        Connection ctn = getDB_Connection();
        this.ImagesList = new imagesArrayList();

        /*get a result set listing all images to populate the "jump to image" drop down*/
        String sFldCharLeft = this.bMySQL ? "`" : "[";
        String sFldCharRight = this.bMySQL ? "`" : "]";
        String sSQL = null;
        if (this.sCategory.equalsIgnoreCase("ALL")) {
            sSQL = "SELECT * FROM album ORDER BY " + sFldCharLeft + "name" + sFldCharRight + ";";
        }
        else if (this.sCategory.equalsIgnoreCase("UNCATEGORIZED")) {
            sSQL = "SELECT * FROM album WHERE ";
            Iterator iter = this.DeckCategories.iterator();
            String sAnd = " AND \r\n";
            while (iter.hasNext()) {
                String sNextCategory = (String) iter.next();
                if (!sNextCategory.equalsIgnoreCase("ALL") && !sNextCategory.equalsIgnoreCase("UNCATEGORIZED")) {
                    sSQL += sFldCharLeft + sNextCategory + sFldCharRight + " = False";
                    sSQL += iter.hasNext() ? " AND \r\n" : "";
                }
            }

            if (sSQL.endsWith(sAnd)) {
                sSQL = sSQL.substring(0, (sSQL.length() - sAnd.length())) + " ORDER BY " + sFldCharLeft + "name" + sFldCharRight + ";";
            }
        }
        else {
            sSQL = "SELECT * FROM album WHERE " + sFldCharLeft + this.sCategory + sFldCharRight + " = True ORDER BY " + sFldCharLeft + "name" + sFldCharRight + ";";
        }
        Statement stmt = ctn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet rst = stmt.executeQuery(sSQL);

        while (rst.next()) {
            String[] sPair = {rst.getString("ID"), rst.getString("name")};
            if (this.sViewStyle.equalsIgnoreCase("THUMBNAIL")) {
                    /* The image type of a thumbnail will most probably be a gif. If so, getting the filename of the thumbnail will come down to
                     * modifying the name of the fullsize image by changing its file extension (probably "jpg") to "gif". No other change
                     * will be necessary because by rule in the deckman website, without a file extension, the name of a fullsize image and
                     * its corresponding thumbnail should be identical.*/
                String sSrc = codeLib.switchFileExtension(sPair[1], "gif");
                sPair[1] = sSrc;
            }
            this.ImagesList.add(sPair);
        }

        rst.close();
        rst = null;
    }



    /**
     * This function returns an ArrayList of HashMaps containing the ID and text values for each image in the
     * album table of the deckman database. The HashMap is the linked kind to ensure stable iteration order.
     * A call to this function should be the last in line of other functionality that involve interaction with
     * the database, so the connection is closed.
     */
    public imagesArrayList getImagesList() throws SQLException {
        if (this.ImagesList == null) {
            setImagesList();
        }
        breakDB_Connection();
        return this.ImagesList;
    }



    /**
     * This function sets a LinkedHashMap containing all field values for a single image. It represents
     * one row in the album table. The HashMap is the linked kind to ensure stable iteration order.
     * @param iIdx int, will identify the ArrayList item in this.ImagesList that contains the ID of
     * the particular image that is to be set. The ID, in turn, will identify the database table row
     * that corresponds to the image whose data is to be reflected in the LinkedHashMap.
     * @param Image LinkedHashMap, This will most often be this.ThisImage. It's use as described above
     * applies to setting the vertical array of checkboxes on the left hand side of the JSP page with the
     * correct values. It's second use is in function setDeckCategories, to set variable this.DeckCategories
     * with it's keyset. If LinkedHashMap Image is not this.ThisImage, but this.DummyImage, then it is only
     * this second usage that this function is being called for because DummyImage is only needed for 
     * its "property" names, not its "property" values.
     */
    private void setImage(LinkedHashMap Image, int iIdx) throws SQLException {
        if (this.ImagesList != null) {
            if (this.ImagesList.isEmpty()) {
                return; // database table is empty or there are no images that match the selected category.
            }
        }

        Connection ctn = getDB_Connection();
        ResultSet rst = null;

        if (Image == this.ThisImage) {
            String[] sImage = (String[]) this.ImagesList.get(iIdx);
            PreparedStatement pstmt = ctn.prepareStatement("SELECT * FROM album WHERE ID = ?", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            int iID = Integer.parseInt(sImage[0]);
            pstmt.setInt(1, iID);
            rst = pstmt.executeQuery();
        }
        else if (Image == this.DummyImage) {
            // retrieve the first row of the table (the data in the row is irrelevant, only the field names are).
            Statement stmt = ctn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            if (this.bMySQL) {
                rst = stmt.executeQuery("SELECT * FROM album LIMIT 0,1;");
            }
            else {
                rst = stmt.executeQuery("SELECT TOP 1 * FROM album;");
            }
        }
        rst.next();

        ResultSetMetaData rsmd = rst.getMetaData();
        if (Image == null) {
            Image = new LinkedHashMap();
        }
        else {
            Image.clear();
        }

        int iFlds = rsmd.getColumnCount();
        for (int i = 1; i <= iFlds; i++) {
            String sFldID = rsmd.getColumnName(i);
            int iType = rsmd.getColumnType(i);

            String sValue = rst.getString(sFldID);
            if (iType == java.sql.Types.BIT) {    //MSAccess booleans are BIT types.
                Boolean oFldVal = new Boolean(rst.getBoolean(sFldID)); //must get a Boolean object, not a boolean primitive type.
                Image.put(sFldID, oFldVal);
            }
            else {   //all other types can be converted to strings
                String sFldVal = rst.getString(sFldID);
                Image.put(sFldID, sFldVal);
            }
        }

        rst.close();
        rst = null;
    }



    public LinkedHashMap getImage() {
        return this.ThisImage;
    }



    /**
     * This function sets a Set containing each image category. These happen to be the names of the majority of
     * fields in the album table of the deckman database. These fields each indicate a particular deck category and
     * contain a boolean value that indicates if the image corresponding to the same table row belongs in that category.
     * Since the HashMap "ThisImage" includes all field names connected to an image, those of them that end up forming
     * this list can be determined by including them all except those checked for in the code below. An alternate
     * method would be to check for all the HashMap values that can successfully be cast to Boolean wrapper objects
     * that can yield a boolean value as follows:   ((Boolean)ThisImage.get(sFldID)).booleanValue(), where sFldID is
     * keyvalue in the hashmap matching the field name. This would work because only the deck category fields are booleans
     */
    private void setDeckCategories() throws SQLException {
        this.DeckCategories = new LinkedHashSet();
        this.DeckCategories.add("ALL");

        if (this.ThisImage.isEmpty()) {
            /* DummyImage exists only for use in this function. It stands in for ThisImage in case it
             * is empty so that the DeckCategories Set can be populated */
            if (this.DummyImage.isEmpty()) {
                Object oDummyImg = this.session.getAttribute("DUMMYIMAGE");
                if (oDummyImg == null) {
                    setImage(this.DummyImage, 0);
                    this.session.setAttribute("DUMMYIMAGE", this.DummyImage);
                }
                else {
                    this.DummyImage = (LinkedHashMap) oDummyImg;
                }
            }
            this.DeckCategories.addAll(((LinkedHashMap) (this.DummyImage.clone())).keySet());
        }
        else {
            this.DeckCategories.addAll(((LinkedHashMap) (this.ThisImage.clone())).keySet());
        }

        this.DeckCategories.add("UNCATEGORIZED");
        List Unwanted = Arrays.asList(new String[]{"name", "skip", "ID"});
        this.DeckCategories.removeAll(Unwanted);
    }



    public Set getDeckCategories() throws SQLException {
        return this.DeckCategories;
    }



    /**
     * This function returns an ArrayList containing every deck picture thumbnail that has been associated with the
     * category identified by sCategory and is triggered when the user makes a selection from cbxCategory. The
     * ArrayList will be iterated through later on down in the form to build HTML that will display a series of
     * thumbnails for sCategory. When a thumbnail is clicked the form will be posted and will return with the maximum
     * size view style of the thumbnail. Javascript code added here for that click event will set txtIndex to a
     * value that will identify what maximum size image to display in the same that the selectedIndex of cbxJumpTo
     * would if it where being clicked (cbxJumpTo ImagesList turned into an HTML dropdown). This function is triggered
     * when the user makes a selection from cbxCategory.
     */
    private void setCategoryThumbnails() throws SQLException {

        Connection ctn = getDB_Connection();

        String sFldCharLeft = this.bMySQL ? "`" : "[";
        String sFldCharRight = this.bMySQL ? "`" : "]";

        String sSQL = null;
        if (this.sCategory.equalsIgnoreCase("ALL")) {
            sSQL = "SELECT ID, name FROM album ORDER BY " + sFldCharLeft + "name" + sFldCharRight + ";";
        }
        else if (this.sCategory.equalsIgnoreCase("UNCATEGORIZED")) {
            sSQL = "SELECT ID, name FROM album WHERE ";
            Iterator iter = this.DeckCategories.iterator();
            String sAnd = " AND \r\n";
            while (iter.hasNext()) {
                String sNextCategory = (String) iter.next();
                if (!sNextCategory.equalsIgnoreCase("ALL") && !sNextCategory.equalsIgnoreCase("UNCATEGORIZED")) {
                    sSQL += sFldCharLeft + sNextCategory + sFldCharRight + " = False";
                    sSQL += iter.hasNext() ? " AND \r\n" : "";
                }
            }
            if (sSQL.endsWith(sAnd)) {
                sSQL = sSQL.substring(0, (sSQL.length() - sAnd.length())) + " ORDER BY " + sFldCharLeft + "name" + sFldCharRight + ";";
            }
        }
        else {
            sSQL = "SELECT ID, name FROM album WHERE " + sFldCharLeft + this.sCategory + sFldCharRight + " = True ORDER BY " + sFldCharLeft + "name" + sFldCharRight + ";";
        }

        Statement stmt = ctn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet rst = stmt.executeQuery(sSQL);
        ArrayList list = new ArrayList();

        String s;
        int iImages = 1;
        while (rst.next()) {
            String sID = rst.getString("ID");
            String sName = rst.getString("name");
            int iIdx = this.ImagesList.indexOf(sID, sName);
            String sSrc = rst.getString("name");
            /* The image type of a thumbnail will most probably be a gif. If so, getting the filename of the thumbnail will come down to
             * modifying the name of the fullsize image by changing its file extension (probably "jpg") to "gif". No other change
             * will be necessary because by rule in the deckman website, without a file extension, the name of a fullsize image and
             * its corresponding thumbnail should be identical.*/
            sSrc = codeLib.switchFileExtension(sSrc, "gif"); // sSrc will probably have "jpg" file extension. Needs "gif" file extension to become a thumbnail name.
            s = "<img src=\"" + this.sThumbnailContextPath + "/" + sSrc + "\" style='margin:20px; cursor:hand;' onclick=\"javascript: ";
            s += "doSubmit('jump', '" + String.valueOf(iIdx) + "');\">";
            s += iImages % 6 == 0 ? "<br>" : "";
            list.add(s);
            iImages++;
        }

        if (list.isEmpty()) {
            list.add("<div width=300 class='EditFailure' style='margin:50px;'>THERE ARE NO DECK PICTURES TO WHICH THE SELECTED CATEGORY HAS BEEN ASSOCIATED</div>");
        }

        rst.close();
        rst = null;

        this.CategoryThumbs = list;
    }



    public ArrayList getCategoryThumbnails() {
        return this.CategoryThumbs;
    }



    private boolean doEdit(int iIdx) throws SQLException {
        Connection ctn = getDB_Connection();
        String[] sImage = (String[]) this.ImagesList.get(iIdx);
        int iID = Integer.parseInt(sImage[0]);

        // Create the sql for the UPDATE statement and store the values to update to in an array.
        String sSQL = "UPDATE album SET ";
        String sFldCharLeft = this.bMySQL ? "`" : "[";
        String sFldCharRight = this.bMySQL ? "`" : "]";

        // Remove fields that aren't strictly deck categories
        List Unwanted = Arrays.asList(new String[]{"ALL", "UNCATEGORIZED"});
        Set strictCategories = new LinkedHashSet(this.DeckCategories);
        strictCategories.removeAll(Unwanted);

        int iFlds = strictCategories.size();
        boolean[] bValues = new boolean[iFlds];
        for (int i = 0; i < iFlds; i++) {
            String sFldID = (String) (strictCategories.toArray()[i]);
            sSQL += (sFldCharLeft + sFldID + sFldCharRight + " = ?, ");
            String sParam = this.request.getParameter(sFldID);
            bValues[i] = sParam == null ? false : sParam.equals("on");
        }
        sSQL = sSQL.replaceAll(",\\x20$", " WHERE ID = ?;");



        PreparedStatement pstmt = ctn.prepareStatement(sSQL);

        //Set the parameters for the UPDATE statement with the values in the array.
        int i = 0; //"If the control variable is declared inside the loop control structure, it cannot be referenced outside the loop. Hence it is declared before the loop control structure."
        for (; i < bValues.length; i++) {
            pstmt.setBoolean((i + 1), bValues[i]);
        }
        pstmt.setInt((i + 1), iID);

        //Execute the UPDATE statement and close the associated objects.
        int iRecordsAffected = pstmt.executeUpdate();
        setImage(this.ThisImage, iIdx);
        pstmt.close();
        pstmt = null;

        return iRecordsAffected == 1;
    }



    public static void main(String[] args) {

    }
}
