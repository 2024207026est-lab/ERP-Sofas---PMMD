package pe.erp.sofas.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;



@Entity(name = "Material")
@Table(name = "material")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
public class MaterialPMMD {

    public static final java.util.List<String> CATEGORIAS = java.util.List.of(
            "Madera", "Tableros", "Espuma", "Tapicería", "Patas", "Insumos", "Acabados", "Mecanismos", "Embalaje");



    private static final Set<String> ENTERAS = Set.of("unidad", "par", "juego");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, length = 30)
    private String categoria;

    @Column(nullable = false, length = 20)
    private String unidad;

    @Builder.Default
    @Column(nullable = false)
    private Double stock = 0d;



    @Builder.Default
    @Column(nullable = false)
    private Double reservado = 0d;

    @Builder.Default
    @Column(nullable = false)
    private Double minimo = 0d;

    @Column(nullable = false)
    private Double costo;

    private String ubicacion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "proveedor_id")
    private ProveedorPMMD proveedor;



    @Builder.Default
    @Column(nullable = false)
    private Boolean bajoPedido = false;

    public double getDisponible() {
        return Math.max(0, stock - reservado);
    }



    public String getEstado() {
        if (Boolean.TRUE.equals(bajoPedido) && stock <= 0) return "Bajo pedido";
        double dPMMD = stock - reservado;
        if (dPMMD <= 0.0001) return "Agotado";
        if (dPMMD < minimo) return "Crítico";
        if (dPMMD < minimo * 1.3) return "Stock bajo";
        return "Normal";
    }

    public boolean isCritico() {
        String ePMMD = getEstado();
        return "Agotado".equals(ePMMD) || "Crítico".equals(ePMMD);
    }

    public boolean isBajoMinimo() {
        return !Boolean.TRUE.equals(bajoPedido) && stock - reservado < minimo;
    }

    public boolean isEntera() {
        return ENTERAS.contains(unidad);
    }

    public boolean isTela() {
        return TipoTelaPMMD.porMaterial(codigo) != null;
    }

    public double getValor() {
        return costo * stock;
    }

    @Override
    public String toString() {
        return codigo + " · " + nombre;
    }
}
