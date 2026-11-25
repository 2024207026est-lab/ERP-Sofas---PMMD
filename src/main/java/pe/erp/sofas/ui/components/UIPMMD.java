package pe.erp.sofas.ui.components;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.util.function.Consumer;



public final class UIPMMD {

    private UIPMMD() {
    }

    public static Label label(String texto, String... clasesPMMD) {
        Label l = new Label(texto);
        l.getStyleClass().addAll(clasesPMMD);
        return l;
    }

    public static Label wrap(String texto, String... clasesPMMD) {
        Label l = label(texto, clasesPMMD);
        l.setWrapText(true);
        l.setMinHeight(Region.USE_PREF_SIZE);
        return l;
    }

    public static HBox hbox(double spacing, Node... nodos) {
        HBox hPMMD = new HBox(spacing, nodos);
        hPMMD.setAlignment(Pos.CENTER_LEFT);
        return hPMMD;
    }

    public static VBox vbox(double spacing, Node... nodos) {
        return new VBox(spacing, nodos);
    }

    public static Region spacer() {
        Region r = new Region();
        HBox.setHgrow(r, Priority.ALWAYS);
        VBox.setVgrow(r, Priority.ALWAYS);
        return r;
    }

    public static <T extends Node> T grow(T nodo) {
        HBox.setHgrow(nodo, Priority.ALWAYS);
        VBox.setVgrow(nodo, Priority.ALWAYS);
        if (nodo instanceof Region r) {
            r.setMaxWidth(Double.MAX_VALUE);
        }
        return nodo;
    }

    public static Button button(String texto, String iconoPMMD, String... clasesPMMD) {
        Button bPMMD = new Button(texto);
        if (iconoPMMD != null) bPMMD.setGraphic(IcoPMMD.of(iconoPMMD, 17));
        bPMMD.getStyleClass().add("btn");
        bPMMD.getStyleClass().addAll(clasesPMMD);
        bPMMD.setMnemonicParsing(false);
        bPMMD.setMinWidth(Region.USE_PREF_SIZE);
        return bPMMD;
    }

    public static Button primary(String texto, String iconoPMMD) {
        return button(texto, iconoPMMD, "btn-primary");
    }

    public static Button secondary(String texto, String iconoPMMD) {
        return button(texto, iconoPMMD, "btn-secondary");
    }

    public static Button danger(String texto, String iconoPMMD) {
        return button(texto, iconoPMMD, "btn-danger");
    }

    public static Button success(String texto, String iconoPMMD) {
        return button(texto, iconoPMMD, "btn-success");
    }



    public static Button iconButton(String iconoPMMD, String tooltip, String... clasesPMMD) {
        Button bPMMD = new Button();
        bPMMD.setGraphic(IcoPMMD.of(iconoPMMD, 18));
        bPMMD.getStyleClass().addAll("btn", "btn-icon");
        bPMMD.getStyleClass().addAll(clasesPMMD);
        tooltip(bPMMD, tooltip);
        bPMMD.setAccessibleText(tooltip);
        return bPMMD;
    }

    public static void tooltip(Control cPMMD, String texto) {
        Tooltip t = new Tooltip(texto);
        t.setShowDelay(Duration.millis(250));
        cPMMD.setTooltip(t);
    }

    public static VBox card(Node... contenido) {
        VBox v = new VBox(14, contenido);
        v.getStyleClass().add("card");
        v.setPadding(new Insets(18, 20, 18, 20));
        return v;
    }



    public static HBox cardHeader(String tituloPMMD, String subtituloPMMD, Node... accionesPMMD) {
        VBox textosPMMD = new VBox(2, label(tituloPMMD, "card-title"));
        if (subtituloPMMD != null) textosPMMD.getChildren().add(label(subtituloPMMD, "card-subtitle"));
        HBox hPMMD = hbox(8, textosPMMD, spacer());
        hPMMD.getChildren().addAll(accionesPMMD);
        return hPMMD;
    }



    public static VBox field(String etiquetaPMMD, Node control) {
        VBox v = new VBox(6, label(etiquetaPMMD, "form-label"), control);
        if (control instanceof Region r) r.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(v, Priority.ALWAYS);
        return v;
    }

    public static TextField text(String valor, String promptPMMD) {
        TextField t = new TextField(valor);
        t.setPromptText(promptPMMD);
        return t;
    }

    public static GridPane grid(int columnas) {
        GridPane gPMMD = new GridPane();
        gPMMD.setHgap(14);
        gPMMD.setVgap(14);
        for (int iPMMD = 0; iPMMD < columnas; iPMMD++) {
            ColumnConstraints ccPMMD = new ColumnConstraints();
            ccPMMD.setPercentWidth(100.0 / columnas);
            ccPMMD.setHgrow(Priority.ALWAYS);
            gPMMD.getColumnConstraints().add(ccPMMD);
        }
        return gPMMD;
    }



    public static VBox dato(String etiquetaPMMD, String valor) {
        Label v = wrap(valor == null || valor.isBlank() ? "—" : valor, "dato-valor");
        return new VBox(3, label(etiquetaPMMD, "dato-label"), v);
    }

    public static VBox dato(String etiquetaPMMD, Node valor) {
        return new VBox(3, label(etiquetaPMMD, "dato-label"), valor);
    }

