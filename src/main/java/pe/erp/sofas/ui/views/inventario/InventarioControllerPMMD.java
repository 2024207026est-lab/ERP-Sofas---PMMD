package pe.erp.sofas.ui.views.inventario;

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
import pe.erp.sofas.service.calculo.UnidadCompraPMMD;
import pe.erp.sofas.ui.components.*;

import java.util.*;



@Component
@Lazy
@Scope("prototype")
@RequiredArgsConstructor
public class InventarioControllerPMMD implements VistaPMMD {

    private final InventarioServicePMMD inventarioPMMD;

    private final TelaServicePMMD telasPMMD;

    private final ProveedorServicePMMD proveedoresPMMD;

    private final MrpServicePMMD mrpPMMD;

    private final OverlaysPMMD overlaysPMMD;

    private final ToastPMMD toastPMMD;

    private final NavigatorPMMD navigatorPMMD;

    @FXML private VBox raiz;

    private final VBox kpisPMMD = new VBox();

    private SegmentedControlPMMD tabsPMMD;

    private SearchFieldPMMD buscadorPMMD;

    private ComboBox<String> categoriaPMMD;

    private ComboBox<String> estadoPMMD;

    private final StackPane contenidoPMMD = new StackPane();

    private final TableView<MaterialPMMD> tablaMatPMMD = CellsPMMD.tabla("Ningún material coincide con los filtros.");

    private ComboBox<MaterialPMMD> kardexMaterialPMMD;

    private final TableView<MovimientoPMMD> tablaKardexPMMD = CellsPMMD.tabla("Sin movimientos.");

    private final Map<Long, Double> saldosPMMD = new HashMap<>();

    @FXML
    public void initialize() {
        tabsPMMD = new SegmentedControlPMMD("Materiales", "Kardex", "Alertas");
        tabsPMMD.getStyleClass().add("tabs");
        tabsPMMD.valorProperty().addListener((oPMMD, aPMMD, bPMMD) -> renderPMMD());
        buscadorPMMD = new SearchFieldPMMD("Buscar material, código o ubicación");
        buscadorPMMD.setPrefWidth(300);
        buscadorPMMD.textProperty().addListener((oPMMD, aPMMD, bPMMD) -> renderPMMD());
        List<String> catsPMMD = new ArrayList<>();
        catsPMMD.add(null);
        catsPMMD.addAll(MaterialPMMD.CATEGORIAS);
        categoriaPMMD = FormsPMMD.combo(catsPMMD, null, cPMMD -> cPMMD == null ? "Todas las categorías" : cPMMD);
        categoriaPMMD.setPrefWidth(190);
        categoriaPMMD.valueProperty().addListener((oPMMD, aPMMD, bPMMD) -> renderPMMD());
        estadoPMMD = FormsPMMD.combo(Arrays.asList(null, "Normal", "Stock bajo", "Crítico", "Agotado", "Bajo pedido"), null, sPMMD -> sPMMD == null ? "Todos los estados" : sPMMD);
        estadoPMMD.setPrefWidth(170);
        estadoPMMD.valueProperty().addListener((oPMMD, aPMMD, bPMMD) -> renderPMMD());
        Button nuevoPMMD = UIPMMD.primary("Registrar material", "ADD");
        nuevoPMMD.setOnAction(ePMMD -> nuevoMaterialPMMD());
        construirTablasPMMD();
        VBox.setVgrow(contenidoPMMD, Priority.ALWAYS);
        raiz.getChildren().setAll(kpisPMMD, UIPMMD.hbox(10, tabsPMMD, UIPMMD.spacer(), buscadorPMMD, categoriaPMMD, estadoPMMD, nuevoPMMD), contenidoPMMD);
    }

    @Override
    public void onShow(IntentPMMD intentPMMD) {
        if (intentPMMD.es(IntentPMMD.ACCION_ABRIR) && intentPMMD.dato() != null) {
            tabsPMMD.setValor("Kardex");
            renderPMMD();
            kardexMaterialPMMD.getItems().stream().filter(m -> m != null && m.getCodigo().equals(intentPMMD.dato().toString())).findFirst().ifPresent(kardexMaterialPMMD::setValue);
            return;
        }
        renderPMMD();
    }

