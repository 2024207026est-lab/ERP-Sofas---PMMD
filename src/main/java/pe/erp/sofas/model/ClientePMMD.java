package pe.erp.sofas.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity(name = "Cliente")
@Table(name = "cliente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
public class ClientePMMD {

    public enum Tipo { 
        PERSONA, 
         EMPRESA;

        public String nombre() {
            return this == PERSONA ? "Persona" : "Empresa";
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Tipo tipo;

    @Column(nullable = false)
    private String nombre;



    @Column(length = 11)
    private String documento;

    private String telefono;

    private String email;

    private String direccion;

    private String ciudad;

    private LocalDate fechaRegistro;

    public boolean tieneRuc() {
        return documento != null && documento.length() == 11;
    }

    public String getTipoDocumento() {
        return tieneRuc() ? "RUC" : "DNI";
    }

    @Override
    public String toString() {
        return nombre;
    }
}
