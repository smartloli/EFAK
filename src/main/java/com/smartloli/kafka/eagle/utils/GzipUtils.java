package com.smartloli.kafka.eagle.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

/**
 * @Date Mar 24, 2016
 *
 * @Author dengjie
 * 
 * @Note English : GZIP compress & uncompress class utils
 * 
 *       中文 : GZIP压缩解压类，压缩数据结果
 */
public class GzipUtils {

	private static final String UTF_16 = "UTF-16";

	/**
	 * English : Strings compress to bytes
	 * 
	 * 中文 : 字符串压缩为字节数组
	 */
	public static byte[] compressToByte(String str) {
		if (str == null || str.length() == 0) {
			return null;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		GZIPOutputStream gzip;
		try {
			gzip = new GZIPOutputStream(out);
			gzip.write(str.getBytes(UTF_16));
			gzip.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return out.toByteArray();
	}

	/**
	 * English : Strings compress to bytes
	 * 
	 * 中文 : 字符串压缩为字节数组
	 */
	public static byte[] compressToByte(String str, String encoding) {
		if (str == null || str.length() == 0) {
			return null;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		GZIPOutputStream gzip;
		try {
			gzip = new GZIPOutputStream(out);
			gzip.write(str.getBytes(encoding));
			gzip.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return out.toByteArray();
	}
}