/**
 * WebSecurityConfig.java
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

import org.kafka.eagle.web.security.handle.AuthenticationSuccessHandler;
import org.kafka.eagle.web.security.handle.AuthenticationFailureHandler;
import org.kafka.eagle.web.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

/**
 * Description: Web安全配置
 *
 * @Author: smartloli
 * @Date: 2023/6/28 23:41
 * @Version: 3.4.0
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

        @Autowired
        private AuthenticationSuccessHandler authenticationSuccessHandler;

        @Autowired
        private AuthenticationFailureHandler authenticationFailureHandler;

        @Autowired
        private SysUserService sysUserService;

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                // 创建自定义的RequestCache
                HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
                requestCache.setMatchingRequestParameterName(null); // 禁用默认的continue参数
                
                // 创建自定义的AuthenticationEntryPoint
                LoginUrlAuthenticationEntryPoint authenticationEntryPoint = new LoginUrlAuthenticationEntryPoint("/login");
                authenticationEntryPoint.setUseForward(false);
                
                http.authorizeHttpRequests(authz -> authz
                                .requestMatchers("/login", "/error/**", "/statics/**", "/css/**", "/js/**",
                                                "/images/**", "/fonts/**",
                                                "/plugins/**", "/api/password/**", "/password-tool")
                                .permitAll()
                                .requestMatchers("/users", "/config", "/scheduler").hasRole("ADMIN")
                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .loginProcessingUrl("/login.do")
                                                .defaultSuccessUrl("/dashboard")
                                                .successHandler(authenticationSuccessHandler)
                                                .failureHandler(authenticationFailureHandler)
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/login")
                                                .permitAll())
                                .exceptionHandling(exceptions -> exceptions
                                                .authenticationEntryPoint(authenticationEntryPoint))
                                .requestCache(cache -> cache
                                                .requestCache(requestCache))
                                .csrf(csrf -> csrf.disable());

                return http.build();
        }

        @Bean
        public AuthenticationManager authenticationManager() {
                DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
                authProvider.setUserDetailsService(sysUserService);
                authProvider.setPasswordEncoder(bCryptPasswordEncoder());
                return new ProviderManager(authProvider);
        }

        @Bean
        public BCryptPasswordEncoder bCryptPasswordEncoder() {
                return new BCryptPasswordEncoder();
        }
}