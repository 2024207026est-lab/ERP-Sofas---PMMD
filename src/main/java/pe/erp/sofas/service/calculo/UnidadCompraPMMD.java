package pe.erp.sofas.service.calculo;






public final class UnidadCompraPMMD {

    private UnidadCompraPMMD() {
    }

    public static double paso(String unidadPMMD) {
        if (unidadPMMD == null) return 1;
        return switch (unidadPMMD) {
            case "pt" -> 50;
            case "m" -> 10;
            case "unidad" -> 10;
            case "kg" -> 5;
            default -> 1;
        };
    }

    public static double redondear(String unidadPMMD, double cantidadPMMD) {
        if (cantidadPMMD <= 0) return 0;
        double paso = paso(unidadPMMD);
        return Math.ceil(cantidadPMMD / paso - 1e-9) * paso;
    }
}
