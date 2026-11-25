package pe.erp.sofas.ui.views.sala;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pe.erp.sofas.core.*;
import pe.erp.sofas.model.*;
import pe.erp.sofas.service.*;
import pe.erp.sofas.ui.AppWindowPMMD;
import pe.erp.sofas.ui.components.*;
import pe.erp.sofas.ui.three.RoomBuilderPMMD;
import pe.erp.sofas.ui.three.SofaBuilderPMMD;
import pe.erp.sofas.ui.three.Viewer3DPMMD;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.*;







@Component
@Lazy
@Scope("prototype")
@RequiredArgsConstructor
public class SalaControllerPMMD implements VistaPMMD.ConCiclo {

    private enum Tipo { 
        SOFA, 
         ALFOMBRA, 
          PLANTA }



    private static final class Mueble {
        
        final String nombre;
        
        final Tipo tipo;
        
        final ProductoPMMD producto;
        
        final ConfigSofaPMMD config;
        
        final double ancho, fondo, precio;
        
        double x, y;
        
        int rot;

        Mueble(String nombre, Tipo tipo, ProductoPMMD producto, ConfigSofaPMMD config, double ancho, double fondo, double precio, double x, double y, int rot) {
            this.nombre = nombre;
            this.tipo = tipo;
            this.producto = producto;
            this.config = config;
            this.ancho = ancho;
            this.fondo = fondo;
            this.precio = precio;
            this.x = x;
            this.y = y;
            this.rot = rot;
        }

        double w() {
            return rot % 180 == 0 ? ancho : fondo;
        }

        double h() {
            return rot % 180 == 0 ? fondo : ancho;
        }

        double x0() {
            return x - w() / 2;
        }

        double y0() {
            return y - h() / 2;
        }
    }

    private final ProductoServicePMMD productosPMMD;

    private final ConfiguradorServicePMMD configuradorPMMD;

    private final TelaServicePMMD telasPMMD;

    private final ClienteServicePMMD clientesPMMD;

    private final ExportServicePMMD exportPMMD;

    private final OverlaysPMMD overlaysPMMD;

    private final ToastPMMD toastPMMD;

    private final NavigatorPMMD navigatorPMMD;

    private final AppWindowPMMD appWindowPMMD;

    private final ThemeManagerPMMD temaPMMD;

    @FXML private VBox raiz;

    private final List<Mueble> mueblesPMMD = new ArrayList<>();

    private Mueble seleccionPMMD;

    private double anchoSalaPMMD = 420, largoSalaPMMD = 360, altoSalaPMMD = 250;

    private int paredPuertaPMMD = 0;

    private double posPuertaPMMD = 40, anchoPuertaPMMD = 90;

    private int paredVentanaPMMD = 2;

    private double posVentanaPMMD = 130, anchoVentanaPMMD = 160;

    private final Canvas canvasPMMD = new Canvas(700, 520);

    private final StackPane areaPlanoPMMD = new StackPane();

    private final Viewer3DPMMD visor3dPMMD = new Viewer3DPMMD(false);

    private SegmentedControlPMMD vistaPMMD;

    private final VBox resumenPMMD = new VBox(10);

    private final VBox propiedadesPMMD = new VBox(10);

    private double escalaPMMD = 1, oxPMMD = 0, oyPMMD = 0;

    private double dragDxPMMD, dragDyPMMD;

    private boolean iniciadoPMMD;

    private String proyectoPMMD = "Sala · Rosa Quispe · sofá + sillón";

