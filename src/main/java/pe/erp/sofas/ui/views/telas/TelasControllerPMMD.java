package pe.erp.sofas.ui.views.telas;

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
import pe.erp.sofas.service.calculo.ExplosionPMMD;
import pe.erp.sofas.service.calculo.Opciones;
import pe.erp.sofas.ui.components.*;

import java.util.*;



@Component
@Lazy
@Scope("prototype")
@RequiredArgsConstructor
public class TelasControllerPMMD implements VistaPMMD {

    private static final List<String> DISPONIBILIDADPMMD = List.of("Todas", "Disponible", "Stock bajo", "Agotado", "Descontinuada");

    private final TelaServicePMMD telaServicePMMD;

    private final ProductoServicePMMD productoServicePMMD;

    private final FichaServicePMMD fichaServicePMMD;

    private final CorteServicePMMD corteServicePMMD;

    private final InventarioServicePMMD inventarioServicePMMD;

    private final OverlaysPMMD overlaysPMMD;

    private final ToastPMMD toastPMMD;

    private final ConfirmDialogPMMD confirmPMMD;

    private final NavigatorPMMD navigatorPMMD;

    @FXML private VBox raiz;

    private final VBox kpisPMMD = new VBox();

    private SearchFieldPMMD buscadorPMMD;

    private final ChipFilterBarPMMD chipsPMMD = new ChipFilterBarPMMD();

    private ComboBox<String> cmbDispPMMD;

    private CheckBox chkMascotasPMMD;

    private final VBox tarjetasPMMD = new VBox(16);

    private String resaltarPMMD;

    @FXML
    public void initialize() {
        buscadorPMMD = new SearchFieldPMMD("Buscar color o código (TL-07, Mostaza…)");
        buscadorPMMD.setPrefWidth(300);
        buscadorPMMD.textProperty().addListener((oPMMD, aPMMD, bPMMD) -> renderPMMD());
        cmbDispPMMD = FormsPMMD.combo(DISPONIBILIDADPMMD, "Todas", sPMMD -> "Disponibilidad: " + sPMMD);
        cmbDispPMMD.setPrefWidth(230);
        cmbDispPMMD.valueProperty().addListener((oPMMD, aPMMD, bPMMD) -> renderPMMD());
        chkMascotasPMMD = new CheckBox("Apta para mascotas");
        chkMascotasPMMD.selectedProperty().addListener((oPMMD, aPMMD, bPMMD) -> renderPMMD());
        chipsPMMD.seleccionProperty().addListener((oPMMD, aPMMD, bPMMD) -> renderPMMD());
        Button calcPMMD = UIPMMD.secondary("Calculadora de tela", "CALCULATE");
        calcPMMD.setOnAction(ePMMD -> calculadoraPMMD(null, null));
        Button nuevaPMMD = UIPMMD.primary("Registrar tela", "ADD");
        nuevaPMMD.setOnAction(ePMMD -> registrar());
        HBox barra = UIPMMD.hbox(10, buscadorPMMD, cmbDispPMMD, chkMascotasPMMD, UIPMMD.spacer(), calcPMMD, nuevaPMMD);
        ScrollPane spPMMD = UIPMMD.scroll(tarjetasPMMD);
        VBox.setVgrow(spPMMD, Priority.ALWAYS);
        raiz.getChildren().setAll(kpisPMMD, barra, chipsPMMD, spPMMD);
    }

    @Override
    public void onShow(IntentPMMD intentPMMD) {
        List<TelaPMMD> todasPMMD = telaServicePMMD.telas();
        List<ChipFilterBarPMMD.Chip> csPMMD = new ArrayList<>();
        csPMMD.add(new ChipFilterBarPMMD.Chip("*", "Todos los tipos", todasPMMD.size()));
        for (TipoTelaPMMD tPMMD : TipoTelaPMMD.values()) csPMMD.add(new ChipFilterBarPMMD.Chip(tPMMD.name(), tPMMD.nombre(), todasPMMD.stream().filter(xPMMD -> xPMMD.getTipo() == tPMMD).count()));
        chipsPMMD.setChips(csPMMD);
        resaltarPMMD = intentPMMD.es(IntentPMMD.ACCION_ABRIR) && intentPMMD.dato() != null ? intentPMMD.dato().toString() : null;
        if (resaltarPMMD != null) {
            buscadorPMMD.setText("");
            chipsPMMD.seleccionar("*");
            cmbDispPMMD.setValue("Todas");
        }
        renderPMMD();
        if (resaltarPMMD != null) telaServicePMMD.porCodigo(resaltarPMMD).ifPresent(this::usos);
    }

