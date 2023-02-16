/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.smartloli.kafka.eagle.plugin.mysql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartloli.kafka.eagle.common.util.SystemConfigUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * The MySql storage plugin provides access methods and closing methods for
 * accessing MySql database.
 *
 * @author smartloli.
 * <p>
 * Created by Nov 23, 2016
 * <p>
 * Update by smartloli Sep 12, 2021
 * Settings prefixed with 'kafka.eagle.' will be deprecated, use 'efak.' instead.
 */
public class MySqlStoragePlugin {

    private final static Logger LOG = LoggerFactory.getLogger(MySqlStoragePlugin.class);

    static {
        try {
            String mysqlDriver = SystemConfigUtils.getProperty("efak.driver");
            Class.forName(mysqlDriver);
        } catch (Exception e) {
            LOG.error("Initialization MySql Driver has error,msg is " + e.getMessage());
        }
    }

    /**
     * Get mysql connection object.
     */
    public static Connection getInstance(String address, String username, String password) {
        Connection connection = null;
        try {
            connection = (Connection) DriverManager.getConnection(address, username, password);
        } catch (Exception e) {
            LOG.error("Create mysql connection has error address[" + address + "],username[" + username + "],password[" + password + "],msg is " + e.getMessage());
        }
        return connection;
    }

    /**
     * Close mysql.
     */
    public static void close(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (SQLException e) {
                LOG.error("Close connection has error,msg is " + e.getMessage());
            }
        }
    }

}
