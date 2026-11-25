package pe.erp.sofas.model;

import jakarta.persistence.*;
import lombok.*;

@Entity(name = "Empresa")
@Table(name = "empresa")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmpresaPMMD {

    @Id
    private Long id;

    private String razonSocial;

    private String nombreComercial;

    private String ruc;

    private String direccion;

    private String ciudad;

    private String telefono;

    private String email;

    private String web;


    private String logo;
}