    private void renderPMMD() {
        List<MaterialPMMD> matsPMMD = inventarioPMMD.materiales();
        long criticosPMMD = matsPMMD.stream().filter(MaterialPMMD::isCritico).count();
        kpisPMMD.getChildren().setAll(UIPMMD.fila(
                new KpiCardPMMD("ACCOUNT_BALANCE_WALLET", "wood", "Valor del inventario", MoneyPMMD.fmt0(inventarioPMMD.valorTotal()), "A costo unitario"),
                new KpiCardPMMD("ALL_INBOX", "blue", "Materiales", String.valueOf(matsPMMD.size()), MaterialPMMD.CATEGORIAS.size() + " categorías"),
                new KpiCardPMMD("WARNING", "red", "Crítico o agotado", String.valueOf(criticosPMMD), matsPMMD.stream().filter(m -> "Stock bajo".equals(m.getEstado())).count() + " más en stock bajo")
                        .alClic(() -> tabsPMMD.setValor("Alertas")),
                new KpiCardPMMD("HISTORY", "green", "Movimientos del mes", String.valueOf(inventarioPMMD.movimientosDelMes()), "Entradas, salidas, reservas y consumos")));
        String tPMMD = tabsPMMD.getValor();
        boolean esMatPMMD = "Materiales".equals(tPMMD);
        buscadorPMMD.setDisable(!esMatPMMD);
        categoriaPMMD.setDisable(!esMatPMMD);
        estadoPMMD.setDisable(!esMatPMMD);
        switch (tPMMD) {
            case "Kardex" -> contenidoPMMD.getChildren().setAll(kardex(matsPMMD));
            case "Alertas" -> contenidoPMMD.getChildren().setAll(alertas());
            default -> {
                String qPMMD = BusquedaServicePMMD.normalizar(buscadorPMMD.getText());
                tablaMatPMMD.getItems().setAll(matsPMMD.stream()
                        .filter(m -> categoriaPMMD.getValue() == null || m.getCategoria().equals(categoriaPMMD.getValue()))
                        .filter(m -> estadoPMMD.getValue() == null || m.getEstado().equals(estadoPMMD.getValue()))
                        .filter(m -> qPMMD.isEmpty() || BusquedaServicePMMD.normalizar(m.getCodigo() + " " + m.getNombre() + " " + m.getUbicacion()).contains(qPMMD)).toList());
                VBox card = new VBox(tablaMatPMMD);
                VBox.setVgrow(tablaMatPMMD, Priority.ALWAYS);
                card.getStyleClass().addAll("card", "table-card");
                contenidoPMMD.getChildren().setAll(card);
            }
        }
    }

