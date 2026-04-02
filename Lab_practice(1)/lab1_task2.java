// Data Insertion 

package comm.dbms.lab;
import java.sql.*;
public class lab1_task2 {
	public static void main(String []abc) throws SQLException
	{
		String url = "jdbc:derby:mydb;create=true";   // creates folder 'mydb' if not exists

        Connection conn = DriverManager.getConnection(url);
        insertData(conn);
            
	}
	
	
	static void insertData(Connection conn) throws SQLException {
	    String sql = "INSERT INTO DEPARTMENT (did, dname) VALUES (?, ?)";

	    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        // Row 1
	        pstmt.setInt(1, 1);
	        pstmt.setString(2, "Computer Science");
	        pstmt.executeUpdate();

	        // Row 2
	        pstmt.setInt(1, 2);
	        pstmt.setString(2, "Mathematics");
	        pstmt.executeUpdate();
	        
	        // Row 3
	        pstmt.setInt(1, 3);
	        pstmt.setString(3, "CS IT");
	        pstmt.executeUpdate();     //This row is skipped bcz some data is already inserted to modify it need to update the table. 
	        

	        System.out.println("3 departments inserted successfully.");
	    
	    } catch (SQLException e) {
	        if ("23505".equals(e.getSQLState())) {   // duplicate primary key
	        	   System.out.println("Some records already exist → skipping.");
	        } else {
	            throw e;
	        }
	    }
	
	}
}

