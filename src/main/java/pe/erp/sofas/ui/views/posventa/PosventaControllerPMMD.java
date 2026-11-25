package pe.erp.sofas.ui.views.posventa;

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
import pe.erp.sofas.service.calculo.CoberturaPMMD;
import pe.erp.sofas.ui.AppWindowPMMD;
import pe.erp.sofas.ui.components.*;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;



@Component
@Lazy
@Scope("prototype")
@RequiredArgsConstructor
public class PosventaControllerPMMD implements VistaPMMD {

    private final RetapizadoServicePMMD retapizadoPMMD;

    private final GarantiaServicePMMD garantiasPMMD;

    private final ClienteServicePMMD clientesPMMD;

    private final ProductoServicePMMD productosPMMD;

    private final TelaServicePMMD telasPMMD;

    private final ImagenServicePMMD imagenesPMMD;

    private final ExportServicePMMD exportPMMD;

    private final OverlaysPMMD overlaysPMMD;

    private final ToastPMMD toastPMMD;

    private final NavigatorPMMD navigatorPMMD;

    private final AppWindowPMMD appWindowPMMD;

    @FXML private VBox raiz;

    private SegmentedControlPMMD tabsPMMD;

    private final Button accionPMMD = UIPMMD.primary("Nuevo retapizado", "ADD");

    private final VBox contenidoPMMD = new VBox(14);

    @FXML
    public void initialize() {
        tabsPMMD = new SegmentedControlPMMD("Retapizado", "Garantías y reclamos");
        tabsPMMD.getStyleClass().add("tabs");
        tabsPMMD.valorProperty().addListener((oPMMD, aPMMD, bPMMD) -> renderPMMD());
        accionPMMD.setOnAction(ePMMD -> {
            if ("Retapizado".equals(tabsPMMD.getValor())) nuevoRetapizadoPMMD();
            else nuevoReclamoPMMD();
        });
        ScrollPane spPMMD = UIPMMD.scroll(contenidoPMMD);
        VBox.setVgrow(spPMMD, Priority.ALWAYS);
        raiz.getChildren().setAll(UIPMMD.hbox(10, tabsPMMD, UIPMMD.spacer(), accionPMMD), spPMMD);
    }

    @Override
    public void onShow(IntentPMMD intentPMMD) {
        if (intentPMMD.dato() != null && intentPMMD.dato().toString().startsWith("GR-")) tabsPMMD.setValor("Garantías y reclamos");
        else if (intentPMMD.dato() != null && intentPMMD.dato().toString().startsWith("RT-")) tabsPMMD.setValor("Retapizado");
        renderPMMD();
        if (intentPMMD.dato() != null && intentPMMD.dato().toString().startsWith("GR-")) {
            garantiasPMMD.listar().stream().filter(gPMMD -> gPMMD.getCodigo().equals(intentPMMD.dato().toString())).findFirst().ifPresent(this::detalleReclamoPMMD);
        }
    }

    private void renderPMMD() {
        boolean retPMMD = "Retapizado".equals(tabsPMMD.getValor());
        accionPMMD.setText(retPMMD ? "Nuevo retapizado" : "Registrar reclamo");
        if (retPMMD) renderRetapizadoPMMD();
        else renderGarantiasPMMD();
    }



