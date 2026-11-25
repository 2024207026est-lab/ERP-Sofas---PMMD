package pe.erp.sofas.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity(name = "ConfiguracionGuardada")
@Table(name = "configuracion_guardada")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
public class ConfiguracionGuardadaPMMD {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "usuario_id")
    private UsuarioPMMD usuario;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConfigSofaPMMD.Modelo modelo;

    private Integer cuerpos;

    @Enumerated(EnumType.STRING)
    private ConfigSofaPMMD.Chaise chaise;

    private Integer profundidad;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tela_id")
    private TelaPMMD tela;

    @Enumerated(EnumType.STRING)
    private ConfigSofaPMMD.Brazo brazo;

    @Enumerated(EnumType.STRING)
    private ConfigSofaPMMD.Patas patas;

    @Enumerated(EnumType.STRING)
    private ConfigSofaPMMD.AcabadoMadera acabado;

    @Enumerated(EnumType.STRING)
    private ConfigSofaPMMD.Espuma espuma;

    private Integer cojines;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tela_cojines_id")
    private TelaPMMD telaCojines;

    @Enumerated(EnumType.STRING)
    private ConfigSofaPMMD.Mecanismo mecanismo;

    @Builder.Default
    @Column(nullable = false)
    private Boolean esFavorito = false;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime fechaModificacion = LocalDateTime.now();

    @Column(length = 50)
    private String etiqueta;

    @PreUpdate
    void preUpdate() {
        this.fechaModificacion = LocalDateTime.now();
    }

    public ConfigSofaPMMD toConfigSofa() {
        return new ConfigSofaPMMD(
            modelo,
            cuerpos != null ? cuerpos : 1,
            chaise != null ? chaise : ConfigSofaPMMD.Chaise.NINGUNA,
            profundidad != null ? profundidad : 92,
            tela != null ? tela.getId() : null,
            brazo != null ? brazo : ConfigSofaPMMD.Brazo.RECTO,
            patas != null ? patas : ConfigSofaPMMD.Patas.MADERA,
            acabado != null ? acabado : ConfigSofaPMMD.AcabadoMadera.NATURAL,
            espuma != null ? espuma : ConfigSofaPMMD.Espuma.D23,
            cojines != null ? cojines : 1,
            telaCojines != null ? telaCojines.getId() : null,
            mecanismo != null ? mecanismo : ConfigSofaPMMD.Mecanismo.FIJO
        );
    }

    public static ConfiguracionGuardadaPMMD fromConfigSofa(ConfigSofaPMMD configPMMD, UsuarioPMMD usuario, String nombre, String descripcion) {
        return ConfiguracionGuardadaPMMD.builder()
            .usuario(usuario)
            .nombre(nombre)
            .descripcion(descripcion)
            .modelo(configPMMD.modelo())
            .cuerpos(configPMMD.cuerpos())
            .chaise(configPMMD.chaise())
            .profundidad(configPMMD.profundidad())
            .brazo(configPMMD.brazo())
            .patas(configPMMD.patas())
            .acabado(configPMMD.acabado())
            .espuma(configPMMD.espuma())
            .cojines(configPMMD.cojines())
            .mecanismo(configPMMD.mecanismo())
            .build();
    }
}
