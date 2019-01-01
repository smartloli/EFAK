/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.smartloli.kafka.eagle.common.util;

import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide the single entry point for accessing configuration properties.
 * 
 * @author smartloli.
 * 
 *         Created by May 25, 2015
 */
public final class SystemConfigUtils {
	private static Properties mConfig;

	private static final Logger LOG = LoggerFactory.getLogger(SystemConfigUtils.class);
	static {
		mConfig = new Properties();
		getReources("system-config.properties");
	}

	/** Load profiles from different operate systems. */
	private static void getReources(String name) {
		try {
			try {
				String osName = System.getProperties().getProperty("os.name");
				if (osName.contains("Mac") || osName.contains("Win")) {
					mConfig.load(SystemConfigUtils.class.getClassLoader().getResourceAsStream(name));
				} else {
					mConfig.load(new FileInputStream(System.getProperty("user.dir") + "/conf/" + name));
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			LOG.info("Successfully loaded default properties.");

			if (LOG.isDebugEnabled()) {
				LOG.debug("SystemConfig looks like this ...");

				String key = null;
				Enumeration<Object> keys = mConfig.keys();
				while (keys.hasMoreElements()) {
					key = (String) keys.nextElement();
					LOG.debug(key + "=" + mConfig.getProperty(key));
				}
			}
		} catch (Exception e) {
			LOG.error("Load system name has error,msg is " + e.getMessage());
		}
	}

	/**
	 * Retrieve a property as a boolean ... defaults to false if not present.
	 */
	public static boolean getBooleanProperty(String name) {
		return getBooleanProperty(name, false);
	}

	/**
	 * Retrieve a property as a boolean with specified default if not present.
	 */
	public static boolean getBooleanProperty(String name, boolean defaultValue) {
		String value = SystemConfigUtils.getProperty(name);
		if (value == null) {
			return defaultValue;
		}
		return Boolean.valueOf(value).booleanValue();
	}

	/** Retrieve a property as a boolean array. */
	public static boolean[] getBooleanPropertyArray(String name, boolean[] defaultValue, String splitStr) {
		String value = SystemConfigUtils.getProperty(name);
		if (value == null) {
			return defaultValue;
		}
		try {
			String[] propertyArray = value.split(splitStr);
			boolean[] result = new boolean[propertyArray.length];
			for (int i = 0; i < propertyArray.length; i++) {
				result[i] = Boolean.valueOf(propertyArray[i]).booleanValue();
			}
			return result;
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return defaultValue;
		}
	}

	/** Retrieve a property as a int,defaults to 0 if not present. */
	public static int getIntProperty(String name) {
		return getIntProperty(name, 0);
	}

	/** Retrieve a property as a int. */
	public static int getIntProperty(String name, int defaultValue) {
		String value = SystemConfigUtils.getProperty(name);
		if (value == null) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return defaultValue;
		}
	}

	/** Retrieve a property as a int array. */
	public static int[] getIntPropertyArray(String name, int[] defaultValue, String splitStr) {
		String value = SystemConfigUtils.getProperty(name);
		if (value == null) {
			return defaultValue;
		}
		try {
			String[] propertyArray = value.split(splitStr);
			int[] result = new int[propertyArray.length];
			for (int i = 0; i < propertyArray.length; i++) {
				result[i] = Integer.parseInt(propertyArray[i]);
			}
			return result;
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/** Retrieve a property as a long,defaults to 0L if not present. */
	public static Long getLongProperty(String name) {
		return getLongProperty(name, 0L);
	}

	/** Retrieve a property as a long. */
	public static Long getLongProperty(String name, Long defaultValue) {
		String value = SystemConfigUtils.getProperty(name);
		if (value == null || "".equals(value)) {
			return defaultValue;
		}
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return defaultValue;
		}
	}

	/** Retrieve a property value. */
	public static String getProperty(String key) {
		return mConfig.getProperty(key);
	}

	/**
	 * Retrieve a property value & default value.
	 * 
	 * @param key
	 *            Retrieve key
	 * @param defaultValue
	 *            Return default retrieve value
	 * @return String.
	 */
	public static String getProperty(String key, String defaultValue) {
		LOG.debug("Fetching property [" + key + "=" + mConfig.getProperty(key) + "]");
		String value = SystemConfigUtils.getProperty(key);
		if (value == null || "".equals(value)) {
			return defaultValue;
		}
		return value;
	}

	/** Retrieve a property as a array. */
	public static String[] getPropertyArray(String name, String[] defaultValue, String splitStr) {
		String value = SystemConfigUtils.getProperty(name);
		if (value == null) {
			return defaultValue;
		}
		try {
			String[] propertyArray = value.split(splitStr);
			return propertyArray;
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/** Retrieve a property as a array,no default value. */
	public static String[] getPropertyArray(String name, String splitStr) {
		String value = SystemConfigUtils.getProperty(name);
		if (value == null) {
			return null;
		}
		try {
			String[] propertyArray = value.split(splitStr);
			return propertyArray;
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/** Retrieve map property keys. */
	public static Map<String, String> getPropertyMap(String name) {
		String[] maps = getPropertyArray(name, ",");
		Map<String, String> map = new HashMap<String, String>();
		try {
			for (String str : maps) {
				String[] array = str.split(":");
				if (array.length > 1) {
					map.put(array[0], array[1]);
				}
			}
		} catch (Exception e) {
			LOG.error("Get PropertyMap info has error,key is :" + name);
		}
		return map;
	}

	/** Retrieve all property keys. */
	public static Enumeration<Object> keys() {
		return mConfig.keys();
	}

	/**
	 * Reload special property file.
	 * 
	 * @param name
	 *            System configure name.
	 */
	public static void reload(String name) {
		mConfig.clear();
		getReources(name);
	}

	/** Construction method. */
	private SystemConfigUtils() {
	}
}
