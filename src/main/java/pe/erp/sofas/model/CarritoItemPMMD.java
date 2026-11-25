package pe.erp.sofas.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity(name = "CarritoItem")
@Table(name = "carrito_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
public class CarritoItemPMMD {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "carrito_id")
    private CarritoPMMD carrito;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "configuracion_id")
    private ConfiguracionGuardadaPMMD configuracion;

    @Column(nullable = false)
    private Integer cantidad;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaAnadido = LocalDateTime.now();

    private String notas;

    public CarritoItemPMMD copiaConCantidad(Integer nuevaCantidadPMMD) {
        return toBuilder()
            .id(null)
            .cantidad(nuevaCantidadPMMD)
            .build();
    }
}