    private void construirTablasPMMD() {
        TableColumn<MaterialPMMD, String> valorPMMD = CellsPMMD.derecha("Valor", m -> MoneyPMMD.fmt(m.getValor()), 100, "cell-strong");
        valorPMMD.setComparator(Comparator.comparing(MoneyPMMD::parse));
        tablaMatPMMD.getColumns().addAll(List.of(
                CellsPMMD.texto("Código", MaterialPMMD::getCodigo, 70, "cell-mono"),
                CellsPMMD.texto("Material", MaterialPMMD::getNombre, 200, "cell-strong"),
                CellsPMMD.texto("Categoría", MaterialPMMD::getCategoria, 100, "cell-muted"),
                CellsPMMD.nodo("Stock · mínimo", this::celdaStockPMMD, 220),
                CellsPMMD.derecha("C. unit.", m -> MoneyPMMD.fmt(m.getCosto()), 80, "cell-muted"),
                valorPMMD,
                CellsPMMD.texto("Ubicación", MaterialPMMD::getUbicacion, 140, "cell-muted"),
                CellsPMMD.badge("Estado", MaterialPMMD::getEstado, 100),
                CellsPMMD.acciones("", m -> new Node[]{
                        botonPMMD("MOVE_TO_INBOX", "Entrada", () -> movimientoPMMD(m, TipoMovimientoPMMD.ENTRADA)),
                        botonPMMD("UNARCHIVE", "Salida", () -> movimientoPMMD(m, TipoMovimientoPMMD.SALIDA)),
                        botonPMMD("TUNE", "Ajuste", () -> movimientoPMMD(m, TipoMovimientoPMMD.AJUSTE)),
                        botonPMMD("HISTORY", "Ver kardex", () -> {
                            tabsPMMD.setValor("Kardex");
                            kardexMaterialPMMD.setValue(kardexMaterialPMMD.getItems().stream().filter(xPMMD -> xPMMD != null && xPMMD.getId().equals(m.getId())).findFirst().orElse(null));
                        })}, 140)));
        tablaMatPMMD.setFixedCellSize(54);
        tablaMatPMMD.setRowFactory(tvPMMD -> {
            TableRow<MaterialPMMD> rPMMD = new TableRow<>();
            rPMMD.setOnMouseClicked(ePMMD -> {
                if (ePMMD.getClickCount() == 2 && !rPMMD.isEmpty()) {
                    tabsPMMD.setValor("Kardex");
                    kardexMaterialPMMD.setValue(kardexMaterialPMMD.getItems().stream().filter(xPMMD -> xPMMD != null && xPMMD.getId().equals(rPMMD.getItem().getId())).findFirst().orElse(null));
                }
            });
            return rPMMD;
        });
        kardexMaterialPMMD = FormsPMMD.combo(List.of(), null, m -> m == null ? "Todos los materiales" : m.getCodigo() + " · " + m.getNombre());
        kardexMaterialPMMD.setPrefWidth(340);
        kardexMaterialPMMD.valueProperty().addListener((oPMMD, aPMMD, bPMMD) -> llenarKardexPMMD());
        tablaKardexPMMD.getColumns().addAll(List.of(
                CellsPMMD.texto("Fecha", m -> AppClockPMMD.fmt(m.getFecha().toLocalDate()) + " " + m.getFecha().toLocalTime().toString().substring(0, 5), 120),
                CellsPMMD.texto("Material", m -> m.getMaterial().getCodigo() + " · " + m.getMaterial().getNombre(), 220, "cell-strong"),
                CellsPMMD.nodo("Tipo", m -> new StatusBadgePMMD(m.getTipo().nombre(), m.getTipo().variante()).conIcono(m.getTipo().icono()), 130),
                CellsPMMD.nodo("Cantidad", m -> {
                    double efPMMD = m.getEfecto();
                    String signoPMMD = !m.getTipo().afectaSaldo() ? "" : efPMMD >= 0 ? "+" : "−";
                    Label lPMMD = UIPMMD.label(signoPMMD + MoneyPMMD.numero(Math.abs(m.getTipo() == TipoMovimientoPMMD.AJUSTE ? efPMMD : m.getCantidad())) + " " + m.getMaterial().getUnidad(), "cell-title");
                    if (!m.getTipo().afectaSaldo()) lPMMD.getStyleClass().add("muted");
                    else lPMMD.setStyle(efPMMD >= 0 ? "-fx-text-fill: -is-success-ink;" : "-fx-text-fill: -is-danger-ink;");
                    return lPMMD;
                }, 120),
                CellsPMMD.texto("Referencia", MovimientoPMMD::getReferencia, 100, "cell-mono"),
                CellsPMMD.texto("Concepto", MovimientoPMMD::getConcepto, 240, "cell-muted"),
                CellsPMMD.derecha("Saldo", m -> MoneyPMMD.numero(saldosPMMD.getOrDefault(m.getId(), 0d)), 90, "cell-strong"),
                CellsPMMD.texto("Usuario", MovimientoPMMD::getUsuario, 90, "cell-muted")));
    }

    private Button botonPMMD(String iconoPMMD, String tipPMMD, Runnable rPMMD) {
        Button bPMMD = UIPMMD.iconButton(iconoPMMD, tipPMMD, "btn-ghost", "btn-xs");
        bPMMD.setOnAction(ePMMD -> rPMMD.run());
        return bPMMD;
    }

