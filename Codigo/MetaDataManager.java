package proyectotbd2;

import java.sql.*;
import java.util.ArrayList;

public class MetaDataManager {

    private static String getCurrentUser(Connection conn) {
        try (ResultSet rs = conn.createStatement().executeQuery("SELECT CURRENT USER")) {
            if (rs.next()) return rs.getString(1).toUpperCase();
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public static ArrayList<String> getTables(Connection conn) {
        ArrayList<String> list = new ArrayList<>();
        String user = getCurrentUser(conn);
        if (user == null) return list;
        try (ResultSet rs = conn.createStatement().executeQuery(
            "SELECT t.table_name FROM SYSTABLE t JOIN SYSUSER u ON t.creator = u.user_id " +
            "WHERE t.table_type = 'BASE' AND UPPER(u.user_name) = '" + user + "'"
        )) { while (rs.next()) list.add(rs.getString("table_name")); }
        catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public static ArrayList<String> getColumns(Connection conn, String tableName) {
        ArrayList<String> list = new ArrayList<>();
        try (ResultSet rs = conn.createStatement().executeQuery(
            "SELECT column_name FROM SYSCOLUMN WHERE table_id = " +
            "(SELECT table_id FROM SYSTABLE WHERE table_name = '" + tableName + "')"
        )) { while (rs.next()) list.add(rs.getString("column_name")); }
        catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public static ArrayList<String> getViews(Connection conn) {
        ArrayList<String> list = new ArrayList<>();
        String user = getCurrentUser(conn);
        if (user == null) return list;
        try (ResultSet rs = conn.createStatement().executeQuery(
            "SELECT t.table_name FROM SYSTABLE t JOIN SYSUSER u ON t.creator = u.user_id " +
            "WHERE t.table_type = 'VIEW' AND UPPER(u.user_name) = '" + user + "'"
        )) { while (rs.next()) list.add(rs.getString("table_name")); }
        catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public static ArrayList<String> getProceduresAndFunctions(Connection conn) {
        ArrayList<String> list = new ArrayList<>();
        String user = getCurrentUser(conn);
        if (user == null) return list;
        try (ResultSet rs = conn.createStatement().executeQuery(
            "SELECT p.proc_name FROM SYSPROCEDURE p JOIN SYSUSER u ON p.creator = u.user_id " +
            "WHERE UPPER(u.user_name) = '" + user + "'"
        )) { while (rs.next()) list.add(rs.getString("proc_name")); }
        catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public static ArrayList<String> getUsers(Connection conn) {
        ArrayList<String> list = new ArrayList<>();
        try (ResultSet rs = conn.createStatement().executeQuery(
            "SELECT user_name FROM SYSUSER WHERE user_name NOT LIKE 'SYS%' AND user_name NOT LIKE 'ISYS%'"
        )) { while (rs.next()) list.add(rs.getString("user_name")); }
        catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public static ArrayList<String> getTriggers(Connection conn) {
        ArrayList<String> list = new ArrayList<>();
        String user = getCurrentUser(conn);
        if (user == null) return list;
        try (ResultSet rs = conn.createStatement().executeQuery(
            "SELECT tr.trigger_name FROM SYSTRIGGER tr JOIN SYSTABLE t ON tr.table_id = t.table_id " +
            "JOIN SYSUSER u ON t.creator = u.user_id WHERE UPPER(u.user_name) = '" + user + "'"
        )) { while (rs.next()) list.add(rs.getString("trigger_name")); }
        catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public static ArrayList<String> getIndexes(Connection conn) {
        ArrayList<String> list = new ArrayList<>();
        String user = getCurrentUser(conn);
        if (user == null) return list;
        try (ResultSet rs = conn.createStatement().executeQuery(
            "SELECT i.index_name FROM SYSINDEX i JOIN SYSTABLE t ON i.table_id = t.table_id " +
            "JOIN SYSUSER u ON t.creator = u.user_id WHERE UPPER(u.user_name) = '" + user + "'"
        )) { while (rs.next()) list.add(rs.getString("index_name")); }
        catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public static ArrayList<String> getSequences(Connection conn) {
        ArrayList<String> list = new ArrayList<>();
        String user = getCurrentUser(conn);
        if (user == null) return list;
        try (ResultSet rs = conn.createStatement().executeQuery(
            "SELECT s.sequence_name FROM SYSSEQUENCE s JOIN SYSUSER u ON s.owner = u.user_id " +
            "WHERE UPPER(u.user_name) = '" + user + "'"
        )) { while (rs.next()) list.add(rs.getString("sequence_name")); }
        catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public static ArrayList<String> getTablespaces(Connection conn) {
        ArrayList<String> list = new ArrayList<>();
        try (ResultSet rs = conn.createStatement().executeQuery(
            "SELECT dbspace_name FROM SYSDBSPACE WHERE dbspace_name NOT LIKE 'SYS%' AND dbspace_name NOT LIKE 'ISYS%'"
        )) { while (rs.next()) list.add(rs.getString("dbspace_name")); }
        catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public static String generateTableDDL(Connection conn, String tableName) {
        StringBuilder ddl = new StringBuilder("CREATE TABLE " + tableName + " (\n");
        ArrayList<String> pkCols = new ArrayList<>();

        try {
            try (ResultSet pk = conn.createStatement().executeQuery(
                "SELECT c.column_name FROM SYSCOLUMN c " +
                "JOIN SYSIXCOL ic ON c.table_id = ic.table_id AND c.column_id = ic.column_id " +
                "JOIN SYSINDEX i   ON ic.table_id = i.table_id  AND ic.index_id  = i.index_id " +
                "WHERE i.index_category = 1 " +
                "AND c.table_id = (SELECT table_id FROM SYSTABLE WHERE table_name = '" + tableName + "')"
            )) { while (pk.next()) pkCols.add(pk.getString("column_name")); }
            catch (Exception ignored) {}

            try (ResultSet rs = conn.createStatement().executeQuery(
                "SELECT c.column_name, c.width, c.scale, c.nulls, c.default_value, " +
                "c.column_type, c.domain_id " +
                "FROM SYSCOLUMN c " +
                "WHERE c.table_id = (SELECT table_id FROM SYSTABLE WHERE table_name = '" + tableName + "')" +
                " ORDER BY c.column_id"
            )) {
                while (rs.next()) {
                    String col     = rs.getString("column_name");
                    int    width   = rs.getInt("width");
                    int    scale   = rs.getInt("scale");
                    String nulls   = rs.getString("nulls");
                    String defVal  = rs.getString("default_value");
                    String colType = rs.getString("column_type");
                    int    domId   = rs.getInt("domain_id");

                    String type = domainIdToType(domId);
                    String tl   = type.toLowerCase();

                    ddl.append("    ").append(col).append(" ").append(type);

                    if (tl.contains("char") || tl.contains("binary") || tl.equals("long varbit")) {
                        ddl.append("(").append(width).append(")");
                    } else if (tl.equals("decimal") || tl.equals("numeric")) {
                        ddl.append("(").append(width).append(", ").append(scale).append(")");
                    }

                    if ("I".equals(colType) || "A".equals(colType)) {
                        ddl.append(" DEFAULT AUTOINCREMENT");
                    } else if (defVal != null && !defVal.trim().isEmpty()) {
                        ddl.append(" DEFAULT ").append(defVal);
                    }

                    if ("N".equals(nulls)) ddl.append(" NOT NULL");
                    if (pkCols.contains(col)) ddl.append(" PRIMARY KEY");

                    ddl.append(",\n");
                }
            }

            int last = ddl.lastIndexOf(",");
            if (last != -1) ddl.deleteCharAt(last);
            ddl.append("\n);");

        } catch (Exception e) { e.printStackTrace(); }
        return ddl.toString();
    }

    private static String domainIdToType(int id) {
        switch (id) {
            case 1:  return "SMALLINT";
            case 2:  return "INTEGER";
            case 3:  return "NUMERIC";
            case 4:  return "FLOAT";
            case 5:  return "DOUBLE";
            case 6:  return "DATE";
            case 7:  return "VARCHAR";
            case 8:  return "CHAR";
            case 9:  return "LONG VARCHAR";
            case 10: return "BINARY";
            case 11: return "LONG BINARY";
            case 12: return "TIME";
            case 13: return "TIMESTAMP";
            case 14: return "TINYINT";
            case 15: return "BIGINT";
            case 16: return "UNSIGNED SMALLINT";
            case 17: return "UNSIGNED INT";
            case 18: return "UNSIGNED BIGINT";
            case 19: return "BIT";
            case 20: return "LONG VARBIT";
            case 21: return "VARBIT";
            case 22: return "DECIMAL";
            case 23: return "REAL";
            case 24: return "DATETIME";
            case 25: return "UNIQUEIDENTIFIER";
            case 26: return "UNIQUEIDENTIFIERSTR";
            case 27: return "UNSIGNED TINYINT";
            case 28: return "TEXT";
            case 29: return "IMAGE";
            case 30: return "MONEY";
            case 31: return "SMALLMONEY";
            case 32: return "SMALLDATETIME";
            default: return "INTEGER";
        }
    }

    public static String generateViewDDL(Connection conn, String viewName) {
        try (ResultSet rs = conn.createStatement().executeQuery(
            "SELECT view_def FROM SYSVIEWS WHERE view_name = '" + viewName + "'"
        )) {
            if (rs.next()) {
                String def = rs.getString("view_def");
                return "CREATE VIEW " + viewName + " AS\n" + (def != null ? def : "-- definition not available");
            }
        } catch (Exception e) {
            try (ResultSet rs2 = conn.createStatement().executeQuery(
                "SELECT v.view_def FROM SYSVIEW v " +
                "JOIN SYSTABLE t ON v.view_object_id = t.object_id " +
                "WHERE t.table_name = '" + viewName + "'"
            )) {
                if (rs2.next()) {
                    String def = rs2.getString("view_def");
                    return "CREATE VIEW " + viewName + " AS\n" + (def != null ? def : "-- definition not available");
                }
            } catch (Exception e2) { e2.printStackTrace(); }
        }
        return "-- DDL not available for view: " + viewName;
    }

    public static String generateProcedureDDL(Connection conn, String procName) {
        try (ResultSet rs = conn.createStatement().executeQuery(
            "SELECT proc_defn FROM SYSPROCEDURE WHERE proc_name = '" + procName + "'"
        )) {
            if (rs.next()) {
                String def = rs.getString("proc_defn");
                return def != null ? def : "-- definition not available for: " + procName;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return "-- DDL not available for procedure: " + procName;
    }

    public static String generateTriggerDDL(Connection conn, String triggerName) {
        try (ResultSet rs = conn.createStatement().executeQuery(
            "SELECT trigger_defn FROM SYSTRIGGER WHERE trigger_name = '" + triggerName + "'"
        )) {
            if (rs.next()) {
                String def = rs.getString("trigger_defn");
                return def != null ? def : "-- definition not available for: " + triggerName;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return "-- DDL not available for trigger: " + triggerName;
    }

    public static String generateIndexDDL(Connection conn, String indexName) {
        StringBuilder ddl = new StringBuilder();
        try (ResultSet rs = conn.createStatement().executeQuery(
            "SELECT i.index_name, i.\"unique\", i.index_type, t.table_name " +
            "FROM SYSINDEX i JOIN SYSTABLE t ON i.table_id = t.table_id " +
            "WHERE i.index_name = '" + indexName + "'"
        )) {
            if (rs.next()) {
                String unique = rs.getString("unique");
                String table  = rs.getString("table_name");
                ddl.append("CREATE ").append("Y".equals(unique) ? "UNIQUE " : "")
                   .append("INDEX ").append(indexName).append(" ON ").append(table).append(" (");

                try (ResultSet cols = conn.createStatement().executeQuery(
                    "SELECT c.column_name, ic.order " +
                    "FROM SYSIDXCOL ic " +
                    "JOIN SYSCOLUMN c ON ic.table_id = c.table_id AND ic.column_id = c.column_id " +
                    "WHERE ic.index_id = (SELECT index_id FROM SYSINDEX WHERE index_name = '" + indexName + "') " +
                    "ORDER BY ic.sequence"
                )) {
                    StringBuilder colList = new StringBuilder();
                    while (cols.next()) {
                        if (colList.length() > 0) colList.append(", ");
                        colList.append(cols.getString("column_name"));
                        String ord = cols.getString("order");
                        if ("D".equals(ord)) colList.append(" DESC");
                    }
                    ddl.append(colList).append(");");
                } catch (Exception e2) {
                    try (ResultSet cols2 = conn.createStatement().executeQuery(
                        "SELECT c.column_name FROM SYSIXCOL ix " +
                        "JOIN SYSCOLUMN c ON ix.table_id = c.table_id AND ix.column_id = c.column_id " +
                        "WHERE ix.index_id = (SELECT index_id FROM SYSINDEX WHERE index_name = '" + indexName + "')"
                    )) {
                        StringBuilder colList = new StringBuilder();
                        while (cols2.next()) {
                            if (colList.length() > 0) colList.append(", ");
                            colList.append(cols2.getString("column_name"));
                        }
                        ddl.append(colList).append(");");
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return ddl.length() > 0 ? ddl.toString() : "-- DDL not available for index: " + indexName;
    }
}
