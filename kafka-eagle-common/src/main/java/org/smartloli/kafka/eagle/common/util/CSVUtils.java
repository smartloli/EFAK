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
package org.smartloli.kafka.eagle.common.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.beanutils.BeanUtils;

import java.io.*;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.zip.GZIPOutputStream;

/**
 * Tool class for exporting data results.
 *
 * @author smartloli.
 * <p>
 * Created by Jul 18, 2021
 */
public class CSVUtils {
    /**
     * Create csv file .
     */
    public static String create(List exportData, LinkedHashMap map, String outPutPath, String fileName) {
        File csvFile = null;
        BufferedWriter csvFileOutputStream = null;
        try {
            File file = new File(outPutPath);
            if (!file.exists()) {
                file.mkdir();
            }
            csvFile = File.createTempFile(fileName, ".csv", new File(outPutPath));
            csvFileOutputStream = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(csvFile), "UTF-8"), 1024);
            for (Iterator propertyIterator = map.entrySet().iterator(); propertyIterator.hasNext(); ) {
                java.util.Map.Entry propertyEntry = (java.util.Map.Entry) propertyIterator.next();
                csvFileOutputStream.write((String) propertyEntry.getValue() != null ? (String) propertyEntry.getValue() : "");
                if (propertyIterator.hasNext()) {
                    csvFileOutputStream.write(",");
                }
            }
            csvFileOutputStream.newLine();
            for (Iterator iterator = exportData.iterator(); iterator.hasNext(); ) {
                Object row = (Object) iterator.next();
                for (Iterator propertyIterator = map.entrySet().iterator(); propertyIterator.hasNext(); ) {
                    java.util.Map.Entry propertyEntry = (java.util.Map.Entry) propertyIterator.next();
                    csvFileOutputStream.write((String) BeanUtils.getProperty(row, (String) propertyEntry.getKey()));
                    if (propertyIterator.hasNext()) {
                        csvFileOutputStream.write(",");
                    }
                }
                if (iterator.hasNext()) {
                    csvFileOutputStream.newLine();
                }
            }
            csvFileOutputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                csvFileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return compress(csvFile.getAbsolutePath());
    }

    private static String compress(String inFileName) {
        String outFileName = inFileName + ".gz";
        FileInputStream in = null;
        try {
            in = new FileInputStream(new File(inFileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        GZIPOutputStream out = null;
        try {
            out = new GZIPOutputStream(new FileOutputStream(outFileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] buf = new byte[10240];
        int len = 0;
        try {
            while (((in.available() > 10240) && (in.read(buf)) > 0)) {
                out.write(buf);
            }
            len = in.available();
            in.read(buf, 0, len);
            out.write(buf, 0, len);
            in.close();
            out.flush();
            out.close();
        } catch (IOException e) {

        }
        new File(inFileName).delete();
        JSONObject target = new JSONObject();
        target.put("download", new File(outFileName).getName());
        target.put("size", new File(outFileName).length());
        return target.toJSONString();
    }
}
