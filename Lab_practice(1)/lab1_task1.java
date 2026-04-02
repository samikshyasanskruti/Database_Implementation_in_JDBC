// Table Creation

package comm.dbms.lab;
import java.sql.*;

public class lab1_task1 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String url="jdbc:derby:mydb;create=True";
		try(Connection conn=DriverManager.getConnection(url))
		{
			System.out.println("connection established");
			Statement stmt=conn.createStatement();
			String sql = "CREATE TABLE DEPARTMENT (did INTEGER PRIMARY KEY,dname VARCHAR(50) NOT NULL)";
			stmt.executeUpdate(sql);
			System.out.println("Table DEPARTMENT Crteated");
		}
		catch(SQLException e)
		{
			if("XOY32".equals(e.getSQLState()))
			{
				System.out.println("Table DEPARTMENT already exists -> ok.");
			}
			else
			{
				e.printStackTrace();
			}
		}

	}

}