package pe.erp.sofas.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;



@Entity(name = "ConsumoReal")
@Table(name = "consumo_real")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class ConsumoRealPMMD {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 12)
    private String opCodigo;

    private String producto;



    @Column(length = 16)
    private String fichaUid;

    private String responsable;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "material_id")
    private MaterialPMMD material;

    @Column(nullable = false)
    private Double estimado;

    @Column(nullable = false)
    private Double realConsumido;

    private LocalDate fecha;

    public double getDiferencia() {
        return realConsumido - estimado;
    }

    public double getDiferenciaPct() {
        return estimado == 0 ? 0 : (realConsumido - estimado) * 100 / estimado;
    }

    public double getDiferenciaValor() {
        return getDiferencia() * material.getCosto();
    }
}
