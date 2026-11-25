package pe.erp.sofas.ui.views.produccion;

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
import pe.erp.sofas.service.calculo.MedidasPMMD;
import pe.erp.sofas.service.calculo.Opciones;
import pe.erp.sofas.ui.AppWindowPMMD;
import pe.erp.sofas.ui.components.*;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;






@Component
@Lazy
@Scope("prototype")
@RequiredArgsConstructor
public class FichasControllerPMMD implements VistaPMMD {

    private final FichaServicePMMD fichaServicePMMD;

    private final ProductoServicePMMD productoServicePMMD;

    private final InventarioServicePMMD inventarioServicePMMD;

    private final ExportServicePMMD exportServicePMMD;

    private final ParametroServicePMMD parametrosPMMD;

    private final CalculadoraMaterialesPMMD calculadoraPMMD;

    private final OverlaysPMMD overlaysPMMD;

    private final ToastPMMD toastPMMD;

    private final ConfirmDialogPMMD confirmPMMD;

    private final NavigatorPMMD navigatorPMMD;

    private final AppWindowPMMD appWindowPMMD;

    @FXML private VBox raiz;

    private SearchFieldPMMD buscadorPMMD;

    private final ChipFilterBarPMMD chipsPMMD = new ChipFilterBarPMMD();

    private final VBox listaPMMD = new VBox(8);

    private final VBox editorPMMD = new VBox(14);

    private final VBox resumenPMMD = new VBox(14);

    private Long seleccionPMMD;

    private TipoTelaPMMD telaJuegoPMMD;

    @FXML
    public void initialize() {
        buscadorPMMD = new SearchFieldPMMD("Buscar ficha o modelo");
        buscadorPMMD.textProperty().addListener((oPMMD, aPMMD, bPMMD) -> renderListaPMMD());
        chipsPMMD.setChips(List.of(new ChipFilterBarPMMD.Chip("*", "Todas", -1), new ChipFilterBarPMMD.Chip("APROBADA", "Aprobada", -1),
                new ChipFilterBarPMMD.Chip("BORRADOR", "Borrador", -1), new ChipFilterBarPMMD.Chip("OBSOLETA", "Obsoleta", -1)));
        chipsPMMD.seleccionProperty().addListener((oPMMD, aPMMD, bPMMD) -> renderListaPMMD());
        Button nuevaPMMD = UIPMMD.button("Nueva ficha", "ADD", "btn-sm", "btn-secondary");
        nuevaPMMD.setOnAction(ePMMD -> nuevaFichaPMMD());
        ScrollPane spLista = UIPMMD.scroll(listaPMMD);
        VBox.setVgrow(spLista, Priority.ALWAYS);
        VBox izq = new VBox(10, UIPMMD.hbox(8, UIPMMD.label("Fichas", "card-title"), UIPMMD.spacer(), nuevaPMMD), buscadorPMMD, chipsPMMD, spLista);
        izq.getStyleClass().add("card");
        izq.setPadding(new javafx.geometry.Insets(14));
        izq.setPrefWidth(236);
        izq.setMinWidth(236);
        izq.setMaxWidth(236);
        ScrollPane spEditor = UIPMMD.scroll(editorPMMD);
        spEditor.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        ScrollPane spResumen = UIPMMD.scroll(resumenPMMD);
        spResumen.setPrefWidth(268);
        spResumen.setMinWidth(268);
        spResumen.setMaxWidth(268);
        HBox cuerpoPMMD = new HBox(14, izq, spEditor, spResumen);
        HBox.setHgrow(spEditor, Priority.ALWAYS);
        VBox.setVgrow(cuerpoPMMD, Priority.ALWAYS);
        raiz.getChildren().setAll(new ProduccionTabsPMMD(ModuloPMMD.FICHAS, navigatorPMMD, calculadoraPMMD::abrir), cuerpoPMMD);
    }

    @Override
    public void onShow(IntentPMMD intentPMMD) {
        if (intentPMMD.es(IntentPMMD.ACCION_ABRIR) && intentPMMD.dato() != null) {
            fichaServicePMMD.porUid(intentPMMD.dato().toString()).ifPresent(fPMMD -> seleccionPMMD = fPMMD.getId());
        } else if (intentPMMD.es(IntentPMMD.ACCION_PRESELECCIONAR) && intentPMMD.dato() != null) {
            productoServicePMMD.porCodigo(intentPMMD.dato().toString()).ifPresent(pPMMD -> {
                FichaTecnicaPMMD fPMMD = fichaServicePMMD.ultimaDe(pPMMD);
                if (fPMMD != null) seleccionPMMD = fPMMD.getId();
                else toastPMMD.info(pPMMD.getNombre() + " no tiene ficha: usa «Nueva ficha» para crearla.");
            });
        }
        if (seleccionPMMD == null) {
            FichaTecnicaPMMD fPMMD = fichaServicePMMD.aprobada("FT-001");
            if (fPMMD != null) seleccionPMMD = fPMMD.getId();
        }
        renderListaPMMD();
        renderEditorPMMD();
    }

    private void seleccionarPMMD(Long idPMMD) {
        seleccionPMMD = idPMMD;
        renderListaPMMD();
        renderEditorPMMD();
    }