    @FXML
    public void initialize() {
        vistaPMMD = SegmentedControlPMMD.conIconos(new String[]{"Planta 2D", "Vista 3D"}, new String[]{"GRID_ON", "MDOAL_3D_ROTATION"});
        vistaPMMD.valorProperty().addListener((oPMMD, aPMMD, bPMMD) -> mostrarVistaPMMD());
        Button rotar = UIPMMD.secondary("Rotar 90°", "ROTATE_RIGHT");
        rotar.setOnAction(ePMMD -> rotar());
        Button eliminar = UIPMMD.button("Eliminar", "DELETE_OUTLINE", "btn-danger-soft");
        eliminar.setOnAction(ePMMD -> eliminar());
        Button configurarPMMD = UIPMMD.secondary("Volver al configurador", "MDOAL_3D_ROTATION");
        configurarPMMD.setOnAction(ePMMD -> navigatorPMMD.goTo("configurador"));
        HBox barra = UIPMMD.hbox(10, UIPMMD.label(proyectoPMMD, "card-title"), UIPMMD.spacer(), vistaPMMD, rotar, eliminar, configurarPMMD);

        areaPlanoPMMD.getStyleClass().add("planner-host");
        areaPlanoPMMD.getChildren().add(canvasPMMD);
        areaPlanoPMMD.setMinSize(300, 300);
        areaPlanoPMMD.widthProperty().addListener((oPMMD, aPMMD, bPMMD) -> dibujarPMMD());
        areaPlanoPMMD.heightProperty().addListener((oPMMD, aPMMD, bPMMD) -> dibujarPMMD());
        canvasPMMD.widthProperty().bind(areaPlanoPMMD.widthProperty());
        canvasPMMD.heightProperty().bind(areaPlanoPMMD.heightProperty());
        canvasPMMD.setFocusTraversable(true);
        canvasPMMD.setOnMousePressed(ePMMD -> {
            canvasPMMD.requestFocus();
            double cxPMMD = (ePMMD.getX() - oxPMMD) / escalaPMMD, cyPMMD = (ePMMD.getY() - oyPMMD) / escalaPMMD;
            seleccionPMMD = null;
            for (int iPMMD = mueblesPMMD.size() - 1; iPMMD >= 0; iPMMD--) {
                Mueble mPMMD = mueblesPMMD.get(iPMMD);
                if (cxPMMD >= mPMMD.x0() && cxPMMD <= mPMMD.x0() + mPMMD.w() && cyPMMD >= mPMMD.y0() && cyPMMD <= mPMMD.y0() + mPMMD.h()) {
                    seleccionPMMD = mPMMD;
                    dragDxPMMD = cxPMMD - mPMMD.x;
                    dragDyPMMD = cyPMMD - mPMMD.y;
                    break;
                }
            }
            refrescarPMMD();
        });
        canvasPMMD.setOnMouseDragged(ePMMD -> {
            if (seleccionPMMD == null) return;
            double cxPMMD = (ePMMD.getX() - oxPMMD) / escalaPMMD, cyPMMD = (ePMMD.getY() - oyPMMD) / escalaPMMD;
            seleccionPMMD.x = Math.round((cxPMMD - dragDxPMMD) / 5) * 5;
            seleccionPMMD.y = Math.round((cyPMMD - dragDyPMMD) / 5) * 5;
            dibujarPMMD();
        });
        canvasPMMD.setOnMouseReleased(ePMMD -> refrescarPMMD());
        canvasPMMD.setOnKeyPressed(ePMMD -> {
            if (ePMMD.getCode() == KeyCode.R) rotar();
            else if (ePMMD.getCode() == KeyCode.DELETE || ePMMD.getCode() == KeyCode.BACK_SPACE) eliminar();
        });
        visor3dPMMD.setVisible(false);
        StackPane centro = new StackPane(areaPlanoPMMD, visor3dPMMD);
        VBox.setVgrow(centro, Priority.ALWAYS);
        VBox izq = new VBox(10, barra, centro);
        HBox.setHgrow(izq, Priority.ALWAYS);

        VBox derPMMD = new VBox(14, salaCardPMMD(), catalogoPMMD(), propiedadesPMMD, resumenPMMD);
        ScrollPane spDer = UIPMMD.scroll(derPMMD);
        spDer.setPrefWidth(320);
        spDer.setMinWidth(300);
        spDer.setMaxWidth(320);
        HBox cuerpoPMMD = new HBox(14, izq, spDer);
        VBox.setVgrow(cuerpoPMMD, Priority.ALWAYS);
        raiz.getChildren().setAll(cuerpoPMMD);
    }

    @Override
    public void onShow(IntentPMMD intentPMMD) {
        if (!iniciadoPMMD) {
            iniciadoPMMD = true;
            productosPMMD.porCodigo("SF-001").ifPresent(p -> mueblesPMMD.add(new Mueble("Sofá Moderno 3 cuerpos (220 cm)", Tipo.SOFA, p, null, 220, 92, 2690, 210, 60, 0)));
            productosPMMD.porCodigo("SF-003").ifPresent(p -> mueblesPMMD.add(new Mueble(p.getNombre(), Tipo.SOFA, p, null, 95, 90, p.getPrecio().doubleValue(), 360, 185, 90)));
            mueblesPMMD.add(new Mueble("Alfombra 200 × 140", Tipo.ALFOMBRA, null, null, 200, 140, 0, 210, 190, 0));
            mueblesPMMD.add(new Mueble("Planta", Tipo.PLANTA, null, null, 40, 40, 0, 40, 40, 0));
        }
        if (intentPMMD.es("AGREGAR") && intentPMMD.dato() instanceof ConfigSofaPMMD c) {
            var cotPMMD = configuradorPMMD.cotizar(c);
            double fondo = c.conChaise() ? c.profundidad() + 65 : c.profundidad();
            Mueble mPMMD = new Mueble(c.modelo().nombre() + " a medida", Tipo.SOFA, null, c, c.largo(), fondo, cotPMMD.precio(), anchoSalaPMMD / 2, largoSalaPMMD / 2, 0);
            mueblesPMMD.add(mPMMD);
            seleccionPMMD = mPMMD;
            toastPMMD.info("Se agregó el sofá configurado (" + MoneyPMMD.fmt0(cotPMMD.precio()) + "). Arrástralo a su lugar.");
        }
        refrescarPMMD();
        if (intentPMMD.es("VISTA3D")) vistaPMMD.setValor("Vista 3D");
    }

    @Override
    public void onHide() {
        visor3dPMMD.detenerGiro();
    }

    private void mostrarVistaPMMD() {
        boolean tresPMMD = "Vista 3D".equals(vistaPMMD.getValor());
        areaPlanoPMMD.setVisible(!tresPMMD);
        visor3dPMMD.setVisible(tresPMMD);
        if (tresPMMD) construir3DPMMD(true);
    }



