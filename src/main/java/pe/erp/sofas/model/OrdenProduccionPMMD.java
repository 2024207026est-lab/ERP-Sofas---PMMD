package pe.erp.sofas.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import pe.erp.sofas.core.AppClockPMMD;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Entity(name = "OrdenProduccion")
@Table(name = "orden_produccion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
public class OrdenProduccionPMMD {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 12)
    private String codigo;



    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id")
    private ProductoPMMD producto;



    private String nombrePersonalizado;

    @Builder.Default
    @Column(nullable = false)
    private Integer cantidad = 1;



    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_id")
    private ClientePMMD cliente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "responsable_id")
    private EmpleadoPMMD responsable;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 10)
    private PrioridadPMMD prioridad = PrioridadPMMD.MEDIA;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 20)
    private EtapaPMMD etapa = EtapaPMMD.PENDIENTE;

    private LocalDate fechaInicio;

    private LocalDate fechaEntrega;


    private LocalDate fechaFin;

    @Column(precision = 12, scale = 2)
    private BigDecimal costo;



    @Column(length = 16)
    private String fichaUid;



    private Integer largo;

    private Integer fondo;

    private Integer alto;

    private Integer cuerpos;



    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "op_sustitucion", joinColumns = @JoinColumn(name = "op_id"))
    @MapKeyColumn(name = "original", length = 20)
    @Column(name = "elegido", length = 20)
    @Fetch(FetchMode.SUBSELECT)
    private Map<String, String> sustituciones = new HashMap<>();



    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "op_extra", joinColumns = @JoinColumn(name = "op_id"))
    @Fetch(FetchMode.SUBSELECT)
    private List<LineaExtraPMMD> extras = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tela_id")
    private TelaPMMD tela;



    @Column(length = 300)
    private String autorizacion;

    @Column(length = 12)
    private String pedidoCodigo;

    @Column(length = 12)
    private String retapizadoCodigo;



    @Column(length = 600)
    private String especificacion;

    @Builder.Default
    @Column(nullable = false)
    private Boolean anulada = false;

    public String getProductoNombre() {
        if (nombrePersonalizado != null && !nombrePersonalizado.isBlank()) return nombrePersonalizado;
        return producto == null ? "—" : producto.getNombre();
    }

    public boolean isRetrasada() {
        return etapa != EtapaPMMD.LISTO && !Boolean.TRUE.equals(anulada)
                && fechaEntrega != null && fechaEntrega.isBefore(AppClockPMMD.hoy());
    }

    public boolean isActiva() {
        return !Boolean.TRUE.equals(anulada) && etapa != EtapaPMMD.LISTO;
    }

    public String getClienteNombre() {
        return cliente == null ? "Stock de tienda" : cliente.getNombre();
    }

    public boolean isSinFicha() {
        return fichaUid == null;
    }

    public String getEstado() {
        if (Boolean.TRUE.equals(anulada)) return "Cancelado";
        if (etapa == EtapaPMMD.LISTO) return "Listo";
        if (isRetrasada()) return "Retrasado";
        if (etapa == EtapaPMMD.PENDIENTE) return "Pendiente";
        return "En producción";
    }

    public String getTelaNombre() {
        return tela == null ? "Según ficha" : tela.getNombre();
    }

    @Override
    public String toString() {
        return codigo + " · " + getProductoNombre();
    }
}