    private void renderListaPMMD() {
        String qPMMD = BusquedaServicePMMD.normalizar(buscadorPMMD.getText());
        String estPMMD = chipsPMMD.getSeleccion();
        listaPMMD.getChildren().clear();
        for (FichaTecnicaPMMD fPMMD : fichaServicePMMD.todas()) {
            if (estPMMD != null && !estPMMD.equals("*") && !fPMMD.getEstado().name().equals(estPMMD)) continue;
            if (!qPMMD.isEmpty() && !BusquedaServicePMMD.normalizar(fPMMD.getCodigo() + " " + fPMMD.getNombre()).contains(qPMMD)) continue;
            double costoPMMD = costoSeguroPMMD(fPMMD);
            VBox cPMMD = new VBox(4,
                    UIPMMD.hbox(6, UIPMMD.codigo(fPMMD.getEtiqueta()), UIPMMD.spacer(), new StatusBadgePMMD(fPMMD.getEstado().nombre())),
                    UIPMMD.label(fPMMD.getNombre(), "cell-title"),
                    UIPMMD.hbox(6, Boolean.TRUE.equals(fPMMD.getCompuesta()) ? new StatusBadgePMMD("Compuesta") : new Region(), UIPMMD.spacer(),
                            UIPMMD.label(costoPMMD > 0 ? MoneyPMMD.fmt(costoPMMD) : "—", "cell-sub")));
            cPMMD.getStyleClass().addAll("card", "ficha-card");
            if (fPMMD.getId().equals(seleccionPMMD)) cPMMD.getStyleClass().add("ficha-card-selected");
            cPMMD.setOnMouseClicked(ePMMD -> seleccionarPMMD(fPMMD.getId()));
            listaPMMD.getChildren().add(cPMMD);
        }
        if (listaPMMD.getChildren().isEmpty()) listaPMMD.getChildren().add(UIPMMD.label("Sin fichas con ese filtro.", "muted"));
    }

    private double costoSeguroPMMD(FichaTecnicaPMMD fPMMD) {
        try {
            return fichaServicePMMD.costoEstandar(fPMMD);
        } catch (RuntimeException ePMMD) {
            return 0;
        }
    }



    private void renderEditorPMMD() {
        editorPMMD.getChildren().clear();
        resumenPMMD.getChildren().clear();
        if (seleccionPMMD == null) {
            editorPMMD.getChildren().add(UIPMMD.empty("DESCRIPTION", "Elige una ficha", "Selecciona una ficha de la lista para ver su BOM.", null));
            return;
        }
        FichaTecnicaPMMD fPMMD = fichaServicePMMD.ficha(seleccionPMMD);
        ExplosionPMMD.Resultado basePMMD = fichaServicePMMD.explotar(fPMMD);
        ExplosionPMMD.Resultado conOpcionalesPMMD = fichaServicePMMD.explotar(fPMMD, 1, new Opciones(null, Map.of(), List.of(), true, 0, 0));

        StackPane iconoPMMD = new StackPane(IcoPMMD.of(Boolean.TRUE.equals(fPMMD.getCompuesta()) ? "WIDGETS" : "WEEKEND", 22));
        iconoPMMD.getStyleClass().addAll("kpi-icon", "kpi-wood");
        String aprobadoPMMD = fPMMD.getAprobadoPor() == null ? "Sin aprobar" : "Aprobada por " + fPMMD.getAprobadoPor() + " el " + AppClockPMMD.fmt(fPMMD.getFechaAprobacion());
        VBox tituloPMMD = new VBox(3, UIPMMD.hbox(8, UIPMMD.label(fPMMD.getNombre(), "card-title"), new StatusBadgePMMD(fPMMD.getEstado().nombre())),
                UIPMMD.label(fPMMD.getCodigo() + " · versión " + fPMMD.getVersion() + " · " + fPMMD.getLargo() + " × " + fPMMD.getFondo() + " × " + fPMMD.getAlto() + " cm · "
                        + fPMMD.getCuerpos() + (fPMMD.getCuerpos() == 1 ? " cuerpo" : " cuerpos") + (Boolean.TRUE.equals(fPMMD.getChaise()) ? " · con chaise" : ""), "cell-sub"),
                UIPMMD.label(aprobadoPMMD, "cell-sub"));
        Button simular = UIPMMD.button("Simular medida", "STRAIGHTEN", "btn-sm", "btn-secondary");
        simular.setDisable(Boolean.TRUE.equals(fPMMD.getCompuesta()));
        simular.setOnAction(ePMMD -> simular(fPMMD));
        Button duplicar = UIPMMD.button("Duplicar como nueva versión", "CONTENT_COPY", "btn-sm", "btn-secondary");
        duplicar.setOnAction(ePMMD -> {
            FichaTecnicaPMMD[] nPMMD = new FichaTecnicaPMMD[1];
            if (toastPMMD.intentar(() -> nPMMD[0] = fichaServicePMMD.duplicar(fPMMD.getId()))) {
                toastPMMD.exito(nPMMD[0].getEtiqueta() + " creada en Borrador a partir de " + fPMMD.getEtiqueta() + ".");
                seleccionarPMMD(nPMMD[0].getId());
            }
        });
        Button aprobar = UIPMMD.button("Aprobar", "VERIFIED", "btn-sm", "btn-success");
        aprobar.setDisable(fPMMD.getEstado() == EstadoFichaPMMD.APROBADA);
        aprobar.setOnAction(ePMMD -> confirmPMMD.mostrar(ConfirmDialogPMMD.Tipo.INFO, "¿Aprobar " + fPMMD.getEtiqueta() + "?",
                "Será la versión vigente para nuevas órdenes, cotizaciones y el costo estándar. La versión aprobada anterior pasará a Obsoleta; "
                        + "las órdenes en curso conservan su versión.", "Aprobar", () -> {
                    if (toastPMMD.intentar(() -> fichaServicePMMD.aprobar(fPMMD.getId()))) {
                        toastPMMD.exito(fPMMD.getEtiqueta() + " aprobada.");
                        seleccionarPMMD(fPMMD.getId());
                    }
                }));
        Button obsoleta = UIPMMD.button("Marcar obsoleta", "BLOCK", "btn-sm", "btn-ghost");
        obsoleta.setDisable(fPMMD.getEstado() == EstadoFichaPMMD.OBSOLETA);
        obsoleta.setOnAction(ePMMD -> confirmPMMD.mostrar("¿Marcar " + fPMMD.getEtiqueta() + " como obsoleta?",
                "Dejará de usarse para nuevas órdenes y cotizaciones" + (fPMMD.getEstado() == EstadoFichaPMMD.APROBADA ? "; el modelo quedará sin ficha aprobada." : "."),
                "Marcar obsoleta", () -> {
                    if (toastPMMD.intentar(() -> fichaServicePMMD.marcarObsoleta(fPMMD.getId()))) {
                        toastPMMD.exito(fPMMD.getEtiqueta() + " marcada como obsoleta.");
                        seleccionarPMMD(fPMMD.getId());
                    }
                }));
        Button pdf = UIPMMD.button("Exportar ficha (PDF)", "PICTURE_AS_PDF", "btn-sm", "btn-ghost");
        pdf.setOnAction(ePMMD -> exportarPMMD(fPMMD, basePMMD));
        HBox cabPMMD = UIPMMD.hbox(12, iconoPMMD, tituloPMMD);
        FlowPane accionesPMMD = new FlowPane(8, 8, simular, duplicar, aprobar, obsoleta, pdf);
        VBox cardPMMD = UIPMMD.card(cabPMMD, accionesPMMD);
        editorPMMD.getChildren().add(cardPMMD);
        if (!fPMMD.isEditable()) {
            editorPMMD.getChildren().add(UIPMMD.banner("lock-banner", "LOCK", "Esta versión está " + fPMMD.getEstado().nombre().toLowerCase()
                    + ": al editar cualquier celda se crea automáticamente la versión " + (maxVersionPMMD(fPMMD) + 1) + " en Borrador con el cambio. "
                    + "Las órdenes en curso conservan su versión."));
        }
        if (fPMMD.getNotas() != null) editorPMMD.getChildren().add(UIPMMD.wrap(fPMMD.getNotas(), "muted"));

        if (Boolean.TRUE.equals(fPMMD.getCompuesta())) editorPMMD.getChildren().add(compuestaPMMD(fPMMD, basePMMD));
        else {
            editorPMMD.getChildren().add(bom(fPMMD, conOpcionalesPMMD));
            editorPMMD.getChildren().add(manoObraPMMD(fPMMD, basePMMD));
        }
        renderResumenPMMD(fPMMD, basePMMD);
    }

