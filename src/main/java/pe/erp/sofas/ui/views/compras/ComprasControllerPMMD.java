package pe.erp.sofas.ui.views.compras;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pe.erp.sofas.core.*;
import pe.erp.sofas.model.*;
import pe.erp.sofas.service.*;
import pe.erp.sofas.ui.components.*;

import java.math.BigDecimal;
import java.util.*;



@Component
@Lazy
@Scope("prototype")
@RequiredArgsConstructor
public class ComprasControllerPMMD implements VistaPMMD {

    private final CompraServicePMMD comprasPMMD;

    private final ProveedorServicePMMD proveedoresPMMD;

    private final InventarioServicePMMD inventarioPMMD;

    private final TelaServicePMMD telasPMMD;

    private final OverlaysPMMD overlaysPMMD;

    private final ToastPMMD toastPMMD;

    private final ConfirmDialogPMMD confirmPMMD;

    private final NavigatorPMMD navigatorPMMD;

    @FXML private VBox raiz;

    private final VBox kpisPMMD = new VBox();

    private final ChipFilterBarPMMD chipsPMMD = new ChipFilterBarPMMD();

    private final TableView<OrdenCompraPMMD> tablaPMMD = CellsPMMD.tabla("Sin órdenes de compra con este filtro.");

    private OverlaysPMMD.Handle panelPMMD;

    @FXML
    public void initialize() {
        chipsPMMD.seleccionProperty().addListener((oPMMD, aPMMD, bPMMD) -> renderPMMD());
        Button nueva = UIPMMD.primary("Nueva orden de compra", "ADD");
        nueva.setOnAction(ePMMD -> nueva());
        TableColumn<OrdenCompraPMMD, String> total = CellsPMMD.derecha("Total", oPMMD -> MoneyPMMD.fmt(oPMMD.getTotal()), 110, "cell-strong");
        total.setComparator(Comparator.comparing(MoneyPMMD::parse));
        tablaPMMD.getColumns().addAll(List.of(
                CellsPMMD.nodo("Orden", oPMMD -> UIPMMD.hbox(6, UIPMMD.label(oPMMD.getCodigo(), "mono"), Boolean.TRUE.equals(oPMMD.getNueva()) ? new StatusBadgePMMD("Nueva") : new Region()), 150),
                CellsPMMD.texto("Proveedor", oPMMD -> oPMMD.getProveedor().getRazonSocial(), 210, "cell-strong"),
                CellsPMMD.texto("Fecha", oPMMD -> AppClockPMMD.fmt(oPMMD.getFecha()), 90),
                CellsPMMD.texto("Materiales", oPMMD -> oPMMD.getItems().stream().map(iPMMD -> iPMMD.getMaterial().getNombre()).reduce((aPMMD, bPMMD) -> aPMMD + ", " + bPMMD).orElse(""), 260, "cell-muted"),
                CellsPMMD.nodo("Origen", oPMMD -> new StatusBadgePMMD(oPMMD.getOrigen(), oPMMD.getOrigen().startsWith("OP-") ? "wood" : StatusBadgePMMD.variante(oPMMD.getOrigen())), 110),
                total,
                CellsPMMD.badge("Estado", oPMMD -> oPMMD.getEstado() == EstadoCompraPMMD.APROBADA ? "Aprobada OC" : oPMMD.getEstado().nombre(), 100)));
        tablaPMMD.setRowFactory(tvPMMD -> {
            TableRow<OrdenCompraPMMD> rPMMD = new TableRow<>();
            rPMMD.setOnMouseClicked(ePMMD -> {
                if (!rPMMD.isEmpty()) detallePMMD(rPMMD.getItem());
            });
            return rPMMD;
        });
        VBox cardPMMD = new VBox(tablaPMMD);
        VBox.setVgrow(tablaPMMD, Priority.ALWAYS);
        cardPMMD.getStyleClass().addAll("card", "table-card");
        VBox.setVgrow(cardPMMD, Priority.ALWAYS);
        raiz.getChildren().setAll(kpisPMMD, UIPMMD.hbox(10, chipsPMMD, UIPMMD.spacer(), nueva), cardPMMD);
    }

