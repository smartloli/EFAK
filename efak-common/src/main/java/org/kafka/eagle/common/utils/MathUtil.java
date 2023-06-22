/**
 * MathUtils.java
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
package org.kafka.eagle.common.utils;

/**
 * Math util tools.
 *
 * @Author: smartloli
 * @Date: 2023/6/22 21:02
 * @Version: 3.4.0
 */
public class MathUtil {
    /**
     * Round up by numerator and denominator.
     */
    public static int ceil(int numerator, int denominator) {
        return (denominator % numerator == 0) ? (denominator / numerator) : (denominator / numerator + 1);
    }

    public static double percent(long number1, long number2) {
        double percent = 0.00;
        if (number1 <= number2) {
            percent = StrUtils.numberic(number1 * 100.0 / number2);
        } else {
            percent = StrUtils.numberic(number2 * 100.0 / number1);
        }
        return percent;
    }
}
