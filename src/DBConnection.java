import java.sql.*;
public class DBConnection {
 static final String URL="jdbc:mysql://localhost:3306/hospital_management?useSSL=false&serverTimezone=UTC";
 static final String USER="root";
 static final String PASSWORD="YOUR_MYSQL_PASSWORD";
 public static Connection getConnection() throws SQLException { return DriverManager.getConnection(URL,USER,PASSWORD); }
}
