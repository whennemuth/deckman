package deckman.images.display;

import java.sql.*;
import java.util.*;
import deckman.settings.initialization;
import deckman.utils.codeLib;


/**
 * This class provides the logic for a certain display scenario where a number of items are to be
 * displayed by being distributed over a series of pages. The items are to be arranged on each page
 * in a grid fashion with the grid containing a specified number of items. Number of pages, items
 * per grid and the constituent items for each grid referred to by index are calculated by this class.
 * This class can be used to drive a web based pagination display consisting of a series of consequetive
 * page numbers as commonly seen on a website that has more items to display than can be
 * conveniently retrieved and viewed on one page.
 * @author Warren
 */
public class imagePaginator extends ArrayList {

    private int iRowsPerPage = 0;
    private int iColsPerPage = 0;
    private String[] AllCategories = null;
    private String[] SelectedCategories = null;
    private int iPages = 0;
    private String sExceptionStackTrace = null;
    initialization init = null;



    /**
     * Constructor - Will create a paginator that represents all items (non-filtered)
     * @param iRowsPerPage - How many rows in a "grid" of items.
     * @param iColsPerPage - How many columns in a "grid" of items.
     * @param init - Will be used to access stored properties required to establish a connection
     * to the database from which will be retrieved all items to be displayed.
     */
    public imagePaginator(int iRowsPerPage, int iColsPerPage, initialization init) {
        this(iRowsPerPage, iColsPerPage, null, init);
    }



    /**
     * Constructor - Will create a filtered paginator in that only those items whose
     * category matches the SelectedCategories parameter will be represented.
     * @param iRowsPerPage - How many rows in a "grid" of items.
     * @param iColsPerPage - How many columns in a "grid" of items.
     * @param SelectedCategories - Images are to be displayed on a webpage. Usually not
     * all of them will be because there will be a filter that reduces the list to images of
     * a certain category or categories. This category filter is set here as an array of categories.
     * @param init - Will be used to access stored properties required to establish a connection
     * to the database from which will be retrieved all items to be displayed.
     */
    public imagePaginator(int iRowsPerPage, int iColsPerPage, String[] SelectedCategories, initialization init) {
        this.iRowsPerPage = iRowsPerPage;
        this.iColsPerPage = iColsPerPage;
        this.init = init;
        this.SelectedCategories = SelectedCategories;

        try {
            populateBaseImageListing(); // get all images from the database
            setPageCount(); // set how many web pages it will take to display all images in selected categories
        }
        catch (ClassNotFoundException e) {
            this.sExceptionStackTrace = codeLib.getStackTraceAsString(e);
        }
        catch (SQLException e) {
            this.sExceptionStackTrace = codeLib.getStackTraceAsString(e);
        }
        catch (Exception e) {
            this.sExceptionStackTrace = codeLib.getStackTraceAsString(e);
        }
    }



    /**
     * This function populates the ArrayList superclass of this imagePaginator instance with
     * one list item for each row in the album table in the deckman database. The name of each
     * fullsize image and the correponding thumbnail name as a pair are added in the form of
     * a Map to the list. This full listing will make it unnecessary to make an additional
     * calls to the database as all the image data is extracted (as opposed to only the data
     * necessary to cover those images that would be displayed on a single page).
     * @throws java.lang.ClassNotFoundException
     * @throws java.sql.SQLException
     */
    private void populateBaseImageListing() throws ClassNotFoundException, SQLException {
        database db = new database(this.init);
        Connection ctn = db.getDB_Connection();
        boolean bPopulate = true;
        String sFldCharLeft = db.isMySQL() ? "`" : "[";
        String sFldCharRight = db.isMySQL() ? "`" : "]";
        String sql;

        if(this.SelectedCategories[0] == null){
                // get the first record, but only for the metadata - the record will not be added as an imageItem
            bPopulate = false;
            if (db.isMySQL()) {
                sql = "SELECT * FROM album LIMIT 0,1;";
            }
            else {
                sql = "SELECT TOP 1 * FROM album;";
            }
        }
        else if(this.SelectedCategories[0].equalsIgnoreCase("ALL CATEGORIES")){
                // populate with images from all categories by default
            sql = "SELECT * FROM album WHERE " + sFldCharLeft + "DO NOT USE" + sFldCharRight + " = False;";            
        }
        else{
            sql = "SELECT * FROM album WHERE (";
            String sOr = " OR \r\n";
            String[] a = this.SelectedCategories;

            for(int i=0; i<a.length; i++){
                String sNextCategory = a[i];
                sql += sFldCharLeft + sNextCategory + sFldCharRight + " = True";
                sql += ((i+1) == a.length) ? "" : sOr;
            }

            sql += ") AND (" + sFldCharLeft + "DO NOT USE" + sFldCharRight + " = False) " +
                    "ORDER BY " + sFldCharLeft + "name" + sFldCharRight + ";";
        }

        Statement stmt = ctn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet rst = stmt.executeQuery(sql);

        if(bPopulate){
            while (rst.next()) {
                String sImageName = rst.getString("name");
                String sThumbContext = this.init.getPropertyAsString("ThumbnailContextPath");
                String sImageContext = this.init.getPropertyAsString("FullsizeContextPath");
                this.add(new imageItem(sImageName, sThumbContext, sImageContext));
            }
        }
        
        setCategoryListing(rst.getMetaData());

        rst.close();
        rst = null;

        db.breakDB_Connection(ctn);
    }



