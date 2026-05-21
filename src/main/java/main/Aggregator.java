package main;

import model.AdzunaStrategy;
import model.Model;
import model.Provider;
import view.HtmlView;

public class Aggregator {

    public static void main(String[] args) {
        AppConfig config = AppConfig.fromEnvironment();
        HtmlView view = new HtmlView();

        Model model = new Model(
                view,
                new Provider(new AdzunaStrategy(config.getAdzunaAppId(), config.getAdzunaAppKey()))
        );
        Controller controller = new Controller(model);

        view.setController(controller);

        view.emulateCitySelection();
    }
}