    private Node celdaStockPMMD(MaterialPMMD m) {
        String color = switch (m.getEstado()) {
            case "Normal" -> "#16A34A";
            case "Stock bajo", "Bajo pedido" -> "#F59E0B";
            default -> "#DC2626";
        };
        ProgressBarMiniPMMD barraPMMD = new ProgressBarMiniPMMD(m.getMinimo() <= 0 ? 1 : m.getDisponible() / (m.getMinimo() * 2), color, 6);
        barraPMMD.setPrefWidth(70);
        Label stockPMMD = UIPMMD.label(MoneyPMMD.numero(m.getStock()) + " " + m.getUnidad(), "cell-title");
        Label resPMMD = UIPMMD.label(m.getReservado() > 0 ? "res. " + MoneyPMMD.numero(m.getReservado()) : "sin reservas", "cell-sub");
        Tooltip.install(resPMMD, new Tooltip("Disponible: " + MoneyPMMD.numero(m.getDisponible()) + " · mínimo " + MoneyPMMD.numero(m.getMinimo())));
        VBox vPMMD = new VBox(2, UIPMMD.hbox(6, stockPMMD, resPMMD), UIPMMD.hbox(6, UIPMMD.grow(barraPMMD), UIPMMD.label("mín. " + MoneyPMMD.numero(m.getMinimo()), "cell-sub")));
        return vPMMD;
    }

    private Node kardex(List<MaterialPMMD> matsPMMD) {
        MaterialPMMD selPMMD = kardexMaterialPMMD.getValue();
        List<MaterialPMMD> itemsPMMD = new ArrayList<>();
        itemsPMMD.add(null);
        itemsPMMD.addAll(matsPMMD);
        kardexMaterialPMMD.getItems().setAll(itemsPMMD);
        kardexMaterialPMMD.setValue(selPMMD == null ? null : matsPMMD.stream().filter(m -> m.getId().equals(selPMMD.getId())).findFirst().orElse(null));
        llenarKardexPMMD();
        VBox card = new VBox(tablaKardexPMMD);
        VBox.setVgrow(tablaKardexPMMD, Priority.ALWAYS);
        card.getStyleClass().addAll("card", "table-card");
        VBox.setVgrow(card, Priority.ALWAYS);
        HBox leyenda = new HBox(8);
        for (TipoMovimientoPMMD tPMMD : TipoMovimientoPMMD.values()) leyenda.getChildren().add(new StatusBadgePMMD(tPMMD.nombre(), tPMMD.variante()).conIcono(tPMMD.icono()));
        return new VBox(10, UIPMMD.hbox(10, kardexMaterialPMMD, UIPMMD.spacer(), UIPMMD.label("Reserva, Liberación y Retazo no alteran el saldo", "cell-sub")), leyenda, card);
    }



    private void llenarKardexPMMD() {
        MaterialPMMD selPMMD = kardexMaterialPMMD.getValue();
        List<MovimientoPMMD> movs = selPMMD == null ? inventarioPMMD.movimientos() : inventarioPMMD.kardex(selPMMD.getId());
        List<MovimientoPMMD> crono = new ArrayList<>(movs);
        crono.sort(Comparator.comparing(MovimientoPMMD::getFecha).thenComparing(MovimientoPMMD::getId));
        saldosPMMD.clear();
        Map<Long, Double> acumPMMD = new HashMap<>();
        if (selPMMD == null) {

            List<MovimientoPMMD> todosPMMD = new ArrayList<>(inventarioPMMD.movimientos());
            todosPMMD.sort(Comparator.comparing(MovimientoPMMD::getFecha).thenComparing(MovimientoPMMD::getId));
            for (MovimientoPMMD m : todosPMMD) {
                double sPMMD = acumPMMD.getOrDefault(m.getMaterial().getId(), 0d) + m.getEfecto();
                acumPMMD.put(m.getMaterial().getId(), sPMMD);
                saldosPMMD.put(m.getId(), sPMMD);
            }
        } else {
            double sPMMD = 0;
            for (MovimientoPMMD m : crono) {
                sPMMD += m.getEfecto();
                saldosPMMD.put(m.getId(), sPMMD);
            }
        }
        List<MovimientoPMMD> recientesPMMD = new ArrayList<>(crono);
        Collections.reverse(recientesPMMD);
        tablaKardexPMMD.getItems().setAll(recientesPMMD);
    }