    /**
     * This function returns the name of each deck category as represented by each applicable
     * field name of the album table in the deckman database.
     * @param rsmd - A ResultSetMetaData object used to obtain the field (category) names
     * @throws java.sql.SQLException
     */
    private void setCategoryListing(ResultSetMetaData rsmd) throws SQLException {
        int iFlds = rsmd.getColumnCount();
        AllCategories = new String[iFlds - 3];

        int iCat = 0;
        for (int i = 1; i <= iFlds; i++) {
            String sFldName = rsmd.getColumnName(i);
            if (!sFldName.equalsIgnoreCase("name") &&
                    !sFldName.equalsIgnoreCase("id") &&
                    !sFldName.equalsIgnoreCase("do not use") &&
                    !sFldName.equalsIgnoreCase("mark for deletion")) {
                AllCategories[iCat++] = sFldName;
            }
        }
        AllCategories[iCat] = "ALL CATEGORIES";
    }



    /**
     * @return the category listing as described in {@link #setCategoryListing setCategoryListing}
     */
    public String[] getCategoryListing() {
        return this.AllCategories;
    }



    /**
     * Images are to be displayed on a webpage. Usually not all of them will be because
     * there will be a filter that reduces the list to images of a certain category or
     * categories. This category filter is set here as an array of categories.
     * @param cats - The array of categories as described above.
     */
    public void setSelectedCategoryListing(String[] cats) {
        this.SelectedCategories = cats;
    }



    /**
     * @return the category array described in {@link #setSelectedCategoryListing setSelectedCategoryListing}
     */
    public String[] getSelectedCategoryListing() {
        return this.SelectedCategories;
    }



    /**
     * Thumbnail images are displayed on a webpage in x rows, y images per row, where x and y
     * are specified as parameters in the constructor of this class. Also knowing how many total
     * images combine from selected categories, the number of pages it will take to display them
     * all in this grid fashion can be calculated here, with this.iPages receiving the result.
     */
    private void setPageCount() {
        int iImagesPerPage = iRowsPerPage * iColsPerPage;
        int iTotalImages = this.size();
        if (iTotalImages <= iImagesPerPage) {
            iPages = 1;
        }
        else {
            iPages = iTotalImages / (iImagesPerPage);
            if (iTotalImages % iImagesPerPage > 0) {
                iPages++;
            }
        }
    }



    /**
     * @return Thumbnail images are to be displayed in a grid fashion, with x images per grid
     * and one grid per page. Returned here is how many grids (pages) it will take to display
     * all the thumbnail images.
     */
    public int getPageCount() {
        return iPages;
    }


    public String getGridHTML(grid.designer designer, int iPage) {
        grid imgGrid = new grid(this.toArray(), iRowsPerPage, iColsPerPage, iPage);
        return imgGrid.toDesignedString(designer);
    }


    public class imageItem {
        private String sImageName = null;
        private String sThumbContext = null;
        private String sImageContext = null;

        public imageItem(String sImageName, String sThumbContext, String sImageContext){
            this.sImageName = sImageName;
            this.sImageContext = sImageContext;
            this.sThumbContext = sThumbContext;
        }
        public String getImageName(){
            return sImageName;
        }
        public String getImageSrc(){
            return (sImageContext + "/" + sImageName);
        }
        public String getThumbName(){
            return codeLib.switchFileExtension(sImageName, "gif");
        }
        public String getThumbSrc(){
            return (sThumbContext + "/" + getThumbName());
        }
        @Override
        public String toString() {
            return codeLib.removeFileExtension(sImageName);
        }
    }
    
    @Override
    public String toString(){

        String s = "";
        if (this.sExceptionStackTrace == null) {
            Iterator iter = this.iterator();
            s += ("\r\nPAGE COUNT:\r\n" + this.getPageCount() + "\r\n");
            s += "\r\n";
            
            s += ("SELECTED CATEGORIES:" + "\r\n");
            String[] cats = this.getSelectedCategoryListing();
            for (int i = 0; i < cats.length; i++) {
                s += (cats[i] + "\r\n");
            }
            s += "\r\n";
            
            s += ("ALL CATEGORIES" + "\r\n");
            cats = this.getCategoryListing();
            for (int i = 0; i < cats.length; i++) {
                s += (cats[i] + "\r\n");
            }
            s += "\r\n";

            s += ("IMAGES IN SELECTED CATEGORIES (" + String.valueOf(this.size()) + ")\r\n");
            while(iter.hasNext()){
                imageItem image = (imageItem)iter.next();
                s += (image.toString() + "\r\n");
            }

            return s;
        }
        else {
            return this.sExceptionStackTrace;
        }

    }

    public static void main(String[] args) {

        String[] selectedCats = new String[]{
//            "Deck, pressure treated",
            "Deck, mahogany",
//            "Deck, white cedar",
            "Deck, composite",
//            "Deck, other",
            "Rail, pressure treated",
//            "rail, mahogany",
//            "rail, white cedar",
//            "rail, synthetic",
//            "rail, other",
//            "Benches",
//            "Walkways",
//            "Facia options",
//            "Porches",
//            "Gazebos, arbors, trellis",
//            "Under deck enclosure",
//            "Roof decks",
//            "Privacy Fences/Screens",
            "Showers"
        };

        initialization init = initialization.getInstance();
        imagePaginator paginator = new imagePaginator(5, 8, selectedCats, init);
        System.out.println(paginator.toString());
    }

}
