package pe.erp.sofas.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Entity(name = "Pedido")
@Table(name = "pedido")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
public class PedidoPMMD {

    public enum Tipo { 
        ESTANDAR, 
         PERSONALIZADO;

        public String nombre() {
            return this == ESTANDAR ? "Estándar" : "Personalizado";
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 12)
    private String codigo;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "cliente_id")
    private ClientePMMD cliente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id")
    private ProductoPMMD producto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Tipo tipo;

    @Builder.Default
    private Integer cantidad = 1;

    private LocalDate fecha;

    private LocalDate fechaEntrega;



    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    @Builder.Default
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal adelanto = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private EstadoPedidoPMMD estado;

    @Column(length = 12)
    private String opCodigo;

    private Integer largo;

    private Integer fondo;

    private Integer alto;



    private String material;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tela_id")
    private TelaPMMD tela;

    @Column(length = 600)
    private String observaciones;



    @Column(length = 800)
    private String configuracion;



    @Column(length = 600)
    private String especificacion;

    public BigDecimal getSaldo() {
        return precio.subtract(adelanto).max(BigDecimal.ZERO);
    }

    public double getPctPagado() {
        return precio.signum() == 0 ? 0 : adelanto.divide(precio, 4, RoundingMode.HALF_UP).doubleValue();
    }

    public boolean isRetrasado() {
        return !estado.cerrado() && estado != EstadoPedidoPMMD.LISTO && fechaEntrega != null
                && fechaEntrega.isBefore(pe.erp.sofas.core.AppClockPMMD.hoy());
    }

    public String getProductoNombre() {
        return producto == null ? "Sofá a medida" : producto.getNombre();
    }

    public String getMedidas() {
        if (largo == null || largo == 0) return producto == null ? "—" : producto.getMedidas();
        return largo + " × " + fondo + " × " + alto + " cm";
    }

    public String getColor() {
        return tela == null ? "—" : tela.getColor();
    }
}
