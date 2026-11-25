package pe.erp.sofas.service;

import java.util.Collection;
import java.util.function.Function;



public final class CodigosPMMD {

    private CodigosPMMD() {
    }

    public static <T> String siguiente(Collection<T> existentesPMMD, Function<T, String> codigoPMMD, String prefijoPMMD, int digitosPMMD) {
        int maxPMMD = existentesPMMD.stream()
                .map(codigoPMMD)
                .filter(cPMMD -> cPMMD != null && cPMMD.startsWith(prefijoPMMD))
                .map(cPMMD -> cPMMD.substring(prefijoPMMD.length()).replaceAll("\\D", ""))
                .filter(sPMMD -> !sPMMD.isEmpty())
                .mapToInt(Integer::parseInt)
                .max()
                .orElse(0);
        return prefijoPMMD + String.format("%0" + digitosPMMD + "d", maxPMMD + 1);
    }
}
