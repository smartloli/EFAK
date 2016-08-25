package com.smartloli.kafka.eagle.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartloli.kafka.eagle.domain.OffsetsSQLiteDomain;
import com.smartloli.kafka.eagle.utils.SQLitePoolUtils;

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
		try {
			connSQL = SQLitePoolUtils.getSQLiteConn();
			connSQL.setAutoCommit(false);
			connSQL.setSavepoint();

			long nowTime = System.currentTimeMillis();
			PreparedStatement sqlStatement = connSQL.prepareStatement(sql);
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
						LOG.info("[SQLiteService.replace] replace batch,rowCount=" + rowCount + "; commit spent time =" + (System.currentTimeMillis() - nowTime) / 1000.0 + "s");
						LOG.info("[SQLiteService.replace] Commit to sqlite db has successed.");
					} catch (Exception e) {
						connSQL.rollback();
						LOG.error("[SQLiteService.replace] replace batch error is " + e.getMessage());
					}
				}
			}

			if (rowCount % MAX_COMMIT_SIZE != 0) {
				try {
					sqlStatement.executeBatch();
					connSQL.commit();
					LOG.info("[SQLiteService.replace] replace batch,rowCount=" + rowCount + "; commit spent time =" + (System.currentTimeMillis() - nowTime) / 1000.0 + "s");
					LOG.info("[SQLiteService.replace] replace to sqlite db has successed.");
				} catch (Exception e) {
					connSQL.rollback();
					LOG.error("[SQLiteService.replace] replace batch error = " + e.getMessage());
				}
			}

		} catch (Exception ex) {
			LOG.error(ex.getMessage());
		} finally {
			if (connSQL != null) {
				SQLitePoolUtils.release(connSQL);
			}
		}
	}

	public static void update(String sql) {
		Connection connSQL = null;
		try {
			connSQL = SQLitePoolUtils.getSQLiteConn();
			PreparedStatement sqlStatement = connSQL.prepareStatement(sql);
			sqlStatement.executeUpdate();
		} catch (Exception ex) {
			LOG.error(ex.getMessage());
		} finally {
			if (connSQL != null) {
				SQLitePoolUtils.release(connSQL);
			}
		}
	}

	public static void insert(OffsetsSQLiteDomain offsets, String sql) {
		Connection connSQL = null;
		try {
			connSQL = SQLitePoolUtils.getSQLiteConn();
			PreparedStatement sqlStatement = connSQL.prepareStatement(sql);
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
			if (connSQL != null) {
				SQLitePoolUtils.release(connSQL);
			}
		}
	}

	public static List<OffsetsSQLiteDomain> query(String sql) {
		List<OffsetsSQLiteDomain> list = new ArrayList<OffsetsSQLiteDomain>();
		Connection connSQL = null;
		try {
			connSQL = SQLitePoolUtils.getSQLiteConn();
			ResultSet rs = connSQL.createStatement().executeQuery(sql);
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
			if (connSQL != null) {
				SQLitePoolUtils.release(connSQL);
			}
		}
		return list;
	}

	public static void main(String[] args) {
		System.out.println(query("select * from offsets where topic='test_data4' and created between '2016-08-25 17:00:00' and '2016-08-25 17:50:55'"));
		// update("delete from offsets where created='2016-08-23 16:50'");
//		 testInsert();
	}

	public static void testInsert() {
		List<OffsetsSQLiteDomain> list = new ArrayList<OffsetsSQLiteDomain>();
		OffsetsSQLiteDomain offset = new OffsetsSQLiteDomain();
		offset.setGroup("group1");
		offset.setCreated("2016-08-25 17:00");
		offset.setLag(70);
		offset.setLogSize(1625);
		offset.setOffsets(70);
		offset.setTopic("test_data4");
		list.add(offset);
		String sql = "INSERT INTO offsets values(?,?,?,?,?,?)";
		insert(list, sql);
	}

}
