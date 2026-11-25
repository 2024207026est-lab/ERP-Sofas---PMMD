package pe.erp.sofas.ui.three;

import javafx.animation.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Bounds;
import javafx.scene.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Duration;







public class Viewer3DPMMD extends Region {

    public enum Camara { 
        FRONTAL("Frontal", 0, 8), 
           LATERAL("Lateral", 90, 8), 
              TRES_CUARTOS("3/4", -35, 20), 
                 SUPERIOR("Superior", 0, 80);

        private final String nombre;

        private final double yaw;

        private final double pitch;

        Camara(String nombre, double yaw, double pitch) {
            this.nombre = nombre;
            this.yaw = yaw;
            this.pitch = pitch;
        }

        public String nombre() {
            return nombre;
        }
    }

    private final Group mundoPMMD = new Group();

    private final Group contenido = new Group();

    private final Group pisoPMMD = new Group();

    private final SubScene subScene;

    private final PerspectiveCamera camara = new PerspectiveCamera(true);

    private final DoubleProperty yaw = new SimpleDoubleProperty(-35);

    private final DoubleProperty pitch = new SimpleDoubleProperty(20);

    private final DoubleProperty distanciaPMMD = new SimpleDoubleProperty(520);

    private final Rotate rotYPMMD = new Rotate(0, Rotate.Y_AXIS);

    private final Rotate rotXPMMD = new Rotate(0, Rotate.X_AXIS);

    private final Translate objetivoPMMD = new Translate(0, -40, 0);

    private final Translate alejarPMMD = new Translate(0, 0, -520);

    private double mxPMMD, myPMMD;

    private boolean girandoPMMD;

    private double distanciaBasePMMD = 520;

    private final AnimationTimer giroPMMD = new AnimationTimer() {

        private long ultimoPMMD;

        @Override
        public void handle(long nowPMMD) {
            if (ultimoPMMD != 0) yaw.set(yaw.get() + (nowPMMD - ultimoPMMD) / 1e9 * 30);
            ultimoPMMD = nowPMMD;
        }

        @Override
        public void stop() {
            super.stop();
            ultimoPMMD = 0;
        }
    };

    public Viewer3DPMMD(boolean conPisoPMMD) {
        getStyleClass().add("viewer3d");
        camara.setFieldOfView(32);
        camara.setNearClip(1);
        camara.setFarClip(20000);
        Group rigPMMD = new Group(camara);
        camara.getTransforms().add(alejarPMMD);
        rigPMMD.getTransforms().addAll(objetivoPMMD, rotYPMMD, rotXPMMD);
        rotYPMMD.angleProperty().bind(yaw);
        rotXPMMD.angleProperty().bind(pitch.negate());
        alejarPMMD.zProperty().bind(distanciaPMMD.negate());

        AmbientLight ambientePMMD = new AmbientLight(Color.gray(0.52));
        DirectionalLight principalPMMD = new DirectionalLight(Color.gray(0.62));
        principalPMMD.setDirection(new javafx.geometry.Point3D(-0.6, 1, 0.8));
        DirectionalLight rellenoPMMD = new DirectionalLight(Color.gray(0.28));
        rellenoPMMD.setDirection(new javafx.geometry.Point3D(0.8, 0.6, -0.5));
        PointLight brilloPMMD = new PointLight(Color.gray(0.18));
        brilloPMMD.setTranslateY(-400);
        brilloPMMD.setTranslateZ(-300);
        mundoPMMD.getChildren().addAll(ambientePMMD, principalPMMD, rellenoPMMD, brilloPMMD, pisoPMMD, contenido, rigPMMD);
        if (conPisoPMMD) construirPisoPMMD(600, 600);

        subScene = new SubScene(mundoPMMD, 400, 300, true, SceneAntialiasing.BALANCED);
        subScene.setCamera(camara);
        subScene.setFill(Color.TRANSPARENT);
        getChildren().add(subScene);

        subScene.setOnMousePressed(ePMMD -> {
            mxPMMD = ePMMD.getSceneX();
            myPMMD = ePMMD.getSceneY();
            if (girandoPMMD) detenerGiro();
        });
        subScene.setOnMouseDragged(ePMMD -> {
            double dxPMMD = ePMMD.getSceneX() - mxPMMD, dyPMMD = ePMMD.getSceneY() - myPMMD;
            mxPMMD = ePMMD.getSceneX();
            myPMMD = ePMMD.getSceneY();
            if (ePMMD.getButton() == MouseButton.SECONDARY || ePMMD.isShiftDown()) {
                objetivoPMMD.setX(objetivoPMMD.getX() - dxPMMD * distanciaPMMD.get() / 900);
                objetivoPMMD.setY(objetivoPMMD.getY() - dyPMMD * distanciaPMMD.get() / 900);
            } else {
                yaw.set(yaw.get() + dxPMMD * 0.4);
                pitch.set(Math.max(-5, Math.min(85, pitch.get() + dyPMMD * 0.3)));
            }
        });
        subScene.setOnScroll(ePMMD -> distanciaPMMD.set(Math.max(distanciaBasePMMD * 0.35, Math.min(distanciaBasePMMD * 3, distanciaPMMD.get() * (ePMMD.getDeltaY() > 0 ? 0.92 : 1.08)))));
        subScene.setOnMouseClicked(ePMMD -> {
            if (ePMMD.getClickCount() == 2) centrar(true);
        });

        sceneProperty().addListener((oPMMD, aPMMD, sPMMD) -> {
            if (sPMMD == null) giroPMMD.stop();
            else if (girandoPMMD) giroPMMD.start();
        });
        visibleProperty().addListener((oPMMD, aPMMD, v) -> {
            if (!v) giroPMMD.stop();
            else if (girandoPMMD) giroPMMD.start();
        });
    }

