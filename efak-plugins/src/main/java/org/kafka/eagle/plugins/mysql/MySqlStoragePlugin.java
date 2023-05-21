/**
 * MySqlStoragePlugin.java
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
import org.kafka.eagle.pojo.mysql.MySQLDataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * To use the plugin, instantiate this class with the required configuration parameters,
 * and then register it with the storage system or use it directly to perform database
 * operations.
 *
 * @Author: smartloli
 * @Date: 2023/5/21 15:33
 * @Version: 3.4.0
 */
@Slf4j
public class MySqlStoragePlugin {


    /**
     * Get mysql connection object.
     */
    public static Connection getInstance(MySQLDataSource mySQLDataSource) {
        Connection connection = null;
        try {
            Class.forName(mySQLDataSource.getDbDriverName());
            connection = (Connection) DriverManager.getConnection(mySQLDataSource.getDbUrl(), mySQLDataSource.getDbUserName(), mySQLDataSource.getDbPassword());
        } catch (Exception e) {
            log.error("Create mysql connection has error, MySQL address {}, Error message is: {}", mySQLDataSource, e);
        }
        return connection;
    }

    /**
     * Close mysql.
     */
    public static void close(Connection connection) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            log.error("Close connection has error,msg is: {}", e);
        }
    }

}