    private int maxVersionPMMD(FichaTecnicaPMMD fPMMD) {
        return fichaServicePMMD.todas().stream().filter(x -> x.getCodigo().equals(fPMMD.getCodigo())).mapToInt(FichaTecnicaPMMD::getVersion).max().orElse(fPMMD.getVersion());
    }



    private void aplicarPMMD(java.util.function.Supplier<FichaServicePMMD.Edicion> edicionPMMD, String descripcionPMMD) {
        FichaServicePMMD.Edicion[] r = new FichaServicePMMD.Edicion[1];
        if (toastPMMD.intentar(() -> r[0] = edicionPMMD.get())) {
            if (r[0].nuevaVersion()) {
                toastPMMD.advertencia("Se creó " + r[0].ficha().getEtiqueta() + " en Borrador con el cambio (" + descripcionPMMD + "). La versión anterior no se modificó.");
            }
            seleccionarPMMD(r[0].ficha().getId());
        }
    }

    private Node bom(FichaTecnicaPMMD fPMMD, ExplosionPMMD.Resultado r) {
        VBox vPMMD = new VBox(6);
        vPMMD.getStyleClass().add("card");
        vPMMD.setPadding(new javafx.geometry.Insets(16));
        vPMMD.getChildren().add(UIPMMD.cardHeader("Lista de materiales (BOM)", fPMMD.getLineas().size() + " líneas · agrupadas por componente"));
        HBox headPMMD = fila(UIPMMD.label("MATERIAL"), UIPMMD.label("BASE"), UIPMMD.label("MERMA"), UIPMMD.label("FÓRMULA"),
                UIPMMD.label("BRUTO"), UIPMMD.label("COSTO"), UIPMMD.label("OPC."), UIPMMD.label(""));
        headPMMD.getStyleClass().setAll("bom-head");
        vPMMD.getChildren().add(headPMMD);
        Map<ComponentePMMD, List<Integer>> gruposPMMD = new LinkedHashMap<>();
        for (ComponentePMMD cPMMD : ComponentePMMD.values()) gruposPMMD.put(cPMMD, new ArrayList<>());
        for (int iPMMD = 0; iPMMD < fPMMD.getLineas().size(); iPMMD++) gruposPMMD.get(fPMMD.getLineas().get(iPMMD).getComponente()).add(iPMMD);
        for (var gPMMD : gruposPMMD.entrySet()) {
            if (gPMMD.getValue().isEmpty()) continue;
            double totalPMMD = gPMMD.getValue().stream().mapToDouble(iPMMD -> r.lineas().get(iPMMD).costo()).sum();
            Region puntoPMMD = new Region();
            puntoPMMD.getStyleClass().add("legend-dot");
            puntoPMMD.setStyle("-fx-background-color: " + gPMMD.getKey().color() + ";");
            HBox ghPMMD = UIPMMD.hbox(8, puntoPMMD, UIPMMD.label(gPMMD.getKey().nombre(), "cell-title"), UIPMMD.label(gPMMD.getValue().size() + (gPMMD.getValue().size() == 1 ? " línea" : " líneas"), "cell-sub"),
                    UIPMMD.spacer(), UIPMMD.label(MoneyPMMD.fmt(totalPMMD), "cell-title"));
            ghPMMD.getStyleClass().add("bom-group");
            vPMMD.getChildren().add(ghPMMD);
            for (int iPMMD : gPMMD.getValue()) vPMMD.getChildren().add(filaBomPMMD(fPMMD, iPMMD, r.lineas().get(iPMMD)));
        }

        ComboBox<MaterialPMMD> matPMMD = FormsPMMD.combo(inventarioServicePMMD.materiales(), null, mPMMD -> mPMMD.getCodigo() + " · " + mPMMD.getNombre());
        matPMMD.setPromptText("Material");
        ComboBox<ComponentePMMD> comp = FormsPMMD.combo(List.of(ComponentePMMD.values()), ComponentePMMD.TAPIZADO, ComponentePMMD::nombre);
        TextField cantPMMD = FormsPMMD.decimal(1);
        cantPMMD.setPrefWidth(80);
        Button agregarPMMD = UIPMMD.button("Agregar línea", "ADD", "btn-sm", "btn-primary");
        agregarPMMD.disableProperty().bind(matPMMD.valueProperty().isNull());
        agregarPMMD.setOnAction(ePMMD -> {
            MaterialPMMD mPMMD = matPMMD.getValue();
            String tipoPMMD = mPMMD.getCategoria().equals("Madera") ? "Madera" : mPMMD.getCategoria().equals("Espuma") ? "Espuma" : mPMMD.isTela() ? "Tela"
                    : mPMMD.getCategoria().equals("Tableros") ? "Tableros" : "Insumos";
            double mermaPMMD = mPMMD.isEntera() ? 0 : parametrosPMMD.merma(tipoPMMD);
            aplicarPMMD(() -> fichaServicePMMD.agregarLinea(fPMMD.getId(), mPMMD.getId(), comp.getValue(), ValidacionPMMD.valor(cantPMMD), mermaPMMD,
                    mPMMD.isEntera() ? DependenciaPMMD.F : DependenciaPMMD.L), "línea agregada");
        });
        HBox addPMMD = UIPMMD.hbox(8, matPMMD, comp, cantPMMD, agregarPMMD);
        HBox.setHgrow(matPMMD, Priority.ALWAYS);
        addPMMD.setStyle("-fx-padding: 10 0 0 0;");
        vPMMD.getChildren().add(addPMMD);
        return vPMMD;
    }