    private void construirPisoPMMD(double w, double dPMMD) {
        pisoPMMD.getChildren().clear();
        MeshView sueloPMMD = new MeshView(MallasPMMD.plano(9000, 9000));
        PhongMaterial msPMMD = new PhongMaterial(Color.web("#EEF1F5"));
        msPMMD.setSpecularColor(Color.gray(0.05));
        sueloPMMD.setMaterial(msPMMD);
        sueloPMMD.setCullFace(CullFace.NONE);
        sueloPMMD.setTranslateY(0.2);
        MeshView sombraPMMD = new MeshView(MallasPMMD.plano(w, dPMMD * 0.75));
        PhongMaterial mshPMMD = new PhongMaterial(Color.WHITE);
        mshPMMD.setDiffuseMap(TexturasPMMD.sombra());
        mshPMMD.setSpecularColor(Color.gray(0.01));
        sombraPMMD.setMaterial(mshPMMD);
        sombraPMMD.setCullFace(CullFace.NONE);
        sombraPMMD.setTranslateY(-0.1);
        pisoPMMD.getChildren().addAll(sueloPMMD, sombraPMMD);
    }

    public void setFondoPiso(String hexPMMD) {
        if (!pisoPMMD.getChildren().isEmpty() && pisoPMMD.getChildren().get(0) instanceof MeshView mv) {
            ((PhongMaterial) mv.getMaterial()).setDiffuseColor(Color.web(hexPMMD));
        }
    }



    public void setContenido(Node nPMMD, boolean recentrarPMMD) {
        contenido.getChildren().setAll(nPMMD);
        if (recentrarPMMD) centrar(false);
    }

    public Group contenido() {
        return contenido;
    }



