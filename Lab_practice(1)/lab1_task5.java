package comm.dbms.lab;

import java.sql.*;

public class lab1_task5{

    static String url = "jdbc:derby:mydb;create=true";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(url)) {

            System.out.println("Connection established.\n");

            createTable(conn);
            insertData(conn);
            displayData(conn);
            updateData(conn);
            displayData(conn);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    
    static void createTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE DEPARTMENT12 (" +
                     "did INTEGER PRIMARY KEY, " +
                     "dname VARCHAR(50) NOT NULL)";

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("Table DEPARTMENT created.");
        } catch (SQLException e) {
            if ("X0Y32".equals(e.getSQLState())) {
                System.out.println("Table already exists → OK.");
            } else {
                throw e;
            }
        }
    }

    
    static void insertData(Connection conn) throws SQLException {

        String sql = "INSERT INTO DEPARTMENT12 (did, dname) VALUES (?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String[] names = {"cs", "it", "etc", "eee", "mech"};

            for (int i = 0; i < names.length; i++) {
                pstmt.setInt(1, i + 1);
                pstmt.setString(2, names[i]);
                pstmt.executeUpdate();
            }

            System.out.println("Initial data inserted.");

        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                System.out.println("Some records already exist → Skipping insert.");
            } else {
                throw e;
            }
        }
    }


    static void updateData(Connection conn) throws SQLException {

        String updateSql = "UPDATE DEPARTMENT12 SET dname = ? WHERE did = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {

            String[] updatedNames = {"csit", "it", "etc", "eee"};

            for (int i = 0; i < updatedNames.length; i++) {
                pstmt.setString(1, updatedNames[i]);
                pstmt.setInt(2, i + 1);
                pstmt.executeUpdate();
            }

            System.out.println("Records updated successfully.");
        }
    }

    
    static void displayData(Connection conn) throws SQLException {

        String sql = "SELECT * FROM DEPARTMENT12 ORDER BY did";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n=== Final Department Table ===");

            while (rs.next()) {
                System.out.println("DID: " + rs.getInt("did") +
                                   " | DNAME: " + rs.getString("dname"));
            }
        }
    }
}