    @Override
    public void onShow(IntentPMMD intentPMMD) {
        renderPMMD();
        if (intentPMMD.es(IntentPMMD.ACCION_ABRIR) && intentPMMD.dato() != null) {
            comprasPMMD.listar().stream().filter(oPMMD -> oPMMD.getCodigo().equals(intentPMMD.dato().toString())).findFirst().ifPresent(this::detallePMMD);
        } else if (intentPMMD.es(IntentPMMD.ACCION_NUEVO)) nueva();
    }

    private void renderPMMD() {
        List<OrdenCompraPMMD> todasPMMD = comprasPMMD.listar();
        List<ChipFilterBarPMMD.Chip> csPMMD = new ArrayList<>();
        csPMMD.add(new ChipFilterBarPMMD.Chip("*", "Todas", todasPMMD.size()));
        for (EstadoCompraPMMD ePMMD : EstadoCompraPMMD.values()) csPMMD.add(new ChipFilterBarPMMD.Chip(ePMMD.name(), ePMMD.nombre(), todasPMMD.stream().filter(oPMMD -> oPMMD.getEstado() == ePMMD).count()));
        chipsPMMD.setChips(csPMMD);
        double transitoPMMD = todasPMMD.stream().filter(oPMMD -> oPMMD.getEstado().enTransito()).mapToDouble(oPMMD -> oPMMD.getTotal().doubleValue()).sum();
        double porPagarPMMD = comprasPMMD.porPagar().stream().mapToDouble(oPMMD -> oPMMD.getTotal().doubleValue()).sum();
        kpisPMMD.getChildren().setAll(UIPMMD.fila(
                new KpiCardPMMD("SHOPPING_CART", "blue", "Órdenes del mes", String.valueOf(todasPMMD.stream().filter(oPMMD -> oPMMD.getFecha().getMonthValue() == AppClockPMMD.hoy().getMonthValue()).count()), "Octubre"),
                new KpiCardPMMD("EDIT", "amber", "Por enviar", String.valueOf(todasPMMD.stream().filter(oPMMD -> oPMMD.getEstado() == EstadoCompraPMMD.BORRADOR).count()), "En borrador"),
                new KpiCardPMMD("LOCAL_SHIPPING", "teal", "En tránsito", MoneyPMMD.fmt0(transitoPMMD), "Solicitadas y aprobadas"),
                new KpiCardPMMD("PAYMENTS", "red", "Por pagar", MoneyPMMD.fmt0(porPagarPMMD), "Aprobadas o recibidas sin pago")));
        String selPMMD = chipsPMMD.getSeleccion();
        tablaPMMD.getItems().setAll(todasPMMD.stream().filter(oPMMD -> selPMMD == null || selPMMD.equals("*") || oPMMD.getEstado().name().equals(selPMMD)).toList());
    }

