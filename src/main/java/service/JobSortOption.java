package service;

import java.util.Locale;

public enum JobSortOption {

    RELEVANCE("relevance");

    private final String apiValue;

    JobSortOption(String apiValue) {
        this.apiValue = apiValue;
    }

    public String getApiValue() {
        return apiValue;
    }

    public static JobSortOption fromApiValue(String value) {
        if (value == null || value.isBlank()) {
            return RELEVANCE;
        }

        String normalizedValue = value.trim().toLowerCase(Locale.ROOT);
        for (JobSortOption option : values()) {
            if (option.apiValue.equals(normalizedValue)) {
                return option;
            }
        }

        throw new IllegalArgumentException("Unsupported sort option: " + value);
    }
}
