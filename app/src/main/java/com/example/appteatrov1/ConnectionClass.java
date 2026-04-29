package com.example.appteatrov1;
import android.util.Log;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Objects;
public class ConnectionClass {
    protected static String db = "mydb";

    protected static String ip = "10.0.2.2";

    protected static String puerto = "3306";

    protected static String usuario = "root";

    protected static String password = "roku987654";

    public Connection CONN() {
        Connection conn = null;
        try{
            Class.forName("com.mysql.jdbc.Driver");
            String connectionString = "jdbc:mysql://" + ip + ":" + puerto + "/" + db;
            conn = DriverManager.getConnection(connectionString,usuario,password);
        } catch (Exception e) {
            Log.e("Error", Objects.requireNonNull(e.getMessage()));
        }
        return conn;
    }
}
