package pe.erp.sofas.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.erp.sofas.model.EstadoFichaPMMD;
import pe.erp.sofas.model.FichaTecnicaPMMD;
import pe.erp.sofas.model.MaterialPMMD;
import pe.erp.sofas.repository.FichaTecnicaRepositoryPMMD;
import pe.erp.sofas.repository.MaterialRepositoryPMMD;
import pe.erp.sofas.service.calculo.CatalogoPMMD;



@Service
@RequiredArgsConstructor
public class CatalogoServicePMMD implements CatalogoPMMD {

    private final FichaTecnicaRepositoryPMMD fichaRepositoryPMMD;

    private final MaterialRepositoryPMMD materialRepositoryPMMD;

    @Override
    public FichaTecnicaPMMD fichaAprobada(String codigoPMMD) {
        return fichaRepositoryPMMD.findByCodigoOrderByVersionDesc(codigoPMMD).stream()
                .filter(fPMMD -> fPMMD.getEstado() == EstadoFichaPMMD.APROBADA).findFirst().orElse(null);
    }

    @Override
    public MaterialPMMD material(String codigoPMMD) {
        return materialRepositoryPMMD.findByCodigo(codigoPMMD).orElse(null);
    }
}
