package pe.erp.sofas.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

import java.io.File;



public class CarpetaBaseDatosPMMD implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment envPMMD, SpringApplication appPMMD) {
        String dir = envPMMD.getProperty("app.db.dir");
        if (dir != null && !dir.isBlank()) {
            new File(dir).mkdirs();
            new File(dir, "images").mkdirs();
        }
    }
}