    private void renderPMMD() {
        if (cmbDispPMMD == null) return;
        kpisPMMD.getChildren().setAll(UIPMMD.fila(
                new KpiCardPMMD("PALETTE", "blue", "Colores activos", String.valueOf(telaServicePMMD.activas().size()), TipoTelaPMMD.values().length + " tipos de tela"),
                new KpiCardPMMD("STRAIGHTEN", "teal", "Metros en stock", MoneyPMMD.numero(telaServicePMMD.metrosEnStock()) + " m", "Suma del muestrario"),
                new KpiCardPMMD("WARNING", "amber", "Stock bajo o agotado", String.valueOf(telaServicePMMD.conAlerta()), "Colores por reponer"),
                new KpiCardPMMD("ACCOUNT_BALANCE_WALLET", "wood", "Valor del muestrario", MoneyPMMD.fmt0(telaServicePMMD.valorMuestrario()), "A costo por metro")));
        String qPMMD = BusquedaServicePMMD.normalizar(buscadorPMMD.getText());
        String tipoSelPMMD = chipsPMMD.getSeleccion();
        String dispPMMD = cmbDispPMMD.getValue();
        tarjetasPMMD.getChildren().clear();
        for (TipoTelaPMMD tipo : TipoTelaPMMD.values()) {
            if (tipoSelPMMD != null && !tipoSelPMMD.equals("*") && !tipoSelPMMD.equals(tipo.name())) continue;
            if (chkMascotasPMMD.isSelected() && !tipo.mascotas()) continue;
            List<TelaPMMD> coloresPMMD = telaServicePMMD.porTipo(tipo).stream()
                    .filter(tPMMD -> dispPMMD.equals("Todas") || tPMMD.getEstado().equals(dispPMMD))
                    .filter(tPMMD -> qPMMD.isEmpty() || BusquedaServicePMMD.normalizar(tPMMD.getColor() + " " + tPMMD.getCodigo() + " " + tPMMD.getCodigoProveedor()).contains(qPMMD))
                    .toList();
            if (coloresPMMD.isEmpty()) continue;
            tarjetasPMMD.getChildren().add(tarjetaTipoPMMD(tipo, coloresPMMD));
        }
        if (tarjetasPMMD.getChildren().isEmpty()) {
            tarjetasPMMD.getChildren().add(UIPMMD.empty("PALETTE", "Ningún color coincide", "Cambia el tipo, la disponibilidad o la búsqueda.", null));
        }
    }

    private Node tarjetaTipoPMMD(TipoTelaPMMD tipo, List<TelaPMMD> coloresPMMD) {
        List<TelaPMMD> todosPMMD = telaServicePMMD.porTipo(tipo);
        double metrosPMMD = todosPMMD.stream().filter(tPMMD -> !tPMMD.getDescontinuada()).mapToDouble(TelaPMMD::getStock).sum();
        HBox caracteristicas = new HBox(6);
        if (tipo.antimanchas()) caracteristicas.getChildren().add(chip("Antimanchas", "CLEANING_SERVICES"));
        if (tipo.mascotas()) caracteristicas.getChildren().add(chip("Mascotas", "PETS"));
        if (tipo.impermeable()) caracteristicas.getChildren().add(chip("Impermeable", "OPACITY"));
        if (!tipo.permiteGiro()) caracteristicas.getChildren().add(chip("Sentido del pelo", "SWAP_VERT"));
        ColorSwatchPMMD muestra = new ColorSwatchPMMD(coloresPMMD.get(0).getHex(), tipo, 40);
        VBox titulo = new VBox(2, UIPMMD.label(tipo.nombre(), "tela-tipo"),
                UIPMMD.label("S/ " + (int) tipo.costoMetro() + "/m · recargo al cliente " + (int) tipo.recargoPct() + " % · Martindale "
                        + MoneyPMMD.entero(tipo.martindale()) + " ciclos", "cell-sub"));
        VBox totales = new VBox(2, UIPMMD.label(todosPMMD.stream().filter(tPMMD -> !tPMMD.getDescontinuada()).count() + " colores · " + MoneyPMMD.numero(metrosPMMD) + " m", "cell-title"),
                UIPMMD.label("Material " + tipo.materialCodigo(), "code"));
        totales.setAlignment(Pos.CENTER_RIGHT);
        HBox cabPMMD = new HBox(14, muestra, titulo, caracteristicas, UIPMMD.spacer(), totales);
        cabPMMD.getStyleClass().add("tela-header");
        FlowPane gridPMMD = new FlowPane(12, 12);
        for (TelaPMMD tPMMD : coloresPMMD) gridPMMD.getChildren().add(muestra(tPMMD));
        return UIPMMD.card(cabPMMD, gridPMMD);
    }

