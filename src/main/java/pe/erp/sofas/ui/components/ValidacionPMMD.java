package pe.erp.sofas.ui.components;

import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;






public class ValidacionPMMD {

    private record Regla(
      Node control, 
        Label error, 
          Supplier<String> mensaje) {
    }

    private final List<Regla> reglasPMMD = new ArrayList<>();

    private final BooleanProperty validoPMMD = new SimpleBooleanProperty(true);

    private boolean tocadoPMMD;



    public VBox campo(String etiquetaPMMD, Node control, Supplier<String> regla) {
        Label error = UIPMMD.label("", "field-error");
        error.setVisible(false);
        error.setManaged(false);
        error.setWrapText(true);
        VBox vPMMD = new VBox(5, UIPMMD.label(etiquetaPMMD, "form-label"), control, error);
        if (control instanceof Region r) r.setMaxWidth(Double.MAX_VALUE);
        javafx.scene.layout.HBox.setHgrow(vPMMD, Priority.ALWAYS);
        if (regla != null) agregarPMMD(control, error, regla);
        return vPMMD;
    }

    public ValidacionPMMD regla(Node control, Supplier<String> regla) {
        agregarPMMD(control, null, regla);
        return this;
    }

    private void agregarPMMD(Node control, Label error, Supplier<String> regla) {
        reglasPMMD.add(new Regla(control, error, regla));
        Observable oPMMD = observablePMMD(control);
        if (oPMMD != null) oPMMD.addListener(xPMMD -> {
            tocadoPMMD = true;
            evaluar();
        });
        evaluar();
    }



    public void escuchar(Observable... dependenciasPMMD) {
        for (Observable oPMMD : dependenciasPMMD) oPMMD.addListener(xPMMD -> evaluar());
    }

    private static Observable observablePMMD(Node c) {
        if (c instanceof TextInputControl t) return t.textProperty();
        if (c instanceof ComboBoxBase<?> cb) return cb.valueProperty();
        if (c instanceof ChoiceBox<?> ch) return ch.valueProperty();
        if (c instanceof CheckBox ck) return ck.selectedProperty();
        if (c instanceof Spinner<?> sp) return sp.valueProperty();
        return null;
    }

    public boolean evaluar() {
        boolean okPMMD = true;
        for (Regla r : reglasPMMD) {
            String msgPMMD;
            try {
                msgPMMD = r.mensaje().get();
            } catch (RuntimeException ePMMD) {
                msgPMMD = "Valor inválido.";
            }
            boolean error = msgPMMD != null;
            okPMMD &= !error;
            boolean mostrarPMMD = error && (tocadoPMMD || r.control().getProperties().containsKey("tocado"));
            r.control().getStyleClass().remove("field-invalid");
            if (mostrarPMMD) r.control().getStyleClass().add("field-invalid");
            if (r.error() != null) {
                r.error().setText(mostrarPMMD ? msgPMMD : "");
                r.error().setVisible(mostrarPMMD);
                r.error().setManaged(mostrarPMMD);
            }
        }
        validoPMMD.set(okPMMD);
        return okPMMD;
    }

    public BooleanProperty validoProperty() {
        return validoPMMD;
    }



    public void enlazar(Button guardarPMMD) {
        guardarPMMD.disableProperty().bind(validoPMMD.not());
    }



    public static String requerido(TextInputControl t, String nombrePMMD) {
        return t.getText() == null || t.getText().isBlank() ? "Ingresa " + nombrePMMD + "." : null;
    }

    public static String requerido(ComboBoxBase<?> c, String nombrePMMD) {
        return c.getValue() == null ? "Elige " + nombrePMMD + "." : null;
    }

    public static String numero(TextInputControl t, double minPMMD, String nombrePMMD) {
        try {
            double vPMMD = Double.parseDouble(t.getText().replace(",", "").replace("S/", "").trim());
            return vPMMD < minPMMD ? nombrePMMD + " debe ser " + (minPMMD == 0 ? "cero o más" : "mayor o igual a " + pe.erp.sofas.core.MoneyPMMD.numero(minPMMD)) + "." : null;
        } catch (NumberFormatException | NullPointerException ePMMD) {
            return "Ingresa " + nombrePMMD.toLowerCase() + " válido.";
        }
    }

    public static double valor(TextInputControl t) {
        try {
            return Double.parseDouble(t.getText().replace(",", "").replace("S/", "").trim());
        } catch (NumberFormatException | NullPointerException ePMMD) {
            return 0;
        }
    }
}
