package pe.erp.sofas.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.erp.sofas.model.*;
import pe.erp.sofas.repository.FichaTecnicaRepositoryPMMD;
import pe.erp.sofas.repository.TelaRepositoryPMMD;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CalculoPrecioServicePMMD {

    private final FichaTecnicaRepositoryPMMD fichaTecnicaRepoPMMD;

    private final TelaRepositoryPMMD telaRepoPMMD;

    private static final BigDecimal COSTO_HORA_MO_PMMD = BigDecimal.valueOf(45);

    private static final BigDecimal MARGEN_BASEPMMD = BigDecimal.valueOf(1.40);

    private static final BigDecimal IVAPMMD = BigDecimal.valueOf(1.18);

    public BigDecimal calcularPrecio(ConfigSofaPMMD configPMMD) {
        return calcularPrecio(configPMMD, BigDecimal.ZERO);
    }

    public BigDecimal calcularPrecio(ConfigSofaPMMD configPMMD, BigDecimal descuentoPct) {
        try {
            BigDecimal costoBOM = calcularCostoBOMPMMD(configPMMD);
            BigDecimal costoMO = calcularCostoMOPMMD(configPMMD);
            BigDecimal costoTotalPMMD = costoBOM.add(costoMO);


            BigDecimal conMargen = costoTotalPMMD.multiply(MARGEN_BASEPMMD);


            if (descuentoPct.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal factorDescuentoPMMD = BigDecimal.ONE.subtract(descuentoPct.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
                conMargen = conMargen.multiply(factorDescuentoPMMD);
            }


            BigDecimal conIGV = conMargen.multiply(IVAPMMD);

            return conIGV.setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            throw new RuntimeException("Error calculando precio: " + e.getMessage(), e);
        }
    }

    private BigDecimal calcularCostoBOMPMMD(ConfigSofaPMMD configPMMD) {
        FichaTecnicaPMMD fichaPMMD = obtenerFichaTecnicaPMMD(configPMMD);
        if (fichaPMMD == null) {
            return BigDecimal.valueOf(800);
        }

        BigDecimal costoBOM = BigDecimal.ZERO;


        for (LineaBOMPMMD lineaPMMD : fichaPMMD.getLineas()) {
            if (lineaPMMD.getMaterial() != null && lineaPMMD.getMaterial().getCosto() != null) {

                BigDecimal cantidadPMMD = BigDecimal.valueOf(lineaPMMD.getCantidad() != null ? lineaPMMD.getCantidad() : 1);

                if ("F".equals(lineaPMMD.getDependencia().toString())) {
                    cantidadPMMD = cantidadPMMD.multiply(BigDecimal.valueOf(configPMMD.cuerpos()));
                }
                costoBOM = costoBOM.add(BigDecimal.valueOf(lineaPMMD.getMaterial().getCosto()).multiply(cantidadPMMD));
            }
        }


        costoBOM = costoBOM.add(calcularRecargosOpcionalesPMMD(configPMMD));

        return costoBOM;
    }

    private BigDecimal calcularRecargosOpcionalesPMMD(ConfigSofaPMMD configPMMD) {
        BigDecimal recargasPMMD = BigDecimal.ZERO;


        if (configPMMD.espuma() == ConfigSofaPMMD.Espuma.D30) {
            recargasPMMD = recargasPMMD.add(BigDecimal.valueOf(80));
        } else if (configPMMD.espuma() == ConfigSofaPMMD.Espuma.VISCO) {
            recargasPMMD = recargasPMMD.add(BigDecimal.valueOf(150));
        }


        ConfigSofaPMMD.Patas patasPMMD = configPMMD.patas();
        if (patasPMMD == ConfigSofaPMMD.Patas.METAL_NEGRO) {
            recargasPMMD = recargasPMMD.add(BigDecimal.valueOf(14 * 4));
        } else if (patasPMMD == ConfigSofaPMMD.Patas.METAL_DORADO) {
            recargasPMMD = recargasPMMD.add(BigDecimal.valueOf(20 * 4));
        } else if (patasPMMD == ConfigSofaPMMD.Patas.ZOCALO) {
            recargasPMMD = recargasPMMD.add(BigDecimal.valueOf(60));
        }


        if (configPMMD.mecanismo() == ConfigSofaPMMD.Mecanismo.RECLINABLE_MANUAL) {
            recargasPMMD = recargasPMMD.add(BigDecimal.valueOf(250));
        } else if (configPMMD.mecanismo() == ConfigSofaPMMD.Mecanismo.RECLINABLE_ELECTRICO) {
            recargasPMMD = recargasPMMD.add(BigDecimal.valueOf(600));
        } else if (configPMMD.mecanismo() == ConfigSofaPMMD.Mecanismo.CAMA) {
            recargasPMMD = recargasPMMD.add(BigDecimal.valueOf(300));
        }


        if (configPMMD.conChaise()) {
            recargasPMMD = recargasPMMD.add(BigDecimal.valueOf(200));
        }


        if (configPMMD.telaId() != null) {
            Optional<TelaPMMD> telaPMMD = telaRepoPMMD.findById(configPMMD.telaId());
            if (telaPMMD.isPresent() && telaPMMD.get().getTipo() != null) {
                double recargoPctPMMD = telaPMMD.get().getTipo().recargoPct();
                if (recargoPctPMMD > 0) {
                    recargasPMMD = recargasPMMD.add(BigDecimal.valueOf(recargoPctPMMD * 0.8));
                }
            }
        }

        return recargasPMMD;
    }

    private BigDecimal calcularCostoMOPMMD(ConfigSofaPMMD configPMMD) {
        FichaTecnicaPMMD fichaPMMD = obtenerFichaTecnicaPMMD(configPMMD);
        if (fichaPMMD == null) {
            return BigDecimal.valueOf(150);
        }

        double horasPMMD = fichaPMMD.getHorasMO();

        horasPMMD = horasPMMD * (1 + (configPMMD.cuerpos() - 1) * 0.15);

        return COSTO_HORA_MO_PMMD.multiply(BigDecimal.valueOf(horasPMMD)).setScale(2, RoundingMode.HALF_UP);
    }

    private FichaTecnicaPMMD obtenerFichaTecnicaPMMD(ConfigSofaPMMD configPMMD) {
        try {
            String codigoPMMD = configPMMD.modelo().producto();
            var fichasPMMD = fichaTecnicaRepoPMMD.findByCodigoOrderByVersionDesc(codigoPMMD);
            return fichasPMMD.isEmpty() ? null : fichasPMMD.get(0);
        } catch (Exception e) {
            return null;
        }
    }

    public DesglosePrecio desglosar(ConfigSofaPMMD configPMMD, BigDecimal descuentoPct) {
        BigDecimal costoBOM = calcularCostoBOMPMMD(configPMMD);
        BigDecimal costoMO = calcularCostoMOPMMD(configPMMD);
        BigDecimal costoTotalPMMD = costoBOM.add(costoMO);

        BigDecimal conMargen = costoTotalPMMD.multiply(MARGEN_BASEPMMD);
        BigDecimal montoDescuentoPMMD = conMargen.multiply(descuentoPct.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
        BigDecimal conDescuento = conMargen.subtract(montoDescuentoPMMD);

        BigDecimal igv = conDescuento.multiply(BigDecimal.valueOf(0.18)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal conIGV = conDescuento.add(igv);

        return new DesglosePrecio(
            costoBOM,
            costoMO,
            conMargen,
            descuentoPct,
            conDescuento,
            igv,
            conIGV
        );
    }

    public record DesglosePrecio(
        
        BigDecimal costoBOM,
        
        BigDecimal costoMO,
        
        BigDecimal conMargen,
        
        BigDecimal descuentoPct,
        
        BigDecimal conDescuento,
        
        BigDecimal igv,
        
        BigDecimal precioFinal
    ) {}
}