    private void detallePMMD(OrdenCompraPMMD o0PMMD) {
        comprasPMMD.marcarVista(o0PMMD.getId());
        OrdenCompraPMMD oPMMD = comprasPMMD.orden(o0PMMD.getId());
        if (panelPMMD != null) panelPMMD.cerrar();
        GridPane g = new GridPane();
        g.setHgap(12);
        g.setVgap(4);
        ColumnConstraints c0PMMD = new ColumnConstraints();
        c0PMMD.setHgrow(Priority.ALWAYS);
        g.getColumnConstraints().addAll(c0PMMD, new ColumnConstraints(90), new ColumnConstraints(80), new ColumnConstraints(90));
        g.addRow(0, UIPMMD.label("MATERIAL", "dato-label"), UIPMMD.label("CANTIDAD", "dato-label"), UIPMMD.label("PRECIO", "dato-label"), UIPMMD.label("SUBTOTAL", "dato-label"));
        int iPMMD = 1;
        for (ItemCompraPMMD itPMMD : oPMMD.getItems()) {
            VBox matPMMD = new VBox(0, UIPMMD.label(itPMMD.getMaterial().getNombre(), "cell-title"), UIPMMD.label(itPMMD.getMaterial().getCodigo()
                    + (itPMMD.getTela() != null ? " · color " + itPMMD.getTela().getColor() : ""), "cell-sub"));
            g.addRow(iPMMD++, matPMMD, UIPMMD.label(MoneyPMMD.numero(itPMMD.getCantidad()) + " " + itPMMD.getMaterial().getUnidad(), "cell-sub"),
                    UIPMMD.label(MoneyPMMD.fmt(itPMMD.getPrecioUnitario()), "cell-sub"), UIPMMD.label(MoneyPMMD.fmt(itPMMD.getSubtotal()), "cell-title"));
        }
        VBox totales = new VBox(4,
                UIPMMD.hbox(8, UIPMMD.label("Subtotal", "muted"), UIPMMD.spacer(), UIPMMD.label(MoneyPMMD.fmt(oPMMD.getSubtotal()), "cell-title")),
                UIPMMD.hbox(8, UIPMMD.label("IGV 18 %", "muted"), UIPMMD.spacer(), UIPMMD.label(MoneyPMMD.fmt(oPMMD.getIgv()), "cell-title")),
                UIPMMD.hbox(8, UIPMMD.label("Total", "card-title"), UIPMMD.spacer(), UIPMMD.label(MoneyPMMD.fmt(oPMMD.getTotal()), "price-value")));
        VBox cuerpoPMMD = new VBox(14, UIPMMD.hbox(8, new StatusBadgePMMD(oPMMD.getEstado().nombre()), new StatusBadgePMMD(oPMMD.getOrigen(), "wood"), UIPMMD.spacer(),
                        UIPMMD.label("Emitida " + AppClockPMMD.fmt(oPMMD.getFecha()) + (oPMMD.getFechaRecepcion() != null ? " · recibida " + AppClockPMMD.fmt(oPMMD.getFechaRecepcion()) : ""), "cell-sub")),
                UIPMMD.dato("Proveedor", oPMMD.getProveedor().getRazonSocial() + " · RUC " + oPMMD.getProveedor().getRuc() + " · " + oPMMD.getProveedor().getCondicionPago()), g, totales);
        if (oPMMD.getObservaciones() != null) cuerpoPMMD.getChildren().add(UIPMMD.wrap(oPMMD.getObservaciones(), "muted"));
        if (oPMMD.getFechaPago() != null) cuerpoPMMD.getChildren().add(UIPMMD.banner("ok-banner", "PAYMENTS", "Pagada el " + AppClockPMMD.fmt(oPMMD.getFechaPago()) + "."));
        HBox piePMMD = UIPMMD.hbox(8);
        if (oPMMD.getEstado() != EstadoCompraPMMD.RECIBIDA && oPMMD.getEstado() != EstadoCompraPMMD.CANCELADA) {
            Button cancelarPMMD = UIPMMD.button("Cancelar OC", "CANCEL", "btn-danger-soft");
            cancelarPMMD.setOnAction(ePMMD -> confirmPMMD.mostrar("¿Cancelar " + oPMMD.getCodigo() + "?", "La orden dejará de contar como compra en tránsito.", "Cancelar OC",
                    () -> accionPMMD(() -> comprasPMMD.cancelar(oPMMD.getId()), oPMMD.getCodigo() + " cancelada.")));
            piePMMD.getChildren().add(cancelarPMMD);
        }
        piePMMD.getChildren().add(UIPMMD.spacer());
        switch (oPMMD.getEstado()) {
            case BORRADOR -> {
                Button bPMMD = UIPMMD.primary("Solicitar al proveedor", "SEND");
                bPMMD.setOnAction(ePMMD -> accionPMMD(() -> comprasPMMD.solicitar(oPMMD.getId()), oPMMD.getCodigo() + " solicitada a " + oPMMD.getProveedor().getRazonSocial() + "."));
                piePMMD.getChildren().add(bPMMD);
            }
            case SOLICITADA -> {
                Button bPMMD = UIPMMD.primary("Aprobar", "VERIFIED");
                bPMMD.setOnAction(ePMMD -> accionPMMD(() -> comprasPMMD.aprobar(oPMMD.getId()), oPMMD.getCodigo() + " aprobada."));
                piePMMD.getChildren().add(bPMMD);
            }
            case APROBADA -> {
                Button bPMMD = UIPMMD.success("Recibir en almacén", "MOVE_TO_INBOX");
                bPMMD.setOnAction(ePMMD -> confirmPMMD.mostrar(ConfirmDialogPMMD.Tipo.INFO, "¿Recibir " + oPMMD.getCodigo() + "?",
                        "Se sumarán " + oPMMD.getItems().size() + " materiales al stock y se registrará una Entrada por línea en el kardex.", "Recibir",
                        () -> accionPMMD(() -> comprasPMMD.recibir(oPMMD.getId()), oPMMD.getCodigo() + " recibida · " + oPMMD.getItems().size() + " materiales ingresaron al almacén.")));
                piePMMD.getChildren().add(bPMMD);
            }
            default -> {
            }
        }
        if ((oPMMD.getEstado() == EstadoCompraPMMD.APROBADA || oPMMD.getEstado() == EstadoCompraPMMD.RECIBIDA) && oPMMD.getFechaPago() == null) {
            Button pagoPMMD = UIPMMD.secondary("Registrar pago", "PAYMENTS");
            pagoPMMD.setOnAction(ePMMD -> accionPMMD(() -> comprasPMMD.registrarPago(oPMMD.getId()), "Pago de " + oPMMD.getCodigo() + " registrado."));
            piePMMD.getChildren().add(pagoPMMD);
        }
        panelPMMD = overlaysPMMD.panel(oPMMD.getCodigo(), oPMMD.getProveedor().getRazonSocial(), cuerpoPMMD, piePMMD, 560);
    }