    private Node salaCardPMMD() {
        TextField ancho = FormsPMMD.entero((int) anchoSalaPMMD), largoPMMD = FormsPMMD.entero((int) largoSalaPMMD), altoPMMD = FormsPMMD.entero((int) altoSalaPMMD);
        for (TextField tPMMD : List.of(ancho, largoPMMD, altoPMMD)) tPMMD.textProperty().addListener((oPMMD, aPMMD, bPMMD) -> {
            int vPMMD = FormsPMMD.entero(tPMMD);
            if (vPMMD < 150) return;
            anchoSalaPMMD = FormsPMMD.entero(ancho) >= 150 ? FormsPMMD.entero(ancho) : anchoSalaPMMD;
            largoSalaPMMD = FormsPMMD.entero(largoPMMD) >= 150 ? FormsPMMD.entero(largoPMMD) : largoSalaPMMD;
            altoSalaPMMD = FormsPMMD.entero(altoPMMD) >= 200 ? FormsPMMD.entero(altoPMMD) : altoSalaPMMD;
            refrescarPMMD();
        });
        List<String> paredesPMMD = List.of("Frente (abajo)", "Derecha", "Fondo (arriba)", "Izquierda");
        ComboBox<String> pPuertaPMMD = FormsPMMD.combo(paredesPMMD, paredesPMMD.get(paredPuertaPMMD), sPMMD -> sPMMD);
        TextField posPPMMD = FormsPMMD.entero((int) posPuertaPMMD);
        ComboBox<String> pVentanaPMMD = FormsPMMD.combo(paredesPMMD, paredesPMMD.get(paredVentanaPMMD), sPMMD -> sPMMD);
        TextField posVPMMD = FormsPMMD.entero((int) posVentanaPMMD);
        pPuertaPMMD.valueProperty().addListener((oPMMD, aPMMD, bPMMD) -> {
            paredPuertaPMMD = paredesPMMD.indexOf(bPMMD);
            refrescarPMMD();
        });
        pVentanaPMMD.valueProperty().addListener((oPMMD, aPMMD, bPMMD) -> {
            paredVentanaPMMD = paredesPMMD.indexOf(bPMMD);
            refrescarPMMD();
        });
        posPPMMD.textProperty().addListener((oPMMD, aPMMD, bPMMD) -> {
            posPuertaPMMD = FormsPMMD.entero(posPPMMD);
            refrescarPMMD();
        });
        posVPMMD.textProperty().addListener((oPMMD, aPMMD, bPMMD) -> {
            posVentanaPMMD = FormsPMMD.entero(posVPMMD);
            refrescarPMMD();
        });
        GridPane gPMMD = UIPMMD.grid(3);
        gPMMD.add(UIPMMD.field("Ancho (cm)", ancho), 0, 0);
        gPMMD.add(UIPMMD.field("Largo (cm)", largoPMMD), 1, 0);
        gPMMD.add(UIPMMD.field("Alto (cm)", altoPMMD), 2, 0);
        gPMMD.add(UIPMMD.field("Puerta en", pPuertaPMMD), 0, 1, 2, 1);
        gPMMD.add(UIPMMD.field("Desde (cm)", posPPMMD), 2, 1);
        gPMMD.add(UIPMMD.field("Ventana en", pVentanaPMMD), 0, 2, 2, 1);
        gPMMD.add(UIPMMD.field("Desde (cm)", posVPMMD), 2, 2);
        return UIPMMD.card(UIPMMD.label("Medidas de la sala", "card-title"), gPMMD);
    }

    private Node catalogoPMMD() {
        VBox listaPMMD = new VBox(2);
        for (ProductoPMMD p : productosPMMD.listar()) {
            if (p.getForma() == FormaPMMD.JUEGO) continue;
            double fondo = p.getFondo() == null ? 90 : p.getFondo();
            listaPMMD.getChildren().add(item("WEEKEND", p.getNombre(), p.getLargo() + " × " + Math.round(fondo) + " cm · " + MoneyPMMD.fmt0(p.getPrecio()),
                    () -> agregar(new Mueble(p.getNombre(), Tipo.SOFA, p, null, p.getLargo(), fondo, p.getPrecio().doubleValue(), anchoSalaPMMD / 2, largoSalaPMMD / 2, 0))));
        }
        listaPMMD.getChildren().add(item("CROP_LANDSCAPE", "Alfombra", "200 × 140 cm · decoración",
                () -> agregar(new Mueble("Alfombra 200 × 140", Tipo.ALFOMBRA, null, null, 200, 140, 0, anchoSalaPMMD / 2, largoSalaPMMD / 2, 0))));
        listaPMMD.getChildren().add(item("LOCAL_FLORIST", "Planta", "40 × 40 cm · decoración",
                () -> agregar(new Mueble("Planta", Tipo.PLANTA, null, null, 40, 40, 0, 40, largoSalaPMMD - 40, 0))));
        return UIPMMD.card(UIPMMD.cardHeader("Catálogo", "Clic para agregar a la sala"), listaPMMD);
    }

    private Node item(String iconoPMMD, String tituloPMMD, String subPMMD, Runnable rPMMD) {
        StackPane ic = new StackPane(IcoPMMD.of(iconoPMMD, 16));
        ic.getStyleClass().add("popup-item-icon");
        HBox h = new HBox(10, ic, new VBox(1, UIPMMD.label(tituloPMMD, "cell-title"), UIPMMD.label(subPMMD, "cell-sub")), UIPMMD.spacer(), IcoPMMD.of("ADD", 16));
        h.getStyleClass().add("catalog-item");
        h.setOnMouseClicked(ePMMD -> rPMMD.run());
        return h;
    }