    private Node alertas() {
        List<MaterialPMMD> bajoPMMD = inventarioPMMD.bajoMinimo();
        VBox listaPMMD = new VBox(8);
        if (bajoPMMD.isEmpty()) listaPMMD.getChildren().add(UIPMMD.empty("DONE_ALL", "Sin alertas", "Todos los materiales están sobre su mínimo.", null));
        for (MaterialPMMD m : bajoPMMD) {
            double sugeridoPMMD = UnidadCompraPMMD.redondear(m.getUnidad(), Math.max(m.getMinimo() * 2 - m.getDisponible(), m.getMinimo()));
            Button ocPMMD = UIPMMD.button("Generar OC", "SHOPPING_CART", "btn-sm", "btn-wood");
            ocPMMD.setOnAction(ePMMD -> generarOCPMMD(List.of(m)));
            HBox hPMMD = UIPMMD.hbox(12, new StatusBadgePMMD(m.getEstado()), new VBox(1, UIPMMD.label(m.getCodigo() + " · " + m.getNombre(), "cell-title"),
                            UIPMMD.label("Disponible " + MoneyPMMD.numero(m.getDisponible()) + " " + m.getUnidad() + " (stock " + MoneyPMMD.numero(m.getStock()) + ", reservado "
                                    + MoneyPMMD.numero(m.getReservado()) + ") · mínimo " + MoneyPMMD.numero(m.getMinimo()) + " · proveedor "
                                    + (m.getProveedor() == null ? "—" : m.getProveedor().getRazonSocial()), "cell-sub")),
                    UIPMMD.spacer(), UIPMMD.label("Sugerido: " + MoneyPMMD.numero(sugeridoPMMD) + " " + m.getUnidad(), "cell-sub"), ocPMMD);
            hPMMD.getStyleClass().add("alert-row");
            listaPMMD.getChildren().add(hPMMD);
        }
        Button todasPMMD = UIPMMD.primary("Generar OC de todas", "SHOPPING_CART");
        todasPMMD.setDisable(bajoPMMD.isEmpty());
        todasPMMD.setOnAction(ePMMD -> generarOCPMMD(bajoPMMD));
        List<TelaPMMD> telasBajasPMMD = telasPMMD.activas().stream().filter(tPMMD -> !"Disponible".equals(tPMMD.getEstado())).toList();
        VBox telasBoxPMMD = new VBox(6, UIPMMD.label("Colores del muestrario por reponer", "card-title"));
        for (TelaPMMD tPMMD : telasBajasPMMD) {
            Hyperlink lPMMD = new Hyperlink("Ver en Telas y colores");
            lPMMD.setOnAction(ePMMD -> navigatorPMMD.goTo("telas", IntentPMMD.abrir(tPMMD.getCodigo())));
            telasBoxPMMD.getChildren().add(UIPMMD.hbox(10, new ColorSwatchPMMD(tPMMD.getHex(), tPMMD.getTipo(), 22), UIPMMD.label(tPMMD.getNombre() + " · " + MoneyPMMD.numero(tPMMD.getStock()) + " m", "cell-title"),
                    new StatusBadgePMMD(tPMMD.getEstado()), UIPMMD.spacer(), lPMMD));
        }
        return new VBox(14, UIPMMD.card(UIPMMD.cardHeader("Materiales bajo el mínimo", bajoPMMD.size() + " materiales · el disponible descuenta lo reservado", todasPMMD), listaPMMD), UIPMMD.card(telasBoxPMMD));
    }

    private void generarOCPMMD(List<MaterialPMMD> matsPMMD) {
        List<MrpServicePMMD.Fila> filasPMMD = new ArrayList<>();
        for (MaterialPMMD m : matsPMMD) {
            double falta = Math.max(m.getMinimo() * 2 - m.getDisponible(), m.getMinimo());
            if (m.getProveedor() != null) filasPMMD.add(new MrpServicePMMD.Fila(m, falta, 0, falta, AppClockPMMD.hoy(), m.getProveedor(), Set.of("Alerta de stock")));
        }
        List<String>[] cPMMD = new List[1];
        if (toastPMMD.intentar(() -> cPMMD[0] = mrpPMMD.generarOrdenes(filasPMMD, "Alerta de stock"))) {
            toastPMMD.exito("Órdenes de compra en borrador: " + String.join(", ", cPMMD[0]) + ".");
            navigatorPMMD.datosCambiaron();
        }
    }

