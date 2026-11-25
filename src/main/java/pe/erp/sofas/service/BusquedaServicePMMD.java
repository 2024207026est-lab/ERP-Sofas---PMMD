package pe.erp.sofas.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.erp.sofas.model.*;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;



@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BusquedaServicePMMD {

    public record Resultado(
      String grupo, 
        String icono, 
          String titulo, 
            String subtitulo, 
              String modulo, 
                Object dato) {
    }

    private static final int MAX_POR_GRUPOPMMD = 4;

    private final ProductoServicePMMD productoServicePMMD;

    private final TelaServicePMMD telaServicePMMD;

    private final ClienteServicePMMD clienteServicePMMD;

    private final PedidoServicePMMD pedidoServicePMMD;

    private final ProduccionServicePMMD produccionServicePMMD;

    private final InventarioServicePMMD inventarioServicePMMD;

    public List<Resultado> buscar(String textoPMMD) {
        String qPMMD = normalizar(textoPMMD);
        List<Resultado> rPMMD = new ArrayList<>();
        if (qPMMD.length() < 2) return rPMMD;
        int nPMMD = 0;
        for (ProductoPMMD pPMMD : productoServicePMMD.listar()) {
            if (nPMMD < MAX_POR_GRUPOPMMD && coincidePMMD(qPMMD, pPMMD.getCodigo(), pPMMD.getNombre(), pPMMD.getCategoria(), pPMMD.getColor())) {
                rPMMD.add(new Resultado("Sofás", "WEEKEND", pPMMD.getNombre(), pPMMD.getCodigo() + " · " + pPMMD.getMedidas(), "modelos", pPMMD.getCodigo()));
                nPMMD++;
            }
        }
        nPMMD = 0;
        for (TelaPMMD tPMMD : telaServicePMMD.telas()) {
            if (nPMMD < MAX_POR_GRUPOPMMD && coincidePMMD(qPMMD, tPMMD.getCodigo(), tPMMD.getColor(), tPMMD.getTipo().nombre(), tPMMD.getCodigoProveedor())) {
                rPMMD.add(new Resultado("Telas", "PALETTE", tPMMD.getNombre(), tPMMD.getCodigo() + " · " + InventarioServicePMMD.fmt(tPMMD.getStock()) + " m · " + tPMMD.getEstado(), "telas", tPMMD.getCodigo()));
                nPMMD++;
            }
        }
        nPMMD = 0;
        for (ClientePMMD cPMMD : clienteServicePMMD.listar()) {
            if (nPMMD < MAX_POR_GRUPOPMMD && coincidePMMD(qPMMD, cPMMD.getCodigo(), cPMMD.getNombre(), cPMMD.getDocumento(), cPMMD.getTelefono())) {
                rPMMD.add(new Resultado("Clientes", "PERSON", cPMMD.getNombre(), cPMMD.getCodigo() + " · " + cPMMD.getTipoDocumento() + " " + cPMMD.getDocumento(), "clientes", cPMMD.getCodigo()));
                nPMMD++;
            }
        }
        nPMMD = 0;
        for (PedidoPMMD pPMMD : pedidoServicePMMD.listar()) {
            if (nPMMD < MAX_POR_GRUPOPMMD && coincidePMMD(qPMMD, pPMMD.getCodigo(), pPMMD.getCliente().getNombre(), pPMMD.getProductoNombre())) {
                rPMMD.add(new Resultado("Pedidos", "ASSIGNMENT", pPMMD.getCodigo() + " · " + pPMMD.getProductoNombre(), pPMMD.getCliente().getNombre() + " · " + pPMMD.getEstado().nombre(), "pedidos", pPMMD.getCodigo()));
                nPMMD++;
            }
        }
        nPMMD = 0;
        for (OrdenProduccionPMMD oPMMD : produccionServicePMMD.ordenes()) {
            if (nPMMD < MAX_POR_GRUPOPMMD && coincidePMMD(qPMMD, oPMMD.getCodigo(), oPMMD.getProductoNombre(), oPMMD.getClienteNombre(), oPMMD.getTelaNombre())) {
                rPMMD.add(new Resultado("Órdenes", "BUILD", oPMMD.getCodigo() + " · " + oPMMD.getProductoNombre(), oPMMD.getClienteNombre() + " · " + oPMMD.getEtapa().nombre(), "produccion", oPMMD.getCodigo()));
                nPMMD++;
            }
        }
        nPMMD = 0;
        for (MaterialPMMD mPMMD : inventarioServicePMMD.materiales()) {
            if (nPMMD < MAX_POR_GRUPOPMMD && coincidePMMD(qPMMD, mPMMD.getCodigo(), mPMMD.getNombre(), mPMMD.getCategoria())) {
                rPMMD.add(new Resultado("Materiales", "ALL_INBOX", mPMMD.getNombre(), mPMMD.getCodigo() + " · " + InventarioServicePMMD.fmt(mPMMD.getStock()) + " " + mPMMD.getUnidad() + " · " + mPMMD.getEstado(), "inventario", mPMMD.getCodigo()));
                nPMMD++;
            }
        }
        return rPMMD;
    }

    private static boolean coincidePMMD(String qPMMD, String... camposPMMD) {
        for (String cPMMD : camposPMMD) if (cPMMD != null && normalizar(cPMMD).contains(qPMMD)) return true;
        return false;
    }

    public static String normalizar(String sPMMD) {
        if (sPMMD == null) return "";
        return Normalizer.normalize(sPMMD, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).trim();
    }
}
