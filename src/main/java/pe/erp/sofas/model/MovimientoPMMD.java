package pe.erp.sofas.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;



@Entity(name = "Movimiento")
@Table(name = "movimiento", indexes = @Index(columnList = "material_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class MovimientoPMMD {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "material_id")
    private MaterialPMMD material;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMovimientoPMMD tipo;

    @Column(nullable = false)
    private Double cantidad;

    private String referencia;

    private String concepto;



    private Double saldo;

    private String usuario;



    public double getEfecto() {
        return switch (tipo) {
            case ENTRADA -> cantidad;
            case SALIDA, CONSUMO_OP -> -cantidad;
            case AJUSTE -> cantidad;
            default -> 0;
        };
    }
}
