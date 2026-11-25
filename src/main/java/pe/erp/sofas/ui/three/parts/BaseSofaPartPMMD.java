package pe.erp.sofas.ui.three.parts;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Translate;
import pe.erp.sofas.ui.three.MallasPMMD;

import java.util.HashMap;
import java.util.Map;






public abstract class BaseSofaPartPMMD implements SofaPartPMMD {

    protected final String nombre;

    protected final double ancho;

    protected final double fondo;

    protected final double alto;

    public BaseSofaPartPMMD(String nombre, double ancho, double fondo, double alto) {
        this.nombre = nombre;
        this.ancho = ancho;
        this.fondo = fondo;
        this.alto = alto;
    }

    @Override
    public PartDimensions getDimensions() {
        return new PartDimensions(ancho, fondo, alto);
    }

    @Override
    public Map<String, Punto3D> getAnchors() {

        Map<String, Punto3D> anchorsPMMD = new HashMap<>();
        anchorsPMMD.put("left", new Punto3D(-ancho / 2, 0, 0));
        anchorsPMMD.put("right", new Punto3D(ancho / 2, 0, 0));
        anchorsPMMD.put("front", new Punto3D(0, 0, -fondo / 2));
        anchorsPMMD.put("back", new Punto3D(0, 0, fondo / 2));
        return anchorsPMMD;
    }

    @Override
    public String getName() {
        return nombre;
    }





    protected MeshView cajaRedondeada(double wPMMD, double hPMMD, double dPMMD, double radioPMMD, PhongMaterial materialPMMD) {
        MeshView mvPMMD = new MeshView(MallasPMMD.cajaRedondeada(wPMMD, hPMMD, dPMMD, radioPMMD, 4));
        mvPMMD.setMaterial(materialPMMD);
        mvPMMD.setCullFace(javafx.scene.shape.CullFace.NONE);
        return mvPMMD;
    }





    protected Node agregar(Group grupoPMMD, Node nodoPMMD, double xPMMD, double yCentroPMMD, double zPMMD) {
        nodoPMMD.setTranslateX(xPMMD);
        nodoPMMD.setTranslateY(-yCentroPMMD);
        nodoPMMD.setTranslateZ(zPMMD);
        grupoPMMD.getChildren().add(nodoPMMD);
        return nodoPMMD;
    }





    protected PhongMaterial materialPorDefecto() {
        return new PhongMaterial(javafx.scene.paint.Color.web("#B9B7B1"));
    }
}
