package pe.erp.sofas.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity(name = "ReclamoGarantia")
@Table(name = "reclamo_garantia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
public class ReclamoGarantiaPMMD {

    public static final java.util.List<String> FALLAS = java.util.List.of(
            "Pata floja", "Estructura quebrada", "Mecanismo trabado", "Espuma hundida", "Costura abierta",
            "Tela con pilling", "Decoloración de tela", "Cierre roto");

    public static final java.util.List<String> SOLUCIONES = java.util.List.of(
            "Por definir", "Reparación en domicilio", "Reparación en taller", "Cambio de espuma",
            "Cambio de funda", "Reemplazo del mecanismo", "Cambio de producto", "No procede");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String codigo;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "cliente_id")
    private ClientePMMD cliente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id")
    private ProductoPMMD producto;

    private LocalDate fechaCompra;

    @Column(nullable = false)
    private String falla;

    @Column(length = 600)
    private String descripcion;



    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoReclamoPMMD estado;

    @Column(length = 600)
    private String diagnostico;

    private String solucion;

    @Builder.Default
    @Column(precision = 12, scale = 2)
    private BigDecimal costo = BigDecimal.ZERO;

    private LocalDate fechaCierre;

    public String getProductoNombre() {
        return producto == null ? "—" : producto.getNombre();
    }
}
