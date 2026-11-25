package pe.erp.sofas.ui;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.util.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pe.erp.sofas.core.*;
import pe.erp.sofas.service.BusquedaServicePMMD;
import pe.erp.sofas.service.GarantiaServicePMMD;
import pe.erp.sofas.service.InventarioServicePMMD;
import pe.erp.sofas.service.NotificacionServicePMMD;
import pe.erp.sofas.ui.components.*;

import java.util.*;







@Component
@Lazy
@Scope("prototype")
@RequiredArgsConstructor
public class ShellControllerPMMD implements NavigatorPMMD.Host {

    private static final double ANCHO_MENUPMMD = 248;

    private static final double ANCHO_CONTRAIDOPMMD = 72;

    private static final int MAX_HISTORIALPMMD = 25;

    private final AppWindowPMMD appWindowPMMD;

    private final NavigatorPMMD navigatorPMMD;

    private final OverlaysPMMD overlaysPMMD;

    private final ToastPMMD toastPMMD;

    private final ConfirmDialogPMMD confirmDialogPMMD;

    private final ThemeManagerPMMD themeManagerPMMD;

    private final SessionPMMD sessionPMMD;

    private final BusquedaServicePMMD busquedaServicePMMD;

    private final NotificacionServicePMMD notificacionServicePMMD;

    private final GarantiaServicePMMD garantiaServicePMMD;

    private final InventarioServicePMMD inventarioServicePMMD;

    @FXML private StackPane raiz;

    @FXML private VBox sidebar;

    @FXML private HBox marca;

    @FXML private VBox nav;

    @FXML private VBox pieMenu;

    @FXML private HBox buscadorHost;

    @FXML private Label lblFecha;

    @FXML private Button btnTema;

    @FXML private StackPane campanaHost;

    @FXML private HBox perfil;

    @FXML private Button btnRegresar;

    @FXML private Label lblTitulo;

    @FXML private Label lblDescripcion;

    @FXML private StackPane vistaHost;

    @FXML private Pane capaPopups;

    @FXML private VBox zonaToasts;

    private final Map<ModuloPMMD, HBox> itemsMenuPMMD = new EnumMap<>(ModuloPMMD.class);

    private final Map<ModuloPMMD, Label> contadoresPMMD = new EnumMap<>(ModuloPMMD.class);

    private final Map<ModuloPMMD, AppWindowPMMD.Cargado<VistaPMMD>> vistasPMMD = new EnumMap<>(ModuloPMMD.class);

    private final Deque<ModuloPMMD> historialPMMD = new ArrayDeque<>();

    private final List<Node> textosMenuPMMD = new ArrayList<>();

    private ModuloPMMD actual;

    private int tokenNavegacionPMMD;

    private boolean contraidoPMMD;

    private SearchFieldPMMD buscadorPMMD;

    private Label contadorCampanaPMMD;

    private Region popupAbiertoPMMD;

    private Node anclaPopupPMMD;

    private Button btnContraerPMMD;

    @FXML
    public void initialize() {
        toastPMMD.registrar(zonaToasts);
        overlaysPMMD.registrar(raiz);
        navigatorPMMD.registrar(this);

        construirMenuPMMD();
        construirBarraSuperiorPMMD();
        btnRegresar.setGraphic(IcoPMMD.of("ARROW_BACK", 17));
        btnRegresar.setText("Regresar");
        btnRegresar.setMinWidth(Region.USE_PREF_SIZE);
        UIPMMD.tooltip(btnRegresar, "Volver a la pantalla anterior");

        raiz.addEventFilter(KeyEvent.KEY_PRESSED, ePMMD -> {
            if (ePMMD.getCode() == KeyCode.K && ePMMD.isShortcutDown()) {
                buscadorPMMD.campo().requestFocus();
                buscadorPMMD.campo().selectAll();
                ePMMD.consume();
            } else if (ePMMD.getCode() == KeyCode.ESCAPE && popupAbiertoPMMD != null) {
                cerrarPopupPMMD();
                ePMMD.consume();
            }
        });
        raiz.addEventFilter(MouseEvent.MOUSE_PRESSED, ePMMD -> {
            if (popupAbiertoPMMD != null && !dentroPMMD(ePMMD.getTarget(), popupAbiertoPMMD) && !dentroPMMD(ePMMD.getTarget(), anclaPopupPMMD)) cerrarPopupPMMD();
        });
        if (PrefsPMMD.bool(PrefsPMMD.MENU_CONTRAIDO, false)) contraerPMMD(true, false);
    }

