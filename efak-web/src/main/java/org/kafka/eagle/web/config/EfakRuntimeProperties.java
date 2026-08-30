package org.kafka.eagle.web.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Process role and node identity.
 *
 * {@code all} keeps the current single-process mode (UI + collector).
 * {@code web} serves HTTP only and does not join shard membership.
 * {@code worker} runs scheduled collection and does not need to sit behind the UI LB.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "efak")
public class EfakRuntimeProperties {

    /**
     * Process role: all, web, or worker.
     */
    private String role = "all";

    /**
     * Optional stable node id. Empty means ip:port.
     */
    private String nodeId = "";

    public boolean isWorker() {
        String value = role == null ? "all" : role.trim();
        return "all".equalsIgnoreCase(value) || "worker".equalsIgnoreCase(value);
    }

    public boolean isWeb() {
        String value = role == null ? "all" : role.trim();
        return "all".equalsIgnoreCase(value) || "web".equalsIgnoreCase(value);
    }

    public String normalizedRole() {
        if (isWorker() && isWeb()) {
            return "all";
        }
        if (isWorker()) {
            return "worker";
        }
        return "web";
    }
}
