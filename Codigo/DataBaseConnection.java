package proyectotbd2;

import java.sql.Connection;
import java.sql.DriverManager;

public class DataBaseConnection {

    public static Connection connect(String connectionType,String host,String port,String engine,String dbPath,String user,String password) {
        Connection connection = null;
        try {
            Class.forName("sap.jdbc4.sqlanywhere.IDriver");
            String url;
            if(connectionType.equals("FILE")) {
                url = "jdbc:sqlanywhere:"+"uid="+user+";"+"pwd="+password+";"+"dbf="+dbPath;
            }else{
                url = "jdbc:sqlanywhere:"+"host="+host+":"+port+";"+"eng="+engine+";"+ "uid=" + user + ";"+"pwd="+password;
            }
            connection =DriverManager.getConnection(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connection;
    }
}