    private HBox fila(Node... nPMMD) {
        double[] wPMMD = {-1, 58, 50, 104, 66, 72, 26, 28};
        HBox hPMMD = new HBox(5);
        hPMMD.setAlignment(Pos.CENTER_LEFT);
        for (int iPMMD = 0; iPMMD < nPMMD.length; iPMMD++) {
            Node x = nPMMD[iPMMD];
            if (x instanceof Region r) {
                if (wPMMD[iPMMD] > 0) {
                    r.setPrefWidth(wPMMD[iPMMD]);
                    r.setMinWidth(wPMMD[iPMMD]);
                    r.setMaxWidth(wPMMD[iPMMD]);
                } else {
                    HBox.setHgrow(r, Priority.ALWAYS);
                    r.setMaxWidth(Double.MAX_VALUE);
                    r.setMinWidth(108);
                }
            }
            hPMMD.getChildren().add(x);
        }
        hPMMD.getStyleClass().add("bom-row");
        return hPMMD;
    }

    private Node filaBomPMMD(FichaTecnicaPMMD fPMMD, int iPMMD, ExplosionPMMD.Linea calcPMMD) {
        LineaBOMPMMD lPMMD = fPMMD.getLineas().get(iPMMD);
        MaterialPMMD mPMMD = lPMMD.getMaterial();
        VBox matPMMD = new VBox(0, UIPMMD.label(mPMMD.getNombre(), "cell-title"), UIPMMD.label(mPMMD.getCodigo() + " · stock " + MoneyPMMD.numero(mPMMD.getStock()) + " " + mPMMD.getUnidad(), "cell-sub"));
        TextField cantPMMD = celda(lPMMD.getCantidad(), vPMMD -> aplicarPMMD(() -> fichaServicePMMD.editarLinea(fPMMD.getId(), iPMMD, x -> x.setCantidad(vPMMD)), "cantidad de " + mPMMD.getNombre()));
        TextField mermaPMMD = celda(lPMMD.getMermaPct(), vPMMD -> aplicarPMMD(() -> fichaServicePMMD.editarLinea(fPMMD.getId(), iPMMD, x -> x.setMermaPct(vPMMD)), "merma de " + mPMMD.getNombre()));
        ComboBox<DependenciaPMMD> depPMMD = FormsPMMD.combo(List.of(DependenciaPMMD.values()), lPMMD.getDependencia(), DependenciaPMMD::nombre);
        depPMMD.getStyleClass().add("cell-field");
        depPMMD.valueProperty().addListener((oPMMD, aPMMD, bPMMD) -> {
            if (bPMMD != null && bPMMD != lPMMD.getDependencia()) aplicarPMMD(() -> fichaServicePMMD.editarLinea(fPMMD.getId(), iPMMD, x -> x.setDependencia(bPMMD)), "fórmula de " + mPMMD.getNombre());
        });
        Tooltip.install(depPMMD, new Tooltip(lPMMD.getDependencia().expresion()));
        CheckBox opcPMMD = new CheckBox();
        opcPMMD.setSelected(Boolean.TRUE.equals(lPMMD.getOpcional()));
        opcPMMD.selectedProperty().addListener((oPMMD, aPMMD, bPMMD) -> aplicarPMMD(() -> fichaServicePMMD.editarLinea(fPMMD.getId(), iPMMD, x -> x.setOpcional(bPMMD)), "opcional"));
        Button quitarPMMD = UIPMMD.iconButton("DELETE_OUTLINE", "Quitar línea", "btn-ghost", "btn-xs");
        quitarPMMD.setOnAction(ePMMD -> confirmPMMD.mostrar("¿Quitar " + mPMMD.getNombre() + " de la ficha?",
                fPMMD.isEditable() ? "La línea se elimina de " + fPMMD.getEtiqueta() + "." : "Se creará la siguiente versión en Borrador sin esta línea.",
                "Quitar", () -> aplicarPMMD(() -> fichaServicePMMD.quitarLinea(fPMMD.getId(), iPMMD), "línea quitada")));
        Label brutoPMMD = UIPMMD.label(MoneyPMMD.numero(calcPMMD.bruta()), "cell-sub");
        Label costoPMMD = UIPMMD.label(MoneyPMMD.fmt(calcPMMD.costo()), Boolean.TRUE.equals(lPMMD.getOpcional()) ? "cell-sub" : "cell-title");
        Tooltip.install(cantPMMD, new Tooltip("Cantidad base en " + mPMMD.getUnidad() + " para " + fPMMD.getLargo() + " × " + fPMMD.getFondo() + " × " + fPMMD.getAlto() + " cm"));
        brutoPMMD.setText(MoneyPMMD.numero(calcPMMD.bruta()) + " " + mPMMD.getUnidad());
        HBox hPMMD = fila(matPMMD, cantPMMD, mermaPMMD, depPMMD, brutoPMMD, costoPMMD, opcPMMD, quitarPMMD);
        if (Boolean.TRUE.equals(lPMMD.getOpcional())) hPMMD.setOpacity(0.7);
        return hPMMD;
    }

