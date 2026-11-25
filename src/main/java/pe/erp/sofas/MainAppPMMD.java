package pe.erp.sofas;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import pe.erp.sofas.ui.AppWindowPMMD;



public class MainAppPMMD extends Application {

    private ConfigurableApplicationContext contextoPMMD;

    @Override
    public void init() {

        this.contextoPMMD = crearContextoSpringPMMD();
    }

    private ConfigurableApplicationContext crearContextoSpringPMMD() {
        return new SpringApplicationBuilder(SofasApplicationPMMD.class)
                .web(WebApplicationType.NONE)
                .headless(false)
                .run(getParameters().getRaw().toArray(String[]::new));
    }

    @Override
    public void start(Stage stagePMMD) {
        contextoPMMD.getBean(AppWindowPMMD.class).iniciar(stagePMMD, getHostServices());
    }

    @Override
    public void stop() {
        contextoPMMD.close();
        Platform.exit();
    }
}
