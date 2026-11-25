package pe.erp.sofas.ui.components;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;

import java.util.function.Function;



public final class CellsPMMD {

    private CellsPMMD() {
    }



    public static <T> TableColumn<T, String> texto(String tituloPMMD, Function<T, String> valorPMMD, double anchoPMMD, String... clases) {
        TableColumn<T, String> cPMMD = new TableColumn<>(tituloPMMD.toUpperCase(pe.erp.sofas.core.AppClockPMMD.PERU));
        cPMMD.setCellValueFactory(cdPMMD -> new ReadOnlyObjectWrapper<>(valorPMMD.apply(cdPMMD.getValue())));
        cPMMD.setCellFactory(colPMMD -> new TableCell<>() {

            @Override
            protected void updateItem(String itemPMMD, boolean emptyPMMD) {
                super.updateItem(itemPMMD, emptyPMMD);
                setText(emptyPMMD ? null : itemPMMD);
                getStyleClass().removeAll(clases);
                if (!emptyPMMD) getStyleClass().addAll(clases);
            }
        });
        if (anchoPMMD > 0) {
            cPMMD.setPrefWidth(anchoPMMD);
            cPMMD.setMinWidth(Math.min(anchoPMMD, 72));
        }
        return cPMMD;
    }



    public static <T> TableColumn<T, String> derecha(String tituloPMMD, Function<T, String> valorPMMD, double anchoPMMD, String... clases) {
        TableColumn<T, String> cPMMD = texto(tituloPMMD, valorPMMD, anchoPMMD, clases);
        cPMMD.getStyleClass().add("col-right");
        return cPMMD;
    }



    public static <T> TableColumn<T, T> nodo(String tituloPMMD, Function<T, Node> fabrica, double anchoPMMD) {
        TableColumn<T, T> cPMMD = new TableColumn<>(tituloPMMD.toUpperCase(pe.erp.sofas.core.AppClockPMMD.PERU));
        cPMMD.setCellValueFactory(cdPMMD -> new ReadOnlyObjectWrapper<>(cdPMMD.getValue()));
        cPMMD.setCellFactory(colPMMD -> new TableCell<>() {

            @Override
            protected void updateItem(T itemPMMD, boolean emptyPMMD) {
                super.updateItem(itemPMMD, emptyPMMD);
                setText(null);
                setGraphic(emptyPMMD || itemPMMD == null ? null : fabrica.apply(itemPMMD));
            }
        });
        if (anchoPMMD > 0) cPMMD.setPrefWidth(anchoPMMD);
        cPMMD.setSortable(false);
        return cPMMD;
    }

    public static <T> TableColumn<T, T> badge(String tituloPMMD, Function<T, String> estadoPMMD, double anchoPMMD) {
        return nodo(tituloPMMD, tPMMD -> new StatusBadgePMMD(estadoPMMD.apply(tPMMD)), anchoPMMD);
    }



    public static <T> TableColumn<T, T> acciones(String tituloPMMD, Function<T, Node[]> botonesPMMD, double anchoPMMD) {
        TableColumn<T, T> cPMMD = nodo(tituloPMMD, tPMMD -> {
            HBox hPMMD = new HBox(4, botonesPMMD.apply(tPMMD));
            hPMMD.setAlignment(Pos.CENTER_RIGHT);
            return hPMMD;
        }, anchoPMMD);
        cPMMD.getStyleClass().add("col-right");
        return cPMMD;
    }



    public static <T> TableView<T> tabla(String vacioPMMD) {
        TableView<T> tPMMD = new TableView<>();
        tPMMD.getStyleClass().add("is-table");
        tPMMD.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tPMMD.setPlaceholder(UIPMMD.empty("SEARCH", "Sin resultados", vacioPMMD, null));
        return tPMMD;
    }
}
