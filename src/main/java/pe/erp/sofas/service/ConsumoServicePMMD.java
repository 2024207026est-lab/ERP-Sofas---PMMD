package pe.erp.sofas.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.erp.sofas.core.NegocioExceptionPMMD;
import pe.erp.sofas.model.ConsumoRealPMMD;
import pe.erp.sofas.model.FichaTecnicaPMMD;
import pe.erp.sofas.repository.ConsumoRealRepositoryPMMD;

import java.util.*;
import java.util.stream.Collectors;



@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConsumoServicePMMD {

    public record Ranking(
      String nombre, 
        double desvioPct, 
          double valor, 
            int ordenes) {
    }

    public record Kpis(
      double desvioPromedioPct, 
        double costoSobremerma, 
          long ordenes, 
            String materialMayorDesvio,
                       
                       double desvioMaterialPct) {
    }

    private final ConsumoRealRepositoryPMMD repositoryPMMD;

    private final FichaServicePMMD fichaServicePMMD;

    public List<ConsumoRealPMMD> registros() {
        return repositoryPMMD.findAllByOrderByFechaDescIdDesc();
    }

    public List<ConsumoRealPMMD> filtrar(String opPMMD, String productoPMMD) {
        return registros().stream()
                .filter(cPMMD -> opPMMD == null || opPMMD.equals(cPMMD.getOpCodigo()))
                .filter(cPMMD -> productoPMMD == null || productoPMMD.equals(cPMMD.getProducto()))
                .toList();
    }

    @Transactional
    public ConsumoRealPMMD editarReal(Long idPMMD, double realPMMD) {
        if (realPMMD < 0) throw new NegocioExceptionPMMD("El consumo real no puede ser negativo.");
        ConsumoRealPMMD cPMMD = repositoryPMMD.findById(idPMMD).orElseThrow();
        cPMMD.setRealConsumido(realPMMD);
        return repositoryPMMD.save(cPMMD);
    }



    public static double desvioPct(Collection<ConsumoRealPMMD> lPMMD) {
        double estPMMD = lPMMD.stream().mapToDouble(cPMMD -> cPMMD.getEstimado() * cPMMD.getMaterial().getCosto()).sum();
        double realPMMD = lPMMD.stream().mapToDouble(cPMMD -> cPMMD.getRealConsumido() * cPMMD.getMaterial().getCosto()).sum();
        return estPMMD == 0 ? 0 : (realPMMD - estPMMD) * 100 / estPMMD;
    }

    public Kpis kpis(List<ConsumoRealPMMD> lPMMD) {
        double sobremerma = lPMMD.stream().filter(cPMMD -> cPMMD.getDiferencia() > 0).mapToDouble(ConsumoRealPMMD::getDiferenciaValor).sum();
        Map<String, List<ConsumoRealPMMD>> porMatPMMD = lPMMD.stream().collect(Collectors.groupingBy(cPMMD -> cPMMD.getMaterial().getNombre()));
        var peorPMMD = porMatPMMD.entrySet().stream().max(Comparator.comparingDouble(ePMMD -> desvioPct(ePMMD.getValue())));
        return new Kpis(desvioPct(lPMMD), sobremerma, lPMMD.stream().map(ConsumoRealPMMD::getOpCodigo).distinct().count(),
                peorPMMD.map(Map.Entry::getKey).orElse("—"), peorPMMD.map(ePMMD -> desvioPct(ePMMD.getValue())).orElse(0d));
    }

    public List<Ranking> porTrabajador(List<ConsumoRealPMMD> lPMMD) {
        return rankingPMMD(lPMMD, ConsumoRealPMMD::getResponsable);
    }

    public List<Ranking> porProducto(List<ConsumoRealPMMD> lPMMD) {
        return rankingPMMD(lPMMD, ConsumoRealPMMD::getProducto);
    }

    private List<Ranking> rankingPMMD(List<ConsumoRealPMMD> lPMMD, java.util.function.Function<ConsumoRealPMMD, String> clavePMMD) {
        Map<String, List<ConsumoRealPMMD>> gPMMD = lPMMD.stream().collect(Collectors.groupingBy(clavePMMD, LinkedHashMap::new, Collectors.toList()));
        List<Ranking> rPMMD = new ArrayList<>();
        gPMMD.forEach((k, vPMMD) -> rPMMD.add(new Ranking(k, desvioPct(vPMMD), vPMMD.stream().mapToDouble(ConsumoRealPMMD::getDiferenciaValor).sum(),
                (int) vPMMD.stream().map(ConsumoRealPMMD::getOpCodigo).distinct().count())));
        rPMMD.sort(Comparator.comparingDouble(Ranking::desvioPct).reversed());
        return rPMMD;
    }






    @Transactional
    public FichaTecnicaPMMD ajustarFicha(String codigoFichaPMMD, List<ConsumoRealPMMD> basePMMD) {
        FichaTecnicaPMMD fPMMD = fichaServicePMMD.aprobada(codigoFichaPMMD);
        if (fPMMD == null) throw new NegocioExceptionPMMD("No hay ficha aprobada " + codigoFichaPMMD + " para ajustar.");
        Map<String, double[]> accPMMD = new HashMap<>();
        for (ConsumoRealPMMD cPMMD : basePMMD) {
            if (cPMMD.getFichaUid() == null || !cPMMD.getFichaUid().startsWith(codigoFichaPMMD + "v")) continue;
            double[] aPMMD = accPMMD.computeIfAbsent(cPMMD.getMaterial().getCodigo(), k -> new double[2]);
            aPMMD[0] += cPMMD.getRealConsumido();
            aPMMD[1] += cPMMD.getEstimado();
        }
        if (accPMMD.isEmpty()) throw new NegocioExceptionPMMD("No hay consumos registrados con la ficha " + codigoFichaPMMD + ".");
        Map<String, Double> factoresPMMD = new HashMap<>();
        accPMMD.forEach((k, aPMMD) -> {
            if (aPMMD[1] > 0) factoresPMMD.put(k, aPMMD[0] / aPMMD[1]);
        });
        return fichaServicePMMD.ajustarConFactores(fPMMD, factoresPMMD);
    }



    public Optional<String> fichaComun(List<ConsumoRealPMMD> lPMMD) {
        Set<String> codigosPMMD = lPMMD.stream().map(ConsumoRealPMMD::getFichaUid).filter(Objects::nonNull)
                .map(uPMMD -> uPMMD.substring(0, uPMMD.indexOf('v'))).collect(Collectors.toSet());
        return codigosPMMD.size() == 1 ? Optional.of(codigosPMMD.iterator().next()) : Optional.empty();
    }
}
