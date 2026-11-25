package pe.erp.sofas.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.erp.sofas.core.NegocioExceptionPMMD;
import pe.erp.sofas.model.*;
import pe.erp.sofas.repository.*;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductoServicePMMD {
    


    public record CostoEstandar(
      FichaTecnicaPMMD ficha, 
        double costo, 
          double margenPct) {
    }

    private final ProductoRepositoryPMMD repositoryPMMD;

    private final FichaServicePMMD fichaServicePMMD;

    private final OrdenProduccionRepositoryPMMD opRepositoryPMMD;

    private final PedidoRepositoryPMMD pedidoRepositoryPMMD;

    public List<ProductoPMMD> listar() {
        return repositoryPMMD.findAllByOrderByCodigoAsc().stream().filter(ProductoPMMD::getActivo).toList();
    }

    public ProductoPMMD producto(Long idPMMD) {
        return repositoryPMMD.findById(idPMMD).orElseThrow(() -> new NegocioExceptionPMMD("El modelo ya no existe."));
    }

    public Optional<ProductoPMMD> porCodigo(String codigoPMMD) {
        return repositoryPMMD.findByCodigo(codigoPMMD);
    }

    public String siguienteCodigo() {
        return CodigosPMMD.siguiente(repositoryPMMD.findAll(), ProductoPMMD::getCodigo, "SF-", 3);
    }

    public CostoEstandar costoEstandar(ProductoPMMD pPMMD) {
        FichaTecnicaPMMD f = fichaServicePMMD.aprobadaDe(pPMMD);
        if (f == null) return null;
        double costo = fichaServicePMMD.costoEstandar(f);
        double precioPMMD = pPMMD.getPrecio().doubleValue();
        return new CostoEstandar(f, costo, precioPMMD == 0 ? 0 : (precioPMMD - costo) * 100 / precioPMMD);
    }

    @Transactional
    public ProductoPMMD guardar(ProductoPMMD pPMMD) {
        validar(pPMMD);
        if (pPMMD.getId() == null) pPMMD.setCodigo(siguienteCodigo());
        return repositoryPMMD.save(pPMMD);
    }

    public static void validar(ProductoPMMD pPMMD) {
        if (pPMMD.getNombre() == null || pPMMD.getNombre().isBlank()) throw new NegocioExceptionPMMD("El nombre del modelo es obligatorio.");
        if (pPMMD.getPrecio() == null || pPMMD.getPrecio().signum() <= 0) throw new NegocioExceptionPMMD("Ingresa el precio de venta.");
        if (pPMMD.getCosto() == null || pPMMD.getCosto().signum() < 0) throw new NegocioExceptionPMMD("Ingresa el costo.");
        if (pPMMD.getPrecio().compareTo(pPMMD.getCosto()) <= 0) throw new NegocioExceptionPMMD("El precio debe ser mayor que el costo.");
        if (pPMMD.getStock() == null || pPMMD.getStock() < 0) throw new NegocioExceptionPMMD("El stock no puede ser negativo.");
    }

    @Transactional
    public void eliminar(Long idPMMD) {
        ProductoPMMD pPMMD = producto(idPMMD);
        boolean enUsoPMMD = opRepositoryPMMD.findAll().stream().anyMatch(oPMMD -> oPMMD.getProducto() != null && oPMMD.getProducto().getId().equals(idPMMD) && oPMMD.isActiva())
                || pedidoRepositoryPMMD.findAll().stream().anyMatch(xPMMD -> xPMMD.getProducto() != null && xPMMD.getProducto().getId().equals(idPMMD) && !xPMMD.getEstado().cerrado());
        if (enUsoPMMD) throw new NegocioExceptionPMMD("El modelo tiene pedidos u órdenes activas; no se puede eliminar.");

        pPMMD.setActivo(false);
        repositoryPMMD.save(pPMMD);
    }

    @Transactional
    public ProductoPMMD fijarImagen(Long idPMMD, int indicePMMD, String rutaPMMD) {
        ProductoPMMD pPMMD = producto(idPMMD);
        pPMMD.setImagen(indicePMMD, rutaPMMD);
        return repositoryPMMD.save(pPMMD);
    }
}
