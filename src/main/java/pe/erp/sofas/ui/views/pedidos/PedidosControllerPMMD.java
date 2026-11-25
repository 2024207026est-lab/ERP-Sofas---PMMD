package pe.erp.sofas.ui.views.pedidos;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pe.erp.sofas.core.*;
import pe.erp.sofas.model.*;
import pe.erp.sofas.service.*;
import pe.erp.sofas.ui.AppWindowPMMD;
import pe.erp.sofas.ui.components.*;
import pe.erp.sofas.ui.three.Render3DPMMD;
import pe.erp.sofas.ui.three.SofaBuilderPMMD;
import pe.erp.sofas.ui.three.Viewer3DPMMD;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;



@Component
@Lazy
@Scope("prototype")
@RequiredArgsConstructor
public class PedidosControllerPMMD implements VistaPMMD {

    private final PedidoServicePMMD pedidosPMMD;

    private final ClienteServicePMMD clientesPMMD;

    private final ProductoServicePMMD productosPMMD;

    private final TelaServicePMMD telasPMMD;

    private final ProduccionServicePMMD produccionPMMD;

    private final EmpleadoServicePMMD empleadosPMMD;

    private final ExportServicePMMD exportPMMD;

    private final OverlaysPMMD overlaysPMMD;

    private final ToastPMMD toastPMMD;

    private final ConfirmDialogPMMD confirmPMMD;

    private final NavigatorPMMD navigatorPMMD;

    private final AppWindowPMMD appWindowPMMD;

    @FXML private VBox raiz;

    private SearchFieldPMMD buscadorPMMD;

    private ComboBox<EstadoPedidoPMMD> cmbEstadoPMMD;

    private ComboBox<PedidoPMMD.Tipo> cmbTipoPMMD;

    private final VBox listaPMMD = new VBox(8);

    private final VBox detallePMMD = new VBox(14);

    private final Label conteoPMMD = UIPMMD.label("", "cell-sub");

    private Long seleccionPMMD;

    @FXML
    public void initialize() {
        buscadorPMMD = new SearchFieldPMMD("Buscar pedido, cliente o modelo");
        buscadorPMMD.textProperty().addListener((oPMMD, aPMMD, bPMMD) -> renderListaPMMD());
        List<EstadoPedidoPMMD> esPMMD = new ArrayList<>();
        esPMMD.add(null);
        esPMMD.addAll(List.of(EstadoPedidoPMMD.values()));
        cmbEstadoPMMD = FormsPMMD.combo(esPMMD, null, ePMMD -> ePMMD == null ? "Todos los estados" : ePMMD.nombre());
        cmbEstadoPMMD.valueProperty().addListener((oPMMD, aPMMD, bPMMD) -> renderListaPMMD());
        List<PedidoPMMD.Tipo> tsPMMD = new ArrayList<>();
        tsPMMD.add(null);
        tsPMMD.addAll(List.of(PedidoPMMD.Tipo.values()));
        cmbTipoPMMD = FormsPMMD.combo(tsPMMD, null, tPMMD -> tPMMD == null ? "Todos los tipos" : tPMMD.nombre());
        cmbTipoPMMD.valueProperty().addListener((oPMMD, aPMMD, bPMMD) -> renderListaPMMD());
        Button nuevo = UIPMMD.primary("Nuevo pedido", "ADD");
        nuevo.setOnAction(ePMMD -> nuevo(null));
        nuevo.setMaxWidth(Double.MAX_VALUE);
        ScrollPane spLista = UIPMMD.scroll(listaPMMD);
        VBox.setVgrow(spLista, Priority.ALWAYS);
        VBox izq = new VBox(10, nuevo, buscadorPMMD, UIPMMD.hbox(8, cmbEstadoPMMD, cmbTipoPMMD), conteoPMMD, spLista);
        HBox.setHgrow(cmbEstadoPMMD, Priority.ALWAYS);
        HBox.setHgrow(cmbTipoPMMD, Priority.ALWAYS);
        izq.setPrefWidth(380);
        izq.setMinWidth(340);
        izq.setMaxWidth(400);
        ScrollPane spDet = UIPMMD.scroll(detallePMMD);
        HBox.setHgrow(spDet, Priority.ALWAYS);
        HBox cuerpoPMMD = new HBox(16, izq, spDet);
        VBox.setVgrow(cuerpoPMMD, Priority.ALWAYS);
        raiz.getChildren().setAll(cuerpoPMMD);
    }

    @Override
    public void onShow(IntentPMMD intentPMMD) {
        if (intentPMMD.es(IntentPMMD.ACCION_ABRIR) && intentPMMD.dato() != null) {
            pedidosPMMD.porCodigo(intentPMMD.dato().toString()).ifPresent(pPMMD -> seleccionPMMD = pPMMD.getId());
        }
        if (seleccionPMMD == null && !pedidosPMMD.listar().isEmpty()) seleccionPMMD = pedidosPMMD.listar().get(0).getId();
        renderListaPMMD();
        renderDetallePMMD();
        if (intentPMMD.es(IntentPMMD.ACCION_NUEVO)) nuevo(intentPMMD.dato() instanceof ClientePMMD c ? c : null);
    }

