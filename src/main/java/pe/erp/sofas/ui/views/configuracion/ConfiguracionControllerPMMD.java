package pe.erp.sofas.ui.views.configuracion;

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

import java.util.*;



@Component
@Lazy
@Scope("prototype")
@RequiredArgsConstructor
public class ConfiguracionControllerPMMD implements VistaPMMD {

    private static final String[][] SECCIONESPMMD = {{"empresa", "Empresa", "BUSINESS"}, {"usuarios", "Usuarios y roles", "ADMIN_PANEL_SETTINGS"},
            {"produccion", "Producción", "BUILD"}, {"preferencias", "Preferencias", "TUNE"}, {"perfil", "Mi perfil", "ACCOUNT_CIRCLE"}};

    private final ConfiguracionServicePMMD configPMMD;

    private final ParametroServicePMMD parametrosPMMD;

    private final ImagenServicePMMD imagenesPMMD;

    private final ThemeManagerPMMD temaPMMD;

    private final SessionPMMD sessionPMMD;

    private final OverlaysPMMD overlaysPMMD;

    private final ToastPMMD toastPMMD;

    @FXML private VBox raiz;

    private final VBox menuPMMD = new VBox();

    private final VBox contenidoPMMD = new VBox(16);

    private String seccionPMMD = "empresa";

    @FXML
    public void initialize() {
        menuPMMD.getStyleClass().addAll("card", "settings-menu");
        menuPMMD.setPrefWidth(240);
        menuPMMD.setMinWidth(220);
        menuPMMD.setMaxHeight(Region.USE_PREF_SIZE);
        ScrollPane sp = UIPMMD.scroll(contenidoPMMD);
        HBox.setHgrow(sp, Priority.ALWAYS);
        HBox cuerpoPMMD = new HBox(16, menuPMMD, sp);
        VBox.setVgrow(cuerpoPMMD, Priority.ALWAYS);
        raiz.getChildren().setAll(cuerpoPMMD);
    }

    @Override
    public void onShow(IntentPMMD intentPMMD) {
        if (intentPMMD.es(IntentPMMD.ACCION_ABRIR) && intentPMMD.dato() != null) seccionPMMD = intentPMMD.dato().toString();
        renderPMMD();
    }

