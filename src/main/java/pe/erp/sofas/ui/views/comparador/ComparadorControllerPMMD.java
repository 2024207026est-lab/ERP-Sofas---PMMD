package pe.erp.sofas.ui.views.comparador;

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
import pe.erp.sofas.ui.AppWindowPMMD;
import pe.erp.sofas.ui.components.UIPMMD;
import pe.erp.sofas.ui.three.SofaBuilderPMMD;
import pe.erp.sofas.ui.three.Viewer3DPMMD;

import java.util.ArrayList;
import java.util.List;









@Component
@Lazy
@Scope("prototype")
@RequiredArgsConstructor
public class ComparadorControllerPMMD implements VistaPMMD.ConCiclo {

    private final ConfiguradorServicePMMD configuradorPMMD;

    private final TelaServicePMMD telasPMMD;

    private final ToastPMMD toastPMMD;

    private final AppWindowPMMD appWindowPMMD;

    private final GestorCarritoServicePMMD gestorCarritoPMMD;

    private final PersistenciaConfiguracionServicePMMD persistenciaPMMD;

    private final SessionPMMD sessionPMMD;

    private final SofaBuilderPMMD builderPMMD = new SofaBuilderPMMD();

    private final List<Viewer3DPMMD> visoresPMMD = new ArrayList<>();

    private final ConfigSofaPMMD[] configsPMMD = new ConfigSofaPMMD[3];

    private VBox raizPMMD;

    private Label[] labelConfiguracionesPMMD;

    private void inicializarPMMD() {
        raizPMMD = new VBox(10);
        raizPMMD.setPadding(new Insets(12));
        raizPMMD.setStyle("-fx-background-color: #f5f5f5;");


        Label tituloPMMD = UIPMMD.label("Comparador A/B/C", "title");
        tituloPMMD.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");


        HBox comparadorPMMD = crearComparadorPMMD();
        VBox.setVgrow(comparadorPMMD, Priority.ALWAYS);


        VBox infoPanelPMMD = crearPanelInfoPMMD();

        raizPMMD.getChildren().addAll(tituloPMMD, comparadorPMMD, infoPanelPMMD);
    }





    private HBox crearComparadorPMMD() {
        HBox hboxPMMD = new HBox(10);
        hboxPMMD.setPadding(new Insets(10));
        hboxPMMD.setStyle("-fx-background-color: white; -fx-border-color: #ccc; -fx-border-radius: 8;");

        labelConfiguracionesPMMD = new Label[3];

        for (int iPMMD = 0; iPMMD < 3; iPMMD++) {
            VBox visorPanelPMMD = new VBox(5);
            visorPanelPMMD.setStyle("-fx-border-color: #ddd; -fx-border-radius: 4; -fx-padding: 10;");


            labelConfiguracionesPMMD[iPMMD] = UIPMMD.label("Opción " + (iPMMD + 1), "card");
            labelConfiguracionesPMMD[iPMMD].setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");


            Viewer3DPMMD visorPMMD = new Viewer3DPMMD(true);
            visorPMMD.setMinSize(300, 400);
            visoresPMMD.add(visorPMMD);


            final int indexPMMD = iPMMD;
            visorPMMD.yawProperty().addListener((obsPMMD, oldVPMMD, newVPMMD) -> {
                for (int jPMMD = 0; jPMMD < 3; jPMMD++) {
                    if (jPMMD != indexPMMD) {
                        visoresPMMD.get(jPMMD).yawProperty().setValue(newVPMMD);
                    }
                }
            });
            visorPMMD.pitchProperty().addListener((obsPMMD, oldVPMMD, newVPMMD) -> {
                for (int jPMMD = 0; jPMMD < 3; jPMMD++) {
                    if (jPMMD != indexPMMD) {
                        visoresPMMD.get(jPMMD).pitchProperty().setValue(newVPMMD);
                    }
                }
            });


            Button btnAgregarCarritoPMMD = UIPMMD.button("Agregar al carrito", null, "btn-primary");
            btnAgregarCarritoPMMD.setMaxWidth(Double.MAX_VALUE);
            btnAgregarCarritoPMMD.setOnAction(ePMMD -> agregarAlCarrito(indexPMMD));

            visorPanelPMMD.getChildren().addAll(labelConfiguracionesPMMD[iPMMD], visorPMMD, btnAgregarCarritoPMMD);
            HBox.setHgrow(visorPanelPMMD, Priority.ALWAYS);
            hboxPMMD.getChildren().add(visorPanelPMMD);


            if (iPMMD == 0) {
                configsPMMD[iPMMD] = configuradorPMMD.defecto(ConfigSofaPMMD.Modelo.MODERNO);
            } else {
                configsPMMD[iPMMD] = null;
            }
        }

        return hboxPMMD;
    }