    private void agregar(Mueble mPMMD) {
        mueblesPMMD.add(mPMMD);
        seleccionPMMD = mPMMD;
        refrescarPMMD();
    }

    private void rotar() {
        if (seleccionPMMD == null) {
            toastPMMD.info("Selecciona un mueble en la planta.");
            return;
        }
        seleccionPMMD.rot = (seleccionPMMD.rot + 90) % 360;
        refrescarPMMD();
    }

    private void eliminar() {
        if (seleccionPMMD == null) return;
        mueblesPMMD.remove(seleccionPMMD);
        toastPMMD.info(seleccionPMMD.nombre + " quitado de la sala.");
        seleccionPMMD = null;
        refrescarPMMD();
    }

    private void refrescarPMMD() {
        dibujarPMMD();
        renderPropiedadesPMMD();
        renderResumenPMMD();
        if (visor3dPMMD.isVisible()) construir3DPMMD(false);
    }

    private void renderPropiedadesPMMD() {
        if (seleccionPMMD == null) {
            propiedadesPMMD.getChildren().setAll(UIPMMD.card(UIPMMD.label("Selección", "card-title"), UIPMMD.wrap("Haz clic en un mueble para moverlo, rotarlo (R) o eliminarlo (Supr).", "muted")));
            return;
        }
        Mueble mPMMD = seleccionPMMD;
        List<String> problemas = problemas(mPMMD);
        VBox c = UIPMMD.card(UIPMMD.label(mPMMD.nombre, "card-title"),
                UIPMMD.label(Math.round(mPMMD.w()) + " × " + Math.round(mPMMD.h()) + " cm en planta · rotación " + mPMMD.rot + "°", "cell-sub"),
                UIPMMD.label("Posición: " + Math.round(mPMMD.x0()) + " cm desde la izquierda, " + Math.round(mPMMD.y0()) + " cm desde el fondo", "cell-sub"));
        if (problemas.isEmpty()) c.getChildren().add(UIPMMD.banner("ok-banner", "CHECK_CIRCLE", "Ubicación válida."));
        for (String p : problemas) c.getChildren().add(UIPMMD.banner(p.startsWith("Fuera") ? "danger-banner" : "lock-banner", "WARNING", p));
        propiedadesPMMD.getChildren().setAll(c);
    }

    private void renderResumenPMMD() {
        double areaPMMD = anchoSalaPMMD * largoSalaPMMD;
        double ocupadoPMMD = mueblesPMMD.stream().filter(mPMMD -> mPMMD.tipo != Tipo.ALFOMBRA).mapToDouble(mPMMD -> mPMMD.ancho * mPMMD.fondo).sum();
        double librePMMD = Math.max(0, areaPMMD - ocupadoPMMD);
        double totalPMMD = mueblesPMMD.stream().mapToDouble(mPMMD -> mPMMD.precio).sum();
        long conProblemasPMMD = mueblesPMMD.stream().filter(mPMMD -> !problemas(mPMMD).isEmpty()).count();
        Button cotizarPMMD = UIPMMD.primary("Crear cotización", "REQUEST_QUOTE");
        cotizarPMMD.setMaxWidth(Double.MAX_VALUE);
        cotizarPMMD.setOnAction(ePMMD -> crearCotizacionPMMD());
        VBox c = UIPMMD.card(UIPMMD.label("Resumen", "card-title"),
                UIPMMD.hbox(8, UIPMMD.miniStat("Ocupación del piso", String.format("%.0f %%", ocupadoPMMD * 100 / areaPMMD)), UIPMMD.miniStat("Libre para circular", String.format("%.1f m²", librePMMD / 10000))),
                UIPMMD.hbox(8, UIPMMD.label("Total de la propuesta", "muted"), UIPMMD.spacer(), UIPMMD.label(MoneyPMMD.fmt0(totalPMMD), "price-value")),
                conProblemasPMMD == 0 ? UIPMMD.banner("ok-banner", "CHECK_CIRCLE", "Todos los muebles caben y no bloquean la puerta.")
                        : UIPMMD.banner("lock-banner", "WARNING", conProblemasPMMD + (conProblemasPMMD == 1 ? " mueble con observaciones." : " muebles con observaciones.")),
                cotizarPMMD);
        resumenPMMD.getChildren().setAll(c);
    }



    private double[] barridoPuertaPMMD() {
        double aPMMD = anchoPuertaPMMD;
        return switch (paredPuertaPMMD) {
            case 0 -> new double[]{posPuertaPMMD, largoSalaPMMD - aPMMD, aPMMD, aPMMD};
            case 2 -> new double[]{posPuertaPMMD, 0, aPMMD, aPMMD};
            case 1 -> new double[]{anchoSalaPMMD - aPMMD, posPuertaPMMD, aPMMD, aPMMD};
            default -> new double[]{0, posPuertaPMMD, aPMMD, aPMMD};
        };
    }

    private static boolean cruzaPMMD(double axPMMD, double ayPMMD, double awPMMD, double ahPMMD, double bxPMMD, double byPMMD, double bwPMMD, double bhPMMD) {
        return axPMMD < bxPMMD + bwPMMD - 0.5 && bxPMMD < axPMMD + awPMMD - 0.5 && ayPMMD < byPMMD + bhPMMD - 0.5 && byPMMD < ayPMMD + ahPMMD - 0.5;
    }

