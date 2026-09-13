package classification;

import java.util.ArrayList;
import java.util.List;

import domain.JobPosting;

public class JobTagClassifier {

    public List<String> classify(JobPosting job) {
        List<String> tags = new ArrayList<>();

        if (job.techRelated()) {
            tags.add("tech");
        }
        addIfPresent(tags, job.seniority());
        addIfPresent(tags, job.workType());

        return tags;
    }

    private void addIfPresent(List<String> tags, String value) {
        if (value != null && !value.isBlank() && !tags.contains(value)) {
            tags.add(value);
        }
    }
}