    private void seleccionarPMMD(Long idPMMD) {
        seleccionPMMD = idPMMD;
        renderListaPMMD();
        renderDetallePMMD();
    }

    private void renderListaPMMD() {
        String qPMMD = BusquedaServicePMMD.normalizar(buscadorPMMD.getText());
        List<PedidoPMMD> l = pedidosPMMD.listar().stream()
                .filter(pPMMD -> cmbEstadoPMMD.getValue() == null || pPMMD.getEstado() == cmbEstadoPMMD.getValue())
                .filter(pPMMD -> cmbTipoPMMD.getValue() == null || pPMMD.getTipo() == cmbTipoPMMD.getValue())
                .filter(pPMMD -> qPMMD.isEmpty() || BusquedaServicePMMD.normalizar(pPMMD.getCodigo() + " " + pPMMD.getCliente().getNombre() + " " + pPMMD.getProductoNombre()).contains(qPMMD))
                .toList();
        conteoPMMD.setText(l.size() + (l.size() == 1 ? " pedido" : " pedidos") + " · " + pedidosPMMD.pendientesAMedida() + " a medida pendientes");
        listaPMMD.getChildren().clear();
        for (PedidoPMMD pPMMD : l) {
            ProgressBarMiniPMMD pago = new ProgressBarMiniPMMD(pPMMD.getPctPagado(), pPMMD.getPctPagado() >= 1 ? "#16A34A" : pPMMD.getPctPagado() >= 0.5 ? "#2563EB" : "#F59E0B", 5);
            HBox tela = new HBox(6);
            tela.setAlignment(Pos.CENTER_LEFT);
            if (pPMMD.getTela() != null) tela.getChildren().add(new ColorSwatchPMMD(pPMMD.getTela().getHex(), pPMMD.getTela().getTipo(), 14));
            tela.getChildren().add(UIPMMD.label(pPMMD.getProductoNombre() + (pPMMD.getCantidad() > 1 ? " ×" + pPMMD.getCantidad() : ""), "cell-sub"));
            VBox c = new VBox(6,
                    UIPMMD.hbox(6, UIPMMD.codigo(pPMMD.getCodigo()), pPMMD.getTipo() == PedidoPMMD.Tipo.PERSONALIZADO ? new StatusBadgePMMD("Personalizado") : new StatusBadgePMMD("Estándar"),
                            UIPMMD.spacer(), new StatusBadgePMMD(pPMMD.isRetrasado() ? "Retrasado" : pPMMD.getEstado().nombre())),
                    UIPMMD.label(pPMMD.getCliente().getNombre(), "cell-title"), tela,
                    UIPMMD.hbox(6, UIPMMD.label("Entrega " + AppClockPMMD.fmt(pPMMD.getFechaEntrega()), "cell-sub"), UIPMMD.spacer(), UIPMMD.label(MoneyPMMD.fmt0(pPMMD.getPrecio()), "cell-title")),
                    pago);
            c.getStyleClass().add("order-card");
            if (pPMMD.getId().equals(seleccionPMMD)) c.getStyleClass().add("order-card-selected");
            c.setOnMouseClicked(ePMMD -> seleccionarPMMD(pPMMD.getId()));
            listaPMMD.getChildren().add(c);
        }
        if (l.isEmpty()) listaPMMD.getChildren().add(UIPMMD.empty("ASSIGNMENT", "Sin pedidos", "No hay pedidos con estos filtros.", null));
    }



