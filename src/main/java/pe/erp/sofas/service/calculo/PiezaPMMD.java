package pe.erp.sofas.service.calculo;

import pe.erp.sofas.model.ComponentePMMD;






public record PiezaPMMD(
  String nombre, 
    ComponentePMMD componente, 
      double ancho, 
        double alto, 
          int cantidad,
                    
                    boolean girable, 
                      String origen) {

    public PiezaPMMD conCantidad(int n) {
        return new PiezaPMMD(nombre, componente, ancho, alto, n, girable, origen);
    }

    public PiezaPMMD conOrigen(String o) {
        return new PiezaPMMD(nombre, componente, ancho, alto, cantidad, girable, o);
    }

    public PiezaPMMD conGiro(boolean g) {
        return new PiezaPMMD(nombre, componente, ancho, alto, cantidad, g, origen);
    }

    public double area() {
        return ancho * alto;
    }
}