    private VBox crearPanelInfoPMMD() {
        VBox panelPMMD = new VBox(8);
        panelPMMD.setPadding(new Insets(10));
        panelPMMD.setStyle("-fx-border-color: #ddd; -fx-border-radius: 4; -fx-background-color: #fafafa;");

        Label tituloPMMD = UIPMMD.label("Comparativa", "subtitle");
        tituloPMMD.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");


        TableView<ComparisonRow> tablaPMMD = new TableView<>();
        tablaPMMD.setMinHeight(150);
        tablaPMMD.setMaxHeight(300);

        TableColumn<ComparisonRow, String> propiedadColPMMD = new TableColumn<>("Propiedad");
        propiedadColPMMD.setCellValueFactory(paramPMMD -> new javafx.beans.property.SimpleStringProperty(paramPMMD.getValue().propiedad));
        propiedadColPMMD.setPrefWidth(100);

        TableColumn<ComparisonRow, String> opcion1ColPMMD = new TableColumn<>("Opción A");
        opcion1ColPMMD.setCellValueFactory(paramPMMD -> new javafx.beans.property.SimpleStringProperty(paramPMMD.getValue().opcion1));
        opcion1ColPMMD.setPrefWidth(120);

        TableColumn<ComparisonRow, String> opcion2ColPMMD = new TableColumn<>("Opción B");
        opcion2ColPMMD.setCellValueFactory(paramPMMD -> new javafx.beans.property.SimpleStringProperty(paramPMMD.getValue().opcion2));
        opcion2ColPMMD.setPrefWidth(120);

        TableColumn<ComparisonRow, String> opcion3ColPMMD = new TableColumn<>("Opción C");
        opcion3ColPMMD.setCellValueFactory(paramPMMD -> new javafx.beans.property.SimpleStringProperty(paramPMMD.getValue().opcion3));
        opcion3ColPMMD.setPrefWidth(120);

        tablaPMMD.getColumns().addAll(propiedadColPMMD, opcion1ColPMMD, opcion2ColPMMD, opcion3ColPMMD);


        tablaPMMD.getItems().addAll(
            new ComparisonRow("Modelo", "Moderno", "-", "-"),
            new ComparisonRow("Cuerpos", "3", "-", "-"),
            new ComparisonRow("Largo", "210 cm", "-", "-"),
            new ComparisonRow("Tela", "Chenille gris", "-", "-"),
            new ComparisonRow("Precio", "S/ 3,500", "-", "-")
        );

        panelPMMD.getChildren().addAll(tituloPMMD, tablaPMMD);
        return panelPMMD;
    }





    public void cargarConfiguracion(int posicionPMMD, ConfigSofaPMMD configPMMD) {
        if (posicionPMMD < 0 || posicionPMMD >= 3) return;

        configsPMMD[posicionPMMD] = configPMMD;
        var telaPMMD = configPMMD.telaId() != null ? telasPMMD.tela(configPMMD.telaId()) : null;
        var telaCojinesPMMD = configPMMD.telaCojinesId() != null ? telasPMMD.tela(configPMMD.telaCojinesId()) : null;

        Group sofaGeomPMMD = builderPMMD.build(SofaBuilderPMMD.Opciones.de(configPMMD, telaPMMD, telaCojinesPMMD));
        visoresPMMD.get(posicionPMMD).setContenido(sofaGeomPMMD, true);


        labelConfiguracionesPMMD[posicionPMMD].setText(configPMMD.modelo().nombre());
    }

    @Override
    public void onShow(IntentPMMD intentPMMD) {
        if (raizPMMD == null) {
            inicializarPMMD();

            if (configsPMMD[0] != null && labelConfiguracionesPMMD != null) {
                cargarConfiguracion(0, configsPMMD[0]);
            }
        }
    }

    @Override
    public void onHide() {

        visoresPMMD.forEach(Viewer3DPMMD::detenerGiro);
    }

    private void agregarAlCarrito(int posicionPMMD) {
        if (configsPMMD[posicionPMMD] == null) {
            toastPMMD.advertencia("Opción " + (posicionPMMD + 1) + " no configurada");
            return;
        }

        try {
            String nombrePMMD = "Comparador - " + configsPMMD[posicionPMMD].modelo().nombre();
            var configGuardadaPMMD = persistenciaPMMD.guardar(sessionPMMD.usuario(), configsPMMD[posicionPMMD], nombrePMMD, "");
            gestorCarritoPMMD.agregarAlCarrito(sessionPMMD.usuario(), configGuardadaPMMD.getId(), 1);
            toastPMMD.exito("Agregado al carrito: " + nombrePMMD);
        } catch (Exception ePMMD) {
            toastPMMD.error("Error: " + ePMMD.getMessage());
        }
    }





    public static class ComparisonRow {

        public String propiedad;

        public String opcion1;

        public String opcion2;

        public String opcion3;

        public ComparisonRow(String propiedad, String opcion1, String opcion2, String opcion3) {
            this.propiedad = propiedad;
            this.opcion1 = opcion1;
            this.opcion2 = opcion2;
            this.opcion3 = opcion3;
        }
    }
}