    private List<String> problemas(Mueble mPMMD) {
        List<String> lPMMD = new ArrayList<>();
        if (mPMMD.x0() < -0.5 || mPMMD.y0() < -0.5 || mPMMD.x0() + mPMMD.w() > anchoSalaPMMD + 0.5 || mPMMD.y0() + mPMMD.h() > largoSalaPMMD + 0.5) lPMMD.add("Fuera de la sala.");
        if (mPMMD.tipo != Tipo.ALFOMBRA) {
            for (Mueble oPMMD : mueblesPMMD) {
                if (oPMMD == mPMMD || oPMMD.tipo == Tipo.ALFOMBRA) continue;
                if (cruzaPMMD(mPMMD.x0(), mPMMD.y0(), mPMMD.w(), mPMMD.h(), oPMMD.x0(), oPMMD.y0(), oPMMD.w(), oPMMD.h())) {
                    lPMMD.add("Choca con " + oPMMD.nombre + ".");
                    break;
                }
            }
            double[] bPMMD = barridoPuertaPMMD();
            if (cruzaPMMD(mPMMD.x0(), mPMMD.y0(), mPMMD.w(), mPMMD.h(), bPMMD[0], bPMMD[1], bPMMD[2], bPMMD[3])) lPMMD.add("Bloquea el barrido de la puerta.");
        }
        return lPMMD;
    }



