package pe.erp.sofas.ui.components;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;



public class FormGridPMMD extends VBox {

    private final GridPane gridPMMD = new GridPane();

    private final Label error = UIPMMD.label("", "form-error");

    private final int columnas;

    private int colPMMD;

    private int filaPMMD;

    public FormGridPMMD(int columnas) {
        super(14);
        this.columnas = columnas;
        gridPMMD.setHgap(14);
        gridPMMD.setVgap(14);
        for (int iPMMD = 0; iPMMD < columnas; iPMMD++) {
            ColumnConstraints ccPMMD = new ColumnConstraints();
            ccPMMD.setPercentWidth(100.0 / columnas);
            ccPMMD.setHgrow(Priority.ALWAYS);
            gridPMMD.getColumnConstraints().add(ccPMMD);
        }
        error.setGraphic(IcoPMMD.of("ERROR", 16));
        error.setWrapText(true);
        error.setMaxWidth(Double.MAX_VALUE);
        error.setVisible(false);
        error.setManaged(false);
        getChildren().addAll(error, gridPMMD);
    }

    public FormGridPMMD campo(String etiquetaPMMD, Node controlPMMD) {
        return campo(etiquetaPMMD, controlPMMD, 1);
    }

    public FormGridPMMD campo(String etiquetaPMMD, Node controlPMMD, int spanPMMD) {
        return nodo(UIPMMD.field(etiquetaPMMD, controlPMMD), spanPMMD);
    }

    public FormGridPMMD nodo(Node n, int spanPMMD) {
        if (colPMMD + spanPMMD > columnas) {
            colPMMD = 0;
            filaPMMD++;
        }
        gridPMMD.add(n, colPMMD, filaPMMD, spanPMMD, 1);
        if (n instanceof Region r) r.setMaxWidth(Double.MAX_VALUE);
        colPMMD += spanPMMD;
        if (colPMMD >= columnas) {
            colPMMD = 0;
            filaPMMD++;
        }
        return this;
    }



    public FormGridPMMD seccion(String numeroPMMD, String tituloPMMD) {
        if (colPMMD != 0) {
            colPMMD = 0;
            filaPMMD++;
        }
        Label numPMMD = UIPMMD.label(numeroPMMD, "form-section-num");
        var hPMMD = UIPMMD.hbox(10, numPMMD, UIPMMD.label(tituloPMMD, "form-section-title"));
        if (filaPMMD > 0) hPMMD.setStyle("-fx-padding: 8 0 0 0;");
        gridPMMD.add(hPMMD, 0, filaPMMD++, columnas, 1);
        return this;
    }

    public void error(String mensajePMMD) {
        error.setText(mensajePMMD);
        error.setVisible(true);
        error.setManaged(true);
        UIPMMD.entrar(error);
    }

    public void limpiarError() {
        error.setVisible(false);
        error.setManaged(false);
    }
}
