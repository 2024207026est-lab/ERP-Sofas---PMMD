package pe.erp.sofas.service.calculo;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.function.Function;
import pe.erp.sofas.model.DependenciaPMMD;

import java.util.EnumMap;
import java.util.Map;







public final class FormulaPMMD {
    


    private static final Function PATASPMMD = new Function("patas", 1) {

        @Override
        public double apply(double... argsPMMD) {
            return patas(argsPMMD[0]);
        }
    };

    private static final Map<DependenciaPMMD, Expression> CACHEPMMD = new EnumMap<>(DependenciaPMMD.class);

    private FormulaPMMD() {
    }

    public static int patas(double largoPMMD) {
        return largoPMMD <= 160 ? 4 : 6;
    }



    public static synchronized double factor(DependenciaPMMD depPMMD, MedidasPMMD pedidasPMMD, MedidasPMMD basePMMD) {
        if (depPMMD == null || depPMMD == DependenciaPMMD.F || pedidasPMMD == null || basePMMD == null) return 1;
        Expression ePMMD = CACHEPMMD.computeIfAbsent(depPMMD, dPMMD -> new ExpressionBuilder(dPMMD.expresion())
                .variables("L", "F", "A", "n", "L0", "F0", "A0", "n0")
                .function(PATASPMMD)
                .build());
        ePMMD.setVariable("L", pedidasPMMD.largo()).setVariable("F", pedidasPMMD.fondo()).setVariable("A", pedidasPMMD.alto())
                .setVariable("n", pedidasPMMD.cuerpos())
                .setVariable("L0", nzPMMD(basePMMD.largo())).setVariable("F0", nzPMMD(basePMMD.fondo())).setVariable("A0", nzPMMD(basePMMD.alto()))
                .setVariable("n0", Math.max(1, basePMMD.cuerpos()));
        double vPMMD = ePMMD.evaluate();
        return Double.isFinite(vPMMD) ? vPMMD : 1;
    }

    private static double nzPMMD(double vPMMD) {
        return vPMMD <= 0 ? 1 : vPMMD;
    }



    public static double factorManoObra(MedidasPMMD pedidasPMMD, MedidasPMMD basePMMD) {
        if (pedidasPMMD == null || basePMMD == null || basePMMD.largo() <= 0 || pedidasPMMD.igual(basePMMD)) return 1;
        return Math.pow(pedidasPMMD.largo() / basePMMD.largo(), 0.7);
    }



    public static double redondearDecena(double vPMMD) {
        return Math.round(vPMMD / 10.0) * 10.0;
    }

    public static double r2(double vPMMD) {
        return Math.round(vPMMD * 100.0) / 100.0;
    }
}
