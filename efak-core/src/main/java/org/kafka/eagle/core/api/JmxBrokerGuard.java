package org.kafka.eagle.core.api;

import lombok.extern.slf4j.Slf4j;
import org.kafka.eagle.dto.jmx.JMXInitializeInfo;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Per-broker JMX circuit breaker. After consecutive failures the broker is
 * skipped for a cooldown so one stuck node cannot stall a full collect round.
 */
@Slf4j
public final class JmxBrokerGuard {

    private static final int FAILURE_THRESHOLD = 5;
    private static final long OPEN_MILLIS = 60_000L;

    private static final ConcurrentHashMap<String, State> STATES = new ConcurrentHashMap<>();

    private JmxBrokerGuard() {
    }

    public static boolean isOpen(JMXInitializeInfo info) {
        State state = STATES.get(key(info));
        if (state == null) {
            return false;
        }
        if (state.openUntil > 0 && System.currentTimeMillis() < state.openUntil) {
            return true;
        }
        if (state.openUntil > 0 && System.currentTimeMillis() >= state.openUntil) {
            state.failures.set(0);
            state.openUntil = 0;
        }
        return false;
    }

    public static void success(JMXInitializeInfo info) {
        STATES.remove(key(info));
    }

    public static void fail(JMXInitializeInfo info) {
        State state = STATES.computeIfAbsent(key(info), k -> new State());
        int failures = state.failures.incrementAndGet();
        if (failures >= FAILURE_THRESHOLD && state.openUntil == 0) {
            state.openUntil = System.currentTimeMillis() + OPEN_MILLIS;
            log.warn("JMX circuit open for {}:{} after {} failures, skip for {}s",
                    info.getHost(), info.getPort(), failures, OPEN_MILLIS / 1000);
        }
    }

    static String key(JMXInitializeInfo info) {
        return info.getHost() + ":" + info.getPort();
    }

    private static final class State {
        private final AtomicInteger failures = new AtomicInteger();
        private volatile long openUntil;
    }
}