    private void renderDetallePMMD() {
        detallePMMD.getChildren().clear();
        if (seleccionPMMD == null) return;
        PedidoPMMD pPMMD = pedidosPMMD.pedido(seleccionPMMD);
        HBox cabPMMD = UIPMMD.hbox(10, new VBox(2, UIPMMD.hbox(8, UIPMMD.label(pPMMD.getCodigo(), "card-title"), new StatusBadgePMMD(pPMMD.getTipo().nombre()),
                        new StatusBadgePMMD(pPMMD.isRetrasado() ? "Retrasado" : pPMMD.getEstado().nombre())),
                UIPMMD.label(pPMMD.getCliente().getNombre() + " · " + pPMMD.getCliente().getTipoDocumento() + " " + pPMMD.getCliente().getDocumento() + " · " + pPMMD.getCliente().getTelefono(), "cell-sub")),
                UIPMMD.spacer());
        Hyperlink verClientePMMD = new Hyperlink("Ver cliente");
        verClientePMMD.setOnAction(ePMMD -> navigatorPMMD.goTo("clientes", IntentPMMD.abrir(pPMMD.getCliente().getCodigo())));
        cabPMMD.getChildren().add(verClientePMMD);
        detallePMMD.getChildren().add(UIPMMD.card(cabPMMD, timeline(pPMMD), accionesPMMD(pPMMD)));


        GridPane g = UIPMMD.grid(2);
        g.add(UIPMMD.dato("Modelo", pPMMD.getProductoNombre() + (pPMMD.getCantidad() > 1 ? " × " + pPMMD.getCantidad() : "")), 0, 0);
        g.add(UIPMMD.dato("Medidas", pPMMD.getMedidas()), 1, 0);
        HBox tela = new HBox(8);
        tela.setAlignment(Pos.CENTER_LEFT);
        if (pPMMD.getTela() != null) tela.getChildren().addAll(new ColorSwatchPMMD(pPMMD.getTela().getHex(), pPMMD.getTela().getTipo(), 22), UIPMMD.label(pPMMD.getTela().getNombre(), "dato-valor"));
        else tela.getChildren().add(UIPMMD.label("Según el modelo", "dato-valor"));
        g.add(UIPMMD.dato("Material y color", tela), 0, 1);
        g.add(UIPMMD.dato("Fecha del pedido", AppClockPMMD.fmt(pPMMD.getFecha()) + " · entrega " + AppClockPMMD.fmt(pPMMD.getFechaEntrega())), 1, 1);
        VBox esp = new VBox(12, UIPMMD.label("Especificación", "card-title"), g);
        if (pPMMD.getEspecificacion() != null) esp.getChildren().add(UIPMMD.wrap(pPMMD.getEspecificacion(), "muted"));
        if (pPMMD.getObservaciones() != null && !pPMMD.getObservaciones().isBlank()) {
            esp.getChildren().add(UIPMMD.banner("lock-banner", "INFO", pPMMD.getObservaciones()));
        }
        HBox espCardPMMD = new HBox(16, miniaturaPMMD(pPMMD), esp);
        HBox.setHgrow(esp, Priority.ALWAYS);
        detallePMMD.getChildren().add(UIPMMD.card(espCardPMMD));

        detallePMMD.getChildren().add(UIPMMD.columnas(new double[]{1.1, 1}, pagos(pPMMD), ordenVinculadaPMMD(pPMMD)));
    }

    private Node miniaturaPMMD(PedidoPMMD pPMMD) {
        ImageSlotPMMD sPMMD = new ImageSlotPMMD("Foto: " + pPMMD.getProductoNombre(), 260, 175, 12).soloLectura();
        ConfigSofaPMMD cfgPMMD = ConfigSofaPMMD.leer(pPMMD.getConfiguracion());
        if (cfgPMMD != null) {
            TelaPMMD tPMMD = cfgPMMD.telaId() == null ? null : telasPMMD.tela(cfgPMMD.telaId());
            TelaPMMD tcPMMD = cfgPMMD.telaCojinesId() == null ? tPMMD : telasPMMD.tela(cfgPMMD.telaCojinesId());
            sPMMD.mostrar(Viewer3DPMMD.render(new SofaBuilderPMMD().build(SofaBuilderPMMD.Opciones.de(cfgPMMD, tPMMD, tcPMMD)), 520, 350, -28, 16));
        } else if (pPMMD.getProducto() != null) {
            if (pPMMD.getProducto().getImagenPrincipal() == null) sPMMD.respaldo(() -> Render3DPMMD.modelo(pPMMD.getProducto()));
            sPMMD.mostrar(pPMMD.getProducto().getImagenPrincipal());
        }
        return sPMMD;
    }

    private Node timeline(PedidoPMMD pPMMD) {
        HBox h = new HBox();
        h.setAlignment(Pos.TOP_CENTER);
        EstadoPedidoPMMD[] flujoPMMD = EstadoPedidoPMMD.flujo();
        boolean canceladoPMMD = pPMMD.getEstado() == EstadoPedidoPMMD.CANCELADO;
        int actualPMMD = canceladoPMMD ? -1 : Arrays.asList(flujoPMMD).indexOf(pPMMD.getEstado());
        for (int iPMMD = 0; iPMMD < flujoPMMD.length; iPMMD++) {
            EstadoPedidoPMMD ePMMD = flujoPMMD[iPMMD];
            StackPane nodo = new StackPane(IcoPMMD.of(iPMMD < actualPMMD ? "CHECK" : ePMMD.icono(), 17));
            nodo.getStyleClass().add("timeline-node");
            if (iPMMD < actualPMMD) nodo.getStyleClass().add("timeline-done");
            if (iPMMD == actualPMMD) nodo.getStyleClass().add(ePMMD == EstadoPedidoPMMD.ENTREGADO ? "timeline-done" : "timeline-current");
            Label l = UIPMMD.wrap(ePMMD.nombre(), "timeline-label");
            l.setAlignment(Pos.CENTER);
            VBox pasoPMMD = new VBox(6, nodo, l);
            pasoPMMD.setAlignment(Pos.TOP_CENTER);
            pasoPMMD.setPrefWidth(90);
            h.getChildren().add(pasoPMMD);
            if (iPMMD < flujoPMMD.length - 1) {
                Region lineaPMMD = new Region();
                lineaPMMD.getStyleClass().add("timeline-line");
                if (iPMMD < actualPMMD) lineaPMMD.getStyleClass().add("timeline-line-done");
                HBox.setHgrow(lineaPMMD, Priority.ALWAYS);
                lineaPMMD.setTranslateY(18);
                h.getChildren().add(lineaPMMD);
            }
        }
        if (canceladoPMMD) {
            VBox vPMMD = new VBox(8, h, UIPMMD.banner("danger-banner", "CANCEL", "Pedido cancelado."));
            h.setOpacity(0.45);
            return vPMMD;
        }
        return h;
    }

