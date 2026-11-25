package pe.erp.sofas.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;



@Entity(name = "Retapizado")
@Table(name = "retapizado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
public class RetapizadoPMMD {

    public static final java.util.List<String> ESTRUCTURAS = java.util.List.of("Buena", "Regular", "Requiere refuerzo");

    public static final java.util.List<String> ESPUMAS = java.util.List.of("Buena", "Hundida", "Cambio total");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String codigo;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "cliente_id")
    private ClientePMMD cliente;

    @Column(nullable = false)
    private String descripcion;

    @Builder.Default
    private Integer cuerpos = 3;

    private Integer largo;

    private Integer fondo;

    private Integer alto;



    private String estructura;



    private String espuma;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tela_id")
    private TelaPMMD tela;



    private Double metros;

    private LocalDate fechaRegistro;

    private LocalDate fechaRecojo;

    private LocalDate fechaEntrega;

    @Column(precision = 12, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoRetapizadoPMMD estado;

    @Column(length = 12)
    private String opCodigo;



    private String foto;

    public String getMedidas() {
        return (largo == null ? "—" : largo) + " × " + (fondo == null ? "—" : fondo) + " × " + (alto == null ? "—" : alto) + " cm";
    }
}