    private void renderRetapizadoPMMD() {
        RetapizadoServicePMMD.Kpis kPMMD = retapizadoPMMD.kpis();
        TableView<RetapizadoPMMD> t = CellsPMMD.tabla("Sin retapizados.");
        t.getColumns().addAll(List.of(
                CellsPMMD.texto("Código", RetapizadoPMMD::getCodigo, 64, "cell-mono"),
                CellsPMMD.nodo("Cliente", rPMMD -> UIPMMD.wrap(rPMMD.getCliente().getNombre(), "cell-title"), 140),
                CellsPMMD.nodo("Sofá del cliente", rPMMD -> new VBox(0, UIPMMD.label(rPMMD.getDescripcion(), "cell-title"), UIPMMD.label(rPMMD.getCuerpos() + " cuerpos · " + rPMMD.getMedidas()
                        + " · estructura " + rPMMD.getEstructura().toLowerCase() + " · espuma " + rPMMD.getEspuma().toLowerCase(), "cell-sub")), 280),
                CellsPMMD.nodo("Tela nueva", rPMMD -> rPMMD.getTela() == null ? UIPMMD.label("Por elegir", "cell-sub")
                        : UIPMMD.hbox(6, new ColorSwatchPMMD(rPMMD.getTela().getHex(), rPMMD.getTela().getTipo(), 18), new VBox(0, UIPMMD.label(rPMMD.getTela().getNombre(), "cell-title"),
                        UIPMMD.label(rPMMD.getMetros() == null ? "" : MoneyPMMD.numero(rPMMD.getMetros()) + " m", "cell-sub"))), 180),
                CellsPMMD.nodo("Recojo · entrega", rPMMD -> new VBox(0, UIPMMD.label("Recojo " + AppClockPMMD.fmt(rPMMD.getFechaRecojo()), "cell-sub"),
                        UIPMMD.label("Entrega " + AppClockPMMD.fmt(rPMMD.getFechaEntrega()), "cell-sub")), 130),
                CellsPMMD.derecha("Total", rPMMD -> rPMMD.getTotal() == null ? "—" : MoneyPMMD.fmt0(rPMMD.getTotal()), 76, "cell-strong"),
                CellsPMMD.nodo("Estado", rPMMD -> {
                    VBox vPMMD = new VBox(2, new StatusBadgePMMD(rPMMD.getEstado().nombre()));
                    if (rPMMD.getOpCodigo() != null) {
                        Hyperlink lPMMD = new Hyperlink(rPMMD.getOpCodigo());
                        lPMMD.setOnAction(ePMMD -> navigatorPMMD.goTo("produccion", IntentPMMD.abrir(rPMMD.getOpCodigo())));
                        vPMMD.getChildren().add(lPMMD);
                    }
                    return vPMMD;
                }, 120),
                CellsPMMD.acciones("", rPMMD -> {
                    List<Node> nPMMD = new ArrayList<>();
                    if (rPMMD.getEstado().accion() != null) {
                        Button bPMMD = UIPMMD.button(rPMMD.getEstado().accion(), null, "btn-sm", "btn-secondary");
                        bPMMD.setOnAction(ePMMD -> {
                            String[] mPMMD = new String[1];
                            if (toastPMMD.intentar(() -> mPMMD[0] = retapizadoPMMD.avanzar(rPMMD.getId()))) {
                                toastPMMD.exito(mPMMD[0]);
                                navigatorPMMD.datosCambiaron();
                                renderPMMD();
                            }
                        });
                        nPMMD.add(bPMMD);
                    }
                    Button pdfPMMD = UIPMMD.iconButton("PICTURE_AS_PDF", "Cotización PDF", "btn-ghost", "btn-xs");
                    pdfPMMD.setDisable(rPMMD.getTela() == null);
                    pdfPMMD.setOnAction(ePMMD -> cotizacionPdfPMMD(rPMMD));
                    nPMMD.add(pdfPMMD);
                    return nPMMD.toArray(Node[]::new);
                }, 190)));
        t.getItems().setAll(retapizadoPMMD.listar());
        t.setFixedCellSize(56);
        t.setPrefHeight(56 * Math.max(4, t.getItems().size()) + 44);
        VBox cardPMMD = new VBox(t);
        cardPMMD.getStyleClass().addAll("card", "table-card");
        contenidoPMMD.getChildren().setAll(UIPMMD.fila(
                new KpiCardPMMD("BUILD_CIRCLE", "blue", "Retapizados activos", String.valueOf(kPMMD.activos()), "Aún no entregados"),
                new KpiCardPMMD("BUILD", "violet", "En taller", String.valueOf(kPMMD.enTaller()), "Con orden de producción"),
                new KpiCardPMMD("LOCAL_SHIPPING", "wood", "Recojos programados", String.valueOf(kPMMD.recojos()), "Por recoger del cliente"),
                new KpiCardPMMD("PAYMENTS", "green", "Ingresos del mes", MoneyPMMD.fmt0(kPMMD.ingresosMes()), "Retapizados entregados")),
                UIPMMD.label("Flujo: Por cotizar → Cotizado → Aprobado → Recojo programado → En taller (crea la OP en Corte de tela) → Listo → Entregado", "cell-sub"), cardPMMD);
    }

