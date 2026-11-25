package pe.erp.sofas.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity(name = "Empleado")
@Table(name = "empleado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
public class EmpleadoPMMD {

    public static final java.util.List<String> AREAS = java.util.List.of(
            "Taller · Carpintería", "Taller · Costura", "Taller · Tapizado", "Taller · Espumado", "Tienda", "Almacén", "Administración");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String codigo;

    @Column(nullable = false)
    private String nombre;

    @Column(length = 8)
    private String dni;

    private String cargo;

    private String area;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RolPMMD rol;

    private String telefono;

    private LocalDate ingreso;



    @Builder.Default
    private String estado = "Activo";



    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EtapaPMMD etapa;

    private String color;

    public boolean isTaller() {
        return area != null && area.startsWith("Taller");
    }

    @Override
    public String toString() {
        return nombre;
    }
}