    private void renderPMMD() {
        menuPMMD.getChildren().clear();
        for (String[] sPMMD : SECCIONESPMMD) {
            HBox itemPMMD = new HBox(10, IcoPMMD.of(sPMMD[2], 18), UIPMMD.label(sPMMD[1]));
            itemPMMD.getStyleClass().add("settings-item");
            itemPMMD.pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("active"), sPMMD[0].equals(seccionPMMD));
            itemPMMD.setOnMouseClicked(ePMMD -> {
                seccionPMMD = sPMMD[0];
                renderPMMD();
            });
            menuPMMD.getChildren().add(itemPMMD);
        }
        contenidoPMMD.getChildren().setAll(switch (seccionPMMD) {
            case "usuarios" -> usuarios();
            case "produccion" -> produccion();
            case "preferencias" -> preferencias();
            case "perfil" -> perfil();
            default -> empresa();
        });
    }



    private Node empresa() {
        EmpresaPMMD ePMMD = configPMMD.empresa();
        ValidacionPMMD vPMMD = new ValidacionPMMD();
        TextField rsPMMD = UIPMMD.text(ePMMD.getRazonSocial(), "Razón social");
        TextField ncPMMD = UIPMMD.text(ePMMD.getNombreComercial(), "Nombre comercial");
        TextField rucPMMD = FormsPMMD.digitos(ePMMD.getRuc(), 11);
        TextField dirPMMD = UIPMMD.text(ePMMD.getDireccion(), "Dirección");
        TextField ciudadPMMD = UIPMMD.text(ePMMD.getCiudad(), "Ciudad");
        TextField telPMMD = UIPMMD.text(ePMMD.getTelefono(), "Teléfono");
        TextField emailPMMD = UIPMMD.text(ePMMD.getEmail(), "Correo");
        TextField webPMMD = UIPMMD.text(ePMMD.getWeb(), "Web");
        ImageSlotPMMD logo = new ImageSlotPMMD("Logo de la empresa", 180, 120, 12);
        logo.mostrar(ePMMD.getLogo());
        String[] rutaLogoPMMD = {ePMMD.getLogo()};
        logo.alElegir(f -> toastPMMD.intentar(() -> {
            rutaLogoPMMD[0] = imagenesPMMD.guardar(f, "logo", 600, 400);
            ImagenesPMMD.olvidar(rutaLogoPMMD[0]);
            logo.mostrar(rutaLogoPMMD[0]);
        }));
        logo.alQuitar(() -> {
            rutaLogoPMMD[0] = null;
            logo.mostrar((String) null);
        });
        FormGridPMMD f = new FormGridPMMD(2);
        f.nodo(vPMMD.campo("Razón social", rsPMMD, () -> ValidacionPMMD.requerido(rsPMMD, "la razón social")), 1);
        f.nodo(vPMMD.campo("Nombre comercial", ncPMMD, null), 1);
        f.nodo(vPMMD.campo("RUC", rucPMMD, () -> rucPMMD.getText().matches("\\d{11}") ? null : "El RUC debe tener 11 dígitos."), 1);
        f.nodo(vPMMD.campo("Teléfono", telPMMD, null), 1);
        f.nodo(vPMMD.campo("Dirección", dirPMMD, null), 1);
        f.nodo(vPMMD.campo("Ciudad", ciudadPMMD, null), 1);
        f.nodo(vPMMD.campo("Correo", emailPMMD, null), 1);
        f.nodo(vPMMD.campo("Web", webPMMD, null), 1);
        Button guardarPMMD = UIPMMD.primary("Guardar", "SAVE");
        vPMMD.enlazar(guardarPMMD);
        guardarPMMD.setOnAction(xPMMD -> {
            ePMMD.setRazonSocial(rsPMMD.getText().trim());
            ePMMD.setNombreComercial(ncPMMD.getText());
            ePMMD.setRuc(rucPMMD.getText());
            ePMMD.setDireccion(dirPMMD.getText());
            ePMMD.setCiudad(ciudadPMMD.getText());
            ePMMD.setTelefono(telPMMD.getText());
            ePMMD.setEmail(emailPMMD.getText());
            ePMMD.setWeb(webPMMD.getText());
            ePMMD.setLogo(rutaLogoPMMD[0]);
            if (toastPMMD.intentar(() -> configPMMD.guardarEmpresa(ePMMD))) toastPMMD.exito("Datos de la empresa guardados. Se usan en cotizaciones, tickets y reportes.");
        });
        HBox hPMMD = new HBox(20, logo, f);
        HBox.setHgrow(f, Priority.ALWAYS);
        return UIPMMD.card(UIPMMD.cardHeader("Empresa", "Aparece en comprobantes de prueba, cotizaciones y reportes", guardarPMMD), hPMMD);
    }



    private Node usuarios() {
        TableView<UsuarioPMMD> t = CellsPMMD.tabla("Sin usuarios.");
        t.getColumns().addAll(List.of(
                CellsPMMD.nodo("Usuario", uPMMD -> UIPMMD.hbox(10, new AvatarInitialsPMMD(uPMMD.getNombre(), 30, "gradient"), new VBox(0, UIPMMD.label(uPMMD.getNombre(), "cell-title"),
                        UIPMMD.label(uPMMD.getUsername() + " · " + uPMMD.getEmail(), "cell-sub"))), 280),
                CellsPMMD.texto("Cargo", UsuarioPMMD::getCargo, 160),
                CellsPMMD.badge("Rol", uPMMD -> uPMMD.getRol().nombre(), 150),
                CellsPMMD.texto("Último acceso", uPMMD -> AppClockPMMD.fmt(uPMMD.getUltimoAcceso()), 110),
                CellsPMMD.badge("Estado", uPMMD -> uPMMD.getActivo() ? "Activo" : "Inactivo", 80)));
        t.getItems().setAll(configPMMD.usuarios());
        t.setFixedCellSize(52);
        t.setPrefHeight(52 * t.getItems().size() + 44);
        t.setRowFactory(tvPMMD -> {
            TableRow<UsuarioPMMD> rPMMD = new TableRow<>();
            rPMMD.setOnMouseClicked(ePMMD -> {
                if (!rPMMD.isEmpty() && ePMMD.getClickCount() == 2) usuario(rPMMD.getItem());
            });
            return rPMMD;
        });
        Button nuevoPMMD = UIPMMD.primary("Nuevo usuario", "PERSON_ADD");
        nuevoPMMD.setOnAction(ePMMD -> usuario(null));
        VBox tablaPMMD = new VBox(t);
        tablaPMMD.getStyleClass().addAll("card", "table-card");


        GridPane m = new GridPane();
        m.setHgap(6);
        m.setVgap(4);
        List<RolPMMD> rolesPMMD = List.of(RolPMMD.values());
        m.add(UIPMMD.label("MÓDULO", "dato-label"), 0, 0);
        for (int cPMMD = 0; cPMMD < rolesPMMD.size(); cPMMD++) {
            Label lPMMD = UIPMMD.wrap(rolesPMMD.get(cPMMD).nombre(), "dato-label");
            lPMMD.setMaxWidth(80);
            lPMMD.setAlignment(Pos.CENTER);
            m.add(lPMMD, cPMMD + 1, 0);
        }
        int filaPMMD = 1;
        for (ModuloPMMD modPMMD : ModuloPMMD.values()) {
            if (!modPMMD.enMenu()) continue;
            m.add(UIPMMD.label(modPMMD.titulo(), "cell-title"), 0, filaPMMD);
            for (int cPMMD = 0; cPMMD < rolesPMMD.size(); cPMMD++) {
                RolPMMD rolPMMD = rolesPMMD.get(cPMMD);
                PermisoPMMD pPMMD = configPMMD.permisos(rolPMMD).stream().filter(xPMMD -> xPMMD.getModulo().equals(modPMMD.clave())).findFirst().orElse(null);
                String inicial = rolPMMD == RolPMMD.GERENTE ? "Editar" : pPMMD == null || !pPMMD.getVer() ? "—" : pPMMD.getEditar() ? "Editar" : "Ver";
                Button nivelPMMD = new Button(inicial);
                nivelPMMD.getStyleClass().addAll("badge", "badge-" + variantePermisoPMMD(inicial), "clickable");
                nivelPMMD.setMinWidth(70);
                nivelPMMD.setDisable(rolPMMD == RolPMMD.GERENTE);
                UIPMMD.tooltip(nivelPMMD, "Clic para cambiar: sin acceso → ver → editar");
                nivelPMMD.setOnAction(ePMMD -> {
                    String bPMMD = switch (nivelPMMD.getText()) {
                        case "—" -> "Ver";
                        case "Ver" -> "Editar";
                        default -> "—";
                    };
                    configPMMD.fijarPermiso(rolPMMD, modPMMD.clave(), !"—".equals(bPMMD), "Editar".equals(bPMMD));
                    nivelPMMD.setText(bPMMD);
                    nivelPMMD.getStyleClass().removeAll("badge-gray", "badge-blue", "badge-green");
                    nivelPMMD.getStyleClass().add("badge-" + variantePermisoPMMD(bPMMD));
                    toastPMMD.exito(rolPMMD.nombre() + " · " + modPMMD.titulo() + ": " + ("—".equals(bPMMD) ? "sin acceso" : bPMMD.toLowerCase()) + ".");
                });
                m.add(nivelPMMD, cPMMD + 1, filaPMMD);
                GridPane.setHalignment(nivelPMMD, javafx.geometry.HPos.CENTER);
            }
            filaPMMD++;
        }
        ScrollPane spmPMMD = new ScrollPane(m);
        spmPMMD.setFitToHeight(true);
        spmPMMD.getStyleClass().add("page-scroll");
        return new VBox(16, UIPMMD.card(UIPMMD.cardHeader("Usuarios", "Doble clic para editar · contraseñas con BCrypt", nuevoPMMD), tablaPMMD),
                UIPMMD.card(UIPMMD.cardHeader("Matriz de permisos por módulo", "El Gerente tiene acceso total"), spmPMMD));
    }

    private static String variantePermisoPMMD(String nPMMD) {
        return switch (nPMMD) {
            case "Editar" -> "green";
            case "Ver" -> "blue";
            default -> "gray";
        };
    }

    private void usuario(UsuarioPMMD existentePMMD) {
        UsuarioPMMD uPMMD = existentePMMD == null ? UsuarioPMMD.builder().activo(true).rol(RolPMMD.VENDEDOR).build() : existentePMMD;
        ValidacionPMMD vPMMD = new ValidacionPMMD();
        TextField userPMMD = UIPMMD.text(uPMMD.getUsername(), "usuario");
        userPMMD.setDisable(existentePMMD != null);
        TextField nombre = UIPMMD.text(uPMMD.getNombre(), "Nombre completo");
        TextField cargoPMMD = UIPMMD.text(uPMMD.getCargo(), "Cargo");
        TextField emailPMMD = UIPMMD.text(uPMMD.getEmail(), "correo@innovasur.pe");
        ComboBox<RolPMMD> rolPMMD = FormsPMMD.combo(List.of(RolPMMD.values()), uPMMD.getRol(), RolPMMD::nombre);
        PasswordField passPMMD = new PasswordField();
        passPMMD.setPromptText(existentePMMD == null ? "Mínimo 6 caracteres" : "Dejar vacío para no cambiar");
        CheckBox activoPMMD = new CheckBox("Usuario activo");
        activoPMMD.setSelected(uPMMD.getActivo());
        FormGridPMMD f = new FormGridPMMD(2);
        f.nodo(vPMMD.campo("Usuario", userPMMD, () -> userPMMD.getText() != null && userPMMD.getText().matches("[a-zA-Z0-9._]{3,30}") ? null : "De 3 a 30 caracteres, sin espacios."), 1);
        f.nodo(vPMMD.campo("Rol", rolPMMD, null), 1);
        f.nodo(vPMMD.campo("Nombre", nombre, () -> ValidacionPMMD.requerido(nombre, "el nombre")), 2);
        f.nodo(vPMMD.campo("Cargo", cargoPMMD, null), 1);
        f.nodo(vPMMD.campo("Correo", emailPMMD, null), 1);
        f.nodo(vPMMD.campo("Contraseña", passPMMD, () -> existentePMMD == null && passPMMD.getText().length() < 6 ? "Mínimo 6 caracteres."
                : existentePMMD != null && !passPMMD.getText().isEmpty() && passPMMD.getText().length() < 6 ? "Mínimo 6 caracteres." : null), 1);
        f.nodo(activoPMMD, 1);
        Button cancelarPMMD = UIPMMD.secondary("Cancelar", null);
        Button okPMMD = UIPMMD.primary("Guardar", "SAVE");
        vPMMD.enlazar(okPMMD);
        var hPMMD = overlaysPMMD.modal(existentePMMD == null ? "Nuevo usuario" : uPMMD.getNombre(), null, f, 560, cancelarPMMD, okPMMD);
        cancelarPMMD.setOnAction(ePMMD -> hPMMD.cerrar());
        okPMMD.setOnAction(ePMMD -> {
            uPMMD.setUsername(userPMMD.getText().trim());
            uPMMD.setNombre(nombre.getText().trim());
            uPMMD.setCargo(cargoPMMD.getText());
            uPMMD.setEmail(emailPMMD.getText());
            uPMMD.setRol(rolPMMD.getValue());
            uPMMD.setActivo(activoPMMD.isSelected());
            if (toastPMMD.intentar(() -> configPMMD.guardarUsuario(uPMMD, passPMMD.getText()))) {
                hPMMD.cerrar();
                toastPMMD.exito("Usuario " + uPMMD.getUsername() + " guardado.");
                renderPMMD();
            }
        });
    }



    private Node produccion() {
        GridPane mermasPMMD = UIPMMD.grid(5);
        int cPMMD = 0;
        Map<String, TextField> mermaCamposPMMD = new LinkedHashMap<>();
        for (String tipoPMMD : ParametroServicePMMD.MERMAS.keySet()) {
            TextField t = FormsPMMD.decimal(parametrosPMMD.merma(tipoPMMD));
            mermaCamposPMMD.put(tipoPMMD, t);
            mermasPMMD.add(UIPMMD.field(tipoPMMD + " (%)", t), cPMMD++, 0);
        }
        GridPane horasPMMD = UIPMMD.grid(6);
        cPMMD = 0;
        Map<EtapaMOPMMD, TextField> horaCamposPMMD = new LinkedHashMap<>();
        for (EtapaMOPMMD ePMMD : EtapaMOPMMD.values()) {
            TextField t = FormsPMMD.decimal(parametrosPMMD.costoHora(ePMMD));
            horaCamposPMMD.put(ePMMD, t);
            horasPMMD.add(UIPMMD.field(ePMMD.nombre() + " (S/ h)", t), cPMMD++, 0);
        }
        TextField indPMMD = FormsPMMD.decimal(parametrosPMMD.indirectos());
        TextField margenPMMD = FormsPMMD.decimal(parametrosPMMD.margenObjetivo());
        GridPane otrosPMMD = UIPMMD.grid(4);
        otrosPMMD.add(UIPMMD.field("Indirectos (%)", indPMMD), 0, 0);
        otrosPMMD.add(UIPMMD.field("Margen objetivo (%)", margenPMMD), 1, 0);
        Button guardarPMMD = UIPMMD.primary("Guardar parámetros", "SAVE");
        guardarPMMD.setOnAction(ePMMD -> {
            mermaCamposPMMD.forEach((kPMMD, t) -> parametrosPMMD.fijar("merma." + kPMMD, ValidacionPMMD.valor(t)));
            horaCamposPMMD.forEach((kPMMD, t) -> parametrosPMMD.fijar("costoHora." + kPMMD.name(), ValidacionPMMD.valor(t)));
            parametrosPMMD.fijar(ParametroServicePMMD.INDIRECTOS, ValidacionPMMD.valor(indPMMD));
            parametrosPMMD.fijar(ParametroServicePMMD.MARGEN_OBJETIVO, ValidacionPMMD.valor(margenPMMD));
            toastPMMD.exito("Parámetros de producción guardados. Se aplican a nuevas líneas de ficha y alertas de margen.");
        });
        return UIPMMD.card(UIPMMD.cardHeader("Producción", "Valores por defecto para fichas técnicas y costeo", guardarPMMD),
                UIPMMD.label("Mermas por defecto", "form-section-title"), mermasPMMD,
                UIPMMD.label("Costo por hora de mano de obra", "form-section-title"), horasPMMD,
                UIPMMD.label("Costeo", "form-section-title"), otrosPMMD,
                UIPMMD.wrap("Los costos por hora de cada ficha se guardan en sus operaciones; los nuevos valores se usan al crear fichas nuevas.", "cell-sub"));
    }



    private Node preferencias() {
        HBox temasPMMD = new HBox(14, temaTilePMMD("Claro", false), temaTilePMMD("Oscuro", true));
        SegmentedControlPMMD densidadPMMD = new SegmentedControlPMMD(ThemeManagerPMMD.DENSIDADES.toArray(String[]::new));
        densidadPMMD.setValor(temaPMMD.getDensidad());
        densidadPMMD.valorProperty().addListener((oPMMD, aPMMD, bPMMD) -> {
            temaPMMD.setDensidad(bPMMD);
            toastPMMD.info("Densidad de tablas: " + bPMMD.toLowerCase() + ".");
        });
        return UIPMMD.card(UIPMMD.cardHeader("Preferencias", "Se guardan en este equipo"), UIPMMD.label("Tema", "form-section-title"), temasPMMD,
                UIPMMD.label("Densidad de tablas", "form-section-title"), densidadPMMD,
                UIPMMD.label("Atajos: Ctrl+K buscar · Esc cerrar modales y paneles · Enter confirmar", "cell-sub"));
    }

    private Node temaTilePMMD(String nombre, boolean oscuroPMMD) {
        Region muestra = new Region();
        muestra.setPrefSize(160, 90);
        muestra.setStyle("-fx-background-radius: 10; -fx-background-color: " + (oscuroPMMD ? "linear-gradient(to right, #0A1226 30%, #0B1220 30%)" : "linear-gradient(to right, #14213D 30%, #F8FAFC 30%)") + ";");
        VBox vPMMD = new VBox(8, muestra, UIPMMD.label(nombre, "cell-title"));
        vPMMD.getStyleClass().add("theme-tile");
        vPMMD.pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("active"), temaPMMD.isOscuro() == oscuroPMMD);
        vPMMD.setOnMouseClicked(ePMMD -> {
            temaPMMD.setOscuro(oscuroPMMD);
            renderPMMD();
        });
        return vPMMD;
    }



    private Node perfil() {
        UsuarioPMMD uPMMD = sessionPMMD.usuario();
        PasswordField actualPMMD = new PasswordField(), nuevaPMMD = new PasswordField(), repetirPMMD = new PasswordField();
        ValidacionPMMD vPMMD = new ValidacionPMMD();
        VBox camposPMMD = new VBox(10, vPMMD.campo("Contraseña actual", actualPMMD, () -> ValidacionPMMD.requerido(actualPMMD, "tu contraseña actual")),
                vPMMD.campo("Nueva contraseña", nuevaPMMD, () -> nuevaPMMD.getText().length() < 6 ? "Mínimo 6 caracteres." : null),
                vPMMD.campo("Repetir", repetirPMMD, () -> !repetirPMMD.getText().equals(nuevaPMMD.getText()) ? "No coincide." : null));
        vPMMD.escuchar(nuevaPMMD.textProperty());
        Button cambiarPMMD = UIPMMD.primary("Cambiar contraseña", "LOCK");
        vPMMD.enlazar(cambiarPMMD);
        cambiarPMMD.setOnAction(ePMMD -> {
            if (uPMMD == null || !AuthServicePMMD.verificar(actualPMMD.getText(), uPMMD.getPasswordHash())) {
                toastPMMD.error("La contraseña actual no es correcta.");
                return;
            }
            if (toastPMMD.intentar(() -> configPMMD.guardarUsuario(uPMMD, nuevaPMMD.getText()))) {
                toastPMMD.exito("Contraseña actualizada.");
                actualPMMD.clear();
                nuevaPMMD.clear();
                repetirPMMD.clear();
            }
        });
        return UIPMMD.card(UIPMMD.hbox(14, new AvatarInitialsPMMD(sessionPMMD.nombre(), 60, "gradient"), new VBox(2, UIPMMD.label(sessionPMMD.nombre(), "card-title"),
                        UIPMMD.label(sessionPMMD.cargo() + " · " + sessionPMMD.rol().nombre() + " · usuario " + sessionPMMD.username(), "cell-sub"))),
                UIPMMD.label("Cambiar contraseña", "form-section-title"), camposPMMD, UIPMMD.hbox(8, UIPMMD.spacer(), cambiarPMMD));
    }
}
