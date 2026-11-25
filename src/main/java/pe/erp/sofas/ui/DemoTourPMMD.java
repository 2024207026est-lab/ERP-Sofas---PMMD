package pe.erp.sofas.ui;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.util.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import pe.erp.sofas.core.IntentPMMD;
import pe.erp.sofas.core.NavigatorPMMD;
import pe.erp.sofas.core.SessionPMMD;
import pe.erp.sofas.core.ThemeManagerPMMD;
import pe.erp.sofas.service.AuthServicePMMD;
import pe.erp.sofas.ui.components.SnapshotPMMD;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;






@Component
@Lazy
@RequiredArgsConstructor
@Slf4j
public class DemoTourPMMD {

    private static final String PASOSPMMD = "login,dashboard,modelos,telas,configurador,sala,produccion,fichas,mrp,corte,consumo,"
            + "pedidos,ventas,clientes,posventa,inventario,proveedores,compras,personal,finanzas,reportes,configuracion,theme:dark,dashboard,produccion";

    private final AppWindowPMMD appWindowPMMD;

    private final AuthServicePMMD authServicePMMD;

    private final SessionPMMD sessionPMMD;

    private final NavigatorPMMD navigatorPMMD;

    private final ThemeManagerPMMD themeManagerPMMD;

    public void ejecutar(String carpeta) {
        File dir = new File(carpeta);
        dir.mkdirs();
        String[] pasosPMMD = System.getProperty("innova.tour.steps", PASOSPMMD).split(",");
        List<Runnable> accionesPMMD = new ArrayList<>();
        int nPMMD = 0;
        for (String pasoPMMD : pasosPMMD) {
            String nombrePMMD = String.format("%02d-%s", ++nPMMD, pasoPMMD.replaceAll("[^a-zA-Z0-9-]", "_"));
            accionesPMMD.add(() -> aplicarPMMD(pasoPMMD.trim()));
            accionesPMMD.add(() -> capturarPMMD(new File(dir, nombrePMMD + ".png")));
        }
        accionesPMMD.add(Platform::exit);
        encadenarPMMD(accionesPMMD, 0, Double.parseDouble(System.getProperty("innova.tour.delay", "1500")));
    }

    private void encadenarPMMD(List<Runnable> accionesPMMD, int iPMMD, double esperaPMMD) {
        if (iPMMD >= accionesPMMD.size()) return;
        PauseTransition pPMMD = new PauseTransition(Duration.millis(iPMMD % 2 == 0 ? 300 : esperaPMMD));
        pPMMD.setOnFinished(ePMMD -> {
            try {
                accionesPMMD.get(iPMMD).run();
            } catch (RuntimeException exPMMD) {
                log.error("Paso del recorrido falló", exPMMD);
            }
            encadenarPMMD(accionesPMMD, iPMMD + 1, esperaPMMD);
        });
        pPMMD.play();
    }

    private void aplicarPMMD(String pasoPMMD) {
        if (pasoPMMD.equals("login")) return;
        if (pasoPMMD.startsWith("theme:")) {
            themeManagerPMMD.setOscuro(pasoPMMD.endsWith("dark"));
            return;
        }
        if (sessionPMMD.usuario() == null) {
            sessionPMMD.iniciar(authServicePMMD.login(AuthServicePMMD.USUARIO_DEMO, "innova2026"));
            appWindowPMMD.mostrarShell("dashboard");
        }
        String[] partesPMMD = pasoPMMD.split(":", 2);
        IntentPMMD intentPMMD = IntentPMMD.NONE;
        if (partesPMMD.length > 1) {
            String accion = partesPMMD[1];
            if (accion.equals("NEW")) intentPMMD = IntentPMMD.NEW;
            else if (accion.startsWith("OPEN=")) intentPMMD = IntentPMMD.abrir(accion.substring(5));
            else if (accion.contains("=")) intentPMMD = new IntentPMMD(accion.substring(0, accion.indexOf('=')), accion.substring(accion.indexOf('=') + 1));
            else intentPMMD = new IntentPMMD(accion, null);
        }
        navigatorPMMD.goTo(partesPMMD[0], intentPMMD);
    }

    private void capturarPMMD(File destinoPMMD) {
        try {
            ImageIO.write(SnapshotPMMD.imagen(appWindowPMMD.scene().snapshot(null)), "png", destinoPMMD);
            log.info("Captura {}", destinoPMMD);
        } catch (IOException ePMMD) {
            log.error("No se pudo guardar la captura", ePMMD);
        }
    }
}