    private Label chip(String textoPMMD, String iconoPMMD) {
        Label lPMMD = UIPMMD.label(textoPMMD, "feature-chip");
        lPMMD.setGraphic(IcoPMMD.of(iconoPMMD, 12));
        return lPMMD;
    }

    private Node muestra(TelaPMMD tPMMD) {
        ColorSwatchPMMD sw = new ColorSwatchPMMD(tPMMD.getHex(), tPMMD.getTipo(), 52);
        double ratio = tPMMD.getMinimo() <= 0 ? 1 : tPMMD.getStock() / (tPMMD.getMinimo() * 2.5);
        String color = switch (tPMMD.getEstado()) {
            case "Disponible" -> "#16A34A";
            case "Stock bajo" -> "#F59E0B";
            case "Descontinuada" -> "#94A3B8";
            default -> "#DC2626";
        };
        ProgressBarMiniPMMD barra = new ProgressBarMiniPMMD(ratio, color, 6);
        barra.setPrefWidth(120);
        Tooltip.install(barra, new Tooltip(MoneyPMMD.numero(tPMMD.getStock()) + " m · mínimo " + MoneyPMMD.numero(tPMMD.getMinimo()) + " m"));
        Button rolloPMMD = UIPMMD.button("Rollo", "ADD", "btn-sm", "btn-secondary");
        rolloPMMD.setDisable(tPMMD.getDescontinuada());
        rolloPMMD.setOnAction(ePMMD -> entradaRollo(tPMMD));
        rolloPMMD.setStyle("-fx-padding: 0 10 0 8;");
        Button sofasPMMD = UIPMMD.button("Sofás", null, "btn-sm", "btn-ghost");
        sofasPMMD.setStyle("-fx-padding: 0 8;");
        sofasPMMD.setOnAction(ePMMD -> usos(tPMMD));
        Button estadoPMMD = UIPMMD.iconButton(tPMMD.getDescontinuada() ? "RESTORE" : "BLOCK", tPMMD.getDescontinuada() ? "Reactivar color" : "Descontinuar color", "btn-ghost", "btn-xs");
        estadoPMMD.setOnAction(ePMMD -> alternarPMMD(tPMMD));
        VBox tilePMMD = new VBox(7, sw, UIPMMD.label(tPMMD.getColor(), "swatch-name"), UIPMMD.codigo(tPMMD.getCodigo() + " · " + tPMMD.getCodigoProveedor()),
                new StatusBadgePMMD(tPMMD.getEstado()), UIPMMD.hbox(6, UIPMMD.label(MoneyPMMD.numero(tPMMD.getStock()) + " m", "cell-title"), UIPMMD.spacer(),
                UIPMMD.label("mín. " + MoneyPMMD.numero(tPMMD.getMinimo()), "cell-sub")), barra, UIPMMD.hbox(4, rolloPMMD, sofasPMMD, UIPMMD.spacer(), estadoPMMD));
        tilePMMD.getStyleClass().add("swatch-tile");
        if (tPMMD.getDescontinuada()) tilePMMD.getStyleClass().add("swatch-tile-off");
        if (tPMMD.getCodigo().equals(resaltarPMMD)) tilePMMD.setStyle("-fx-border-color: -is-primary; -fx-border-width: 2;");
        tilePMMD.setPrefWidth(196);
        tilePMMD.setMaxWidth(196);
        tilePMMD.setMinWidth(196);
        return tilePMMD;
    }

