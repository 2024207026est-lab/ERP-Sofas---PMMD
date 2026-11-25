package pe.erp.sofas.ui.views.login;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.RequiredArgsConstructor;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pe.erp.sofas.core.*;
import pe.erp.sofas.model.UsuarioPMMD;
import pe.erp.sofas.service.AuthServicePMMD;
import pe.erp.sofas.service.ImagenServicePMMD;
import pe.erp.sofas.ui.AppWindowPMMD;
import pe.erp.sofas.ui.components.*;

import java.io.File;

@Component
@Lazy
@Scope("prototype")
@RequiredArgsConstructor
public class LoginControllerPMMD {

    private final AuthServicePMMD authServicePMMD;

    private final SessionPMMD sessionPMMD;

    private final AppWindowPMMD appWindowPMMD;

    private final ToastPMMD toastPMMD;

    private final OverlaysPMMD overlaysPMMD;

    private final ImagenServicePMMD imagenServicePMMD;

    @FXML private StackPane raiz;

    @FXML private VBox formulario;

    @FXML private StackPane logoHost;

    @FXML private HBox cajaUsuario;

    @FXML private HBox cajaPassword;

    @FXML private TextField txtUsuario;

    @FXML private PasswordField txtPassword;

    @FXML private TextField txtPasswordVisible;

    @FXML private Button btnOjo;

    @FXML private CheckBox chkRecordar;

    @FXML private Button btnIngresar;

    @FXML private HBox notaDemo;

    @FXML private StackPane hero;

    @FXML private Label errUsuario;

    @FXML private Label errPassword;

    @FXML private HBox errorGeneral;

    @FXML private Label lblErrorGeneral;

    @FXML
    public void initialize() {
        overlaysPMMD.registrar(raiz);
        logoHost.getChildren().add(new LogoMarkPMMD(42, "SOFÁS · JULIACA"));
        cajaUsuario.getChildren().add(0, IcoPMMD.of("PERSON_OUTLINE", 19));
        cajaPassword.getChildren().add(0, IcoPMMD.of("LOCK", 19));
        btnOjo.setGraphic(IcoPMMD.of("VISIBILITY", 18));
        UIPMMD.tooltip(btnOjo, "Mostrar contraseña");
        notaDemo.getChildren().add(0, IcoPMMD.of("INFO", 18));
        errorGeneral.getChildren().add(0, IcoPMMD.of("ERROR", 16));
        txtPasswordVisible.textProperty().bindBidirectional(txtPassword.textProperty());

        boolean recordarPMMD = PrefsPMMD.bool(PrefsPMMD.RECORDAR, true);
        chkRecordar.setSelected(recordarPMMD);
        if (recordarPMMD) txtUsuario.setText(PrefsPMMD.str(PrefsPMMD.ULTIMO_USUARIO, "cmendoza"));
        txtUsuario.textProperty().addListener((oPMMD, aPMMD, bPMMD) -> limpiarPMMD(errUsuario, cajaUsuario));
        txtPassword.textProperty().addListener((oPMMD, aPMMD, bPMMD) -> limpiarPMMD(errPassword, cajaPassword));

        hero.getChildren().add(construirHeroPMMD());
        UIPMMD.entrar(formulario);
        Platform.runLater(() -> (txtUsuario.getText().isEmpty() ? txtUsuario : txtPassword).requestFocus());
    }

    private void limpiarPMMD(Label errPMMD, HBox cajaPMMD) {
        errPMMD.setVisible(false);
        errPMMD.setManaged(false);
        cajaPMMD.getStyleClass().remove("field-invalid");
        errorGeneral.setVisible(false);
        errorGeneral.setManaged(false);
    }

    private void errorPMMD(Label errPMMD, HBox cajaPMMD, String msgPMMD) {
        errPMMD.setText(msgPMMD);
        errPMMD.setVisible(true);
        errPMMD.setManaged(true);
        if (!cajaPMMD.getStyleClass().contains("field-invalid")) cajaPMMD.getStyleClass().add("field-invalid");
    }

    @FXML
    private void onMostrarPasswordPMMD() {
        boolean mostrarPMMD = !txtPasswordVisible.isVisible();
        txtPasswordVisible.setVisible(mostrarPMMD);
        txtPassword.setVisible(!mostrarPMMD);
        ((FontIcon) btnOjo.getGraphic()).setIconCode(IcoPMMD.ikon(mostrarPMMD ? "VISIBILITY_OFF" : "VISIBILITY"));
        btnOjo.getTooltip().setText(mostrarPMMD ? "Ocultar contraseña" : "Mostrar contraseña");
    }



    @FXML
    private void onOlvidoPMMD() {
        ValidacionPMMD vPMMD = new ValidacionPMMD();
        TextField correoPMMD = UIPMMD.text(txtUsuario.getText(), "Usuario o correo @innovasur.pe");
        VBox cuerpoPMMD = new VBox(14, UIPMMD.wrap("Te enviaremos un enlace para restablecer tu contraseña. Si no lo recibes, "
                        + "pide ayuda a la administradora (Milagros Cahuana).", "muted"),
                vPMMD.campo("Usuario o correo", correoPMMD, () -> ValidacionPMMD.requerido(correoPMMD, "tu usuario o correo")));
        Button cancelarPMMD = UIPMMD.secondary("Cancelar", null);
        Button enviarPMMD = UIPMMD.primary("Enviar enlace", "SEND");
        vPMMD.enlazar(enviarPMMD);
        var hPMMD = overlaysPMMD.modal("Recuperar contraseña", "Simulado: no se envía ningún correo real.", cuerpoPMMD, 440, cancelarPMMD, enviarPMMD);
        cancelarPMMD.setOnAction(ePMMD -> hPMMD.cerrar());
        enviarPMMD.setOnAction(ePMMD -> {
            hPMMD.cerrar();
            toastPMMD.exito("Enlace de recuperación enviado a " + (correoPMMD.getText().contains("@") ? correoPMMD.getText() : correoPMMD.getText().trim() + "@innovasur.pe") + ".");
        });
        Platform.runLater(correoPMMD::requestFocus);
    }

