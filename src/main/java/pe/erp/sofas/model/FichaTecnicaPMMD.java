package pe.erp.sofas.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;






@Entity(name = "FichaTecnica")
@Table(name = "ficha_tecnica", uniqueConstraints = @UniqueConstraint(columnNames = {"codigo", "version"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
public class FichaTecnicaPMMD {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String codigo;

    @Column(nullable = false)
    private Integer version;

    @Column(nullable = false, unique = true, length = 16)
    private String uid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EstadoFichaPMMD estado;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id")
    private ProductoPMMD producto;

    @Column(nullable = false)
    private String nombre;



    private Integer largo;

    private Integer fondo;

    private Integer alto;

    @Builder.Default
    private Integer cuerpos = 3;

    @Builder.Default
    private Boolean chaise = false;



    private String tipoCorte;

    @Builder.Default
    private Double indirectosPct = 6d;

    private String aprobadoPor;

    private LocalDate fechaAprobacion;

    @Column(length = 500)
    private String notas;

    @Builder.Default
    private Boolean compuesta = false;



    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "ficha_hijo", joinColumns = @JoinColumn(name = "ficha_id"))
    @OrderColumn(name = "orden")
    @Column(name = "codigo_hijo", length = 10)
    @Fetch(FetchMode.SUBSELECT)
    private List<String> hijos = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "ficha", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("orden ASC")
    @Fetch(FetchMode.SUBSELECT)
    private List<LineaBOMPMMD> lineas = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "ficha", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("orden ASC")
    @Fetch(FetchMode.SUBSELECT)
    private List<OperacionMOPMMD> operaciones = new ArrayList<>();

    public static String uid(String codigo, int version) {
        return codigo + "v" + version;
    }



    public String getEtiqueta() {
        return codigo + " v" + version;
    }

    public boolean isEditable() {
        return estado == EstadoFichaPMMD.BORRADOR;
    }

    public void agregarLinea(LineaBOMPMMD lPMMD) {
        lPMMD.setFicha(this);
        lPMMD.setOrden(lineas.size());
        lineas.add(lPMMD);
    }

    public void agregarOperacion(OperacionMOPMMD oPMMD) {
        oPMMD.setFicha(this);
        oPMMD.setOrden(operaciones.size());
        operaciones.add(oPMMD);
    }

    public double getHorasMO() {
        return operaciones.stream().mapToDouble(OperacionMOPMMD::getHoras).sum();
    }

    @Override
    public String toString() {
        return getEtiqueta() + " · " + nombre;
    }
}