    private void alternarPMMD(TelaPMMD tPMMD) {
        boolean descPMMD = !tPMMD.getDescontinuada();
        confirmPMMD.mostrar(descPMMD ? ConfirmDialogPMMD.Tipo.PELIGRO : ConfirmDialogPMMD.Tipo.INFO,
                (descPMMD ? "¿Descontinuar " : "¿Reactivar ") + tPMMD.getNombre() + "?",
                descPMMD ? "El color deja de ofrecerse y sus " + MoneyPMMD.numero(tPMMD.getStock()) + " m dejan de sumar al stock del material " + tPMMD.getTipo().materialCodigo() + "."
                        : "El color vuelve al muestrario y sus metros suman otra vez al inventario.",
                descPMMD ? "Descontinuar" : "Reactivar", () -> {
                    if (toastPMMD.intentar(() -> telaServicePMMD.alternarDescontinuada(tPMMD.getId()))) {
                        toastPMMD.exito(tPMMD.getNombre() + (descPMMD ? " descontinuada." : " reactivada."));
                        renderPMMD();
                        navigatorPMMD.datosCambiaron();
                    }
                });
    }

    private void entradaRollo(TelaPMMD tPMMD) {
        ValidacionPMMD vPMMD = new ValidacionPMMD();
        TextField metrosPMMD = FormsPMMD.decimal(30);
        TextField refPMMD = UIPMMD.text("", "Guía o factura (opcional)");
        Label resultado = UIPMMD.label("", "muted");
        Runnable prevPMMD = () -> resultado.setText("Quedará en " + MoneyPMMD.numero(tPMMD.getStock() + ValidacionPMMD.valor(metrosPMMD)) + " m (hoy " + MoneyPMMD.numero(tPMMD.getStock()) + " m).");
        metrosPMMD.textProperty().addListener((oPMMD, aPMMD, bPMMD) -> prevPMMD.run());
        prevPMMD.run();
        VBox cuerpoPMMD = new VBox(14, UIPMMD.hbox(12, new ColorSwatchPMMD(tPMMD.getHex(), tPMMD.getTipo(), 44), new VBox(2, UIPMMD.label(tPMMD.getNombre(), "card-title"), UIPMMD.codigo(tPMMD.getCodigo()))),
                vPMMD.campo("Metros recibidos", metrosPMMD, () -> ValidacionPMMD.numero(metrosPMMD, 0.1, "Los metros")), vPMMD.campo("Referencia", refPMMD, null), resultado,
                UIPMMD.wrap("Suma al color y al material " + tPMMD.getTipo().materialCodigo() + " del inventario, y registra una Entrada en el kardex.", "cell-sub"));
        Button cancelarPMMD = UIPMMD.secondary("Cancelar", null);
        Button okPMMD = UIPMMD.primary("Registrar entrada", "MOVE_TO_INBOX");
        vPMMD.enlazar(okPMMD);
        var hPMMD = overlaysPMMD.modal("Entrada de rollo", tPMMD.getTipo().nombre(), cuerpoPMMD, 440, cancelarPMMD, okPMMD);
        cancelarPMMD.setOnAction(ePMMD -> hPMMD.cerrar());
        okPMMD.setOnAction(ePMMD -> {
            if (toastPMMD.intentar(() -> telaServicePMMD.entradaRollo(tPMMD.getId(), ValidacionPMMD.valor(metrosPMMD), refPMMD.getText()))) {
                hPMMD.cerrar();
                toastPMMD.exito("+" + metrosPMMD.getText() + " m de " + tPMMD.getNombre() + " · kardex " + tPMMD.getTipo().materialCodigo() + " actualizado.");
                renderPMMD();
                navigatorPMMD.datosCambiaron();
            }
        });
    }

