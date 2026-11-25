package pe.erp.sofas.model;

import jakarta.persistence.*;
import lombok.*;



@Entity(name = "Tela")
@Table(name = "tela")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
public class TelaPMMD {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoTelaPMMD tipo;



    @Column(nullable = false)
    private String color;

    @Column(nullable = false, length = 9)
    private String hex;



    private String codigoProveedor;

    @Builder.Default
    @Column(nullable = false)
    private Double stock = 0d;

    @Builder.Default
    @Column(nullable = false)
    private Double minimo = 8d;

    @Builder.Default
    @Column(nullable = false)
    private Boolean descontinuada = false;



    public String getEstado() {
        if (Boolean.TRUE.equals(descontinuada)) return "Descontinuada";
        if (stock <= 0) return "Agotado";
        if (stock < minimo) return "Stock bajo";
        return "Disponible";
    }

    public String getNombre() {
        return tipo.nombre() + " " + color;
    }

    @Override
    public String toString() {
        return getNombre();
    }
}
