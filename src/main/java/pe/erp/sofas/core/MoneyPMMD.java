package pe.erp.sofas.core;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;



public final class MoneyPMMD {

    private static final DecimalFormatSymbols SIMBOLOSPMMD = DecimalFormatSymbols.getInstance(Locale.US);

    private MoneyPMMD() {
    }

    public static String fmt(BigDecimal montoPMMD) {
        return "S/ " + dfPMMD("#,##0.00").format(montoPMMD == null ? BigDecimal.ZERO : montoPMMD);
    }

    public static String fmt(double montoPMMD) {
        return fmt(BigDecimal.valueOf(montoPMMD));
    }



    public static String fmt0(BigDecimal montoPMMD) {
        return "S/ " + dfPMMD("#,##0").format(montoPMMD == null ? BigDecimal.ZERO : montoPMMD.setScale(0, RoundingMode.HALF_UP));
    }

    public static String fmt0(double montoPMMD) {
        return fmt0(BigDecimal.valueOf(montoPMMD));
    }



    public static String corto(double montoPMMD) {
        if (Math.abs(montoPMMD) >= 1000) return "S/ " + dfPMMD("#,##0.#").format(montoPMMD / 1000) + "k";
        return "S/ " + dfPMMD("#,##0").format(montoPMMD);
    }

    public static String numero(double nPMMD) {
        return dfPMMD("#,##0.##").format(nPMMD);
    }

    public static String entero(long nPMMD) {
        return dfPMMD("#,##0").format(nPMMD);
    }

    public static BigDecimal of(String valor) {
        return new BigDecimal(valor).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal of(double valor) {
        return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP);
    }



    public static BigDecimal parse(String textoPMMD) {
        if (textoPMMD == null) return null;
        String limpio = textoPMMD.replace("S/", "").replace(",", "").trim();
        if (limpio.isEmpty()) return null;
        try {
            return new BigDecimal(limpio).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException ePMMD) {
            return null;
        }
    }

    private static DecimalFormat dfPMMD(String patron) {
        return new DecimalFormat(patron, SIMBOLOSPMMD);
    }
}
