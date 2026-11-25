package pe.erp.sofas.service.calculo;

import pe.erp.sofas.model.FichaTecnicaPMMD;
import pe.erp.sofas.model.MaterialPMMD;



public interface CatalogoPMMD {
    

    
    FichaTecnicaPMMD fichaAprobada(String codigoPMMD);

    MaterialPMMD material(String codigoPMMD);
}
