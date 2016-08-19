package com.smartloli.kafka.eagle.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
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

	// write to sqlite with batch
	public static void insert(List<? extends OffsetsSQLiteDomain> list, String sql) {
		try {
			Connection connSQL = SQLitePoolUtils.getSQLiteConn();
			connSQL.setAutoCommit(false);
			connSQL.setSavepoint();

			long nowTime = System.currentTimeMillis();
			PreparedStatement sqlStatement = connSQL.prepareStatement(sql);
			int rowCount = 0;

			for (OffsetsSQLiteDomain p : list) {
				rowCount++;
				sqlStatement.setString(1, p.getTopic());
				sqlStatement.setString(2, p.getCreated());
				sqlStatement.setLong(3, p.getLogSize());
				sqlStatement.setLong(4, p.getOffsets());
				sqlStatement.setLong(5, p.getLag());
				sqlStatement.setLong(6, p.getProducer());
				sqlStatement.setLong(7, p.getConsumer());
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
				} finally {
					try {
						// sqlStatement.close();
						// connSQL.close();
						SQLitePoolUtils.release();
					} catch (Exception ex) {
						LOG.error("[SQLiteService.replace] Release SQLite has error, msg is " + ex.getMessage());
					}
				}
			}

		} catch (Exception ex) {
			LOG.error(ex.getMessage());
		}
	}

	public static void insert(OffsetsSQLiteDomain offsets, String sql) {
		try {
			Connection connSQL = SQLitePoolUtils.getSQLiteConn();
			PreparedStatement sqlStatement = connSQL.prepareStatement(sql);
			sqlStatement.setString(1, offsets.getTopic());
			sqlStatement.setString(2, offsets.getCreated());
			sqlStatement.setLong(3, offsets.getLogSize());
			sqlStatement.setLong(4, offsets.getOffsets());
			sqlStatement.setLong(5, offsets.getLag());
			sqlStatement.setLong(6, offsets.getProducer());
			sqlStatement.setLong(7, offsets.getConsumer());
			sqlStatement.executeUpdate();
		} catch (Exception ex) {
			LOG.error(ex.getMessage());
		} finally {
			try {
				SQLitePoolUtils.release();
			} catch (Exception ex) {
				LOG.error("[SQLiteService.replace] Release SQLite has error, msg is " + ex.getMessage());
			}
		}
	}

	public static void query(String sql) {
		try {
			Connection connSQL = SQLitePoolUtils.getSQLiteConn();
			ResultSet rs = connSQL.createStatement().executeQuery(sql);
			while (rs.next()) {
				System.out.println(rs.getString("topic"));
				System.out.println(rs.getString("created"));
				System.out.println(rs.getString("logsize"));
				System.out.println(rs.getString("offsets"));
				System.out.println(rs.getString("lag"));
				System.out.println(rs.getString("producer"));
				System.out.println(rs.getString("consumer"));
				return;
			}
		} catch (Exception ex) {
			LOG.error(ex.getMessage());
		}
	}

	public static void main(String[] args) {
		 query("select * from offsets");
		//testInsert();
	}

	private static void testInsert() {
		List<OffsetsSQLiteDomain> list = new ArrayList<OffsetsSQLiteDomain>();
		for (int i = 0; i < 28800; i++) {
			OffsetsSQLiteDomain offset = new OffsetsSQLiteDomain();
			offset.setConsumer(i);
			offset.setCreated(new Date().toString());
			offset.setLag(i);
			offset.setLogSize(i);
			offset.setOffsets(i);
			offset.setProducer(i);
			offset.setTopic("test" + i);
			list.add(offset);
		}
		String sql = "INSERT INTO offsets values(?,?,?,?,?,?,?)";
		insert(list, sql);
	}

}
