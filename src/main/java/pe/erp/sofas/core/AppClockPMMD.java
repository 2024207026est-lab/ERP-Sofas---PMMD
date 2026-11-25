package pe.erp.sofas.core;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;






public final class AppClockPMMD {
    


    public static final LocalDate HOY = LocalDate.of(2026, 10, 15);

    public static final LocalTime HORA = LocalTime.of(10, 30);

    public static final Locale PERU = Locale.forLanguageTag("es-PE");

    public static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static final DateTimeFormatter FECHA_CORTA = DateTimeFormatter.ofPattern("dd/MM");

    private static final DateTimeFormatter LARGAPMMD = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", PERU);

    private static final DateTimeFormatter DIA_MESPMMD = DateTimeFormatter.ofPattern("d MMM", PERU);

    private AppClockPMMD() {
    }

    public static LocalDate hoy() {
        return HOY;
    }

    public static String horaActual() {
        return HORA.format(DateTimeFormatter.ofPattern("HH:mm"));
    }



    public static String fechaLarga() {
        return capitalizar(HOY.format(LARGAPMMD));
    }

    public static String fmt(LocalDate fechaPMMD) {
        return fechaPMMD == null ? "—" : fechaPMMD.format(FECHA);
    }

    public static String corta(LocalDate fechaPMMD) {
        return fechaPMMD == null ? "—" : fechaPMMD.format(FECHA_CORTA);
    }



    public static String diaMes(LocalDate fechaPMMD) {
        return fechaPMMD == null ? "—" : fechaPMMD.format(DIA_MESPMMD).replace(".", "");
    }



    public static String mesCorto(int mesPMMD) {
        String mPMMD = capitalizar(java.time.Month.of(mesPMMD).getDisplayName(java.time.format.TextStyle.SHORT, PERU)).replace(".", "");
        return mPMMD.equals("Set") ? "Sep" : mPMMD;
    }

    public static String mesLargo(int mesPMMD) {
        return capitalizar(java.time.Month.of(mesPMMD).getDisplayName(java.time.format.TextStyle.FULL, PERU));
    }

    public static String capitalizar(String sPMMD) {
        return sPMMD == null || sPMMD.isEmpty() ? sPMMD : Character.toUpperCase(sPMMD.charAt(0)) + sPMMD.substring(1);
    }
}
