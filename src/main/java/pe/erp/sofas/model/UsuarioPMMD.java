package pe.erp.sofas.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity(name = "Usuario")
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
public class UsuarioPMMD {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String username;

    @Column(nullable = false)
    private String nombre;

    private String email;



    private String cargo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RolPMMD rol;



    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    private LocalDate ultimoAcceso;

    public String getPrimerNombre() {
        return nombre.trim().split("\\s+")[0];
    }
}
