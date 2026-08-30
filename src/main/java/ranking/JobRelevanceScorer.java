package service;

import vo.JobPosting;

import java.util.Locale;
import java.util.Set;

public class JobRelevanceScorer {

    private static final Set<String> GENERAL_ROLE_WORDS = Set.of(
            "engineer", "developer", "analyst", "manager", "specialist",
            "architect", "administrator", "consultant", "technician");

    public int score(JobPosting job, JobSearchCriteria criteria) {
        if (job == null || criteria == null) {
            return 0;
        }

        return scoreTitle(job.getTitle(), criteria.getPosition())
                + scoreDescription(job.getDescription(), criteria.getPosition())
                + scorePreference(job.getSeniority(), criteria.getPreferredSeniority(), 8)
                + scorePreference(job.getWorkType(), criteria.getPreferredWorkType(), 8)
                + scorePreference(job.getEmploymentType(), criteria.getPreferredEmploymentType(), 6)
                + scorePreference(job.getEmploymentSchedule(), criteria.getPreferredEmploymentSchedule(), 6)
                + scoreLocation(job.getCity(), criteria.getLocation());
    }

    private int scoreTitle(String title, String searchQuery) {
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

    private int scoreDescription(String description, String searchQuery) {
        if (description == null || searchQuery == null || searchQuery.isBlank()) {
            return 0;
        }

        String normalizedDescription = normalize(description);
        String normalizedQuery = normalize(searchQuery);
        String[] keywords = normalizedQuery.split("\\s+");
        int matchingKeywords = countMatchingKeywords(normalizedDescription, keywords);

        if (normalizedDescription.contains(normalizedQuery)) {
            return 5;
        }
        if (matchingKeywords == keywords.length) {
            return 3;
        }
        return matchingKeywords > 0 ? 1 : 0;
    }

    private int scorePreference(String value, String preference, int matchingScore) {
        if (preference == null || preference.isBlank()) {
            return 0;
        }

        return normalize(preference).equals(normalize(value)) ? matchingScore : 0;
    }

    private int scoreLocation(String city, String requestedLocation) {
        if (city == null || city.isBlank() || requestedLocation == null || requestedLocation.isBlank()) {
            return 0;
        }

        String normalizedCity = normalize(city);
        String normalizedLocation = normalize(requestedLocation);
        return normalizedLocation.equals(normalizedCity)
                || normalizedLocation.startsWith(normalizedCity + ",") ? 4 : 0;
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