    private void dibujarPMMD() {
        double WPMMD = canvasPMMD.getWidth(), HPMMD = canvasPMMD.getHeight();
        if (WPMMD <= 0 || HPMMD <= 0) return;
        boolean darkPMMD = temaPMMD.isOscuro();
        GraphicsContext gPMMD = canvasPMMD.getGraphicsContext2D();
        gPMMD.clearRect(0, 0, WPMMD, HPMMD);
        double margenPMMD = 46;
        escalaPMMD = Math.min((WPMMD - 2 * margenPMMD) / anchoSalaPMMD, (HPMMD - 2 * margenPMMD) / largoSalaPMMD);
        oxPMMD = (WPMMD - anchoSalaPMMD * escalaPMMD) / 2;
        oyPMMD = (HPMMD - largoSalaPMMD * escalaPMMD) / 2;

        gPMMD.setFill(Color.web(darkPMMD ? "#1E2A44" : "#FBF7F1"));
        gPMMD.fillRect(oxPMMD, oyPMMD, anchoSalaPMMD * escalaPMMD, largoSalaPMMD * escalaPMMD);
        gPMMD.setStroke(Color.web(darkPMMD ? "#24324D" : "#E7DFD3"));
        gPMMD.setLineWidth(1);
        for (double x = 50; x < anchoSalaPMMD; x += 50) gPMMD.strokeLine(oxPMMD + x * escalaPMMD, oyPMMD, oxPMMD + x * escalaPMMD, oyPMMD + largoSalaPMMD * escalaPMMD);
        for (double y = 50; y < largoSalaPMMD; y += 50) gPMMD.strokeLine(oxPMMD, oyPMMD + y * escalaPMMD, oxPMMD + anchoSalaPMMD * escalaPMMD, oyPMMD + y * escalaPMMD);

        gPMMD.setStroke(Color.web(darkPMMD ? "#CBD5E1" : "#14213D"));
        gPMMD.setLineWidth(6);
        gPMMD.strokeRect(oxPMMD, oyPMMD, anchoSalaPMMD * escalaPMMD, largoSalaPMMD * escalaPMMD);

        double[] vPMMD = tramoPMMD(paredVentanaPMMD, posVentanaPMMD, anchoVentanaPMMD);
        gPMMD.setStroke(Color.web("#93C5FD"));
        gPMMD.setLineWidth(6);
        gPMMD.strokeLine(vPMMD[0], vPMMD[1], vPMMD[2], vPMMD[3]);
        gPMMD.setStroke(Color.WHITE);
        gPMMD.setLineWidth(1.5);
        gPMMD.strokeLine(vPMMD[0], vPMMD[1], vPMMD[2], vPMMD[3]);

        double[] p = tramoPMMD(paredPuertaPMMD, posPuertaPMMD, anchoPuertaPMMD);
        gPMMD.setStroke(Color.web(darkPMMD ? "#1E2A44" : "#FBF7F1"));
        gPMMD.setLineWidth(7);
        gPMMD.strokeLine(p[0], p[1], p[2], p[3]);
        double[] bPMMD = barridoPuertaPMMD();
        gPMMD.setStroke(Color.web("#A47148"));
        gPMMD.setLineWidth(1.5);
        gPMMD.setLineDashes(5, 4);
        double rPMMD = anchoPuertaPMMD * escalaPMMD;
        double bxPMMD = oxPMMD + bPMMD[0] * escalaPMMD, byPMMD = oyPMMD + bPMMD[1] * escalaPMMD;
        switch (paredPuertaPMMD) {
            case 0 -> gPMMD.strokeArc(bxPMMD - rPMMD, byPMMD, 2 * rPMMD, 2 * rPMMD, 0, 90, ArcType.OPEN);
            case 2 -> gPMMD.strokeArc(bxPMMD - rPMMD, byPMMD - rPMMD, 2 * rPMMD, 2 * rPMMD, 270, 90, ArcType.OPEN);
            case 1 -> gPMMD.strokeArc(bxPMMD, byPMMD - rPMMD, 2 * rPMMD, 2 * rPMMD, 90, 90, ArcType.OPEN);
            default -> gPMMD.strokeArc(bxPMMD - rPMMD, byPMMD - rPMMD, 2 * rPMMD, 2 * rPMMD, 270, 90, ArcType.OPEN);
        }
        gPMMD.setLineDashes();

        List<Mueble> ordenPMMD = new ArrayList<>(mueblesPMMD);
        ordenPMMD.sort(Comparator.comparing(mPMMD -> mPMMD.tipo != Tipo.ALFOMBRA));
        for (Mueble mPMMD : ordenPMMD) {
            List<String> prPMMD = problemas(mPMMD);
            boolean fueraPMMD = prPMMD.stream().anyMatch(sPMMD -> sPMMD.startsWith("Fuera"));
            Color rellenoPMMD = switch (mPMMD.tipo) {
                case ALFOMBRA -> Color.web("#CBB89B", 0.45);
                case PLANTA -> Color.web("#3F7D4E", 0.75);
                default -> Color.web(mPMMD.config != null ? "#2563EB" : "#A47148", 0.82);
            };
            if (fueraPMMD) rellenoPMMD = Color.web("#DC2626", 0.75);
            else if (!prPMMD.isEmpty()) rellenoPMMD = Color.web("#F59E0B", 0.8);
            double x = oxPMMD + mPMMD.x0() * escalaPMMD, y = oyPMMD + mPMMD.y0() * escalaPMMD, w = mPMMD.w() * escalaPMMD, h = mPMMD.h() * escalaPMMD;
            gPMMD.setFill(rellenoPMMD);
            if (mPMMD.tipo == Tipo.PLANTA) gPMMD.fillOval(x, y, w, h);
            else gPMMD.fillRoundRect(x, y, w, h, 8, 8);
            if (mPMMD.tipo == Tipo.SOFA) {

                gPMMD.setFill(Color.rgb(0, 0, 0, 0.18));
                double tPMMD = 18 * escalaPMMD;
                switch (mPMMD.rot) {
                    case 0 -> gPMMD.fillRect(x, y, w, tPMMD);
                    case 90 -> gPMMD.fillRect(x + w - tPMMD, y, tPMMD, h);
                    case 180 -> gPMMD.fillRect(x, y + h - tPMMD, w, tPMMD);
                    default -> gPMMD.fillRect(x, y, tPMMD, h);
                }
            }
            gPMMD.setStroke(mPMMD == seleccionPMMD ? Color.web("#2563EB") : Color.web(darkPMMD ? "#E5E7EB" : "#FFFFFF"));
            gPMMD.setLineWidth(mPMMD == seleccionPMMD ? 2.5 : 1);
            if (mPMMD.tipo == Tipo.PLANTA) gPMMD.strokeOval(x, y, w, h);
            else gPMMD.strokeRoundRect(x, y, w, h, 8, 8);
            if (w > 40 && h > 18) {
                gPMMD.setFill(mPMMD.tipo == Tipo.ALFOMBRA ? Color.web("#7A4E2B") : Color.WHITE);
                gPMMD.setFont(Font.font("Inter", FontWeight.BOLD, 11));
                gPMMD.setTextAlign(TextAlignment.CENTER);
                gPMMD.setTextBaseline(VPos.CENTER);
                gPMMD.fillText(recortarPMMD(mPMMD.nombre, w), x + w / 2, y + h / 2, w - 6);
            }
        }
        if (seleccionPMMD != null) cotasPMMD(gPMMD, seleccionPMMD, darkPMMD);

        gPMMD.setFill(Color.web(darkPMMD ? "#94A3B8" : "#64748B"));
        gPMMD.setFont(Font.font("Inter", 11));
        gPMMD.setTextAlign(TextAlignment.LEFT);
        gPMMD.setTextBaseline(VPos.TOP);
        gPMMD.fillText("Sala " + Math.round(anchoSalaPMMD) + " × " + Math.round(largoSalaPMMD) + " cm · cuadrícula de 50 cm", oxPMMD, oyPMMD + largoSalaPMMD * escalaPMMD + 10);
        gPMMD.setTextAlign(TextAlignment.RIGHT);
        gPMMD.fillText("Fondo ↑", oxPMMD + anchoSalaPMMD * escalaPMMD, oyPMMD - 18);
    }

    private static String recortarPMMD(String sPMMD, double w) {
        int maxPMMD = (int) (w / 6.5);
        return sPMMD.length() <= maxPMMD ? sPMMD : sPMMD.substring(0, Math.max(3, maxPMMD - 1)) + "…";
    }



    private double[] tramoPMMD(int paredPMMD, double posPMMD, double ancho) {
        return switch (paredPMMD) {
            case 0 -> new double[]{oxPMMD + posPMMD * escalaPMMD, oyPMMD + largoSalaPMMD * escalaPMMD, oxPMMD + (posPMMD + ancho) * escalaPMMD, oyPMMD + largoSalaPMMD * escalaPMMD};
            case 2 -> new double[]{oxPMMD + posPMMD * escalaPMMD, oyPMMD, oxPMMD + (posPMMD + ancho) * escalaPMMD, oyPMMD};
            case 1 -> new double[]{oxPMMD + anchoSalaPMMD * escalaPMMD, oyPMMD + posPMMD * escalaPMMD, oxPMMD + anchoSalaPMMD * escalaPMMD, oyPMMD + (posPMMD + ancho) * escalaPMMD};
            default -> new double[]{oxPMMD, oyPMMD + posPMMD * escalaPMMD, oxPMMD, oyPMMD + (posPMMD + ancho) * escalaPMMD};
        };
    }

