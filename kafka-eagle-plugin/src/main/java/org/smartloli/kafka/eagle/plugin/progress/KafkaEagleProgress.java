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
package org.smartloli.kafka.eagle.plugin.progress;

import java.text.DecimalFormat;

import org.smartloli.kafka.eagle.common.util.CalendarUtils;

/**
 * Print kafka eagle progress.
 * 
 * @author smartloli.
 *
 *         Created by Mar 4, 2020
 */
public class KafkaEagleProgress {

	private String msg;
	private int barLength;
	private char showChar;
	private DecimalFormat formater = new DecimalFormat("#.##%");
	private final String DATA_FORMAT_YEAN_MON_DAY_HOUR_MIN_SEC = "yyyy-MM-dd HH:mm:ss";

	public KafkaEagleProgress(int barLength, char showChar, String initMsg) {
		this.barLength = barLength;
		this.showChar = showChar;
		if (initMsg.equals("port")) {
			this.msg = "[" + CalendarUtils.getCustomDate(DATA_FORMAT_YEAN_MON_DAY_HOUR_MIN_SEC) + "] INFO: Port Progress: [";
		} else if (initMsg.equals("config")) {
			this.msg = "[" + CalendarUtils.getCustomDate(DATA_FORMAT_YEAN_MON_DAY_HOUR_MIN_SEC) + "] INFO: Config Progress: [";
		} else if (initMsg.equals("startup")) {
			this.msg = "[" + CalendarUtils.getCustomDate(DATA_FORMAT_YEAN_MON_DAY_HOUR_MIN_SEC) + "] INFO: Startup Progress: [";
		} else {
			this.msg = "[" + CalendarUtils.getCustomDate(DATA_FORMAT_YEAN_MON_DAY_HOUR_MIN_SEC) + "] INFO: Startup Progress: [";
		}
	}

	/** Show kafka eagle progress. */
	public void show(int value) {
		if (value < 0 || value > 100) {
			return;
		}
		reset();
		float rate = (float) (value * 1.0 / 100);
		draw(this.barLength, rate);
		if (value == 100L) {
			afterComplete();
		}
	}

	private void afterComplete() {
		System.out.print("\n");
	}

	private void draw(int barLength, float rate) {
		int length = (int) (barLength * rate);
		System.out.print(this.msg);
		for (int i = 0; i < length; i++) {
			System.out.print(this.showChar);
		}
		for (int i = 0; i < barLength - length; i++) {
			System.out.print(" ");
		}
		System.out.print("] | " + format(rate));
	}

	private String format(float num) {
		return this.formater.format(num);
	}

	private void reset() {
		System.out.print("\r");
	}

	public static void main(String[] args) {
		if (args.length > 0) {
			KafkaEagleProgress progress = new KafkaEagleProgress(50, '#', args[0]);
			for (int i = 1; i <= 100; i++) {
				progress.show(i);
				try {
					if (args[0].equals("port")) {
						Thread.sleep(30);
					} else if (args[0].equals("config")) {
						Thread.sleep(30);
					} else if (args[0].equals("startup")) {
						Thread.sleep(20);
					}
				} catch (Exception e) {
				}
			}
		}
	}

}
