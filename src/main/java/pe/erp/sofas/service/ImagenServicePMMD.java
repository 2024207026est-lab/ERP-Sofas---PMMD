package pe.erp.sofas.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pe.erp.sofas.core.NegocioExceptionPMMD;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Locale;





@Service
public class ImagenServicePMMD {

    private final File carpeta;

    public ImagenServicePMMD(@Value("${app.db.dir:${user.home}/.innovasur}") String dir) {
        this.carpeta = new File(dir, "images");
        carpeta.mkdirs();
    }

    public File carpeta() {
        return carpeta;
    }

    public File archivo(String idPMMD) {
        return new File(carpeta, idPMMD + ".jpg");
    }



    public String guardar(File origenPMMD, String idPMMD, int ancho, int alto) {
        String nPMMD = origenPMMD.getName().toLowerCase(Locale.ROOT);
        if (!(nPMMD.endsWith(".jpg") || nPMMD.endsWith(".jpeg") || nPMMD.endsWith(".png") || nPMMD.endsWith(".webp"))) {
            throw new NegocioExceptionPMMD("Formato no soportado: usa JPG, PNG o WebP.");
        }
        BufferedImage imgPMMD;
        try {
            imgPMMD = ImageIO.read(origenPMMD);
        } catch (IOException ePMMD) {
            imgPMMD = null;
        }
        if (imgPMMD == null) {
            throw new NegocioExceptionPMMD(nPMMD.endsWith(".webp")
                    ? "Este equipo no puede leer WebP; convierte la foto a JPG o PNG."
                    : "No se pudo leer la imagen.");
        }
        BufferedImage recortePMMD = cover(imgPMMD, ancho, alto);
        File destinoPMMD = archivo(idPMMD);
        try {
            ImageIO.write(recortePMMD, "jpg", destinoPMMD);
        } catch (IOException ePMMD) {
            throw new NegocioExceptionPMMD("No se pudo guardar la imagen: " + ePMMD.getMessage());
        }
        return destinoPMMD.getAbsolutePath();
    }

    public static BufferedImage cover(BufferedImage imgPMMD, int ancho, int alto) {
        double ratioPMMD = ancho / (double) alto;
        int wPMMD = imgPMMD.getWidth(), hPMMD = imgPMMD.getHeight();
        int vwPMMD = (int) Math.min(wPMMD, Math.round(hPMMD * ratioPMMD));
        int vhPMMD = (int) Math.round(vwPMMD / ratioPMMD);
        int xPMMD = (wPMMD - vwPMMD) / 2, yPMMD = (hPMMD - vhPMMD) / 2;
        BufferedImage outPMMD = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
        Graphics2D gPMMD = outPMMD.createGraphics();
        gPMMD.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        gPMMD.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        gPMMD.setColor(Color.WHITE);
        gPMMD.fillRect(0, 0, ancho, alto);
        gPMMD.drawImage(imgPMMD, 0, 0, ancho, alto, xPMMD, yPMMD, xPMMD + vwPMMD, yPMMD + vhPMMD, null);
        gPMMD.dispose();
        return outPMMD;
    }

    public void eliminar(String ruta) {
        if (ruta == null) return;
        File fPMMD = new File(ruta);
        if (fPMMD.getParentFile() != null && fPMMD.getParentFile().equals(carpeta)) fPMMD.delete();
    }
}
