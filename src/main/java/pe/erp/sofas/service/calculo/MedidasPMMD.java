package pe.erp.sofas.service.calculo;

import pe.erp.sofas.model.FichaTecnicaPMMD;



public record MedidasPMMD(
  double largo, 
    double fondo, 
      double alto, 
        int cuerpos) {

    public static MedidasPMMD de(FichaTecnicaPMMD fPMMD) {
        return new MedidasPMMD(nzPMMD(fPMMD.getLargo()), nzPMMD(fPMMD.getFondo()), nzPMMD(fPMMD.getAlto()), fPMMD.getCuerpos() == null ? 1 : fPMMD.getCuerpos());
    }

    private static double nzPMMD(Integer vPMMD) {
        return vPMMD == null ? 0 : vPMMD;
    }

    public boolean igual(MedidasPMMD oPMMD) {
        return oPMMD != null && Math.abs(largo - oPMMD.largo) < 0.01 && Math.abs(fondo - oPMMD.fondo) < 0.01
                && Math.abs(alto - oPMMD.alto) < 0.01 && cuerpos == oPMMD.cuerpos;
    }

    public String texto() {
        return Math.round(largo) + " × " + Math.round(fondo) + " × " + Math.round(alto) + " cm";
    }
}
