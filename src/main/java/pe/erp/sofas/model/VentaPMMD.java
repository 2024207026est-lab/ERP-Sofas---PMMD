package pe.erp.sofas.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;



@Entity(name = "Venta")
@Table(name = "venta", uniqueConstraints = @UniqueConstraint(columnNames = {"serie", "numero"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class VentaPMMD {

    public enum Comprobante { 
        BOLETA, 
         FACTURA;

        public String nombre() {
            return this == BOLETA ? "Boleta" : "Factura";
        }

        public String serie() {
            return this == BOLETA ? "B001" : "F001";
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 4)
    private String serie;

    @Column(nullable = false)
    private Integer numero;

    @Column(nullable = false)
    private LocalDateTime fecha;



    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_id")
    private ClientePMMD cliente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Comprobante comprobante;



    private String metodoPago;

    @Builder.Default
    @Column(precision = 12, scale = 2)
    private BigDecimal descuento = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(precision = 12, scale = 2)
    private BigDecimal recibido;

    @Column(precision = 12, scale = 2)
    private BigDecimal vuelto;

    private String vendedor;

    @Builder.Default
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    private List<ItemVentaPMMD> items = new ArrayList<>();

    public void agregar(ItemVentaPMMD itemPMMD) {
        itemPMMD.setVenta(this);
        items.add(itemPMMD);
    }

    public String getNumeroCompleto() {
        return serie + "-" + String.format("%06d", numero);
    }

    public String getClienteNombre() {
        return cliente == null ? "Cliente varios" : cliente.getNombre();
    }

    public int getUnidades() {
        return items.stream().mapToInt(ItemVentaPMMD::getCantidad).sum();
    }
}
