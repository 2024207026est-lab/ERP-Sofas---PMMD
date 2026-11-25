package pe.erp.sofas.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.erp.sofas.core.NegocioExceptionPMMD;
import pe.erp.sofas.model.MaterialPMMD;
import pe.erp.sofas.model.OrdenCompraPMMD;
import pe.erp.sofas.model.ProveedorPMMD;
import pe.erp.sofas.repository.MaterialRepositoryPMMD;
import pe.erp.sofas.repository.OrdenCompraRepositoryPMMD;
import pe.erp.sofas.repository.ProveedorRepositoryPMMD;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProveedorServicePMMD {

    private final ProveedorRepositoryPMMD repositoryPMMD;

    private final MaterialRepositoryPMMD materialRepositoryPMMD;

    private final OrdenCompraRepositoryPMMD compraRepositoryPMMD;

    public List<ProveedorPMMD> listar() {
        return repositoryPMMD.findAllByOrderByCodigoAsc();
    }

    public List<ProveedorPMMD> activos() {
        return listar().stream().filter(ProveedorPMMD::getActivo).toList();
    }

    public ProveedorPMMD proveedor(Long idPMMD) {
        return repositoryPMMD.findById(idPMMD).orElseThrow(() -> new NegocioExceptionPMMD("El proveedor ya no existe."));
    }

    public List<MaterialPMMD> materiales(ProveedorPMMD pPMMD) {
        return materialRepositoryPMMD.findAllByOrderByCodigoAsc().stream()
                .filter(mPMMD -> mPMMD.getProveedor() != null && mPMMD.getProveedor().getId().equals(pPMMD.getId())).toList();
    }

    public List<OrdenCompraPMMD> compras(ProveedorPMMD pPMMD) {
        return compraRepositoryPMMD.findAllByOrderByCodigoDesc().stream().filter(oPMMD -> oPMMD.getProveedor().getId().equals(pPMMD.getId())).toList();
    }

    @Transactional
    public ProveedorPMMD guardar(ProveedorPMMD pPMMD) {
        if (pPMMD.getRazonSocial() == null || pPMMD.getRazonSocial().isBlank()) throw new NegocioExceptionPMMD("La razón social es obligatoria.");
        if (pPMMD.getRuc() == null || !pPMMD.getRuc().matches("\\d{11}")) throw new NegocioExceptionPMMD("El RUC debe tener 11 dígitos.");
        boolean duplicadoPMMD = listar().stream().anyMatch(xPMMD -> xPMMD.getRuc() != null && xPMMD.getRuc().equals(pPMMD.getRuc()) && !xPMMD.getId().equals(pPMMD.getId()));
        if (duplicadoPMMD) throw new NegocioExceptionPMMD("Ya existe un proveedor con ese RUC.");
        if (pPMMD.getId() == null) pPMMD.setCodigo(CodigosPMMD.siguiente(repositoryPMMD.findAll(), ProveedorPMMD::getCodigo, "PR-", 2));
        return repositoryPMMD.save(pPMMD);
    }
}
