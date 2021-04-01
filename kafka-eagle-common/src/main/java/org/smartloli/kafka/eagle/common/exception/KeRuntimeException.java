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
package org.smartloli.kafka.eagle.common.exception;

/**
 * Defines the parent class of the runtime.
 *
 * @author smartloli.
 * <p>
 * Created by Apr 02, 2021
 */
public class KeRuntimeException extends RuntimeException {

    public KeRuntimeException() {
        super();
    }

    public KeRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public KeRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public KeRuntimeException(String message) {
        super(message);
    }

    public KeRuntimeException(Throwable cause) {
        super(cause);
    }

    public static KeRuntimeException create(String format, Object... args) {
        return create(null, format, args);
    }

    public static KeRuntimeException create(Throwable cause, String format, Object... args) {
        return new KeRuntimeException(String.format(format, args), cause);
    }

    /**
     * Throws a runtime exception if current thread interrupt flag has been set clears current thread interrupt flag.
     */
    public static void checkInterrupted() {
        if (Thread.interrupted()) {
            // This exception will ensure the control layer will immediately get back control
            throw new KeRuntimeException("Interrupt received; aborting current operation");
        }
    }

}
