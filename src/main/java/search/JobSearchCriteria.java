package search;

public record JobSearchCriteria(
        String location,
        String position,
        String category,
        String seniority,
        String workType,
        String tag,
        String preferredSeniority,
        String preferredWorkType,
        String preferredEmploymentType,
        String preferredEmploymentSchedule,
        String minimumSalary,
        String postedWithinDays,
        JobSortOption sortOption
){    
}
