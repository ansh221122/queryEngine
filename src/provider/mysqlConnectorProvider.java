package provider;

import java.sql.Connection;
import java.sql.DriverManager;

import objectPoolPattern.connectionpoolPrototype.ConnectionPoolWrapper;
import objectPoolPattern.pool.ObjectPool;
import reflection.utility.Provides;

public class mysqlConnectorProvider {

//	@Provides
//	public ObjectPool<Connection> buildConnectionMysqlObjectPool() throws Exception {
//		//Class.forName("com.mysql.jdbc.Driver");
//		ConnectionPoolWrapper wrapper = new ConnectionPoolWrapper("jdbc:mysql://localhost:3306/reflection", "root", "", 20);
//		
//		return wrapper.getPool();
//	}
	
	@Provides
	public Connection buildConnectionMysql() throws Exception {
		Class.forName("com.mysql.jdbc.Driver");
		//ConnectionPoolWrapper wrapper = new ConnectionPoolWrapper("jdbc:mysql://localhost:3306/reflection", "root", "", 20);
		
		return DriverManager.getConnection("jdbc:mysql://localhost:3306/reflection",  "root", "");
	}
}
