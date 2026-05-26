package service;

import java.util.Locale;
import java.util.Set;

public class JobRelevanceScorer {

    private static final Set<String> GENERAL_ROLE_WORDS = Set.of(
            "engineer", "developer", "analyst", "manager", "specialist",
            "architect", "administrator", "consultant", "technician");

    public int score(String title, String searchQuery) {
        if (title == null || searchQuery == null || searchQuery.isBlank()) {
            return 0;
        }

        String normalizedTitle = normalize(title);
        String normalizedQuery = normalize(searchQuery);
        String[] keywords = normalizedQuery.split("\\s+");
        int matchingKeywords = countMatchingKeywords(normalizedTitle, keywords);
        int score;

        if (normalizedTitle.equals(normalizedQuery)) {
            score = 100;
        } else if (normalizedTitle.contains(normalizedQuery)) {
            score = 75;
        } else if (matchingKeywords == keywords.length) {
            score = 50;
        } else {
            score = calculatePartialKeywordScore(normalizedTitle, keywords);
        }
        return score;
    }

    private int countMatchingKeywords(String title, String[] keywords) {
        int matches = 0;
        for (String keyword : keywords) {
            if (!keyword.isBlank() && title.contains(keyword)) {
                matches++;
            }
        }
        return matches;
    }

    private int calculatePartialKeywordScore(String title, String[] keywords) {
        int score = 0;
        for (String keyword : keywords) {
            if (!keyword.isBlank() && title.contains(keyword)) {
                score += GENERAL_ROLE_WORDS.contains(keyword) ? 10 : 20;
            }
        }
        return score;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }
}
