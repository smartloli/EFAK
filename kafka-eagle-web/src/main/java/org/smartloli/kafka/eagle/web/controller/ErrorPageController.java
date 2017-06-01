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

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * Error page controller to viewer data.
 * 
 * @author smartloli.
 *
 *         Created by Sep 6, 2016
 */
@Controller
@RequestMapping("/errors")
public class ErrorPageController {

	/** 403 error page viewer. */
	@RequestMapping(value = "/403", method = RequestMethod.GET)
	public ModelAndView e403(HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/error/403");
		return mav;
	}
	
	/** 404 error page viewer. */
	@RequestMapping(value = "/404", method = RequestMethod.GET)
	public ModelAndView e404(HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/error/404");
		return mav;
	}

	/** 405 error page viewer. */
	@RequestMapping(value = "/405", method = RequestMethod.GET)
	public ModelAndView e405(HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/error/405");
		return mav;
	}

	/** 500 error page viewer. */
	@RequestMapping(value = "/500", method = RequestMethod.GET)
	public ModelAndView e500(HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/error/500");
		return mav;
	}

	/** 503 error page viewer. */
	@RequestMapping(value = "/503", method = RequestMethod.GET)
	public ModelAndView e503(HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/error/503");
		return mav;
	}

}
