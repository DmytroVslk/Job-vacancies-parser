package main;

import model.AdzunaJobProvider;
import model.JobProvider;
import model.Model;
import view.HtmlView;

public class Aggregator {

    public static void main(String[] args) {
        AppConfig config = AppConfig.fromEnvironment();
        HtmlView view = new HtmlView();

        JobProvider adzunaProvider = new AdzunaJobProvider(
                config.getAdzunaAppId(),
                config.getAdzunaAppKey(),
                config.getAdzunaCountry()
        );
        Model model = new Model(view, adzunaProvider);
        Controller controller = new Controller(model);

        view.setController(controller);

        view.emulateCitySelection();
    }
}
