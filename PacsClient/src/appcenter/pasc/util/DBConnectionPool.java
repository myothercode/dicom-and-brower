package appcenter.pasc.util;

import appcenter.logHelper.LogForError;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * @author mark.yang
 *         13-5-17
 *         pasc
 */
public class DBConnectionPool {

	// 1. 创建核心连接池对象
	private static ArrayList<Connection> pool = new ArrayList<Connection>();

	// 连接参数
	private static String driverName = "";
	private static String url = "";
	private static String userName = "";
	private static String password = "";
	private static int max = 20;
	private static int min = 5;

	public DBConnectionPool() {
		init();
	}

	static {
		userName = BaseConfig.userName;
		password = BaseConfig.passWord;
		max = 15;
		min = 5;
		if(BaseConfig.dbType.equalsIgnoreCase("sqlServer") && BaseConfig.dbConnectType.equals("jdbc")) {
			driverName = "com.microsoft.jdbc.sqlserver.SQLServerDriver";
			url = "jdbc:microsoft:sqlserver://" + BaseConfig.dbUrl + ";DatabaseName=" + BaseConfig.dbName;
		} else if(BaseConfig.dbType.equalsIgnoreCase("sqlServer") && BaseConfig.dbConnectType.equals("odbc")) {
			driverName = "sun.jdbc.odbc.JdbcOdbcDriver";
			url = "jdbc:odbc:" + BaseConfig.dbName;
		}
		try {
			Class.forName(driverName);
		} catch(ClassNotFoundException e) {
			LogForError.logError(e);
		}
	}

	// 2. 创建连接功能:创建一个Connection对象，放入pool中
	private static void createConnection() {
		try {
			Connection conn = DriverManager.getConnection(url, userName, password);
			pool.add(conn);
		} catch(SQLException e) {
			LogForError.logError(e);
		}
	}

	// 3. 维持连接数量(让连接维持在最小连接数到最大连接数之间，如果低于最小连接数则创建连接)
	private static void keepCount() {
		int size = pool.size();

		while(size <= min) {
			if(pool.size() == max) {
				break;
			}
			createConnection();
		}

	}

	// 4. 对外提供连接获取方法
	public static Connection getConnection() {
		// 1. 维持连接数量
		keepCount();
		// 2. 对外提供连接(把提供出去的连接从列表中删除)
		return pool.remove(pool.size() - 1);
	}

	// 5. 给用户销毁连接的方法(归还连接 1. 如果池子连接数已经满了，则销毁连接 2. 如未满则归还连接)
	public static void close(Connection conn) {
		// 连接池未满
		if(pool.size() < max) {
			pool.add(conn);
		} else {
			try {
				conn.close();
			} catch(SQLException e) {
				LogForError.logError(e);
			}
		}
	}

	// 6. 初始化连接池(把连接池创建满)
	public static void init() {
		keepCount();
	}

	// 7. 销毁连接池(把池中每个连接销毁)
	public static void destroy() {
		for(Connection conn : pool) {
			try {
				conn.close();
			} catch(SQLException e) {
				LogForError.logError(e);
			}
		}
	}
}