    private Node accionesPMMD(PedidoPMMD pPMMD) {
        FlowPane fPMMD = new FlowPane(8, 8);
        boolean cerradoPMMD = pPMMD.getEstado().cerrado();
        if (!cerradoPMMD && pPMMD.getSaldo().signum() > 0) {
            Button adelPMMD = UIPMMD.secondary(pPMMD.getAdelanto().signum() == 0 ? "Registrar adelanto" : "Registrar pago", "PAYMENTS");
            adelPMMD.setOnAction(ePMMD -> registrarPagoPMMD(pPMMD));
            fPMMD.getChildren().add(adelPMMD);
        }
        if (!cerradoPMMD) {
            EstadoPedidoPMMD sigPMMD = pPMMD.getEstado().siguiente();
            if (sigPMMD == EstadoPedidoPMMD.EN_PRODUCCION && pPMMD.getOpCodigo() == null) {
                Button opPMMD = UIPMMD.button("Generar OP", "BUILD", "btn-wood");
                opPMMD.setOnAction(ePMMD -> generarOP(pPMMD));
                fPMMD.getChildren().add(opPMMD);
            } else if (sigPMMD != pPMMD.getEstado()) {
                Button avPMMD = UIPMMD.primary("Pasar a " + sigPMMD.nombre().toLowerCase(), "ARROW_FORWARD");
                if (sigPMMD == EstadoPedidoPMMD.LISTO && pPMMD.getOpCodigo() != null) {
                    produccionPMMD.porCodigo(pPMMD.getOpCodigo()).filter(opPMMD -> opPMMD.getEtapa() != EtapaPMMD.LISTO).ifPresent(opPMMD -> {
                        avPMMD.setDisable(true);
                        avPMMD.setText("Listo cuando " + opPMMD.getCodigo() + " termine");
                    });
                }
                avPMMD.setOnAction(ePMMD -> {
                    if (toastPMMD.intentar(() -> pedidosPMMD.avanzar(pPMMD.getId()))) {
                        toastPMMD.exito(pPMMD.getCodigo() + " → " + sigPMMD.nombre() + ".");
                        navigatorPMMD.datosCambiaron();
                        seleccionarPMMD(pPMMD.getId());
                    }
                });
                fPMMD.getChildren().add(avPMMD);
            }
            if (pPMMD.getOpCodigo() == null && (pPMMD.getEstado() == EstadoPedidoPMMD.REGISTRADO || pPMMD.getEstado() == EstadoPedidoPMMD.COTIZADO)) {
                Button opPMMD = UIPMMD.button("Generar OP", "BUILD", "btn-ghost");
                opPMMD.setOnAction(ePMMD -> generarOP(pPMMD));
                fPMMD.getChildren().add(opPMMD);
            }
        }
        Button pdfPMMD = UIPMMD.secondary("Cotización PDF", "PICTURE_AS_PDF");
        pdfPMMD.setOnAction(ePMMD -> cotizacionPMMD(pPMMD));
        Button wspPMMD = UIPMMD.secondary("WhatsApp", "CHAT");
        wspPMMD.setOnAction(ePMMD -> whatsapp(pPMMD));
        fPMMD.getChildren().addAll(pdfPMMD, wspPMMD);
        if (!cerradoPMMD) {
            Button cancelarPMMD = UIPMMD.button("Cancelar", "CANCEL", "btn-danger-soft");
            cancelarPMMD.setOnAction(ePMMD -> confirmPMMD.mostrar("¿Cancelar " + pPMMD.getCodigo() + "?",
                    "El pedido de " + pPMMD.getCliente().getNombre() + " quedará cancelado" + (pPMMD.getOpCodigo() != null ? " y la orden " + pPMMD.getOpCodigo() + " se anulará liberando sus reservas." : ".")
                            + (pPMMD.getAdelanto().signum() > 0 ? " Recuerda gestionar la devolución del adelanto de " + MoneyPMMD.fmt(pPMMD.getAdelanto()) + "." : ""),
                    "Cancelar pedido", () -> {
                        if (toastPMMD.intentar(() -> pedidosPMMD.cancelar(pPMMD.getId()))) {
                            toastPMMD.exito(pPMMD.getCodigo() + " cancelado.");
                            navigatorPMMD.datosCambiaron();
                            seleccionarPMMD(pPMMD.getId());
                        }
                    }));
            fPMMD.getChildren().add(cancelarPMMD);
        }
        return fPMMD;
    }

