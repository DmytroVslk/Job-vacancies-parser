package service;

import vo.JobPosting;

import java.util.ArrayList;
import java.util.List;

public class JobTagClassifier {

    public List<String> classify(JobPosting job) {
        List<String> tags = new ArrayList<>();

        if (job.isTechRelated()) {
            tags.add("tech");
        }
        addIfPresent(tags, job.getSeniority());
        addIfPresent(tags, job.getWorkType());

        return tags;
    }

    private void addIfPresent(List<String> tags, String value) {
        if (value != null && !value.isBlank() && !tags.contains(value)) {
            tags.add(value);
        }
    }
}
