package utils;

import java.sql.Connection;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;

import constants.Constants;

public class ConnectionSingleton {

	private static ConnectionSingleton instance = null;
	
	private static BoneCP boneCP = null;
	
	public static ConnectionSingleton getInstance() {
		if (instance == null) {
			instance = new ConnectionSingleton();
		}
		return instance;
	}
	
	protected ConnectionSingleton() {
		try {
			BoneCPConfig config = new BoneCPConfig();
			config.setJdbcUrl(Constants.URL);
			config.setUsername(Constants.USERNAME);
			config.setPassword(Constants.PASSWORD);
			config.setMinConnectionsPerPartition(8);
			config.setMaxConnectionsPerPartition(16);
			config.setPartitionCount(1);
			boneCP = new BoneCP(config);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Connection getConnection() {
		try {
			Connection conn = boneCP.getConnection();
			return conn;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}