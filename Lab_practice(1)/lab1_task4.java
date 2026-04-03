// Update Table

package comm.dbms.lab;
import java.sql.*;

public class lab1_task4 {

	public static void main(String []abc) throws SQLException
	{
		String url = "jdbc:derby:mydb;create=true";   // creates folder 'mydb' if not exists

        Connection conn = DriverManager.getConnection(url);
        updateAndDelete(conn);
            
	}
	
	static void updateAndDelete(Connection conn) throws SQLException {
	    // Update
	    String updateSql = "UPDATE DEPARTMENT SET dname = ? WHERE did = ?";
	    try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
	        pstmt.setString(1, "CS & IT");
	        pstmt.setInt(2, 1);
	        int count = pstmt.executeUpdate();
	        System.out.println(count + " row(s) updated.");
	    }

	    // Delete
	    String deleteSql = "DELETE FROM DEPARTMENT WHERE did = 2";
	    try (Statement stmt = conn.createStatement()) {
	        int count = stmt.executeUpdate(deleteSql);
	        System.out.println(count + " row(s) deleted.");
	    }
	}
	
}	