    public void centrar(boolean animarPMMD) {
        Bounds bPMMD = contenido.getBoundsInParent();
        if (bPMMD.isEmpty()) return;
        double tamPMMD = Math.max(bPMMD.getWidth(), Math.max(bPMMD.getHeight(), bPMMD.getDepth()));
        distanciaBasePMMD = Math.max(160, tamPMMD * 2.25);
        double cx = (bPMMD.getMinX() + bPMMD.getMaxX()) / 2, cy = (bPMMD.getMinY() + bPMMD.getMaxY()) / 2, cz = (bPMMD.getMinZ() + bPMMD.getMaxZ()) / 2;
        if (pisoPMMD.getChildren().size() > 1) {
            pisoPMMD.getChildren().get(1).setScaleX(Math.max(0.4, bPMMD.getWidth() / 420));
            pisoPMMD.getChildren().get(1).setScaleZ(Math.max(0.4, bPMMD.getDepth() / 300));
            pisoPMMD.getChildren().get(1).setTranslateX(cx);
            pisoPMMD.getChildren().get(1).setTranslateZ(cz);
        }
        if (animarPMMD) {
            new Timeline(new KeyFrame(Duration.millis(450),
                    new KeyValue(objetivoPMMD.xProperty(), cx, Interpolator.EASE_BOTH),
                    new KeyValue(objetivoPMMD.yProperty(), cy, Interpolator.EASE_BOTH),
                    new KeyValue(objetivoPMMD.zProperty(), cz, Interpolator.EASE_BOTH),
                    new KeyValue(distanciaPMMD, distanciaBasePMMD, Interpolator.EASE_BOTH))).play();
        } else {
            objetivoPMMD.setX(cx);
            objetivoPMMD.setY(cy);
            objetivoPMMD.setZ(cz);
            distanciaPMMD.set(distanciaBasePMMD);
        }
    }

    public void camara(Camara cPMMD) {
        detenerGiro();
        double destinoPMMD = cPMMD.yaw;

        double actualPMMD = yaw.get() % 360;
        yaw.set(actualPMMD);
        double deltaPMMD = ((destinoPMMD - actualPMMD + 540) % 360) - 180;
        new Timeline(new KeyFrame(Duration.millis(650),
                new KeyValue(yaw, actualPMMD + deltaPMMD, Interpolator.EASE_BOTH),
                new KeyValue(pitch, cPMMD.pitch, Interpolator.EASE_BOTH),
                new KeyValue(distanciaPMMD, distanciaBasePMMD * (cPMMD == Camara.SUPERIOR ? 1.15 : 1), Interpolator.EASE_BOTH))).play();
    }

    public boolean alternarGiro() {
        if (girandoPMMD) detenerGiro();
        else {
            girandoPMMD = true;
            if (getScene() != null && isVisible()) giroPMMD.start();
        }
        return girandoPMMD;
    }

    public void detenerGiro() {
        girandoPMMD = false;
        giroPMMD.stop();
    }

    public boolean isGirando() {
        return girandoPMMD;
    }

    public WritableImage captura(double escalaPMMD) {
        SnapshotParameters spPMMD = new SnapshotParameters();
        spPMMD.setFill(Color.WHITE);
        spPMMD.setTransform(javafx.scene.transform.Transform.scale(escalaPMMD, escalaPMMD));
        return subScene.snapshot(spPMMD, null);
    }

    public SubScene subScene() {
        return subScene;
    }

    public void fijarVista(double yawGradosPMMD, double pitchGradosPMMD) {
        yaw.set(yawGradosPMMD);
        pitch.set(pitchGradosPMMD);
    }

    public DoubleProperty yawProperty() {
        return yaw;
    }

    public DoubleProperty pitchProperty() {
        return pitch;
    }

    @Override
    protected void layoutChildren() {
        subScene.setWidth(getWidth());
        subScene.setHeight(getHeight());
    }



    public static WritableImage render(Node nPMMD, double w, double h, double yawGradosPMMD, double pitchGradosPMMD) {
        Viewer3DPMMD v = new Viewer3DPMMD(true);
        v.subScene.setFill(Color.web("#EEF2F7"));

        Scene temporalPMMD = new Scene(new Group(v), w, h, true, SceneAntialiasing.BALANCED);
        temporalPMMD.setFill(Color.web("#EEF2F7"));
        v.resize(w, h);
        v.layoutChildren();
        v.setContenido(nPMMD, true);
        v.fijarVista(yawGradosPMMD, pitchGradosPMMD);
        v.distanciaPMMD.set(v.distanciaBasePMMD * 0.95);
        return temporalPMMD.snapshot(null);
    }
}
