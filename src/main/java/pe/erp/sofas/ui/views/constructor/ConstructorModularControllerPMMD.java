package pe.erp.sofas.ui.views.constructor;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pe.erp.sofas.core.IntentPMMD;
import pe.erp.sofas.core.SessionPMMD;
import pe.erp.sofas.core.ToastPMMD;
import pe.erp.sofas.core.VistaPMMD;
import pe.erp.sofas.model.ConfigSofaPMMD;
import pe.erp.sofas.service.ConfiguradorServicePMMD;
import pe.erp.sofas.service.GestorCarritoServicePMMD;
import pe.erp.sofas.service.PersistenciaConfiguracionServicePMMD;
import pe.erp.sofas.service.TelaServicePMMD;
import pe.erp.sofas.ui.components.UIPMMD;
import pe.erp.sofas.ui.three.SofaBuilderPMMD;
import pe.erp.sofas.ui.three.Viewer3DPMMD;
import pe.erp.sofas.ui.three.parts.SofaPartAssemblerPMMD;
import pe.erp.sofas.ui.three.parts.families.FamiliaFactoryPMMD;
import pe.erp.sofas.ui.three.parts.families.SofaFamiliaPMMD;

import java.util.ArrayList;
import java.util.List;
















@Component
@Lazy
@Scope("prototype")
@RequiredArgsConstructor
public class ConstructorModularControllerPMMD implements VistaPMMD.ConCiclo {

    private final ConfiguradorServicePMMD configuradorPMMD;

    private final TelaServicePMMD telasPMMD;

    private final ToastPMMD toastPMMD;

    private final GestorCarritoServicePMMD gestorCarritoPMMD;

    private final PersistenciaConfiguracionServicePMMD persistenciaPMMD;

    private final SessionPMMD sessionPMMD;

    private final FamiliaFactoryPMMD familiaFactoryPMMD = new FamiliaFactoryPMMD();

    private final SofaBuilderPMMD builderPMMD = new SofaBuilderPMMD();

    private VBox raizPMMD;

    private Viewer3DPMMD visorPMMD;

    private ListView<String> listaModulosPMMD;

    private List<String> modulosActualesPMMD = new ArrayList<>();

    private void inicializarPMMD() {
        raizPMMD = new VBox(10);
        raizPMMD.setPadding(new Insets(12));


        Label tituloPMMD = UIPMMD.label("Constructor Modular", "title");
        tituloPMMD.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");


        HBox contenidoPMMD = new HBox(10);
        VBox.setVgrow(contenidoPMMD, Priority.ALWAYS);


        visorPMMD = new Viewer3DPMMD(true);
        visorPMMD.setMinSize(400, 500);
        HBox.setHgrow(visorPMMD, Priority.ALWAYS);


        VBox panelControlPMMD = crearPanelControlPMMD();
        panelControlPMMD.setPrefWidth(300);

        contenidoPMMD.getChildren().addAll(visorPMMD, panelControlPMMD);

        raizPMMD.getChildren().addAll(tituloPMMD, contenidoPMMD);
    }





    private VBox crearPanelControlPMMD() {
        VBox panelPMMD = new VBox(10);
        panelPMMD.setPadding(new Insets(10));
        panelPMMD.setStyle("-fx-border-color: #ddd; -fx-border-radius: 4; -fx-background-color: #fafafa;");


        Label tituloPMMD = UIPMMD.label("Módulos", "subtitle");
        tituloPMMD.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");


        VBox paletaPMMD = crearPaletaModulosPMMD();


        Separator sep1PMMD = new Separator();


        Label tituloListaPMMD = UIPMMD.label("Tu armado", "subtitle");
        tituloListaPMMD.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");


        listaModulosPMMD = new ListView<>();
        listaModulosPMMD.setPrefHeight(200);
        listaModulosPMMD.setStyle("-fx-font-size: 11px;");
        VBox.setVgrow(listaModulosPMMD, Priority.ALWAYS);


        VBox botonesPMMD = new VBox(5);

        HBox botonesTopPMMD = new HBox(5);
        Button btnLimpiarPMMD = UIPMMD.button("Limpiar", null, "btn-sm");
        btnLimpiarPMMD.setOnAction(ePMMD -> {
            modulosActualesPMMD.clear();
            listaModulosPMMD.getItems().clear();
            actualizarVisorPMMD();
            toastPMMD.advertencia("Armado limpiado");
        });

        Button btnPrevisualizarPlantaPMMD = UIPMMD.button("Vista planta", null, "btn-sm");
        btnPrevisualizarPlantaPMMD.setOnAction(ePMMD -> {
            toastPMMD.info("Vista en planta: " + calcularDimensionesPMMD());
        });

        botonesTopPMMD.getChildren().addAll(btnLimpiarPMMD, btnPrevisualizarPlantaPMMD);

        Button btnGuardarCarritoPMMD = UIPMMD.button("Guardar al carrito", null, "btn-primary");
        btnGuardarCarritoPMMD.setMaxWidth(Double.MAX_VALUE);
        btnGuardarCarritoPMMD.setOnAction(ePMMD -> guardarYAgregarAlCarritoPMMD());

        botonesPMMD.getChildren().addAll(botonesTopPMMD, btnGuardarCarritoPMMD);

        panelPMMD.getChildren().addAll(tituloPMMD, paletaPMMD, sep1PMMD, tituloListaPMMD, listaModulosPMMD, botonesPMMD);
        return panelPMMD;
    }