    private TextField celda(double valorPMMD, Consumer<Double> alCambiarPMMD) {
        TextField t = FormsPMMD.decimal(valorPMMD);
        t.getStyleClass().add("cell-field");
        Runnable commitPMMD = () -> {
            double vPMMD = ValidacionPMMD.valor(t);
            if (Math.abs(vPMMD - valorPMMD) > 1e-9 && vPMMD >= 0) alCambiarPMMD.accept(vPMMD);
        };
        t.setOnAction(ePMMD -> commitPMMD.run());
        t.focusedProperty().addListener((oPMMD, aPMMD, bPMMD) -> {
            if (!bPMMD) commitPMMD.run();
        });
        return t;
    }

    private Node manoObraPMMD(FichaTecnicaPMMD fPMMD, ExplosionPMMD.Resultado r) {
        VBox vPMMD = new VBox(6);
        vPMMD.getStyleClass().add("card");
        vPMMD.setPadding(new javafx.geometry.Insets(16));
        vPMMD.getChildren().add(UIPMMD.cardHeader("Mano de obra", MoneyPMMD.numero(fPMMD.getHorasMO()) + " horas · " + MoneyPMMD.fmt(ExplosionPMMD.costoManoObra(fPMMD))));
        GridPane gPMMD = new GridPane();
        gPMMD.setHgap(12);
        gPMMD.setVgap(4);
        ColumnConstraints c0PMMD = new ColumnConstraints();
        c0PMMD.setHgrow(Priority.ALWAYS);
        gPMMD.getColumnConstraints().addAll(c0PMMD, new ColumnConstraints(90), new ColumnConstraints(110), new ColumnConstraints(100));
        gPMMD.addRow(0, UIPMMD.label("ETAPA", "dato-label"), UIPMMD.label("HORAS", "dato-label"), UIPMMD.label("COSTO / HORA", "dato-label"), UIPMMD.label("COSTO", "dato-label"));
        for (int iPMMD = 0; iPMMD < fPMMD.getOperaciones().size(); iPMMD++) {
            OperacionMOPMMD oPMMD = fPMMD.getOperaciones().get(iPMMD);
            int idxPMMD = iPMMD;
            TextField hPMMD = celda(oPMMD.getHoras(), valPMMD -> aplicarPMMD(() -> fichaServicePMMD.editarHoras(fPMMD.getId(), idxPMMD, valPMMD), "horas de " + oPMMD.getEtapa().nombre()));
            gPMMD.addRow(iPMMD + 1, UIPMMD.label(oPMMD.getEtapa().nombre(), "cell-title"), hPMMD, UIPMMD.label("S/ " + MoneyPMMD.numero(oPMMD.getCostoHora()) + " por hora", "cell-sub"),
                    UIPMMD.label(MoneyPMMD.fmt(oPMMD.getCosto()), "cell-title"));
        }
        vPMMD.getChildren().add(gPMMD);
        return vPMMD;
    }

