/**
 * MySqlBatchUpdateInjector.java
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
package org.kafka.eagle.web.dao.method;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.springframework.util.StringUtils;

/**
 * Description: TODO
 * @Author: smartloli
 * @Date: 2023/8/14 23:07
 * @Version: 3.4.0
 */
public class ReplaceBatchSomeColumn extends AbstractMethod {

    public ReplaceBatchSomeColumn() {
        super("replaceBatchSomeColumn");
    }

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        final String sql = "<script>insert into %s %s values %s ON DUPLICATE KEY UPDATE %s</script>";
        final String tableName = tableInfo.getTableName();
        final String filedSql = prepareFieldSql(tableInfo);
        final String modelValuesSql = prepareModelValuesSql(tableInfo);
        final String duplicateKeySql =prepareDuplicateKeySql(tableInfo);
        final String sqlResult = String.format(sql, tableName, filedSql, modelValuesSql,duplicateKeySql);
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sqlResult, modelClass);
        return this.addInsertMappedStatement(mapperClass, modelClass, "replaceBatchSomeColumn", sqlSource, new NoKeyGenerator(), null, null);
    }

    /**
     * Prepare ON DUPLICATE KEY UPDATE sql.
     * @param tableInfo
     * @return
     */
    private String prepareDuplicateKeySql(TableInfo tableInfo) {
        final StringBuilder duplicateKeySql = new StringBuilder();
        if(!StringUtils.isEmpty(tableInfo.getKeyColumn())) {
            duplicateKeySql.append(tableInfo.getKeyColumn()).append("=values(").append(tableInfo.getKeyColumn()).append("),");
        }

        tableInfo.getFieldList().forEach(x -> {
            duplicateKeySql.append(x.getColumn())
                    .append("=values(")
                    .append(x.getColumn())
                    .append("),");
        });
        duplicateKeySql.delete(duplicateKeySql.length() - 1, duplicateKeySql.length());
        return duplicateKeySql.toString();
    }

    /**
     * Prepare field sql string.
     * @param tableInfo
     * @return
     */
    private String prepareFieldSql(TableInfo tableInfo) {
        StringBuilder fieldSql = new StringBuilder();
        fieldSql.append(tableInfo.getKeyColumn()).append(",");
        tableInfo.getFieldList().forEach(x -> {
            fieldSql.append(x.getColumn()).append(",");
        });
        fieldSql.delete(fieldSql.length() - 1, fieldSql.length());
        fieldSql.insert(0, "(");
        fieldSql.append(")");
        return fieldSql.toString();
    }

    private String prepareModelValuesSql(TableInfo tableInfo){
        final StringBuilder valueSql = new StringBuilder();
        valueSql.append("<foreach collection=\"list\" item=\"item\" index=\"index\" open=\"(\" separator=\"),(\" close=\")\">");
        if(!StringUtils.isEmpty(tableInfo.getKeyProperty())) {
            valueSql.append("#{item.").append(tableInfo.getKeyProperty()).append("},");
        }
        tableInfo.getFieldList().forEach(x -> valueSql.append("#{item.").append(x.getProperty()).append("},"));
        valueSql.delete(valueSql.length() - 1, valueSql.length());
        valueSql.append("</foreach>");
        return valueSql.toString();
    }

}
