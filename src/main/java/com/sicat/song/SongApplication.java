package com.sicat.song;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SongApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(SongApplication.class);
        application.setDefaultProperties(buildRenderDatabaseDefaults());
        application.run(args);
    }

    private static Map<String, Object> buildRenderDatabaseDefaults() {
        Map<String, Object> defaults = new HashMap<>();

        String databaseUrl = firstPresent(
            "SPRING_DATASOURCE_URL",
            "JDBC_DATABASE_URL",
            "DATABASE_JDBC_URL",
            "DATABASE_URL",
            "POSTGRES_URL",
            "RENDER_POSTGRES_URL"
        );

        if (databaseUrl != null && !databaseUrl.isBlank()) {
            applyDatabaseUrl(defaults, databaseUrl);
            return defaults;
        }

        String host = firstPresent("DB_HOST", "PGHOST", "POSTGRES_HOST", "RENDER_POSTGRES_HOST");
        if (host == null || host.isBlank()) {
            // Local/dev fallback when nothing is provided via env vars.
            defaults.put("spring.datasource.url", "jdbc:postgresql://localhost:5432/db_song?sslmode=disable");
            defaults.put("spring.datasource.username", "postgres");
            defaults.put("spring.datasource.password", "admin");
            return defaults;
        }

        String port = firstPresent("DB_PORT", "PGPORT", "POSTGRES_PORT", "RENDER_POSTGRES_PORT");
        String db = firstPresent("DB_NAME", "PGDATABASE", "POSTGRES_DB", "RENDER_POSTGRES_DB");
        String sslMode = firstPresent("DB_SSLMODE", "PGSSLMODE", "POSTGRES_SSLMODE", "RENDER_POSTGRES_SSLMODE");

        String jdbcUrl = "jdbc:postgresql://" + host + ":" + defaultIfBlank(port, "5432") + "/" + defaultIfBlank(db, "postgres")
            + "?sslmode=" + defaultIfBlank(sslMode, "require");
        defaults.put("spring.datasource.url", jdbcUrl);

        String username = firstPresent("DB_USERNAME", "DB_USER", "PGUSER", "POSTGRES_USER", "RENDER_POSTGRES_USER");
        String password = firstPresent("DB_PASSWORD", "PGPASSWORD", "POSTGRES_PASSWORD", "RENDER_POSTGRES_PASSWORD");
        if (username != null && !username.isBlank()) {
            defaults.put("spring.datasource.username", username);
        }
        if (password != null && !password.isBlank()) {
            defaults.put("spring.datasource.password", password);
        }

        return defaults;
    }

    private static void applyDatabaseUrl(Map<String, Object> defaults, String databaseUrl) {
        if (databaseUrl.startsWith("jdbc:postgresql://")) {
            defaults.put("spring.datasource.url", databaseUrl);
            return;
        }

        if (!(databaseUrl.startsWith("postgres://") || databaseUrl.startsWith("postgresql://"))) {
            return;
        }

        URI uri = URI.create(databaseUrl);

        StringBuilder jdbc = new StringBuilder("jdbc:postgresql://")
            .append(uri.getHost());

        if (uri.getPort() != -1) {
            jdbc.append(":").append(uri.getPort());
        }

        jdbc.append(uri.getPath() == null ? "" : uri.getPath());

        String query = uri.getQuery();
        if (query != null && !query.isBlank()) {
            jdbc.append("?").append(query);
            if (!query.contains("sslmode=")) {
                jdbc.append("&sslmode=require");
            }
        } else {
            jdbc.append("?sslmode=require");
        }

        defaults.put("spring.datasource.url", jdbc.toString());

        String userInfo = uri.getUserInfo();
        if (userInfo != null && !userInfo.isBlank()) {
            String[] parts = userInfo.split(":", 2);
            if (parts.length > 0 && !parts[0].isBlank()) {
                defaults.put("spring.datasource.username", urlDecode(parts[0]));
            }
            if (parts.length > 1 && !parts[1].isBlank()) {
                defaults.put("spring.datasource.password", urlDecode(parts[1]));
            }
        }

        // Some providers set DATABASE_URL without userinfo; allow separate env vars as fallback.
        if (!defaults.containsKey("spring.datasource.username")) {
            String fallbackUsername = firstPresent(
                "SPRING_DATASOURCE_USERNAME",
                "DB_USERNAME",
                "DB_USER",
                "PGUSER",
                "POSTGRES_USER"
            );
            if (fallbackUsername != null && !fallbackUsername.isBlank()) {
                defaults.put("spring.datasource.username", fallbackUsername);
            }
        }

        if (!defaults.containsKey("spring.datasource.password")) {
            String fallbackPassword = firstPresent(
                "SPRING_DATASOURCE_PASSWORD",
                "DB_PASSWORD",
                "PGPASSWORD",
                "POSTGRES_PASSWORD"
            );
            if (fallbackPassword != null && !fallbackPassword.isBlank()) {
                defaults.put("spring.datasource.password", fallbackPassword);
            }
        }
    }

    private static String firstPresent(String... names) {
        for (String name : names) {
            String value = System.getenv(name);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private static String defaultIfBlank(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }

    private static String urlDecode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

}