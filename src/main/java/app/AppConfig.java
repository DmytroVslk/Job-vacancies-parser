package main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppConfig {

    private static final String DOTENV_FILE = ".env";

    private static final String ADZUNA_APP_ID_ENV = "ADZUNA_APP_ID";
    private static final String ADZUNA_APP_KEY_ENV = "ADZUNA_APP_KEY";
    private static final String JOOBLE_API_KEY_ENV = "JOOBLE_API_KEY";
    private static final String SERVER_PORT_ENV = "SERVER_PORT";
    private static final String ADZUNA_COUNTRY_ENV = "ADZUNA_COUNTRY";

    private static final int DEFAULT_SERVER_PORT = 8080;
    private static final String DEFAULT_ADZUNA_COUNTRY = "us";

    private final String adzunaAppId;
    private final String adzunaAppKey;
    private final String joobleApiKey;
    private final int serverPort;
    private final String adzunaCountry;

    private AppConfig(String adzunaAppId, String adzunaAppKey, String joobleApiKey, int serverPort, String adzunaCountry) {
        this.adzunaAppId = adzunaAppId;
        this.adzunaAppKey = adzunaAppKey;
        this.joobleApiKey = joobleApiKey;
        this.serverPort = serverPort;
        this.adzunaCountry = adzunaCountry;
    }

    public static AppConfig fromEnvironment() {
        Map<String, String> dotenvValues = loadDotenv();

        return new AppConfig(
                readRequiredConfig(ADZUNA_APP_ID_ENV, dotenvValues),
                readRequiredConfig(ADZUNA_APP_KEY_ENV, dotenvValues),
                readOptionalConfig(JOOBLE_API_KEY_ENV, "", dotenvValues),
                readServerPort(dotenvValues),
                readOptionalConfig(ADZUNA_COUNTRY_ENV, DEFAULT_ADZUNA_COUNTRY, dotenvValues).toLowerCase()
        );
    }

    public String getAdzunaAppId() {
        return adzunaAppId;
    }

    public String getAdzunaAppKey() {
        return adzunaAppKey;
    }

    public String getJoobleApiKey() {
        return joobleApiKey;
    }

    public int getServerPort() {
        return serverPort;
    }

    public String getAdzunaCountry() {
        return adzunaCountry;
    }

    private static String readRequiredConfig(String name, Map<String, String> dotenvValues) {
        String value = readConfig(name, dotenvValues);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Missing required configuration value: " + name
                            + ". Set it in your environment or in the local .env file."
            );
        }
        return value.trim();
    }

    private static String readOptionalConfig(String name, String defaultValue, Map<String, String> dotenvValues) {
        String value = readConfig(name, dotenvValues);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }

    private static String readConfig(String name, Map<String, String> dotenvValues) {
        String value = System.getenv(name);
        if (value != null && !value.isBlank()) {
            return value;
        }
        return dotenvValues.get(name);
    }

    private static int readServerPort(Map<String, String> dotenvValues) {
        String value = readOptionalConfig(SERVER_PORT_ENV, String.valueOf(DEFAULT_SERVER_PORT), dotenvValues);
        try {
            int port = Integer.parseInt(value);
            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException(
                        "Invalid SERVER_PORT: " + value + ". Use a port between 1 and 65535."
                );
            }
            return port;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Invalid SERVER_PORT: " + value + ". Use a numeric port between 1 and 65535.",
                    e
            );
        }
    }

    private static Map<String, String> loadDotenv() {
        Map<String, String> values = new HashMap<>();
        Path dotenvPath = Path.of(DOTENV_FILE);

        if (!Files.exists(dotenvPath)) {
            return values;
        }

        try {
            List<String> lines = Files.readAllLines(dotenvPath);
            for (String line : lines) {
                readDotenvLine(line, values);
            }
            return values;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read local .env file.", e);
        }
    }

    private static void readDotenvLine(String line, Map<String, String> values) {
        String trimmed = line.trim();

        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
            return;
        }

        int equalsIndex = trimmed.indexOf('=');
        if (equalsIndex <= 0) {
            return;
        }

        String name = trimmed.substring(0, equalsIndex).trim();
        String value = trimmed.substring(equalsIndex + 1).trim();

        values.put(name, removeOptionalQuotes(value));
    }

    private static String removeOptionalQuotes(String value) {
        if (value.length() >= 2
                && ((value.startsWith("\"") && value.endsWith("\""))
                || (value.startsWith("'") && value.endsWith("'")))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}
