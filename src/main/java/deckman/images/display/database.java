
package deckman.images.display;

import java.sql.*;
import deckman.settings.initialization;


/**
 *
 * @author Warren
 */

public class database {

    private String sDB_Driver = null;
    private String sDB_ConnectionString = null;
    private String sDB_User = null;
    private String sDB_Password = null;
    private boolean bMySQL = false;

    public database(initialization init){
        sDB_Driver = init.getPropertyAsString("DB_Driver");
        sDB_ConnectionString = init.getPropertyAsString("DB_ConnectionString");
        sDB_User = init.getPropertyAsString("DB_User");
        sDB_Password = init.getPropertyAsString("DB_Password");
        bMySQL = sDB_Driver.matches("(?i).*mysql.*");
    }

    public database(String sDB_Driver, String sDB_ConnectionString, String sDB_User, String sDB_Password){
        this.sDB_Driver = sDB_Driver;
        this.sDB_ConnectionString = sDB_ConnectionString;
        this.sDB_Password = sDB_Password;
        this.sDB_User = sDB_User;
    }

    public boolean isMySQL(){
        return bMySQL;
    }
    
        /**
         * This function accesses the global initialization object for the stored properties that
         * are required to open a database connection (user, password, driver, connection string)
         * and returns the connection once opened.
         * @return An open database connection
         * @throws java.lang.ClassNotFoundException
         * @throws java.sql.SQLException
         */
    public Connection getDB_Connection() throws ClassNotFoundException, SQLException {

        Connection DbConn = null;

        Class.forName(sDB_Driver);
        if (bMySQL) {
            //ctn = DriverManager.getConnection("jdbc:mysql://localhost/deckman?user=root&password=mypassword");
            DbConn = DriverManager.getConnection(sDB_ConnectionString, sDB_User, sDB_Password);
        }
        else {
            DbConn = DriverManager.getConnection(sDB_ConnectionString);
        }

        return DbConn;
    }



    /**
     * This function closes the a provided java.sql.connection object if it is not
     * already and sets it to null. Thorough attempts at error handling and prevention are used.
     * @param DbConn
     * @throws java.sql.SQLException
     */
    public void breakDB_Connection(Connection DbConn) throws SQLException {
        if (DbConn != null) {
            try {
                if (DbConn.isValid(5)) {
                    DbConn.close();
                }
                else {
                    if (!DbConn.isClosed()) {
                        DbConn.close();
                    }
                }
            }
            catch (AbstractMethodError e) {
                // probably means the version of the JDBC driver does not implement the isValid method
                try {
                    if (!DbConn.isClosed()) {
                        DbConn.close();
                    }
                }
                catch (Exception ex) {
                }
            }
            DbConn = null;
        }
    }

}
