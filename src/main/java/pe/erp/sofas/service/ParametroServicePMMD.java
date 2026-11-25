package pe.erp.sofas.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.erp.sofas.model.EtapaPMMD;
import pe.erp.sofas.model.EtapaMOPMMD;
import pe.erp.sofas.model.ParametroPMMD;
import pe.erp.sofas.repository.ParametroRepositoryPMMD;

import java.util.LinkedHashMap;
import java.util.Map;



@Service
@RequiredArgsConstructor
public class ParametroServicePMMD {

    public static final String INDIRECTOS = "produccion.indirectos";

    public static final String MARGEN_OBJETIVO = "produccion.margen";

    public static final Map<String, Double> MERMAS = new LinkedHashMap<>();

    static {
        MERMAS.put("Madera", 18d);
        MERMAS.put("Espuma", 12d);
        MERMAS.put("Tela", 10d);
        MERMAS.put("Tableros", 8d);
        MERMAS.put("Insumos", 5d);
    }



    public static final Map<EtapaPMMD, Double> DESTAJO = new LinkedHashMap<>();

    static {
        DESTAJO.put(EtapaPMMD.CORTE_MADERA, 15d);
        DESTAJO.put(EtapaPMMD.ARMADO, 45d);
        DESTAJO.put(EtapaPMMD.CINCHADO, 15d);
        DESTAJO.put(EtapaPMMD.ESPUMADO, 20d);
        DESTAJO.put(EtapaPMMD.CORTE_TELA, 18d);
        DESTAJO.put(EtapaPMMD.COSTURA, 40d);
        DESTAJO.put(EtapaPMMD.TAPIZADO, 60d);
        DESTAJO.put(EtapaPMMD.ACABADO, 15d);
        DESTAJO.put(EtapaPMMD.CONTROL_CALIDAD, 8d);
    }

    private final ParametroRepositoryPMMD repositoryPMMD;

    public double numero(String clave, double defectoPMMD) {
        return repositoryPMMD.findById(clave).map(pPMMD -> {
            try {
                return Double.parseDouble(pPMMD.getValor());
            } catch (NumberFormatException ePMMD) {
                return defectoPMMD;
            }
        }).orElse(defectoPMMD);
    }

    @Transactional
    public void fijar(String clave, double valorPMMD) {
        repositoryPMMD.save(new ParametroPMMD(clave, String.valueOf(valorPMMD)));
    }

    public double indirectos() {
        return numero(INDIRECTOS, 6);
    }

    public double margenObjetivo() {
        return numero(MARGEN_OBJETIVO, 35);
    }

    public double merma(String tipoPMMD) {
        return numero("merma." + tipoPMMD, MERMAS.getOrDefault(tipoPMMD, 5d));
    }

    public double costoHora(EtapaMOPMMD etapaPMMD) {
        return numero("costoHora." + etapaPMMD.name(), etapaPMMD.costoHora());
    }

    public double tarifaDestajo(EtapaPMMD etapaPMMD) {
        return numero("destajo." + etapaPMMD.name(), DESTAJO.getOrDefault(etapaPMMD, 0d));
    }
}
