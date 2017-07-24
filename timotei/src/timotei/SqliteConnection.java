package timotei;

import java.sql.*;
        
public class SqliteConnection {
    public static Connection Connector() {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection("jdbc:sqlite:tietokanta.sqlite");
            return conn;
        } catch (ClassNotFoundException | SQLException ex) {
            return null;
        }
    }
}
