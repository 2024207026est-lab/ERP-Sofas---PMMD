package pe.erp.sofas.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.erp.sofas.model.ConfigSofaPMMD;
import pe.erp.sofas.model.UsuarioPMMD;

import javafx.scene.image.WritableImage;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class GeneradorPDFServicePMMD {

    private final CalculoPrecioServicePMMD precioServicePMMD;

    private final PersistenciaConfiguracionServicePMMD configServicePMMD;

    private static final DateTimeFormatter FECHA_FMTPMMD = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] generarPresupuesto(ConfigSofaPMMD configPMMD, String nombreClientePMMD, UsuarioPMMD usuarioPMMD) {
        return generarPresupuesto(configPMMD, nombreClientePMMD, usuarioPMMD, null);
    }

    public byte[] generarPresupuesto(ConfigSofaPMMD configPMMD, String nombreClientePMMD, UsuarioPMMD usuarioPMMD, WritableImage imagenSofaPMMD) {
        try {
            ByteArrayOutputStream baosPMMD = new ByteArrayOutputStream();
            Document docPMMD = new Document(PageSize.A4);
            PdfWriter.getInstance(docPMMD, baosPMMD);
            docPMMD.open();


            agregarEncabezadoPMMD(docPMMD);


            agregarDatosPresupuestoPMMD(docPMMD, nombreClientePMMD);


            agregarEspecificacionesPMMD(docPMMD, configPMMD);


            if (imagenSofaPMMD != null) {
                agregarImagenPMMD(docPMMD, imagenSofaPMMD);
            }


            agregarDesglosePrecioPMMD(docPMMD, configPMMD);


            agregarTerminosPMMD(docPMMD);

            docPMMD.close();
            return baosPMMD.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF: " + e.getMessage(), e);
        }
    }

    private void agregarEncabezadoPMMD(Document docPMMD) throws DocumentException {
        PdfPTable tablaPMMD = new PdfPTable(2);
        tablaPMMD.setWidthPercentage(100);


        PdfPCell celdaEmpresaPMMD = new PdfPCell();
        celdaEmpresaPMMD.addElement(new Paragraph("SISTEMA ERP SOFAS SOFÁS",
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
        celdaEmpresaPMMD.addElement(new Paragraph("Fábrica y tienda de sofás premium",
            FontFactory.getFont(FontFactory.HELVETICA, 10)));
        celdaEmpresaPMMD.addElement(new Paragraph("Juliaca, Puno - Perú",
            FontFactory.getFont(FontFactory.HELVETICA, 9)));
        tablaPMMD.addCell(celdaEmpresaPMMD);


        PdfPCell celdaContactoPMMD = new PdfPCell();
        celdaContactoPMMD.setHorizontalAlignment(Element.ALIGN_RIGHT);
        celdaContactoPMMD.addElement(new Paragraph("PRESUPUESTO",
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        celdaContactoPMMD.addElement(new Paragraph("Fecha: " + LocalDate.now().format(FECHA_FMTPMMD),
            FontFactory.getFont(FontFactory.HELVETICA, 10)));
        tablaPMMD.addCell(celdaContactoPMMD);

        tablaPMMD.setSpacingBefore(10);
        tablaPMMD.setSpacingAfter(20);
        docPMMD.add(tablaPMMD);
    }

    private void agregarDatosPresupuestoPMMD(Document docPMMD, String nombreClientePMMD) throws DocumentException {
        Paragraph clientePMMD = new Paragraph("Cliente: " + nombreClientePMMD,
            FontFactory.getFont(FontFactory.HELVETICA, 11));
        clientePMMD.setSpacingAfter(10);
        docPMMD.add(clientePMMD);
    }

    private void agregarEspecificacionesPMMD(Document docPMMD, ConfigSofaPMMD configPMMD) throws DocumentException {
        docPMMD.add(new Paragraph("ESPECIFICACIONES DEL SOFÁ",
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));

        PdfPTable tablaPMMD = new PdfPTable(2);
        tablaPMMD.setWidthPercentage(100);
        tablaPMMD.setSpacingBefore(10);


        PdfPCell headerCellPMMD = new PdfPCell(new Paragraph("Propiedad",
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
        headerCellPMMD.setBackgroundColor(new Color(200, 200, 200));
        tablaPMMD.addCell(headerCellPMMD);

        headerCellPMMD = new PdfPCell(new Paragraph("Valor",
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
        headerCellPMMD.setBackgroundColor(new Color(200, 200, 200));
        tablaPMMD.addCell(headerCellPMMD);


        agregarFilaEspecificacionPMMD(tablaPMMD, "Modelo", configPMMD.modelo().nombre());
        agregarFilaEspecificacionPMMD(tablaPMMD, "Cuerpos", String.valueOf(configPMMD.cuerpos()));
        agregarFilaEspecificacionPMMD(tablaPMMD, "Largo", configPMMD.largo() + " cm");
        agregarFilaEspecificacionPMMD(tablaPMMD, "Fondo", configPMMD.profundidad() + " cm");
        agregarFilaEspecificacionPMMD(tablaPMMD, "Alto", configPMMD.alto() + " cm");
        agregarFilaEspecificacionPMMD(tablaPMMD, "Brazo", configPMMD.brazo().nombre());
        agregarFilaEspecificacionPMMD(tablaPMMD, "Patas", configPMMD.patas().nombre());
        agregarFilaEspecificacionPMMD(tablaPMMD, "Espuma", configPMMD.espuma().nombre());
        agregarFilaEspecificacionPMMD(tablaPMMD, "Mecanismo", configPMMD.mecanismo().nombre());
        if (configPMMD.conChaise()) {
            agregarFilaEspecificacionPMMD(tablaPMMD, "Chaise", configPMMD.chaise().nombre());
        }

        tablaPMMD.setSpacingAfter(20);
        docPMMD.add(tablaPMMD);
    }

    private void agregarFilaEspecificacionPMMD(PdfPTable tablaPMMD, String propiedad, String valor) {
        PdfPCell celda1PMMD = new PdfPCell(new Paragraph(propiedad,
            FontFactory.getFont(FontFactory.HELVETICA, 10)));
        tablaPMMD.addCell(celda1PMMD);

        PdfPCell celda2PMMD = new PdfPCell(new Paragraph(valor,
            FontFactory.getFont(FontFactory.HELVETICA, 10)));
        tablaPMMD.addCell(celda2PMMD);
    }

    private void agregarImagenPMMD(Document docPMMD, WritableImage imagenPMMD) throws DocumentException {
        try {

            File tempFilePMMD = File.createTempFile("sofa-", ".png");


            docPMMD.add(new Paragraph("\n"));
            docPMMD.add(new Paragraph("VISTA 3D DEL SOFÁ",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
            docPMMD.add(new Paragraph("(Imagen capturada del visor 3D)",
                FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9)));
        } catch (Exception e) {

        }
    }

    private void agregarDesglosePrecioPMMD(Document docPMMD, ConfigSofaPMMD configPMMD) throws DocumentException {
        docPMMD.add(new Paragraph("\nDESGLOSE DE PRECIOS",
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));

        CalculoPrecioServicePMMD.DesglosePrecio desglosePMMD = precioServicePMMD.desglosar(configPMMD, java.math.BigDecimal.ZERO);

        PdfPTable tablaPMMD = new PdfPTable(2);
        tablaPMMD.setWidthPercentage(50);
        tablaPMMD.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tablaPMMD.setSpacingBefore(10);

        agregarFilaPrecioPMMD(tablaPMMD, "Costo BOM", desglosePMMD.costoBOM());
        agregarFilaPrecioPMMD(tablaPMMD, "Costo M.O.", desglosePMMD.costoMO());
        agregarFilaPrecioPMMD(tablaPMMD, "Subtotal", desglosePMMD.conMargen());

        PdfPCell igvCellPMMD = new PdfPCell(new Paragraph("IGV (18%)",
            FontFactory.getFont(FontFactory.HELVETICA, 10)));
        tablaPMMD.addCell(igvCellPMMD);

        PdfPCell igvValorPMMD = new PdfPCell(new Paragraph("S/ " + desglosePMMD.igv(),
            FontFactory.getFont(FontFactory.HELVETICA, 10)));
        tablaPMMD.addCell(igvValorPMMD);


        PdfPCell totalCellPMMD = new PdfPCell(new Paragraph("TOTAL",
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
        totalCellPMMD.setBackgroundColor(new Color(200, 200, 200));
        tablaPMMD.addCell(totalCellPMMD);

        PdfPCell totalValorPMMD = new PdfPCell(new Paragraph("S/ " + desglosePMMD.precioFinal(),
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
        totalValorPMMD.setBackgroundColor(new Color(200, 200, 200));
        tablaPMMD.addCell(totalValorPMMD);

        tablaPMMD.setSpacingAfter(20);
        docPMMD.add(tablaPMMD);
    }

    private void agregarFilaPrecioPMMD(PdfPTable tablaPMMD, String concepto, java.math.BigDecimal montoPMMD) {
        PdfPCell celda1PMMD = new PdfPCell(new Paragraph(concepto,
            FontFactory.getFont(FontFactory.HELVETICA, 10)));
        tablaPMMD.addCell(celda1PMMD);

        PdfPCell celda2PMMD = new PdfPCell(new Paragraph("S/ " + montoPMMD,
            FontFactory.getFont(FontFactory.HELVETICA, 10)));
        tablaPMMD.addCell(celda2PMMD);
    }

    private void agregarTerminosPMMD(Document docPMMD) throws DocumentException {
        docPMMD.add(new Paragraph("\nTÉRMINOS Y CONDICIONES",
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));

        Paragraph terminosPMMD = new Paragraph(
            "Este presupuesto es válido por 7 días. Los precios incluyen IGV (18%). " +
            "Sujeto a disponibilidad de materiales y cambios en el mercado. " +
            "Se requiere 50% de adelanto para iniciar la fabricación.",
            FontFactory.getFont(FontFactory.HELVETICA, 9));
        terminosPMMD.setSpacingBefore(5);
        docPMMD.add(terminosPMMD);
    }

    public void guardarPresupuesto(ConfigSofaPMMD configPMMD, String nombreClientePMMD, UsuarioPMMD usuarioPMMD,
                                   String rutaArchivo, WritableImage imagenSofaPMMD) {
        try {
            byte[] pdfPMMD = generarPresupuesto(configPMMD, nombreClientePMMD, usuarioPMMD, imagenSofaPMMD);
            FileOutputStream fosPMMD = new FileOutputStream(rutaArchivo);
            fosPMMD.write(pdfPMMD);
            fosPMMD.close();
        } catch (Exception e) {
            throw new RuntimeException("Error guardando PDF: " + e.getMessage(), e);
        }
    }
}