    private void usos(TelaPMMD tPMMD) {
        TelaServicePMMD.Usos uPMMD = telaServicePMMD.usos(tPMMD);
        VBox cuerpoPMMD = new VBox(14);
        cuerpoPMMD.getChildren().add(UIPMMD.hbox(12, new ColorSwatchPMMD(tPMMD.getHex(), tPMMD.getTipo(), 48),
                new VBox(2, UIPMMD.label(tPMMD.getNombre(), "card-title"), UIPMMD.label(MoneyPMMD.numero(tPMMD.getStock()) + " m · " + tPMMD.getEstado(), "cell-sub"))));
        cuerpoPMMD.getChildren().add(seccionPMMD("Modelos del catálogo", uPMMD.productos().stream().map(pPMMD -> fila("WEEKEND", pPMMD.getNombre(), pPMMD.getCodigo() + " · " + MoneyPMMD.fmt(pPMMD.getPrecio()),
                () -> navigatorPMMD.goTo("modelos", IntentPMMD.abrir(pPMMD.getCodigo())))).toList()));
        cuerpoPMMD.getChildren().add(seccionPMMD("Pedidos", uPMMD.pedidos().stream().map(pPMMD -> fila("ASSIGNMENT", pPMMD.getCodigo() + " · " + pPMMD.getProductoNombre(),
                pPMMD.getCliente().getNombre() + " · " + pPMMD.getEstado().nombre(), () -> navigatorPMMD.goTo("pedidos", IntentPMMD.abrir(pPMMD.getCodigo())))).toList()));
        cuerpoPMMD.getChildren().add(seccionPMMD("Órdenes de producción", uPMMD.ordenes().stream().map(oPMMD -> fila("BUILD", oPMMD.getCodigo() + " · " + oPMMD.getProductoNombre() + " ×" + oPMMD.getCantidad(),
                oPMMD.getClienteNombre() + " · " + oPMMD.getEtapa().nombre(), () -> navigatorPMMD.goTo("produccion", IntentPMMD.abrir(oPMMD.getCodigo())))).toList()));
        overlaysPMMD.panel("Sofás con esta tela", uPMMD.total() + " usos en catálogo, pedidos y producción", cuerpoPMMD, null, 440);
    }

    private Node seccionPMMD(String titulo, List<Node> filasPMMD) {
        VBox vPMMD = new VBox(4, UIPMMD.label(titulo + " (" + filasPMMD.size() + ")", "card-title"));
        if (filasPMMD.isEmpty()) vPMMD.getChildren().add(UIPMMD.label("Sin registros.", "muted"));
        vPMMD.getChildren().addAll(filasPMMD);
        return vPMMD;
    }

    private Node fila(String iconoPMMD, String titulo, String subPMMD, Runnable rPMMD) {
        StackPane ic = new StackPane(IcoPMMD.of(iconoPMMD, 16));
        ic.getStyleClass().add("popup-item-icon");
        HBox hPMMD = new HBox(10, ic, new VBox(1, UIPMMD.label(titulo, "cell-title"), UIPMMD.label(subPMMD, "cell-sub")), UIPMMD.spacer(), IcoPMMD.of("CHEVRON_RIGHT", 16));
        hPMMD.getStyleClass().add("alert-row");
        hPMMD.setOnMouseClicked(ePMMD -> rPMMD.run());
        return hPMMD;
    }