    private void accionPMMD(Runnable rPMMD, String okPMMD) {
        if (toastPMMD.intentar(rPMMD)) {
            toastPMMD.exito(okPMMD);
            if (panelPMMD != null) panelPMMD.cerrar();
            navigatorPMMD.datosCambiaron();
            renderPMMD();
        }
    }



    private final class Fila {
        
        final ComboBox<MaterialPMMD> material = FormsPMMD.combo(inventarioPMMD.materiales(), null, mPMMD -> mPMMD.getCodigo() + " · " + mPMMD.getNombre());
        
        final ComboBox<TelaPMMD> color = FormsPMMD.combo(List.of(), null, TelaPMMD::getColor);
        
        final TextField cantidad = FormsPMMD.decimal(10);
        
        final TextField precio = FormsPMMD.decimal(BigDecimal.ZERO);
        
        final HBox nodo;

        Fila(VBox contenedorPMMD, Runnable cambioPMMD) {
            color.setPromptText("Color");
            color.setDisable(true);
            color.setPrefWidth(140);
            cantidad.setPrefWidth(80);
            precio.setPrefWidth(90);
            material.valueProperty().addListener((oPMMD, aPMMD, bPMMD) -> {
                if (bPMMD == null) return;
                precio.setText(String.valueOf(bPMMD.getCosto()));
                color.setDisable(!bPMMD.isTela());
                color.getItems().setAll(bPMMD.isTela() ? telasPMMD.porTipo(TipoTelaPMMD.porMaterial(bPMMD.getCodigo())).stream().filter(tPMMD -> !tPMMD.getDescontinuada()).toList() : List.of());
                cambioPMMD.run();
            });
            cantidad.textProperty().addListener((oPMMD, aPMMD, bPMMD) -> cambioPMMD.run());
            precio.textProperty().addListener((oPMMD, aPMMD, bPMMD) -> cambioPMMD.run());
            Button quitarPMMD = UIPMMD.iconButton("DELETE_OUTLINE", "Quitar", "btn-ghost", "btn-xs");
            nodo = UIPMMD.hbox(8, material, color, cantidad, precio, quitarPMMD);
            HBox.setHgrow(material, Priority.ALWAYS);
            quitarPMMD.setOnAction(ePMMD -> {
                contenedorPMMD.getChildren().remove(nodo);
                filasPMMD.remove(this);
                cambioPMMD.run();
            });
        }
    }

    private final List<Fila> filasPMMD = new ArrayList<>();