    private Node pagos(PedidoPMMD pPMMD) {
        ProgressBarMiniPMMD barraPMMD = new ProgressBarMiniPMMD(pPMMD.getPctPagado(), pPMMD.getPctPagado() >= 1 ? "#16A34A" : "#2563EB", 8);
        VBox vPMMD = UIPMMD.card(UIPMMD.cardHeader("Pagos", String.format("%.0f %% pagado", pPMMD.getPctPagado() * 100)),
                UIPMMD.hbox(8, UIPMMD.miniStat("Precio", MoneyPMMD.fmt0(pPMMD.getPrecio())), UIPMMD.miniStat("A cuenta", MoneyPMMD.fmt0(pPMMD.getAdelanto())), UIPMMD.miniStat("Saldo", MoneyPMMD.fmt0(pPMMD.getSaldo()))),
                barraPMMD);
        if (!PedidoServicePMMD.cubreMinimo(pPMMD) && !pPMMD.getEstado().cerrado()) {
            vPMMD.getChildren().add(UIPMMD.banner("lock-banner", "INFO", "Para confirmar se necesita un adelanto mínimo del 50 % (" + MoneyPMMD.fmt(pPMMD.getPrecio().multiply(PedidoServicePMMD.MIN_ADELANTO)) + ")."));
        }
        for (PagoPMMD pago : pedidosPMMD.pagos(pPMMD)) {
            vPMMD.getChildren().add(UIPMMD.hbox(8, IcoPMMD.of("PAYMENTS", 16), new VBox(0, UIPMMD.label(pago.getConcepto() + " · " + pago.getMetodo(), "cell-title"),
                    UIPMMD.label(AppClockPMMD.fmt(pago.getFecha()), "cell-sub")), UIPMMD.spacer(), UIPMMD.label(MoneyPMMD.fmt(pago.getMonto()), "cell-title")));
        }
        return vPMMD;
    }

    private Node ordenVinculadaPMMD(PedidoPMMD pPMMD) {
        VBox vPMMD = UIPMMD.card(UIPMMD.cardHeader("Orden de producción", null));
        if (pPMMD.getOpCodigo() == null) {
            vPMMD.getChildren().add(UIPMMD.label("Aún sin orden de producción.", "muted"));
            return vPMMD;
        }
        produccionPMMD.porCodigo(pPMMD.getOpCodigo()).ifPresent(opPMMD -> {
            Hyperlink l = new Hyperlink(opPMMD.getCodigo() + " · " + opPMMD.getProductoNombre());
            l.setOnAction(ePMMD -> navigatorPMMD.goTo("produccion", IntentPMMD.abrir(opPMMD.getCodigo())));
            Region puntoPMMD = new Region();
            puntoPMMD.getStyleClass().add("legend-dot");
            puntoPMMD.setStyle("-fx-background-color: " + opPMMD.getEtapa().color() + ";");
            var indPMMD = produccionPMMD.indicador(opPMMD);
            vPMMD.getChildren().addAll(l, UIPMMD.hbox(8, puntoPMMD, UIPMMD.label(opPMMD.getEtapa().nombre() + " · etapa " + opPMMD.getEtapa().paso() + " de 10", "cell-title")),
                    new ProgressBarMiniPMMD(opPMMD.getEtapa().avance(), opPMMD.getEtapa().color(), 6),
                    UIPMMD.hbox(8, new StatusBadgePMMD(indPMMD.texto(), indPMMD.variante()), UIPMMD.spacer(),
                            UIPMMD.label("Responsable: " + (opPMMD.getResponsable() == null ? "—" : opPMMD.getResponsable().getNombre()), "cell-sub")));
        });
        return vPMMD;
    }