    private void movimientoPMMD(MaterialPMMD m, TipoMovimientoPMMD tipoPMMD) {
        ValidacionPMMD vPMMD = new ValidacionPMMD();
        TextField cantidadPMMD = FormsPMMD.decimal(tipoPMMD == TipoMovimientoPMMD.AJUSTE ? 0 : 1);
        if (tipoPMMD == TipoMovimientoPMMD.AJUSTE) cantidadPMMD.setTextFormatter(new TextFormatter<String>(cPMMD -> cPMMD.getControlNewText().matches("-?[0-9.,]*") ? cPMMD : null));
        TextField refPMMD = UIPMMD.text("", tipoPMMD == TipoMovimientoPMMD.ENTRADA ? "Guía o factura" : "Vale o documento");
        ComboBox<String> motivoPMMD = FormsPMMD.combo(switch (tipoPMMD) {
            case ENTRADA -> List.of("Compra sin OC", "Devolución del taller", "Donación / muestra", "Otro");
            case SALIDA -> List.of("Uso en taller (sin OP)", "Merma / daño", "Muestra para cliente", "Otro");
            default -> List.of("Inventario físico", "Error de registro", "Otro");
        }, null, sPMMD -> sPMMD);
        FormGridPMMD f = new FormGridPMMD(2);
        ComboBox<TelaPMMD> color = null;
        if (m.isTela()) {
            color = FormsPMMD.combo(telasPMMD.porTipo(TipoTelaPMMD.porMaterial(m.getCodigo())).stream().filter(tPMMD -> !tPMMD.getDescontinuada()).toList(), null,
                    tPMMD -> tPMMD.getColor() + " · " + MoneyPMMD.numero(tPMMD.getStock()) + " m");
            ComboBox<TelaPMMD> ccPMMD = color;
            f.nodo(vPMMD.campo("Color de la tela", ccPMMD, () -> ValidacionPMMD.requerido(ccPMMD, "el color")), 2);
        }
        f.nodo(vPMMD.campo(tipoPMMD == TipoMovimientoPMMD.AJUSTE ? "Cantidad (+/−, " + m.getUnidad() + ")" : "Cantidad (" + m.getUnidad() + ")", cantidadPMMD, () -> {
            double xPMMD = ValidacionPMMD.valor(cantidadPMMD);
            if (tipoPMMD == TipoMovimientoPMMD.AJUSTE) return xPMMD == 0 ? "Ingresa una cantidad distinta de cero." : m.getStock() + xPMMD < 0 ? "El stock quedaría negativo." : null;
            if (xPMMD <= 0) return "La cantidad debe ser mayor a cero.";
            return tipoPMMD == TipoMovimientoPMMD.SALIDA && xPMMD > m.getDisponible() + 1e-9 ? "Solo hay " + MoneyPMMD.numero(m.getDisponible()) + " disponibles (el resto está reservado)." : null;
        }), 1);
        f.nodo(vPMMD.campo("Referencia", refPMMD, null), 1);
        f.nodo(vPMMD.campo("Motivo", motivoPMMD, () -> ValidacionPMMD.requerido(motivoPMMD, "el motivo")), 2);
        VBox cuerpoPMMD = new VBox(12, UIPMMD.hbox(10, UIPMMD.miniStat("Stock", MoneyPMMD.numero(m.getStock()) + " " + m.getUnidad()), UIPMMD.miniStat("Reservado", MoneyPMMD.numero(m.getReservado())),
                UIPMMD.miniStat("Disponible", MoneyPMMD.numero(m.getDisponible()))), f);
        Button cancelarPMMD = UIPMMD.secondary("Cancelar", null);
        Button okPMMD = UIPMMD.primary("Registrar " + tipoPMMD.nombre().toLowerCase(), tipoPMMD.icono());
        vPMMD.enlazar(okPMMD);
        var hPMMD = overlaysPMMD.modal(tipoPMMD.nombre() + " · " + m.getNombre(), m.getCodigo() + " · " + m.getUbicacion(), cuerpoPMMD, 520, cancelarPMMD, okPMMD);
        cancelarPMMD.setOnAction(ePMMD -> hPMMD.cerrar());
        ComboBox<TelaPMMD> colorFinalPMMD = color;
        okPMMD.setOnAction(ePMMD -> {
            if (toastPMMD.intentar(() -> inventarioPMMD.movimientoManual(m.getId(), tipoPMMD, ValidacionPMMD.valor(cantidadPMMD),
                    colorFinalPMMD == null || colorFinalPMMD.getValue() == null ? null : colorFinalPMMD.getValue().getId(), refPMMD.getText(), motivoPMMD.getValue()))) {
                hPMMD.cerrar();
                toastPMMD.exito(tipoPMMD.nombre() + " de " + cantidadPMMD.getText() + " " + m.getUnidad() + " de " + m.getNombre() + " registrada en el kardex.");
                navigatorPMMD.datosCambiaron();
                renderPMMD();
            }
        });
    }

