// Verify the Table

package comm.dbms.lab;
import java.sql.*;
public class lab1_task3 {


	public static void main(String []abc) throws SQLException
	{
		String url = "jdbc:derby:mydb;create=true";   // creates folder 'mydb' if not exists

        Connection conn = DriverManager.getConnection(url);
        printAllDepartments(conn);
            
	}
	
	static void printAllDepartments(Connection conn) throws SQLException {
	    String sql = "SELECT did, dname FROM DEPARTMENT ORDER BY did";

	    try (Statement stmt = conn.createStatement();
	         ResultSet rs = stmt.executeQuery(sql)) {

	        System.out.println("\n=== All Departments ===");
	        boolean hasData = false;

	        while (rs.next()) {
	            hasData = true;
	            int id = rs.getInt("did");
	            String name = rs.getString("dname");
	            System.out.println("ID: " + id + " → " + name);
	        }

	        if (!hasData) {
	            System.out.println("(No records found)");
	        }
	    }
	}

}