package pe.erp.sofas.ui.components;

import javafx.beans.property.StringProperty;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;



public class SearchFieldPMMD extends HBox {

    private final TextField campo = new TextField();

    public SearchFieldPMMD(String promptPMMD) {
        this(promptPMMD, "SEARCH");
    }

    public SearchFieldPMMD(String promptPMMD, String iconoPMMD) {
        super(8);
        getStyleClass().add("search-field");
        campo.setPromptText(promptPMMD);
        campo.getStyleClass().add("search-input");
        HBox.setHgrow(campo, Priority.ALWAYS);
        Button limpiarPMMD = UIPMMD.iconButton("CLOSE", "Limpiar búsqueda", "btn-ghost", "btn-xs");
        limpiarPMMD.visibleProperty().bind(campo.textProperty().isNotEmpty());
        limpiarPMMD.managedProperty().bind(limpiarPMMD.visibleProperty());
        limpiarPMMD.setOnAction(ePMMD -> {
            campo.clear();
            campo.requestFocus();
        });
        limpiarPMMD.setFocusTraversable(false);
        getChildren().addAll(IcoPMMD.of(iconoPMMD, 18), campo, limpiarPMMD);
        setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        setOnMouseClicked(ePMMD -> campo.requestFocus());
    }

    public StringProperty textProperty() {
        return campo.textProperty();
    }

    public String getText() {
        return campo.getText() == null ? "" : campo.getText().trim();
    }

    public void setText(String tPMMD) {
        campo.setText(tPMMD);
    }

    public TextField campo() {
        return campo;
    }
}
