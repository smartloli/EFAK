package com.smartloli.kafka.eagle.utils;

import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Date May 25, 2015
 * 
 * @Author dengjie
 * 
 * @Note This is the single entry point for accessing configuration properties.
 * 
 */
public final class SystemConfigUtils {
	private static Properties mConfig;

	private static final Logger LOG = LoggerFactory.getLogger(SystemConfigUtils.class);
	static {
		mConfig = new Properties();
		getReources("system-config.properties");
	}

	private static void getReources(String name) {
		try {
			try {
				String osName = System.getProperties().getProperty("os.name");
				if (osName.contains("Mac") || osName.contains("Win")) {
					mConfig.load(SystemConfigUtils.class.getClassLoader().getResourceAsStream(name));
				} else {
					mConfig.load(new FileInputStream(System.getProperty("user.dir") + "/conf/" + name));
				}
			} catch (Exception exp1) {
				exp1.printStackTrace();
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
			e.printStackTrace();
		}
	}

	/**
	 * Reload special property file
	 * 
	 * @param name
	 */
	public static void reload(String name) {
		mConfig.clear();
		getReources(name);
	}

	private SystemConfigUtils() {
	}

	/**
	 * Retrieve a property value
	 */
	public static String getProperty(String key) {
		return mConfig.getProperty(key);
	}

	public static String getProperty(String key, String defaultValue) {
		LOG.debug("Fetching property [" + key + "=" + mConfig.getProperty(key) + "]");
		String value = SystemConfigUtils.getProperty(key);
		if (value == null) {
			return defaultValue;
		}
		return value;
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

	/**
	 * Retrieve a property as a int,defaults to 0 if not present.
	 */
	public static int getIntProperty(String name) {
		return getIntProperty(name, 0);
	}

	public static Long getLongProperty(String name) {
		return getLongProperty(name, 0L);
	}

	/**
	 * Retrieve a property as a int
	 */
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

	public static Long getLongProperty(String name, Long defaultValue) {
		String value = SystemConfigUtils.getProperty(name);
		if (value == null) {
			return defaultValue;
		}
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return defaultValue;
		}
	}

	/**
	 * 
	 */
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

	/**
	 */
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

	/**
	 */
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

	/**
	 */
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

	/**
	 * Retrieve all property keys
	 */
	public static Enumeration<Object> keys() {
		return mConfig.keys();
	}

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
			e.printStackTrace();
		}
		return map;
	}
}
