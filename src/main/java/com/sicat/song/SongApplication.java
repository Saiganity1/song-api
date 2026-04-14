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

        // Some hosts provide DATABASE_URL as postgres://user:pass@host:port/db?sslmode=require.
        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl == null || databaseUrl.isBlank()) {
            return defaults;
        }

        if (databaseUrl.startsWith("jdbc:postgresql://")) {
            defaults.put("spring.datasource.url", databaseUrl);
            return defaults;
        }

        if (!(databaseUrl.startsWith("postgres://") || databaseUrl.startsWith("postgresql://"))) {
            return defaults;
        }

        URI uri = URI.create(databaseUrl);

        StringBuilder jdbc = new StringBuilder("jdbc:postgresql://")
            .append(uri.getHost());

        if (uri.getPort() != -1) {
            jdbc.append(":").append(uri.getPort());
        }

        jdbc.append(uri.getPath() == null ? "" : uri.getPath());

        if (uri.getQuery() != null && !uri.getQuery().isBlank()) {
            jdbc.append("?").append(uri.getQuery());
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

        return defaults;
    }

    private static String urlDecode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

}