    @FXML
    private void onIngresarPMMD() {
        if (btnIngresar.isDisabled()) return;
        boolean okPMMD = true;
        if (txtUsuario.getText() == null || txtUsuario.getText().isBlank()) {
            errorPMMD(errUsuario, cajaUsuario, "Ingresa tu usuario.");
            okPMMD = false;
        }
        if (txtPassword.getText() == null || txtPassword.getText().isEmpty()) {
            errorPMMD(errPassword, cajaPassword, "Ingresa tu contraseña.");
            okPMMD = false;
        }
        if (!okPMMD) return;
        ProgressIndicator spinnerPMMD = new ProgressIndicator();
        spinnerPMMD.getStyleClass().add("btn-spinner");
        spinnerPMMD.setPrefSize(16, 16);
        btnIngresar.setGraphic(spinnerPMMD);
        btnIngresar.setText("Verificando…");
        btnIngresar.setDisable(true);
        PauseTransition esperaPMMD = new PauseTransition(Duration.seconds(1));
        esperaPMMD.setOnFinished(ePMMD -> {
            try {
                UsuarioPMMD uPMMD = authServicePMMD.login(txtUsuario.getText(), txtPassword.getText());
                sessionPMMD.iniciar(uPMMD);
                PrefsPMMD.setBool(PrefsPMMD.RECORDAR, chkRecordar.isSelected());
                if (chkRecordar.isSelected()) PrefsPMMD.setStr(PrefsPMMD.ULTIMO_USUARIO, uPMMD.getUsername());
                appWindowPMMD.mostrarShell("dashboard");
                toastPMMD.exito("Sesión iniciada. ¡Buen día, " + uPMMD.getPrimerNombre() + "!");
            } catch (NegocioExceptionPMMD exPMMD) {
                btnIngresar.setGraphic(null);
                btnIngresar.setText("Iniciar sesión");
                btnIngresar.setDisable(false);
                lblErrorGeneral.setText(exPMMD.getMessage());
                errorGeneral.setVisible(true);
                errorGeneral.setManaged(true);
                cajaPassword.getStyleClass().add("field-invalid");
                txtPassword.selectAll();
                txtPassword.requestFocus();
            }
        });
        esperaPMMD.play();
    }



    private StackPane construirHeroPMMD() {
        StackPane tarjetaPMMD = new StackPane();
        tarjetaPMMD.getStyleClass().add("login-hero");
        ImageSlotPMMD fotoPMMD = new ImageSlotPMMD("Foto: sala con sofá (arrastra una imagen aquí)", -1, 10, 18);
        fotoPMMD.setMinHeight(0);
        fotoPMMD.setPrefHeight(Region.USE_COMPUTED_SIZE);
        fotoPMMD.setMaxHeight(Double.MAX_VALUE);
        File localPMMD = imagenServicePMMD.archivo("login");
        fotoPMMD.mostrar(localPMMD.exists() ? localPMMD.getAbsolutePath() : "classpath:/images/sofas/login.jpg");
        fotoPMMD.alElegir(fPMMD -> toastPMMD.intentar(() -> {
            String rutaPMMD = imagenServicePMMD.guardar(fPMMD, "login", 1600, 1200);
            ImagenesPMMD.olvidar(rutaPMMD);
            fotoPMMD.mostrar(rutaPMMD);
        }));
        fotoPMMD.alQuitar(() -> {
            imagenServicePMMD.eliminar(localPMMD.getAbsolutePath());
            ImagenesPMMD.olvidar(localPMMD.getAbsolutePath());
            fotoPMMD.mostrar("classpath:/images/sofas/login.jpg");
        });
        tarjetaPMMD.getChildren().add(fotoPMMD);
        Region degradadoPMMD = new Region();
        degradadoPMMD.getStyleClass().add("login-hero-gradient");
        degradadoPMMD.setMouseTransparent(true);
        tarjetaPMMD.getChildren().add(degradadoPMMD);

        Label titular = UIPMMD.wrap("Sofás hechos a tu medida en Juliaca desde 2012.", "hero-title");
        Label sub = UIPMMD.wrap("Telas, producción del taller, pedidos a medida y ventas de SISTEMA ERP SOFAS conectados en una sola plataforma.", "hero-subtitle");
        VBox textosPMMD = new VBox(12, titular, sub);
        textosPMMD.setMouseTransparent(true);
        textosPMMD.setMaxWidth(560);
        textosPMMD.setPadding(new Insets(0, 44, 44, 44));
        textosPMMD.setAlignment(Pos.BOTTOM_LEFT);
        StackPane.setAlignment(textosPMMD, Pos.BOTTOM_LEFT);
        textosPMMD.setMaxHeight(Region.USE_PREF_SIZE);
        tarjetaPMMD.getChildren().add(textosPMMD);

        Rectangle clipPMMD = new Rectangle();
        clipPMMD.setArcWidth(36);
        clipPMMD.setArcHeight(36);
        clipPMMD.widthProperty().bind(tarjetaPMMD.widthProperty());
        clipPMMD.heightProperty().bind(tarjetaPMMD.heightProperty());
        tarjetaPMMD.setClip(clipPMMD);
        return tarjetaPMMD;
    }
}
