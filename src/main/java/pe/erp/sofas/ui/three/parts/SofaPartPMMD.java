package pe.erp.sofas.ui.three.parts;

import javafx.geometry.Bounds;
import javafx.scene.Group;
import java.util.Map;








public interface SofaPartPMMD {
    






    
    Group build();





    
    PartDimensions getDimensions();






    
    Map<String, Punto3D> getAnchors();




    
    String getName();





    record PartDimensions(
        
        double ancho,
        
        double fondo,
        
        double alto
    ) {}





    record Punto3D(
     double x, 
       double y, 
         double z) {}
}