    private Node compuestaPMMD(FichaTecnicaPMMD fPMMD, ExplosionPMMD.Resultado r) {
        VBox vPMMD = new VBox(12);
        VBox hijasPMMD = new VBox(8);
        for (FichaTecnicaPMMD hPMMD : fichaServicePMMD.hijas(fPMMD)) {
            Hyperlink linkPMMD = new Hyperlink(hPMMD.getEtiqueta());
            linkPMMD.setOnAction(ePMMD -> seleccionarPMMD(hPMMD.getId()));
            HBox cPMMD = UIPMMD.hbox(10, UIPMMD.label(hPMMD.getNombre(), "cell-title"), linkPMMD, UIPMMD.spacer(), new StatusBadgePMMD(hPMMD.getEstado().nombre()),
                    UIPMMD.label(MoneyPMMD.fmt(fichaServicePMMD.costoEstandar(hPMMD)), "cell-title"));
            cPMMD.getStyleClass().add("card");
            cPMMD.setPadding(new javafx.geometry.Insets(10, 14, 10, 14));
            hijasPMMD.getChildren().add(cPMMD);
        }
        ComboBox<TipoTelaPMMD> telaPMMD = FormsPMMD.combo(List.of(TipoTelaPMMD.values()), telaJuegoPMMD, TipoTelaPMMD::nombre);
        telaPMMD.setPromptText("Según cada ficha");
        telaPMMD.valueProperty().addListener((oPMMD, aPMMD, bPMMD) -> {
            telaJuegoPMMD = bPMMD;
            renderEditorPMMD();
        });
        ExplosionPMMD.Resultado consPMMD = r;
        if (telaJuegoPMMD != null) {
            Map<String, String> sustPMMD = new HashMap<>();
            for (TipoTelaPMMD t : TipoTelaPMMD.values()) sustPMMD.put(t.materialCodigo(), telaJuegoPMMD.materialCodigo());
            consPMMD = fichaServicePMMD.explotar(fPMMD, 1, Opciones.sustituir(sustPMMD));
        }
        TableView<ExplosionPMMD.PorMaterial> t = CellsPMMD.tabla("Sin materiales.");
        t.getColumns().addAll(List.of(
                CellsPMMD.texto("Código", pmPMMD -> pmPMMD.material().getCodigo(), 80, "cell-mono"),
                CellsPMMD.texto("Material consolidado", pmPMMD -> pmPMMD.material().getNombre(), 260, "cell-strong"),
                CellsPMMD.derecha("Cantidad", pmPMMD -> MoneyPMMD.numero(pmPMMD.cantidad()) + " " + pmPMMD.material().getUnidad(), 120),
                CellsPMMD.derecha("Costo", pmPMMD -> MoneyPMMD.fmt(pmPMMD.costo()), 110)));
        t.getItems().setAll(consPMMD.porMaterial());
        t.setFixedCellSize(40);
        t.setPrefHeight(40 * consPMMD.porMaterial().size() + 44);
        VBox tablaCardPMMD = new VBox(t);
        tablaCardPMMD.getStyleClass().addAll("card", "table-card");
        vPMMD.getChildren().addAll(UIPMMD.label("Fichas hijas (versiones aprobadas)", "card-title"), hijasPMMD,
                UIPMMD.hbox(10, UIPMMD.label("Misma tela para todo el juego", "form-label"), telaPMMD), tablaCardPMMD);
        return vPMMD;
    }



