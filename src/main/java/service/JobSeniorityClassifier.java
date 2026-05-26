package service;

import vo.JobPosting;

import java.util.Locale;
import java.util.regex.Pattern;

public class JobSeniorityClassifier {

    private static final SeniorityRule[] TITLE_RULES = {
            new SeniorityRule("internship", "\\b(intern|internship)\\b"),
            new SeniorityRule("lead", "\\b(staff|principal)\\b.*\\b(engineer|developer|architect|sre|scientist)\\b|\\b(engineer|developer|architect|sre|scientist)\\b.*\\b(staff|principal)\\b"),
            new SeniorityRule("lead", "\\blead\\b"),
            new SeniorityRule("senior", "\\b(senior|sr)\\.?\\b"),
            new SeniorityRule("mid", "\\b(mid|mid-level|mid level|intermediate)\\b"),
            new SeniorityRule("junior", "\\b(junior|jr)\\.?\\b|\\bentry[- ]level\\b")
    };

    private static final SeniorityRule[] DESCRIPTION_RULES = {
            new SeniorityRule("internship", "\\b(internship|intern role|intern position)\\b"),
            new SeniorityRule("lead", "\\b(lead role|lead position)\\b"),
            new SeniorityRule("senior", "\\b(senior[- ]level|senior role|senior position)\\b"),
            new SeniorityRule("mid", "\\b(mid[- ]level|intermediate[- ]level|mid role|mid position)\\b"),
            new SeniorityRule("junior", "\\b(junior[- ]level|junior role|junior position|entry[- ]level)\\b")
    };

    public String classify(JobPosting job) {
        String titleSeniority = findMatch(job.getTitle(), TITLE_RULES);
        if (!titleSeniority.isEmpty()) {
            return titleSeniority;
        }
        return findMatch(job.getDescription(), DESCRIPTION_RULES);
    }

    private String findMatch(String text, SeniorityRule[] rules) {
        String normalizedText = text == null ? "" : text.toLowerCase(Locale.ROOT);
        for (SeniorityRule rule : rules) {
            if (rule.pattern.matcher(normalizedText).find()) {
                return rule.seniority;
            }
        }
        return "";
    }

    private static class SeniorityRule {
        private final String seniority;
        private final Pattern pattern;

        private SeniorityRule(String seniority, String pattern) {
            this.seniority = seniority;
            this.pattern = Pattern.compile(pattern);
        }
    }
}
