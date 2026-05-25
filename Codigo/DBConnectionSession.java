package proyectotbd2;

import java.sql.Connection;

public class DBConnectionSession {
    private String connectionName;
    private Connection connection;

    public DBConnectionSession(String connectionName, Connection connection) {
        this.connectionName = connectionName;
        this.connection = connection;
    }

    public String getConnectionName() {
        return connectionName;
    }

    public Connection getConnection() {
        return connection;
    }

    @Override
    public String toString() {
        return connectionName;
    }
}