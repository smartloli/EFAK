package com.smartloli.kafka.eagle.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @Date Aug 18, 2016
 *
 * @Author smartloli
 *
 * @Email smartloli.org@gmail.com
 *
 * @Note Store offsets daily Kafka and lag details
 */
public class SQLiteUtils {

	public static void main(String[] args) throws ClassNotFoundException {
		Class.forName("org.sqlite.JDBC");

		Connection connection = null;
		try {
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:/Users/dengjie/hadoop/workspace/kafka-eagle/src/main/resources/ke.db");
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			statement.executeUpdate("drop table if exists offsets");
			statement.executeUpdate("create table offsets (id integer, topic string,offsets long,lag long,)");
			statement.executeUpdate("insert into offsets values(1, 'leo')");
			statement.executeUpdate("insert into offsets values(2, 'yui')");
			ResultSet rs = statement.executeQuery("select * from person");
			while (rs.next()) {
				// read the result set
				System.out.println("name = " + rs.getString("name"));
				System.out.println("id = " + rs.getInt("id"));
			}
		} catch (SQLException e) {
			// if the error message is "out of memory",
			// it probably means no database file is found
			System.err.println(e.getMessage());
		} finally {
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				// connection close failed.
				System.err.println(e);
			}
		}
	}

}
