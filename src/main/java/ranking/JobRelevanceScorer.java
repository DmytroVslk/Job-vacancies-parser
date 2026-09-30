package ranking;

import domain.JobPosting;
import search.JobSearchCriteria;

public interface JobRelevanceScorer {
    int score(JobPosting job, JobSearchCriteria criteria);
}
