package pe.erp.sofas.model;

import jakarta.persistence.*;
import lombok.*;



@Entity(name = "Reserva")
@Table(name = "reserva", uniqueConstraints = @UniqueConstraint(columnNames = {"op_id", "material_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class ReservaPMMD {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "op_id")
    private OrdenProduccionPMMD op;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "material_id")
    private MaterialPMMD material;

    @Column(nullable = false)
    private Double cantidad;
}
