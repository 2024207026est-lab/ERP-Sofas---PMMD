package pe.erp.sofas.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



@Entity(name = "Producto")
@Table(name = "producto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
public class ProductoPMMD {

    public static final List<String> CATEGORIAS = List.of(
            "Sofá", "Juego de sala", "Seccional", "Sillón", "Sofá cama", "Chesterfield", "Reclinable", "Puff");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String codigo;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, length = 30)
    private String categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FormaPMMD forma;



    private String material;



    private String color;



    private String colorHex;

    @Builder.Default
    private Integer largo = 0;

    @Builder.Default
    private Integer fondo = 0;

    @Builder.Default
    private Integer alto = 0;

    @Builder.Default
    private Integer cuerpos = 3;



    private String medidasTexto;

    @Column(precision = 12, scale = 2)
    private BigDecimal costo;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    @Builder.Default
    @Column(nullable = false)
    private Integer stock = 0;

    @Column(length = 600)
    private String descripcion;



    @Column(length = 1500)
    private String imagenes;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;



    public String getEstado() {
        return estadoPara(stock);
    }

    public static String estadoPara(int stock) {
        if (stock <= 0) return "A pedido";
        if (stock <= 2) return "Stock bajo";
        return "Disponible";
    }

    public String getMedidas() {
        if (medidasTexto != null && !medidasTexto.isBlank()) return medidasTexto;
        return largo + " × " + fondo + " × " + alto + " cm";
    }

    public BigDecimal getMargen() {
        if (costo == null || precio == null || precio.signum() == 0) return BigDecimal.ZERO;
        return precio.subtract(costo).multiply(BigDecimal.valueOf(100)).divide(precio, 1, java.math.RoundingMode.HALF_UP);
    }



    public List<String> getListaImagenes() {
        List<String> lPMMD = new ArrayList<>(Arrays.asList(new String[5]));
        if (imagenes != null && !imagenes.isEmpty()) {
            String[] pPMMD = imagenes.split("\\|", -1);
            for (int iPMMD = 0; iPMMD < Math.min(5, pPMMD.length); iPMMD++) lPMMD.set(iPMMD, pPMMD[iPMMD].isBlank() ? null : pPMMD[iPMMD]);
        }
        return lPMMD;
    }

    public void setImagen(int indicePMMD, String rutaPMMD) {
        List<String> lPMMD = getListaImagenes();
        lPMMD.set(indicePMMD, rutaPMMD);
        imagenes = String.join("|", lPMMD.stream().map(sPMMD -> sPMMD == null ? "" : sPMMD).toList());
    }

    public String getImagenPrincipal() {
        return getListaImagenes().get(0);
    }

    @Override
    public String toString() {
        return nombre;
    }
}
