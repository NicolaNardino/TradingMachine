package com.projects.tradingMachine.utility.database;

import org.apache.commons.dbcp2.BasicDataSource;

public final class PooledDataSourceBuilder {

	/**
	 * Creates a database connection pool. 
	 * 
	 * @param dbProperties
	 * @param poolSize
	 * @return
	 */
	public static BasicDataSource getDataSource(final DatabaseProperties dbProperties, final int poolSize) {
		final BasicDataSource ds = new BasicDataSource();
		ds.setUrl(DbUtility.getMySqlConnectionUrl(dbProperties));
		ds.setUsername(dbProperties.getUserName());
		ds.setPassword(dbProperties.getPassword());
		ds.setInitialSize(poolSize);
		ds.setMaxTotal(poolSize);
		return ds;
	}
}