    private void cotizacionPdfPMMD(RetapizadoPMMD rPMMD) {
        File f = ArchivosPMMD.guardar(contenidoPMMD, "Cotizacion-" + rPMMD.getCodigo(), "PDF", "pdf");
        if (f == null) return;
        TareasPMMD.ejecutar(overlaysPMMD, toastPMMD, "Generando cotización…", () -> {
            exportPMMD.cotizacionRetapizado(rPMMD, retapizadoPMMD.cotizar(rPMMD), f);
            return f;
        }, aPMMD -> {
            toastPMMD.exito("Cotización guardada: " + aPMMD.getName());
            appWindowPMMD.hostServices().showDocument(aPMMD.toURI().toString());
        });
    }

    private void nuevoRetapizadoPMMD() {
        ValidacionPMMD vPMMD = new ValidacionPMMD();
        ComboBox<ClientePMMD> clientePMMD = FormsPMMD.combo(clientesPMMD.listar(), null, ClientePMMD::getNombre);
        Spinner<Integer> cuerposPMMD = new Spinner<>(1, 4, 3);
        cuerposPMMD.setMaxWidth(Double.MAX_VALUE);
        TextField descPMMD = UIPMMD.text("", "Sofá 3 cuerpos color vino, tela gastada");
        TextField largoPMMD = FormsPMMD.entero(200), fondoPMMD = FormsPMMD.entero(90), altoPMMD = FormsPMMD.entero(85);
        ComboBox<String> estructuraPMMD = FormsPMMD.combo(RetapizadoPMMD.ESTRUCTURAS, "Buena", sPMMD -> sPMMD);
        ComboBox<String> espumaPMMD = FormsPMMD.combo(RetapizadoPMMD.ESPUMAS, "Hundida", sPMMD -> sPMMD);
        DatePicker recojoPMMD = FormsPMMD.fecha(AppClockPMMD.hoy().plusDays(2));
        ComboBox<TelaPMMD> telaPMMD = FormsPMMD.combo(telasPMMD.activas(), null, t -> t.getNombre() + " · " + MoneyPMMD.numero(t.getStock()) + " m");
        File[] fotoPMMD = new File[1];
        ImageSlotPMMD slot = new ImageSlotPMMD("Foto del sofá del cliente", 220, 150, 12);
        slot.alElegir(f -> {
            fotoPMMD[0] = f;
            slot.mostrar(new javafx.scene.image.Image(f.toURI().toString(), 600, 0, true, true));
        });
        slot.alQuitar(() -> {
            fotoPMMD[0] = null;
            slot.mostrar((javafx.scene.image.Image) null);
        });
        VBox cotiz = new VBox(8);
        Runnable calcularPMMD = () -> {
            if (telaPMMD.getValue() == null || FormsPMMD.entero(largoPMMD) < 40) {
                cotiz.getChildren().setAll(UIPMMD.label("Elige la tela para ver la cotización automática.", "muted"));
                return;
            }
            try {
                RetapizadoServicePMMD.Cotizacion cPMMD = retapizadoPMMD.cotizar(cuerposPMMD.getValue(), FormsPMMD.entero(largoPMMD), FormsPMMD.entero(fondoPMMD), FormsPMMD.entero(altoPMMD),
                        estructuraPMMD.getValue(), espumaPMMD.getValue(), telaPMMD.getValue());
                GridPane gPMMD = new GridPane();
                gPMMD.setHgap(12);
                gPMMD.setVgap(4);
                int iPMMD = 0;
                for (var lPMMD : cPMMD.lineas()) {
                    gPMMD.addRow(iPMMD++, UIPMMD.label(lPMMD.concepto(), "cell-sub"), UIPMMD.label(lPMMD.material().getNombre(), "cell-title"),
                            UIPMMD.label(MoneyPMMD.numero(lPMMD.cantidad()) + " " + lPMMD.material().getUnidad(), "cell-sub"), UIPMMD.label(MoneyPMMD.fmt(lPMMD.costo()), "cell-title"));
                }
                gPMMD.addRow(iPMMD, UIPMMD.label("Mano de obra", "cell-sub"), UIPMMD.label("(tapizado + costura) × 1.2 + acabado", "cell-title"),
                        UIPMMD.label(MoneyPMMD.numero(cPMMD.horas()) + " h × S/ 9", "cell-sub"), UIPMMD.label(MoneyPMMD.fmt(cPMMD.costoMO()), "cell-title"));
                cotiz.getChildren().setAll(UIPMMD.label("Cotización automática · base " + cPMMD.ficha().getEtiqueta() + " escalada", "card-title"), gPMMD,
                        UIPMMD.hbox(10, UIPMMD.miniStat("Costo", MoneyPMMD.fmt(cPMMD.costo())), UIPMMD.miniStat("Precio (costo × 1.06 × 1.45)", MoneyPMMD.fmt0(cPMMD.precio())),
                                UIPMMD.miniStat("Tela", MoneyPMMD.numero(cPMMD.metrosTelaPMMD()) + " m")));
                if (cPMMD.aviso() != null) cotiz.getChildren().add(UIPMMD.banner("danger-banner", "WARNING", cPMMD.aviso()));
            } catch (RuntimeException exPMMD) {
                cotiz.getChildren().setAll(UIPMMD.label(exPMMD.getMessage(), "field-error"));
            }
        };
        for (var pPMMD : List.of(estructuraPMMD.valueProperty(), espumaPMMD.valueProperty(), telaPMMD.valueProperty())) pPMMD.addListener((oPMMD, aPMMD, bPMMD) -> calcularPMMD.run());
        cuerposPMMD.valueProperty().addListener((oPMMD, aPMMD, bPMMD) -> calcularPMMD.run());
        for (TextField tfPMMD : List.of(largoPMMD, fondoPMMD, altoPMMD)) tfPMMD.textProperty().addListener((oPMMD, aPMMD, bPMMD) -> calcularPMMD.run());
        calcularPMMD.run();
        FormGridPMMD f = new FormGridPMMD(3);
        f.nodo(vPMMD.campo("Cliente", clientePMMD, () -> ValidacionPMMD.requerido(clientePMMD, "el cliente")), 2);
        f.nodo(vPMMD.campo("Cuerpos", cuerposPMMD, null), 1);
        f.nodo(vPMMD.campo("Descripción del sofá", descPMMD, () -> ValidacionPMMD.requerido(descPMMD, "la descripción")), 3);
        f.nodo(vPMMD.campo("Largo (cm)", largoPMMD, () -> ValidacionPMMD.numero(largoPMMD, 40, "El largo")), 1);
        f.nodo(vPMMD.campo("Fondo (cm)", fondoPMMD, () -> ValidacionPMMD.numero(fondoPMMD, 40, "El fondo")), 1);
        f.nodo(vPMMD.campo("Alto (cm)", altoPMMD, () -> ValidacionPMMD.numero(altoPMMD, 40, "El alto")), 1);
        f.nodo(vPMMD.campo("Estructura", estructuraPMMD, null), 1);
        f.nodo(vPMMD.campo("Espuma", espumaPMMD, null), 1);
        f.nodo(vPMMD.campo("Fecha de recojo", recojoPMMD, () -> recojoPMMD.getValue() == null ? "Elige la fecha." : null), 1);
        f.nodo(vPMMD.campo("Tela nueva", telaPMMD, null), 3);
        HBox cuerpoPMMD = new HBox(16, slot, new VBox(14, f, cotiz));
        HBox.setHgrow(cuerpoPMMD.getChildren().get(1), Priority.ALWAYS);
        Button cancelarPMMD = UIPMMD.secondary("Cancelar", null);
        Button okPMMD = UIPMMD.primary("Registrar retapizado", "SAVE");
        vPMMD.enlazar(okPMMD);
        var hPMMD = overlaysPMMD.modal("Nuevo retapizado", "La cotización usa la ficha de 1, 2 o 3 cuerpos escalada a las medidas", cuerpoPMMD, 900, cancelarPMMD, okPMMD);
        cancelarPMMD.setOnAction(ePMMD -> hPMMD.cerrar());
        okPMMD.setOnAction(ePMMD -> {
            RetapizadoPMMD[] rPMMD = new RetapizadoPMMD[1];
            if (toastPMMD.intentar(() -> {
                rPMMD[0] = retapizadoPMMD.crear(clientePMMD.getValue().getId(), cuerposPMMD.getValue(), descPMMD.getText(), FormsPMMD.entero(largoPMMD), FormsPMMD.entero(fondoPMMD),
                        FormsPMMD.entero(altoPMMD), estructuraPMMD.getValue(), espumaPMMD.getValue(), recojoPMMD.getValue(), telaPMMD.getValue() == null ? null : telaPMMD.getValue().getId(), null);
                if (fotoPMMD[0] != null) {
                    String rutaPMMD = imagenesPMMD.guardar(fotoPMMD[0], rPMMD[0].getCodigo(), 1200, 800);
                    retapizadoPMMD.fijarFoto(rPMMD[0].getId(), rutaPMMD);
                }
            })) {
                hPMMD.cerrar();
                toastPMMD.exito(rPMMD[0].getCodigo() + " registrado · " + rPMMD[0].getEstado().nombre() + (rPMMD[0].getTotal() == null ? "." : " por " + MoneyPMMD.fmt0(rPMMD[0].getTotal()) + "."));
                renderPMMD();
            }
        });
    }



