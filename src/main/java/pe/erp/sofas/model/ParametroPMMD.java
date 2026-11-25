package pe.erp.sofas.model;

import jakarta.persistence.*;
import lombok.*;



@Entity(name = "Parametro")
@Table(name = "parametro")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParametroPMMD {

    @Id
    @Column(length = 60)
    private String clave;

    @Column(nullable = false)
    private String valor;
}
