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
package org.kafka.eagle.tool.version;

import java.io.*;
import java.net.URL;
import java.util.StringTokenizer;

/**
 * <p>
 * FIGlet 字体渲染器，用于将文本转换为 ASCII 艺术字。
 * </p>
 * @author Mr.SmartLoli
 * @since 2025/6/19 23:59:45
 * @version 5.0.0
 */
public class FigletFont {
    
    // 字体配置
    public char hardblank;
    public int height = -1;
    public int heightWithoutDescenders = -1;
    public int maxLine = -1;
    public int smushMode = -1;
    public char font[][][] = null;
    public String fontName = "";
    
    // 常量
    public static final int MAX_CHARS = 1024;
    public static final int REGULAR_CHARS = 102;

    /**
     * 获取指定 ASCII 码的字符表示。
     * 
     * @param c ASCII 字符码
     * @return 该字符的二维字符数组
     */
    public char[][] getChar(int c) {
        return font[c];
    }

    /**
     * 获取字符某一行的字符串表示。
     * 
     * @param c ASCII 字符码
     * @param l 行号
     * @return 该字符对应行的字符串表示
     */
    public String getCharLineString(int c, int l) {
        if (font[c][l] == null) {
            return null;
        } else {
            return new String(font[c][l]);
        }
    }

    /**
     * 从输入流构造 FigletFont。
     * 
     * @param stream 包含字体数据的输入流
     * @throws IOException 读取字体数据出错时抛出
     */
    public FigletFont(InputStream stream) throws IOException {
        font = new char[MAX_CHARS][][];
        BufferedReader data = null;
        String dummyS;
        int dummyI;
        int charCode;

        try {
            data = new BufferedReader(new InputStreamReader(new BufferedInputStream(stream), "UTF-8"));

            // 解析字体头信息
            dummyS = data.readLine();
            StringTokenizer st = new StringTokenizer(dummyS, " ");
            String s = st.nextToken();
            hardblank = s.charAt(s.length() - 1);
            height = Integer.parseInt(st.nextToken());
            heightWithoutDescenders = Integer.parseInt(st.nextToken());
            maxLine = Integer.parseInt(st.nextToken());
            smushMode = Integer.parseInt(st.nextToken());
            dummyI = Integer.parseInt(st.nextToken());

            // 读取字体名称（若存在）
            if (dummyI > 0) {
                st = new StringTokenizer(data.readLine(), " ");
                if (st.hasMoreElements()) {
                    fontName = st.nextToken();
                }
            }

            // 初始化字符映射
            int[] charsTo = new int[REGULAR_CHARS];
            int j = 0;
            
            // 标准 ASCII 字符（32-126）
            for (int c = 32; c <= 126; ++c) {
                charsTo[j++] = c;
            }
            
            // 额外的德语字符
            for (int additional : new int[]{196, 214, 220, 228, 246, 252, 223}) {
                charsTo[j++] = additional;
            }

            // 跳过额外头部行
            for (int i = 0; i < dummyI - 1; i++) {
                dummyS = data.readLine();
            }
            
            // 解析字符定义
            int charPos = 0;
            while (dummyS != null) {
                if (charPos < REGULAR_CHARS) {
                    charCode = charsTo[charPos++];
                } else {
                    dummyS = data.readLine();
                    if (dummyS == null) {
                        continue;
                    }
                    charCode = convertCharCode(dummyS);
                }
                
                // 读取字符的每一行
                for (int h = 0; h < height; h++) {
                    dummyS = data.readLine();
                    if (dummyS != null) {
                        if (h == 0)
                            font[charCode] = new char[height][];
                        int t = dummyS.length() - 1 - ((h == height - 1) ? 1 : 0);
                        if (height == 1)
                            t++;
                        font[charCode][h] = new char[t];
                        for (int l = 0; l < t; l++) {
                            char a = dummyS.charAt(l);
                            font[charCode][h][l] = (a == hardblank) ? ' ' : a;
                        }
                    }
                }
            }
        } finally {
            if (data != null) {
                data.close();
            }
        }
    }

    /**
     * 将字符编码字符串转换为整数。
     * 
     * @param input 字符编码字符串（支持十六进制、八进制或十进制）
     * @return 整数形式的字符编码
     */
    int convertCharCode(String input) {
        String codeTag = input.concat(" ").split(" ")[0];
        if (codeTag.matches("^0[xX][0-9a-fA-F]+$")) {
            return Integer.parseInt(codeTag.substring(2), 16);
        } else if (codeTag.matches("^0[0-7]+$")) {
            return Integer.parseInt(codeTag.substring(1), 8);
        } else {
            return Integer.parseInt(codeTag);
        }
    }

    /**
     * 使用当前字体将文本消息转换为 ASCII 艺术字。
     * 
     * @param message 待转换的文本
     * @return 转换后的 ASCII 艺术字字符串
     */
    public String convert(String message) {
        StringBuilder result = new StringBuilder();
        for (int l = 0; l < this.height - 1; l++) {
            for (int c = 0; c < message.length(); c++) {
                result.append(this.getCharLineString((int) message.charAt(c), l));
            }
            result.append('\n');
        }
        return result.toString();
    }

    /**
     * 使用给定输入流的字体将文本转换为 ASCII 艺术字。
     * 
     * @param fontFileStream 包含字体数据的输入流
     * @param message 待转换的文本
     * @return 转换后的 ASCII 艺术字字符串
     * @throws IOException 读取字体失败时抛出
     */
    public static String convertOneLine(InputStream fontFileStream, String message) throws IOException {
        return new FigletFont(fontFileStream).convert(message);
    }

    /**
     * 使用默认字体将文本转换为 ASCII 艺术字。
     * 
     * @param message 待转换的文本
     * @return 转换后的 ASCII 艺术字字符串
     * @throws IOException 读取默认字体失败时抛出
     */
    public static String convertOneLine(String message) throws IOException {
        return convertOneLine(FigletFont.class.getClassLoader().getResourceAsStream("standard.flf"), message);
    }

    /**
     * 使用文件中的字体将文本转换为 ASCII 艺术字。
     * 
     * @param fontFile 字体文件
     * @param message 待转换的文本
     * @return 转换后的 ASCII 艺术字字符串
     * @throws IOException 读取字体文件失败时抛出
     */
    public static String convertOneLine(File fontFile, String message) throws IOException {
        return convertOneLine(new FileInputStream(fontFile), message);
    }

    /**
     * 使用路径指定的字体将文本转换为 ASCII 艺术字。
     * 
     * @param fontPath 字体路径（支持 classpath:、http://、https://）
     * @param message 待转换的文本
     * @return 转换后的 ASCII 艺术字字符串
     * @throws IOException 读取字体失败时抛出
     */
    public static String convertOneLine(String fontPath, String message) throws IOException {
        InputStream fontStream = null;
        if (fontPath.startsWith("classpath:")) {
            fontStream = FigletFont.class.getResourceAsStream(fontPath.substring(10));
        } else if (fontPath.startsWith("http://") || fontPath.startsWith("https://")) {
            fontStream = new URL(fontPath).openStream();
        } else {
            fontStream = new FileInputStream(fontPath);
        }
        return convertOneLine(fontStream, message);
    }
}