    private void nuevoMaterialPMMD() {
        ValidacionPMMD vPMMD = new ValidacionPMMD();
        TextField nombrePMMD = UIPMMD.text("", "Nombre del material");
        ComboBox<String> catPMMD = FormsPMMD.combo(MaterialPMMD.CATEGORIAS, "Insumos", sPMMD -> sPMMD);
        ComboBox<String> unidadPMMD = FormsPMMD.combo(InventarioServicePMMD.UNIDADES, "unidad", sPMMD -> sPMMD);
        TextField costoPMMD = FormsPMMD.decimal(0), stockPMMD = FormsPMMD.decimal(0), minimoPMMD = FormsPMMD.decimal(0);
        TextField ubicPMMD = UIPMMD.text("", "Almacén A · Rack 1");
        ComboBox<ProveedorPMMD> provPMMD = FormsPMMD.combo(proveedoresPMMD.activos(), null, ProveedorPMMD::getRazonSocial);
        CheckBox bajoPedidoPMMD = new CheckBox("Se compra bajo pedido (sin stock)");
        FormGridPMMD f = new FormGridPMMD(3);
        f.nodo(vPMMD.campo("Nombre", nombrePMMD, () -> ValidacionPMMD.requerido(nombrePMMD, "el nombre")), 3);
        f.nodo(vPMMD.campo("Categoría", catPMMD, null), 1);
        f.nodo(vPMMD.campo("Unidad", unidadPMMD, null), 1);
        f.nodo(vPMMD.campo("Costo unitario (S/)", costoPMMD, () -> ValidacionPMMD.numero(costoPMMD, 0, "El costo")), 1);
        f.nodo(vPMMD.campo("Stock inicial", stockPMMD, () -> ValidacionPMMD.numero(stockPMMD, 0, "El stock")), 1);
        f.nodo(vPMMD.campo("Mínimo", minimoPMMD, () -> ValidacionPMMD.numero(minimoPMMD, 0, "El mínimo")), 1);
        f.nodo(vPMMD.campo("Ubicación", ubicPMMD, null), 1);
        f.nodo(vPMMD.campo("Proveedor", provPMMD, null), 2);
        f.nodo(bajoPedidoPMMD, 1);
        Button cancelarPMMD = UIPMMD.secondary("Cancelar", null);
        Button okPMMD = UIPMMD.primary("Registrar material", "SAVE");
        vPMMD.enlazar(okPMMD);
        var hPMMD = overlaysPMMD.modal("Registrar material", "El stock inicial queda registrado como Entrada en el kardex", f, 640, cancelarPMMD, okPMMD);
        cancelarPMMD.setOnAction(ePMMD -> hPMMD.cerrar());
        okPMMD.setOnAction(ePMMD -> {
            MaterialPMMD[] gPMMD = new MaterialPMMD[1];
            if (toastPMMD.intentar(() -> gPMMD[0] = inventarioPMMD.guardarMaterial(MaterialPMMD.builder().nombre(nombrePMMD.getText().trim()).categoria(catPMMD.getValue())
                    .unidad(unidadPMMD.getValue()).costo(ValidacionPMMD.valor(costoPMMD)).stock(ValidacionPMMD.valor(stockPMMD)).minimo(ValidacionPMMD.valor(minimoPMMD))
                    .ubicacion(ubicPMMD.getText()).proveedor(provPMMD.getValue()).bajoPedido(bajoPedidoPMMD.isSelected()).reservado(0d).build()))) {
                hPMMD.cerrar();
                toastPMMD.exito(gPMMD[0].getCodigo() + " · " + gPMMD[0].getNombre() + " registrado.");
                renderPMMD();
            }
        });
    }
}
