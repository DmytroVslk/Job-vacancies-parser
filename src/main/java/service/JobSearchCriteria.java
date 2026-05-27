package service;

public class JobSearchCriteria {

    private final String location;
    private final String position;
    private final String category;
    private final String seniority;
    private final String workType;
    private final String tag;
    private final String preferredSeniority;
    private final String preferredWorkType;
    private final String preferredEmploymentType;
    private final String preferredEmploymentSchedule;
    private final JobSortOption sortOption;

    public JobSearchCriteria(
            String location,
            String position,
            String category,
            String seniority,
            String workType,
            String tag
    ) {
        this(location, position, category, seniority, workType, tag, "", "", "", "", JobSortOption.RELEVANCE);
    }

    public JobSearchCriteria(
            String location,
            String position,
            String category,
            String seniority,
            String workType,
            String tag,
            String preferredSeniority
    ) {
        this(location, position, category, seniority, workType, tag, preferredSeniority, "", "", "", JobSortOption.RELEVANCE);
    }

    public JobSearchCriteria(
            String location,
            String position,
            String category,
            String seniority,
            String workType,
            String tag,
            String preferredSeniority,
            String preferredWorkType
    ) {
        this(location, position, category, seniority, workType, tag,
                preferredSeniority, preferredWorkType, "", "", JobSortOption.RELEVANCE);
    }

    public JobSearchCriteria(
            String location,
            String position,
            String category,
            String seniority,
            String workType,
            String tag,
            String preferredSeniority,
            String preferredWorkType,
            String preferredEmploymentType,
            String preferredEmploymentSchedule
    ) {
        this(location, position, category, seniority, workType, tag,
                preferredSeniority, preferredWorkType, preferredEmploymentType,
                preferredEmploymentSchedule, JobSortOption.RELEVANCE);
    }

    public JobSearchCriteria(
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
            JobSortOption sortOption
    ) {
        this.location = clean(location);
        this.position = clean(position);
        this.category = clean(category);
        this.seniority = clean(seniority);
        this.workType = clean(workType);
        this.tag = clean(tag);
        this.preferredSeniority = clean(preferredSeniority);
        this.preferredWorkType = clean(preferredWorkType);
        this.preferredEmploymentType = clean(preferredEmploymentType);
        this.preferredEmploymentSchedule = clean(preferredEmploymentSchedule);
        this.sortOption = sortOption == null ? JobSortOption.RELEVANCE : sortOption;
    }

    public String getLocation() {
        return location;
    }

    public String getPosition() {
        return position;
    }

    public String getCategory() {
        return category;
    }

    public String getSeniority() {
        return seniority;
    }

    public String getWorkType() {
        return workType;
    }

    public String getTag() {
        return tag;
    }

    public String getPreferredSeniority() {
        return preferredSeniority;
    }

    public String getPreferredWorkType() {
        return preferredWorkType;
    }

    public String getPreferredEmploymentType() {
        return preferredEmploymentType;
    }

    public String getPreferredEmploymentSchedule() {
        return preferredEmploymentSchedule;
    }

    public JobSortOption getSortOption() {
        return sortOption;
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