    private void nueva() {
        filasPMMD.clear();
        ComboBox<ProveedorPMMD> proveedorPMMD = FormsPMMD.combo(proveedoresPMMD.activos(), null, ProveedorPMMD::getRazonSocial);
        VBox lineas = new VBox(8);
        Label total = UIPMMD.label("", "price-value");
        CheckBox solicitar = new CheckBox("Enviar la solicitud al proveedor al guardar");
        Runnable recalcular = () -> {
            double sPMMD = filasPMMD.stream().mapToDouble(fPMMD -> ValidacionPMMD.valor(fPMMD.cantidad) * ValidacionPMMD.valor(fPMMD.precio)).sum();
            total.setText("Subtotal " + MoneyPMMD.fmt(sPMMD) + " · total con IGV " + MoneyPMMD.fmt(sPMMD * 1.18));
        };
        Runnable agregarPMMD = () -> {
            Fila fPMMD = new Fila(lineas, recalcular);
            filasPMMD.add(fPMMD);
            lineas.getChildren().add(fPMMD.nodo);
            recalcular.run();
        };
        proveedorPMMD.valueProperty().addListener((oPMMD, aPMMD, bPMMD) -> {
            if (bPMMD == null) return;
            List<MaterialPMMD> suyosPMMD = proveedoresPMMD.materiales(bPMMD);
            filasPMMD.forEach(fPMMD -> fPMMD.material.getItems().setAll(suyosPMMD.isEmpty() ? inventarioPMMD.materiales() : suyosPMMD));
        });
        Button mas = UIPMMD.button("Agregar material", "ADD", "btn-sm", "btn-secondary");
        mas.setOnAction(ePMMD -> {
            agregarPMMD.run();
            if (proveedorPMMD.getValue() != null) {
                List<MaterialPMMD> suyosPMMD = proveedoresPMMD.materiales(proveedorPMMD.getValue());
                if (!suyosPMMD.isEmpty()) filasPMMD.get(filasPMMD.size() - 1).material.getItems().setAll(suyosPMMD);
            }
        });
        agregarPMMD.run();
        VBox cuerpoPMMD = new VBox(12, UIPMMD.field("Proveedor", proveedorPMMD),
                UIPMMD.hbox(8, UIPMMD.label("MATERIAL", "dato-label"), UIPMMD.spacer(), UIPMMD.label("COLOR · CANTIDAD · PRECIO S/ (SIN IGV)", "dato-label")), lineas, mas, total, solicitar);
        Button cancelarPMMD = UIPMMD.secondary("Cancelar", null);
        Button okPMMD = UIPMMD.primary("Crear orden", "SAVE");
        var hPMMD = overlaysPMMD.modal("Nueva orden de compra", "Los precios de compra se registran sin IGV", cuerpoPMMD, 760, cancelarPMMD, okPMMD);
        cancelarPMMD.setOnAction(ePMMD -> hPMMD.cerrar());
        okPMMD.setOnAction(ePMMD -> {
            OrdenCompraPMMD[] ocPMMD = new OrdenCompraPMMD[1];
            if (proveedorPMMD.getValue() == null) {
                toastPMMD.error("Elige el proveedor.");
                return;
            }
            List<CompraServicePMMD.LineaCompra> lsPMMD = filasPMMD.stream().filter(fPMMD -> fPMMD.material.getValue() != null)
                    .map(fPMMD -> new CompraServicePMMD.LineaCompra(fPMMD.material.getValue().getId(), fPMMD.color.getValue() == null ? null : fPMMD.color.getValue().getId(),
                            ValidacionPMMD.valor(fPMMD.cantidad), FormsPMMD.dinero(fPMMD.precio))).toList();
            if (toastPMMD.intentar(() -> ocPMMD[0] = comprasPMMD.crear(proveedorPMMD.getValue().getId(), lsPMMD, solicitar.isSelected(), null, "Manual", true))) {
                hPMMD.cerrar();
                toastPMMD.exito(ocPMMD[0].getCodigo() + " creada en " + ocPMMD[0].getEstado().nombre().toLowerCase() + " por " + MoneyPMMD.fmt(ocPMMD[0].getTotal()) + ".");
                navigatorPMMD.datosCambiaron();
                renderPMMD();
            }
        });
    }
}
