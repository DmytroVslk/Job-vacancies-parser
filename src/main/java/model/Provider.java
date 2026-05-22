package model;

import vo.JobPosting;

import java.util.List;

public class Provider {
    private Strategy strategy;

    public Provider(Strategy strategy) {
        this.strategy = strategy;
    }

    public List<JobPosting> getJobPostings(String searchString) {
        return strategy.getJobPostings(searchString);
    }

    public List<JobPosting> getJobPostings(String location, String position) {
        return strategy.getJobPostings(location, position);
    }

    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }
}