    private void renderResumenPMMD(FichaTecnicaPMMD fPMMD, ExplosionPMMD.Resultado r) {
        double precioPMMD = fPMMD.getProducto() == null ? 0 : fPMMD.getProducto().getPrecio().doubleValue();
        double costoPMMD = r.costoEstandar();
        double margenPMMD = precioPMMD == 0 ? 0 : (precioPMMD - costoPMMD) * 100 / precioPMMD;
        TextField indPMMD = celda(fPMMD.getIndirectosPct(), vPMMD -> aplicarPMMD(() -> fichaServicePMMD.editarIndirectos(fPMMD.getId(), vPMMD), "indirectos"));
        indPMMD.setPrefWidth(70);
        indPMMD.setDisable(Boolean.TRUE.equals(fPMMD.getCompuesta()) && false);
        VBox costosPMMD = UIPMMD.card(UIPMMD.label("Resumen de costos", "card-title"),
                filaResumenPMMD("Costo de materiales", MoneyPMMD.fmt(r.costoMateriales())),
                filaResumenPMMD("Mano de obra (" + MoneyPMMD.numero(r.horasMO()) + " h)", MoneyPMMD.fmt(r.costoMO())),
                UIPMMD.hbox(8, minimoPMMD(UIPMMD.label("Indirectos", "muted")), indPMMD, minimoPMMD(UIPMMD.label("%", "muted")), UIPMMD.spacer(), minimoPMMD(UIPMMD.label(MoneyPMMD.fmt(r.indirectos()), "cell-title"))),
                totalResumenPMMD("Costo estándar", MoneyPMMD.fmt(costoPMMD)),
                filaResumenPMMD("Precio de venta", MoneyPMMD.fmt(precioPMMD)),
                UIPMMD.hbox(8, UIPMMD.label("Margen", "muted"), UIPMMD.spacer(), new StatusBadgePMMD(String.format("%.1f %%", margenPMMD), margenPMMD >= parametrosPMMD.margenObjetivo() ? "green" : "red")));
        if (margenPMMD < parametrosPMMD.margenObjetivo() && precioPMMD > 0) {
            costosPMMD.getChildren().add(UIPMMD.banner("danger-banner", "WARNING", "Margen bajo el " + (int) parametrosPMMD.margenObjetivo()
                    + " %. Precio sugerido: " + MoneyPMMD.fmt0(costoPMMD / 0.65) + " (costo / 0.65)."));
        }
        var comp = fichaServicePMMD.composicion(r).stream().map(cPMMD -> new DonutChartPMMD.Segmento(cPMMD.nombre(), cPMMD.valor(), cPMMD.color())).toList();
        VBox donaPMMD = UIPMMD.card(UIPMMD.label("Composición del costo", "card-title"), new DonutChartPMMD(comp, 150, MoneyPMMD.corto(costoPMMD), "costo", true));
        List<OrdenProduccionPMMD> usosPMMD = fichaServicePMMD.usos(fPMMD);
        VBox usoPMMD = UIPMMD.card(UIPMMD.label("Uso de esta versión", "card-title"));
        if (usosPMMD.isEmpty()) usoPMMD.getChildren().add(UIPMMD.label("Ninguna orden activa usa " + fPMMD.getEtiqueta() + ".", "muted"));
        for (OrdenProduccionPMMD oPMMD : usosPMMD) {
            Hyperlink lPMMD = new Hyperlink(oPMMD.getCodigo() + " · " + oPMMD.getProductoNombre() + " ×" + oPMMD.getCantidad());
            lPMMD.setOnAction(ePMMD -> navigatorPMMD.goTo("produccion", IntentPMMD.abrir(oPMMD.getCodigo())));
            usoPMMD.getChildren().add(UIPMMD.hbox(6, lPMMD, UIPMMD.spacer(), UIPMMD.label(oPMMD.getEtapa().nombre(), "cell-sub")));
        }
        resumenPMMD.getChildren().setAll(costosPMMD, donaPMMD, usoPMMD);
    }

    private static Label minimoPMMD(Label lPMMD) {
        lPMMD.setMinWidth(Region.USE_PREF_SIZE);
        return lPMMD;
    }

    private HBox filaResumenPMMD(String aPMMD, String bPMMD) {
        HBox hPMMD = UIPMMD.hbox(8, UIPMMD.label(aPMMD, "muted"), UIPMMD.spacer(), UIPMMD.label(bPMMD, "cell-title"));
        hPMMD.getStyleClass().add("resumen-row");
        return hPMMD;
    }

    private HBox totalResumenPMMD(String aPMMD, String bPMMD) {
        HBox hPMMD = UIPMMD.hbox(8, UIPMMD.label(aPMMD), UIPMMD.spacer(), UIPMMD.label(bPMMD));
        hPMMD.getStyleClass().addAll("resumen-row", "resumen-total");
        return hPMMD;
    }



    private void simular(FichaTecnicaPMMD fPMMD) {
        TextField largoPMMD = FormsPMMD.entero(fPMMD.getLargo()), fondoPMMD = FormsPMMD.entero(fPMMD.getFondo()), altoPMMD = FormsPMMD.entero(fPMMD.getAlto());
        Spinner<Integer> piezasPMMD = new Spinner<>(1, 50, 1);
        piezasPMMD.setEditable(true);
        VBox tabla = new VBox(4);
        Runnable calcPMMD = () -> {
            int L = FormsPMMD.entero(largoPMMD), F = FormsPMMD.entero(fondoPMMD), A = FormsPMMD.entero(altoPMMD);
            if (L < 30 || F < 30 || A < 30) return;
            FichaServicePMMD.Simulacion sPMMD = fichaServicePMMD.simular(fPMMD, new MedidasPMMD(L, F, A, fPMMD.getCuerpos()), piezasPMMD.getValue());
            GridPane gPMMD = new GridPane();
            gPMMD.setHgap(12);
            gPMMD.setVgap(3);
            ColumnConstraints c0PMMD = new ColumnConstraints();
            c0PMMD.setHgrow(Priority.ALWAYS);
            gPMMD.getColumnConstraints().addAll(c0PMMD, new ColumnConstraints(90), new ColumnConstraints(90), new ColumnConstraints(90));
            gPMMD.addRow(0, UIPMMD.label("MATERIAL", "dato-label"), UIPMMD.label("FICHA BASE", "dato-label"), UIPMMD.label("SIMULADO", "dato-label"), UIPMMD.label("DIFERENCIA", "dato-label"));
            int iPMMD = 1;
            for (var fila : sPMMD.filas()) {
                Label difPMMD = UIPMMD.label(String.format("%+.2f", fila.diferencia()), "cell-title");
                if (fila.diferencia() > 1e-6) difPMMD.setStyle("-fx-text-fill: -is-warn-ink;");
                else if (fila.diferencia() < -1e-6) difPMMD.setStyle("-fx-text-fill: -is-success-ink;");
                gPMMD.addRow(iPMMD++, UIPMMD.label(fila.material().getNombre() + " · " + fila.componente().nombre(), "cell-sub"),
                        UIPMMD.label(MoneyPMMD.numero(fila.base()) + " " + fila.material().getUnidad(), "cell-sub"),
                        UIPMMD.label(MoneyPMMD.numero(fila.simulado()), "cell-title"), difPMMD);
            }
            double cbPMMD = sPMMD.base().costoEstandar(), csPMMD = sPMMD.simulado().costoEstandar();
            Label difC = UIPMMD.label(String.format("%+.1f %%", cbPMMD == 0 ? 0 : (csPMMD - cbPMMD) * 100 / cbPMMD), "mini-stat-value");
            difC.setStyle(csPMMD > cbPMMD ? "-fx-text-fill: -is-warn-ink;" : "-fx-text-fill: -is-success-ink;");
            VBox dvPMMD = new VBox(2, difC, UIPMMD.label("Diferencia", "mini-stat-label"));
            dvPMMD.getStyleClass().add("mini-stat");
            HBox.setHgrow(dvPMMD, Priority.ALWAYS);
            tabla.getChildren().setAll(UIPMMD.hbox(10, UIPMMD.miniStat("Costo base", MoneyPMMD.fmt(cbPMMD)), UIPMMD.miniStat("Costo simulado", MoneyPMMD.fmt(csPMMD)), dvPMMD), gPMMD);
        };
        for (TextField t : List.of(largoPMMD, fondoPMMD, altoPMMD)) t.textProperty().addListener((oPMMD, aPMMD, bPMMD) -> calcPMMD.run());
        piezasPMMD.valueProperty().addListener((oPMMD, aPMMD, bPMMD) -> calcPMMD.run());
        calcPMMD.run();
        GridPane in = UIPMMD.grid(4);
        in.add(UIPMMD.field("Largo (cm)", largoPMMD), 0, 0);
        in.add(UIPMMD.field("Fondo (cm)", fondoPMMD), 1, 0);
        in.add(UIPMMD.field("Alto (cm)", altoPMMD), 2, 0);
        in.add(UIPMMD.field("Piezas", piezasPMMD), 3, 0);
        Button cerrarPMMD = UIPMMD.secondary("Cerrar", null);
        var hPMMD = overlaysPMMD.modal("Simular medida · " + fPMMD.getEtiqueta(), "Base " + fPMMD.getLargo() + " × " + fPMMD.getFondo() + " × " + fPMMD.getAlto() + " cm", new VBox(16, in, tabla), 680, cerrarPMMD);
        cerrarPMMD.setOnAction(ePMMD -> hPMMD.cerrar());
    }

