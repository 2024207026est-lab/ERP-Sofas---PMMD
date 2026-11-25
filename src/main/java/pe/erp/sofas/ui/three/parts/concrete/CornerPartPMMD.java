package pe.erp.sofas.ui.three.parts.concrete;

import javafx.scene.Group;
import javafx.scene.paint.PhongMaterial;
import pe.erp.sofas.ui.three.parts.BaseSofaPartPMMD;








public class CornerPartPMMD extends BaseSofaPartPMMD {

    private final PhongMaterial materialAsiento;

    private final PhongMaterial materialRespaldo;

    public CornerPartPMMD(double tamanioPMMD, PhongMaterial materialAsiento, PhongMaterial materialRespaldo) {
        super("Esquinero", tamanioPMMD, tamanioPMMD, 85);
        this.materialAsiento = materialAsiento == null ? materialPorDefecto() : materialAsiento;
        this.materialRespaldo = materialRespaldo == null ? materialPorDefecto() : materialRespaldo;
    }

    @Override
    public Group build() {
        Group gPMMD = new Group();


        agregar(gPMMD, cajaRedondeada(ancho, 18, fondo, 3, materialAsiento), 0, 18 / 2, 0);


        double alturaCojinPMMD = 14;
        agregar(gPMMD, cajaRedondeada(ancho - 4, alturaCojinPMMD, fondo - 4, 5, materialAsiento),
                0, 18 + alturaCojinPMMD / 2, 0);


        double altoRespPMMD = alto - 18 - 6;


        agregar(gPMMD, cajaRedondeada(ancho - 4, altoRespPMMD, 20, 6, materialRespaldo),
                0, 18 + altoRespPMMD / 2, fondo / 2 - 10);


        agregar(gPMMD, cajaRedondeada(20, altoRespPMMD, fondo - 4, 6, materialRespaldo),
                ancho / 2 - 10, 18 + altoRespPMMD / 2, 0);

        return gPMMD;
    }
}
