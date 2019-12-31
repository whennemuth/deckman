<%-- 
    Document   : photogenerator
    Created on : Dec 14, 2008, 3:40:33 AM
    Author     : Warren
--%>

<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="deckman.images.display.*, deckman.settings.initialization;"%>

<style>
    .cell{
        border-style: solid;
        border-width: 1px;
        border-color: #99C2AD;
    }
    
    .noborder{
        border-style: none;
    }
    
    .pagelinkActive{
        font-family: monospace;
        font-size: 12px;
        text-decoration: none;
        color: #2F221A;
    }
    
    .pagelinkDead{
        font-family: monospace;
        font-size: 12px;
        text-decoration: none;
        color: #AE9E8F;
    }
    
    .pagelink_selected{
        font-family: monospace;
        font-size: 14px;
        font-weight: bold;
        color: blue;
    }
</style>

<%
        String sSelectedPage = request.getParameter("page");
        int iSelectedPage = sSelectedPage == null ? 1 : Integer.parseInt(sSelectedPage);

        String sCategoryURLencoded = request.getParameter("category");
        String sCategoryURLdecoded = null;
        if (sCategoryURLencoded != null) {
            sCategoryURLdecoded = java.net.URLDecoder.decode(sCategoryURLencoded, "UTF-8");
        }

        initialization init = initialization.getInstance(session);
        imagePaginator paginator = new imagePaginator(5, 7, new String[]{sCategoryURLdecoded}, init);
%>

<%!
    public String getCategoriesListBox(imagePaginator paginator, String sCategoryURLencoded,
            String sCategoryURLdecoded, JspWriter out) throws java.io.IOException {

        String[] categories = paginator.getCategoryListing();
        StringBuffer buff = new StringBuffer();
        buff.append("<select name='category' style='width:246px' onchange='javascript: document.forms[0].submit();'>\r\n");
        buff.append("<option " + (sCategoryURLencoded == null ? "selected" : "") + "><- Select a Category -></option>\r\n");

        for (int i = 0; i < categories.length; i++) {
            String sSelected = categories[i].equalsIgnoreCase(sCategoryURLdecoded) ? "selected" : "";
            String sVal = java.net.URLEncoder.encode(categories[i], "UTF-8");
            buff.append("<option value=\"" + sVal + "\" " + sSelected + ">" + categories[i] + "</option>\r\n");
        }

        buff.append("</select>\r\n");

        return buff.toString();
    }

    public void printIndexOfGrids(imagePaginator paginator, int iSelectedPage, String sCategoryURLencoded,
            HttpServletRequest request, JspWriter out) throws java.io.IOException {

        if (sCategoryURLencoded != null) {

            out.print("<table width='800px'><tr><td align='center'>");

            String sPrevious, sIndex, sNext;

            if (iSelectedPage == 1) {
                sPrevious = "&nbsp;&nbsp;<span class='pagelinkDead'>&lt;previous</span>&nbsp;&nbsp;";
            } else {
                sPrevious = "&nbsp;&nbsp;<a " +
                        "class='pagelinkActive' " +
                        "href=\"photos.jsp?" +
                        "page=" + String.valueOf(iSelectedPage - 1) +
                        "&category=" + sCategoryURLencoded +
                        "\">&lt;previous</a>&nbsp;&nbsp;";
            }
            out.print(sPrevious);

            for (int i = 1; i <= paginator.getPageCount(); i++) {
                if (iSelectedPage == i) {
                    sIndex = "<span class='pagelink_selected'>" +
                            String.valueOf(i) +
                            "</span>&nbsp;&nbsp;";
                } else {
                    sIndex = "<a " +
                            "class='pagelinkActive' " +
                            "href=\"photos.jsp?" +
                            "page=" + String.valueOf(i) +
                            "&category=" + sCategoryURLencoded +
                            "\">" + String.valueOf(i) + "</a>&nbsp;&nbsp;";
                }
                out.print(sIndex);
            }

            int iPages = paginator.getPageCount();
            if (iPages == iSelectedPage) {
                sNext = "<span class='pagelinkDead'>next&gt;</span>&nbsp;&nbsp;";
            } else {
                sNext = "<a " +
                        "class='pagelinkActive' " +
                        "href=\"photos.jsp?" +
                        "page=" + String.valueOf(iSelectedPage + 1) +
                        "&category=" + sCategoryURLencoded +
                        "\">next&gt;</a>&nbsp;&nbsp;";
            }
            out.print(sNext);
            out.print("</td></tr></table>");
        }
    }

    public void printGrid(imagePaginator paginator, int iSelectedPage, HttpServletRequest request, JspWriter out) throws java.io.IOException {

        grid.designer designer = new grid.designer() {

            public String getCell(Object oCellContent) {
                if (oCellContent instanceof imagePaginator.imageItem) {
                    imagePaginator.imageItem item = (imagePaginator.imageItem) oCellContent;
                    String sCell =
                            "\t" +
                            "<td>" +
                            "<a href=\"" + item.getImageSrc() + "\" target='_blank'>" +
                            "<img src=\"" + item.getThumbSrc() + "\" class='cell'>" +
                            "</a>" +
                            "</td>" +
                            "\r\n";
                    return sCell;
                } else {
                    return "\t<td class='noborder'>" + oCellContent.toString() + "</td>\r\n";
                }
            }

            public String getRow(String sInnerHTML) {
                return "<tr>\r\n" + sInnerHTML + "</tr>\r\n";
            }
        };

        out.println("<table cellpadding='6px' cellspacing=0 border='0'>");
        out.print(paginator.getGridHTML(designer, iSelectedPage));
        out.println("</table>");
    }
%>
