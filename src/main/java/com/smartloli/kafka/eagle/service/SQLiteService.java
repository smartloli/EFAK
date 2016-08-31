package com.smartloli.kafka.eagle.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartloli.kafka.eagle.domain.OffsetsSQLiteDomain;
import com.smartloli.kafka.eagle.utils.SQLiteUtils;

/**
 * @Date Aug 18, 2016
 *
 * @Author smartloli
 *
 * @Email smartloli.org@gmail.com
 *
 * @Note TODO
 */
public class SQLiteService {

	private static Logger LOG = LoggerFactory.getLogger(SQLiteService.class);
	private static int MAX_COMMIT_SIZE = 1000;

	/**
	 * write stats data to sqlite with batch
	 * 
	 * @param list
	 * @param sql
	 */
	public static void insert(List<? extends OffsetsSQLiteDomain> list, String sql) {
		Connection connSQL = null;
		PreparedStatement sqlStatement = null;
		try {
			connSQL = SQLiteUtils.getInstance();
			// connSQL.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS offsets (groups string,topic string,created string,logsize long,offsets long,lag long)");
			connSQL.setAutoCommit(false);
			connSQL.setSavepoint();

			long nowTime = System.currentTimeMillis();
			sqlStatement = connSQL.prepareStatement(sql);
			int rowCount = 0;

			for (OffsetsSQLiteDomain p : list) {
				rowCount++;
				sqlStatement.setString(1, p.getGroup());
				sqlStatement.setString(2, p.getTopic());
				sqlStatement.setString(3, p.getCreated());
				sqlStatement.setLong(4, p.getLogSize());
				sqlStatement.setLong(5, p.getOffsets());
				sqlStatement.setLong(6, p.getLag());
				sqlStatement.addBatch();

				if (rowCount % MAX_COMMIT_SIZE == 0) {
					try {
						sqlStatement.executeBatch();
						connSQL.commit();
						LOG.info("[SQLiteService.insert.Batch] insert batch,rowCount=" + rowCount + "; commit spent time =" + (System.currentTimeMillis() - nowTime) / 1000.0 + "s");
						LOG.info("[SQLiteService.insert.Batch] Commit to sqlite db has successed.");
					} catch (Exception e) {
						connSQL.rollback();
						LOG.error("[SQLiteService.insert] insert batch error is " + e.getMessage());
					}
				}
			}

			if (rowCount % MAX_COMMIT_SIZE != 0) {
				try {
					sqlStatement.executeBatch();
					connSQL.commit();
					LOG.info("[SQLiteService.insert.Batch] insert batch,rowCount=" + rowCount + "; commit spent time =" + (System.currentTimeMillis() - nowTime) / 1000.0 + "s");
					LOG.info("[SQLiteService.insert.Batch] insert to sqlite db has successed.");
				} catch (Exception e) {
					connSQL.rollback();
					LOG.error("[SQLiteService.insert] insert batch error = " + e.getMessage());
				}
			}

		} catch (Exception ex) {
			LOG.error(ex.getMessage());
		} finally {
			try {
				sqlStatement.close();
			} catch (SQLException e) {
				LOG.error("[SQLiteService.insert.Batch] Close (sqlStatement) has error,msg is " + e.getMessage());
			}
			SQLiteUtils.close(connSQL);
		}
	}

	public static void update(String sql) {
		Connection connSQL = null;
		PreparedStatement sqlStatement = null;
		try {
			connSQL = SQLiteUtils.getInstance();
			// connSQL.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS offsets (groups string,topic string,created string,logsize long,offsets long,lag long)");
			sqlStatement = connSQL.prepareStatement(sql);
			sqlStatement.executeUpdate();
		} catch (Exception ex) {
			LOG.error("[SQLiteService.update] " + ex.getMessage());
		} finally {
			try {
				sqlStatement.close();
			} catch (SQLException e) {
				LOG.error("[SQLiteService.update] Close (sqlStatement) has error,msg is " + e.getMessage());
			}
			SQLiteUtils.close(connSQL);
		}
	}

	public static void insert(OffsetsSQLiteDomain offsets, String sql) {
		Connection connSQL = null;
		PreparedStatement sqlStatement = null;
		try {
			connSQL = SQLiteUtils.getInstance();
			// connSQL.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS offsets (groups string,topic string,created string,logsize long,offsets long,lag long)");
			sqlStatement = connSQL.prepareStatement(sql);
			sqlStatement.setString(1, offsets.getGroup());
			sqlStatement.setString(2, offsets.getTopic());
			sqlStatement.setString(3, offsets.getCreated());
			sqlStatement.setLong(4, offsets.getLogSize());
			sqlStatement.setLong(5, offsets.getOffsets());
			sqlStatement.setLong(6, offsets.getLag());
			sqlStatement.executeUpdate();
		} catch (Exception ex) {
			LOG.error(ex.getMessage());
		} finally {
			try {
				sqlStatement.close();
			} catch (SQLException e) {
				e.printStackTrace();
				LOG.error("[SQLiteService.insert] Close (sqlStatement) has error,msg is " + e.getMessage());
			}
			SQLiteUtils.close(connSQL);
		}
	}

	public static List<OffsetsSQLiteDomain> query(String sql) {
		List<OffsetsSQLiteDomain> list = new ArrayList<OffsetsSQLiteDomain>();
		Connection connSQL = null;
		ResultSet rs = null;
		try {
			connSQL = SQLiteUtils.getInstance();
			connSQL.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS offsets (groups string,topic string,created string,logsize long,offsets long,lag long)");
			rs = connSQL.createStatement().executeQuery(sql);
			while (rs.next()) {
				OffsetsSQLiteDomain offsetSQLite = new OffsetsSQLiteDomain();
				offsetSQLite.setCreated(rs.getString("created"));
				offsetSQLite.setLag(rs.getLong("lag"));
				offsetSQLite.setLogSize(rs.getLong("logsize"));
				offsetSQLite.setOffsets(rs.getLong("offsets"));
				offsetSQLite.setTopic(rs.getString("topic"));
				offsetSQLite.setGroup(rs.getString("groups"));
				list.add(offsetSQLite);
			}
		} catch (Exception ex) {
			LOG.error(ex.getMessage());
		} finally {
			try {
				rs.close();
			} catch (SQLException e) {
				LOG.error("[SQLiteService.query] Close (ResultSet) has error,msg is " + e.getMessage());
			}
			SQLiteUtils.close(connSQL);
		}
		return list;
	}

}
