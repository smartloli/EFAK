package org.kafka.eagle.web.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Applies classpath SQL under {@code sql/upgrade/} on startup.
 * CREATE TABLE / ADD COLUMN / ADD INDEX are skipped when already present.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SchemaUpgradeRunner implements ApplicationRunner {

    private static final String HISTORY_TABLE = "ke_schema_history";
    private static final String SCRIPT_PATTERN = "classpath:sql/upgrade/*.sql";
    private static final Pattern ADD_COLUMN = Pattern.compile(
            "^ALTER\\s+TABLE\\s+`?([\\w]+)`?\\s+ADD\\s+COLUMN\\s+`?([\\w]+)`?",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern ADD_INDEX = Pattern.compile(
            "^ALTER\\s+TABLE\\s+`?([\\w]+)`?\\s+ADD\\s+(?:UNIQUE\\s+)?(?:INDEX|KEY)\\s+`?([\\w]+)`?",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern CREATE_TABLE = Pattern.compile(
            "^CREATE\\s+TABLE\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?`?([\\w]+)`?",
            Pattern.CASE_INSENSITIVE);

    private final DataSource dataSource;

    public SchemaUpgradeRunner(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        try (Connection connection = dataSource.getConnection()) {
            String product = connection.getMetaData().getDatabaseProductName();
            if (product == null || !(product.toLowerCase(Locale.ROOT).contains("mysql")
                    || product.toLowerCase(Locale.ROOT).contains("mariadb"))) {
                log.info("Skip SQL upgrades on {}", product);
                return;
            }
            ensureHistoryTable(connection);
            Resource[] resources = new PathMatchingResourcePatternResolver().getResources(SCRIPT_PATTERN);
            Arrays.sort(resources, Comparator.comparing(Resource::getFilename, Comparator.nullsLast(String::compareTo)));
            for (Resource resource : resources) {
                applyScript(connection, resource);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Database schema upgrade failed", e);
        }
    }

    private void applyScript(Connection connection, Resource resource) throws Exception {
        String filename = resource.getFilename();
        if (filename == null || !filename.endsWith(".sql")) {
            return;
        }
        String script = readResource(resource);
        String checksum = sha256(script);
        if (alreadyApplied(connection, filename, checksum)) {
            log.info("Schema script already applied: {}", filename);
            return;
        }
        log.info("Applying schema script {}", filename);
        connection.setAutoCommit(false);
        try {
            for (String statement : splitStatements(script)) {
                executeIdempotent(connection, statement);
            }
            recordApplied(connection, filename, checksum);
            connection.commit();
            log.info("Applied schema script {}", filename);
        } catch (Exception e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private void executeIdempotent(Connection connection, String sql) throws Exception {
        String trimmed = sql.trim();
        if (trimmed.isEmpty()) {
            return;
        }
        Matcher addColumn = ADD_COLUMN.matcher(trimmed);
        if (addColumn.find()) {
            String table = addColumn.group(1);
            String column = addColumn.group(2);
            if (!tableExists(connection, table)) {
                log.warn("Skip ADD COLUMN, table {} does not exist yet", table);
                return;
            }
            if (columnExists(connection, table, column)) {
                log.info("Skip existing column {}.{}", table, column);
                return;
            }
        }
        Matcher addIndex = ADD_INDEX.matcher(trimmed);
        if (addIndex.find()) {
            String table = addIndex.group(1);
            String index = addIndex.group(2);
            if (!tableExists(connection, table)) {
                log.warn("Skip ADD INDEX, table {} does not exist yet", table);
                return;
            }
            if (indexExists(connection, table, index)) {
                log.info("Skip existing index {}.{}", table, index);
                return;
            }
        }
        Matcher createTable = CREATE_TABLE.matcher(trimmed);
        if (createTable.find() && tableExists(connection, createTable.group(1))
                && !trimmed.toUpperCase(Locale.ROOT).contains("IF NOT EXISTS")) {
            log.info("Skip existing table {}", createTable.group(1));
            return;
        }
        try (Statement statement = connection.createStatement()) {
            statement.execute(trimmed);
        }
    }

    private boolean alreadyApplied(Connection connection, String filename, String checksum) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT checksum FROM " + HISTORY_TABLE + " WHERE script_name = ?")) {
            ps.setString(1, filename);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }
                String previous = rs.getString(1);
                if (previous != null && !previous.equals(checksum)) {
                    log.warn("Schema script {} checksum changed (was {}, now {}), re-applying", filename, previous, checksum);
                    return false;
                }
                return true;
            }
        }
    }

    private void recordApplied(Connection connection, String filename, String checksum) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO " + HISTORY_TABLE + " (version, script_name, applied_at, success, checksum) VALUES (?,?,?,?,?) "
                        + "ON DUPLICATE KEY UPDATE applied_at = VALUES(applied_at), success = VALUES(success), checksum = VALUES(checksum), version = VALUES(version)")) {
            ps.setString(1, versionOf(filename));
            ps.setString(2, filename);
            ps.setObject(3, LocalDateTime.now());
            ps.setInt(4, 1);
            ps.setString(5, checksum);
            ps.executeUpdate();
        }
    }

    private void ensureHistoryTable(Connection connection) throws Exception {
        String ddl = "CREATE TABLE IF NOT EXISTS " + HISTORY_TABLE + " ("
                + "version varchar(64) NOT NULL, "
                + "script_name varchar(255) NOT NULL, "
                + "applied_at datetime NOT NULL, "
                + "success tinyint(1) NOT NULL DEFAULT 1, "
                + "checksum varchar(64) DEFAULT NULL, "
                + "PRIMARY KEY (script_name)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        try (Statement statement = connection.createStatement()) {
            statement.execute(ddl);
        }
    }

    private boolean tableExists(Connection connection, String table) throws Exception {
        DatabaseMetaData meta = connection.getMetaData();
        try (ResultSet rs = meta.getTables(connection.getCatalog(), null, table, new String[]{"TABLE"})) {
            if (rs.next()) {
                return true;
            }
        }
        try (ResultSet rs = meta.getTables(connection.getCatalog(), null, table.toUpperCase(Locale.ROOT), new String[]{"TABLE"})) {
            if (rs.next()) {
                return true;
            }
        }
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?")) {
            ps.setString(1, table);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean columnExists(Connection connection, String table, String column) throws Exception {
        DatabaseMetaData meta = connection.getMetaData();
        try (ResultSet rs = meta.getColumns(connection.getCatalog(), null, table, column)) {
            if (rs.next()) {
                return true;
            }
        }
        try (ResultSet rs = meta.getColumns(connection.getCatalog(), null, table, column.toUpperCase(Locale.ROOT))) {
            if (rs.next()) {
                return true;
            }
        }
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?")) {
            ps.setString(1, table);
            ps.setString(2, column);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean indexExists(Connection connection, String table, String index) throws Exception {
        DatabaseMetaData meta = connection.getMetaData();
        try (ResultSet rs = meta.getIndexInfo(connection.getCatalog(), null, table, false, false)) {
            while (rs.next()) {
                String name = rs.getString("INDEX_NAME");
                if (index.equalsIgnoreCase(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static List<String> splitStatements(String script) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inSingle = false;
        boolean inDouble = false;
        String[] lines = script.split("\\R");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("--") && !inSingle && !inDouble) {
                continue;
            }
            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                if (c == '\'' && !inDouble) {
                    inSingle = !inSingle;
                } else if (c == '"' && !inSingle) {
                    inDouble = !inDouble;
                }
                if (c == ';' && !inSingle && !inDouble) {
                    String sql = current.toString().trim();
                    if (!sql.isEmpty()) {
                        statements.add(sql);
                    }
                    current.setLength(0);
                } else {
                    current.append(c);
                }
            }
            current.append('\n');
        }
        String tail = current.toString().trim();
        if (!tail.isEmpty()) {
            statements.add(tail);
        }
        return statements;
    }

    private static String readResource(Resource resource) throws Exception {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
            }
        }
        return builder.toString();
    }

    private static String versionOf(String filename) {
        int sep = filename.indexOf("__");
        if (filename.startsWith("V") && sep > 1) {
            return filename.substring(0, sep);
        }
        return filename.replace(".sql", "");
    }

    private static String sha256(String value) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder();
        for (byte b : digest) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
}
