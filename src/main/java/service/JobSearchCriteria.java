package service;

public class JobSearchCriteria {

    private final String location;
    private final String position;
    private final String category;
    private final String seniority;
    private final String workType;
    private final String tag;

    public JobSearchCriteria(
            String location,
            String position,
            String category,
            String seniority,
            String workType,
            String tag
    ) {
        this.location = clean(location);
        this.position = clean(position);
        this.category = clean(category);
        this.seniority = clean(seniority);
        this.workType = clean(workType);
        this.tag = clean(tag);
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

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
