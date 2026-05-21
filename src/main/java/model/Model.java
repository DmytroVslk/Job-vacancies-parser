package model;

import view.View;
import vo.JobPosting;

import java.util.ArrayList;
import java.util.List;

public class Model {
    private View view;
    private Provider[] providers;

    public Model(View view, Provider... providers) {
        if (view == null || providers == null || providers.length == 0) {
            throw new IllegalArgumentException();
        }
        this.view = view;
        this.providers = providers;
    }

    public void selectCity(String city) {
        List<JobPosting> vacancies = new ArrayList<>();
        for (Provider provider : providers) {
            vacancies.addAll(provider.getJavaJobPostings(city));
        }
        view.update(vacancies);
    }

    public void selectCities(String[] cities) {
        List<JobPosting> allVacancies = new ArrayList<>();
        for (String city : cities) {
            for (Provider provider : providers) {
                allVacancies.addAll(provider.getJavaJobPostings(city));
            }
        }
        view.update(allVacancies);
    }

    public List<JobPosting> getJobPostings(String city) {
        List<JobPosting> vacancies = new ArrayList<>();
        for (Provider provider : providers) {
            vacancies.addAll(provider.getJavaJobPostings(city));
        }
        return vacancies;
    }

    public List<JobPosting> getJobPostings(String city, String position) {
        List<JobPosting> vacancies = new ArrayList<>();
        for (Provider provider : providers) {
            vacancies.addAll(provider.getJavaJobPostings(city, position));
        }
        return vacancies;
    }
}
