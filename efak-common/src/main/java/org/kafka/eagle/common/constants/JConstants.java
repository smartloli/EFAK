/**
 * JConstants.java
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
package org.kafka.eagle.common.constants;

/**
 * Note that the JConstants class should only contain constants and should not be instantiated.
 * It serves as a utility class that can be imported and used directly, without the need for
 * instantiation.
 * @Author: smartloli
 * @Date: 2023/5/21 20:14
 * @Version: 3.4.0
 */
public final class JConstants {

    private JConstants() {

    }

    /**
     * Get databases.
     */
    public static final String SHOW_DATABASES = "SHOW DATABASES";

    /**
     * Create database script.
     */
    public static String CREATE_DB_SQL = "CREATE DATABASE IF NOT EXISTS %s";

}
