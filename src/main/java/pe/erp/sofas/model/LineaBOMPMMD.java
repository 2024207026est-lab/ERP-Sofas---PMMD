package pe.erp.sofas.model;

import jakarta.persistence.*;
import lombok.*;



@Entity(name = "LineaBOM")
@Table(name = "linea_bom")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
public class LineaBOMPMMD {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ficha_id")
    @ToString.Exclude
    private FichaTecnicaPMMD ficha;

    private Integer orden;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "material_id")
    private MaterialPMMD material;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ComponentePMMD componente;

    @Column(nullable = false)
    private Double cantidad;

    @Builder.Default
    @Column(nullable = false)
    private Double mermaPct = 0d;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 4)
    @Builder.Default
    private DependenciaPMMD dependencia = DependenciaPMMD.F;

    @Builder.Default
    @Column(nullable = false)
    private Boolean opcional = false;



    public LineaBOMPMMD copia() {
        return toBuilder().id(null).ficha(null).build();
    }
}
