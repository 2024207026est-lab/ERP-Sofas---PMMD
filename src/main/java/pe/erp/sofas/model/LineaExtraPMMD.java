package pe.erp.sofas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;



@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LineaExtraPMMD {

    @Column(name = "material_codigo", length = 20)
    private String materialCodigo;

    @Column(length = 20)
    private String componente;

    private Double cantidad;
}