    public void arrancar(String moduloInicioPMMD) {
        mostrar(ModuloPMMD.porClave(moduloInicioPMMD == null ? "dashboard" : moduloInicioPMMD), IntentPMMD.NONE, false);
    }



    private void construirMenuPMMD() {
        LogoMarkPMMD logoPMMD = new LogoMarkPMMD(38, "ERP · SOFÁS");
        textosMenuPMMD.add(logoPMMD.getChildren().get(1));
        btnContraerPMMD = UIPMMD.iconButton("MENU_OPEN", "Contraer menú", "btn-ghost", "nav-toggle");
        marca.getChildren().addAll(logoPMMD, UIPMMD.spacer(), btnContraerPMMD);
        marca.setAlignment(Pos.CENTER_LEFT);
        btnContraerPMMD.setOnAction(ePMMD -> contraerPMMD(!contraidoPMMD, true));

        String seccionPMMD = "";
        for (ModuloPMMD mPMMD : ModuloPMMD.values()) {
            if (!mPMMD.enMenu()) continue;
            if (mPMMD.seccion() != null && !mPMMD.seccion().equals(seccionPMMD)) {
                seccionPMMD = mPMMD.seccion();
                Label tituloPMMD = UIPMMD.label(seccionPMMD, "nav-section");
                nav.getChildren().add(tituloPMMD);
                textosMenuPMMD.add(tituloPMMD);
            }
            Label texto = UIPMMD.label(mPMMD.titulo(), "nav-text");
            Label contador = UIPMMD.label("", "nav-badge");
            contador.setVisible(false);
            contador.setManaged(false);
            HBox itemPMMD = new HBox(12, IcoPMMD.of(mPMMD.icono(), 19), texto, UIPMMD.spacer(), contador);
            if (mPMMD == ModuloPMMD.CONFIGURADOR) {
                Label tresPMMD = UIPMMD.label("3D", "nav-badge", "nav-badge-3d");
                itemPMMD.getChildren().add(tresPMMD);
                textosMenuPMMD.add(tresPMMD);
            }
            itemPMMD.getStyleClass().add("nav-item");
            itemPMMD.setAlignment(Pos.CENTER_LEFT);
            itemPMMD.setFocusTraversable(true);
            itemPMMD.setAccessibleText(mPMMD.titulo());
            Tooltip t = new Tooltip(mPMMD.titulo());
            t.setShowDelay(Duration.millis(250));
            Tooltip.install(itemPMMD, t);
            itemPMMD.setOnMouseClicked(ePMMD -> mostrar(mPMMD, IntentPMMD.NONE, true));
            itemPMMD.setOnKeyPressed(ePMMD -> {
                if (ePMMD.getCode() == KeyCode.ENTER || ePMMD.getCode() == KeyCode.SPACE) mostrar(mPMMD, IntentPMMD.NONE, true);
            });
            textosMenuPMMD.add(texto);
            itemsMenuPMMD.put(mPMMD, itemPMMD);
            contadoresPMMD.put(mPMMD, contador);
            nav.getChildren().add(itemPMMD);
        }


        AvatarInitialsPMMD avatar = new AvatarInitialsPMMD(sessionPMMD.nombre(), 36, "gradient");
        VBox textos = new VBox(1, UIPMMD.label(sessionPMMD.nombre(), "user-name"), UIPMMD.label(sessionPMMD.cargo(), "user-role"));
        HBox tarjetaPMMD = new HBox(10, avatar, textos);
        tarjetaPMMD.setAlignment(Pos.CENTER_LEFT);
        tarjetaPMMD.getStyleClass().add("user-card");
        textosMenuPMMD.add(textos);
        Label salirTxt = UIPMMD.label("Cerrar sesión", "nav-text");
        HBox salirPMMD = new HBox(12, IcoPMMD.of("EXIT_TO_APP", 19), salirTxt);
        salirPMMD.getStyleClass().addAll("nav-item", "nav-logout");
        salirPMMD.setAlignment(Pos.CENTER_LEFT);
        salirPMMD.setFocusTraversable(true);
        salirPMMD.setOnMouseClicked(ePMMD -> cerrarSesion());
        salirPMMD.setOnKeyPressed(ePMMD -> {
            if (ePMMD.getCode() == KeyCode.ENTER || ePMMD.getCode() == KeyCode.SPACE) cerrarSesion();
        });
        textosMenuPMMD.add(salirTxt);
        pieMenu.getChildren().addAll(tarjetaPMMD, salirPMMD);
    }

