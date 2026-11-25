package pe.erp.sofas.ui.three.materials;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;













public class PBRMaterialPMMD extends PhongMaterial {

    private Image diffuseMapPMMD;

    private Image specularMapPMMD;

    private Image normalMapPMMD;

    public PBRMaterialPMMD(Color diffuseColorPMMD) {
        super(diffuseColorPMMD);
    }






    public PBRMaterialPMMD conDiffuseMap(Image mapaPMMD) {
        this.diffuseMapPMMD = mapaPMMD;
        this.setDiffuseMap(mapaPMMD);
        return this;
    }











    public PBRMaterialPMMD conSpecularMap(Image mapaPMMD) {
        this.specularMapPMMD = mapaPMMD;
        this.setSpecularMap(mapaPMMD);
        return this;
    }












    public PBRMaterialPMMD conNormalMap(Image mapaPMMD) {
        this.normalMapPMMD = mapaPMMD;
        this.setBumpMap(mapaPMMD);
        return this;
    }





    public PBRMaterialPMMD conSpecularPower(double powerPMMD) {
        this.setSpecularPower(powerPMMD);
        return this;
    }









    public PBRMaterialPMMD conSpecularColor(Color color) {
        this.setSpecularColor(color);
        return this;
    }



    public Image getDiffuseMapLocal() {
        return diffuseMapPMMD;
    }

    public Image getSpecularMapLocal() {
        return specularMapPMMD;
    }

    public Image getNormalMapLocal() {
        return normalMapPMMD;
    }






    public static PBRMaterialPMMD crearCuero(Color color) {
        PBRMaterialPMMD mPMMD = new PBRMaterialPMMD(color);
        mPMMD.setSpecularPower(40);
        mPMMD.setSpecularColor(Color.color(0.6, 0.55, 0.5));
        return mPMMD;
    }






    public static PBRMaterialPMMD crearTerciopelo(Color color) {
        PBRMaterialPMMD mPMMD = new PBRMaterialPMMD(color);
        mPMMD.setSpecularPower(25);
        mPMMD.setSpecularColor(Color.color(0.7, 0.7, 0.8));
        return mPMMD;
    }






    public static PBRMaterialPMMD crearTela(Color color) {
        PBRMaterialPMMD mPMMD = new PBRMaterialPMMD(color);
        mPMMD.setSpecularPower(8);
        mPMMD.setSpecularColor(Color.color(0.3, 0.3, 0.3));
        return mPMMD;
    }
}
