package domain;

import java.util.List;

public record JobPosting(
        String title,
        String city,
        String companyName,
        String websiteName,
        String source,
        String url,
        String salary,
        String postedDate,
        String description,
        String category,
        String seniority,
        String workType,
        String employmentType,
        String employmentSchedule,
        boolean techRelated,
        List<String> tags
) {
    public JobPosting {
        tags = tags == null ? List.of() : List.copyOf(tags);
    }

    public JobPosting withClassification(
            String source,
            String seniority,
            String workType,
            boolean techRelated,
            List<String> tags
    ) {
        return new JobPosting(
                title,
                city,
                companyName,
                websiteName,
                source,
                url,
                salary,
                postedDate,
                description,
                category,
                seniority,
                workType,
                employmentType,
                employmentSchedule,
                techRelated,
                tags
        );
    }
}
