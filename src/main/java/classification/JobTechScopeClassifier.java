package service;

import vo.JobPosting;

import java.util.Locale;
import java.util.regex.Pattern;

public class JobTechScopeClassifier {

    private static final Pattern TECH_CATEGORY = Pattern.compile(
            "\\b(it jobs?|information technology|software|technology|tech)\\b"
    );

    private static final Pattern TECH_ROLE = Pattern.compile(
            "\\b(software (engineer|developer)|java developer|python developer|javascript developer"
                    + "|front[- ]?end developer|back[- ]?end developer|full[- ]?stack developer"
                    + "|mobile developer|android developer|ios developer|web developer"
                    + "|qa (engineer|tester)|quality assurance|test automation engineer|sdet"
                    + "|devops|site reliability engineer|sre|platform engineer"
                    + "|data (analyst|engineer|scientist)|machine learning engineer|ml engineer"
                    + "|cloud (engineer|architect)|security engineer|cybersecurity analyst"
                    + "|systems? administrator|database administrator|network engineer"
                    + "|it support|help desk|technical support engineer"
                    + "|it project manager|technical project manager)\\b"
    );

    private static final Pattern TECH_DESCRIPTION = Pattern.compile(
            "\\b(software development|quality assurance|test automation|devops|site reliability"
                    + "|data engineering|data science|machine learning|cloud infrastructure"
                    + "|cybersecurity|information technology|it support)\\b"
    );

    public boolean isTechRelated(JobPosting job) {
        return matches(job.getTitle(), TECH_ROLE)
                || matches(job.getCategory(), TECH_CATEGORY)
                || matches(job.getDescription(), TECH_DESCRIPTION);
    }

    private boolean matches(String value, Pattern pattern) {
        String normalizedValue = value == null ? "" : value.toLowerCase(Locale.ROOT);
        return pattern.matcher(normalizedValue).find();
    }
}
