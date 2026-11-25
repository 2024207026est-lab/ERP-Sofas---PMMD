package pe.erp.sofas.model;

import jakarta.persistence.*;
import lombok.*;



@Entity(name = "OperacionMO")
@Table(name = "operacion_mo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
public class OperacionMOPMMD {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ficha_id")
    @ToString.Exclude
    private FichaTecnicaPMMD ficha;

    private Integer orden;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EtapaMOPMMD etapa;

    @Column(nullable = false)
    private Double horas;

    @Column(nullable = false)
    private Double costoHora;

    public double getCosto() {
        return horas * costoHora;
    }

    public OperacionMOPMMD copia() {
        return toBuilder().id(null).ficha(null).build();
    }
}
