package pe.erp.sofas.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "Carrito")
@Table(name = "carrito")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
public class CarritoPMMD {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "usuario_id")
    private UsuarioPMMD usuario;

    @Builder.Default
    @OneToMany(mappedBy = "carrito", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("fechaAnadido ASC")
    private List<CarritoItemPMMD> items = new ArrayList<>();

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime fechaModificacion = LocalDateTime.now();

    @PreUpdate
    void preUpdate() {
        this.fechaModificacion = LocalDateTime.now();
    }

    public void agregarItem(ConfiguracionGuardadaPMMD configPMMD, Integer cantidadPMMD) {
        CarritoItemPMMD itemPMMD = CarritoItemPMMD.builder()
            .carrito(this)
            .configuracion(configPMMD)
            .cantidad(cantidadPMMD)
            .build();
        items.add(itemPMMD);
    }

    public void removerItem(Long itemIdPMMD) {
        items.removeIf(itemPMMD -> itemPMMD.getId().equals(itemIdPMMD));
    }

    public void actualizarCantidad(Long itemIdPMMD, Integer nuevaCantidadPMMD) {
        items.stream()
            .filter(itemPMMD -> itemPMMD.getId().equals(itemIdPMMD))
            .findFirst()
            .ifPresent(itemPMMD -> itemPMMD.setCantidad(nuevaCantidadPMMD));
    }

    public void vaciar() {
        items.clear();
    }

    public Integer obtenerCantidadTotal() {
        return items.stream()
            .mapToInt(CarritoItemPMMD::getCantidad)
            .sum();
    }

    public Integer obtenerCantidadItems() {
        return items.size();
    }
}