    private void registrar() {
        ValidacionPMMD vPMMD = new ValidacionPMMD();
        ComboBox<TipoTelaPMMD> tipo = FormsPMMD.combo(List.of(TipoTelaPMMD.values()), TipoTelaPMMD.CHENILLE, TipoTelaPMMD::nombre);
        TextField color = UIPMMD.text("", "Nombre del color (p. ej. Vino)");
        ColorPicker pickerPMMD = new ColorPicker(javafx.scene.paint.Color.web("#7A3B3B"));
        pickerPMMD.setMaxWidth(Double.MAX_VALUE);
        TextField codigoPMMD = UIPMMD.text("", "Código del proveedor (CH-210 VIN)");
        TextField stockPMMD = FormsPMMD.decimal(0);
        TextField minimoPMMD = FormsPMMD.decimal(8);
        ColorSwatchPMMD vistaPMMD = new ColorSwatchPMMD("#7A3B3B", TipoTelaPMMD.CHENILLE, 64);
        Runnable actPMMD = () -> vistaPMMD.pintar(hexPMMD(pickerPMMD.getValue()), tipo.getValue());
        pickerPMMD.valueProperty().addListener((oPMMD, aPMMD, bPMMD) -> actPMMD.run());
        tipo.valueProperty().addListener((oPMMD, aPMMD, bPMMD) -> actPMMD.run());
        FormGridPMMD f = new FormGridPMMD(2);
        f.nodo(UIPMMD.hbox(14, vistaPMMD, UIPMMD.wrap("La muestra aplica la textura del tipo de tela elegido.", "muted")), 2);
        f.nodo(vPMMD.campo("Tipo de tela", tipo, () -> ValidacionPMMD.requerido(tipo, "el tipo")), 1);
        f.nodo(vPMMD.campo("Nombre del color", color, () -> {
            String ePMMD = ValidacionPMMD.requerido(color, "el nombre del color");
            if (ePMMD != null) return ePMMD;
            return telaServicePMMD.porTipo(tipo.getValue()).stream().anyMatch(tPMMD -> tPMMD.getColor().equalsIgnoreCase(color.getText().trim()))
                    ? "Ya existe ese color en " + tipo.getValue().nombre() + "." : null;
        }), 1);
        f.nodo(vPMMD.campo("Color", pickerPMMD, null), 1);
        f.nodo(vPMMD.campo("Código del proveedor", codigoPMMD, null), 1);
        f.nodo(vPMMD.campo("Stock inicial (m)", stockPMMD, () -> ValidacionPMMD.numero(stockPMMD, 0, "El stock")), 1);
        f.nodo(vPMMD.campo("Mínimo (m)", minimoPMMD, () -> ValidacionPMMD.numero(minimoPMMD, 0, "El mínimo")), 1);
        vPMMD.escuchar(tipo.valueProperty());
        Button cancelarPMMD = UIPMMD.secondary("Cancelar", null);
        Button okPMMD = UIPMMD.primary("Registrar tela", "SAVE");
        vPMMD.enlazar(okPMMD);
        var hPMMD = overlaysPMMD.modal("Registrar tela", "Nuevo color del muestrario", f, 560, cancelarPMMD, okPMMD);
        cancelarPMMD.setOnAction(ePMMD -> hPMMD.cerrar());
        okPMMD.setOnAction(ePMMD -> {
            if (toastPMMD.intentar(() -> {
                TelaPMMD tPMMD = telaServicePMMD.registrar(tipo.getValue(), color.getText(), hexPMMD(pickerPMMD.getValue()), codigoPMMD.getText(),
                        ValidacionPMMD.valor(stockPMMD), ValidacionPMMD.valor(minimoPMMD));
                toastPMMD.exito(tPMMD.getCodigo() + " · " + tPMMD.getNombre() + " registrada.");
            })) {
                hPMMD.cerrar();
                onShow(IntentPMMD.NONE);
            }
        });
    }

    private static String hexPMMD(javafx.scene.paint.Color cPMMD) {
        return String.format("#%02X%02X%02X", (int) (cPMMD.getRed() * 255), (int) (cPMMD.getGreen() * 255), (int) (cPMMD.getBlue() * 255));
    }



