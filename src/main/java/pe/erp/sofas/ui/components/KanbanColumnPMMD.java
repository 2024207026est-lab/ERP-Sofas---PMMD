package pe.erp.sofas.ui.components;

import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import pe.erp.sofas.model.EtapaPMMD;

import java.util.List;
import java.util.function.BiConsumer;



public class KanbanColumnPMMD extends VBox {

    public KanbanColumnPMMD(EtapaPMMD etapaPMMD, List<KanbanCardPMMD> tarjetasPMMD, int sofasPMMD, BiConsumer<Long, EtapaPMMD> alSoltarPMMD) {
        super(10);
        getStyleClass().add("kanban-column");
        setMinWidth(248);
        setPrefWidth(248);
        setMaxWidth(248);

        Region puntoPMMD = new Region();
        puntoPMMD.getStyleClass().add("legend-dot");
        puntoPMMD.setStyle("-fx-background-color: " + etapaPMMD.color() + ";");
        var cabeceraPMMD = UIPMMD.hbox(8, puntoPMMD, IcoPMMD.of(etapaPMMD.icono(), 15), UIPMMD.label(etapaPMMD.nombre(), "kanban-title"), UIPMMD.spacer(),
                UIPMMD.label(String.valueOf(tarjetasPMMD.size()), "kanban-count"));
        cabeceraPMMD.setPadding(new Insets(2, 4, 0, 4));
        var subPMMD = UIPMMD.label(sofasPMMD + (sofasPMMD == 1 ? " sofá" : " sofás"), "cell-sub");
        subPMMD.setPadding(new Insets(0, 4, 2, 22));

        VBox lista = new VBox(10);
        lista.getChildren().addAll(tarjetasPMMD);
        lista.setPadding(new Insets(2, 2, 8, 2));
        if (tarjetasPMMD.isEmpty()) {
            var vacioPMMD = UIPMMD.label("Arrastra una orden aquí", "kanban-empty");
            vacioPMMD.setMaxWidth(Double.MAX_VALUE);
            lista.getChildren().add(vacioPMMD);
        }
        ScrollPane spPMMD = new ScrollPane(lista);
        spPMMD.setFitToWidth(true);
        spPMMD.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        spPMMD.getStyleClass().add("kanban-scroll");
        VBox.setVgrow(spPMMD, Priority.ALWAYS);
        getChildren().addAll(cabeceraPMMD, subPMMD, spPMMD);

        setOnDragOver(ePMMD -> {
            if (ePMMD.getDragboard().hasString() && ePMMD.getDragboard().getString().startsWith(KanbanCardPMMD.DRAG_PREFIX)) ePMMD.acceptTransferModes(TransferMode.MOVE);
            ePMMD.consume();
        });
        setOnDragEntered(ePMMD -> {
            if (ePMMD.getDragboard().hasString()) getStyleClass().add("drop-target");
        });
        setOnDragExited(ePMMD -> getStyleClass().remove("drop-target"));
        setOnDragDropped(ePMMD -> {
            boolean okPMMD = false;
            if (ePMMD.getDragboard().hasString() && ePMMD.getDragboard().getString().startsWith(KanbanCardPMMD.DRAG_PREFIX)) {
                long idPMMD = Long.parseLong(ePMMD.getDragboard().getString().substring(KanbanCardPMMD.DRAG_PREFIX.length()));
                okPMMD = true;

                javafx.application.Platform.runLater(() -> alSoltarPMMD.accept(idPMMD, etapaPMMD));
            }
            ePMMD.setDropCompleted(okPMMD);
            ePMMD.consume();
        });
    }
}
