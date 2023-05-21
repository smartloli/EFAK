/**
 * MySqlRecordSchema.java
 * <p>
 * Copyright 2023 smartloli
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kafka.eagle.plugins.mysql;

import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.common.constants.JConstants;
import org.kafka.eagle.pojo.mysql.MySQLDataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * The schema can be used in conjunction with the data access layer to map Java objects to database records,
 * allowing for seamless CRUD (Create, Read, Update, Delete) operations on the corresponding database table.
 *
 * @Author: smartloli
 * @Date: 2023/5/21 15:53
 * @Version: 3.4.0
 */
@Slf4j
public class MySqlRecordSchema {
    /**
     * Load database schema script.
     */
    public static void schema(MySQLDataSource mySQLDataSource) {

        String url = mySQLDataSource.getDbUrl();
        String host = url.split("//")[1].split("/")[0].split(":")[0];
        String port = url.split("//")[1].split("/")[0].split(":")[1];
        String[] urls = url.split("//");
        String db = urls[1].split("/")[1].split("\\?")[0];
        String subUrl = urls[0];
        mySQLDataSource.setDbName(db);
        StringBuilder sb = new StringBuilder();
        sb.append(subUrl).append("//").append(host).append(":").append(port);
        mySQLDataSource.setDbUrl(sb.toString());

        if (database(mySQLDataSource)) {
            // tables(username, password, host, port, db);
        }
    }

    private static boolean database(MySQLDataSource mySQLDataSource) {
        Connection connection = MySqlStoragePlugin.getInstance(mySQLDataSource);
        ResultSet rs = null;
        List<String> dbs = new ArrayList<>();
        try {
            rs = connection.createStatement().executeQuery(JConstants.SHOW_DATABASES);
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    dbs.add(rs.getString(i));
                }
            }
            if (dbs.contains(mySQLDataSource.getDbName())) {
                log.info("The [" + mySQLDataSource.getDbName() + "] database already exists. Do not need to create it.");
            } else {
                boolean status = connection.createStatement().execute(String.format(JConstants.CREATE_DB_SQL, mySQLDataSource.getDbName()));
                if (!status) {
                    log.info("SQL statement affect the 0 records. Create [" + mySQLDataSource.getDbName() + "] has successed.");
                } else {
                    log.error("Create [" + mySQLDataSource.getDbName() + "] has failed.");
                }
            }
            return true;
        } catch (SQLException e) {
            log.error("Create database[" + mySQLDataSource.getDbName() + "] has error, msg is: {}", e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                MySqlStoragePlugin.close(connection);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
