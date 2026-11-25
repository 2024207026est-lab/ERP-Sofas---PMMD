package pe.erp.sofas.ui.three.parts;

import javafx.scene.Group;
import javafx.scene.Node;

import java.util.ArrayList;
import java.util.List;














public class SofaPartAssemblerPMMD {

    private final List<SofaPartPMMD> partes = new ArrayList<>();

    private PartLayoutPMMD layout = PartLayoutPMMD.LINEAL;

    public SofaPartAssemblerPMMD() {
    }

    public SofaPartAssemblerPMMD conLayout(PartLayoutPMMD layout) {
        this.layout = layout;
        return this;
    }

    public SofaPartAssemblerPMMD agregar(SofaPartPMMD partePMMD) {
        this.partes.add(partePMMD);
        return this;
    }

    public SofaPartAssemblerPMMD agregarTodas(List<SofaPartPMMD> partes) {
        this.partes.addAll(partes);
        return this;
    }






    public Ensamblado ensamblar() {
        if (partes.isEmpty()) {
            return new Ensamblado(new Group(), new SofaPartPMMD.PartDimensions(0, 0, 0), 0, 0, 0);
        }

        Group resultado = new Group();
        double posXPMMD = 0;
        double maxZPMMD = 0;
        double maxYPMMD = 0;

        for (SofaPartPMMD partePMMD : partes) {
            Group geomPMMD = partePMMD.build();
            SofaPartPMMD.PartDimensions dimPMMD = partePMMD.getDimensions();


            geomPMMD.setTranslateX(posXPMMD + dimPMMD.ancho() / 2);

            geomPMMD.setTranslateY(-dimPMMD.alto() / 2);

            geomPMMD.setTranslateZ(0);

            resultado.getChildren().add(geomPMMD);


            posXPMMD += dimPMMD.ancho();
            maxZPMMD = Math.max(maxZPMMD, dimPMMD.fondo());
            maxYPMMD = Math.max(maxYPMMD, dimPMMD.alto());
        }

        double largoTotal = posXPMMD;
        double fondoTotal = maxZPMMD;
        double altoTotal = maxYPMMD;

        return new Ensamblado(resultado, new SofaPartPMMD.PartDimensions(largoTotal, fondoTotal, altoTotal),
                largoTotal, fondoTotal, altoTotal);
    }





    public record Ensamblado(
        
        Group grupo,
        
        SofaPartPMMD.PartDimensions dimensiones,
        
        double largoTotal,
        
        double fondoTotal,
        
        double altoTotal
    ) {}

    public String resumen() {
        return "Ensamblador con " + partes.size() + " partes, layout " + layout;
    }
}
