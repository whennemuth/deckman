package deckman.tests;

import java.sql.*;

public class mySqlTest {
    
    public mySqlTest() {
        
    }
    
    public static void main(String[] args){
        Connection ctn;
        boolean bMySql = true;
        try{
            if(bMySql){
                Class.forName("com.mysql.jdbc.Driver");
                //ctn = DriverManager.getConnection("jdbc:mysql://localhost/deckman?user=root&password=mypassword");
                ctn = DriverManager.getConnection("jdbc:mysql://localhost/deckman", "root", "mypassword");
            }
            else{
                Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
                ctn = DriverManager.getConnection("jdbc:odbc:deckman");
            }
            
            Statement stmt = ctn.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = stmt.executeQuery("SELECT * FROM album;");
            while (rs.next()){
                System.out.println(rs.getString("text"));
            }
        }
        catch(java.lang.ClassNotFoundException e1){
            System.out.println(e1.toString());
            e1.printStackTrace();
        }
        catch(java.sql.SQLException e2){
            System.out.println(e2.toString());
            e2.printStackTrace();
        }
    }
    
}