    private void exportarPMMD(FichaTecnicaPMMD fPMMD, ExplosionPMMD.Resultado r) {
        File archivoPMMD = ArchivosPMMD.guardar(editorPMMD, "Ficha-" + fPMMD.getCodigo() + "-v" + fPMMD.getVersion(), "PDF", "pdf");
        if (archivoPMMD == null) return;
        double precioPMMD = fPMMD.getProducto() == null ? 0 : fPMMD.getProducto().getPrecio().doubleValue();
        TareasPMMD.ejecutar(overlaysPMMD, toastPMMD, "Exportando ficha…", () -> {
            exportServicePMMD.fichaTecnica(fPMMD, r, precioPMMD, archivoPMMD);
            return archivoPMMD;
        }, aPMMD -> {
            toastPMMD.exito("Ficha exportada: " + aPMMD.getName());
            appWindowPMMD.hostServices().showDocument(aPMMD.toURI().toString());
        });
    }

    private void nuevaFichaPMMD() {
        List<ProductoPMMD> sinPMMD = productoServicePMMD.listar().stream().filter(pPMMD -> fichaServicePMMD.ultimaDe(pPMMD) == null).toList();
        if (sinPMMD.isEmpty()) {
            toastPMMD.info("Todos los modelos ya tienen ficha técnica. Usa «Duplicar como nueva versión» para cambiar una.");
            return;
        }
        ComboBox<ProductoPMMD> pPMMD = FormsPMMD.combo(sinPMMD, sinPMMD.get(0), x -> x.getCodigo() + " · " + x.getNombre());
        List<FichaTecnicaPMMD> aprobadasPMMD = fichaServicePMMD.todas().stream().filter(x -> x.getEstado() == EstadoFichaPMMD.APROBADA && !Boolean.TRUE.equals(x.getCompuesta())).toList();
        ComboBox<FichaTecnicaPMMD> plantillaPMMD = FormsPMMD.combo(aprobadasPMMD, aprobadasPMMD.isEmpty() ? null : aprobadasPMMD.get(0), x -> x.getEtiqueta() + " · " + x.getNombre());
        Button cancelarPMMD = UIPMMD.secondary("Cancelar", null);
        Button okPMMD = UIPMMD.primary("Crear en Borrador", "ADD");
        var hPMMD = overlaysPMMD.modal("Nueva ficha técnica", "Se crea en Borrador copiando las líneas de una ficha aprobada", new VBox(14,
                UIPMMD.field("Modelo sin ficha", pPMMD), UIPMMD.field("Copiar de", plantillaPMMD)), 480, cancelarPMMD, okPMMD);
        cancelarPMMD.setOnAction(ePMMD -> hPMMD.cerrar());
        okPMMD.setOnAction(ePMMD -> {
            FichaTecnicaPMMD[] nPMMD = new FichaTecnicaPMMD[1];
            if (toastPMMD.intentar(() -> nPMMD[0] = fichaServicePMMD.crearPara(pPMMD.getValue(), plantillaPMMD.getValue()))) {
                hPMMD.cerrar();
                toastPMMD.exito(nPMMD[0].getEtiqueta() + " creada en Borrador para " + pPMMD.getValue().getNombre() + ".");
                seleccionarPMMD(nPMMD[0].getId());
            }
        });
    }
}
