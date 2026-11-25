package pe.erp.sofas.core;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import pe.erp.sofas.ui.components.UIPMMD;

import java.util.ArrayDeque;
import java.util.Deque;






@Component
@Lazy
public class OverlaysPMMD {
    


    public final class Handle {

        private final StackPane capa;

        private final Region caja;

        private final boolean panel;

        private Runnable alCerrar;

        private Handle(StackPane capa, Region caja, boolean panel) {
            this.capa = capa;
            this.caja = caja;
            this.panel = panel;
        }

        public void cerrar() {
            if (!abiertosPMMD.remove(this)) return;
            FadeTransition fPMMD = new FadeTransition(Duration.millis(160), capa);
            fPMMD.setToValue(0);
            fPMMD.setOnFinished(ePMMD -> raiz.getChildren().remove(capa));
            fPMMD.play();
            if (alCerrar != null) alCerrar.run();
        }

        public Handle alCerrar(Runnable rPMMD) {
            this.alCerrar = rPMMD;
            return this;
        }

        public Region caja() {
            return caja;
        }
    }

    private StackPane raiz;

    private final Deque<Handle> abiertosPMMD = new ArrayDeque<>();

    public void registrar(StackPane raiz) {
        this.raiz = raiz;
        raiz.addEventFilter(KeyEvent.KEY_PRESSED, ePMMD -> {
            if (ePMMD.getCode() == KeyCode.ESCAPE && !abiertosPMMD.isEmpty()) {
                abiertosPMMD.peek().cerrar();
                ePMMD.consume();
            }
        });
    }

    public boolean hayAbiertos() {
        return !abiertosPMMD.isEmpty();
    }

    public void cerrarTodos() {
        while (!abiertosPMMD.isEmpty()) abiertosPMMD.peek().cerrar();
    }





    public Handle modal(String tituloPMMD, String subtituloPMMD, Node cuerpo, double anchoPMMD, Node... piePMMD) {
        VBox caja = new VBox();
        caja.getStyleClass().add("modal");
        caja.setMaxWidth(anchoPMMD);
        caja.setPrefWidth(anchoPMMD);
        caja.setMaxHeight(Region.USE_PREF_SIZE);

        Handle[] refPMMD = new Handle[1];
        if (tituloPMMD != null) {
            VBox textosPMMD = new VBox(3, UIPMMD.label(tituloPMMD, "modal-title"));
            if (subtituloPMMD != null) textosPMMD.getChildren().add(UIPMMD.label(subtituloPMMD, "modal-subtitle"));
            Button xPMMD = UIPMMD.iconButton("CLOSE", "Cerrar (Esc)", "btn-ghost");
            xPMMD.setOnAction(ePMMD -> refPMMD[0].cerrar());
            HBox headerPMMD = UIPMMD.hbox(12, textosPMMD, UIPMMD.spacer(), xPMMD);
            headerPMMD.getStyleClass().add("modal-header");
            caja.getChildren().add(headerPMMD);
        }
        ScrollPane sp = new ScrollPane(cuerpo);
        sp.setFitToWidth(true);
        sp.getStyleClass().add("modal-scroll");
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox bodyPMMD = new VBox(sp);
        bodyPMMD.getStyleClass().add("modal-body");
        VBox.setVgrow(sp, Priority.ALWAYS);
        caja.getChildren().add(bodyPMMD);
        if (piePMMD.length > 0) {
            HBox footerPMMD = UIPMMD.hbox(10);
            footerPMMD.setAlignment(Pos.CENTER_RIGHT);
            footerPMMD.getChildren().addAll(piePMMD);
            footerPMMD.getStyleClass().add("modal-footer");
            caja.getChildren().add(footerPMMD);
        }

        sp.prefViewportHeightProperty().bind(javafx.beans.binding.Bindings.createDoubleBinding(
                () -> Math.min(Math.max(cuerpo.prefHeight(anchoPMMD - 48), cuerpo.getLayoutBounds().getHeight()),
                        raiz.getHeight() * 0.86 - 150), raiz.heightProperty(), cuerpo.layoutBoundsProperty()));

        StackPane capa = new StackPane(caja);
        capa.getStyleClass().add("overlay-backdrop");
        capa.setPadding(new Insets(24));
        StackPane.setAlignment(caja, Pos.CENTER);
        refPMMD[0] = abrirPMMD(capa, caja, false);

        caja.setScaleX(0.97);
        caja.setScaleY(0.97);
        caja.setTranslateY(8);
        ScaleTransition s = new ScaleTransition(Duration.millis(220), caja);
        s.setToX(1);
        s.setToY(1);
        TranslateTransition t = new TranslateTransition(Duration.millis(220), caja);
        t.setToY(0);
        new ParallelTransition(s, t).play();
        return refPMMD[0];
    }