    private VBox crearPaletaModulosPMMD() {
        VBox paletaPMMD = new VBox(5);
        paletaPMMD.setStyle("-fx-border-color: #eee; -fx-border-radius: 4; -fx-padding: 8; -fx-background-color: white;");

        String[] modulosPMMD = {
            "Brazo izq + 1 cuerpo",
            "Central (sin brazos)",
            "Esquinero",
            "Chaise",
            "Puff",
            "Brazo der + 1 cuerpo"
        };

        for (String moduloPMMD : modulosPMMD) {
            Button btnPMMD = UIPMMD.button(moduloPMMD, null, "btn-sm");
            btnPMMD.setMaxWidth(Double.MAX_VALUE);
            btnPMMD.setOnAction(ePMMD -> agregarModuloPMMD(moduloPMMD));
            paletaPMMD.getChildren().add(btnPMMD);
        }

        return paletaPMMD;
    }





    private void agregarModuloPMMD(String moduloPMMD) {
        modulosActualesPMMD.add(moduloPMMD);
        if (listaModulosPMMD != null) {
            listaModulosPMMD.getItems().add((listaModulosPMMD.getItems().size() + 1) + ". " + moduloPMMD);
        }
        actualizarVisorPMMD();
        toastPMMD.exito("Agregado: " + moduloPMMD);
    }





    private void actualizarVisorPMMD() {


        try {
            ConfigSofaPMMD configPMMD = configuradorPMMD.defecto(ConfigSofaPMMD.Modelo.MODERNO)
                .conCuerpos(Math.min(4, Math.max(1, modulosActualesPMMD.size())));

            var telaPMMD = configPMMD.telaId() != null ? telasPMMD.tela(configPMMD.telaId()) : null;
            var telaCojinesPMMD = configPMMD.telaCojinesId() != null ? telasPMMD.tela(configPMMD.telaCojinesId()) : null;

            Group sofaGeomPMMD = builderPMMD.build(SofaBuilderPMMD.Opciones.de(configPMMD, telaPMMD, telaCojinesPMMD));
            visorPMMD.setContenido(sofaGeomPMMD, true);
        } catch (Exception ePMMD) {
            toastPMMD.error("Error al actualizar visor: " + ePMMD.getMessage());
        }
    }





    private String calcularDimensionesPMMD() {
        int largoPMMD = modulosActualesPMMD.size() * 52;
        int fondoPMMD = 92;
        return largoPMMD + " × " + fondoPMMD + " cm";
    }

    private void guardarYAgregarAlCarritoPMMD() {
        if (modulosActualesPMMD.isEmpty()) {
            toastPMMD.advertencia("Debes agregar al menos un módulo");
            return;
        }

        try {
            ConfigSofaPMMD configPMMD = configuradorPMMD.defecto(ConfigSofaPMMD.Modelo.MODERNO)
                .conCuerpos(Math.min(4, Math.max(1, modulosActualesPMMD.size())));

            String nombrePMMD = "Constructor - " + modulosActualesPMMD.size() + " módulos";
            var configGuardadaPMMD = persistenciaPMMD.guardar(sessionPMMD.usuario(), configPMMD, nombrePMMD, "");
            gestorCarritoPMMD.agregarAlCarrito(sessionPMMD.usuario(), configGuardadaPMMD.getId(), 1);
            toastPMMD.exito("Configuración guardada al carrito");

            modulosActualesPMMD.clear();
            listaModulosPMMD.getItems().clear();
            actualizarVisorPMMD();
        } catch (Exception ePMMD) {
            toastPMMD.error("Error: " + ePMMD.getMessage());
        }
    }

    @Override
    public void onShow(IntentPMMD intentPMMD) {
        if (raizPMMD == null) {
            inicializarPMMD();
        }

        if (modulosActualesPMMD.isEmpty()) {
            agregarModuloPMMD("Brazo izq + 1 cuerpo");
            agregarModuloPMMD("Central (sin brazos)");
            agregarModuloPMMD("Brazo der + 1 cuerpo");
        }
    }

    @Override
    public void onHide() {
        visorPMMD.detenerGiro();
    }
}
