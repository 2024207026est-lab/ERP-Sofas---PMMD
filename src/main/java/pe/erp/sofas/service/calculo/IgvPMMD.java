package pe.erp.sofas.service.calculo;

import java.math.BigDecimal;
import java.math.RoundingMode;



public final class IgvPMMD {

    public static final BigDecimal TASA = new BigDecimal("0.18");

    private static final BigDecimal FACTORPMMD = BigDecimal.ONE.add(TASA);

    private IgvPMMD() {
    }

    public static BigDecimal subtotal(BigDecimal totalPMMD) {
        return totalPMMD.divide(FACTORPMMD, 2, RoundingMode.HALF_UP);
    }

    public static BigDecimal igv(BigDecimal totalPMMD) {
        return totalPMMD.setScale(2, RoundingMode.HALF_UP).subtract(subtotal(totalPMMD));
    }
}