    private void calculadoraPMMD(ProductoPMMD inicialPMMD, TelaPMMD telaInicialPMMD) {
        List<ProductoPMMD> conFichaPMMD = productoServicePMMD.listar().stream().filter(pPMMD -> fichaServicePMMD.aprobadaDe(pPMMD) != null).toList();
        ComboBox<ProductoPMMD> modeloPMMD = FormsPMMD.combo(conFichaPMMD, inicialPMMD == null ? conFichaPMMD.get(0) : inicialPMMD, pPMMD -> pPMMD.getCodigo() + " · " + pPMMD.getNombre());
        Spinner<Integer> cantidad = new Spinner<>(1, 50, 1);
        cantidad.setEditable(true);
        cantidad.setMaxWidth(Double.MAX_VALUE);
        ComboBox<TelaPMMD> telaPMMD = FormsPMMD.combo(telaServicePMMD.activas(), telaInicialPMMD == null ? telaServicePMMD.activas().get(0) : telaInicialPMMD, TelaPMMD::getNombre);
        VBox res = new VBox(10);
        Button planoPMMD = UIPMMD.secondary("Ver plano de corte", "CONTENT_CUT");
        Runnable calcularPMMD = () -> {
            ProductoPMMD pPMMD = modeloPMMD.getValue();
            TelaPMMD tPMMD = telaPMMD.getValue();
            if (pPMMD == null || tPMMD == null) return;
            FichaTecnicaPMMD f = fichaServicePMMD.aprobadaDe(pPMMD);
            int nPMMD = cantidad.getValue();
            Map<String, String> sustPMMD = new HashMap<>();
            for (TipoTelaPMMD ttPMMD : TipoTelaPMMD.values()) sustPMMD.put(ttPMMD.materialCodigo(), tPMMD.getTipo().materialCodigo());
            ExplosionPMMD.Resultado rPMMD = fichaServicePMMD.explotar(f, nPMMD, Opciones.sustituir(sustPMMD));
            double metrosFichaPMMD = rPMMD.porMaterial().stream().filter(xPMMD -> xPMMD.material().isTela()).mapToDouble(ExplosionPMMD.PorMaterial::cantidad).sum();
            double metrosPlanoPMMD = corteServicePMMD.metrosPlano(f, nPMMD, tPMMD.getTipo()) * 1.10;
            double necesarioPMMD = Math.max(metrosFichaPMMD, metrosPlanoPMMD);
            double faltaPMMD = Math.max(0, necesarioPMMD - tPMMD.getStock());
            double costoPMMD = necesarioPMMD * tPMMD.getTipo().costoMetro();
            double recargoPMMD = pPMMD.getPrecio().doubleValue() * nPMMD * tPMMD.getTipo().recargoPct() / 100;
            Label lf = UIPMMD.label(MoneyPMMD.numero(faltaPMMD) + " m", "mini-stat-value");
            if (faltaPMMD > 0) lf.setStyle("-fx-text-fill: -is-danger;");
            VBox faltantePMMD = new VBox(2, lf, UIPMMD.label("Faltante", "mini-stat-label"));
            faltantePMMD.getStyleClass().add("mini-stat");
            HBox.setHgrow(faltantePMMD, Priority.ALWAYS);
            res.getChildren().setAll(
                    UIPMMD.hbox(10, UIPMMD.miniStat("Según la ficha " + f.getEtiqueta() + " (con merma)", MoneyPMMD.numero(metrosFichaPMMD) + " m"),
                            UIPMMD.miniStat("Según el plano de corte + 10 %", MoneyPMMD.numero(metrosPlanoPMMD) + " m")),
                    UIPMMD.hbox(10, UIPMMD.miniStat("Stock de " + tPMMD.getColor(), MoneyPMMD.numero(tPMMD.getStock()) + " m"), faltantePMMD),
                    UIPMMD.hbox(10, UIPMMD.miniStat("Costo de tela", MoneyPMMD.fmt(costoPMMD)), UIPMMD.miniStat("Recargo al cliente (" + (int) tPMMD.getTipo().recargoPct() + " %)", MoneyPMMD.fmt(recargoPMMD))),
                    faltaPMMD > 0 ? UIPMMD.banner("danger-banner", "WARNING", "No alcanza: faltan " + MoneyPMMD.numero(faltaPMMD) + " m de " + tPMMD.getNombre() + ". Registra un rollo o genera la compra.")
                            : UIPMMD.banner("ok-banner", "CHECK_CIRCLE", "Alcanza el stock del color."));
            planoPMMD.setOnAction(ePMMD -> navigatorPMMD.goTo("corte", IntentPMMD.preseleccionar(f.getUid())));
        };
        modeloPMMD.valueProperty().addListener((oPMMD, aPMMD, bPMMD) -> calcularPMMD.run());
        cantidad.valueProperty().addListener((oPMMD, aPMMD, bPMMD) -> calcularPMMD.run());
        telaPMMD.valueProperty().addListener((oPMMD, aPMMD, bPMMD) -> calcularPMMD.run());
        calcularPMMD.run();
        FormGridPMMD f = new FormGridPMMD(3);
        f.campo("Modelo", modeloPMMD, 3);
        f.campo("Cantidad", cantidad, 1);
        f.campo("Tela y color", telaPMMD, 2);
        VBox cuerpoPMMD = new VBox(16, f, res);
        Button cerrarPMMD = UIPMMD.secondary("Cerrar", null);
        var hPMMD = overlaysPMMD.modal("Calculadora de tela", "Metros por modelo, cantidad y color", cuerpoPMMD, 620, planoPMMD, UIPMMD.spacer(), cerrarPMMD);
        cerrarPMMD.setOnAction(ePMMD -> hPMMD.cerrar());
    }
}