    private void renderGarantiasPMMD() {
        GarantiaServicePMMD.Kpis kPMMD = garantiasPMMD.kpis();
        TableView<ReclamoGarantiaPMMD> t = CellsPMMD.tabla("Sin reclamos.");
        t.getColumns().addAll(List.of(
                CellsPMMD.texto("Reclamo", ReclamoGarantiaPMMD::getCodigo, 80, "cell-mono"),
                CellsPMMD.texto("Cliente", rPMMD -> rPMMD.getCliente().getNombre(), 190, "cell-strong"),
                CellsPMMD.nodo("Sofá y compra", rPMMD -> new VBox(0, UIPMMD.label(rPMMD.getProductoNombre(), "cell-title"), UIPMMD.label("Comprado el " + AppClockPMMD.fmt(rPMMD.getFechaCompra()), "cell-sub")), 200),
                CellsPMMD.texto("Falla", ReclamoGarantiaPMMD::getFalla, 150),
                CellsPMMD.nodo("Cobertura", rPMMD -> {
                    CoberturaPMMD.Resultado cPMMD = garantiasPMMD.cobertura(rPMMD);
                    return new VBox(1, new StatusBadgePMMD(cPMMD.cubierto() ? "En garantía" : "Fuera de garantía"), UIPMMD.label((cPMMD.cubierto() ? "Vence " : "Venció ") + AppClockPMMD.fmt(cPMMD.vence()), "cell-sub"));
                }, 150),
                CellsPMMD.nodo("SLA", rPMMD -> {
                    long dPMMD = garantiasPMMD.sla(rPMMD);
                    Label lPMMD = UIPMMD.label(dPMMD + " días", "cell-title");
                    if (garantiasPMMD.slaVencido(rPMMD)) lPMMD.setStyle("-fx-text-fill: -is-danger;");
                    return lPMMD;
                }, 80),
                CellsPMMD.badge("Estado", rPMMD -> rPMMD.getEstado().nombre(), 120)));
        t.getItems().setAll(garantiasPMMD.listar());
        t.setFixedCellSize(54);
        t.setPrefHeight(54 * Math.max(5, t.getItems().size()) + 44);
        t.setRowFactory(tvPMMD -> {
            TableRow<ReclamoGarantiaPMMD> rPMMD = new TableRow<>();
            rPMMD.setOnMouseClicked(ePMMD -> {
                if (!rPMMD.isEmpty()) detalleReclamoPMMD(rPMMD.getItem());
            });
            return rPMMD;
        });
        VBox cardPMMD = new VBox(t);
        cardPMMD.getStyleClass().addAll("card", "table-card");
        contenidoPMMD.getChildren().setAll(UIPMMD.fila(
                new KpiCardPMMD("REPORT_PROBLEM", "amber", "Reclamos abiertos", String.valueOf(kPMMD.abiertos()), "Por resolver"),
                new KpiCardPMMD("VERIFIED_USER", "green", "Cubiertos por garantía", String.format("%.0f %%", kPMMD.pctCubiertos()), "Del total de reclamos"),
                new KpiCardPMMD("SCHEDULE", "blue", "Tiempo promedio de solución", MoneyPMMD.numero(kPMMD.diasPromedio()) + " días", "Reclamos cerrados"),
                new KpiCardPMMD("PAYMENTS", "red", "Costo de garantías", MoneyPMMD.fmt0(kPMMD.costo()), "Reparaciones y reemplazos")),
                UIPMMD.label("Cobertura: pata floja, estructura quebrada y mecanismo trabado 12 meses (estructura); el resto 6 meses (tapiz y espuma). SLA objetivo: 7 días.", "cell-sub"),
                cardPMMD);
    }

