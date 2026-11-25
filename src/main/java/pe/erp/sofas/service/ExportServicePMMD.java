package pe.erp.sofas.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import pe.erp.sofas.core.AppClockPMMD;
import pe.erp.sofas.core.MoneyPMMD;
import pe.erp.sofas.core.NegocioExceptionPMMD;
import pe.erp.sofas.model.*;
import pe.erp.sofas.service.calculo.ExplosionPMMD;

import java.awt.Color;
import java.io.*;
import java.util.List;



@Service
@RequiredArgsConstructor
public class ExportServicePMMD {

    private static final Color NAVYPMMD = new Color(0x14, 0x21, 0x3D);

    private static final Color WOODPMMD = new Color(0xA4, 0x71, 0x48);

    private static final Color MUTEDPMMD = new Color(0x64, 0x74, 0x8B);

    private static final Color BORDERPMMD = new Color(0xE2, 0xE8, 0xF0);

    private static final Color SURFACE2PMMD = new Color(0xF1, 0xF5, 0xF9);

    private final ConfiguracionServicePMMD configuracionPMMD;

    private BaseFont regularPMMD;

    private BaseFont boldPMMD;

    private BaseFont fuentePMMD(boolean negritaPMMD) {
        try {
            if (regularPMMD == null) {
                regularPMMD = cargarPMMD("/fonts/Inter-Regular.ttf");
                boldPMMD = cargarPMMD("/fonts/Inter-Bold.ttf");
            }
        } catch (Exception ePMMD) {
            try {
                regularPMMD = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, false);
                boldPMMD = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, false);
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }
        return negritaPMMD ? boldPMMD : regularPMMD;
    }

    private BaseFont cargarPMMD(String ruta) throws IOException, DocumentException {
        try (InputStream inPMMD = getClass().getResourceAsStream(ruta)) {
            if (inPMMD == null) throw new IOException(ruta);
            return BaseFont.createFont(ruta.substring(ruta.lastIndexOf('/') + 1), BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, inPMMD.readAllBytes(), null);
        }
    }

    private Font f(float size, boolean negritaPMMD, Color color) {
        return new Font(fuentePMMD(negritaPMMD), size, Font.NORMAL, color);
    }



    private interface Contenido {
        
        void escribir(Document dPMMD) throws DocumentException, IOException;
    }

    private void pdf(File archivo, boolean horizontalPMMD, String titulo, String subtitulo, Contenido contenidoPMMD) {
        Rectangle pagina = horizontalPMMD ? PageSize.A4.rotate() : PageSize.A4;
        Document dPMMD = new Document(pagina, 36, 36, 36, 42);
        try (OutputStream outPMMD = new FileOutputStream(archivo)) {
            PdfWriter wPMMD = PdfWriter.getInstance(dPMMD, outPMMD);
            wPMMD.setPageEvent(new PdfPageEventHelper() {

                @Override
                public void onEndPage(PdfWriter writerPMMD, Document documentPMMD) {
                    PdfContentByte cbPMMD = writerPMMD.getDirectContent();
                    ColumnText.showTextAligned(cbPMMD, Element.ALIGN_LEFT,
                            new Phrase("SISTEMA ERP SOFAS S.A.C. · Documento generado el " + AppClockPMMD.fmt(AppClockPMMD.hoy()) + " · Sin validez tributaria", f(7.5f, false, MUTEDPMMD)),
                            documentPMMD.left(), documentPMMD.bottom() - 18, 0);
                    ColumnText.showTextAligned(cbPMMD, Element.ALIGN_RIGHT, new Phrase("Página " + writerPMMD.getPageNumber(), f(7.5f, false, MUTEDPMMD)),
                            documentPMMD.right(), documentPMMD.bottom() - 18, 0);
                }
            });
            dPMMD.addTitle(titulo);
            dPMMD.addAuthor("SISTEMA ERP SOFAS ERP · Sofás");
            dPMMD.open();
            cabeceraPMMD(dPMMD, titulo, subtitulo);
            contenidoPMMD.escribir(dPMMD);
            dPMMD.close();
        } catch (IOException | DocumentException ePMMD) {
            throw new NegocioExceptionPMMD("No se pudo generar el PDF: " + ePMMD.getMessage());
        }
    }

    private void cabeceraPMMD(Document dPMMD, String titulo, String subtitulo) throws DocumentException {
        EmpresaPMMD ePMMD = configuracionPMMD.empresa();
        PdfPTable tPMMD = new PdfPTable(new float[]{1.1f, 6, 4});
        tPMMD.setWidthPercentage(100);
        PdfPCell logoPMMD = new PdfPCell(new Phrase("IS", f(15, true, new Color(0xE4, 0xC9, 0xA4))));
        logoPMMD.setBackgroundColor(NAVYPMMD);
        logoPMMD.setHorizontalAlignment(Element.ALIGN_CENTER);
        logoPMMD.setVerticalAlignment(Element.ALIGN_MIDDLE);
        logoPMMD.setFixedHeight(34);
        logoPMMD.setBorder(Rectangle.NO_BORDER);
        tPMMD.addCell(logoPMMD);
        PdfPCell marcaPMMD = new PdfPCell();
        marcaPMMD.setBorder(Rectangle.NO_BORDER);
        marcaPMMD.setPaddingLeft(8);
        marcaPMMD.addElement(new Paragraph("SISTEMA ERP SOFAS", f(14, true, NAVYPMMD)));
        marcaPMMD.addElement(new Paragraph("SOFÁS · JULIACA", f(7.5f, true, WOODPMMD)));
        tPMMD.addCell(marcaPMMD);
        PdfPCell datos = new PdfPCell();
        datos.setBorder(Rectangle.NO_BORDER);
        Paragraph pPMMD = new Paragraph(ePMMD.getRazonSocial() + " · RUC " + (ePMMD.getRuc() == null ? "—" : ePMMD.getRuc()), f(7.5f, false, MUTEDPMMD));
        pPMMD.setAlignment(Element.ALIGN_RIGHT);
        datos.addElement(pPMMD);
        Paragraph p2PMMD = new Paragraph((ePMMD.getDireccion() == null ? "" : ePMMD.getDireccion()) + " · " + (ePMMD.getTelefono() == null ? "" : ePMMD.getTelefono()), f(7.5f, false, MUTEDPMMD));
        p2PMMD.setAlignment(Element.ALIGN_RIGHT);
        datos.addElement(p2PMMD);
        tPMMD.addCell(datos);
        dPMMD.add(tPMMD);
        Paragraph titPMMD = new Paragraph(titulo, f(16, true, NAVYPMMD));
        titPMMD.setSpacingBefore(14);
        dPMMD.add(titPMMD);
        if (subtitulo != null) {
            Paragraph subPMMD = new Paragraph(subtitulo, f(9, false, MUTEDPMMD));
            subPMMD.setSpacingAfter(10);
            dPMMD.add(subPMMD);
        }
    }

    private PdfPTable tablaPMMD(List<String> columnas, boolean[] derechaPMMD, List<List<String>> filasPMMD, List<String> totalesPMMD, float[] anchos) {
        PdfPTable tPMMD = anchos == null ? new PdfPTable(columnas.size()) : new PdfPTable(anchos);
        tPMMD.setWidthPercentage(100);
        tPMMD.setHeaderRows(1);
        for (int iPMMD = 0; iPMMD < columnas.size(); iPMMD++) {
            PdfPCell cPMMD = new PdfPCell(new Phrase(columnas.get(iPMMD).toUpperCase(), f(7, true, MUTEDPMMD)));
            cPMMD.setBackgroundColor(SURFACE2PMMD);
            cPMMD.setBorderColor(BORDERPMMD);
            cPMMD.setPadding(5);
            cPMMD.setHorizontalAlignment(derechaPMMD != null && derechaPMMD[iPMMD] ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT);
            tPMMD.addCell(cPMMD);
        }
        for (List<String> filaPMMD : filasPMMD) {
            for (int iPMMD = 0; iPMMD < columnas.size(); iPMMD++) {
                PdfPCell cPMMD = new PdfPCell(new Phrase(iPMMD < filaPMMD.size() ? filaPMMD.get(iPMMD) : "", f(8, false, Color.BLACK)));
                cPMMD.setBorderColor(BORDERPMMD);
                cPMMD.setPadding(5);
                cPMMD.setHorizontalAlignment(derechaPMMD != null && derechaPMMD[iPMMD] ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT);
                tPMMD.addCell(cPMMD);
            }
        }
        if (totalesPMMD != null) {
            for (int iPMMD = 0; iPMMD < columnas.size(); iPMMD++) {
                PdfPCell cPMMD = new PdfPCell(new Phrase(iPMMD < totalesPMMD.size() ? totalesPMMD.get(iPMMD) : "", f(8, true, NAVYPMMD)));
                cPMMD.setBackgroundColor(SURFACE2PMMD);
                cPMMD.setBorderColor(BORDERPMMD);
                cPMMD.setPadding(5);
                cPMMD.setHorizontalAlignment(derechaPMMD != null && derechaPMMD[iPMMD] ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT);
                tPMMD.addCell(cPMMD);
            }
        }
        return tPMMD;
    }

    private PdfPTable datos(String[][] paresPMMD, int columnas) {
        PdfPTable tPMMD = new PdfPTable(columnas);
        tPMMD.setWidthPercentage(100);
        tPMMD.setSpacingAfter(10);
        for (String[] parPMMD : paresPMMD) {
            PdfPCell cPMMD = new PdfPCell();
            cPMMD.setBorderColor(BORDERPMMD);
            cPMMD.setPadding(6);
            cPMMD.addElement(new Paragraph(parPMMD[0], f(7, false, MUTEDPMMD)));
            cPMMD.addElement(new Paragraph(parPMMD[1] == null || parPMMD[1].isBlank() ? "—" : parPMMD[1], f(9, true, Color.BLACK)));
            tPMMD.addCell(cPMMD);
        }
        int restoPMMD = paresPMMD.length % columnas;
        if (restoPMMD != 0) for (int iPMMD = restoPMMD; iPMMD < columnas; iPMMD++) {
            PdfPCell cPMMD = new PdfPCell(new Phrase(""));
            cPMMD.setBorderColor(BORDERPMMD);
            tPMMD.addCell(cPMMD);
        }
        return tPMMD;
    }

    private Paragraph seccionPMMD(String texto) {
        Paragraph pPMMD = new Paragraph(texto, f(10.5f, true, NAVYPMMD));
        pPMMD.setSpacingBefore(8);
        pPMMD.setSpacingAfter(6);
        return pPMMD;
    }

    private void imagenPMMD(Document dPMMD, byte[] pngPMMD, float anchoPMMD) throws IOException, DocumentException {
        if (pngPMMD == null) return;
        Image imgPMMD = Image.getInstance(pngPMMD);
        imgPMMD.scaleToFit(anchoPMMD, anchoPMMD * 0.75f);
        imgPMMD.setAlignment(Element.ALIGN_CENTER);
        imgPMMD.setSpacingAfter(8);
        dPMMD.add(imgPMMD);
    }

    private void imagenArchivoPMMD(Document dPMMD, String ruta, float anchoPMMD) throws IOException, DocumentException {
        if (ruta == null || !new File(ruta).exists()) return;
        Image imgPMMD = Image.getInstance(ruta);
        imgPMMD.scaleToFit(anchoPMMD, anchoPMMD * 0.66f);
        imgPMMD.setAlignment(Element.ALIGN_CENTER);
        imgPMMD.setSpacingAfter(8);
        dPMMD.add(imgPMMD);
    }



    public void pdf(Tabla tPMMD, File archivo) {
        pdf(archivo, tPMMD.columnas().size() > 6, tPMMD.titulo(), tPMMD.subtitulo(),
                dPMMD -> dPMMD.add(tablaPMMD(tPMMD.columnas(), tPMMD.derecha(), tPMMD.filas(), tPMMD.totales(), null)));
    }

    public void excel(Tabla tPMMD, File archivo) {
        try (Workbook wbPMMD = new XSSFWorkbook(); OutputStream outPMMD = new FileOutputStream(archivo)) {
            Sheet sPMMD = wbPMMD.createSheet(tPMMD.titulo().length() > 30 ? tPMMD.titulo().substring(0, 30) : tPMMD.titulo());
            CellStyle titulo = wbPMMD.createCellStyle();
            org.apache.poi.ss.usermodel.Font ftPMMD = wbPMMD.createFont();
            ftPMMD.setBold(true);
            ftPMMD.setFontHeightInPoints((short) 14);
            titulo.setFont(ftPMMD);
            CellStyle cabPMMD = wbPMMD.createCellStyle();
            org.apache.poi.ss.usermodel.Font fcPMMD = wbPMMD.createFont();
            fcPMMD.setBold(true);
            fcPMMD.setColor(IndexedColors.WHITE.getIndex());
            cabPMMD.setFont(fcPMMD);
            cabPMMD.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            cabPMMD.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            CellStyle totPMMD = wbPMMD.createCellStyle();
            org.apache.poi.ss.usermodel.Font fbPMMD = wbPMMD.createFont();
            fbPMMD.setBold(true);
            totPMMD.setFont(fbPMMD);
            totPMMD.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            totPMMD.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row r0PMMD = sPMMD.createRow(0);
            r0PMMD.createCell(0).setCellValue("SISTEMA ERP SOFAS S.A.C. · " + tPMMD.titulo());
            r0PMMD.getCell(0).setCellStyle(titulo);
            sPMMD.addMergedRegion(new CellRangeAddress(0, 0, 0, Math.max(1, tPMMD.columnas().size() - 1)));
            sPMMD.createRow(1).createCell(0).setCellValue(tPMMD.subtitulo() == null ? "" : tPMMD.subtitulo());
            Row hPMMD = sPMMD.createRow(3);
            for (int iPMMD = 0; iPMMD < tPMMD.columnas().size(); iPMMD++) {
                Cell cPMMD = hPMMD.createCell(iPMMD);
                cPMMD.setCellValue(tPMMD.columnas().get(iPMMD));
                cPMMD.setCellStyle(cabPMMD);
            }
            int filaPMMD = 4;
            for (List<String> f : tPMMD.filas()) {
                Row rPMMD = sPMMD.createRow(filaPMMD++);
                for (int iPMMD = 0; iPMMD < f.size(); iPMMD++) escribirCeldaPMMD(rPMMD.createCell(iPMMD), f.get(iPMMD));
            }
            if (tPMMD.totales() != null) {
                Row rPMMD = sPMMD.createRow(filaPMMD);
                for (int iPMMD = 0; iPMMD < tPMMD.totales().size(); iPMMD++) {
                    Cell cPMMD = rPMMD.createCell(iPMMD);
                    escribirCeldaPMMD(cPMMD, tPMMD.totales().get(iPMMD));
                    cPMMD.setCellStyle(totPMMD);
                }
            }
            for (int iPMMD = 0; iPMMD < tPMMD.columnas().size(); iPMMD++) sPMMD.autoSizeColumn(iPMMD);
            wbPMMD.write(outPMMD);
        } catch (IOException ePMMD) {
            throw new NegocioExceptionPMMD("No se pudo generar el Excel: " + ePMMD.getMessage());
        }
    }



    private static void escribirCeldaPMMD(Cell cPMMD, String vPMMD) {
        if (vPMMD == null) return;
        String limpioPMMD = vPMMD.replace("S/", "").replace(",", "").replace("%", "").trim();
        if (!limpioPMMD.isEmpty() && limpioPMMD.matches("[-+]?\\d+(\\.\\d+)?")) {
            cPMMD.setCellValue(Double.parseDouble(limpioPMMD));
        } else {
            cPMMD.setCellValue(vPMMD);
        }
    }



    public void cotizacionPedido(PedidoPMMD pPMMD, File archivo) {
        pdf(archivo, false, "Cotización " + pPMMD.getCodigo(), "Emitida el " + AppClockPMMD.fmt(AppClockPMMD.hoy()) + " · válida por 15 días", dPMMD -> {
            dPMMD.add(datos(new String[][]{
                    {"Cliente", pPMMD.getCliente().getNombre()}, {pPMMD.getCliente().getTipoDocumento(), pPMMD.getCliente().getDocumento()},
                    {"Teléfono", pPMMD.getCliente().getTelefono()}, {"Dirección", pPMMD.getCliente().getDireccion()},
                    {"Entrega estimada", AppClockPMMD.fmt(pPMMD.getFechaEntrega())}, {"Tipo", pPMMD.getTipo().nombre()}}, 3));
            if (pPMMD.getProducto() != null) imagenArchivoPMMD(dPMMD, pPMMD.getProducto().getImagenPrincipal(), 320);
            dPMMD.add(seccionPMMD("Especificación"));
            dPMMD.add(datos(new String[][]{
                    {"Modelo", pPMMD.getProductoNombre()}, {"Cantidad", String.valueOf(pPMMD.getCantidad())},
                    {"Medidas", pPMMD.getMedidas()}, {"Tela y color", pPMMD.getTela() == null ? "—" : pPMMD.getTela().getNombre()},
                    {"Detalle", pPMMD.getEspecificacion()}, {"Observaciones", pPMMD.getObservaciones()}}, 2));
            dPMMD.add(seccionPMMD("Importe"));
            var subPMMD = pe.erp.sofas.service.calculo.IgvPMMD.subtotal(pPMMD.getPrecio());
            dPMMD.add(tablaPMMD(List.of("Concepto", "Importe"), new boolean[]{false, true}, List.of(
                    List.of("Subtotal", MoneyPMMD.fmt(subPMMD)), List.of("IGV 18 %", MoneyPMMD.fmt(pPMMD.getPrecio().subtract(subPMMD))),
                    List.of("Adelanto recibido", MoneyPMMD.fmt(pPMMD.getAdelanto()))),
                    List.of("Total (saldo " + MoneyPMMD.fmt(pPMMD.getSaldo()) + ")", MoneyPMMD.fmt(pPMMD.getPrecio())), new float[]{4, 1.4f}));
            Paragraph nPMMD = new Paragraph("Adelanto mínimo del 50 % para confirmar. Garantía: 12 meses en estructura y 6 meses en tapiz y espuma.", f(8, false, MUTEDPMMD));
            nPMMD.setSpacingBefore(10);
            dPMMD.add(nPMMD);
        });
    }

    public void cotizacionConfigurador(ConfiguradorServicePMMD.Cotizacion cPMMD, String especificacionPMMD, String clientePMMD, byte[] pngPMMD, File archivo) {
        pdf(archivo, false, "Cotización · sofá a medida", "Emitida el " + AppClockPMMD.fmt(AppClockPMMD.hoy()) + " · " + cPMMD.referencia(), dPMMD -> {
            imagenPMMD(dPMMD, pngPMMD, 420);
            dPMMD.add(datos(new String[][]{{"Cliente", clientePMMD}, {"Fabricación estimada", cPMMD.dias() + " días"},
                    {"Configuración", especificacionPMMD}, {"Tela", cPMMD.tela() == null ? "Según ficha" : cPMMD.tela().getNombre()}}, 2));
            var totalPMMD = java.math.BigDecimal.valueOf(cPMMD.precio());
            var subPMMD = pe.erp.sofas.service.calculo.IgvPMMD.subtotal(totalPMMD);
            dPMMD.add(seccionPMMD("Precio"));
            dPMMD.add(tablaPMMD(List.of("Concepto", "Importe"), new boolean[]{false, true},
                    List.of(List.of("Subtotal", MoneyPMMD.fmt(subPMMD)), List.of("IGV 18 %", MoneyPMMD.fmt(totalPMMD.subtract(subPMMD)))),
                    List.of("Total", MoneyPMMD.fmt(totalPMMD)), new float[]{4, 1.4f}));
        });
    }



    public record ItemSala(
      String nombre, 
        String detalle, 
          double precio) {
    }

    public void cotizacionSala(String proyectoPMMD, String clientePMMD, String medidas, List<ItemSala> itemsPMMD, byte[] planoPMMD, byte[] renderPMMD, File archivo) {
        pdf(archivo, false, "Propuesta de sala", proyectoPMMD + " · " + medidas, dPMMD -> {
            dPMMD.add(datos(new String[][]{{"Cliente", clientePMMD}, {"Proyecto", proyectoPMMD}, {"Sala", medidas}, {"Fecha", AppClockPMMD.fmt(AppClockPMMD.hoy())}}, 2));
            imagenPMMD(dPMMD, renderPMMD, 420);
            imagenPMMD(dPMMD, planoPMMD, 380);
            List<List<String>> filasPMMD = new java.util.ArrayList<>();
            double totalPMMD = 0;
            for (ItemSala iPMMD : itemsPMMD) {
                filasPMMD.add(List.of(iPMMD.nombre(), iPMMD.detalle(), iPMMD.precio() > 0 ? MoneyPMMD.fmt(iPMMD.precio()) : "—"));
                totalPMMD += iPMMD.precio();
            }
            dPMMD.add(seccionPMMD("Muebles de la propuesta"));
            dPMMD.add(tablaPMMD(List.of("Mueble", "Detalle", "Precio"), new boolean[]{false, false, true}, filasPMMD, List.of("Total", "", MoneyPMMD.fmt(totalPMMD)), new float[]{3, 4, 1.4f}));
        });
    }

    public void cotizacionRetapizado(RetapizadoPMMD rPMMD, RetapizadoServicePMMD.Cotizacion cPMMD, File archivo) {
        pdf(archivo, false, "Cotización de retapizado " + rPMMD.getCodigo(), "Emitida el " + AppClockPMMD.fmt(AppClockPMMD.hoy()), dPMMD -> {
            imagenArchivoPMMD(dPMMD, rPMMD.getFoto(), 300);
            dPMMD.add(datos(new String[][]{{"Cliente", rPMMD.getCliente().getNombre()}, {"Sofá", rPMMD.getDescripcion()},
                    {"Medidas", rPMMD.getMedidas()}, {"Cuerpos", String.valueOf(rPMMD.getCuerpos())},
                    {"Estructura", rPMMD.getEstructura()}, {"Espuma", rPMMD.getEspuma()},
                    {"Tela nueva", rPMMD.getTela() == null ? "—" : rPMMD.getTela().getNombre()}, {"Metros de tela", MoneyPMMD.numero(cPMMD.metrosTelaPMMD()) + " m"}}, 2));
            dPMMD.add(seccionPMMD("Precio"));
            dPMMD.add(tablaPMMD(List.of("Concepto", "Importe"), new boolean[]{false, true}, List.of(List.of("Retapizado completo (materiales y mano de obra)", MoneyPMMD.fmt(cPMMD.precio()))),
                    List.of("Total", MoneyPMMD.fmt(cPMMD.precio())), new float[]{4, 1.4f}));
        });
    }

    public void hojaTrabajo(OrdenProduccionPMMD opPMMD, ProduccionServicePMMD.EstadoMateriales emPMMD, File archivo) {
        pdf(archivo, false, "Hoja de trabajo " + opPMMD.getCodigo(), opPMMD.getProductoNombre() + " × " + opPMMD.getCantidad(), dPMMD -> {
            dPMMD.add(datos(new String[][]{
                    {"Cliente", opPMMD.getClienteNombre()}, {"Responsable", opPMMD.getResponsable() == null ? "—" : opPMMD.getResponsable().getNombre()},
                    {"Prioridad", opPMMD.getPrioridad().nombre()}, {"Inicio", AppClockPMMD.fmt(opPMMD.getFechaInicio())},
                    {"Entrega", AppClockPMMD.fmt(opPMMD.getFechaEntrega())}, {"Etapa actual", opPMMD.getEtapa().nombre()},
                    {"Tela y color", opPMMD.getTelaNombre()}, {"Ficha", opPMMD.getFichaUid() == null ? "Sin ficha" : opPMMD.getFichaUid().replace("v", " v")},
                    {"Pedido", opPMMD.getPedidoCodigo()}}, 3));
            if (opPMMD.getEspecificacion() != null) {
                dPMMD.add(seccionPMMD("Especificación"));
                dPMMD.add(new Paragraph(opPMMD.getEspecificacion(), f(9, false, Color.BLACK)));
            }
            dPMMD.add(seccionPMMD("Etapas"));
            List<List<String>> etapasPMMD = new java.util.ArrayList<>();
            for (EtapaPMMD ePMMD : EtapaPMMD.values()) {
                etapasPMMD.add(List.of(ePMMD.ordinal() < opPMMD.getEtapa().ordinal() ? "[x]" : "[  ]", ePMMD.nombre(), "", ""));
            }
            dPMMD.add(tablaPMMD(List.of("", "Etapa", "Firma", "Fecha"), null, etapasPMMD, null, new float[]{0.4f, 4, 3, 2}));
            if (emPMMD != null && !emPMMD.lineas().isEmpty()) {
                dPMMD.add(seccionPMMD("Materiales"));
                List<List<String>> filasPMMD = new java.util.ArrayList<>();
                for (var lPMMD : emPMMD.lineas()) {
                    filasPMMD.add(List.of(lPMMD.material().getCodigo(), lPMMD.material().getNombre(), lPMMD.componentes(),
                            MoneyPMMD.numero(lPMMD.necesario()) + " " + lPMMD.material().getUnidad(), MoneyPMMD.numero(lPMMD.reservado()), lPMMD.estado()));
                }
                dPMMD.add(tablaPMMD(List.of("Código", "Material", "Componente", "Necesario", "Reservado", "Estado"),
                        new boolean[]{false, false, false, true, true, false}, filasPMMD, null, new float[]{1.3f, 3.4f, 2.4f, 1.6f, 1.4f, 1.4f}));
            }
        });
    }



    public record FilaCorte(
      String numeros, 
        String pieza, 
          String medidas, 
            int cantidad, 
              String origen) {
    }

    public void hojaCorte(String titulo, String subtitulo, String resumen, byte[] planoPMMD, List<FilaCorte> piezasPMMD, File archivo) {
        pdf(archivo, false, titulo, subtitulo, dPMMD -> {
            Paragraph rPMMD = new Paragraph(resumen, f(9, true, WOODPMMD));
            rPMMD.setSpacingAfter(8);
            dPMMD.add(rPMMD);
            imagenPMMD(dPMMD, planoPMMD, 520);
            List<List<String>> filasPMMD = new java.util.ArrayList<>();
            for (FilaCorte fcPMMD : piezasPMMD) filasPMMD.add(List.of("[  ]", fcPMMD.numeros(), fcPMMD.pieza(), fcPMMD.medidas(), String.valueOf(fcPMMD.cantidad()), fcPMMD.origen()));
            dPMMD.add(tablaPMMD(List.of("Cortada", "N.º", "Pieza", "Medidas (cm)", "Cant.", "Origen"),
                    new boolean[]{false, false, false, false, true, false}, filasPMMD, null, new float[]{0.9f, 1, 4, 2, 0.8f, 1.4f}));
        });
    }

    public void fichaTecnica(FichaTecnicaPMMD fichaPMMD, ExplosionPMMD.Resultado rPMMD, double precio, File archivo) {
        pdf(archivo, false, "Ficha técnica " + fichaPMMD.getEtiqueta(), fichaPMMD.getNombre() + " · " + fichaPMMD.getEstado().nombre(), dPMMD -> {
            dPMMD.add(datos(new String[][]{
                    {"Medidas base", fichaPMMD.getLargo() + " × " + fichaPMMD.getFondo() + " × " + fichaPMMD.getAlto() + " cm"},
                    {"Cuerpos", String.valueOf(fichaPMMD.getCuerpos())}, {"Aprobada por", fichaPMMD.getAprobadoPor()},
                    {"Fecha", AppClockPMMD.fmt(fichaPMMD.getFechaAprobacion())}, {"Indirectos", MoneyPMMD.numero(fichaPMMD.getIndirectosPct()) + " %"},
                    {"Costo estándar", MoneyPMMD.fmt(rPMMD.costoEstandar())}}, 3));
            dPMMD.add(seccionPMMD("Lista de materiales"));
            List<List<String>> filasPMMD = new java.util.ArrayList<>();
            for (ExplosionPMMD.Linea lPMMD : rPMMD.lineas()) {
                filasPMMD.add(List.of(lPMMD.componente().nombre(), lPMMD.material().getCodigo() + " " + lPMMD.material().getNombre(),
                        MoneyPMMD.numero(lPMMD.base()), MoneyPMMD.numero(lPMMD.mermaPct()) + " %", MoneyPMMD.numero(lPMMD.bruta()) + " " + lPMMD.material().getUnidad(),
                        MoneyPMMD.fmt(lPMMD.costo())));
            }
            dPMMD.add(tablaPMMD(List.of("Componente", "Material", "Base", "Merma", "Bruto", "Costo"),
                    new boolean[]{false, false, true, true, true, true}, filasPMMD,
                    List.of("", "Materiales", "", "", "", MoneyPMMD.fmt(rPMMD.costoMateriales())), new float[]{1.6f, 4, 1, 1, 1.6f, 1.4f}));
            dPMMD.add(seccionPMMD("Mano de obra"));
            List<List<String>> moPMMD = new java.util.ArrayList<>();
            for (OperacionMOPMMD oPMMD : fichaPMMD.getOperaciones()) moPMMD.add(List.of(oPMMD.getEtapa().nombre(), MoneyPMMD.numero(oPMMD.getHoras()) + " h", MoneyPMMD.fmt(oPMMD.getCostoHora()) + "/h", MoneyPMMD.fmt(oPMMD.getCosto())));
            dPMMD.add(tablaPMMD(List.of("Etapa", "Horas", "Costo/hora", "Costo"), new boolean[]{false, true, true, true}, moPMMD,
                    List.of("Total", MoneyPMMD.numero(rPMMD.horasMO()) + " h", "", MoneyPMMD.fmt(rPMMD.costoMO())), null));
            dPMMD.add(seccionPMMD("Resumen"));
            dPMMD.add(tablaPMMD(List.of("Concepto", "Importe"), new boolean[]{false, true}, List.of(
                    List.of("Materiales", MoneyPMMD.fmt(rPMMD.costoMateriales())), List.of("Mano de obra", MoneyPMMD.fmt(rPMMD.costoMO())),
                    List.of("Indirectos " + MoneyPMMD.numero(rPMMD.indirectosPct()) + " %", MoneyPMMD.fmt(rPMMD.indirectos())),
                    List.of("Precio de venta", MoneyPMMD.fmt(precio))), List.of("Costo estándar", MoneyPMMD.fmt(rPMMD.costoEstandar())), new float[]{4, 1.4f}));
        });
    }
}
