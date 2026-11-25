package pe.erp.sofas.ui.components;

import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;







public class ImageSlotPMMD extends StackPane {

    private final double radio;

    private final String placeholder;

    private Consumer<File> alElegir;

    private Runnable alQuitar;

    private Supplier<Image> respaldo;

    private boolean editablePMMD = true;

    public ImageSlotPMMD(String placeholder, double anchoPMMD, double altoPMMD, double radio) {
        this.radio = radio;
        this.placeholder = placeholder;
        getStyleClass().add("image-slot");
        if (anchoPMMD > 0) {
            setMinWidth(anchoPMMD);
            setPrefWidth(anchoPMMD);
            setMaxWidth(anchoPMMD);
        } else {
            setMinWidth(0);
            setMaxWidth(Double.MAX_VALUE);
        }
        setMinHeight(altoPMMD);
        setPrefHeight(altoPMMD);
        setMaxHeight(altoPMMD);
        Rectangle clipPMMD = new Rectangle();
        clipPMMD.setArcWidth(radio * 2);
        clipPMMD.setArcHeight(radio * 2);
        clipPMMD.widthProperty().bind(widthProperty());
        clipPMMD.heightProperty().bind(heightProperty());
        setClip(clipPMMD);

        setOnDragOver(ePMMD -> {
            if (editablePMMD && ePMMD.getDragboard().hasFiles() && esImagenPMMD(ePMMD.getDragboard().getFiles().get(0))) {
                ePMMD.acceptTransferModes(TransferMode.COPY);
                getStyleClass().add("image-slot-drop");
            }
            ePMMD.consume();
        });
        setOnDragExited(ePMMD -> getStyleClass().remove("image-slot-drop"));
        setOnDragDropped(ePMMD -> {
            boolean okPMMD = false;
            if (ePMMD.getDragboard().hasFiles() && alElegir != null) {
                File fPMMD = ePMMD.getDragboard().getFiles().get(0);
                if (esImagenPMMD(fPMMD)) {
                    alElegir.accept(fPMMD);
                    okPMMD = true;
                }
            }
            ePMMD.setDropCompleted(okPMMD);
            ePMMD.consume();
        });
        setOnMouseClicked(ePMMD -> {
            if (!editablePMMD || ePMMD.getButton() != MouseButton.PRIMARY || alElegir == null) return;
            if (ePMMD.getClickCount() == 1 && getProperties().get("conImagen") == null) elegir();
        });
        mostrar((Image) null);
    }

    public ImageSlotPMMD alElegir(Consumer<File> cPMMD) {
        this.alElegir = cPMMD;
        configurarMenuPMMD();
        return this;
    }

    public ImageSlotPMMD alQuitar(Runnable rPMMD) {
        this.alQuitar = rPMMD;
        configurarMenuPMMD();
        return this;
    }



    public ImageSlotPMMD respaldo(Supplier<Image> sPMMD) {
        this.respaldo = sPMMD;
        return this;
    }

    public ImageSlotPMMD soloLectura() {
        this.editablePMMD = false;
        getStyleClass().add("image-slot-readonly");
        return this;
    }

    public void elegir() {
        FileChooser fcPMMD = new FileChooser();
        fcPMMD.setTitle(placeholder);
        fcPMMD.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imágenes (JPG, PNG, WebP)", "*.jpg", "*.jpeg", "*.png", "*.webp"));
        File fPMMD = fcPMMD.showOpenDialog(getScene() == null ? null : getScene().getWindow());
        if (fPMMD != null && alElegir != null) alElegir.accept(fPMMD);
    }

    private void configurarMenuPMMD() {
        if (!editablePMMD) return;
        MenuItem reemplazar = new MenuItem("Reemplazar…", IcoPMMD.of("SWAP_HORIZ", 16));
        reemplazar.setOnAction(ePMMD -> elegir());
        MenuItem quitar = new MenuItem("Quitar", IcoPMMD.of("DELETE_OUTLINE", 16));
        quitar.setOnAction(ePMMD -> {
            if (alQuitar != null) alQuitar.run();
        });
        ContextMenu mPMMD = new ContextMenu(reemplazar, quitar);
        setOnContextMenuRequested(ePMMD -> {
            quitar.setDisable(getProperties().get("conImagen") == null || alQuitar == null);
            mPMMD.show(this, ePMMD.getScreenX(), ePMMD.getScreenY());
        });
    }

    public void mostrar(String rutaPMMD) {
        mostrar(ImagenesPMMD.cargar(rutaPMMD));
    }

    public void mostrar(Image imgPMMD) {
        getChildren().clear();
        getStyleClass().removeAll("image-slot-filled");
        getProperties().remove("conImagen");
        Image mostrar = imgPMMD;
        boolean deRespaldoPMMD = false;
        if ((mostrar == null || mostrar.isError()) && respaldo != null) {
            mostrar = respaldo.get();
            deRespaldoPMMD = true;
        }
        if (mostrar != null && !mostrar.isError()) {
            ImageView ivPMMD = new ImageView(mostrar);
            ivPMMD.setSmooth(true);
            ivPMMD.setPreserveRatio(false);
            Image finPMMD = mostrar;
            Runnable ajustarPMMD = () -> {
                double wPMMD = getWidth() > 0 ? getWidth() : getPrefWidth();
                double hPMMD = getHeight() > 0 ? getHeight() : getPrefHeight();
                if (wPMMD <= 0 || hPMMD <= 0) return;
                double ratioPMMD = wPMMD / hPMMD;
                double iwPMMD = finPMMD.getWidth(), ihPMMD = finPMMD.getHeight();
                double vw = Math.min(iwPMMD, ihPMMD * ratioPMMD), vh = vw / ratioPMMD;
                ivPMMD.setViewport(new Rectangle2D((iwPMMD - vw) / 2, (ihPMMD - vh) / 2, vw, vh));
                ivPMMD.setFitWidth(wPMMD);
                ivPMMD.setFitHeight(hPMMD);
            };
            widthProperty().addListener((oPMMD, aPMMD, bPMMD) -> ajustarPMMD.run());
            heightProperty().addListener((oPMMD, aPMMD, bPMMD) -> ajustarPMMD.run());
            ajustarPMMD.run();
            getChildren().add(ivPMMD);
            if (!deRespaldoPMMD) {
                getStyleClass().add("image-slot-filled");
                getProperties().put("conImagen", true);
            } else {
                var etiquetaPMMD = UIPMMD.label("Render 3D", "image-slot-tag");
                StackPane.setAlignment(etiquetaPMMD, Pos.TOP_LEFT);
                getChildren().add(etiquetaPMMD);
            }
        } else {
            VBox vPMMD = new VBox(6, IcoPMMD.of("ADD_PHOTO_ALTERNATE", getPrefHeight() > 120 ? 30 : 20),
                    UIPMMD.wrap(placeholder, "image-slot-text"));
            vPMMD.setAlignment(Pos.CENTER);
            vPMMD.setMaxWidth(Math.max(80, getPrefWidth() - 24));
            if (getPrefHeight() < 70) vPMMD.getChildren().remove(1);
            getChildren().add(vPMMD);
        }
    }

    public Node contenido() {
        return getChildren().isEmpty() ? null : getChildren().get(0);
    }

    private static boolean esImagenPMMD(File fPMMD) {
        String nPMMD = fPMMD.getName().toLowerCase(Locale.ROOT);
        return nPMMD.endsWith(".jpg") || nPMMD.endsWith(".jpeg") || nPMMD.endsWith(".png") || nPMMD.endsWith(".webp");
    }
}