    private void contraerPMMD(boolean valorPMMD, boolean animarPMMD) {
        contraidoPMMD = valorPMMD;
        PrefsPMMD.setBool(PrefsPMMD.MENU_CONTRAIDO, valorPMMD);
        double ancho = valorPMMD ? ANCHO_CONTRAIDOPMMD : ANCHO_MENUPMMD;
        sidebar.pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("collapsed"), valorPMMD);
        UIPMMD.tooltip(btnContraerPMMD, valorPMMD ? "Expandir menú" : "Contraer menú");
        btnContraerPMMD.setGraphic(IcoPMMD.of(valorPMMD ? "MENU" : "MENU_OPEN", 18));
        if (valorPMMD) {
            textosMenuPMMD.forEach(n -> {
                n.setVisible(false);
                n.setManaged(false);
            });
            contadoresPMMD.values().forEach(c -> {
                c.setVisible(false);
                c.setManaged(false);
            });
        }
        Runnable alFinalPMMD = () -> {
            if (!valorPMMD) {
                textosMenuPMMD.forEach(n -> {
                    n.setVisible(true);
                    n.setManaged(true);
                });
                if (contadorCampanaPMMD != null) refrescarContadores();
            }
        };
        if (!animarPMMD) {
            sidebar.setPrefWidth(ancho);
            sidebar.setMinWidth(ancho);
            sidebar.setMaxWidth(ancho);
            alFinalPMMD.run();
            return;
        }
        Interpolator suave = Interpolator.SPLINE(0.4, 0, 0.2, 1);
        Timeline t = new Timeline(new KeyFrame(Duration.millis(220),
                new KeyValue(sidebar.prefWidthProperty(), ancho, suave),
                new KeyValue(sidebar.minWidthProperty(), ancho, suave),
                new KeyValue(sidebar.maxWidthProperty(), ancho, suave)));
        t.setOnFinished(ePMMD -> alFinalPMMD.run());
        t.play();
    }



    private void construirBarraSuperiorPMMD() {
        buscadorPMMD = new SearchFieldPMMD("Buscar sofás, telas, clientes, pedidos…");
        buscadorPMMD.getStyleClass().add("global-search");
        buscadorPMMD.getChildren().add(UIPMMD.label("Ctrl K", "kbd"));
        HBox.setHgrow(buscadorPMMD, Priority.ALWAYS);
        buscadorHost.getChildren().add(buscadorPMMD);
        buscadorHost.setAlignment(Pos.CENTER_LEFT);
        buscadorPMMD.textProperty().addListener((oPMMD, aPMMD, n) -> actualizarBusquedaPMMD());
        buscadorPMMD.campo().focusedProperty().addListener((oPMMD, aPMMD, f) -> {
            if (f && buscadorPMMD.getText().length() >= 2) actualizarBusquedaPMMD();
        });
        buscadorPMMD.campo().setOnKeyPressed(ePMMD -> {
            if (ePMMD.getCode() == KeyCode.ENTER) {
                var resPMMD = busquedaServicePMMD.buscar(buscadorPMMD.getText());
                if (!resPMMD.isEmpty()) abrirResultadoPMMD(resPMMD.get(0));
            }
        });

        lblFecha.setText(AppClockPMMD.fechaLarga());
        lblFecha.setGraphic(IcoPMMD.of("CALENDAR_TODAY", 16));

        actualizarIconoTemaPMMD();
        themeManagerPMMD.oscuroProperty().addListener((oPMMD, aPMMD, n) -> actualizarIconoTemaPMMD());

        Button campanaPMMD = UIPMMD.iconButton("NOTIFICATIONS", "Notificaciones", "btn-ghost", "topbar-btn");
        contadorCampanaPMMD = UIPMMD.label("", "bell-count");
        StackPane.setAlignment(contadorCampanaPMMD, Pos.TOP_RIGHT);
        contadorCampanaPMMD.setMouseTransparent(true);
        campanaHost.getChildren().addAll(campanaPMMD, contadorCampanaPMMD);
        campanaPMMD.setOnAction(ePMMD -> alternarPopupPMMD(campanaHost, this::popupNotificacionesPMMD));

        AvatarInitialsPMMD avatar = new AvatarInitialsPMMD(sessionPMMD.nombre(), 34, "gradient");
        perfil.getChildren().addAll(avatar, IcoPMMD.of("EXPAND_MORE", 18));
        perfil.setAlignment(Pos.CENTER_LEFT);
        perfil.setFocusTraversable(true);
        Tooltip.install(perfil, new Tooltip(sessionPMMD.nombre() + " · " + sessionPMMD.cargo()));
        perfil.setOnMouseClicked(ePMMD -> alternarPopupPMMD(perfil, this::popupPerfilPMMD));
        perfil.setOnKeyPressed(ePMMD -> {
            if (ePMMD.getCode() == KeyCode.ENTER || ePMMD.getCode() == KeyCode.SPACE) alternarPopupPMMD(perfil, this::popupPerfilPMMD);
        });
    }

    private void actualizarIconoTemaPMMD() {
        boolean oscuroPMMD = themeManagerPMMD.isOscuro();
        btnTema.setGraphic(IcoPMMD.of(oscuroPMMD ? "WB_SUNNY" : "NIGHTS_STAY", 19));
        UIPMMD.tooltip(btnTema, oscuroPMMD ? "Cambiar a modo claro" : "Cambiar a modo oscuro");
    }

    @FXML
    private void onTemaPMMD() {
        themeManagerPMMD.alternar();
    }



    private void alternarPopupPMMD(Node anclaPMMD, java.util.function.Supplier<Region> fabricaPMMD) {
        if (popupAbiertoPMMD != null && anclaPopupPMMD == anclaPMMD) {
            cerrarPopupPMMD();
            return;
        }
        abrirPopupPMMD(anclaPMMD, fabricaPMMD.get(), true);
    }

    private void abrirPopupPMMD(Node anclaPMMD, Region contenido, boolean derechaPMMD) {
        cerrarPopupPMMD();
        contenido.getStyleClass().add("popup");
        capaPopups.getChildren().add(contenido);
        contenido.applyCss();
        contenido.autosize();
        Bounds bPMMD = anclaPMMD.localToScene(anclaPMMD.getBoundsInLocal());
        Point2D pPMMD = capaPopups.sceneToLocal(derechaPMMD ? bPMMD.getMaxX() : bPMMD.getMinX(), bPMMD.getMaxY() + 8);
        double ancho = contenido.prefWidth(-1);
        double xPMMD = derechaPMMD ? pPMMD.getX() - ancho : Math.min(pPMMD.getX(), capaPopups.getWidth() - ancho - 16);
        contenido.relocate(xPMMD, pPMMD.getY());
        popupAbiertoPMMD = contenido;
        anclaPopupPMMD = anclaPMMD;
        contenido.setOpacity(0);
        contenido.setTranslateY(-6);
        FadeTransition f = new FadeTransition(Duration.millis(150), contenido);
        f.setToValue(1);
        TranslateTransition t = new TranslateTransition(Duration.millis(150), contenido);
        t.setToY(0);
        new ParallelTransition(f, t).play();
    }

    private void cerrarPopupPMMD() {
        if (popupAbiertoPMMD != null) {
            capaPopups.getChildren().remove(popupAbiertoPMMD);
            popupAbiertoPMMD = null;
            anclaPopupPMMD = null;
        }
    }

    private static boolean dentroPMMD(Object objetivo, Node contenedorPMMD) {
        if (contenedorPMMD == null || !(objetivo instanceof Node n)) return false;
        for (Node xPMMD = n; xPMMD != null; xPMMD = xPMMD.getParent()) if (xPMMD == contenedorPMMD) return true;
        return false;
    }

    private void actualizarBusquedaPMMD() {
        String qPMMD = buscadorPMMD.getText();
        if (qPMMD.length() < 2) {
            if (anclaPopupPMMD == buscadorHost) cerrarPopupPMMD();
            return;
        }
        var resultadosPMMD = busquedaServicePMMD.buscar(qPMMD);
        VBox caja = new VBox(2);
        caja.setPrefWidth(Math.max(440, buscadorHost.getWidth()));
        caja.getStyleClass().add("search-popup");
        if (resultadosPMMD.isEmpty()) {
            caja.getChildren().add(UIPMMD.empty("SEARCH", "Sin resultados para “" + qPMMD + "”.", "Prueba con un código como SF-001, TL-07, PE-0241 u OP-1021.", null));
        } else {
            String grupoPMMD = null;
            boolean primeroPMMD = true;
            for (BusquedaServicePMMD.Resultado rPMMD : resultadosPMMD) {
                if (!rPMMD.grupo().equals(grupoPMMD)) {
                    grupoPMMD = rPMMD.grupo();
                    caja.getChildren().add(UIPMMD.label(grupoPMMD.toUpperCase(AppClockPMMD.PERU), "popup-caption"));
                }
                StackPane ic = new StackPane(IcoPMMD.of(rPMMD.icono(), 17));
                ic.getStyleClass().add("popup-item-icon");
                VBox textos = new VBox(1, UIPMMD.label(rPMMD.titulo(), "popup-item-title"), UIPMMD.label(rPMMD.subtitulo(), "popup-item-sub"));
                HBox filaPMMD = new HBox(12, ic, textos, UIPMMD.spacer());
                if (primeroPMMD) {
                    filaPMMD.getChildren().add(UIPMMD.label("Enter ↵", "kbd"));
                    primeroPMMD = false;
                }
                filaPMMD.setAlignment(Pos.CENTER_LEFT);
                filaPMMD.getStyleClass().add("popup-item");
                UIPMMD.onClick(filaPMMD, ePMMD -> abrirResultadoPMMD(rPMMD));
                caja.getChildren().add(filaPMMD);
            }
        }
        ScrollPane sp = new ScrollPane(caja);
        sp.setFitToWidth(true);
        sp.setMaxHeight(520);
        sp.setPrefViewportHeight(Math.min(500, caja.prefHeight(-1)));
        sp.getStyleClass().add("modal-scroll");
        VBox contPMMD = new VBox(sp);
        cerrarPopupPMMD();
        abrirPopupPMMD(buscadorHost, contPMMD, false);
    }

    private void abrirResultadoPMMD(BusquedaServicePMMD.Resultado rPMMD) {
        cerrarPopupPMMD();
        buscadorPMMD.setText("");
        navigatorPMMD.goTo(rPMMD.modulo(), IntentPMMD.abrir(rPMMD.dato()));
    }

    private Region popupNotificacionesPMMD() {
        VBox caja = new VBox(4);
        caja.setPrefWidth(400);
        Button marcarPMMD = UIPMMD.button("Marcar todas como leídas", null, "btn-link");
        marcarPMMD.setOnAction(ePMMD -> {
            notificacionServicePMMD.marcarTodasLeidas();
            refrescarContadores();
            cerrarPopupPMMD();
            toastPMMD.exito("Notificaciones marcadas como leídas.");
        });
        HBox cabPMMD = UIPMMD.hbox(8, UIPMMD.label("Notificaciones", "popup-title"), UIPMMD.spacer(), marcarPMMD);
        cabPMMD.getStyleClass().add("popup-header");
        caja.getChildren().add(cabPMMD);
        var listaPMMD = notificacionServicePMMD.todas();
        if (listaPMMD.isEmpty()) caja.getChildren().add(UIPMMD.empty("NOTIFICATIONS_NONE", "Sin notificaciones", "Todo está al día.", null));
        for (var n : listaPMMD) {
            StackPane ic = new StackPane(IcoPMMD.of(n.icono(), 17));
            ic.getStyleClass().addAll("notif-icon", "notif-" + n.tipo());
            VBox textos = new VBox(2, UIPMMD.wrap(n.titulo(), "popup-item-title"), UIPMMD.wrap(n.detalle(), "popup-item-sub"),
                    UIPMMD.label(n.hace(), "popup-item-time"));
            HBox.setHgrow(textos, Priority.ALWAYS);
            boolean leidaPMMD = notificacionServicePMMD.leida(n);
            Button leer = UIPMMD.iconButton("DONE", "Marcar como leída", "btn-ghost", "btn-xs");
            leer.setDisable(leidaPMMD);
            leer.setOnAction(ePMMD -> {
                notificacionServicePMMD.marcarLeida(n);
                refrescarContadores();
                cerrarPopupPMMD();
                abrirPopupPMMD(campanaHost, popupNotificacionesPMMD(), true);
            });
            Button descartar = UIPMMD.iconButton("CLOSE", "Descartar", "btn-ghost", "btn-xs");
            descartar.setOnAction(ePMMD -> {
                notificacionServicePMMD.descartar(n);
                refrescarContadores();
                cerrarPopupPMMD();
                abrirPopupPMMD(campanaHost, popupNotificacionesPMMD(), true);
            });
            VBox accionesPMMD = new VBox(2, leer, descartar);
            HBox filaPMMD = new HBox(12, ic, textos);
            if (!leidaPMMD) {
                Region puntoPMMD = new Region();
                puntoPMMD.getStyleClass().add("unread-dot");
                filaPMMD.getChildren().add(puntoPMMD);
            }
            filaPMMD.getChildren().add(accionesPMMD);
            filaPMMD.getStyleClass().add("popup-item");
            if (leidaPMMD) filaPMMD.getStyleClass().add("popup-item-read");
            textos.setOnMouseClicked(ePMMD -> {
                cerrarPopupPMMD();
                notificacionServicePMMD.marcarLeida(n);
                navigatorPMMD.goTo(n.modulo(), n.dato() == null ? IntentPMMD.NONE : IntentPMMD.abrir(n.dato()));
            });
            textos.getStyleClass().add("clickable");
            caja.getChildren().add(filaPMMD);
        }
        return caja;
    }

    private Region popupPerfilPMMD() {
        VBox caja = new VBox(2);
        caja.setPrefWidth(260);
        VBox cabPMMD = new VBox(2, UIPMMD.label(sessionPMMD.nombre(), "popup-item-title"),
                UIPMMD.label(sessionPMMD.cargo() + " · " + sessionPMMD.username(), "popup-item-sub"));
        cabPMMD.getStyleClass().add("popup-header");
        caja.getChildren().add(cabPMMD);
        caja.getChildren().add(itemMenuPMMD("ACCOUNT_CIRCLE", "Mi perfil", () -> navigatorPMMD.goTo("configuracion", IntentPMMD.abrir("perfil"))));
        caja.getChildren().add(itemMenuPMMD("SETTINGS", "Configuración", () -> navigatorPMMD.goTo("configuracion", IntentPMMD.NONE)));
        caja.getChildren().add(UIPMMD.separator());
        HBox salirPMMD = itemMenuPMMD("EXIT_TO_APP", "Cerrar sesión", this::cerrarSesion);
        salirPMMD.getStyleClass().add("popup-item-danger");
        caja.getChildren().add(salirPMMD);
        return caja;
    }

    private HBox itemMenuPMMD(String iconoPMMD, String texto, Runnable accionPMMD) {
        HBox filaPMMD = new HBox(12, IcoPMMD.of(iconoPMMD, 18), UIPMMD.label(texto, "popup-item-title"));
        filaPMMD.setAlignment(Pos.CENTER_LEFT);
        filaPMMD.getStyleClass().addAll("popup-item", "menu-item-row");
        UIPMMD.onClick(filaPMMD, ePMMD -> {
            cerrarPopupPMMD();
            accionPMMD.run();
        });
        return filaPMMD;
    }

    @Override
    public void cerrarSesion() {
        confirmDialogPMMD.mostrar(ConfirmDialogPMMD.Tipo.AVISO, "¿Cerrar sesión?",
                "Se cerrará tu sesión en SISTEMA ERP SOFAS. Los cambios ya están guardados en la base de datos.", "Cerrar sesión", () -> {
                    sessionPMMD.cerrar();
                    appWindowPMMD.mostrarLogin();
                });
    }



    @Override
    public void mostrar(ModuloPMMD moduloPMMD, IntentPMMD intentPMMD, boolean registrarHistorialPMMD) {
        cerrarPopupPMMD();
        overlaysPMMD.cerrarTodos();
        if (registrarHistorialPMMD && actual != null && actual != moduloPMMD) {
            historialPMMD.push(actual);
            while (historialPMMD.size() > MAX_HISTORIALPMMD) historialPMMD.removeLast();
        }
        boolean mismoModuloPMMD = actual == moduloPMMD && vistasPMMD.containsKey(moduloPMMD);
        ModuloPMMD anteriorPMMD = actual;
        actual = moduloPMMD;
        ModuloPMMD marcadoPMMD = moduloPMMD == ModuloPMMD.SALA ? ModuloPMMD.CONFIGURADOR : moduloPMMD;
        itemsMenuPMMD.forEach((mPMMD, itemPMMD) -> itemPMMD.pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("active"), mPMMD == marcadoPMMD));
        lblTitulo.setText(moduloPMMD.titulo());
        lblDescripcion.setText(moduloPMMD.descripcion());
        boolean hayPMMD = !historialPMMD.isEmpty();
        btnRegresar.setVisible(hayPMMD);
        btnRegresar.setManaged(hayPMMD);

        if (anteriorPMMD != null && anteriorPMMD != moduloPMMD && vistasPMMD.containsKey(anteriorPMMD)
                && vistasPMMD.get(anteriorPMMD).controlador() instanceof VistaPMMD.ConCiclo c) c.onHide();

        int tokenPMMD = ++tokenNavegacionPMMD;
        IntentPMMD inPMMD = intentPMMD == null ? IntentPMMD.NONE : intentPMMD;
        if (mismoModuloPMMD) {
            vistasPMMD.get(moduloPMMD).controlador().onShow(inPMMD);
            refrescarContadores();
            return;
        }
        SkeletonPMMD esqueletoPMMD = new SkeletonPMMD();
        vistaHost.getChildren().setAll(esqueletoPMMD);
        PauseTransition esperaPMMD = new PauseTransition(Duration.millis(250));
        esperaPMMD.setOnFinished(ePMMD -> {
            if (tokenPMMD != tokenNavegacionPMMD) return;
            AppWindowPMMD.Cargado<VistaPMMD> vistaPMMD = vistasPMMD.computeIfAbsent(moduloPMMD, mPMMD -> appWindowPMMD.cargar(mPMMD.fxml()));
            vistaHost.getChildren().setAll(vistaPMMD.nodo());
            vistaPMMD.controlador().onShow(inPMMD);
            FadeTransition f = new FadeTransition(Duration.millis(200), vistaPMMD.nodo());
            f.setFromValue(0);
            f.setToValue(1);
            f.play();
            refrescarContadores();
        });
        esperaPMMD.play();
    }

    @Override
    public boolean regresar() {
        if (historialPMMD.isEmpty()) return false;
        mostrar(historialPMMD.pop(), IntentPMMD.NONE, false);
        return true;
    }

    @FXML
    private void onRegresarPMMD() {
        regresar();
    }

    @Override
    public void refrescarContadores() {
        fijarContadorPMMD(ModuloPMMD.POSVENTA, garantiaServicePMMD.abiertos());
        fijarContadorPMMD(ModuloPMMD.INVENTARIO, inventarioServicePMMD.criticos().stream().filter(mPMMD -> !mPMMD.getBajoPedido()).count());
        long noLeidasPMMD = notificacionServicePMMD.noLeidas();
        contadorCampanaPMMD.setText(String.valueOf(noLeidasPMMD));
        contadorCampanaPMMD.setVisible(noLeidasPMMD > 0);
    }

    private void fijarContadorPMMD(ModuloPMMD mPMMD, long valorPMMD) {
        Label lPMMD = contadoresPMMD.get(mPMMD);
        if (lPMMD == null) return;
        lPMMD.setText(String.valueOf(valorPMMD));
        lPMMD.setVisible(valorPMMD > 0 && !contraidoPMMD);
        lPMMD.setManaged(lPMMD.isVisible());
    }

    public ModuloPMMD actual() {
        return actual;
    }
}
