import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.io.*;

public class browse extends HttpServlet{
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("text/html");
        try{
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            Connection ctn = DriverManager.getConnection("jdbc:odbc:deckman");
            writeHTML(out, ctn, false);
        }
        catch(Exception e){}
        out.close();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("text/html");
        try{
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            Connection ctn = DriverManager.getConnection("jdbc:odbc:deckman");
            writeHTML(out, ctn, true);
        }
        catch(Exception e){}
        out.close();
    }


    private String getCategoryOptions(Connection ctn){ //throws SQLException, ClassNotFoundException {
        try{
            Statement stmt = ctn.createStatement();
            ResultSet rst = stmt.executeQuery("SELECT TOP 1 * FROM album;");
            ResultSetMetaData rsmd = rst.getMetaData();
            int iFlds = rsmd.getColumnCount();
            String s = "";
            for(int i=1; i<=iFlds; i++){
                String sFld = rsmd.getColumnName(i);
                    //use the equals() method instead of the intern() method if comparing two string variables.
                    //since a string variable (object) is being compared to a string literal here, the intern() method is used.
                if((sFld.intern() != "text") && (sFld.intern() != "src") && (sFld.intern() != "href") && (sFld.intern() != "skip")){
                    s += "<option value='" + sFld + "'>" + sFld + "</option>\r\n";
                }
            }
            return(s);
        }
        catch(Exception e){ return("<option>" + e.toString().replaceAll("\\:\\x20", "</option><option>") + "</option>\r\n"); }
    }


    private String getItemOptions(Connection ctn){ //throws SQLException, ClassNotFoundException {
        try{
            Statement stmt = ctn.createStatement();
            ResultSet rst = stmt.executeQuery("SELECT text FROM album;");
            String s = "";
            while(rst.next()){
                String sVal = rst.getString("text");
                s += "<option value='" + sVal + "'>" + sVal + "</option>\r\n";
            }
            return(s);
        }
        catch(Exception e){ return("<option>" + e.toString().replaceAll("\\:\\x20", "</option><option>") + "</option>"); }
    }

    private String getRadioButtons(Connection ctn){ //throws SQLException, ClassNotFoundException {
        try{
            Statement stmt = ctn.createStatement();
            ResultSet rst = stmt.executeQuery("SELECT TOP 1 * FROM album;");
            ResultSetMetaData rsmd = rst.getMetaData();
            int iFlds = rsmd.getColumnCount();
            String s = "";
            for(int i=1; i<=iFlds; i++){
                String sFld = rsmd.getColumnName(i);
                    //use the equals() method instead of the intern() method if comparing two string variables.
                    //since a string variable (object) is being compared to a string literal here, the intern() method is used.
                if((sFld.intern() != "text") && (sFld.intern() != "src") && (sFld.intern() != "href") && (sFld.intern() != "skip")){
                    s += "<tr>";
                    s += "<td align='right'>" + sFld + "</td>";
                    s += ("<td><input type='checkbox' id='chk" + i + "' name='chk" + i + "'></td>");
                    s += "</tr>";
                }
            }
            return(s);
        }
        catch(Exception e){ return("<tr><td>" + e.toString().replaceAll("\\:\\x20", "<br>") + "</td></tr>\r\n"); }
    }

    private void writeHTML(PrintWriter out, Connection ctn, boolean bPost){
        String s ="<html>\r\n" +
        "<head>\r\n" +
        "<style type='text/css'>\r\n" +
        ".font1 {\r\n" +
        "   font-family: Verdana, Arial, Helvetica, sans-serif;\r\n" +
        "   font-size: 12px;\r\n" +
        "}\r\n" +
        ".font2 {\r\n" +
        "	font-family: Arial, Helvetica, sans-serif;\r\n" +
        "	font-size: 10px;\r\n" +
        "}\r\n" +
        "</style>\r\n" +
        "<script language='javascript' src='scriptlib.js'></script>\r\n" +
        "</head>\r\n" +
        "<body leftmargin='30' topmargin='30'>\r\n" + 
        "<table border='0' cellpadding=6 style='font-family:verdana; font-size:12px;'>\r\n" + 
        "<tr>\r\n" + 
        "	<td align='right'>Display all images by category (pick one):</td>\r\n" + 
        "	<td>\r\n" + 
        "		<select onchange=\"javascript:doSubmit({task:'display images by category', val:this.value});\">\r\n" + 
        "			<option value=''><- Select One -></option>\r\n" + 
                                getCategoryOptions(ctn) + 
        "		</select>\r\n" + 
        "	</td>\r\n" + 
        "</tr>\r\n" + 
        "<tr>\r\n" + 
        "	<td align='right'>Edit images one by one (start at):</td>\r\n" + 
        "	<td>\r\n" + 
        "		<select onchange=\"javascript:doSubmit({task:'edit images one by one', val:this.value});\">\r\n" + 
        "			<option value=''><- Select One -></option>\r\n" + 
                                getItemOptions(ctn) + 
        "		</select>\r\n" + 
        "	</td>\r\n" + 
        "</tr>\r\n" + 
        "</table>\r\n" + 
        "<br>" + 
        "<table cellspacing='0' cellpadding='2' class='font1'>" + 
        " <tr>" + 
        "  <td colspan=2> <input type='button' value='<< Last' class='font2' style='width:65'>" +
        "   <input type='button' value='Next >>' class='font2' style='width:65'>    " +
        "   <input type='button' value='COMMIT CHANGES' class='font2' style='width:110' onclick='javascript:doSubmit();'>" +
        "  </td>" +
        "  <td rowspan='18' valign='top' style='padding-left:10'><img src='#' width=300 height=300></td>" +
        " </tr>" +
        " <tr>" +
        "  <td colspan=2><hr width='100%'></td>" +
        " </tr>" +
        " <tr>" +
        "  <td colspan=2></td>" +
        " </tr>" + 
                getRadioButtons(ctn) +
        "</body>\r\n" + 
        "</html>";
        out.write(s);
    }
}