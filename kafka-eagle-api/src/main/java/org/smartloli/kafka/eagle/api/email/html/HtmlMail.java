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
package org.smartloli.kafka.eagle.api.email.html;

/**
 * Convert html mail.
 * 
 * @author smartloli.
 *
 *         Created by Mar 26, 2019
 */
public class HtmlMail {

	public String toString() {
		String toString = "<!DOCTYPE html>\n" + 
				"<html>\n" + 
				"<head>\n" + 
				"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n" + 
				"<link rel=\"icon\" type=\"image/ico\" href=\"images/favicon.ico\"/>\n" + 
				"<title>Kafka Eagle Alert</title>\n" + 
				"<style type=\"text/css\">\n" + 
				"html, body, div, span, applet, object, iframe, h1, h2, h3, h4, h5, h6, p, blockquote, pre, a, abbr, acronym, address, big, cite, code, del, dfn, em, img, ins, kbd, q, s, samp, small, strike, strong, sub, sup, tt, var, b, u, i, center, dl, dt, dd, ol, ul, li, fieldset, form, label, legend, table, caption, tbody, tfoot, thead, tr, th, td, article, aside, canvas, details, embed, figure, figcaption, footer, header, hgroup, menu, nav, output, ruby, section, summary, time, mark, audio, video {\n" + 
				"	margin: 0;\n" + 
				"	padding: 0;\n" + 
				"	border: 0;\n" + 
				"	font-size: 100%;\n" + 
				"	font: inherit;\n" + 
				"	vertical-align: baseline;\n" + 
				"}\n" + 
				"article, aside, details, figcaption, figure, footer, header, hgroup, menu, nav, section {\n" + 
				"	display: block;\n" + 
				"}\n" + 
				"body {\n" + 
				"	line-height: 1;\n" + 
				"}\n" + 
				"ol, ul {\n" + 
				"	list-style: none;\n" + 
				"}\n" + 
				"blockquote, q {\n" + 
				"	quotes: none;\n" + 
				"}\n" + 
				"blockquote:before, blockquote:after, q:before, q:after {\n" + 
				"	content: '';\n" + 
				"	content: none;\n" + 
				"}\n" + 
				"table {\n" + 
				"	border-collapse: collapse;\n" + 
				"	border-spacing: 0;\n" + 
				"}\n" + 
				".clear {\n" + 
				"	clear: both;\n" + 
				"	display: block;\n" + 
				"	overflow: hidden;\n" + 
				"	visibility: hidden;\n" + 
				"	width: 0;\n" + 
				"	height: 0;\n" + 
				"}\n" + 
				"body {\n" + 
				"	background: url(https://www.kafka-eagle.org/res/mail/body-bg.png) repeat #232323;\n" + 
				"	font-family: \"HelveticaNeue\", \"Helvetica Neue\", Helvetica, Arial, serif;\n" + 
				"	font-size: 14px;\n" + 
				"	color: #000000;\n" + 
				"	font-weight: 400;\n" + 
				"	line-height: 1.5em;\n" + 
				"}\n" + 
				"h1, h2, h3, h4 {\n" + 
				"	font-family: \"HelveticaNeue\", \"Helvetica Neue\", Helvetica, Arial, serif;\n" + 
				"	color: #000000;\n" + 
				"	font-style: normal;\n" + 
				"	line-height: 1em;\n" + 
				"}\n" + 
				"h1 {\n" + 
				"	font-size: 18px;\n" + 
				"	text-transform: uppercase;\n" + 
				"	font-weight: 700;\n" + 
				"	margin-bottom: 15px;\n" + 
				"}\n" + 
				"h2 {\n" + 
				"	font-size: 16px;\n" + 
				"	font-weight: 700;\n" + 
				"	margin-top: 20px;\n" + 
				"	margin-bottom: 5px;\n" + 
				"}\n" + 
				"h3 {\n" + 
				"	font-size: 15px;\n" + 
				"	color: #5e5e5e;\n" + 
				"	font-style: italic;\n" + 
				"}\n" + 
				"h4 {\n" + 
				"	background: url(https://www.kafka-eagle.org/res/mail/wrapper-bg.png) repeat;\n" + 
				"	font-size: 16px;\n" + 
				"	font-style: italic;\n" + 
				"	font-weight: 400;\n" + 
				"	margin-bottom: 0px;\n" + 
				"	position: absolute;\n" + 
				"	top: -7px;\n" + 
				"	width: 130px;\n" + 
				"	margin-left: -65px;\n" + 
				"	left: 50%;\n" + 
				"}\n" + 
				"a {\n" + 
				"	color: #c16004;\n" + 
				"	font-style: italic;\n" + 
				"	text-decoration: none;\n" + 
				"	cursor: pointer;\n" + 
				"	outline: none;\n" + 
				"}\n" + 
				"a:hover {\n" + 
				"	text-decoration: underline;\n" + 
				"}\n" + 
				"span {\n" + 
				"	color: #c16004;\n" + 
				"}\n" + 
				".copyrights{text-indent:-9999px;height:0;line-height:0;font-size:0;overflow:hidden;}\n" + 
				".divider {\n" + 
				"	background: url(https://www.kafka-eagle.org/res/mail/divider.png) no-repeat center top;\n" + 
				"	width: 620px;\n" + 
				"	height: 3px;\n" + 
				"	margin: 40px auto;\n" + 
				"	display: block;\n" + 
				"	clear: both;\n" + 
				"	position: relative;\n" + 
				"}\n" + 
				".divider_small {\n" + 
				"	background: url(https://www.kafka-eagle.org/res/mail/divider-small.png) no-repeat center top;\n" + 
				"	width: 100%;\n" + 
				"	height: 2px;\n" + 
				"	margin: 20px auto;\n" + 
				"	display: block;\n" + 
				"	clear: both;\n" + 
				"	float: left;\n" + 
				"}\n" + 
				".textstyle1 {\n" + 
				"	font-size: 12px;\n" + 
				"}\n" + 
				".textstyle2 {\n" + 
				"	font-size: 12px;\n" + 
				"	font-style: italic;\n" + 
				"}\n" + 
				"#wrapper {\n" + 
				"	width: 940px;\n" + 
				"	margin: 0 auto;\n" + 
				"}\n" + 
				".logo {\n" + 
				"	width: 276px;\n" + 
				"	height: 58px;\n" + 
				"	padding: 40px 0px;\n" + 
				"	margin: 0 auto;\n" + 
				"}\n" + 
				".textlogo {\n" + 
				"	padding: 40px 0px;\n" + 
				"}\n" + 
				".textlogo h1 {\n" + 
				"	font-size: 40px;\n" + 
				"	color: #ffffff;\n" + 
				"	text-align: center;\n" + 
				"	text-transform: uppercase;\n" + 
				"	margin: 0;\n" + 
				"}\n" + 
				".textlogo h1 a {\n" + 
				"	color: #ffffff;\n" + 
				"}\n" + 
				"#content {\n" + 
				"	background: url(https://www.kafka-eagle.org/res/mail/wrapper-bg.png) repeat #ffffff;\n" + 
				"	width: 620px;\n" + 
				"	padding: 40px 160px;\n" + 
				"	float: left;\n" + 
				"	box-shadow: 0px 1px 2px 0px #000000;\n" + 
				"	-moz-box-shadow: 0px 1px 2px 0px #000000;\n" + 
				"	-webkit-box-shadow: 0px 1px 2px 0px #000000;\n" + 
				"	text-align: center;\n" + 
				"}\n" + 
				".launch {\n" + 
				"	background: url(https://www.kafka-eagle.org/res/mail/kafka-eagle.png) no-repeat;\n" + 
				"	width: 402px;\n" + 
				"	height: 108px;\n" + 
				"	margin: 0 auto;\n" + 
				"}\n" + 
				".countdown_bg {\n" + 
				"	background: url(https://www.kafka-eagle.org/res/mail/countdown-bg.png) no-repeat;\n" + 
				"	width: 560px;\n" + 
				"	height: 60px;\n" + 
				"	margin: 0 auto 20px auto;\n" + 
				"	display: block;\n" + 
				"}\n" + 
				"#defaultCountdown {\n" + 
				"	font-family: \"HelveticaNeue\", \"Helvetica Neue\", Helvetica, Arial, serif;\n" + 
				"	color: #ffffff;\n" + 
				"	font-size: 20px;\n" + 
				"	font-weight: bold;\n" + 
				"	padding-top: 19px;\n" + 
				"	padding-left: 5px;\n" + 
				"	float:left;\n" + 
				"}\n" + 
				"#d, #h, #m, #s {\n" + 
				"	margin-left: 35px;\n" + 
				"	margin-right: 5px;\n" + 
				"	float:left;\n" + 
				"}\n" + 
				"html #d_name, #h_name, #m_name, #s_name {\n" + 
				"	float:left;\n" + 
				"}\n" + 
				"ul.social {\n" + 
				"	width: 100%;\n" + 
				"	height: 32px;\n" + 
				"	text-align: center;\n" + 
				"}\n" + 
				"ul.social li {\n" + 
				"	padding: 0px 2px;\n" + 
				"	display: inline-block;\n" + 
				"	list-style-type: none;\n" + 
				"	text-indent: -9999;\n" + 
				"}\n" + 
				"\n" + 
				"a.vimeo {\n" + 
				"	background: url(https://www.kafka-eagle.org/res/mail/github.png) no-repeat 0 0;\n" + 
				"	width: 32px;\n" + 
				"	height: 32px;\n" + 
				"	display: block;\n" + 
				"}\n" + 
				"a.vimeo:hover {\n" + 
				"	background: url(https://www.kafka-eagle.org/res/mail/github.png) no-repeat 0 -32px;\n" + 
				"}\n" + 
				"</style>\n" + 
				"</head>\n" + 
				"<body>\n" + 
				"<div id=\"wrapper\"> \n" + 
				"  <div id=\"content\">\n" + 
				"    <div class=\"launch\"></div>\n" + 
				"    <div class=\"divider\">\n" + 
				"      <h4>Reporter</h4>\n" + 
				"    </div>\n" + 
				"    <div class=\"countdown_bg\">\n" + 
				"      <div id=\"defaultCountdown\" class=\"countdown\">\n" + 
				"         <div id=\"d\" class=\"numbers\">Type: Consumer</div>\n" + 
				"      </div>\n" + 
				"      <div id=\"defaultCountdown\" class=\"countdown\">\n" + 
				"        <div id=\"d\" class=\"numbers\">ClusterID: Cluster01</div>\n" + 
				"      </div>\n" + 
				"    </div>\n" + 
				"    <div class=\"countdown_bg\">\n" + 
				"      <div id=\"defaultCountdown\" class=\"countdown\">\n" + 
				"         <div id=\"d\" class=\"numbers\">GroupID: kv_gg</div>\n" + 
				"      </div>\n" + 
				"      <div id=\"defaultCountdown\" class=\"countdown\">\n" + 
				"        <div id=\"d\" class=\"numbers\">Topic: t_mm</div>\n" + 
				"      </div>\n" + 
				"    </div>\n" + 
				"    <div class=\"countdown_bg\">\n" + 
				"      <div id=\"defaultCountdown\" class=\"countdown\">\n" + 
				"         <div id=\"d\" class=\"numbers\">LagExpect: 1000</div>\n" + 
				"      </div>\n" + 
				"      <div id=\"defaultCountdown\" class=\"countdown\">\n" + 
				"        <div id=\"d\" class=\"numbers\">LagBlocked: <span>30000</span></div>\n" + 
				"      </div>\n" + 
				"    </div>\n" + 
				"    <div class=\"divider_small\"></div>\n" + 
				"    <h1> Kafka Eagle monitor cluster <span>health</span></h1>\n" + 
				"    If the Kafka cluster or the Zookeeper cluster fails, you will receive the email. Please pay attention and check the availability of the Kafka and Zookeeper clusters.\n" + 
				"    <div class=\"divider\"></div>\n" + 
				"    <p> <a href=\"https://www.kafka-eagle.org/\" target=\"_blank\" class=\"textstyle2\">https://www.kafka-eagle.org</a> </p>\n" + 
				"    <p class=\"textstyle2\">Kafka Eagle By Github </p>\n" + 
				"    <div class=\"divider\"></div>\n" + 
				"    <ul class=\"social\">\n" + 
				"      <li><a href=\"https://github.com/smartloli/kafka-eagle\" target=\"_blank\" class=\"vimeo\"></a></li>\n" + 
				"    </ul>\n" + 
				"  </div>\n" + 
				"  <div class=\"clear\"></div>\n" + 
				"</div>\n" + 
				"</body>\n" + 
				"</html>";
		return toString;
	}
}
