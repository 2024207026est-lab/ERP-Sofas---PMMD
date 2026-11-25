package pe.erp.sofas.ui.components;

import javafx.scene.control.*;
import javafx.util.StringConverter;
import pe.erp.sofas.core.AppClockPMMD;
import pe.erp.sofas.core.MoneyPMMD;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.function.Function;



public final class FormsPMMD {

    private FormsPMMD() {
    }



    public static TextField decimal(BigDecimal valor) {
        TextField tPMMD = new TextField(valor == null ? "" : valor.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString());
        tPMMD.setTextFormatter(new TextFormatter<String>(cPMMD -> cPMMD.getControlNewText().matches("[0-9.,]*") ? cPMMD : null));
        tPMMD.getStyleClass().add("num-field");
        return tPMMD;
    }

    public static TextField decimal(double valor) {
        TextField tPMMD = decimal((BigDecimal) null);
        tPMMD.setText(valor == Math.floor(valor) ? String.valueOf((long) valor) : String.valueOf(valor));
        return tPMMD;
    }

    public static TextField entero(Integer valor) {
        TextField tPMMD = new TextField(valor == null ? "" : String.valueOf(valor));
        tPMMD.setTextFormatter(new TextFormatter<String>(cPMMD -> cPMMD.getControlNewText().matches("\\d{0,7}") ? cPMMD : null));
        return tPMMD;
    }

    public static TextField digitos(String valor, int maxPMMD) {
        TextField tPMMD = new TextField(valor == null ? "" : valor);
        tPMMD.setTextFormatter(new TextFormatter<String>(cPMMD -> cPMMD.getControlNewText().matches("\\d{0," + maxPMMD + "}") ? cPMMD : null));
        return tPMMD;
    }

    public static BigDecimal dinero(TextField tPMMD) {
        return MoneyPMMD.parse(tPMMD.getText());
    }

    public static double numero(TextField tPMMD) {
        BigDecimal bPMMD = MoneyPMMD.parse(tPMMD.getText());
        return bPMMD == null ? 0 : bPMMD.doubleValue();
    }

    public static int entero(TextField tPMMD) {
        try {
            return Integer.parseInt(tPMMD.getText().trim());
        } catch (NumberFormatException ePMMD) {
            return 0;
        }
    }

    public static <T> ComboBox<T> combo(Collection<T> itemsPMMD, T seleccionPMMD, Function<T, String> textoPMMD) {
        ComboBox<T> cPMMD = new ComboBox<>();
        cPMMD.getItems().setAll(itemsPMMD);
        cPMMD.setMaxWidth(Double.MAX_VALUE);
        Function<T, String> seguro = tPMMD -> {
            try {
                return textoPMMD.apply(tPMMD);
            } catch (NullPointerException ePMMD) {
                return "";
            }
        };
        cPMMD.setConverter(new StringConverter<>() {

            @Override
            public String toString(T tPMMD) {
                return seguro.apply(tPMMD);
            }

            @Override
            public T fromString(String sPMMD) {
                return null;
            }
        });

        javafx.util.Callback<ListView<T>, ListCell<T>> fabricaPMMD = lvPMMD -> new ListCell<>() {

            @Override
            protected void updateItem(T itemPMMD, boolean emptyPMMD) {
                super.updateItem(itemPMMD, emptyPMMD);
                setText(emptyPMMD && itemPMMD == null && getIndex() < 0 ? seguro.apply(null) : emptyPMMD ? null : seguro.apply(itemPMMD));
            }
        };
        cPMMD.setCellFactory(fabricaPMMD);
        cPMMD.setButtonCell(new ListCell<>() {

            @Override
            protected void updateItem(T itemPMMD, boolean emptyPMMD) {
                super.updateItem(itemPMMD, emptyPMMD);
                setText(seguro.apply(itemPMMD));
            }
        });
        cPMMD.setValue(seleccionPMMD);
        return cPMMD;
    }

    public static DatePicker fecha(LocalDate valor) {
        DatePicker dPMMD = new DatePicker(valor);
        dPMMD.setMaxWidth(Double.MAX_VALUE);
        dPMMD.setConverter(new StringConverter<>() {

            @Override
            public String toString(LocalDate fPMMD) {
                return fPMMD == null ? "" : fPMMD.format(AppClockPMMD.FECHA);
            }

            @Override
            public LocalDate fromString(String sPMMD) {
                try {
                    return sPMMD == null || sPMMD.isBlank() ? null : LocalDate.parse(sPMMD.trim(), AppClockPMMD.FECHA);
                } catch (Exception ePMMD) {
                    return null;
                }
            }
        });
        dPMMD.setPromptText("dd/mm/aaaa");
        return dPMMD;
    }

    public static TextArea area(String valor, String promptPMMD, int filasPMMD) {
        TextArea aPMMD = new TextArea(valor == null ? "" : valor);
        aPMMD.setPromptText(promptPMMD);
        aPMMD.setPrefRowCount(filasPMMD);
        aPMMD.setWrapText(true);
        return aPMMD;
    }

    public static TextField soloLectura(String valor) {
        TextField tPMMD = new TextField(valor);
        tPMMD.setEditable(false);
        tPMMD.setFocusTraversable(false);
        tPMMD.getStyleClass().add("readonly-field");
        return tPMMD;
    }

    public static String txt(TextInputControl tPMMD) {
        return tPMMD.getText() == null ? "" : tPMMD.getText().trim();
    }
}