    public static Separator separator() {
        Separator sPMMD = new Separator();
        sPMMD.getStyleClass().add("sep");
        return sPMMD;
    }

    public static ScrollPane scroll(Node contenido) {
        ScrollPane spPMMD = new ScrollPane(contenido);
        spPMMD.setFitToWidth(true);
        spPMMD.getStyleClass().add("page-scroll");
        spPMMD.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return spPMMD;
    }



    public static VBox empty(String iconoPMMD, String tituloPMMD, String texto, Button accionPMMD) {
        StackPane circulo = new StackPane(IcoPMMD.of(iconoPMMD, 26));
        circulo.getStyleClass().add("empty-icon");
        VBox v = new VBox(8, circulo, label(tituloPMMD, "empty-title"), wrap(texto, "empty-text"));
        if (accionPMMD != null) {
            VBox.setMargin(accionPMMD, new Insets(6, 0, 0, 0));
            v.getChildren().add(accionPMMD);
        }
        v.setAlignment(Pos.CENTER);
        v.getStyleClass().add("empty-state");
        v.setMaxWidth(420);
        return v;
    }



    public static void entrar(Node nodo) {
        nodo.setOpacity(0);
        FadeTransition f = new FadeTransition(Duration.millis(240), nodo);
        f.setToValue(1);
        TranslateTransition t = new TranslateTransition(Duration.millis(240), nodo);
        t.setFromY(8);
        t.setToY(0);
        new ParallelTransition(f, t).play();
    }

    public static void onClick(Node nodo, Consumer<javafx.scene.input.MouseEvent> accionPMMD) {
        nodo.setOnMouseClicked(ePMMD -> {
            if (ePMMD.getButton() == javafx.scene.input.MouseButton.PRIMARY) accionPMMD.accept(ePMMD);
        });
        nodo.getStyleClass().add("clickable");
    }

    public static ToggleButton toggle(String texto, ToggleGroup grupoPMMD, String... clasesPMMD) {
        ToggleButton t = new ToggleButton(texto);
        t.setToggleGroup(grupoPMMD);
        t.getStyleClass().addAll(clasesPMMD);
        t.setMnemonicParsing(false);
        return t;
    }



    public static GridPane fila(Node... nodos) {
        GridPane gPMMD = new GridPane();
        gPMMD.setHgap(14);
        gPMMD.setMinHeight(Region.USE_PREF_SIZE);
        for (int iPMMD = 0; iPMMD < nodos.length; iPMMD++) {
            ColumnConstraints ccPMMD = new ColumnConstraints();
            ccPMMD.setPercentWidth(100.0 / nodos.length);
            ccPMMD.setHgrow(Priority.ALWAYS);
            gPMMD.getColumnConstraints().add(ccPMMD);
            gPMMD.add(nodos[iPMMD], iPMMD, 0);
            if (nodos[iPMMD] instanceof Region r) {
                r.setMaxWidth(Double.MAX_VALUE);
                r.setMaxHeight(Double.MAX_VALUE);
            }
            GridPane.setFillHeight(nodos[iPMMD], true);
        }
        return gPMMD;
    }



    public static GridPane columnas(double[] pesosPMMD, Node... nodos) {
        GridPane gPMMD = new GridPane();
        gPMMD.setHgap(14);
        gPMMD.setMinHeight(Region.USE_PREF_SIZE);
        double totalPMMD = 0;
        for (double pPMMD : pesosPMMD) totalPMMD += pPMMD;
        for (int iPMMD = 0; iPMMD < nodos.length; iPMMD++) {
            ColumnConstraints ccPMMD = new ColumnConstraints();
            ccPMMD.setPercentWidth(pesosPMMD[iPMMD] * 100 / totalPMMD);
            ccPMMD.setHgrow(Priority.ALWAYS);
            gPMMD.getColumnConstraints().add(ccPMMD);
            gPMMD.add(nodos[iPMMD], iPMMD, 0);
            if (nodos[iPMMD] instanceof Region r) {
                r.setMaxWidth(Double.MAX_VALUE);
                r.setMaxHeight(Double.MAX_VALUE);
            }
            GridPane.setFillHeight(nodos[iPMMD], true);
            GridPane.setValignment(nodos[iPMMD], javafx.geometry.VPos.TOP);
        }
        return gPMMD;
    }



    public static VBox miniStat(String etiquetaPMMD, String valor) {
        VBox v = new VBox(2, label(valor, "mini-stat-value"), label(etiquetaPMMD, "mini-stat-label"));
        v.getStyleClass().add("mini-stat");
        HBox.setHgrow(v, Priority.ALWAYS);
        v.setMaxWidth(Double.MAX_VALUE);
        return v;
    }



    public static HBox banner(String clasePMMD, String iconoPMMD, String texto) {
        Label l = wrap(texto);
        HBox.setHgrow(l, Priority.ALWAYS);
        HBox hPMMD = new HBox(8, IcoPMMD.of(iconoPMMD, 17), l);
        hPMMD.getStyleClass().add(clasePMMD);
        return hPMMD;
    }

    public static Label codigo(String texto) {
        return label(texto, "code");
    }
}
