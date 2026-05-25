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
        try {
            java.util.Map<Integer, String> domainMap = new java.util.HashMap<>();
            try (ResultSet dm = conn.createStatement().executeQuery(
                "SELECT domain_id, domain_name FROM SYSDOMAIN"
            )) { while (dm.next()) domainMap.put(dm.getInt("domain_id"), dm.getString("domain_name")); }
            catch (Exception ignored) {}
            java.util.Set<String> pkCols = new java.util.HashSet<>();
            try (ResultSet pk = conn.createStatement().executeQuery(
                "SELECT column_name FROM SYSCOLUMN " +
                "WHERE pkey = 'Y' " +
                "AND table_id = (SELECT table_id FROM SYSTABLE WHERE table_name = '" + tableName + "')"
            )) { while (pk.next()) pkCols.add(pk.getString("column_name")); }
            catch (Exception ignored) {}
            try (ResultSet rs = conn.createStatement().executeQuery(
                "SELECT column_name, domain_id, width, scale, nulls, \"default\", column_type " +
                "FROM SYSCOLUMN " +
                "WHERE table_id = (SELECT table_id FROM SYSTABLE WHERE table_name = '" + tableName + "')" +
                " ORDER BY column_id"
            )) {
                while (rs.next()) {
                    String col     = rs.getString("column_name");
                    int    domId   = rs.getInt("domain_id");
                    int    width   = rs.getInt("width");
                    int    scale   = rs.getInt("scale");
                    String nulls   = rs.getString("nulls");
                    String defVal  = rs.getString("default");
                    String colType = rs.getString("column_type");

                    String type = domainMap.getOrDefault(domId, "INTEGER");
                    String tl   = type.toLowerCase();

                    ddl.append("    ").append(col).append(" ").append(type.toUpperCase());

                    if (tl.contains("char") || tl.contains("varbit") || tl.contains("binary")) {
                        ddl.append("(").append(width).append(")");
                    } else if (tl.equals("decimal") || tl.equals("numeric")) {
                        ddl.append("(").append(width).append(", ").append(scale).append(")");
                    }

                    if ("I".equals(colType) || "A".equals(colType)) {
                        ddl.append(" DEFAULT AUTOINCREMENT");
                    } else if (defVal != null && !defVal.trim().isEmpty()) {
                        ddl.append(" DEFAULT ").append(defVal.trim());
                    }

                    if ("N".equals(nulls)) ddl.append(" NOT NULL");
                    if (pkCols.contains(col)) ddl.append(" PRIMARY KEY");

                    ddl.append(",\n");
                }
            } catch (Exception e3) { e3.printStackTrace(); }

            int last = ddl.lastIndexOf(",");
            if (last != -1) ddl.deleteCharAt(last);
            ddl.append("\n);");

        } catch (Exception e) { e.printStackTrace(); }
        return ddl.toString();
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
            "SELECT i.index_name, i.\"unique\", t.table_name " +
            "FROM SYSINDEX i JOIN SYSTABLE t ON i.table_id = t.table_id " +
            "WHERE i.index_name = '" + indexName + "'"
        )) {
            if (rs.next()) {
                String unique = rs.getString("unique");
                String table  = rs.getString("table_name");
                ddl.append("CREATE ").append("Y".equals(unique) ? "UNIQUE " : "")
                   .append("INDEX ").append(indexName).append(" ON ").append(table).append(" (");
                try (ResultSet cols = conn.createStatement().executeQuery(
                    "SELECT c.column_name FROM SYSIXCOL ix " +
                    "JOIN SYSCOLUMN c ON ix.table_id = c.table_id AND ix.column_id = c.column_id " +
                    "WHERE ix.index_id = (SELECT index_id FROM SYSINDEX WHERE index_name = '" + indexName + "')"
                )) {
                    StringBuilder colList = new StringBuilder();
                    while (cols.next()) {
                        if (colList.length() > 0) colList.append(", ");
                        colList.append(cols.getString("column_name"));
                    }
                    ddl.append(colList).append(");");
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return ddl.length() > 0 ? ddl.toString() : "-- DDL not available for index: " + indexName;
    }
}