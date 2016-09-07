package com.smartloli.kafka.eagle.test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.smartloli.kafka.eagle.utils.SQLiteUtils;

/**
 * @Date Sep 6, 2016
 *
 * @Author smartloli
 *
 * @Email smartloli.org@gmail.com
 *
 * @Note TODO
 */
public class SQLiteDBTest {
	
	public static void main(String[] args) {
		Connection conn = SQLiteUtils.getInstance();
		try {
			Statement state = conn.createStatement();
			ResultSet rs = state.executeQuery("select count(*) from offsets");
			while(rs.next()){
				System.out.println(rs.getString(1));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