    private void registrarPagoPMMD(PedidoPMMD pPMMD) {
        ValidacionPMMD vPMMD = new ValidacionPMMD();
        BigDecimal minimoPMMD = pPMMD.getPrecio().multiply(PedidoServicePMMD.MIN_ADELANTO).subtract(pPMMD.getAdelanto()).max(BigDecimal.ZERO);
        TextField montoPMMD = FormsPMMD.decimal(minimoPMMD.signum() > 0 ? minimoPMMD : pPMMD.getSaldo());
        ComboBox<String> metodoPMMD = FormsPMMD.combo(List.of("Efectivo", "Tarjeta", "Transferencia", "Yape", "Plin"), "Efectivo", sPMMD -> sPMMD);
        VBox cuerpoPMMD = new VBox(12, UIPMMD.hbox(8, UIPMMD.miniStat("Saldo", MoneyPMMD.fmt(pPMMD.getSaldo())), UIPMMD.miniStat("Para el 50 %", MoneyPMMD.fmt(minimoPMMD))),
                vPMMD.campo("Monto (S/)", montoPMMD, () -> {
                    String ePMMD = ValidacionPMMD.numero(montoPMMD, 0.01, "El monto");
                    if (ePMMD != null) return ePMMD;
                    return ValidacionPMMD.valor(montoPMMD) > pPMMD.getSaldo().doubleValue() + 0.001 ? "Supera el saldo de " + MoneyPMMD.fmt(pPMMD.getSaldo()) + "." : null;
                }), vPMMD.campo("Método de pago", metodoPMMD, null));
        Button cancelarPMMD = UIPMMD.secondary("Cancelar", null);
        Button okPMMD = UIPMMD.primary("Registrar", "PAYMENTS");
        vPMMD.enlazar(okPMMD);
        var h = overlaysPMMD.modal("Registrar pago · " + pPMMD.getCodigo(), pPMMD.getCliente().getNombre(), cuerpoPMMD, 460, cancelarPMMD, okPMMD);
        cancelarPMMD.setOnAction(ePMMD -> h.cerrar());
        okPMMD.setOnAction(ePMMD -> {
            PedidoPMMD[] rPMMD = new PedidoPMMD[1];
            if (toastPMMD.intentar(() -> rPMMD[0] = pedidosPMMD.registrarAdelanto(pPMMD.getId(), FormsPMMD.dinero(montoPMMD), metodoPMMD.getValue()))) {
                h.cerrar();
                toastPMMD.exito("Pago de " + MoneyPMMD.fmt(FormsPMMD.dinero(montoPMMD)) + " registrado" + (rPMMD[0].getEstado() != pPMMD.getEstado() ? " · pedido " + rPMMD[0].getEstado().nombre().toLowerCase() : "") + ".");
                navigatorPMMD.datosCambiaron();
                seleccionarPMMD(pPMMD.getId());
            }
        });
    }

    private void generarOP(PedidoPMMD pPMMD) {
        ComboBox<EmpleadoPMMD> respPMMD = FormsPMMD.combo(empleadosPMMD.taller(), empleadosPMMD.taller().isEmpty() ? null : empleadosPMMD.taller().get(0), EmpleadoPMMD::getNombre);
        Button cancelarPMMD = UIPMMD.secondary("Cancelar", null);
        Button okPMMD = UIPMMD.primary("Generar OP", "BUILD");
        var h = overlaysPMMD.modal("Generar orden de producción", "Usa la ficha aprobada del modelo y pasa el pedido a «En producción»",
                new VBox(12, UIPMMD.wrap(pPMMD.getProductoNombre() + " × " + pPMMD.getCantidad() + (pPMMD.getTela() == null ? "" : " · " + pPMMD.getTela().getNombre())
                        + (pPMMD.getLargo() != null ? " · " + pPMMD.getMedidas() : ""), "muted"), UIPMMD.field("Responsable", respPMMD)), 480, cancelarPMMD, okPMMD);
        cancelarPMMD.setOnAction(ePMMD -> h.cerrar());
        okPMMD.setOnAction(ePMMD -> {
            OrdenProduccionPMMD[] opPMMD = new OrdenProduccionPMMD[1];
            if (toastPMMD.intentar(() -> opPMMD[0] = pedidosPMMD.generarOP(pPMMD.getId(), respPMMD.getValue() == null ? null : respPMMD.getValue().getId()))) {
                h.cerrar();
                toastPMMD.exito(opPMMD[0].getCodigo() + " creada en Pendiente" + (opPMMD[0].isSinFicha() ? " (sin ficha aprobada)" : " con la ficha " + opPMMD[0].getFichaUid().replace("v", " v"))
                        + " · " + pPMMD.getCodigo() + " en producción.");
                navigatorPMMD.datosCambiaron();
                seleccionarPMMD(pPMMD.getId());
            }
        });
    }

    private void cotizacionPMMD(PedidoPMMD pPMMD) {
        File fPMMD = ArchivosPMMD.guardar(detallePMMD, "Cotizacion-" + pPMMD.getCodigo(), "PDF", "pdf");
        if (fPMMD == null) return;
        TareasPMMD.ejecutar(overlaysPMMD, toastPMMD, "Generando cotización…", () -> {
            exportPMMD.cotizacionPedido(pPMMD, fPMMD);
            return fPMMD;
        }, aPMMD -> {
            toastPMMD.exito("Cotización guardada: " + aPMMD.getName());
            appWindowPMMD.hostServices().showDocument(aPMMD.toURI().toString());
        });
    }

