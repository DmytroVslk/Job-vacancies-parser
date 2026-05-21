package main;

public class AppConfig {

    private static final String ADZUNA_APP_ID_ENV = "ADZUNA_APP_ID";
    private static final String ADZUNA_APP_KEY_ENV = "ADZUNA_APP_KEY";

    private final String adzunaAppId;
    private final String adzunaAppKey;

    private AppConfig(String adzunaAppId, String adzunaAppKey) {
        this.adzunaAppId = adzunaAppId;
        this.adzunaAppKey = adzunaAppKey;
    }

    public static AppConfig fromEnvironment() {
        return new AppConfig(
                readRequiredEnv(ADZUNA_APP_ID_ENV),
                readRequiredEnv(ADZUNA_APP_KEY_ENV)
        );
    }

    public String getAdzunaAppId() {
        return adzunaAppId;
    }

    public String getAdzunaAppKey() {
        return adzunaAppKey;
    }

    private static String readRequiredEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Missing required environment variable: " + name
                            + ". Set ADZUNA_APP_ID and ADZUNA_APP_KEY before starting the app."
            );
        }
        return value.trim();
    }
}
