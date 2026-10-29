package provider;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import reflection.utility.Provides;
//import com.mysql.jdbc.*;
public class H2ConnectionProvider {
	@Provides
	public Connection buildConnection() throws SQLException {
		Connection connection = DriverManager.getConnection("jdbc:h2:D:\\Work\\Projects\\eclipse-workspace\\ImageBrowser\\db_files\\db-pluralsight", "sa", "");
		return connection;
	}
	

}
