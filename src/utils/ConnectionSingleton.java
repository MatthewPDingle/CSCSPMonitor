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
			config.setMinConnectionsPerPartition(5);
			config.setMaxConnectionsPerPartition(30);
			config.setPartitionCount(1);
			if (boneCP == null) {
				boneCP = new BoneCP(config);
			}
		}
		catch (Exception e) {
			System.err.println(e.getMessage());
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