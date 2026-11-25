package pe.erp.sofas.ui.views.configurador;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pe.erp.sofas.core.*;
import pe.erp.sofas.model.*;
import pe.erp.sofas.model.ConfigSofaPMMD.*;
import pe.erp.sofas.service.*;
import pe.erp.sofas.ui.AppWindowPMMD;
import pe.erp.sofas.ui.components.*;
import pe.erp.sofas.ui.three.SofaBuilderPMMD;
import pe.erp.sofas.ui.three.Viewer3DPMMD;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.math.BigDecimal;
import java.util.*;






@Component
@Lazy
@Scope("prototype")
@RequiredArgsConstructor
public class ConfiguradorControllerPMMD implements VistaPMMD.ConCiclo {

    private final ConfiguradorServicePMMD configuradorPMMD;

    private final TelaServicePMMD telasPMMD;

    private final ProductoServicePMMD productosPMMD;

    private final ClienteServicePMMD clientesPMMD;

    private final EmpleadoServicePMMD empleadosPMMD;

    private final ExportServicePMMD exportPMMD;

    private final OverlaysPMMD overlaysPMMD;

    private final ToastPMMD toastPMMD;

    private final NavigatorPMMD navigatorPMMD;

    private final AppWindowPMMD appWindowPMMD;

    @FXML private VBox raiz;

    private final Viewer3DPMMD visorPMMD = new Viewer3DPMMD(true);

    private final SofaBuilderPMMD builderPMMD = new SofaBuilderPMMD();

    private final VBox pasosPMMD = new VBox();

    private final HBox barraPrecioPMMD = new HBox();

    private final VBox desglosePMMD = new VBox();

    private ConfigSofaPMMD configPMMD;

    private ConfiguradorServicePMMD.Cotizacion cotizacionPMMD;

    private final ConfigSofaPMMD[] variantesPMMD = new ConfigSofaPMMD[3];

    private final HBox variantesBoxPMMD = new HBox(6);

    private Button btnGiroPMMD;

    private ScrollPane spPasosPMMD;

