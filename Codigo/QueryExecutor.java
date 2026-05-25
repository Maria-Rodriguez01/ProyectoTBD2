package proyectotbd2;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import javax.swing.table.DefaultTableModel;

public class QueryExecutor {

    public static DefaultTableModel executeQuery(Connection conn, String sql) throws Exception {
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            String upperSql = sql.trim().toUpperCase();

            if (upperSql.startsWith("SELECT") || upperSql.startsWith("CALL")) {
                rs = stmt.executeQuery(sql);
                return buildTableModel(rs);
            } else {
                stmt.execute(sql);
                return null;
            }
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
        }
    }

    private static DefaultTableModel buildTableModel(ResultSet rs) throws Exception {
        ResultSetMetaData meta = rs.getMetaData();
        int columns = meta.getColumnCount();
        DefaultTableModel model = new DefaultTableModel();

        for (int i = 1; i <= columns; i++) {
            model.addColumn(meta.getColumnName(i));
        }

        while (rs.next()) {
            Object[] row = new Object[columns];
            for (int i = 1; i <= columns; i++) {
                row[i - 1] = rs.getObject(i);
            }
            model.addRow(row);
        }
        return model;
    }
}

