/**
 * ExcelUtil.java
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
package org.kafka.eagle.plugins.excel;

import org.apache.poi.ss.usermodel.*;
import org.kafka.eagle.pojo.cluster.ClusterCreateInfo;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Excel util tools.
 *
 * @Author: smartloli
 * @Date: 2023/6/3 23:14
 * @Version: 3.4.0
 */
public class ExcelUtil {

    private ExcelUtil() {

    }

    public static List<ClusterCreateInfo> readBrokerInfo(InputStream inputStream) throws IOException {
        // Create a workbook object from the uploaded excel file
        Workbook workbook = WorkbookFactory.create(inputStream);

        // Read the first sheet from the workbook
        Sheet sheet = workbook.getSheetAt(0);

        List<ClusterCreateInfo> data = new ArrayList<>();
        // Read the second row, which is the first row of data.
        int startRow = 1;
        for (int i = startRow; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            DataFormatter formatter = new DataFormatter();
            ClusterCreateInfo clusterCreateInfo = new ClusterCreateInfo();
            clusterCreateInfo.setBrokerId(formatter.formatCellValue(row.getCell(0)));
            clusterCreateInfo.setBrokerHost(formatter.formatCellValue(row.getCell(1)));
            clusterCreateInfo.setBrokerPort(Integer.parseInt(formatter.formatCellValue(row.getCell(2))));
            clusterCreateInfo.setBrokerJmxPort(Integer.parseInt(formatter.formatCellValue(row.getCell(3))));
            data.add(clusterCreateInfo);
        }

        // Close the workbook
        workbook.close();

        return data;
    }

    public static void main(String[] args) throws FileNotFoundException {
        FileInputStream file = new FileInputStream("/Users/smartloli/Desktop/kafka_broker_example.xlsx");
        try {
            List<ClusterCreateInfo> data = readBrokerInfo(file);
            System.out.println(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