    @FXML
    public void initialize() {
        configPMMD = configuradorPMMD.defecto(Modelo.MODERNO);
        variantesBoxPMMD.getStyleClass().add("viewer-toolbar");
        variantesBoxPMMD.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        StackPane.setAlignment(variantesBoxPMMD, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(variantesBoxPMMD, new Insets(12));
        StackPane visorHostPMMD = new StackPane(visorPMMD, toolbar(), pistaPMMD(), variantesBoxPMMD);
        visorHostPMMD.getStyleClass().add("viewer-host");
        visorPMMD.setMinSize(200, 200);
        spPasosPMMD = UIPMMD.scroll(pasosPMMD);
        VBox panelPasosPMMD = new VBox(spPasosPMMD);
        VBox.setVgrow(spPasosPMMD, Priority.ALWAYS);
        panelPasosPMMD.getStyleClass().add("card");
        panelPasosPMMD.setStyle("-fx-padding: 0;");
        GridPane arribaPMMD = UIPMMD.columnas(new double[]{65, 35}, visorHostPMMD, panelPasosPMMD);
        arribaPMMD.setMinHeight(0);
        RowConstraints rcPMMD = new RowConstraints();
        rcPMMD.setVgrow(Priority.ALWAYS);
        arribaPMMD.getRowConstraints().add(rcPMMD);
        VBox.setVgrow(arribaPMMD, Priority.ALWAYS);
        barraPrecioPMMD.getStyleClass().addAll("price-bar", "card");
        barraPrecioPMMD.setStyle("-fx-background-radius: 14px;");
        desglosePMMD.setVisible(false);
        desglosePMMD.setManaged(false);
        raiz.getChildren().setAll(arribaPMMD, desglosePMMD, barraPrecioPMMD);
    }

    @Override
    public void onShow(IntentPMMD intentPMMD) {
        if (intentPMMD.es(IntentPMMD.ACCION_PRESELECCIONAR) && intentPMMD.dato() != null) {
            productosPMMD.porCodigo(intentPMMD.dato().toString()).ifPresent(this::desdeProductoPMMD);
        } else if (intentPMMD.dato() instanceof ConfigSofaPMMD c) {
            configPMMD = c;
        }
        actualizarPMMD(true);
    }

    @Override
    public void onHide() {
        visorPMMD.detenerGiro();
        if (btnGiroPMMD != null) btnGiroPMMD.getStyleClass().remove("btn-primary");
    }

    private void desdeProductoPMMD(ProductoPMMD pPMMD) {
        Modelo mPMMD = switch (pPMMD.getForma()) {
            case SECCIONAL_L, SECCIONAL_U -> Modelo.SECCIONAL_L;
            case SILLON -> Modelo.SILLON;
            case CHESTERFIELD -> Modelo.CHESTERFIELD;
            case SOFA_CAMA -> Modelo.SOFA_CAMA;
            case ESCANDINAVO -> Modelo.ESCANDINAVO;
            default -> Modelo.MODERNO;
        };
        ConfigSofaPMMD c = configuradorPMMD.defecto(mPMMD);
        if (mPMMD == Modelo.MODERNO && pPMMD.getForma() != FormaPMMD.JUEGO) c = c.conCuerpos(Math.max(1, Math.min(4, pPMMD.getCuerpos())));
        if (pPMMD.getForma() == FormaPMMD.RECLINABLE) c = c.conMecanismo(Mecanismo.RECLINABLE_MANUAL);
        Optional<TelaPMMD> t = telasPMMD.activas().stream().filter(xPMMD -> xPMMD.getColor().equalsIgnoreCase(pPMMD.getColor())
                && xPMMD.getTipo().nombre().toLowerCase().startsWith(pPMMD.getMaterial().toLowerCase().split(" ")[0])).findFirst();
        if (t.isPresent()) c = c.conTela(t.get().getId());
        configPMMD = c;
        toastPMMD.info("Configurando a partir de " + pPMMD.getNombre() + ".");
    }



    private Node toolbar() {
        HBox camaras = new HBox(4);
        for (Viewer3DPMMD.Camara c : Viewer3DPMMD.Camara.values()) {
            Button bPMMD = UIPMMD.button(c.nombre(), null, "btn-sm", "btn-ghost");
            bPMMD.setOnAction(ePMMD -> visorPMMD.camara(c));
            camaras.getChildren().add(bPMMD);
        }
        btnGiroPMMD = UIPMMD.iconButton("MDOAL_360", "Giro automático 360°", "btn-ghost");
        btnGiroPMMD.setOnAction(ePMMD -> {
            boolean gPMMD = visorPMMD.alternarGiro();
            if (gPMMD) btnGiroPMMD.getStyleClass().add("btn-primary");
            else btnGiroPMMD.getStyleClass().remove("btn-primary");
        });
        Button centrar = UIPMMD.iconButton("CENTER_FOCUS_STRONG", "Centrar (doble clic)", "btn-ghost");
        centrar.setOnAction(ePMMD -> visorPMMD.centrar(true));
        Button foto = UIPMMD.iconButton("PHOTO_CAMERA", "Captura PNG", "btn-ghost");
        foto.setOnAction(ePMMD -> capturaPngPMMD());
        Button full = UIPMMD.iconButton("FULLSCREEN", "Pantalla completa", "btn-ghost");
        full.setOnAction(ePMMD -> pantallaCompletaPMMD());
        HBox t = new HBox(4, camaras, new Separator(javafx.geometry.Orientation.VERTICAL), btnGiroPMMD, centrar, foto, full);
        t.getStyleClass().add("viewer-toolbar");
        t.setAlignment(Pos.CENTER_LEFT);
        t.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        StackPane.setAlignment(t, Pos.TOP_LEFT);
        StackPane.setMargin(t, new Insets(12));
        return t;
    }

    private Node pistaPMMD() {
        Label lPMMD = UIPMMD.label("Arrastra para girar · rueda para acercar · doble clic para centrar · Shift + arrastrar para mover", "viewer-hint");
        lPMMD.setMouseTransparent(true);
        StackPane.setAlignment(lPMMD, Pos.BOTTOM_LEFT);
        StackPane.setMargin(lPMMD, new Insets(12));
        return lPMMD;
    }

    private TelaPMMD tela() {
        return configPMMD.telaId() == null ? null : telasPMMD.tela(configPMMD.telaId());
    }

    private TelaPMMD telaCojinesPMMD() {
        return configPMMD.telaCojinesId() == null ? tela() : telasPMMD.tela(configPMMD.telaCojinesId());
    }

    private void actualizarPMMD(boolean recentrarPMMD) {
        visorPMMD.setContenido(builderPMMD.build(SofaBuilderPMMD.Opciones.de(configPMMD, tela(), telaCojinesPMMD())), recentrarPMMD);
        try {
            cotizacionPMMD = configuradorPMMD.cotizar(configPMMD);
        } catch (NegocioExceptionPMMD ePMMD) {
            toastPMMD.error(ePMMD.getMessage());
            cotizacionPMMD = null;
        }
        renderPasosPMMD();
        renderPrecioPMMD();
    }

    private void cambiarPMMD(ConfigSofaPMMD nuevoPMMD) {
        boolean recentrarPMMD = nuevoPMMD.cuerpos() != configPMMD.cuerpos() || nuevoPMMD.chaise() != configPMMD.chaise() || nuevoPMMD.modelo() != configPMMD.modelo()
                || nuevoPMMD.profundidad() != configPMMD.profundidad();
        configPMMD = nuevoPMMD;
        actualizarPMMD(recentrarPMMD);
    }



    private void renderPasosPMMD() {
        ScrollPane spPMMD = spPasosPMMD;
        double posPMMD = spPMMD == null ? 0 : spPMMD.getVvalue();
        pasosPMMD.getChildren().setAll(
                pasoPMMD(1, "Modelo base", modelosPMMD()),
                pasoPMMD(2, "Tamaño", tamanoPMMD()),
                pasoPMMD(3, "Tela y color", telaYColorPMMD()),
                pasoPMMD(4, "Brazos", opcionesPMMD(List.of(Brazo.values()), configPMMD.brazo(), Brazo::nombre, bPMMD -> null, bPMMD -> false, bPMMD -> cambiarPMMD(configPMMD.conBrazo(bPMMD)))),
                pasoPMMD(5, "Patas", patas()),
                pasoPMMD(6, "Confort", opcionesPMMD(List.of(Espuma.values()), configPMMD.espuma(), Espuma::nombre, ePMMD -> ePMMD.bajoPedido() ? "bajo pedido" : null,
                        ePMMD -> false, ePMMD -> cambiarPMMD(configPMMD.conEspuma(ePMMD)))),
                pasoPMMD(7, "Cojines decorativos", cojines()),
                pasoPMMD(8, "Mecanismo", opcionesPMMD(List.of(Mecanismo.values()), configPMMD.mecanismo(), Mecanismo::nombre, mPMMD -> switch (mPMMD) {
                    case RECLINABLE_MANUAL -> "un mecanismo por cuerpo";
                    case RECLINABLE_ELECTRICO -> "bajo pedido";
                    case CAMA -> "mecanismo + colchoneta";
                    default -> null;
                }, mPMMD -> false, mPMMD -> cambiarPMMD(configPMMD.conMecanismo(mPMMD)))));
        if (spPMMD != null) javafx.application.Platform.runLater(() -> spPMMD.setVvalue(posPMMD));
    }

    private Node pasoPMMD(int n, String tituloPMMD, Node contenido) {
        Label numPMMD = UIPMMD.label(String.valueOf(n), "cfg-step-num");
        VBox v = new VBox(10, UIPMMD.hbox(10, numPMMD, UIPMMD.label(tituloPMMD, "cfg-step-title")), contenido);
        v.getStyleClass().add("cfg-step");
        return v;
    }

    private <T> Node opcionesPMMD(List<T> valoresPMMD, T actualPMMD, java.util.function.Function<T, String> nombre, java.util.function.Function<T, String> subPMMD,
                              java.util.function.Predicate<T> deshabilitarPMMD, java.util.function.Consumer<T> alElegirPMMD) {
        FlowPane fPMMD = new FlowPane(8, 8);
        ToggleGroup gPMMD = new ToggleGroup();
        for (T v : valoresPMMD) {
            ToggleButton t = new ToggleButton(nombre.apply(v));
            String sPMMD = subPMMD.apply(v);
            if (sPMMD != null) {
                t.setText(nombre.apply(v) + "\n" + sPMMD);
                t.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
            }
            t.getStyleClass().add("opt-tile");
            t.setToggleGroup(gPMMD);
            t.setSelected(v.equals(actualPMMD));
            t.setDisable(deshabilitarPMMD.test(v));
            t.setMinWidth(Region.USE_PREF_SIZE);
            t.setOnAction(ePMMD -> {
                if (!v.equals(actualPMMD)) alElegirPMMD.accept(v);
                else t.setSelected(true);
            });
            fPMMD.getChildren().add(t);
        }
        return fPMMD;
    }

    private Node modelosPMMD() {
        return opcionesPMMD(List.of(Modelo.values()), configPMMD.modelo(), Modelo::nombre, mPMMD -> "desde " + MoneyPMMD.fmt0(configuradorPMMD.desde(mPMMD)),
                mPMMD -> false, mPMMD -> {
                    configPMMD = configuradorPMMD.defecto(mPMMD);
                    actualizarPMMD(true);
                    toastPMMD.info(mPMMD.nombre() + ": valores por defecto aplicados" + (mPMMD == Modelo.CHESTERFIELD ? " (cuero cognac, capitoné)." : "."));
                });
    }

    private Node tamanoPMMD() {
        Node cuerpos = opcionesPMMD(List.of(1, 2, 3, 4), configPMMD.cuerpos(), n -> n + (n == 1 ? " cuerpo" : " cuerpos"),
                n -> ConfigSofaPMMD.largoPara(n) + " cm", n -> configPMMD.modelo() == Modelo.SILLON && n > 1, n -> cambiarPMMD(configPMMD.conCuerpos(n)));
        Node chaise = opcionesPMMD(List.of(Chaise.values()), configPMMD.chaise(), Chaise::nombre, c -> null,
                c -> c != Chaise.NINGUNA && configPMMD.cuerpos() < 2, c -> cambiarPMMD(configPMMD.conChaise(c)));
        Node prof = opcionesPMMD(List.of(92, 105), configPMMD.profundidad(), pPMMD -> pPMMD == 92 ? "Estándar" : "Profunda", pPMMD -> pPMMD + " cm",
                pPMMD -> false, pPMMD -> cambiarPMMD(configPMMD.conProfundidad(pPMMD)));
        return new VBox(8, UIPMMD.label("Cuerpos", "form-label"), cuerpos, UIPMMD.label("Chaise", "form-label"), chaise, UIPMMD.label("Profundidad", "form-label"), prof);
    }

    private Node telaYColorPMMD() {
        TelaPMMD actualPMMD = tela();
        TipoTelaPMMD tipoPMMD = actualPMMD == null ? TipoTelaPMMD.CHENILLE : actualPMMD.getTipo();
        Node tipos = opcionesPMMD(List.of(TipoTelaPMMD.values()), tipoPMMD, TipoTelaPMMD::nombre, t -> "S/ " + (int) t.costoMetro() + "/m · +" + (int) t.recargoPct() + " %",
                t -> telasPMMD.porTipo(t).stream().allMatch(TelaPMMD::getDescontinuada), t -> telasPMMD.porTipo(t).stream().filter(xPMMD -> !xPMMD.getDescontinuada())
                        .max(Comparator.comparingDouble(TelaPMMD::getStock)).ifPresent(xPMMD -> cambiarPMMD(configPMMD.conTela(xPMMD.getId()))));
        FlowPane colores = new FlowPane(10, 10);
        for (TelaPMMD t : telasPMMD.porTipo(tipoPMMD)) {
            if (t.getDescontinuada()) continue;
            ColorSwatchPMMD sw = new ColorSwatchPMMD(t.getHex(), t.getTipo(), 34).seleccionada(actualPMMD != null && t.getId().equals(actualPMMD.getId()));
            Label nombre = UIPMMD.label(t.getColor(), "cell-sub");
            VBox cajaPMMD = new VBox(4, sw, nombre);
            cajaPMMD.setAlignment(Pos.CENTER);
            cajaPMMD.setPrefWidth(64);
            cajaPMMD.getStyleClass().add("clickable");
            Tooltip.install(cajaPMMD, new Tooltip(t.getNombre() + " · " + MoneyPMMD.numero(t.getStock()) + " m · " + t.getEstado()));
            if (t.getStock() <= 0) cajaPMMD.setOpacity(0.55);
            cajaPMMD.setOnMouseClicked(ePMMD -> cambiarPMMD(configPMMD.conTela(t.getId())));
            colores.getChildren().add(cajaPMMD);
        }
        double metrosPMMD = cotizacionPMMD == null || actualPMMD == null ? 0 : cotizacionPMMD.lineas().stream()
                .filter(lPMMD -> lPMMD.material().getCodigo().equals(actualPMMD.getTipo().materialCodigo())).mapToDouble(ConfiguradorServicePMMD.Linea::cantidad).sum();
        String notaPMMD = actualPMMD == null ? "" : telasPMMD.notaDisponibilidad(actualPMMD, metrosPMMD);
        HBox notaBox = UIPMMD.banner(actualPMMD != null && actualPMMD.getStock() >= metrosPMMD ? "ok-banner" : "danger-banner",
                actualPMMD != null && actualPMMD.getStock() >= metrosPMMD ? "CHECK_CIRCLE" : "WARNING",
                (actualPMMD == null ? "" : actualPMMD.getNombre() + ": ") + notaPMMD + (metrosPMMD > 0 ? " · este sofá usa " + MoneyPMMD.numero(metrosPMMD) + " m" : ""));
        return new VBox(10, tipos, colores, notaBox);
    }

    private Node patas() {
        Node tipos = opcionesPMMD(List.of(Patas.values()), configPMMD.patas(), Patas::nombre, pPMMD -> pPMMD.recargo() > 0 ? "+S/ " + (int) pPMMD.recargo() + " c/u"
                : pPMMD.bajoPedido() ? "bajo pedido" : null, pPMMD -> false, pPMMD -> cambiarPMMD(configPMMD.conPatas(pPMMD)));
        VBox v = new VBox(8, tipos);
        if (configPMMD.patas() == Patas.MADERA) {
            HBox acabadosPMMD = new HBox(10);
            for (AcabadoMadera aPMMD : AcabadoMadera.values()) {
                ToggleButton t = new ToggleButton(aPMMD.nombre());
                t.getStyleClass().add("opt-tile");
                Region muestraPMMD = new Region();
                muestraPMMD.setMinSize(18, 18);
                muestraPMMD.setStyle("-fx-background-color: " + aPMMD.hex() + "; -fx-background-radius: 999;");
                t.setGraphic(muestraPMMD);
                t.setContentDisplay(ContentDisplay.LEFT);
                t.setSelected(aPMMD == configPMMD.acabado());
                t.setOnAction(ePMMD -> cambiarPMMD(configPMMD.conAcabado(aPMMD)));
                acabadosPMMD.getChildren().add(t);
            }
            v.getChildren().addAll(UIPMMD.label("Acabado de la madera", "form-label"), acabadosPMMD);
        }
        return v;
    }

    private Node cojines() {
        Node n = opcionesPMMD(List.of(0, 2, 4), configPMMD.cojines(), c -> c == 0 ? "Sin cojines" : c + " cojines", c -> c == 0 ? null : "0.6 m de tela c/u",
                c -> false, c -> cambiarPMMD(configPMMD.conCojines(c, configPMMD.telaCojinesId())));
        VBox v = new VBox(8, n);
        if (configPMMD.cojines() > 0) {
            FlowPane colores = new FlowPane(8, 8);
            TelaPMMD actualPMMD = telaCojinesPMMD();
            for (TelaPMMD t : telasPMMD.activas()) {
                if (t.getStock() <= 0) continue;
                ColorSwatchPMMD sw = new ColorSwatchPMMD(t.getHex(), t.getTipo(), 26).seleccionada(actualPMMD != null && actualPMMD.getId().equals(t.getId()));
                Tooltip.install(sw, new Tooltip(t.getNombre()));
                sw.getStyleClass().add("clickable");
                sw.setOnMouseClicked(ePMMD -> cambiarPMMD(configPMMD.conCojines(configPMMD.cojines(), t.getId())));
                colores.getChildren().add(sw);
            }
            v.getChildren().addAll(UIPMMD.label("Color de los cojines" + (actualPMMD == null ? "" : " · " + actualPMMD.getNombre()), "form-label"), colores);
        }
        return v;
    }



    private void renderPrecioPMMD() {
        if (cotizacionPMMD == null) {
            barraPrecioPMMD.getChildren().setAll(UIPMMD.label("No se pudo cotizar esta configuración.", "muted"));
            return;
        }
        ConfiguradorServicePMMD.Cotizacion c = cotizacionPMMD;
        VBox precioPMMD = fijoPMMD(new VBox(0, UIPMMD.label("PRECIO DE VENTA", "price-label"), UIPMMD.label(MoneyPMMD.fmt0(c.precio()), "price-big")));
        VBox costoPMMD = fijoPMMD(new VBox(1, UIPMMD.label("Costo estándar", "price-label"), UIPMMD.label(MoneyPMMD.fmt(c.costo()), "price-value"), UIPMMD.label(c.referencia(), "code")));
        VBox margenPMMD = fijoPMMD(new VBox(2, UIPMMD.label("Margen", "price-label"), new StatusBadgePMMD(String.format("%.1f %%", c.margenPct()), c.margenPct() >= 35 ? "green" : "red")));
        Label diasL = UIPMMD.label(c.dias() + " días", "price-value");
        VBox diasPMMD = fijoPMMD(new VBox(2, UIPMMD.label("Fabricación", "price-label"), diasL));
        List<String> avisosPMMD = new ArrayList<>();
        if (c.bajoPedido()) avisosPMMD.add("+4 días por material bajo pedido");
        if (c.critico()) avisosPMMD.add("+5 días por material crítico");
        if (!avisosPMMD.isEmpty()) Tooltip.install(diasPMMD, new Tooltip(String.join("\n", avisosPMMD)));
        if (!avisosPMMD.isEmpty()) diasL.setStyle("-fx-text-fill: -is-warn-ink;");
        ToggleButton verDesglosePMMD = new ToggleButton("Desglose");
        verDesglosePMMD.setMinWidth(Region.USE_PREF_SIZE);
        verDesglosePMMD.getStyleClass().addAll("btn", "btn-ghost", "btn-sm");
        verDesglosePMMD.setGraphic(IcoPMMD.of("TABLE_VIEW", 16));
        verDesglosePMMD.setSelected(desglosePMMD.isVisible());
        verDesglosePMMD.setOnAction(ePMMD -> {
            desglosePMMD.setVisible(verDesglosePMMD.isSelected());
            desglosePMMD.setManaged(verDesglosePMMD.isSelected());
            if (verDesglosePMMD.isSelected()) renderDesglosePMMD();
        });
        if (desglosePMMD.isVisible()) renderDesglosePMMD();
        renderVariantesPMMD();
        Button salaPMMD = UIPMMD.secondary("Ver en mi sala", "MEETING_ROOM");
        salaPMMD.setOnAction(ePMMD -> navigatorPMMD.goTo("sala", new IntentPMMD("AGREGAR", configPMMD)));
        Button pdfPMMD = UIPMMD.secondary("Cotización", "PICTURE_AS_PDF");
        UIPMMD.tooltip(pdfPMMD, "Cotización en PDF con el render 3D");
        pdfPMMD.setOnAction(ePMMD -> cotizacionPdfPMMD());
        Button pedidoPMMD = UIPMMD.primary("Crear pedido", "ASSIGNMENT");
        pedidoPMMD.setOnAction(ePMMD -> crearPedido());
        barraPrecioPMMD.setSpacing(16);
        barraPrecioPMMD.getChildren().setAll(precioPMMD, costoPMMD, margenPMMD, diasPMMD, verDesglosePMMD, UIPMMD.spacer(), salaPMMD, pdfPMMD, pedidoPMMD);
    }

    private static VBox fijoPMMD(VBox v) {
        v.setMinWidth(Region.USE_PREF_SIZE);
        return v;
    }

    private void renderDesglosePMMD() {
        ConfiguradorServicePMMD.Cotizacion c = cotizacionPMMD;
        TableView<ConfiguradorServicePMMD.Linea> t = CellsPMMD.tabla("Sin materiales.");
        t.getColumns().addAll(List.of(
                CellsPMMD.nodo("Material", lPMMD -> new VBox(0, UIPMMD.label(lPMMD.material().getNombre(), "cell-title"), UIPMMD.label(lPMMD.material().getCodigo() + " · " + lPMMD.componente(), "cell-sub")), 280),
                CellsPMMD.derecha("Cantidad", lPMMD -> MoneyPMMD.numero(lPMMD.cantidad()) + " " + lPMMD.material().getUnidad(), 110),
                CellsPMMD.derecha("Costo", lPMMD -> MoneyPMMD.fmt(lPMMD.costo()), 100),
                CellsPMMD.badge("Stock", ConfiguradorServicePMMD.Linea::estado, 110)));
        t.getItems().setAll(c.lineas());
        t.setFixedCellSize(40);
        t.setPrefHeight(Math.min(6, c.lineas().size()) * 40 + 42);
        VBox totales = new VBox(6,
                UIPMMD.hbox(8, UIPMMD.label("Materiales", "muted"), UIPMMD.spacer(), UIPMMD.label(MoneyPMMD.fmt(c.costoMateriales()), "cell-title")),
                UIPMMD.hbox(8, UIPMMD.label("Mano de obra (" + MoneyPMMD.numero(c.horasMO()) + " h)", "muted"), UIPMMD.spacer(), UIPMMD.label(MoneyPMMD.fmt(c.costoMO()), "cell-title")),
                UIPMMD.hbox(8, UIPMMD.label("Indirectos 6 %", "muted"), UIPMMD.spacer(), UIPMMD.label(MoneyPMMD.fmt(c.indirectos()), "cell-title")),
                UIPMMD.hbox(8, UIPMMD.label("Costo estándar", "card-title"), UIPMMD.spacer(), UIPMMD.label(MoneyPMMD.fmt(c.costo()), "card-title")),
                UIPMMD.hbox(8, UIPMMD.label("Precio = costo × 1.55 a la decena", "cell-sub"), UIPMMD.spacer(), UIPMMD.label(MoneyPMMD.fmt(c.precio()), "card-title")));
        totales.setPrefWidth(280);
        HBox hPMMD = new HBox(16, t, totales);
        HBox.setHgrow(t, Priority.ALWAYS);
        VBox cardPMMD = UIPMMD.card(hPMMD);
        desglosePMMD.getChildren().setAll(cardPMMD);
    }

    private void renderVariantesPMMD() {
        variantesBoxPMMD.getChildren().clear();
        variantesBoxPMMD.setAlignment(Pos.CENTER);
        String[] letrasPMMD = {"A", "B", "C"};
        for (int iPMMD = 0; iPMMD < 3; iPMMD++) {
            int idxPMMD = iPMMD;
            Button bPMMD = UIPMMD.button(letrasPMMD[iPMMD], null, "btn-sm", variantesPMMD[iPMMD] == null ? "btn-ghost" : "btn-wood");
            bPMMD.setStyle("-fx-padding: 0 10;");
            UIPMMD.tooltip(bPMMD, variantesPMMD[iPMMD] == null ? "Guardar la configuración actual como variante " + letrasPMMD[iPMMD]
                    : "Variante " + letrasPMMD[iPMMD] + ": " + configuradorPMMD.especificacion(variantesPMMD[iPMMD]) + "\nClic: cargar · clic derecho: reemplazar");
            bPMMD.setOnAction(ePMMD -> {
                if (variantesPMMD[idxPMMD] == null) {
                    variantesPMMD[idxPMMD] = configPMMD;
                    toastPMMD.exito("Variante " + letrasPMMD[idxPMMD] + " guardada.");
                    renderVariantesPMMD();
                } else {
                    configPMMD = variantesPMMD[idxPMMD];
                    actualizarPMMD(true);
                }
            });
            bPMMD.setOnContextMenuRequested(ePMMD -> {
                variantesPMMD[idxPMMD] = configPMMD;
                toastPMMD.exito("Variante " + letrasPMMD[idxPMMD] + " reemplazada.");
                renderVariantesPMMD();
            });
            variantesBoxPMMD.getChildren().add(bPMMD);
        }
        Button comparar = UIPMMD.iconButton("COMPARE", "Comparar variantes lado a lado", "btn-ghost");
        comparar.setDisable(Arrays.stream(variantesPMMD).filter(Objects::nonNull).count() < 2);
        comparar.setOnAction(ePMMD -> comparar());
        Label titPMMD = UIPMMD.label("Variantes", "price-label");
        titPMMD.setPadding(new Insets(0, 4, 0, 6));
        variantesBoxPMMD.getChildren().add(0, titPMMD);
        variantesBoxPMMD.getChildren().add(comparar);
    }

    private void comparar() {
        HBox colsPMMD = new HBox(14);
        String[] letrasPMMD = {"A", "B", "C"};
        for (int iPMMD = 0; iPMMD < 3; iPMMD++) {
            ConfigSofaPMMD v = variantesPMMD[iPMMD];
            if (v == null) continue;
            ConfiguradorServicePMMD.Cotizacion c = configuradorPMMD.cotizar(v);
            TelaPMMD t = v.telaId() == null ? null : telasPMMD.tela(v.telaId());
            TelaPMMD tcPMMD = v.telaCojinesId() == null ? t : telasPMMD.tela(v.telaCojinesId());
            WritableImage imgPMMD = Viewer3DPMMD.render(new SofaBuilderPMMD().build(SofaBuilderPMMD.Opciones.de(v, t, tcPMMD)), 300, 200, -30, 16);
            ImageSlotPMMD foto = new ImageSlotPMMD("", 300, 200, 12).soloLectura();
            foto.mostrar(imgPMMD);
            int idxPMMD = iPMMD;
            Button usar = UIPMMD.secondary("Usar esta", null);
            usar.setMaxWidth(Double.MAX_VALUE);
            VBox colPMMD = new VBox(10, UIPMMD.label("Variante " + letrasPMMD[iPMMD], "card-title"), foto,
                    UIPMMD.hbox(8, UIPMMD.label("Precio", "muted"), UIPMMD.spacer(), UIPMMD.label(MoneyPMMD.fmt0(c.precio()), "price-value")),
                    UIPMMD.hbox(8, UIPMMD.label("Fabricación", "muted"), UIPMMD.spacer(), UIPMMD.label(c.dias() + " días", "cell-title")),
                    UIPMMD.hbox(8, UIPMMD.label("Tela", "muted"), UIPMMD.spacer(), t == null ? UIPMMD.label("—") : UIPMMD.hbox(6, new ColorSwatchPMMD(t.getHex(), t.getTipo(), 16), UIPMMD.label(t.getNombre(), "cell-title"))),
                    UIPMMD.wrap(configuradorPMMD.especificacion(v), "cell-sub"), usar);
            colPMMD.getStyleClass().add("variant-card");
            colPMMD.setPrefWidth(320);
            colsPMMD.getChildren().add(colPMMD);
            usar.setOnAction(ePMMD -> {
                configPMMD = variantesPMMD[idxPMMD];
                actualizarPMMD(true);
                overlaysPMMD.cerrarTodos();
            });
        }
        Button cerrarPMMD = UIPMMD.secondary("Cerrar", null);
        var hPMMD = overlaysPMMD.modal("Comparar variantes", "Precio, días de fabricación y tela lado a lado", colsPMMD, 1040, cerrarPMMD);
        cerrarPMMD.setOnAction(ePMMD -> hPMMD.cerrar());
    }



    private byte[] png(double escalaPMMD) {
        try (ByteArrayOutputStream outPMMD = new ByteArrayOutputStream()) {
            ImageIO.write(SnapshotPMMD.imagen(visorPMMD.captura(escalaPMMD)), "png", outPMMD);
            return outPMMD.toByteArray();
        } catch (Exception ePMMD) {
            return null;
        }
    }

    private void capturaPngPMMD() {
        File fPMMD = ArchivosPMMD.guardar(visorPMMD, "Sofa-" + configPMMD.modelo().nombre().replace(" ", "-"), "Imagen PNG", "png");
        if (fPMMD == null) return;
        if (toastPMMD.intentar(() -> {
            try {
                ImageIO.write(SnapshotPMMD.imagen(visorPMMD.captura(2)), "png", fPMMD);
            } catch (java.io.IOException ePMMD) {
                throw new NegocioExceptionPMMD("No se pudo guardar la captura.");
            }
        })) toastPMMD.exito("Captura guardada: " + fPMMD.getName());
    }

    private void pantallaCompletaPMMD() {
        Viewer3DPMMD v = new Viewer3DPMMD(true);
        v.setContenido(new SofaBuilderPMMD().build(SofaBuilderPMMD.Opciones.de(configPMMD, tela(), telaCojinesPMMD())), true);
        Label ayuda = UIPMMD.label("Esc para salir · arrastra para girar · rueda para acercar", "viewer-hint");
        StackPane.setAlignment(ayuda, Pos.BOTTOM_CENTER);
        StackPane.setMargin(ayuda, new Insets(20));
        StackPane root = new StackPane(v, ayuda);
        root.getStyleClass().add("viewer3d");
        Scene sPMMD = new Scene(root, 1280, 800, true);
        sPMMD.getStylesheets().setAll(appWindowPMMD.scene().getStylesheets());
        Stage stPMMD = new Stage();
        stPMMD.initOwner(appWindowPMMD.stage());
        stPMMD.setTitle("Configurador 3D · " + configuradorPMMD.especificacion(configPMMD));
        stPMMD.setScene(sPMMD);
        stPMMD.setFullScreenExitHint("");
        stPMMD.setFullScreen(true);
        sPMMD.setOnKeyPressed(ePMMD -> {
            if (ePMMD.getCode() == KeyCode.ESCAPE) stPMMD.close();
        });
        stPMMD.show();
        javafx.application.Platform.runLater(() -> v.centrar(false));
    }

    private void cotizacionPdfPMMD() {
        if (cotizacionPMMD == null) return;
        List<ClientePMMD> csPMMD = new ArrayList<>();
        csPMMD.add(null);
        csPMMD.addAll(clientesPMMD.listar());
        ComboBox<ClientePMMD> clientePMMD = FormsPMMD.combo(csPMMD, null, c -> c == null ? "Cliente por definir" : c.getNombre());
        Button cancelarPMMD = UIPMMD.secondary("Cancelar", null);
        Button okPMMD = UIPMMD.primary("Generar PDF", "PICTURE_AS_PDF");
        var hPMMD = overlaysPMMD.modal("Cotización PDF", configuradorPMMD.especificacion(configPMMD), UIPMMD.field("Cliente", clientePMMD), 480, cancelarPMMD, okPMMD);
        cancelarPMMD.setOnAction(ePMMD -> hPMMD.cerrar());
        okPMMD.setOnAction(ePMMD -> {
            hPMMD.cerrar();
            File fPMMD = ArchivosPMMD.guardar(visorPMMD, "Cotizacion-sofa-a-medida", "PDF", "pdf");
            if (fPMMD == null) return;
            byte[] imagenPMMD = png(2);
            ConfiguradorServicePMMD.Cotizacion c = cotizacionPMMD;
            String especPMMD = configuradorPMMD.especificacion(configPMMD);
            String nombre = clientePMMD.getValue() == null ? "Por definir" : clientePMMD.getValue().getNombre();
            TareasPMMD.ejecutar(overlaysPMMD, toastPMMD, "Generando cotización…", () -> {
                exportPMMD.cotizacionConfigurador(c, especPMMD, nombre, imagenPMMD, fPMMD);
                return fPMMD;
            }, aPMMD -> {
                toastPMMD.exito("Cotización guardada: " + aPMMD.getName());
                appWindowPMMD.hostServices().showDocument(aPMMD.toURI().toString());
            });
        });
    }

    private void crearPedido() {
        if (cotizacionPMMD == null) return;
        ValidacionPMMD v = new ValidacionPMMD();
        ComboBox<ClientePMMD> clientePMMD = FormsPMMD.combo(clientesPMMD.listar(), null, ClientePMMD::getNombre);
        DatePicker entregaPMMD = FormsPMMD.fecha(AppClockPMMD.hoy().plusDays(cotizacionPMMD.dias()));
        BigDecimal precioPMMD = BigDecimal.valueOf(cotizacionPMMD.precio());
        TextField adelantoPMMD = FormsPMMD.decimal(precioPMMD.divide(BigDecimal.valueOf(2)).setScale(0, java.math.RoundingMode.UP));
        ComboBox<String> metodoPMMD = FormsPMMD.combo(List.of("Efectivo", "Tarjeta", "Transferencia", "Yape", "Plin"), "Efectivo", sPMMD -> sPMMD);
        TextArea obsPMMD = FormsPMMD.area("", "Observaciones (piso, acceso, horario de entrega…)", 2);
        CheckBox crearOPPMMD = new CheckBox("Crear también la orden de producción (guarda la ficha " + cotizacionPMMD.ficha().getEtiqueta() + " y las sustituciones)");
        crearOPPMMD.setSelected(true);
        crearOPPMMD.setWrapText(true);
        ComboBox<EmpleadoPMMD> respPMMD = FormsPMMD.combo(empleadosPMMD.taller(), empleadosPMMD.taller().isEmpty() ? null : empleadosPMMD.taller().get(0), EmpleadoPMMD::getNombre);
        respPMMD.disableProperty().bind(crearOPPMMD.selectedProperty().not());
        FormGridPMMD fPMMD = new FormGridPMMD(2);
        fPMMD.nodo(UIPMMD.wrap(configuradorPMMD.especificacion(configPMMD), "muted"), 2);
        fPMMD.nodo(v.campo("Cliente", clientePMMD, () -> ValidacionPMMD.requerido(clientePMMD, "el cliente")), 2);
        fPMMD.nodo(v.campo("Entrega", entregaPMMD, () -> entregaPMMD.getValue() == null || entregaPMMD.getValue().isBefore(AppClockPMMD.hoy()) ? "Elige una fecha futura." : null), 1);
        fPMMD.nodo(v.campo("Precio", FormsPMMD.soloLectura(MoneyPMMD.fmt(precioPMMD)), null), 1);
        fPMMD.nodo(v.campo("Adelanto (S/)", adelantoPMMD, () -> {
            String ePMMD = ValidacionPMMD.numero(adelantoPMMD, 0, "El adelanto");
            if (ePMMD != null) return ePMMD;
            return ValidacionPMMD.valor(adelantoPMMD) > precioPMMD.doubleValue() ? "El adelanto no puede superar el precio." : null;
        }), 1);
        fPMMD.nodo(v.campo("Método", metodoPMMD, null), 1);
        fPMMD.nodo(v.campo("Observaciones", obsPMMD, null), 2);
        fPMMD.nodo(crearOPPMMD, 2);
        fPMMD.nodo(v.campo("Responsable de la OP", respPMMD, null), 2);
        Label notaPMMD = UIPMMD.label("", "cell-sub");
        Runnable n = () -> notaPMMD.setText(ValidacionPMMD.valor(adelantoPMMD) >= precioPMMD.doubleValue() / 2 ? "Con 50 % o más el pedido queda Confirmado."
                : "Con menos del 50 % el pedido queda Registrado y la OP se crea en Pendiente.");
        adelantoPMMD.textProperty().addListener((oPMMD, aPMMD, bPMMD) -> n.run());
        n.run();
        fPMMD.nodo(notaPMMD, 2);
        Button cancelarPMMD = UIPMMD.secondary("Cancelar", null);
        Button okPMMD = UIPMMD.primary("Crear pedido", "ASSIGNMENT");
        v.enlazar(okPMMD);
        var hPMMD = overlaysPMMD.modal("Crear pedido personalizado", MoneyPMMD.fmt(precioPMMD) + " · " + cotizacionPMMD.dias() + " días de fabricación", fPMMD, 620, cancelarPMMD, okPMMD);
        cancelarPMMD.setOnAction(ePMMD -> hPMMD.cerrar());
        okPMMD.setOnAction(ePMMD -> {
            PedidoPMMD[] pPMMD = new PedidoPMMD[1];
            if (toastPMMD.intentar(() -> pPMMD[0] = configuradorPMMD.crearPedido(configPMMD, clientePMMD.getValue().getId(), entregaPMMD.getValue(),
                    FormsPMMD.dinero(adelantoPMMD), metodoPMMD.getValue(), obsPMMD.getText(), crearOPPMMD.isSelected(), respPMMD.getValue() == null ? null : respPMMD.getValue().getId()))) {
                hPMMD.cerrar();
                toastPMMD.exito(pPMMD[0].getCodigo() + " creado para " + clientePMMD.getValue().getNombre() + (pPMMD[0].getOpCodigo() != null ? " · " + pPMMD[0].getOpCodigo() + " en Pendiente" : "") + ".");
                navigatorPMMD.datosCambiaron();
                navigatorPMMD.goTo("pedidos", IntentPMMD.abrir(pPMMD[0].getCodigo()));
            }
        });
    }
}
