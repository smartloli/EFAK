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
package org.smartloli.kafka.eagle.web.mapper;

import org.apache.commons.io.IOUtils;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.smartloli.kafka.eagle.common.util.ErrorUtils;
import org.smartloli.kafka.eagle.web.mapper.dao.KeBaseMapper;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * Mapper mybatis mysql database factory.
 *
 * @author smartloli.
 * <p>
 * Created by Apr 11, 2021
 */
public class KeSqlMapperFactory {
    private static final String CONFIGURATION_PATH = "ke-mybatis.xml";
    private static final String ENV = "default";


    private static final Map<String, SqlSessionFactory> SQL_SESSION_FACTORYS = new HashMap<String, SqlSessionFactory>();

    /**
     * Create a mapper of environment by Mapper class
     *
     * @param clazz Mapper class
     */
    public static <T> T createMapper(Class<? extends KeBaseMapper> clazz) {
        SqlSessionFactory sqlSessionFactory = getSqlSessionFactory();
        SqlSession sqlSession = sqlSessionFactory.openSession();
        KeBaseMapper mapper = sqlSession.getMapper(clazz);
        return (T) MapperProxy.bind(mapper, sqlSession);
    }

    /**
     * Mapper Proxy executing mapper method and close sqlsession
     */
    private static class MapperProxy implements InvocationHandler {
        private KeBaseMapper mapper;
        private SqlSession sqlSession = null;

        private MapperProxy(KeBaseMapper mapper, SqlSession sqlSession) {
            this.mapper = mapper;
            this.sqlSession = sqlSession;
        }

        private static KeBaseMapper bind(KeBaseMapper mapper, SqlSession sqlSession) {
            return (KeBaseMapper) Proxy.newProxyInstance(mapper.getClass().getClassLoader(), mapper.getClass()
                    .getInterfaces(), new MapperProxy(mapper, sqlSession));
        }

        /**
         * execute mapper method and finally close single sqlSession.
         */
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            Object object = null;
            try {
                object = method.invoke(mapper, args);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (this.sqlSession != null) {
                        this.sqlSession.close();
                        this.sqlSession = null;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            return object;
        }
    }

    /**
     * According to environment get SqlSessionFactory
     *
     * @return SqlSessionFactory
     */
    private static SqlSessionFactory getSqlSessionFactory() {
        String environment = ENV;
        SqlSessionFactory sqlSessionFactory = SQL_SESSION_FACTORYS.get(environment);
        if (sqlSessionFactory != null) {
            return sqlSessionFactory;
        } else {
            InputStream inputStream = null;
            try {
                inputStream = Resources.getResourceAsStream(CONFIGURATION_PATH);
                sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream, environment);
            } catch (IOException e) {
                e.printStackTrace();
                ErrorUtils.print(KeSqlMapperFactory.class).error(String.format("KeSqlMapper execute sql has error, msg is %s.", e));
            } finally {
                IOUtils.closeQuietly(inputStream);
            }

            SQL_SESSION_FACTORYS.put(environment, sqlSessionFactory);
            return sqlSessionFactory;
        }
    }
}