    private void cotasPMMD(GraphicsContext gPMMD, Mueble mPMMD, boolean darkPMMD) {
        Color c = Color.web("#2563EB");
        gPMMD.setStroke(c);
        gPMMD.setFill(c);
        gPMMD.setLineWidth(1);
        gPMMD.setFont(Font.font("Inter", FontWeight.BOLD, 10.5));
        double x = oxPMMD + mPMMD.x0() * escalaPMMD, y = oyPMMD + mPMMD.y0() * escalaPMMD, w = mPMMD.w() * escalaPMMD, h = mPMMD.h() * escalaPMMD;

        gPMMD.strokeLine(x, y - 12, x + w, y - 12);
        gPMMD.setTextAlign(TextAlignment.CENTER);
        gPMMD.setTextBaseline(VPos.BOTTOM);
        gPMMD.fillText(Math.round(mPMMD.w()) + " cm", x + w / 2, y - 14);
        gPMMD.strokeLine(x - 12, y, x - 12, y + h);
        gPMMD.setTextBaseline(VPos.CENTER);
        gPMMD.setTextAlign(TextAlignment.RIGHT);
        gPMMD.fillText(Math.round(mPMMD.h()) + " cm", x - 16, y + h / 2);

        gPMMD.setLineDashes(3, 3);
        gPMMD.setTextAlign(TextAlignment.CENTER);
        double izq = mPMMD.x0(), derPMMD = anchoSalaPMMD - mPMMD.x0() - mPMMD.w(), arrPMMD = mPMMD.y0(), abaPMMD = largoSalaPMMD - mPMMD.y0() - mPMMD.h();
        if (izq > 5) {
            gPMMD.strokeLine(oxPMMD, y + h / 2, x, y + h / 2);
            gPMMD.fillText(Math.round(izq) + "", (oxPMMD + x) / 2, y + h / 2 - 8);
        }
        if (derPMMD > 5) {
            gPMMD.strokeLine(x + w, y + h / 2, oxPMMD + anchoSalaPMMD * escalaPMMD, y + h / 2);
            gPMMD.fillText(Math.round(derPMMD) + "", (x + w + oxPMMD + anchoSalaPMMD * escalaPMMD) / 2, y + h / 2 - 8);
        }
        if (arrPMMD > 5) {
            gPMMD.strokeLine(x + w / 2, oyPMMD, x + w / 2, y);
            gPMMD.fillText(Math.round(arrPMMD) + "", x + w / 2 + 14, (oyPMMD + y) / 2);
        }
        if (abaPMMD > 5) {
            gPMMD.strokeLine(x + w / 2, y + h, x + w / 2, oyPMMD + largoSalaPMMD * escalaPMMD);
            gPMMD.fillText(Math.round(abaPMMD) + "", x + w / 2 + 14, (y + h + oyPMMD + largoSalaPMMD * escalaPMMD) / 2);
        }
        gPMMD.setLineDashes();
    }



    private Node nodo3DPMMD(Mueble mPMMD) {
        SofaBuilderPMMD bPMMD = new SofaBuilderPMMD();
        return switch (mPMMD.tipo) {
            case ALFOMBRA -> RoomBuilderPMMD.alfombra(mPMMD.ancho, mPMMD.fondo);
            case PLANTA -> RoomBuilderPMMD.planta();
            default -> {
                if (mPMMD.config != null) {
                    TelaPMMD tPMMD = mPMMD.config.telaId() == null ? null : telasPMMD.tela(mPMMD.config.telaId());
                    TelaPMMD tcPMMD = mPMMD.config.telaCojinesId() == null ? tPMMD : telasPMMD.tela(mPMMD.config.telaCojinesId());
                    yield bPMMD.build(SofaBuilderPMMD.Opciones.de(mPMMD.config, tPMMD, tcPMMD));
                }
                SofaBuilderPMMD.Opciones oPMMD = SofaBuilderPMMD.Opciones.de(mPMMD.producto);
                if (mPMMD.producto.getCodigo().equals("SF-001")) {
                    oPMMD = new SofaBuilderPMMD.Opciones(oPMMD.forma(), oPMMD.cuerpos(), mPMMD.ancho, oPMMD.fondo(), oPMMD.alto(), oPMMD.chaise(), oPMMD.brazo(), oPMMD.patas(), oPMMD.colorPatas(),
                            "#B9B7B1", oPMMD.tipoTela(), 2, "#C9972E", oPMMD.tipoCojines());
                }
                yield bPMMD.build(oPMMD);
            }
        };
    }

