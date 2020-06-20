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
package org.smartloli.kafka.eagle.web.controller;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * Change local language.
 * 
 * @author smartloli.
 *
 *         Created by Jun 20, 2020
 */

@Controller
@RequestMapping("/i18n")
public class I18nController {

	/** Change language. */
	@RequestMapping(value = "/{language}/", method = RequestMethod.GET)
	public ModelAndView language(@PathVariable("language") String language, HttpServletRequest request, HttpServletResponse response) {
		LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
		language = language.toLowerCase();
		System.out.println("language:" + language);
		if (language == null || language.equals("")) {
			return new ModelAndView("redirect:/");
		} else {
			if (language.equals("zh_cn")) {
				localeResolver.setLocale(request, response, Locale.CHINA);
			} else if (language.equals("en")) {
				localeResolver.setLocale(request, response, Locale.ENGLISH);
			} else {
				localeResolver.setLocale(request, response, Locale.CHINA);
			}
		}

		return new ModelAndView("redirect:/");
	}
}