    private void whatsapp(PedidoPMMD pPMMD) {
        String msgPMMD = pedidosPMMD.mensajeWhatsapp(pPMMD);
        Label texto = UIPMMD.wrap(msgPMMD);
        VBox burbuja = new VBox(texto);
        burbuja.getStyleClass().add("whatsapp-box");
        Button copiarPMMD = UIPMMD.secondary("Copiar mensaje", "CONTENT_COPY");
        Button enviarPMMD = UIPMMD.button("Enviar por WhatsApp", "SEND", "btn-success");
        Button cerrarPMMD = UIPMMD.secondary("Cerrar", null);
        var h = overlaysPMMD.modal("Enviar por WhatsApp (simulado)", "A " + pPMMD.getCliente().getNombre() + " · " + pPMMD.getCliente().getTelefono(),
                new VBox(12, burbuja, UIPMMD.label("No se envía ningún mensaje real: es una simulación.", "cell-sub")), 520, copiarPMMD, UIPMMD.spacer(), cerrarPMMD, enviarPMMD);
        copiarPMMD.setOnAction(ePMMD -> {
            ClipboardContent ccPMMD = new ClipboardContent();
            ccPMMD.putString(msgPMMD);
            Clipboard.getSystemClipboard().setContent(ccPMMD);
            toastPMMD.exito("Mensaje copiado.");
        });
        cerrarPMMD.setOnAction(ePMMD -> h.cerrar());
        enviarPMMD.setOnAction(ePMMD -> {
            h.cerrar();
            toastPMMD.exito("Mensaje enviado a " + pPMMD.getCliente().getTelefono() + " (simulado).");
        });
    }

