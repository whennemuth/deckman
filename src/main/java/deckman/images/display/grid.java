/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package deckman.images.display;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Warren
 */
public class grid {

    private int rows = 0;
    private int cols = 0;
    private int highestItemLength = 0;
    private Object[][] gridArray = null;

    public grid(Object[] items, int rows, int cols, int page){
        this.rows = rows;
        this.cols = cols;
        setGridArray(items, page);
    }

    /**
     * Items are displayed on a page in x rows, y images per row, where x and y
     * are specified as parameters in the constructor of this class. This function determines
     * the images of a particular page as they would be arranged in this "grid" fashion.
     * @param iPage - number of the page whose images the x and y grid arrangement is to be established
     * @return An array of string arrays representing a the "grid" of thumbnail images for the
     * page at the iPage position. This page is one of a series of pages it would take to display
     * all items.
     */
    private void setGridArray(Object[] items, int iPage){
        int iGridCapacity = rows * cols;
        int iStart = (iPage - 1) * iGridCapacity;  // gets the index in the paginator base ArrayList of the first item on the page indicated by iPage
        gridArray = new Object[rows][cols];

        outerLoop:
        for (int x = 0; x < gridArray.length; x++) {
            for (int y = 0; y < gridArray[x].length; y++) {
                try{
                    Object item = items[iStart++];
                    String sItem = item.toString();
                    if(this.highestItemLength < sItem.length()) this.highestItemLength = sItem.length();
                    gridArray[x][y] = item;
                }
                catch(ArrayIndexOutOfBoundsException e){
                    // must be that iPage is the last possible page where the images in it only fill up a portion of the grid capacity
                    break outerLoop;
                }
            }
        }
    }
    

    private String buildGrid(grid.designer designer){
        String sGrid = "";

        outerLoop:
        for (int x = 0; x < gridArray.length; x++) {
            String sRowContent = "";
            for (int y = 0; y < gridArray[x].length; y++) {
                if(gridArray[x][y] == null){
                    /* must be that gridArray was created based on the last page of items possible page where the
                     images in it only fill up a portion of the grid capacity. The remainder of the grid is nulls. */
                    sRowContent += designer.getCell("");
                }
                else{
                    sRowContent += designer.getCell(gridArray[x][y]);
                }
            }
            sGrid += designer.getRow(sRowContent);
        }

        return sGrid;
    }


    @Override
    public String toString() {
        final int highestLength = this.highestItemLength;
        return buildGrid(new grid.designer() {



            public String getCell(Object oCellContent) {
                String sCellContent = oCellContent.toString();
                int iSpaces = highestLength - sCellContent.length();
                for(int i=-2; i<iSpaces; i++){
                    sCellContent += " ";
                }
                return sCellContent;
            }



            public String getRow(String sRowContent) {
                return (sRowContent + "\r\n");
            }
        });
    }


    public String toDesignedString(grid.designer designer){
        return buildGrid(designer);
    }


    public static interface designer {
        public String getCell(Object oCellContent);
        public String getRow(String sRowContent);
    }

    public static void main(String[] args){

        args = new String[]{"html"};
        
        String sUsage = "USAGE: Single Parameter:[\"html\" or \"text\"]";

        if(args.length != 1){
            System.out.println(sUsage);
        }
        else{
            
            Object[] objs = new Object[100];
            String sRandom = "Some random words that are being used to obtain variable lengthed items to go into the " +
                    "objs array. If all of the content were to be of the same length, then the functionality that " +
                    "presents the grid in aligned (not zigzagged) rows and columns using whitespace padding could not " +
                    "be properly tested. This would be unfortunate because I really want to test this. At the end of " +
                    "the last sentence I was at about 63 characters, so I should be reaching 100 characters right " +
                    "about now? No, I'm at 86, which means that I need to consider myself done now";
            
            String[] aRandom = sRandom.split("\\x20+"); // split on the space character(s)
            
            for(int i=0; i<objs.length; i++){
                objs[i] = aRandom[i] + String.valueOf(i);
            }
            int iRows = 5;
            int iCols = 6;
            int iPage = 1;
            grid myGrid = new grid(objs, iRows, iCols, iPage);            
            
            if(args[0].equalsIgnoreCase("text")){
                System.out.println(myGrid.toString());
            }
            else if(args[0].equalsIgnoreCase("html")){

                grid.designer designer = new grid.designer() {



                    public String getCell(Object oCellContent) {
                        String sCellVal = oCellContent.toString();
                        Pattern p = Pattern.compile("\\d+$");
                        Matcher m = p.matcher(sCellVal);
                        boolean bOdd = false;
                        if(m.find()){
                            int i = Integer.parseInt(m.group());
                            if(i % 2 == 1) bOdd = true;
                        }
                        return "\t<td" + (bOdd?" bgcolor='gray'":"") + ">" + sCellVal + "</td>\r\n";
                    }



                    public String getRow(String sInnerHTML) {
                        return "<tr>\r\n" + sInnerHTML + "</tr>\r\n";
                    }


                    public int compareTo(Object o) {
                        return 0;   // not needing comparatibility yet, so returning zero should neutralize it (I think).
                    }
                };


                System.out.println(myGrid.toDesignedString(designer));
            }
            else{
                System.out.println(sUsage);
            }
        }

// RESUME NEXT 1: Rename the jsp pages in fireworks without the lead capital letter and redeploy (online too.)

// RESUME NEXT 2: Code in an "down for maintenance" redirect that can be toggled using a property set from settings_3.jsp

// RESUME NEXT 3: finish javadoc content for and format imagePaginator and grid java files and photos.jsp and photogenerator.jsp.
    }
}