    public Handle cargando(String mensajePMMD) {
        javafx.scene.control.ProgressIndicator pi = new javafx.scene.control.ProgressIndicator();
        pi.setPrefSize(36, 36);
        VBox vPMMD = new VBox(14, pi, UIPMMD.label(mensajePMMD, "card-title"));
        vPMMD.setAlignment(Pos.CENTER);
        vPMMD.setPadding(new Insets(18, 10, 10, 10));
        return modal(null, null, vPMMD, 320);
    }



    public Handle panel(Node contenido, double anchoPMMD) {
        VBox caja = new VBox(contenido);
        VBox.setVgrow(contenido, Priority.ALWAYS);
        caja.getStyleClass().add("side-panel");
        caja.setPrefWidth(anchoPMMD);
        caja.setMaxWidth(anchoPMMD);
        caja.setMaxHeight(Double.MAX_VALUE);

        StackPane capa = new StackPane(caja);
        capa.getStyleClass().add("overlay-backdrop");
        StackPane.setAlignment(caja, Pos.CENTER_RIGHT);
        Handle hPMMD = abrirPMMD(capa, caja, true);
        capa.setOnMousePressed(ePMMD -> {
            if (ePMMD.getTarget() == capa) hPMMD.cerrar();
        });
        caja.setTranslateX(anchoPMMD);
        TranslateTransition t = new TranslateTransition(Duration.millis(260), caja);
        t.setToX(0);
        t.setInterpolator(Interpolator.SPLINE(0.2, 0.8, 0.2, 1));
        t.play();
        return hPMMD;
    }



    public Handle panel(String tituloPMMD, String subtituloPMMD, Node cuerpo, HBox piePMMD, double anchoPMMD) {
        VBox textosPMMD = new VBox(3, UIPMMD.label(tituloPMMD, "modal-title"));
        if (subtituloPMMD != null) textosPMMD.getChildren().add(UIPMMD.label(subtituloPMMD, "modal-subtitle"));
        Button xPMMD = UIPMMD.iconButton("CLOSE", "Cerrar (Esc)", "btn-ghost");
        HBox cabeceraPMMD = UIPMMD.hbox(12, textosPMMD, UIPMMD.spacer(), xPMMD);
        cabeceraPMMD.getStyleClass().add("panel-header");
        cuerpo.getStyleClass().add("panel-body");
        Handle hPMMD = panel(panelLayout(cabeceraPMMD, cuerpo, piePMMD), anchoPMMD);
        xPMMD.setOnAction(ePMMD -> hPMMD.cerrar());
        return hPMMD;
    }



    public static VBox panelLayout(Node cabeceraPMMD, Node cuerpo, Node piePMMD) {
        ScrollPane sp = new ScrollPane(cuerpo);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.getStyleClass().add("modal-scroll");
        VBox.setVgrow(sp, Priority.ALWAYS);
        VBox vPMMD = new VBox();
        if (cabeceraPMMD != null) vPMMD.getChildren().add(cabeceraPMMD);
        vPMMD.getChildren().add(sp);
        if (piePMMD != null) {
            piePMMD.getStyleClass().add("panel-footer");
            vPMMD.getChildren().add(piePMMD);
        }
        return vPMMD;
    }

    private Handle abrirPMMD(StackPane capa, Region caja, boolean panel) {
        Handle hPMMD = new Handle(capa, caja, panel);
        capa.setMinSize(0, 0);
        capa.setOpacity(0);
        raiz.getChildren().add(capa);
        abiertosPMMD.push(hPMMD);
        FadeTransition fPMMD = new FadeTransition(Duration.millis(180), capa);
        fPMMD.setToValue(1);
        fPMMD.play();
        capa.requestFocus();
        return hPMMD;
    }
}
