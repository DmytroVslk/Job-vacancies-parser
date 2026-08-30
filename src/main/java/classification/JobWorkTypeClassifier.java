package service;

import vo.JobPosting;

import java.util.Locale;
import java.util.regex.Pattern;

public class JobWorkTypeClassifier {

    private static final WorkTypeRule[] TITLE_RULES = {
            new WorkTypeRule("hybrid", "\\bhybrid\\b"),
            new WorkTypeRule("remote", "\\b(remote|work from home|wfh)\\b"),
            new WorkTypeRule("onsite", "\\b(on[- ]?site|in[- ]office)\\b")
    };

    private static final WorkTypeRule[] DESCRIPTION_RULES = {
            new WorkTypeRule("hybrid", "\\b(hybrid|hybrid work|hybrid schedule|hybrid role)\\b"),
            new WorkTypeRule("remote", "\\b(fully remote|remote work|remote role|remote position|work from home|wfh)\\b"),
            new WorkTypeRule("onsite", "\\b(on[- ]?site role|on[- ]?site position|in[- ]office role|work on[- ]?site)\\b")
    };

    public String classify(JobPosting job) {
        String titleWorkType = findMatch(job.getTitle(), TITLE_RULES);
        if (!titleWorkType.isEmpty()) {
            return titleWorkType;
        }
        return findMatch(job.getDescription(), DESCRIPTION_RULES);
    }

    private String findMatch(String text, WorkTypeRule[] rules) {
        String normalizedText = text == null ? "" : text.toLowerCase(Locale.ROOT);
        for (WorkTypeRule rule : rules) {
            if (rule.pattern.matcher(normalizedText).find()) {
                return rule.workType;
            }
        }
        return "";
    }

    private static class WorkTypeRule {
        private final String workType;
        private final Pattern pattern;

        private WorkTypeRule(String workType, String pattern) {
            this.workType = workType;
            this.pattern = Pattern.compile(pattern);
        }
    }
}
