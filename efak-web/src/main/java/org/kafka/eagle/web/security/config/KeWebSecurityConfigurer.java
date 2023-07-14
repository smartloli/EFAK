/**
 * MyWebSecurityConfigurer.java
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
package org.kafka.eagle.web.security.config;

import org.kafka.eagle.web.security.handle.KeAuthenctiationSuccessHandler;
import org.kafka.eagle.web.service.ISysUserDaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;

/**
 * Description: TODO
 *
 * @Author: smartloli
 * @Date: 2023/6/28 23:41
 * @Version: 3.4.0
 */
@Configuration
public class KeWebSecurityConfigurer extends WebSecurityConfigurerAdapter {

    @Autowired
    private KeAuthenctiationSuccessHandler keAuthenctiationSuccessHandler;

    @Autowired
    private ISysUserDaoService sysUserDaoService;

    /**
     * BCryptPasswordEncoder
     */
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private PersistentTokenRepository tokenRepository;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(sysUserDaoService).passwordEncoder(bCryptPasswordEncoder);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests() // Which pages can be accessed directly and which ones require verification?
                .antMatchers("/login", "/error/500", "/job/*").permitAll() // accessed directly
                .anyRequest().authenticated() // All remaining addresses require authentication to access
                .and()
                .formLogin()
                .loginPage("/login") // Specify the desired login page.
                .loginProcessingUrl("/login.do") // Handle authentication path requests.
                .defaultSuccessUrl("/")
                .successHandler(keAuthenctiationSuccessHandler)
                // .failureUrl("/login/failed")
                .and()
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login")
                .and()
                .rememberMe()
                .tokenRepository(tokenRepository)
                .and().csrf().disable();


    }

    @Bean
    public PersistentTokenRepository tokenRepository(DataSource dataSource) {
        JdbcTokenRepositoryImpl repository = new JdbcTokenRepositoryImpl();
        repository.setDataSource(dataSource);
        //repository.setCreateTableOnStartup(true);// Automatically create persistent tables
        return repository;
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/assets/**");
    }
}