    private void nuevo(ClientePMMD preClientePMMD) {
        ValidacionPMMD vPMMD = new ValidacionPMMD();
        ComboBox<ClientePMMD> clientePMMD = FormsPMMD.combo(clientesPMMD.listar(), preClientePMMD, c -> c.getNombre() + " · " + c.getDocumento());
        ComboBox<ProductoPMMD> productoPMMD = FormsPMMD.combo(productosPMMD.listar(), null, pPMMD -> pPMMD.getCodigo() + " · " + pPMMD.getNombre());
        ComboBox<PedidoPMMD.Tipo> tipoPMMD = FormsPMMD.combo(List.of(PedidoPMMD.Tipo.values()), PedidoPMMD.Tipo.PERSONALIZADO, PedidoPMMD.Tipo::nombre);
        Spinner<Integer> cantidadPMMD = new Spinner<>(1, 50, 1);
        cantidadPMMD.setEditable(true);
        cantidadPMMD.setMaxWidth(Double.MAX_VALUE);
        TextField largoPMMD = FormsPMMD.entero((Integer) null), fondoPMMD = FormsPMMD.entero((Integer) null), altoPMMD = FormsPMMD.entero((Integer) null);
        largoPMMD.setPromptText("Largo");
        fondoPMMD.setPromptText("Fondo");
        altoPMMD.setPromptText("Alto");
        List<TelaPMMD> tsPMMD = new ArrayList<>();
        tsPMMD.add(null);
        tsPMMD.addAll(telasPMMD.activas());
        ComboBox<TelaPMMD> tela = FormsPMMD.combo(tsPMMD, null, tPMMD -> tPMMD == null ? "Según el modelo" : tPMMD.getNombre() + " · " + MoneyPMMD.numero(tPMMD.getStock()) + " m");
        DatePicker entregaPMMD = FormsPMMD.fecha(AppClockPMMD.hoy().plusDays(20));
        TextField precioPMMD = FormsPMMD.decimal(BigDecimal.ZERO);
        TextField adelantoPMMD = FormsPMMD.decimal(BigDecimal.ZERO);
        ComboBox<String> metodoPMMD = FormsPMMD.combo(List.of("Efectivo", "Tarjeta", "Transferencia", "Yape", "Plin"), "Yape", sPMMD -> sPMMD);
        TextArea obsPMMD = FormsPMMD.area("", "Observaciones (piso, acceso, horario…)", 2);
        Runnable sugerirPMMD = () -> {
            if (productoPMMD.getValue() == null) return;
            ProductoPMMD pPMMD = productoPMMD.getValue();
            if (largoPMMD.getText().isBlank() && pPMMD.getLargo() != null && pPMMD.getForma() != FormaPMMD.JUEGO) {
                largoPMMD.setText(String.valueOf(pPMMD.getLargo()));
                fondoPMMD.setText(String.valueOf(pPMMD.getFondo()));
                altoPMMD.setText(String.valueOf(pPMMD.getAlto()));
            }
            Integer l = largoPMMD.getText().isBlank() ? null : FormsPMMD.entero(largoPMMD);
            precioPMMD.setText(pedidosPMMD.precioSugerido(pPMMD, l, tela.getValue(), cantidadPMMD.getValue()).toPlainString());
        };
        productoPMMD.valueProperty().addListener((oPMMD, aPMMD, bPMMD) -> {
            largoPMMD.clear();
            sugerirPMMD.run();
        });
        tela.valueProperty().addListener((oPMMD, aPMMD, bPMMD) -> sugerirPMMD.run());
        cantidadPMMD.valueProperty().addListener((oPMMD, aPMMD, bPMMD) -> sugerirPMMD.run());
        largoPMMD.focusedProperty().addListener((oPMMD, aPMMD, bPMMD) -> {
            if (!bPMMD) sugerirPMMD.run();
        });
        FormGridPMMD fPMMD = new FormGridPMMD(3);
        fPMMD.seccion("1", "Cliente y modelo");
        fPMMD.nodo(vPMMD.campo("Cliente", clientePMMD, () -> ValidacionPMMD.requerido(clientePMMD, "el cliente")), 2);
        fPMMD.nodo(vPMMD.campo("Tipo", tipoPMMD, null), 1);
        fPMMD.nodo(vPMMD.campo("Modelo", productoPMMD, () -> ValidacionPMMD.requerido(productoPMMD, "el modelo")), 2);
        fPMMD.nodo(vPMMD.campo("Cantidad", cantidadPMMD, null), 1);
        fPMMD.seccion("2", "Medidas y tela");
        fPMMD.nodo(vPMMD.campo("Largo (cm)", largoPMMD, () -> largoPMMD.getText().isBlank() ? null : ValidacionPMMD.numero(largoPMMD, 40, "El largo")), 1);
        fPMMD.nodo(vPMMD.campo("Fondo (cm)", fondoPMMD, () -> fondoPMMD.getText().isBlank() ? null : ValidacionPMMD.numero(fondoPMMD, 40, "El fondo")), 1);
        fPMMD.nodo(vPMMD.campo("Alto (cm)", altoPMMD, () -> altoPMMD.getText().isBlank() ? null : ValidacionPMMD.numero(altoPMMD, 40, "El alto")), 1);
        fPMMD.nodo(vPMMD.campo("Tela y color", tela, null), 3);
        fPMMD.seccion("3", "Entrega y pago");
        fPMMD.nodo(vPMMD.campo("Fecha de entrega", entregaPMMD, () -> entregaPMMD.getValue() == null || entregaPMMD.getValue().isBefore(AppClockPMMD.hoy()) ? "Elige una fecha futura." : null), 1);
        fPMMD.nodo(vPMMD.campo("Precio (S/)", precioPMMD, () -> ValidacionPMMD.numero(precioPMMD, 1, "El precio")), 1);
        fPMMD.nodo(vPMMD.campo("Adelanto (S/)", adelantoPMMD, () -> {
            String ePMMD = ValidacionPMMD.numero(adelantoPMMD, 0, "El adelanto");
            if (ePMMD != null) return ePMMD;
            return ValidacionPMMD.valor(adelantoPMMD) > ValidacionPMMD.valor(precioPMMD) ? "No puede superar el precio." : null;
        }), 1);
        fPMMD.nodo(vPMMD.campo("Método del adelanto", metodoPMMD, null), 1);
        fPMMD.nodo(vPMMD.campo("Observaciones", obsPMMD, null), 2);
        vPMMD.escuchar(precioPMMD.textProperty());
        Button cancelarPMMD = UIPMMD.secondary("Cancelar", null);
        Button okPMMD = UIPMMD.primary("Registrar pedido", "SAVE");
        vPMMD.enlazar(okPMMD);
        var h = overlaysPMMD.modal("Nuevo pedido", "Precio sugerido según el modelo, el largo y el recargo de la tela", fPMMD, 680, cancelarPMMD, okPMMD);
        cancelarPMMD.setOnAction(ePMMD -> h.cerrar());
        okPMMD.setOnAction(ePMMD -> {
            PedidoPMMD[] rPMMD = new PedidoPMMD[1];
            Integer l = largoPMMD.getText().isBlank() ? null : FormsPMMD.entero(largoPMMD);
            if (toastPMMD.intentar(() -> rPMMD[0] = pedidosPMMD.crear(new PedidoServicePMMD.NuevoPedido(clientePMMD.getValue().getId(), productoPMMD.getValue().getId(),
                    tipoPMMD.getValue(), cantidadPMMD.getValue(), l, l == null ? null : FormsPMMD.entero(fondoPMMD), l == null ? null : FormsPMMD.entero(altoPMMD),
                    tela.getValue() == null ? null : tela.getValue().getId(), entregaPMMD.getValue(), FormsPMMD.dinero(precioPMMD), FormsPMMD.dinero(adelantoPMMD),
                    metodoPMMD.getValue(), obsPMMD.getText(), null, null)))) {
                h.cerrar();
                toastPMMD.exito(rPMMD[0].getCodigo() + " registrado para " + rPMMD[0].getCliente().getNombre() + " · " + rPMMD[0].getEstado().nombre() + ".");
                navigatorPMMD.datosCambiaron();
                seleccionarPMMD(rPMMD[0].getId());
            }
        });
    }
}
