package pe.erp.sofas.model;

import jakarta.persistence.*;
import lombok.*;



@Entity(name = "NotificacionEstado")
@Table(name = "notificacion_estado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionEstadoPMMD {

    @Id
    @Column(length = 80)
    private String clave;

    private Boolean leida;

    private Boolean descartada;
}