    private void construir3DPMMD(boolean recentrarPMMD) {
        List<RoomBuilderPMMD.Ubicado> ubicadosPMMD = new ArrayList<>();
        for (Mueble mPMMD : mueblesPMMD) {

            ubicadosPMMD.add(new RoomBuilderPMMD.Ubicado(nodo3DPMMD(mPMMD), mPMMD.x, largoSalaPMMD - mPMMD.y, mPMMD.rot));
        }
        List<RoomBuilderPMMD.Abertura> abPMMD = List.of(new RoomBuilderPMMD.Abertura(paredPuertaPMMD, posPuertaPMMD, anchoPuertaPMMD, true),
                new RoomBuilderPMMD.Abertura(paredVentanaPMMD, paredVentanaPMMD == 1 || paredVentanaPMMD == 3 ? largoSalaPMMD - posVentanaPMMD - anchoVentanaPMMD : posVentanaPMMD, anchoVentanaPMMD, false));
        visor3dPMMD.setContenido(RoomBuilderPMMD.build(anchoSalaPMMD, largoSalaPMMD, altoSalaPMMD, abPMMD, ubicadosPMMD), recentrarPMMD);
        if (recentrarPMMD) visor3dPMMD.fijarVista(-25, 38);
    }



    private byte[] png(Node nPMMD, javafx.scene.image.Image img) {
        try (ByteArrayOutputStream outPMMD = new ByteArrayOutputStream()) {
            javafx.scene.image.WritableImage w;
            if (img instanceof javafx.scene.image.WritableImage wi) w = wi;
            else {
                SnapshotParameters spPMMD = new SnapshotParameters();
                spPMMD.setFill(Color.WHITE);
                w = nPMMD.snapshot(spPMMD, null);
            }
            ImageIO.write(SnapshotPMMD.imagen(w), "png", outPMMD);
            return outPMMD.toByteArray();
        } catch (Exception ePMMD) {
            return null;
        }
    }

    private void crearCotizacionPMMD() {
        List<ClientePMMD> csPMMD = clientesPMMD.listar();
        ComboBox<ClientePMMD> clientePMMD = FormsPMMD.combo(csPMMD, csPMMD.stream().filter(c -> c.getCodigo().equals("C-0101")).findFirst().orElse(null), ClientePMMD::getNombre);
        TextField nombre = UIPMMD.text(proyectoPMMD, "Nombre del proyecto");
        Button cancelarPMMD = UIPMMD.secondary("Cancelar", null);
        Button okPMMD = UIPMMD.primary("Generar PDF", "PICTURE_AS_PDF");
        var h = overlaysPMMD.modal("Crear cotización de la sala", mueblesPMMD.size() + " muebles · " + MoneyPMMD.fmt0(mueblesPMMD.stream().mapToDouble(mPMMD -> mPMMD.precio).sum()),
                new VBox(12, UIPMMD.field("Cliente", clientePMMD), UIPMMD.field("Proyecto", nombre)), 480, cancelarPMMD, okPMMD);
        cancelarPMMD.setOnAction(ePMMD -> h.cerrar());
        okPMMD.setOnAction(ePMMD -> {
            h.cerrar();
            File fPMMD = ArchivosPMMD.guardar(canvasPMMD, "Propuesta-sala", "PDF", "pdf");
            if (fPMMD == null) return;
            Mueble selPMMD = seleccionPMMD;
            seleccionPMMD = null;
            dibujarPMMD();
            byte[] planoPMMD = png(canvasPMMD, null);
            seleccionPMMD = selPMMD;
            dibujarPMMD();
            List<RoomBuilderPMMD.Ubicado> ubicadosPMMD = new ArrayList<>();
            for (Mueble mPMMD : mueblesPMMD) ubicadosPMMD.add(new RoomBuilderPMMD.Ubicado(nodo3DPMMD(mPMMD), mPMMD.x, largoSalaPMMD - mPMMD.y, mPMMD.rot));
            List<RoomBuilderPMMD.Abertura> abPMMD = List.of(new RoomBuilderPMMD.Abertura(paredPuertaPMMD, posPuertaPMMD, anchoPuertaPMMD, true),
                    new RoomBuilderPMMD.Abertura(paredVentanaPMMD, posVentanaPMMD, anchoVentanaPMMD, false));
            byte[] renderPMMD = png(null, Viewer3DPMMD.render(RoomBuilderPMMD.build(anchoSalaPMMD, largoSalaPMMD, altoSalaPMMD, abPMMD, ubicadosPMMD), 900, 560, -25, 38));
            List<ExportServicePMMD.ItemSala> itemsPMMD = mueblesPMMD.stream().map(mPMMD -> new ExportServicePMMD.ItemSala(mPMMD.nombre,
                    Math.round(mPMMD.ancho) + " × " + Math.round(mPMMD.fondo) + " cm" + (mPMMD.config != null ? " · " + configuradorPMMD.especificacion(mPMMD.config) : ""), mPMMD.precio)).toList();
            String cliPMMD = clientePMMD.getValue() == null ? "Por definir" : clientePMMD.getValue().getNombre();
            String medidasPMMD = Math.round(anchoSalaPMMD) + " × " + Math.round(largoSalaPMMD) + " × " + Math.round(altoSalaPMMD) + " cm";
            TareasPMMD.ejecutar(overlaysPMMD, toastPMMD, "Generando propuesta…", () -> {
                exportPMMD.cotizacionSala(nombre.getText(), cliPMMD, medidasPMMD, itemsPMMD, planoPMMD, renderPMMD, fPMMD);
                return fPMMD;
            }, aPMMD -> {
                toastPMMD.exito("Propuesta de sala guardada: " + aPMMD.getName());
                appWindowPMMD.hostServices().showDocument(aPMMD.toURI().toString());
            });
        });
    }
}
