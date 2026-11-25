package pe.erp.sofas.ui.components;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import pe.erp.sofas.core.AppClockPMMD;
import pe.erp.sofas.model.OrdenProduccionPMMD;
import pe.erp.sofas.service.ProduccionServicePMMD;






public class KanbanCardPMMD extends VBox {

    public static final String DRAG_PREFIX = "OP:";

    public KanbanCardPMMD(OrdenProduccionPMMD oPMMD, ProduccionServicePMMD.Indicador indPMMD, Runnable abrirPMMD) {
        super(8);
        getStyleClass().add("kanban-card");
        setStyle("-fx-border-color: transparent transparent transparent " + oPMMD.getPrioridad().color() + ";");

        HBox topPMMD = UIPMMD.hbox(6, UIPMMD.label(oPMMD.getCodigo(), "mono", "kc-code"), UIPMMD.spacer(), new StatusBadgePMMD(oPMMD.getPrioridad().nombre()));
        Label productoPMMD = UIPMMD.wrap(oPMMD.getProductoNombre() + "  ×" + oPMMD.getCantidad(), "kc-title");
        Label clientePMMD = UIPMMD.label(oPMMD.getClienteNombre(), "kc-client");
        clientePMMD.setGraphic(IcoPMMD.of(oPMMD.getCliente() == null ? "STOREFRONT" : "PERSON_OUTLINE", 14));

        HBox telaPMMD = new HBox(7);
        telaPMMD.setAlignment(Pos.CENTER_LEFT);
        if (oPMMD.getTela() != null) {
            telaPMMD.getChildren().addAll(new ColorSwatchPMMD(oPMMD.getTela().getHex(), oPMMD.getTela().getTipo(), 16), UIPMMD.label(oPMMD.getTela().getNombre(), "kc-tela"));
        } else {
            telaPMMD.getChildren().add(UIPMMD.label("Tela según ficha", "kc-tela"));
        }

        ProgressBarMiniPMMD barraPMMD = new ProgressBarMiniPMMD(oPMMD.getEtapa().avance(), oPMMD.getEtapa().color(), 6);
        HBox progresoPMMD = UIPMMD.hbox(8, UIPMMD.grow(barraPMMD), UIPMMD.label(oPMMD.getEtapa().paso() + "/10", "kc-pct"));

        Label indicadorPMMD = UIPMMD.label(indPMMD.texto(), "kc-ind", "kc-ind-" + indPMMD.variante());
        indicadorPMMD.setGraphic(IcoPMMD.of(indPMMD.icono(), 13));

        AvatarInitialsPMMD respPMMD = oPMMD.getResponsable() == null ? new AvatarInitialsPMMD("?", 24, "gray")
                : AvatarInitialsPMMD.conColor(oPMMD.getResponsable().getNombre(), 24, oPMMD.getResponsable().getColor() == null ? "#A47148" : oPMMD.getResponsable().getColor());
        Tooltip.install(respPMMD, new Tooltip(oPMMD.getResponsable() == null ? "Sin responsable" : oPMMD.getResponsable().getNombre()));
        Label fechaPMMD = UIPMMD.label(AppClockPMMD.diaMes(oPMMD.getFechaEntrega()), "kc-date");
        fechaPMMD.setGraphic(IcoPMMD.of("EVENT", 14));
        if (oPMMD.isRetrasada()) fechaPMMD.getStyleClass().add("kc-late");
        HBox piePMMD = UIPMMD.hbox(6, respPMMD, UIPMMD.label(oPMMD.getResponsable() == null ? "—" : oPMMD.getResponsable().getNombre().split(" ")[0], "cell-sub"), UIPMMD.spacer(), fechaPMMD);

        getChildren().addAll(topPMMD, productoPMMD, clientePMMD, telaPMMD, progresoPMMD, indicadorPMMD, piePMMD);
        setOnMouseClicked(ePMMD -> abrirPMMD.run());
        setFocusTraversable(true);
        setOnKeyPressed(ePMMD -> {
            if (ePMMD.getCode() == javafx.scene.input.KeyCode.ENTER) abrirPMMD.run();
        });
        setOnDragDetected(ePMMD -> {
            var dbPMMD = startDragAndDrop(TransferMode.MOVE);
            ClipboardContent ccPMMD = new ClipboardContent();
            ccPMMD.putString(DRAG_PREFIX + oPMMD.getId());
            dbPMMD.setContent(ccPMMD);
            dbPMMD.setDragView(snapshot(null, null), ePMMD.getX(), ePMMD.getY());
            getStyleClass().add("dragging");
            ePMMD.consume();
        });
        setOnDragDone(ePMMD -> getStyleClass().remove("dragging"));
    }
}