    private void detalleReclamoPMMD(ReclamoGarantiaPMMD rPMMD) {
        CoberturaPMMD.Resultado cPMMD = garantiasPMMD.cobertura(rPMMD);
        TextArea diagPMMD = FormsPMMD.area(rPMMD.getDiagnostico(), "Diagnóstico técnico", 3);
        ComboBox<String> solPMMD = FormsPMMD.combo(ReclamoGarantiaPMMD.SOLUCIONES, rPMMD.getSolucion() == null ? "Por definir" : rPMMD.getSolucion(), sPMMD -> sPMMD);
        TextField costoPMMD = FormsPMMD.decimal(rPMMD.getCosto());
        ToggleGroup estadosPMMD = new ToggleGroup();
        FlowPane botones = new FlowPane(6, 6);
        for (EstadoReclamoPMMD ePMMD : EstadoReclamoPMMD.values()) {
            ToggleButton bPMMD = new ToggleButton(ePMMD.nombre());
            bPMMD.getStyleClass().add("opt-tile");
            bPMMD.setUserData(ePMMD);
            bPMMD.setToggleGroup(estadosPMMD);
            bPMMD.setSelected(ePMMD == rPMMD.getEstado());
            botones.getChildren().add(bPMMD);
        }
        estadosPMMD.selectedToggleProperty().addListener((oPMMD, aPMMD, bPMMD) -> {
            if (bPMMD == null) estadosPMMD.selectToggle(aPMMD);
        });
        VBox cuerpoPMMD = new VBox(14,
                UIPMMD.hbox(8, new StatusBadgePMMD(cPMMD.cubierto() ? "En garantía" : "Fuera de garantía"), UIPMMD.label(cPMMD.texto() + " · " + cPMMD.tipo(), "cell-sub")),
                UIPMMD.dato("Cliente", rPMMD.getCliente().getNombre() + " · " + rPMMD.getCliente().getTelefono()),
                UIPMMD.dato("Sofá", rPMMD.getProductoNombre() + " · comprado el " + AppClockPMMD.fmt(rPMMD.getFechaCompra())),
                UIPMMD.dato("Falla reportada el " + AppClockPMMD.fmt(rPMMD.getFecha()), rPMMD.getFalla() + ": " + rPMMD.getDescripcion()),
                UIPMMD.dato("SLA", garantiasPMMD.sla(rPMMD) + " días" + (garantiasPMMD.slaVencido(rPMMD) ? " · supera el objetivo de 7 días" : "")),
                UIPMMD.field("Diagnóstico", diagPMMD), UIPMMD.field("Solución", solPMMD), UIPMMD.field("Costo para la empresa (S/)", costoPMMD),
                UIPMMD.label("Estado", "form-label"), botones);
        if (rPMMD.getFechaCierre() != null) cuerpoPMMD.getChildren().add(UIPMMD.label("Cerrado el " + AppClockPMMD.fmt(rPMMD.getFechaCierre()), "cell-sub"));
        Button guardarPMMD = UIPMMD.primary("Guardar", "SAVE");
        OverlaysPMMD.Handle[] hPMMD = new OverlaysPMMD.Handle[1];
        guardarPMMD.setOnAction(ePMMD -> {
            EstadoReclamoPMMD estPMMD = (EstadoReclamoPMMD) estadosPMMD.getSelectedToggle().getUserData();
            if (toastPMMD.intentar(() -> garantiasPMMD.actualizar(rPMMD.getId(), diagPMMD.getText(), solPMMD.getValue(), FormsPMMD.dinero(costoPMMD), estPMMD))) {
                hPMMD[0].cerrar();
                toastPMMD.exito(rPMMD.getCodigo() + " actualizado · " + estPMMD.nombre() + (estPMMD.cerrado() ? " (cerrado hoy)." : "."));
                navigatorPMMD.datosCambiaron();
                renderPMMD();
            }
        });
        hPMMD[0] = overlaysPMMD.panel(rPMMD.getCodigo() + " · " + rPMMD.getFalla(), rPMMD.getCliente().getNombre(), cuerpoPMMD, UIPMMD.hbox(8, UIPMMD.spacer(), guardarPMMD), 480);
    }

    private void nuevoReclamoPMMD() {
        ValidacionPMMD vPMMD = new ValidacionPMMD();
        ComboBox<ClientePMMD> clientePMMD = FormsPMMD.combo(clientesPMMD.listar(), null, ClientePMMD::getNombre);
        ComboBox<ProductoPMMD> productoPMMD = FormsPMMD.combo(productosPMMD.listar(), null, ProductoPMMD::getNombre);
        DatePicker compraPMMD = FormsPMMD.fecha(AppClockPMMD.hoy().minusMonths(3));
        ComboBox<String> fallaPMMD = FormsPMMD.combo(ReclamoGarantiaPMMD.FALLAS, null, sPMMD -> sPMMD);
        TextArea descPMMD = FormsPMMD.area("", "Qué pasó, desde cuándo, fotos recibidas…", 3);
        Label avisoPMMD = UIPMMD.label("", "semibold");
        Runnable cobPMMD = () -> {
            if (fallaPMMD.getValue() == null || compraPMMD.getValue() == null) {
                avisoPMMD.setText("Elige la falla y la fecha de compra para ver la cobertura.");
                avisoPMMD.setStyle("-fx-text-fill: -is-muted;");
                return;
            }
            CoberturaPMMD.Resultado cPMMD = CoberturaPMMD.evaluar(fallaPMMD.getValue(), compraPMMD.getValue(), AppClockPMMD.hoy());
            avisoPMMD.setText(cPMMD.texto() + " · " + cPMMD.tipo());
            avisoPMMD.setStyle(cPMMD.cubierto() ? "-fx-text-fill: -is-success-ink;" : "-fx-text-fill: -is-danger-ink;");
        };
        fallaPMMD.valueProperty().addListener((oPMMD, aPMMD, bPMMD) -> cobPMMD.run());
        compraPMMD.valueProperty().addListener((oPMMD, aPMMD, bPMMD) -> cobPMMD.run());
        cobPMMD.run();
        FormGridPMMD f = new FormGridPMMD(2);
        f.nodo(vPMMD.campo("Cliente", clientePMMD, () -> ValidacionPMMD.requerido(clientePMMD, "el cliente")), 2);
        f.nodo(vPMMD.campo("Sofá", productoPMMD, () -> ValidacionPMMD.requerido(productoPMMD, "el sofá")), 1);
        f.nodo(vPMMD.campo("Fecha de compra", compraPMMD, () -> compraPMMD.getValue() == null || compraPMMD.getValue().isAfter(AppClockPMMD.hoy()) ? "Revisa la fecha." : null), 1);
        f.nodo(vPMMD.campo("Falla", fallaPMMD, () -> ValidacionPMMD.requerido(fallaPMMD, "la falla")), 2);
        f.nodo(vPMMD.campo("Descripción", descPMMD, null), 2);
        f.nodo(avisoPMMD, 2);
        Button cancelarPMMD = UIPMMD.secondary("Cancelar", null);
        Button okPMMD = UIPMMD.primary("Registrar reclamo", "SAVE");
        vPMMD.enlazar(okPMMD);
        var hPMMD = overlaysPMMD.modal("Registrar reclamo", "La cobertura se calcula en vivo según la falla", f, 560, cancelarPMMD, okPMMD);
        cancelarPMMD.setOnAction(ePMMD -> hPMMD.cerrar());
        okPMMD.setOnAction(ePMMD -> {
            ReclamoGarantiaPMMD[] rPMMD = new ReclamoGarantiaPMMD[1];
            if (toastPMMD.intentar(() -> rPMMD[0] = garantiasPMMD.registrar(clientePMMD.getValue().getId(), productoPMMD.getValue().getId(), compraPMMD.getValue(), fallaPMMD.getValue(), descPMMD.getText()))) {
                hPMMD.cerrar();
                toastPMMD.exito(rPMMD[0].getCodigo() + " registrado · " + garantiasPMMD.cobertura(rPMMD[0]).texto() + ".");
                navigatorPMMD.datosCambiaron();
                renderPMMD();
            }
        });
    }
}
